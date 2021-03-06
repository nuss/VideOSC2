/* package net.videosc.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
//import android.app.Fragment;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.legacy.app.FragmentCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.videosc.R;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.views.AutoFitTextureView;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
*/
/**
 * Created by stefan on 27.03.17, package net.videosc.fragments, project VideOSC22.
 */
/*@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class VideOSCCamera2Fragment extends VideOSCBaseFragment {
	private final static String TAG = "VideOSCCamera2Fragment";

	private FragmentActivity mActivity;
	private static final int REQUEST_CAMERA_PERMISSION = 1;

	private View mContainer;
	private String mCameraId;
	private ImageView mImage;
	private Size mPreviewSize;
	private Surface mSurface;
	private HandlerThread mBackgroundThread;
	private Handler mBackgroundHandler;
	public AutoFitTextureView mTextureView;
	private CaptureRequest mTextureViewRequest;
	private CameraCaptureSession.CaptureCallback mCaptureCallback;
	private long mPrev = 0;
*/

	/**
	 * A {@link Semaphore} to prevent the app from exiting before closing the camera.
	 */
/*	private Semaphore mCameraOpenCloseLock = new Semaphore(1);

	private CameraDevice mCameraDevice;
	private CaptureRequest.Builder mPreviewRequestBuilder;
	private ImageReader mImageReader;
	private CameraCaptureSession mCaptureSession;
*/
	/**
	 * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
	 * {@link TextureView}.
	 */
/*	private final TextureView.SurfaceTextureListener mSurfaceTextureListener
			= new TextureView.SurfaceTextureListener() {

		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
			Log.d(TAG, "onSurfaceTextureAvailable, width: " + width + ", height: " + height);
			openCamera(width, height);
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
			Log.d(TAG, "onSurfaceTextureSizeChanged, width: " + width + ", height: " + height);
			configureTransform(width, height);
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
			Log.d(TAG, "onSurfaceTextureDestroyed");
			return true;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture texture) {
			long now = System.currentTimeMillis();
			float frameRate = Math.round(1000.0f / (now - mPrev) * 10.0f) / 10.0f;
			mPrev = now;
			TextView frameRateText = mContainer.findViewById(R.id.fps);
			if (frameRateText != null)
				frameRateText.setText(String.format(Locale.getDefault(), "%.1f", frameRate));
		}
	};
*/
	/**
	 * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
	 */
/*	private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

		@Override
		public void onOpened(@NonNull CameraDevice cameraDevice) {
			Log.d(TAG, "CameraDevice.StateCallback onOpened: " + cameraDevice);
			// This method is called when the camera is opened.  We start camera preview here.
			mCameraOpenCloseLock.release();
			mCameraDevice = cameraDevice;
			createCameraPreviewSession();
		}

		@Override
		public void onDisconnected(@NonNull CameraDevice cameraDevice) {
			Log.d(TAG, "CameraDevice.StateCallback onDisconnected: " + cameraDevice);
			mCameraOpenCloseLock.release();
			cameraDevice.close();
			mCameraDevice = null;
		}

		@Override
		public void onError(@NonNull CameraDevice cameraDevice, int error) {
			Log.d(TAG, "CameraDevice.StateCallback onError: " + cameraDevice + ", error: " + error);
			mCameraOpenCloseLock.release();
			cameraDevice.close();
			mCameraDevice = null;
			Activity activity = mActivity;
			if (null != activity) {
				activity.finish();
			}
		}

	};

	public VideOSCCamera2Fragment() {
		super();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		mContainer = container;
		View view = inflater.inflate(R.layout.fragment_native_camera, container, false);
		Log.d(TAG, "onCreateView: " + view);

		return view;
	}

	@Override
	public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
		FrameLayout preview;

		mActivity = getActivity();
		startBackgroundThread();
//		CameraManager manager = setUpCameraOutputs();
		mTextureView = new AutoFitTextureView(mActivity);
		preview = view.findViewById(R.id.camera_preview);
		preview.addView(mTextureView);
		Log.d(TAG, "onViewCreated, CameraPreview (mTextureView), width: " + mTextureView.getWidth() + ", height:" + mTextureView.getHeight() + ", transform: " + mTextureView.getTransform(null));
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.d(TAG, "onResume invoked");
		startBackgroundThread();
		// When the screen is turned off and turned back on, the SurfaceTexture is already
		// available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
		// a camera and start preview from here (otherwise, we wait until the surface is ready in
		// the SurfaceTextureListener).
		if (mTextureView.isAvailable()) {
			Log.d(TAG, "onResume, texture view is available");
			openCamera(mTextureView.getWidth(), mTextureView.getHeight());
		} else {
			Log.d(TAG, "onResume, texture view not available, setting surface texture listener: " + mSurfaceTextureListener);
			mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
		}
	}

	@Override
	public void onPause() {
		closeCamera();
		stopBackgroundThread();
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		// TODO
	}
*/
	/**
	 * Opens the camera specified by {@link VideOSCCamera2Fragment#mCameraId}.
	 */
