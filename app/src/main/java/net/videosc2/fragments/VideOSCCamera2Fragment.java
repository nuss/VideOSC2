package net.videosc2.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.Toast;

import net.videosc2.R;
import net.videosc2.activities.VideOSCMainActivity;

import java.util.ArrayList;
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

	public CameraPreview mPreview;

	/**
	 * A {@link Semaphore} to prevent the app from exiting before closing the camera.
	 */
	private Semaphore mCameraOpenCloseLock = new Semaphore(1);

	private CameraDevice mCameraDevice;

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
		mImage = (ImageView) view.findViewById(R.id.camera_downscaled);
		Log.d(TAG, "onViewCreated: " + mImage);
	}

	@Override
	public void onResume() {
		super.onResume();
		openCamera();
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
	private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

		@Override
		public void onOpened(@NonNull CameraDevice cameraDevice) {
			Log.d(TAG, "CameraDevice.StateCallback onOpened: " + cameraDevice);
			// This method is called when the camera is opened.  We start camera preview here.
			mCameraOpenCloseLock.release();
			mCameraDevice = cameraDevice;
			mPreview = new CameraPreview(getActivity().getApplicationContext(), mCameraDevice);
			Log.d(TAG, "mPreview in onOpened: " + mPreview);
			mSurface = mPreview.getSurface();

			try {
				List<Surface> surfaceList = Collections.singletonList(mSurface);
				Log.d(TAG, "surfaceList in onOpened: " + surfaceList + ", surface is valid: " + mSurface.isValid());
				cameraDevice.createCaptureSession(surfaceList, sessionCallback, null);
			} catch (CameraAccessException e) {
				Log.e(TAG, "couldn't create capture session for camera: " + cameraDevice.getId(), e);
			}
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

	CameraCaptureSession.StateCallback sessionCallback = new CameraCaptureSession.StateCallback() {

		@Override
		public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
			Log.d(TAG, "CameraCaptureSession.StateCallback onConfigured: " + cameraCaptureSession);
		}

		@Override
		public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
			Log.d(TAG, "CameraCaptureSession.StateCallback onConfiguredFailed: " + cameraCaptureSession);
		}
	};

	class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

		public CameraPreview(Context context, CameraDevice cameraDevice) {
			super(context);

			mHolder = getHolder();
			mHolder.addCallback(this);
//			mSurface = mHolder.getSurface();

//			Log.d(TAG, "CameraPreview constructor - surface: " + mSurface);

			WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			display.getSize(VideOSCMainActivity.dimensions);
		}

		private Surface getSurface() {
			return mHolder.getSurface();
		}

		@Override
		public void surfaceCreated(SurfaceHolder surfaceHolder) {
			Log.d(TAG, "surfaceCreated");
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

	private void openCamera() {
		CameraManager manager = setUpCameraOutputs();

		try {
			if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
				throw new RuntimeException("Time out waiting to lock camera opening.");
			}
			manager.openCamera(mCameraId, mStateCallback, null);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
		}
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
					mCameraId = cameraId;
					Log.d(TAG, "cameraId: " + cameraId + ", preview size: " + mPreviewSize.getWidth() + ", " + mPreviewSize.getHeight());
				}
			}
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}

		return manager;
	}

}

