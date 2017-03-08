/*
 * Copyright (c) 2014 Rex St. John on behalf of AirPair.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.videosc2.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import net.videosc2.R;
import net.videosc2.activities.VideOSCMainActivity;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageNativeLibrary;

/**
 * Display the down-scaled preview, calculated
 * from the smallest possible preview size
 * Created by Stefan Nussbaumer
 * after a piece of code by
 * Rex St. John (on behalf of AirPair.com) on 3/4/14.
 */
public class VideOSCCameraFragment extends VideOSCBaseFragment {
	final static String TAG = "VideOSCCameraFragment";

	// Native camera.
	private Camera mCamera;

	// View to display the camera output.
	private CameraPreview mPreview;

	// Reference to the ImageView containing the downscaled video frame
	ImageView mImage;

	/**
	 * Default empty constructor.
	 */
	public VideOSCCameraFragment() {
		super();
	}

	/**
	 * OnCreateView fragment override
	 *
	 * @param inflater
	 * @param container
	 * @param savedInstanceState
	 * @return
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_native_camera, container, false);
		mImage = (ImageView) view.findViewById(R.id.camera_downscaled);

		// Create our Preview view and set it as the content of our activity.
		boolean opened = safeCameraOpenInView(view);

		if (!opened) {
			Log.d("CameraGuide", "Error, Camera failed to open");
			return view;
		}

/*
		// Trap the capture button.
		Button captureButton = (Button) view.findViewById(R.id.button_capture);
		captureButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// get an image from the camera
						mCamera.takePicture(null, null, mPicture);
					}
				}
		);
*/

		return view;
	}

	/**
	 * Recommended "safe" way to open the camera.
	 *
	 * @param view
	 * @return
	 */
	private boolean safeCameraOpenInView(View view) {
		boolean qOpened;
		releaseCameraAndPreview();
		mCamera = getCameraInstance();
		qOpened = (mCamera != null);

		if (qOpened) {
			mPreview = new CameraPreview(getActivity().getBaseContext(), mCamera, view);
			FrameLayout preview = (FrameLayout) view.findViewById(R.id.camera_preview);
			preview.addView(mPreview);
			mPreview.startCameraPreview();
		}
		return qOpened;
	}

	/**
	 * Safe method for getting a camera instance.
	 *
	 * @return Camera instance
	 */
	public static Camera getCameraInstance() {
		Camera c = null;

		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c; // returns null if camera is unavailable
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		releaseCameraAndPreview();
	}

	/**
	 * Clear any existing preview / camera.
	 */
	private void releaseCameraAndPreview() {

		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
		if (mPreview != null) {
			mPreview.destroyDrawingCache();
			mPreview.mCamera = null;
		}
	}

	/**
	 * Surface on which the camera projects it's capture results. This is derived both from Google's docs and the
	 * excellent StackOverflow answer provided below.
	 * <p>
	 * Reference / Credit: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
	 */
	class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

		// SurfaceHolder
		private SurfaceHolder mHolder;

		// Our Camera.
		private Camera mCamera;

		// Camera Sizing (For rotation, orientation changes)
		private Camera.Size mPreviewSize;

		// List of supported preview sizes
		private List<Camera.Size> mSupportedPreviewSizes;

		// Flash modes supported by this camera
		private List<String> mSupportedFlashModes;

		public CameraPreview(Context context, Camera camera, View cameraView) {
			super(context);

			// Capture the context
			setCamera(camera);

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			mHolder.setKeepScreenOn(true);
			// deprecated setting, but required on Android versions prior to 3.0
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

			DisplayMetrics dm = new DisplayMetrics();
			WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			display.getSize(VideOSCMainActivity.dimensions);
		}

		/**
		 * Begin the preview of the camera input.
		 */
		public void startCameraPreview() {
			Log.d(TAG, "start camera preview, size: " + mCamera.getParameters().getPreviewSize().width + ", " + mCamera.getParameters().getPreviewSize().height);
			try {
				mCamera.setPreviewDisplay(mHolder);
				mCamera.startPreview();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * Extract supported preview and flash modes from the camera.
		 *
		 * @param camera
		 */
		private void setCamera(Camera camera) {
			Log.d(TAG, "set camera");
			mCamera = camera;
			Camera.Parameters parameters = mCamera.getParameters();
			// Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
			mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
			mPreviewSize = getSmallestPreviewSize(mSupportedPreviewSizes);
			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			mSupportedFlashModes = parameters.getSupportedFlashModes();

			// Set the camera to Auto Flash mode.
			if (mSupportedFlashModes != null && mSupportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
				mCamera.setParameters(parameters);
			}

			requestLayout();
		}

		/**
		 * The Surface has been created, now tell the camera where to draw the preview.
		 *
		 * @param holder
		 */
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Dispose of the camera preview.
		 *
		 * @param holder
		 */
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mCamera != null) {
				mCamera.stopPreview();
			}
		}

		/**
		 * React to surface changed events
		 *
		 * @param holder the surface holder
		 * @param format
		 * @param w
		 * @param h
		 */
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			// If your preview can change or rotate, take care of those events here.
			// Make sure to stop the preview before resizing or reformatting it.

			if (mHolder.getSurface() == null) {
				// preview surface does not exist
				return;
			}

			// stop preview before making changes
			try {
				Camera.Parameters parameters = mCamera.getParameters();

				// Set the auto-focus mode to "continuous"
				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				mCamera.setParameters(parameters);
				Log.d(TAG, "past setParameters");
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.outWidth = 6;
				options.outHeight = 4;
//				options.inMutable = true;
//				options.inDither = false;
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//				options.outMimeType = "image/png";
				mCamera.setPreviewCallback(new Camera.PreviewCallback() {
					@Override
					public void onPreviewFrame(byte[] data, Camera camera) {
						int[] out = new int[mPreviewSize.width * mPreviewSize.height];
						GPUImageNativeLibrary.YUVtoRBGA(data, mPreviewSize.width, mPreviewSize.height, out);
						Bitmap bmp = Bitmap.createBitmap(mPreviewSize.width, mPreviewSize.height, options.inPreferredConfig);
						bmp.copyPixelsFromBuffer(IntBuffer.wrap(out));
						bmp = Bitmap.createScaledBitmap(bmp, options.outWidth, options.outHeight, true);
						BitmapDrawable bmpDraw = new BitmapDrawable(bmp);
						bmpDraw.setAntiAlias(false);
						bmpDraw.setDither(false);
						bmpDraw.setFilterBitmap(false);
//						Log.d(TAG, "is bitmap mutable: " + bmp.isMutable());
//						bmp = Bitmap.createScaledBitmap(bmp, VideOSCMainActivity.dimensions.x, VideOSCMainActivity.dimensions.y, false);
						mImage.bringToFront();
						mImage.setImageDrawable(bmpDraw);
					}
				});
			} catch (Exception e) {
				Log.d(TAG, "exception: " + e);
				e.printStackTrace();
			}
		}


		/**
		 * @param sizes
		 * @return the smallest possible preview size
		 */
		private Camera.Size getSmallestPreviewSize(List<Camera.Size> sizes) {
			// Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
			Camera.Size optimalSize = null;

			for (int i = 1; i < sizes.size(); i++) {
				if (sizes.get(i).width * sizes.get(i).width < sizes.get(i - 1)
						.width * sizes.get(i - 1).height) {
					optimalSize = sizes.get(i);
				}
			}

			return optimalSize;
		}
	}
}
