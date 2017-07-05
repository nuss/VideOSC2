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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
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
import net.videosc2.VideOSCApplication;
import net.videosc2.activities.VideOSCMainActivity;
import net.videosc2.db.SettingsContract;
import net.videosc2.utilities.VideOSCUIHelpers;
import net.videosc2.utilities.enums.RGBModes;

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

	private VideOSCApplication mApp;

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
		mApp = (VideOSCApplication) getActivity().getApplication();
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
		private Point mPixelSize = new Point();

		// lock the state of a pixel after changing its state, otherwise pixels would constantly
		// change their state as long as they're hoevered
		private ArrayList<Boolean[]> lockList = new ArrayList<Boolean[]>();
		// store the states of all pixels
		private ArrayList<Boolean[]> offPxls = new ArrayList<Boolean[]>();

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

			// triplets of booleans, to be added to lockList, offPxls;
			Point res = getResolution();
			Boolean[] falses = {false, false, false};
			for (int i = 0; i < res.x * res.y; i++) {
				lockList.add(falses.clone());
				offPxls.add(falses.clone());
			}
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

			setPixelSize();
			Log.d(TAG, "size: " + getPixelSize());

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
//				for (int[] range : previewFpsRange) {
//					Log.d(TAG, "range: " + range[0] + " : " + range[1]);
//				}
/*
				Log.d(TAG, "min: " + previewFpsRange.get(PREVIEW_FPS_MIN_INDEX)[0] +
						" : " + previewFpsRange.get(PREVIEW_FPS_MIN_INDEX)[1] +
						", max: " + previewFpsRange.get(PREVIEW_FPS_MAX_INDEX)[0] +
						" : " + previewFpsRange.get(PREVIEW_FPS_MAX_INDEX)[1]
				);
*/
				int[] frameRates = getOptimalPreviewFramerates(parameters);
				if (getFramerateFixed())
					parameters.setPreviewFpsRange(frameRates[1], frameRates[1]);
				else
					parameters.setPreviewFpsRange(frameRates[0], frameRates[1]);

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
//						bmp = Bitmap.createScaledBitmap(bmp, outWidth, outHeight, true);
						bmp = drawFrame(Bitmap.createScaledBitmap(bmp, outWidth, outHeight, true), outWidth, outHeight);
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

			if (motionEvent.getAction() == MotionEvent.ACTION_MOVE)
				Log.d(TAG, "x: " + motionEvent.getX() + ", y: " + motionEvent.getY() + ", pressure: " + motionEvent.getPressure());

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

			Log.d(TAG, "current pixel: " + getHoverPixel(motionEvent.getX(), motionEvent.getY()));

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

		/* get min and max framerate (where max <= 30 fps) */
		private int[] getOptimalPreviewFramerates(Camera.Parameters params) {
			List<int[]> previewFpsRanges = params.getSupportedPreviewFpsRange();
			List<Integer> maxs = new ArrayList<>();
			List<Integer> mins = new ArrayList<>();

			for (int[] range : previewFpsRanges) {
				if (range[1] <= 30000) {
					mins.add(range[0]);
					maxs.add(range[1]);
				}
			}

			return new int[]{
				mins.get(mins.indexOf(Collections.min(mins))),
				maxs.get(maxs.indexOf(Collections.max(maxs)))
			};
		}

		private void setPixelSize() {
			Rect surfaceFrame = mHolder.getSurfaceFrame();
			Point resolution = getResolution();
			mPixelSize.x = surfaceFrame.width() / resolution.x;
			mPixelSize.y = surfaceFrame.height() / resolution.y;
		}

		private Point getPixelSize() {
			return mPixelSize;
		}

		private int getHoverPixel(float x, float y) {
			int hIndex = (int) x / mPixelSize.x;
			int vIndex = (int) y / mPixelSize.y;

			return vIndex * getResolution().x + hIndex;
		}

		private Bitmap drawFrame(Bitmap bmp, int width, int height) {
			float rval, gval, bval, alpha;
			int dimensions = getResolution().x * getResolution().y;
			int[] pixels = new int[width * height];
			bmp.getPixels(pixels, 0, width, 0, 0, width, height);

			for (int i = 0; i < dimensions; i++) {
				// only the downsampled image gets inverted as inverting the original would slow
				// down the application considerably
				int rVal = (!mApp.getIsRGBPositive()) ? 0xFF - ((pixels[i] >> 16) & 0xFF)
						: (pixels[i] >> 16) & 0xFF;
				int gVal = (!mApp.getIsRGBPositive()) ? 0xFF - ((pixels[i] >> 8) & 0xFF)
						: (pixels[i] >> 8) & 0xFF;
				int bVal = (!mApp.getIsRGBPositive()) ? 0xFF - (pixels[i] & 0xFF)
						: pixels[i] & 0xFF;

				if (!mApp.getIsRGBPositive())
					pixels[i] = Color.argb(255, rVal, gVal, bVal);

				// compose basic OSC message for slot
/*
				oscR = VideOSCOscHandling.makeMessage(oscR, r + str(i + 1));
				oscG = VideOSCOscHandling.makeMessage(oscG, g + str(i + 1));
				oscB = VideOSCOscHandling.makeMessage(oscB, b + str(i + 1));
*/

				if (mApp.getColorMode().equals(RGBModes.RGB)) {
					if (offPxls.get(i)[0] && !offPxls.get(i)[1] && !offPxls.get(i)[2]) {
						// r
//						alpha = pCamera.isStarted() ? 255 / 3 : 0;
						pixels[i] = Color.argb(255/3, 0, gVal, bVal);
					} else if (!offPxls.get(i)[0] && offPxls.get(i)[1] && !offPxls.get(i)[2]) {
						// g;
//						alpha = cam.isStarted() ? 255 / 3 : 0;
						pixels[i] = Color.argb(255/3, rVal, 0, bVal);
					} else if (!offPxls.get(i)[0] && !offPxls.get(i)[1] && offPxls.get(i)[2]) {
						// b;
//						alpha = cam.isStarted() ? 255 / 3 : 0;
						pixels[i] = Color.argb(255/3, rVal, gVal, 0);
					} else if (offPxls.get(i)[0] && offPxls.get(i)[1] && !offPxls.get(i)[2]) {
						// rg;
//						alpha = cam.isStarted ? 255 / 3 * 2 : 0;
						pixels[i] = Color.argb(255/3*2, 0, 0, bVal);
					} else if (offPxls.get(i)[0] && !offPxls.get(i)[1] && offPxls.get(i)[2]) {
						// rb;
//						alpha = cam.isStarted() ? 255 / 3 * 2 : 0;
						pixels[i] = Color.argb(255/3*2, 0, gVal, 0);
					} else if (!offPxls.get(i)[0] && offPxls.get(i)[1] && offPxls.get(i)[2]) {
						// bg;
//						alpha = cam.isStarted() ? 255 / 3 * 2 : 0;
						pixels[i] = Color.argb(255/3*2, rVal, 0, 0);
					} else if (offPxls.get(i)[0] && offPxls.get(i)[1] && offPxls.get(i)[2]) {
						// rgb
						pixels[i] = Color.argb(0, 0, 0, 0);
					}
				} else if (mApp.getColorMode().equals(RGBModes.R)) {
					if (offPxls.get(i)[0])
						pixels[i] = Color.argb(255, rVal, 255, 255);
					else
						pixels[i] = Color.argb(255, rVal, 0, 0);
				} else if (mApp.getColorMode().equals(RGBModes.G)) {
					if (offPxls.get(i)[1])
						pixels[i] = Color.argb(255, 255, gVal, 255);
					else
						pixels[i] = Color.argb(255, 0, gVal, 0);
				} else if (mApp.getColorMode().equals(RGBModes.B)) {
					if (offPxls.get(i)[2])
						pixels[i] = Color.argb(255, 255, 255, bVal);
					else
						pixels[i] = Color.argb(255, 0, 0, bVal);
				}

/*
				if (play) {
					if (calcsPerPeriod == 1) {
						if (normalize) {
							rval = (float) rVal / 255;
							gval = (float) gVal / 255;
							bval = (float) bVal / 255;
						} else {
							rval = rVal;
							gval = gVal;
							bval = bVal;
						}

						if (!offPxls.get(i)[0]) {
							oscR.add(rval);
							oscP5.send(oscR, broadcastLoc);
						}
						if (!offPxls.get(i)[1]) {
							oscG.add(gval);
							oscP5.send(oscG, broadcastLoc);
						}
						if (!offPxls.get(i)[2]) {
							oscB.add(bval);
							oscP5.send(oscB, broadcastLoc);
						}
					} else {
						curInput[0] = (float) rVal;
						curInput[1] = (float) gVal;
						curInput[2] = (float) bVal;

						curInputList.add(curInput.clone());

						if (lastInputList.size() >= dimensions) {
							rval = lastInputList.get(i)[0];
							gval = lastInputList.get(i)[1];
							bval = lastInputList.get(i)[2];

							if (normalize) {
								rval = rval / 255;
								gval = gval / 255;
								bval = bval / 255;
							}

							if (!offPxls.get(i)[0]) {
								oscR.add(rval);
								oscP5.send(oscR, broadcastLoc);
							}
							if (!offPxls.get(i)[1]) {
								oscG.add(gval);
								oscP5.send(oscG, broadcastLoc);
							}
							if (!offPxls.get(i)[2]) {
								oscB.add(bval);
								oscP5.send(oscB, broadcastLoc);
							}

							float lastInputR = lastInputList.get(i)[0];
							float lastInputG = lastInputList.get(i)[1];
							float lastInputB = lastInputList.get(i)[2];

							slope[0] = (curInput[0] - lastInputR) / calcsPerPeriod;
							slope[1] = (curInput[1] - lastInputG) / calcsPerPeriod;
							slope[2] = (curInput[2] - lastInputB) / calcsPerPeriod;

							slopes.add(slope.clone());
						}
					}
				}
*/
			}

			bmp.setPixels(pixels, 0, width, 0, 0, width, height);

			return bmp;
		}

	}

}

