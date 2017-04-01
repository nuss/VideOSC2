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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
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
import android.widget.TextView;

import net.videosc2.R;
import net.videosc2.activities.VideOSCMainActivity;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
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

	private static long now, prev = 0;
	private static float frameRate;

	// Native camera.
	public Camera mCamera;

	// View to display the camera output.
	private CameraPreview mPreview;
	// preview container
	private ViewGroup previewContainer;

	// Reference to the ImageView containing the downscaled video frame
	private ImageView mImage;

	/**
	 * Default empty constructor.
	 */
	public VideOSCCameraFragment() {
		super();
	}

	/**
	 * OnCreateView fragment override
	 *
	 * @param inflater the layout inflater inflating the layout for the view
	 * @param container the layout's container
	 * @param savedInstanceState a Bundle instance
	 * @return a View
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_native_camera, container, false);
		Log.d(TAG, "onCreateView: " + view);
		// store the container for later re-use
		previewContainer = container;
		mImage = (ImageView) view.findViewById(R.id.camera_downscaled);

		// Create our Preview view and set it as the content of our activity.
		boolean opened = safeCameraOpenInView(view);

		if (!opened) {
			Log.d("CameraGuide", "Error, Camera failed to open");
			return view;
		}

		return view;
	}

	/**
	 * Recommended "safe" way to open the camera.
	 *
	 * @param view the view on which the camera is going to be displayed to the user
	 * @return a boolean, indicating whether opening the camera was successful
	 */
	private boolean safeCameraOpenInView(View view) {
		boolean qOpened;
		releaseCameraAndPreview();
		mCamera = getCameraInstance();
		FrameLayout preview;

		qOpened = (mCamera != null);

		if (qOpened) {
			mPreview = new CameraPreview(getActivity().getApplicationContext(), mCamera);
			if (view.findViewById(R.id.camera_preview) != null) {
				preview = (FrameLayout) view.findViewById(R.id.camera_preview);
				preview.addView(mPreview, -1);
				Log.d(TAG, "camera in preview callback: " + mPreview.pCamera);
				mPreview.previewStarted = mPreview.startCameraPreview();
			} else Log.d(TAG, "FrameLayout is null");
		}
		return qOpened;
	}

	/**
	 * Safe method for getting a camera instance.
	 *
	 * @return Camera instance
	 */
	private static Camera getCameraInstance() {
		Camera c = null;

		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c; // returns null if camera is unavailable
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy called");
		releaseCameraAndPreview();
	}

	/**
	 * Clear any existing preview / camera.
	 */
	private void releaseCameraAndPreview() {
		if (mCamera != null) {
			mCamera.stopPreview();
			// hack: set a null callback as the already set callback
			// otherwise prevails even after camera.release() and
			// causes a crash on quit
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
		if (mPreview != null) {
			mPreview.destroyDrawingCache();
			mPreview.pCamera = null;
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
		private Camera pCamera;

		// Camera Sizing (For rotation, orientation changes)
		private Camera.Size mPreviewSize;

		// List of supported preview sizes
		private List<Camera.Size> mSupportedPreviewSizes;

		// Flash modes supported by this camera
		private List<String> mSupportedFlashModes;

		private boolean previewStarted;

		/**
		 *
		 * @param context the context of the application
		 * @param camera an instance of Camera, to be used throughout CameraPreview
		 */
		public CameraPreview(Context context, Camera camera) {
			super(context);

			Log.d(TAG, "CameraPreview(): " + camera);
			// Capture the context
			setCamera(camera);

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			mHolder.setKeepScreenOn(true);
			// deprecated setting, but required on Android versions prior to 3.0
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

			WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			display.getSize(VideOSCMainActivity.dimensions);
		}

		/**
		 * start the camera preview
		 */
		public boolean startCameraPreview() {
			boolean started = false;
			try {
				pCamera.setPreviewDisplay(mHolder);
				pCamera.startPreview();
				started = true;
			} catch (Exception e) {
				e.printStackTrace();
			}

			return started;
		}

		/**
		 * Extract supported preview and flash modes from the camera.
		 *
		 * @param camera an instance of Camera
		 */
		private void setCamera(Camera camera) {
			pCamera = camera;
			Log.d(TAG, "setCamera(), pCamera: " + pCamera + ", camera: " + camera);
//			Camera.Parameters parameters = mCamera.getParameters();
			Camera.Parameters parameters = camera.getParameters();
			// Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
			mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
			mPreviewSize = getSmallestPreviewSize(mSupportedPreviewSizes);
			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			mSupportedFlashModes = parameters.getSupportedFlashModes();

			// Set the camera to Auto Flash mode.
			if (mSupportedFlashModes != null && mSupportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
//				mCamera.setParameters(parameters);
				camera.setParameters(parameters);
			}

			requestLayout();
		}

		/**
		 * The Surface has been created, now tell the camera where to draw the preview.
		 *
		 * @param holder the surface holder
		 */
		public void surfaceCreated(SurfaceHolder holder) {
			// reactivate the preview after app has been pused and resumed
			if (!previewStarted) pCamera.startPreview();
			try {
				pCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Dispose of the camera preview.
		 *
		 * @param holder the surface holder
		 */
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d(TAG, "surfaceDestroyed");
			if (pCamera != null) {
				pCamera.stopPreview();
				previewStarted = false;
			}
		}

		/**
		 * React to surface changed events
		 *
		 * @param holder the surface holder
		 * @param format the pixel format of the surface
		 * @param w the surface width
		 * @param h the surface height
		 */
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			if (mHolder.getSurface() == null) {
				// preview surface does not exist
				return;
			}

			// stop preview before making changes
			try {
				final Camera.Parameters parameters = pCamera.getParameters();
				// FIXME: auto exposure correction seems to make the camera much slower
//				Log.d(TAG, "camera parameters: " + parameters.flatten());
//				if (parameters.isAutoExposureLockSupported()) {
//					parameters.setAutoExposureLock(true);
//				}
				parameters.setPreviewFpsRange(30000, 30000);
//				parameters.setAntibanding(Camera.Parameters.ANTIBANDING_OFF);
				// Set the auto-focus mode to "continuous"
//				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				pCamera.setParameters(parameters);
				pCamera.setPreviewCallback(new Camera.PreviewCallback() {
					@Override
					public void onPreviewFrame(byte[] data, Camera camera) {
						VideOSCCameraFragment.now = System.currentTimeMillis();
						frameRate = Math.round(1000.0f/(now - prev) * 10.0f) / 10.0f;
						VideOSCCameraFragment.prev = VideOSCCameraFragment.now;
						TextView frameRateText = (TextView) previewContainer.findViewById(R.id.fps);
						if (frameRateText != null) frameRateText.setText(String.format("%.1f", frameRate));

						int outWidth = 6;
						int outHeight = 4;
						Bitmap.Config inPreferredConfig = Bitmap.Config.ARGB_8888;
						int[] out = new int[mPreviewSize.width * mPreviewSize.height];
						GPUImageNativeLibrary.YUVtoRBGA(data, mPreviewSize.width, mPreviewSize.height, out);
						Bitmap bmp = Bitmap.createBitmap(mPreviewSize.width, mPreviewSize.height, inPreferredConfig);
						bmp.copyPixelsFromBuffer(IntBuffer.wrap(out));
						bmp = Bitmap.createScaledBitmap(bmp, outWidth, outHeight, true);
						BitmapDrawable bmpDraw = new BitmapDrawable(getResources(), bmp);
						bmpDraw.setAntiAlias(false);
						bmpDraw.setDither(false);
						bmpDraw.setFilterBitmap(false);
						mImage.bringToFront();
						mImage.setImageDrawable(bmpDraw);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		/**
		 * @param sizes the ArrayList of possible preview sizes
		 * @return the smallest possible preview size
		 */
		private Camera.Size getSmallestPreviewSize(List<Camera.Size> sizes) {
			Camera.Size optimalSize;
			ArrayList<Integer> productList = new ArrayList<>();

			for (Camera.Size size : sizes) {
				productList.add(size.width * size.height);
			}

			int minIndex = productList.indexOf(Collections.min(productList));
			optimalSize = sizes.get(minIndex);

			return optimalSize;
		}
	}
}
