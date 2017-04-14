package net.videosc2.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
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
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import net.videosc2.R;
import net.videosc2.activities.VideOSCMainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by stefan on 27.03.17, package net.videosc2.fragments, project VideOSC22.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class VideOSCCamera2Fragment extends VideOSCBaseFragment {
	private final static String TAG = "VideOSCCamera2Fragment";

	private String mCameraId;
	private ViewGroup previewContainer;
	private ImageView mImage;
	private Size mPreviewSize;
	private SurfaceHolder mHolder;
	private Surface mSurface;
	private HandlerThread mBackgroundThread;
	private Handler mBackgroundHandler;
	public CameraPreview mPreview;
	private CaptureRequest mPreviewRequest;
	private CameraCaptureSession.CaptureCallback mCaptureCallback;

	/**
	 * A {@link Semaphore} to prevent the app from exiting before closing the camera.
	 */
	private Semaphore mCameraOpenCloseLock = new Semaphore(1);

	private CameraDevice mCameraDevice;
	private CaptureRequest.Builder mPreviewRequestBuilder;
	private ImageReader mImageReader;
	private CameraCaptureSession mCaptureSession;

	/**
	 * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
	 */
	private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

		@Override
		public void onOpened(@NonNull CameraDevice cameraDevice) {
			Log.d(TAG, "CameraDevice.StateCallback onOpened: " + cameraDevice);
			// This method is called when the camera is opened.  We start camera preview here.
			mCameraOpenCloseLock.release();
			mCameraDevice = cameraDevice;
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
			Activity activity = getActivity();
			if (null != activity) {
				activity.finish();
			}
		}

	};

	public VideOSCCamera2Fragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_native_camera, container, false);
		Log.d(TAG, "onCreateView: " + view);

		return view;
	}

	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		FrameLayout preview;

		startBackgroundThread();
		CameraManager manager = setUpCameraOutputs();
		mPreview = new CameraPreview(getActivity().getApplicationContext());
		preview = (FrameLayout) view.findViewById(R.id.camera_preview);
		preview.addView(mPreview);
		openCamera(manager);
		Log.d(TAG, "CameraPreview (mPreview): " + mPreview.getWidth() + ", " + mPreview.getHeight() + " (id: " + mPreview.getId() + ")");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume - camera should be opened");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		// TODO
	}

	/**
	 * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
	 */
	CameraCaptureSession.StateCallback mSessionCallback = new CameraCaptureSession.StateCallback() {

		@Override
		public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
			Log.d(TAG, "CameraCaptureSession.StateCallback onConfigured: " + cameraCaptureSession);
		}

		@Override
		public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
			Log.d(TAG, "CameraCaptureSession.StateCallback onConfiguredFailed: " + cameraCaptureSession.getDevice());
		}
	};

	class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
		private CameraDevice pCamera;

		public CameraPreview(Context context) {
			super(context);

			Point holderSize = new Point();
			WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			display.getSize(holderSize);
			mHolder = getHolder();
//			mHolder.setFixedSize(holderSize.x, holderSize.y);
			mHolder.setFixedSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
			mHolder.setFormat(PixelFormat.RGBA_8888);
			mSurface = mHolder.getSurface();
			mHolder.addCallback(this);
		}

		@Override
		public void surfaceCreated(SurfaceHolder surfaceHolder) {
			Log.d(TAG, "surfaceCreated");
			createCameraPreviewSession();
		}

		@Override
		public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
			Log.d(TAG, "surfaceChanged - surfaceHolder: " + surfaceHolder + ", i: " + i + ", i1: " + i1 + ", i2: " + i2);
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
			Log.d(TAG, "surfaceDestroyed - surfaceHolder: " + surfaceHolder);
		}
	}

	/**
	 * Opens the camera specified by {@link VideOSCCamera2Fragment#mCameraId}.
	 */
	private void openCamera(final CameraManager manager) {
		manager.registerAvailabilityCallback(new CameraManager.AvailabilityCallback() {
			@Override
			public void onCameraAvailable(@NonNull String cameraId) {
				super.onCameraAvailable(cameraId);
				try {
					if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
						throw new RuntimeException("Time out waiting to lock camera opening.");
					}
					manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
					Log.d(TAG, "camera opened");
				} catch (CameraAccessException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
				}
			}
		}, null);
	}

	/**
	 * Creates a new {@link CameraCaptureSession} for camera preview.
	 */
	private void createCameraPreviewSession() {
		try {
			// We set up a CaptureRequest.Builder with the output Surface.
			mPreviewRequestBuilder
					= mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			Log.d(TAG, "createCameraPreviewSession: " + mSurface + " (valid: " + mSurface.isValid() + ")" + ", mImageReader surface: " + mImageReader.getSurface() + "(surface is valid: " + mImageReader.getSurface().isValid() + "), camera: " + mCameraDevice);
			mPreviewRequestBuilder.addTarget(mSurface);

			// Here, we create a CameraCaptureSession for camera preview.
			mCameraDevice.createCaptureSession(Arrays.asList(mSurface, mImageReader.getSurface()),
					new CameraCaptureSession.StateCallback() {

						@Override
						public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
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
								mPreviewRequest = mPreviewRequestBuilder.build();
								mCaptureSession.setRepeatingRequest(mPreviewRequest,
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

	private void showToast(String msg) {

	}


	/**
	 * Sets up member variables related to camera.
	 */
	private CameraManager setUpCameraOutputs() {
		Activity activity = getActivity();
		CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
		ArrayList<Integer> productList = new ArrayList<>();

		try {
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

					Size[] previewSizes = map.getOutputSizes(ImageFormat.YUV_420_888);

					for (Size tmpSize : previewSizes) {
						productList.add(tmpSize.getWidth() * tmpSize.getHeight());
					}

					int minIndex = productList.indexOf(Collections.min(productList));
					mPreviewSize = previewSizes[minIndex];
					mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.YUV_420_888, 2);
					mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
					mCameraId = cameraId;
					Log.d(TAG, "mImageReader: " + mImageReader + ", mCameraId: " + mCameraId + ", mPreviewSize: " + mPreviewSize);
				}
			}
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}

		return manager;
	}

	/**
	 * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
	 * still image is ready to be saved.
	 */
	private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
			= new ImageReader.OnImageAvailableListener() {

		@Override
		public void onImageAvailable(ImageReader reader) {
			Log.d(TAG, "onImageAvailable");
			mBackgroundHandler.post(new ImageSaver(reader.acquireLatestImage()));
		}

	};

	/**
	 * Saves a JPEG {@link Image} into the specified {@link File}.
	 */
	private static class ImageSaver implements Runnable {

		/**
		 * The JPEG image
		 */
		private final Image mImage;


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

	/**
	 * Starts a background thread and its {@link Handler}.
	 */
	private void startBackgroundThread() {
		mBackgroundThread = new HandlerThread("CameraBackground");
		mBackgroundThread.start();
		mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
	}

	/**
	 * Stops the background thread and its {@link Handler}.
	 */
	private void stopBackgroundThread() {
		mBackgroundThread.quitSafely();
		try {
			mBackgroundThread.join();
			mBackgroundThread = null;
			mBackgroundHandler = null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}

