/*
 * Copyright 2011 Colin McDonough
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.videosc2.utilities.torch;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

/*
 * Torch is an LED flashlight.
 */
public class Torch extends Activity {

	private static final String TAG = Torch.class.getSimpleName();

	private Camera mCamera;
	private boolean lightOn;
	private boolean previewOn;

	private WakeLock wakeLock;

	private Torch torch;

	public Torch() {
		super();
		torch = this;
	}

	public Torch getTorch() {
		return torch;
	}

	private void getCamera() {
		if (mCamera == null) {
			try {
				mCamera = Camera.open();
			} catch (RuntimeException e) {
				Log.i(TAG, "Camera.open() failed: " + e.getMessage());
			}
		}
	}

	/*
	 * Called by the view (see main.xml)
	 */
	public void toggleLight(View view) {
		toggleLight();
	}

	private void toggleLight() {
		if (lightOn) {
			turnLightOff();
		} else {
			turnLightOn();
		}
	}

	private void turnLightOn() {
		if (mCamera == null) {
			return;
		}
		lightOn = true;
		Parameters parameters = mCamera.getParameters();

		List<String> flashModes = parameters.getSupportedFlashModes();
		// Check if camera flash exists
		if (flashModes == null) {
			return;
		}
		String flashMode = parameters.getFlashMode();
		Log.i(TAG, "Flash mode: " + flashMode);
		Log.i(TAG, "Flash modes: " + flashModes);
		if (!Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
			// Turn on the flash
			if (flashModes.contains(Parameters.FLASH_MODE_TORCH)) {
				parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
				mCamera.setParameters(parameters);
			} else {
				Log.e(TAG, "FLASH_MODE_TORCH not supported");
			}
		}
	}

	private void turnLightOff() {
		if (lightOn) {
			lightOn = false;
			if (mCamera == null) {
				return;
			}
			Parameters parameters = mCamera.getParameters();
			if (parameters == null) {
				return;
			}
			List<String> flashModes = parameters.getSupportedFlashModes();
			String flashMode = parameters.getFlashMode();
			// Check if camera flash exists
			if (flashModes == null) {
				return;
			}
			Log.i(TAG, "Flash mode: " + flashMode);
			Log.i(TAG, "Flash modes: " + flashModes);
			if (!Parameters.FLASH_MODE_OFF.equals(flashMode)) {
				// Turn off the flash
				if (flashModes.contains(Parameters.FLASH_MODE_OFF)) {
					parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
					mCamera.setParameters(parameters);
				} else {
					Log.e(TAG, "FLASH_MODE_OFF not supported");
				}
			}
		}
	}

	@Override
	public void onRestart() {
		super.onRestart();
		Log.i(TAG, "onRestart");
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i(TAG, "onStart");
		getCamera();
	}

	@Override
	public void onResume() {
		super.onResume();
		turnLightOn();
	}

	@Override
	public void onPause() {
		super.onPause();
		turnLightOff();
		Log.i(TAG, "onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
		;
		torch = null;
		Log.i(TAG, "onStop");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mCamera != null) {
			turnLightOff();
			mCamera.release();
		}
		Log.i(TAG, "onDestroy");
	}

}