/*	private void openCamera(int width, int height) {
		Log.d(TAG, "openCamera called - width: " + width + ", height: " + height);
		if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA)
				!= PackageManager.PERMISSION_GRANTED) {
			requestCameraPermission();
			Log.d(TAG, "no camera permission");
			return;
		}
		setUpCameraOutputs();
		configureTransform(width, height);
		CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
		try {
			if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
				throw new RuntimeException("Time out waiting to lock camera opening.");
			}
			Log.d(TAG, "opening camera, camera id: " + mCameraId + ", callback: " + mStateCallback + ", handler: " + mBackgroundHandler);
			if (manager != null) {
				manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
				Log.d(TAG, "camera opened");
			}
		} catch (CameraAccessException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
		}
	}
*/
	/**
	 * Closes the current {@link CameraDevice}.
	 */
/*	private void closeCamera() {
		try {
			mCameraOpenCloseLock.acquire();
			if (null != mCaptureSession) {
				mCaptureSession.close();
				mCaptureSession = null;
			}
			if (null != mCameraDevice) {
				mCameraDevice.close();
				mCameraDevice = null;
			}
			if (null != mImageReader) {
				mImageReader.close();
				mImageReader = null;
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
		} finally {
			mCameraOpenCloseLock.release();
		}
	}
*/

	/**
	 * Creates a new {@link CameraCaptureSession} for camera preview.
	 */
/*	private void createCameraPreviewSession() {
		try {
			SurfaceTexture texture = mTextureView.getSurfaceTexture();
			assert texture != null;

			// We configure the size of default buffer to be the size of camera preview we want.
			texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
			Log.d(TAG, "texture buffer size changed");

			// This is the output Surface we need to start preview.
			Surface surface = new Surface(texture);

			Log.d(TAG, "surface created");
			// We set up a CaptureRequest.Builder with the output Surface.
			mPreviewRequestBuilder
					= mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			mPreviewRequestBuilder.addTarget(surface);
			Surface imgReaderSurface = mImageReader.getSurface();
			mPreviewRequestBuilder.addTarget(imgReaderSurface);

			// Here, we create a CameraCaptureSession for camera preview.
			mCameraDevice.createCaptureSession(Arrays.asList(surface, imgReaderSurface),
					new CameraCaptureSession.StateCallback() {

						@Override
						public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
							Log.d(TAG, "cameraCaptureSession: " + cameraCaptureSession);
							// The camera is already closed
							if (null == mCameraDevice) {
								return;
							}

							// When the session is ready, we start displaying the preview.
							mCaptureSession = cameraCaptureSession;
							try {
								// Auto focus should be continuous for camera preview.
								mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
										CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

								// Finally, we start displaying the camera preview.
								mTextureViewRequest = mPreviewRequestBuilder.build();
								mCaptureSession.setRepeatingRequest(mTextureViewRequest,
										mCaptureCallback, mBackgroundHandler);
								Log.d(TAG, "capture session configured");
							} catch (CameraAccessException e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onConfigureFailed(
								@NonNull CameraCaptureSession cameraCaptureSession) {
							showToast("Failed");
							Log.d(TAG, "configuring capture session failed");
						}
					}, null
			);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}
*/
	/**
	 * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
	 * This method should be called after the camera preview size is determined in
	 * setUpCameraOutputs and also the size of `mTextureView` is fixed.
	 *
	 * @param viewWidth  The width of `mTextureView`
	 * @param viewHeight The height of `mTextureView`
	 */
/*	private void configureTransform(int viewWidth, int viewHeight) {
		Activity activity = getActivity();
		if (null == mTextureView || null == mPreviewSize || null == activity) {
			return;
		}
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		Matrix matrix = new Matrix();
		RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
		RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
		float centerX = viewRect.centerX();
		float centerY = viewRect.centerY();
		if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
			bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
			matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
			float scale = Math.max(
					(float) viewHeight / mPreviewSize.getHeight(),
					(float) viewWidth / mPreviewSize.getWidth());
			matrix.postScale(scale, scale, centerX, centerY);
			matrix.postRotate(90 * (rotation - 2), centerX, centerY);
		} else if (Surface.ROTATION_180 == rotation) {
			matrix.postRotate(180, centerX, centerY);
		}
		mTextureView.setTransform(matrix);
	}

	private void requestCameraPermission() {
		if (FragmentCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
			new ConfirmationDialog().show(getChildFragmentManager(), "dialog");
		} else {
			FragmentCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
					REQUEST_CAMERA_PERMISSION);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
	                                       @NonNull int[] grantResults) {
		if (requestCode == REQUEST_CAMERA_PERMISSION) {
			if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
				ErrorDialog.newInstance(getString(R.string.camera_request_permission))
						.show(getChildFragmentManager(), "dialog");
			}
		} else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}
*/
	/**
	 * Sets up member variables related to camera.
	 */
