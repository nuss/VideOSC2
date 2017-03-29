package net.videosc2.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
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
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import net.videosc2.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Semaphore;

/**
 * Created by stefan on 27.03.17, package net.videosc2.fragments, project VideOSC22.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class VideOSCCamera2Fragment extends VideOSCBaseFragment {
	private final static String TAG = "VideOSCCamera2Fragment";

	private ViewGroup previewContainer;
	private ImageView mImage;
	private Size previewSize;

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
		mImage = (ImageView) view.findViewById(R.id.camera_downscaled);

		Size size = getSmallestPreviewSize();

		if (size.getWidth() > 0 && size.getHeight() > 0)
			previewSize = size;
		else {
			Toast toast = Toast.makeText(getActivity(), "No appropriate preview size could be determined. Sorry!", Toast.LENGTH_SHORT);
			toast.show();
		}

		// TODO

		return view;
	}

	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		Log.d(TAG, "onViewCreated: " + view);
//		view.findViewById(R.id.picture).setOnClickListener(this);
//		view.findViewById(R.id.info).setOnClickListener(this);
//		mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
		mPreview = new CameraPreview(getActivity(), mCameraDevice);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
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
			// This method is called when the camera is opened.  We start camera preview here.
			mCameraOpenCloseLock.release();
			mCameraDevice = cameraDevice;
			createCameraPreviewSession();
		}

		@Override
		public void onDisconnected(@NonNull CameraDevice cameraDevice) {
			mCameraOpenCloseLock.release();
			cameraDevice.close();
			mCameraDevice = null;
		}

		@Override
		public void onError(@NonNull CameraDevice cameraDevice, int error) {
			mCameraOpenCloseLock.release();
			cameraDevice.close();
			mCameraDevice = null;
			Activity activity = getActivity();
			if (null != activity) {
				activity.finish();
			}
		}

	};

/*
	public final TextureView.SurfaceTextureListener mSurfaceTextureListener
			= new TextureView.SurfaceTextureListener() {

		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
			Log.d(TAG, "onSurfaceTextureAvailable");
			openCamera(width, height);
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
			Log.d(TAG, "onSurfaceTextureChanged");
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
			Log.d(TAG, "onSurfaceTextureDestroyed");
			return true;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture texture) {
			Log.d(TAG, "onSurfaceTextureUpdated");
		}

	};
*/

	class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

		public CameraPreview(Context context, CameraDevice cameraDevice) {
			super(context);
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

	private void createCameraPreviewSession() {
		// TODO
	}

	private Size getSmallestPreviewSize() {
		Size optimalSize = new Size(0, 0);
		CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
		ArrayList<Integer> productList = new ArrayList<>();


		try {
			for (String cameraId : manager.getCameraIdList()) {
				CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
				Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

				if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
					continue;
				}

				StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
				assert map != null;
				Size[] previewSizes = map.getOutputSizes(ImageFormat.YUV_420_888);

				for (Size tmpSize : previewSizes) {
					productList.add(tmpSize.getWidth() * tmpSize.getHeight());
				}

				int minIndex = productList.indexOf(Collections.min(productList));
				optimalSize = previewSizes[minIndex];
				// image format will be YUV_420_888
			}
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}

		return optimalSize;
	}

	private void openCamera(int width, int height) {

	}
}

