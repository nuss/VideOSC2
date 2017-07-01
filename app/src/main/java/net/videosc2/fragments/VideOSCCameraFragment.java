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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.videosc2.R;
import net.videosc2.activities.VideOSCMainActivity;
import net.videosc2.db.SettingsContract;
import net.videosc2.utilities.VideOSCUIHelpers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import jp.co.cyberagent.android.gpuimage.GPUImageNativeLibrary;

import static android.hardware.Camera.Parameters.PREVIEW_FPS_MAX_INDEX;
import static android.hardware.Camera.Parameters.PREVIEW_FPS_MIN_INDEX;

/**
 * Display the down-scaled preview, calculated
 * from the smallest possible preview size
 * Created by Stefan Nussbaumer
 * after a piece of code by
 * Rex St. John (on behalf of AirPair.com) on 3/4/14.
 */
public class VideOSCCameraFragment extends VideOSCBaseFragment {
	final static String TAG = "VideOSCCameraFragment";

	private long mNow, mPrev = 0;
	private float mFrameRate;

	// Native camera.
	public Camera mCamera;

	// View to display the camera output.
	private CameraPreview mPreview;
	// preview container
	private ViewGroup mPreviewContainer;

	// Reference to the ImageView containing the downscaled video frame
	private ImageView mImage;

	/**
	 * Default empty constructor.
	 */
	public VideOSCCameraFragment() {
		super();
	}

	public float mCamZoom = 1f;

	private Point mResolution = new Point();

	private boolean isFramerateFixed;

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
		Log.d(TAG, "onCreateView: " + view.getClass());
		// store the container for later re-use
		mPreviewContainer = container;
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
	public boolean safeCameraOpenInView(View view) {
		boolean qOpened;
		// cache current zoom
		int zoom = mPreview != null ? mPreview.getCurrentZoom() : 0;
		releaseCameraAndPreview();
		mCamera = getCameraInstance();
		Log.d(TAG, "which camera: " + VideOSCMainActivity.currentCameraID + ", camera: " + mCamera);
		FrameLayout preview;

		qOpened = (mCamera != null);
		Log.d(TAG, "qOpened: " + qOpened);

		if (qOpened) {
			if (mPreview == null) {
				mPreview = new CameraPreview(getActivity().getApplicationContext(), mCamera);
				if (view.findViewById(R.id.camera_preview) != null) {
					preview = (FrameLayout) view.findViewById(R.id.camera_preview);
					preview.addView(mPreview);
				} else Log.d(TAG, "FrameLayout is null");
			} else {
				mPreview.switchCamera(mCamera);
				// set camera zoom to the zoom value of the old camera
				mPreview.setZoom(zoom);
			}
			mPreview.pPreviewStarted = mPreview.startCameraPreview();
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
			c = Camera.open(VideOSCMainActivity.currentCameraID); // attempt to get a Camera instance
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
			Log.d(TAG, "camera.release now");
			mCamera.release();
			mCamera = null;
		}
		if (mPreview != null) {
			mPreview.destroyDrawingCache();
			mPreview.pCamera = null;
		}
	}

	/**
	 * Get the camera associated with the fragment
	 * @return a Camera instance
	 */
	public Camera getCamera() {
		return mCamera;
	}

	public void setResolution(int width, int height) {
		mResolution.set(width, height);
	}

	public Point getResolution() {
		return mResolution;
	}

	public void setFramerateFixed(int isFixed) {
		isFramerateFixed = isFixed > 0;
	}