/*	private void setUpCameraOutputs(/*int width, int height*//*) {
		CameraManager manager = (CameraManager) mActivity().getSystemService(Context.CAMERA_SERVICE);
		ArrayList<Integer> productList = new ArrayList<>();

		try {
			if (manager != null) {
				for (String cameraId : manager.getCameraIdList()) {
					CameraCharacteristics characteristics
							= manager.getCameraCharacteristics(cameraId);

					// We don't use a front facing camera in this sample.
					Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
					if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {

						StreamConfigurationMap map = characteristics.get(
								CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
						if (map == null) {
							continue;
						}

						int[] outputFormats = map.getOutputFormats();
						for (int format : outputFormats) {
							Log.d(TAG, "format: " + format);
						}
						Size[] previewSizes = map.getOutputSizes(ImageFormat.YUV_420_888);

						for (Size tmpSize : previewSizes) {
							productList.add(tmpSize.getWidth() * tmpSize.getHeight());
						}

						int minIndex = productList.indexOf(Collections.min(productList));
						mPreviewSize = previewSizes[minIndex];
						mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.YUV_420_888, 2);
						mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
						mCameraId = cameraId;
					}
				}
			}
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}
*/
	/**
	 * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
	 * still image is ready to be saved.
	 */
/*	private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
			= new ImageReader.OnImageAvailableListener() {

		@Override
		public void onImageAvailable(ImageReader reader) {
//			Log.d(TAG, "onImageAvailable");
			Image latest = reader.acquireLatestImage();
			//	Log.d(TAG, "latest, width: " + latest.getWidth() + ", height: " + latest.getHeight());
			Image.Plane[] planes = latest.getPlanes();
//			for (int i = 0; i < latest.getPlanes().length; i++) {
//				Log.d(TAG, "plane " + i + ": " + planes[i] + " (" + planes[i].getBuffer() + ")");
//			}
			latest.close();
		}

	};
*/
	/**
	 * Saves a JPEG {@link Image} into the specified {@link File}.
	 */
/*	private static class ImageSaver implements Runnable {*/

		/**
		 * The JPEG image
		 */
/*		private final Image mImage;

		ImageSaver(Image image) {
			mImage = image;
		}

		@Override
		public void run() {
			ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
			byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			Log.d(TAG, "bytes[0]: " + bytes[0]);
		}

	}
*/
	/**
	 * Shows OK/Cancel confirmation dialog about camera permission.
	 */
/*	public static class ConfirmationDialog extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Fragment parent = getParentFragment();
			return new AlertDialog.Builder(getActivity())
					.setMessage(R.string.camera_request_permission)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							FragmentCompat.requestPermissions(parent,
									new String[]{Manifest.permission.CAMERA},
									REQUEST_CAMERA_PERMISSION);
						}
					})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Activity activity = parent.getActivity();
									if (activity != null) {
										activity.finish();
									}
								}
							})
					.create();
		}

		public void show(FragmentManager childFragmentManager, String dialog) {
		}
	}
*/
	/**
	 * Shows an error message dialog.
	 */
/*	public static class ErrorDialog extends DialogFragment {

		private static final String ARG_MESSAGE = "message";

		public static ErrorDialog newInstance(String message) {
			ErrorDialog dialog = new ErrorDialog();
			Bundle args = new Bundle();
			args.putString(ARG_MESSAGE, message);
			dialog.setArguments(args);
			return dialog;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Activity activity = mActivity;
			return new AlertDialog.Builder(activity)
					.setMessage(getArguments().getString(ARG_MESSAGE))
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							activity.finish();
						}
					})
					.create();
		}

		public void show(FragmentManager childFragmentManager, String dialog) {
		}
	}
*/
	/**
	 * Starts a background thread and its {@link Handler}.
	 */
/*	private void startBackgroundThread() {
		mBackgroundThread = new HandlerThread("CameraBackground");
		mBackgroundThread.start();
		mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
	}
*/
	/**
	 * Stops the background thread and its {@link Handler}.
	 */
/*	private void stopBackgroundThread() {
		mBackgroundThread.quitSafely();
		try {
			mBackgroundThread.join();
			mBackgroundThread = null;
			mBackgroundHandler = null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

} */