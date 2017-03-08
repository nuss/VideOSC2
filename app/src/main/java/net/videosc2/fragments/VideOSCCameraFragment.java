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

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

//import com.ultimate.camera.R;
//import com.ultimate.camera.utilities.DialogHelper;

import net.videosc2.R;
import net.videosc2.activities.VideOSCCameraActivity;
import net.videosc2.activities.VideOSCMainActivity;
import net.videosc2.utilities.DialogHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageNativeLibrary;

/**
 * Take a picture directly from inside the app using this fragment.
 * <p>
 * Reference: http://developer.android.com/training/camera/cameradirect.html
 * Reference: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
 * Reference: http://stackoverflow.com/questions/10913181/camera-preview-is-not-restarting
 * <p>
 * Created by Rex St. John (on behalf of AirPair.com) on 3/4/14.
 */
public class VideOSCCameraFragment extends VideOSCBaseFragment {
	final static String TAG = "VideOSCCameraFragment";

	// Native camera.
	private Camera mCamera;

	// View to display the camera output.
	private CameraPreview mPreview;

	// Reference to the containing view.
	private View mCameraView;

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
		mCameraView = view;
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
//		final static String TAG = "CameraPreview";

		// SurfaceHolder
		private SurfaceHolder mHolder;

		// Our Camera.
		private Camera mCamera;

		// Parent Context.
		private Context mContext;

		// Camera Sizing (For rotation, orientation changes)
		private Camera.Size mPreviewSize;

		// List of supported preview sizes
		private List<Camera.Size> mSupportedPreviewSizes;

		// Flash modes supported by this camera
		private List<String> mSupportedFlashModes;

		// View holding this camera.
		private View mCameraView;

		public CameraPreview(Context context, Camera camera, View cameraView) {
			super(context);

			// Capture the context
			mCameraView = cameraView;
			mContext = context;
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
			mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes);
			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			mSupportedFlashModes = parameters.getSupportedFlashModes();
//			mCamera.getParameters().setPreviewFormat(ImageFormat.RGB_565);

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

/*
				// Preview size must exist.
				if (mPreviewSize != null) {
					Camera.Size previewSize = mPreviewSize;
					parameters.setPreviewSize(previewSize.width, previewSize.height);
				}
//				Log.d(TAG, "preview size " + mPreviewSize.width + ", " + mPreviewSize.height);
*/

				mCamera.setParameters(parameters);
				Log.d(TAG, "past setParameters");
//				mCamera.startPreview();
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.outWidth = 6;
				options.outHeight = 4;
				options.inMutable = true;
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				options.outMimeType = "image/png";
				mCamera.setPreviewCallback(new Camera.PreviewCallback() {
					@Override
					public void onPreviewFrame(byte[] data, Camera camera) {
						boolean success = false;

//						ByteArrayOutputStream out = new ByteArrayOutputStream();
						int[] out = new int[mPreviewSize.width * mPreviewSize.height];
						GPUImageNativeLibrary.YUVtoRBGA(data, mPreviewSize.width, mPreviewSize.height, out);
/*
						YuvImage yImg = new YuvImage(data, ImageFormat.NV21, options.outWidth, options.outHeight, null);
						try {
							success = yImg.compressToJpeg(new Rect(0, 0, options.outWidth, options.outHeight), 100, out);
						} catch(IllegalArgumentException e) {
							e.printStackTrace();
						}
*/
//						final int[] rgb = decodeYUV420SP(data, options.outWidth, options.outHeight);
//						Bitmap bmp = Bitmap.createBitmap(rgb, options.outWidth, options.outHeight, options.inPreferredConfig);
//						Log.d(TAG, "screen dimensions: " + dimensions.x + ", " + dimensions.y);
//						if (success) {
//							byte[] imgBytes = out.toByteArray();
//							Bitmap bmp = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length, options);
//							bmp = Bitmap.createScaledBitmap(bmp, VideOSCMainActivity.dimensions.x, VideOSCMainActivity.dimensions.y, false);
							Bitmap bmp = Bitmap.createBitmap(mPreviewSize.width, mPreviewSize.height, Bitmap.Config.ARGB_8888);
							bmp.copyPixelsFromBuffer(IntBuffer.wrap(out));
							bmp = Bitmap.createScaledBitmap(bmp, options.outWidth, options.outHeight, true);
							bmp = Bitmap.createScaledBitmap(bmp, VideOSCMainActivity.dimensions.x, VideOSCMainActivity.dimensions.y, false);
							mImage.bringToFront();
							mImage.setImageBitmap(bmp);
//						}
					}
				});
			} catch (Exception e) {
				Log.d(TAG, "exception: " + e);
				e.printStackTrace();
			}
		}

		/**
		 * convert a YUV frame given by 'data' to an integer array of rgb pixels
		 *
		 * @param data the byte array from the original YUV
		 * @param width width of the frame to be converted
		 * @param height height of the frame to be converted
		 * @return an int array containg the rgb information of the image
		 */
		private int[] decodeYUV420SP(byte[] data, int width, int height) {
			final int frameSize = width * height;

			int rgb[]=new int[width*height];
			for (int j = 0, yp = 0; j < height; j++) {
				int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
				for (int i = 0; i < width; i++, yp++) {
					int y = (0xff & ((int) data[yp])) - 16;
					if (y < 0) y = 0;
					if ((i & 1) == 0) {
						v = (0xff & data[uvp++]) - 128;
						u = (0xff & data[uvp++]) - 128;
					}

					int y1192 = 1192 * y;
					int r = (y1192 + 1634 * v);
					int g = (y1192 - 833 * v - 400 * u);
					int b = (y1192 + 2066 * u);

					if (r < 0) r = 0; else if (r > 262143) r = 262143;
					if (g < 0) g = 0; else if (g > 262143) g = 262143;
					if (b < 0) b = 0; else if (b > 262143) b = 262143;

					rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) &
							0xff00) | ((b >> 10) & 0xff);


				}
			}
			return rgb;
		}

/*
		*/
/**
		 * Calculate the measurements of the layout
		 *
		 * @param widthMeasureSpec
		 * @param heightMeasureSpec
		 *//*

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			// Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
			final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
			final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
			setMeasuredDimension(width, height);

			if (mSupportedPreviewSizes != null) {
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes,*/
/* width, height*//*
);
			}
		}
*/

		/**
		 * @param sizes
		 * @param width
		 * @param height
		 * @return
		 */
		private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes/*, int width, int height*/) {
			// Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
			Camera.Size optimalSize = null;

//			final double ASPECT_TOLERANCE = 0.1;
//			double targetRatio = (double) height / width;

			// Try to find a size match which suits the whole screen minus the menu on the left.
/*
			for (Camera.Size size : sizes) {

				if (size.height != width) continue;
				double ratio = (double) size.width / size.height;
				if (ratio <= targetRatio + ASPECT_TOLERANCE && ratio >= targetRatio - ASPECT_TOLERANCE) {
					optimalSize = size;
				}
			}
*/

			for (int i = 1; i < sizes.size(); i++) {
				if (sizes.get(i).width * sizes.get(i).width < sizes.get(i-1)
						.width * sizes.get(i-1).height) {
					optimalSize = sizes.get(i);
				}
			}

			// If we cannot find the one that matches the aspect ratio, ignore the requirement.
			if (optimalSize == null) {
				// TODO : Backup in case we don't get a size.
			}

			Log.d(TAG, "optimal size: " + optimalSize.width + ", " + optimalSize.height);

			return optimalSize;
		}
	}
}