	public boolean getFramerateFixed() {
		return isFramerateFixed;
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

		private boolean pPreviewStarted;
		private double mOldFingerDistance = 0.0;

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

			// get initial settings from database
			String[] settingsFields = new String[]{
					SettingsContract.SettingsEntries.RES_H,
					SettingsContract.SettingsEntries.RES_V,
					SettingsContract.SettingsEntries.FRAMERATE_FIXED
			};
			SQLiteDatabase db = VideOSCMainActivity.mDbHelper.getReadableDatabase();

			Cursor cursor = db.query(
					SettingsContract.SettingsEntries.TABLE_NAME,
					settingsFields,
					null,
					null,
					null,
					null,
					null
			);

			if (cursor.moveToFirst()) {
				setResolution(
						cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.RES_H)),
						cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.RES_V))
				);
				setFramerateFixed(
						cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.FRAMERATE_FIXED))
				);
			}

			cursor.close();

		}

		/**
		 * switch backside to frontside camera and vice versa
		 * called within safeCameraOpenInView(View view)
		 *
		 * @param camera an instance of Camera
		 */
		public void switchCamera(Camera camera) {
			Log.d(TAG, "switch camera, pCamera: " + camera);
			pCamera = camera;
			mHolder.removeCallback(this);
			ViewGroup parent = (ViewGroup) mPreview.getParent();
			// cache new preview locally and remove old preview later
			// removing old preview immediately caused surfaceDestroyed to be called
			// and switching wasn't finished but ONLY when switching back from front- to backside
			// camera...
			CameraPreview preview = new CameraPreview(getContext(), camera);
			SurfaceHolder holder = preview.getHolder();
			holder.addCallback(this);
			parent.addView(preview);
			parent.removeView(mPreview);
			mPreview = preview;
			mHolder = holder;
		}

		/**
		 * start the camera preview
		 */
		public boolean startCameraPreview() {
			boolean started = false;
			try {
				pCamera.setPreviewDisplay(mHolder);
				pCamera.startPreview();
				Log.d(TAG, "pCamera: " + pCamera);
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
			Camera.Parameters parameters = camera.getParameters();
//			Log.d(TAG, "set camera, parameters: " + parameters.flatten());
			// Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
			mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
			mPreviewSize = getSmallestPreviewSize(mSupportedPreviewSizes);

			Log.d(TAG, "preview size: " + mPreviewSize.width + " x " + mPreviewSize.height);

			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			mSupportedFlashModes = parameters.getSupportedFlashModes();

			// Set the camera to Auto Flash mode.
			if (mSupportedFlashModes != null && mSupportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
			}
			camera.setParameters(parameters);

			requestLayout();
		}

		/**
		 * The Surface has been created, now tell the camera where to draw the preview.
		 *
		 * @param holder the surface holder
		 */
		public void surfaceCreated(SurfaceHolder holder) {
			Log.d(TAG, "surfaceCreated");
			// reactivate the preview after app has been paused and resumed
			if (!pPreviewStarted) pCamera.startPreview();
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
			// prevent errors resulting from camera being used after Camera.release() has been
			// called. Seems to work...
			if (pCamera != null) try {
				pCamera.stopPreview();
				pPreviewStarted = false;
			} catch (Exception e) {
				e.printStackTrace();
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
/*
				for (int[] range : parameters.getSupportedPreviewFpsRange()) {
					Log.d(TAG, "supported preview fps ranges: " + range[0] + " : " + range[1]);
				}
*/
				List<int[]> previewFpsRange = parameters.getSupportedPreviewFpsRange();
				for (int[] range : previewFpsRange) {
					Log.d(TAG, "range: " + range[0] + " : " + range[1]);
				}
/*
				Log.d(TAG, "min: " + previewFpsRange.get(PREVIEW_FPS_MIN_INDEX)[0] +
						" : " + previewFpsRange.get(PREVIEW_FPS_MIN_INDEX)[1] +
						", max: " + previewFpsRange.get(PREVIEW_FPS_MAX_INDEX)[0] +
						" : " + previewFpsRange.get(PREVIEW_FPS_MAX_INDEX)[1]
				);
*/
				if (getFramerateFixed())
//					parameters.setPreviewFpsRange(
//							previewFpsRange.get(PREVIEW_FPS_MAX_INDEX)[0],
//							previewFpsRange.get(PREVIEW_FPS_MAX_INDEX)[1]
//					);
					parameters.setPreviewFpsRange(30000, 30000);
				else // parameters.setPreviewFpsRange(
//						previewFpsRange.get(PREVIEW_FPS_MIN_INDEX)[0],
//						previewFpsRange.get(PREVIEW_FPS_MAX_INDEX)[1]
//				);
					parameters.setPreviewFpsRange(7000, 30000);
//				parameters.setAntibanding(Camera.Parameters.ANTIBANDING_OFF);
				// Set the auto-focus mode to "continuous"
//				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				pCamera.setParameters(parameters);
				pCamera.setPreviewCallback(new Camera.PreviewCallback() {
					@Override
					public void onPreviewFrame(byte[] data, Camera camera) {
						mNow = System.currentTimeMillis();
						mFrameRate = Math.round(1000.0f/(mNow - mPrev) * 10.0f) / 10.0f;
						mPrev = mNow;
						TextView frameRateText = (TextView) mPreviewContainer.findViewById(R.id.fps);
						if (frameRateText != null) frameRateText.setText(String.format(Locale.getDefault(), "%.1f", mFrameRate));
						TextView zoomText = (TextView) mPreviewContainer.findViewById(R.id.zoom);
						if (zoomText != null) zoomText.setText(String.format(Locale.getDefault(), "%.1f", mCamZoom));
						int outWidth = getResolution().x;
						int outHeight = getResolution().y;
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

		@Override
		public boolean onTouchEvent(MotionEvent motionEvent) {
			Camera.Parameters params = pCamera.getParameters();

			if (motionEvent.getPointerCount() > 1 && params.isZoomSupported()) {
				int zoom = params.getZoom();

				if (mOldFingerDistance == 0.0)
					mOldFingerDistance = getFingerSpacing(motionEvent);
				double currFingerDistance = getFingerSpacing(motionEvent);

				if (mOldFingerDistance != currFingerDistance) {
					if (mOldFingerDistance < currFingerDistance) {
						if (zoom + 1 <= params.getMaxZoom()) zoom++;
					} else {
						if (zoom - 1 >= 0) zoom--;
					}
					params.setZoom(zoom);
					mOldFingerDistance = currFingerDistance;
					pCamera.setParameters(params);
//					Log.d(TAG, "zoom: " + params.getZoom() + ", ratio: " + params.getZoomRatios().get(params.getZoom()));
					mCamZoom = (float) (params.getZoomRatios().get(params.getZoom())/100.0);
				}
			}

			return true;
		}

		/** Determine the space between the first two fingers */
		private double getFingerSpacing(MotionEvent event) {
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);

			return Math.sqrt(x * x + y * y);
		}

		/** get current zoom */
		private int getCurrentZoom() {
			Camera.Parameters params = pCamera.getParameters();
			return params.getZoom();
		}

		/** set zoom */
		private void setZoom(int zoom) {
			Camera.Parameters params = pCamera.getParameters();
			params.setZoom(zoom);
			pCamera.setParameters(params);
		}

/*
		private List<int[]> getOptimalPreviewFramerates(Camera.Parameters params) {
			List<int[]> previewFpsRanges = params.getSupportedPreviewFpsRange();
			int[] spans = {};

			for (int i = 0; i < previewFpsRanges.size(); i++) {

			}
		}
*/
	}
}
