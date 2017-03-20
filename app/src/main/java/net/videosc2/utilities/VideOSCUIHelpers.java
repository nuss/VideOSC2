package net.videosc2.utilities;

import android.hardware.Camera;
import android.util.Log;

import java.util.List;

/**
 * Created by stefan on 18.03.17.
 */

public class VideOSCUIHelpers {
	final static String TAG = "VideOSCUIHelpers";

	/**
	 * Does the device have a torch?
	 *
	 * @return a boolean indicating whether the device has an inbuilt flashlight
	 */
	public static boolean hasTorch() {
		Camera camera = null;

		try {
			camera = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			e.printStackTrace();
		}


		if (camera == null) {
			return false;
		}

		Camera.Parameters parameters = camera.getParameters();
		camera.release();

		if (parameters.getFlashMode() == null) {
			return false;
		}

		List<String> supportedFlashModes = parameters.getSupportedFlashModes();

		return !(supportedFlashModes == null || supportedFlashModes.isEmpty()) && supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH);
	}
}
