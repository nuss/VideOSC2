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
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import net.videosc2.R;
import net.videosc2.VideOSCApplication;
import net.videosc2.activities.VideOSCMainActivity;
import net.videosc2.db.SettingsContract;
import net.videosc2.utilities.VideOSCDialogHelper;
import net.videosc2.utilities.VideOSCOscHandler;
import net.videosc2.utilities.VideOSCUIHelpers;
import net.videosc2.utilities.enums.RGBModes;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import jp.co.cyberagent.android.gpuimage.GPUImageNativeLibrary;
import oscP5.OscMessage;
import oscP5.OscP5;

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
	private Camera.Parameters mCameraParams;
	private List<int[]> mSupportedPreviewFpsRanges;


	// View to display the camera output.
	public CameraPreview mPreview;
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

	private int[] mFrameRateRange;

	private String mRed, mGreen, mBlue;

	private VideOSCApplication mApp;
	private static OscP5 mOscP5;


	/**
	 * OnCreateView fragment override
	 *
	 * @param inflater           the layout inflater inflating the layout for the view
	 * @param container          the layout's container
	 * @param savedInstanceState a Bundle instance
	 * @return a View
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		mApp = (VideOSCApplication) getActivity().getApplication();
		mOscP5 = mApp.mOscHelper.getOscP5();
//		Log.d(TAG, "send OSC to: " + mApp.mOscHelper.getBroadcastIP());
		View view = inflater.inflate(R.layout.fragment_native_camera, container, false);
		// store the container for later re-use
		mPreviewContainer = container;
		mImage = (ImageView) view.findViewById(R.id.camera_downscaled);

		// Create our Preview view and set it as the content of our activity.
		safeCameraOpenInView(view);

		return view;
	}

	/**
	 * Recommended "safe" way to open the camera.
	 *
	 * @param view the view on which the camera is going to be displayed to the user
	 */
	public void safeCameraOpenInView(View view) {
		FrameLayout preview;
		// cache current zoom
		int zoom = mPreview != null ? mPreview.getCurrentZoom() : 0;
		releaseCameraAndPreview();
		mCamera = getCameraInstance();
		if (mCamera != null) {
			mCameraParams = mCamera.getParameters();
			mSupportedPreviewFpsRanges = mCameraParams.getSupportedPreviewFpsRange();

			if (mPreview == null) {
				mPreview = new CameraPreview(getActivity().getApplicationContext(), mCamera);
				if (view.findViewById(R.id.camera_preview) != null) {
					preview = (FrameLayout) view.findViewById(R.id.camera_preview);
					preview.addView(mPreview);
					Log.d(TAG, "view found and set");
				} else Log.d(TAG, "FrameLayout is null");
			} else {
				mPreview.switchCamera(mCamera);
				// set camera zoom to the zoom value of the old camera
				mPreview.setZoom(zoom);
				Log.d(TAG, "switch camera");
			}
		} else {
			final Activity activity = getActivity();
			VideOSCDialogHelper.showDialog(
					activity,
					android.R.style.Theme_Holo_Light_Dialog,
					getString(R.string.msg_on_camera_open_fail),
					getString(R.string.OK),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							activity.finish();
						}

					}, null, null
			);
		}
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

	public void setResolution(int width, int height) {
		mResolution.set(width, height);
	}

	public Point getResolution() {
		return mResolution;
	}

	public void setColorOscCmds(String cmd) {
		mRed = String.format("/%1$s/red", cmd);
		mGreen = String.format("/%1$s/green", cmd);
		mBlue = String.format("/%1$s/blue", cmd);
	}

	public String[] getColorOscCmds() {
		return new String[]{mRed, mGreen, mBlue};
	}

	public void setFramerateRange(int index) {
		mFrameRateRange = mSupportedPreviewFpsRanges.get(index);
	}

	public int[] getFramerateRange() {
		return mFrameRateRange;
	}

	/*public interface OnCompleteCameraFragmentListener {
		void onCompleteCameraFragment();
	}

	OnCompleteCameraFragmentListener mListener;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		try {
			this.mListener = (OnCompleteCameraFragmentListener) context;
		} catch (final ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement OnCompleteListener");
		}
	}*/

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

		private double mOldFingerDistance = 0.0;
		private Point mPixelSize = new Point();

		// lock the state of a pixel after changing its state, otherwise pixels would constantly
		// change their state as long as they're hoevered
		private ArrayList<Boolean[]> lockList = new ArrayList<Boolean[]>();
		// store the states of all pixels
		private ArrayList<Boolean[]> offPxls = new ArrayList<Boolean[]>();
		final private Boolean[] falses = {false, false, false};

		private RedOscRunnable mRedOscRunnable;
		private GreenOscRunnable mGreenOscRunnable;
		private BlueOscRunnable mBlueOscRunnable;
		private Thread mRedOscSender;
		private Thread mGreenOscSender;
		private Thread mBlueOscSender;

		private volatile OscMessage oscR, oscG, oscB;

		// debugging
		private long mCountR = 0, mCountG = 0, mCountB = 0;

		/**
		 * @param context the context of the application
		 * @param camera  an instance of Camera, to be used throughout CameraPreview
		 */
		public CameraPreview(Context context, Camera camera) {
			super(context);

			Log.d(TAG, "CameraPreview(): " + camera);
			// Capture the context
			setCamera(camera);
			// explicitely trigger drawing (onDraw)
			setWillNotDraw(false);

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			mHolder.setKeepScreenOn(true);
			// deprecated setting, but required on Android versions prior to 3.0
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

			if (mRedOscSender == null) {
				mRedOscRunnable = new RedOscRunnable();
				RedOscRunnable.setOscHelper(mApp.mOscHelper);
				mRedOscSender = new Thread(mRedOscRunnable);
				mRedOscSender.start();
			}

			if (mGreenOscSender == null) {
				mGreenOscRunnable = new GreenOscRunnable();
				GreenOscRunnable.setOscHelper(mApp.mOscHelper);
				mGreenOscSender = new Thread(mGreenOscRunnable);
				mGreenOscSender.start();
			}

			if (mBlueOscSender == null) {
				mBlueOscRunnable = new BlueOscRunnable();
				BlueOscRunnable.setOscHelper(mApp.mOscHelper);
				mBlueOscSender = new Thread(mBlueOscRunnable);
				mBlueOscSender.start();
			}

			// ???
			/* WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			display.getSize(mApp.getDimensions()); */

			// get initial settings from database
			String[] settingsFields = new String[]{
					SettingsContract.SettingsEntries.RES_H,
					SettingsContract.SettingsEntries.RES_V,
					SettingsContract.SettingsEntries.FRAMERATE_RANGE,
					SettingsContract.SettingsEntries.ROOT_CMD
			};

			final SQLiteDatabase db = ((VideOSCApplication) getActivity().getApplicationContext()).getSettingsHelper().getReadableDatabase();

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
				setFramerateRange(
						cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.FRAMERATE_RANGE))
				);
				setColorOscCmds(
						cursor.getString(cursor.getColumnIndexOrThrow(SettingsContract.SettingsEntries.ROOT_CMD))
				);
			}

			cursor.close();

			// triplets of booleans, to be added to lockList, offPxls;
			Point res = getResolution();
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
		 * Extract supported preview and flash modes from the camera.
		 *
		 * @param camera an instance of Camera
		 */
		private void setCamera(Camera camera) {
			pCamera = camera;
			mCameraParams = camera.getParameters();
			// Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
			mSupportedPreviewSizes = mCameraParams.getSupportedPreviewSizes();
			mPreviewSize = getSmallestPreviewSize(mSupportedPreviewSizes);
			mCameraParams.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			mSupportedFlashModes = mCameraParams.getSupportedFlashModes();

			// Set the camera to Auto Flash mode.
			if (mSupportedFlashModes != null && mSupportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
				mCameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
			}
			camera.setParameters(mCameraParams);

			requestLayout();
		}

		/**
		 * The Surface has been created, now tell the camera where to draw the preview.
		 *
		 * @param holder the surface holder
		 */
		public void surfaceCreated(SurfaceHolder holder) {
			Log.d(TAG, "surfaceCreated: " + pCamera);
			try {
				pCamera.setPreviewDisplay(mHolder);
				pCamera.startPreview();
				View menuButton = mPreviewContainer.findViewById(R.id.show_menu);
				menuButton.bringToFront();
				View indicatorPanel = mPreviewContainer.findViewById(R.id.indicator_panel);
				indicatorPanel.bringToFront();
				Log.d(TAG, "preview should be started");
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
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * React to surface changed events
		 *
		 * @param holder the surface holder
		 * @param format the pixel format of the surface
		 * @param w      the surface width
		 * @param h      the surface height
		 */
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			if (mHolder.getSurface() == null) {
				// preview surface does not exist
				return;
			}

			// memorize current pixel size
			setPixelSize();

			// stop preview before making changes
			try {
				final Camera.Parameters parameters = pCamera.getParameters();
				int[] frameRates = getFramerateRange();
				parameters.setPreviewFpsRange(frameRates[0], frameRates[1]);

				pCamera.setParameters(parameters);
				Log.d(TAG, "camera parameters set");
				pCamera.setPreviewCallback(new Camera.PreviewCallback() {
					@Override
					public void onPreviewFrame(byte[] data, Camera camera) {
						mNow = System.currentTimeMillis();
						mFrameRate = Math.round(1000.0f / (mNow - mPrev) * 10.0f) / 10.0f;
						mPrev = mNow;
						TextView frameRateText = (TextView) mPreviewContainer.findViewById(R.id.fps);
						if (frameRateText != null)
							frameRateText.setText(String.format(Locale.getDefault(), "%.1f", mFrameRate));
						TextView zoomText = (TextView) mPreviewContainer.findViewById(R.id.zoom);
						if (zoomText != null)
							zoomText.setText(String.format(Locale.getDefault(), "%.1f", mCamZoom));
						int outWidth = getResolution().x;
						int outHeight = getResolution().y;
						int previewSize = outWidth * outHeight;
						int diff = previewSize - offPxls.size();
						if (diff != 0) pad(diff);
//						Log.d(TAG, "width: " + outWidth + ", height: " + outHeight + ", offPxls size: " + offPxls.size());
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
			performClick();
			Camera.Parameters params = pCamera.getParameters();

			/* ViewGroup colorModePanel = mApp.getColorModePanel();
			boolean isColorModePanelOpen = mApp.getIsColorModePanelOpen();
			if (colorModePanel != null && isColorModePanelOpen)
				VideOSCUIHelpers.removeView(colorModePanel, mApp.getCamView()); */

//			if (motionEvent.getAction() == MotionEvent.ACTION_MOVE)
				Log.d(TAG, "motion event: " + motionEvent.getActionMasked() + ", x: " + motionEvent.getX() + ", y: " + motionEvent.getY() + ", pressure: " + motionEvent.getPressure());

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
					mCamZoom = (float) (params.getZoomRatios().get(params.getZoom()) / 100.0);
				}
			}

			Log.d(TAG, "current pixel: " + getHoverPixel(motionEvent.getX(), motionEvent.getY()));

			return true;
		}

		@Override
		public boolean performClick() {
			super.performClick();
			return false;
		}

		// hack: haven't been able to implement a proper callback
		// that would've allowed me to implement an OnClickListener from within
		// the activity
		// at least this doesn't cause memory leaks... (hopefully)
		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			// hide elements on screen when clicked outside of them
			setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ViewGroup modePanel = (ViewGroup) mPreviewContainer.findViewById(R.id.color_mode_panel);
					VideOSCUIHelpers.removeView(modePanel, mPreviewContainer);
				}
			});
		}

		/**
		 * Determine the space between the first two fingers
		 */
		private double getFingerSpacing(MotionEvent event) {
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);

			return Math.sqrt(x * x + y * y);
		}

		/**
		 * get current zoom
		 */
		private int getCurrentZoom() {
			Camera.Parameters params = pCamera.getParameters();
			return params.getZoom();
		}

		/**
		 * set zoom
		 */
		private void setZoom(int zoom) {
			Camera.Parameters params = pCamera.getParameters();
			params.setZoom(zoom);
			pCamera.setParameters(params);
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

		// set the preview fps range and update framerate immediately
		// format, w, h must be passed in explicitely as they're demanded by surfaceChanged()
		private void setPreviewFpsRange(Camera.Parameters params, int[] range, int format, int w, int h) {
			params.setPreviewFpsRange(range[0], range[1]);
			surfaceChanged(mHolder, format, w, h);
		}

		private void pad(int diff) {
			if (diff > 0) {
				for (int i = 0; i < diff; i++) {
					offPxls.add(falses.clone());
					lockList.add(falses.clone());
				}
			} /*else if (diff < 0) {
				Log.d(TAG, "diff: " + diff + ", offPxls size: " + offPxls.size());
				offPxls = (ArrayList<Boolean[]>) offPxls.subList(0, offPxls.size() - 1 + diff);
				Log.d(TAG, "offPxls size: " + offPxls.size());
				lockList = (ArrayList<Boolean[]>) lockList.subList(0, lockList.size() - 1 + diff);
			}*/
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

				if (!mApp.getPixelImageHidden()) {
					if (mApp.getColorMode().equals(RGBModes.RGB)) {
						if (offPxls.get(i)[0] && !offPxls.get(i)[1] && !offPxls.get(i)[2]) {
							// mRed
//						alpha = pCamera.isStarted() ? 255 / 3 : 0;
							pixels[i] = Color.argb(255 / 3, 0, gVal, bVal);
						} else if (!offPxls.get(i)[0] && offPxls.get(i)[1] && !offPxls.get(i)[2]) {
							// mGreen;
//						alpha = cam.isStarted() ? 255 / 3 : 0;
							pixels[i] = Color.argb(255 / 3, rVal, 0, bVal);
						} else if (!offPxls.get(i)[0] && !offPxls.get(i)[1] && offPxls.get(i)[2]) {
							// mBlue;
//						alpha = cam.isStarted() ? 255 / 3 : 0;
							pixels[i] = Color.argb(255 / 3, rVal, gVal, 0);
						} else if (offPxls.get(i)[0] && offPxls.get(i)[1] && !offPxls.get(i)[2]) {
							// rg;
//						alpha = cam.isStarted ? 255 / 3 * 2 : 0;
							pixels[i] = Color.argb(255 / 3 * 2, 0, 0, bVal);
						} else if (offPxls.get(i)[0] && !offPxls.get(i)[1] && offPxls.get(i)[2]) {
							// rb;
//						alpha = cam.isStarted() ? 255 / 3 * 2 : 0;
							pixels[i] = Color.argb(255 / 3 * 2, 0, gVal, 0);
						} else if (!offPxls.get(i)[0] && offPxls.get(i)[1] && offPxls.get(i)[2]) {
							// bg;
//						alpha = cam.isStarted() ? 255 / 3 * 2 : 0;
							pixels[i] = Color.argb(255 / 3 * 2, rVal, 0, 0);
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
				} else {
					// all pixels fully transparent
					pixels[i] = Color.argb(0, 0, 0, 0);
				}

				if (mApp.getPlay()) {
//					if (calcsPerPeriod == 1) {
					if (mApp.getNormalized()) {
						rval = (float) rVal / 255;
						gval = (float) gVal / 255;
						bval = (float) bVal / 255;
					} else {
						rval = rVal;
						gval = gVal;
						bval = bVal;
					}

					// all OSC messaging (message construction sending) must happen synchronized
					// otherwise messages easily get overwritten during processing

					synchronized (mRedOscRunnable.mOscLock) {
						if (!offPxls.get(i)[0]) {
							oscR = mApp.mOscHelper.makeMessage(oscR, mRed + (i + 1));
							oscR.add(rval);
							if (VideOSCApplication.getDebugPixelOsc()) {
								RedOscRunnable.setDebugPixelOsc(true);
								oscR.add(++mCountR);
							} else {
								RedOscRunnable.setDebugPixelOsc(false);
							}
							mRedOscRunnable.mMsg = oscR;
							mRedOscRunnable.mOscLock.notify();
						}
					}

					synchronized (mGreenOscRunnable.mOscLock) {
						if (!offPxls.get(i)[1]) {
							oscG = mApp.mOscHelper.makeMessage(oscG, mGreen + (i + 1));
							oscG.add(gval);
							if (VideOSCApplication.getDebugPixelOsc()) {
								GreenOscRunnable.setDebugPixelOsc(true);
								oscG.add(++mCountG);
							} else {
								GreenOscRunnable.setDebugPixelOsc(false);
							}
							mGreenOscRunnable.mMsg = oscG;
							mGreenOscRunnable.mOscLock.notify();
						}
					}

					synchronized (mBlueOscRunnable.mOscLock) {
						if (!offPxls.get(i)[2]) {
							oscB = mApp.mOscHelper.makeMessage(oscB, mBlue + (i + 1));
							oscB.add(bval);
							if (VideOSCApplication.getDebugPixelOsc()) {
								BlueOscRunnable.setDebugPixelOsc(true);
								oscB.add(++mCountB);
							} else {
								BlueOscRunnable.setDebugPixelOsc(false);
							}
							mBlueOscRunnable.mMsg = oscB;
							mBlueOscRunnable.mOscLock.notify();
						}
					}
/*
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
*/
				}
			}

			bmp.setPixels(pixels, 0, width, 0, 0, width, height);

			return bmp;
		}

/*
		private void prepareAndSendOsc(OscMessage msg, float val) {
			msg.add(val);
			mOscRunnable.mMsg = msg;
			mOscRunnable.mOscLock.notify();
		}
*/
	}

	// prevent memory leaks by declaring Runnable static
	// see also https://stackoverflow.com/questions/29694222/is-this-runnable-safe-from-memory-leak
	// or http://www.androiddesignpatterns.com/2013/04/activitys-threads-memory-leaks.html
	private static class RedOscRunnable implements Runnable {
		private volatile OscMessage mMsg;
		private final Object mOscLock = new Object();
		private long mCountSentR = 0;
		private static boolean mDebugPixel = false;
		private static VideOSCOscHandler mOscHelper;

		private static void setDebugPixelOsc(boolean debugPixel) {
			mDebugPixel = debugPixel;
		}

		private static void setOscHelper(VideOSCOscHandler oscHelper) {
			mOscHelper = oscHelper;
		}

		/**
		 * When an object implementing interface <code>Runnable</code> is used
		 * to create a thread, starting the thread causes the object's
		 * <code>run</code> method to be called in that separately executing
		 * thread.
		 * <p>
		 * The general contract of the method <code>run</code> is that it may
		 * take any action whatsoever.
		 *
		 * @see Thread#run()
		 */
		@Override
		@SuppressWarnings("InfiniteLoopStatement")
		public void run() {
			while (true) {
				synchronized (mOscLock) {
					try {
						if (mMsg != null && mMsg.addrPattern().length() > 0 && mMsg.arguments().length > 0) {
							if (mDebugPixel)
								mMsg.add(++mCountSentR);
							mOscP5.send(mMsg, mOscHelper.getBroadcastAddr());
						}
						mOscLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static class GreenOscRunnable implements Runnable {
		private volatile OscMessage mMsg;
		private final Object mOscLock = new Object();
		private long mCountSentG = 0;
		private static boolean mDebugPixel = false;
		private static VideOSCOscHandler mOscHelper;

		private static void setDebugPixelOsc(boolean debugPixel) {
			mDebugPixel = debugPixel;
		}

		private static void setOscHelper(VideOSCOscHandler oscHelper) {
			mOscHelper = oscHelper;
		}

		/**
		 * When an object implementing interface <code>Runnable</code> is used
		 * to create a thread, starting the thread causes the object's
		 * <code>run</code> method to be called in that separately executing
		 * thread.
		 * <p>
		 * The general contract of the method <code>run</code> is that it may
		 * take any action whatsoever.
		 *
		 * @see Thread#run()
		 */
		@Override
		@SuppressWarnings("InfiniteLoopStatement")
		public void run() {
			while (true) {
				synchronized (mOscLock) {
					try {
						if (mMsg != null && mMsg.addrPattern().length() > 0 && mMsg.arguments().length > 0) {
							if (mDebugPixel)
								mMsg.add(++mCountSentG);
							mOscP5.send(mMsg, mOscHelper.getBroadcastAddr());
						}
						mOscLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static class BlueOscRunnable implements Runnable {
		private volatile OscMessage mMsg;
		private final Object mOscLock = new Object();
		private long mCountSentB = 0;
		private static boolean mDebugPixel = false;
		private static VideOSCOscHandler mOscHelper;

		private static void setDebugPixelOsc(boolean debugPixel) {
			mDebugPixel = debugPixel;
		}

		private static void setOscHelper(VideOSCOscHandler oscHelper) {
			mOscHelper = oscHelper;
		}

		/**
		 * When an object implementing interface <code>Runnable</code> is used
		 * to create a thread, starting the thread causes the object's
		 * <code>run</code> method to be called in that separately executing
		 * thread.
		 * <p>
		 * The general contract of the method <code>run</code> is that it may
		 * take any action whatsoever.
		 *
		 * @see Thread#run()
		 */
		@Override
		@SuppressWarnings("InfiniteLoopStatement")
		public void run() {
			while (true) {
				synchronized (mOscLock) {
					try {
						if (mMsg != null && mMsg.addrPattern().length() > 0 && mMsg.arguments().length > 0) {
							if (mDebugPixel)
								mMsg.add(++mCountSentB);
							mOscP5.send(mMsg, mOscHelper.getBroadcastAddr());
						}
						mOscLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
