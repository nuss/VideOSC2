package net.videosc.fragments;

import android.app.FragmentManager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;

import androidx.drawerlayout.widget.DrawerLayout;

public class VideOSCMSBaseFragment extends VideOSCBaseFragment {
	final private static String TAG = "VideOSCMSBaseFragment";
	protected ViewGroup mParentContainer;
	protected ViewGroup mContainer;
	protected ViewGroup mMSButtons;
	protected FragmentManager mManager;
	protected VideOSCBaseFragment mFragment;

	public VideOSCMSBaseFragment() { }

	@Override
	public void onStart() {
		super.onStart();
		final VideOSCMainActivity activity = (VideOSCMainActivity) getActivity();
		final VideOSCApplication app = (VideOSCApplication) activity.getApplication();
		final DrawerLayout toolsDrawer = activity.mToolsDrawerLayout;
		ImageButton ok = mMSButtons.findViewById(R.id.ok);
		ImageButton cancel = mMSButtons.findViewById(R.id.cancel);
		final ViewGroup fpsCalcPanel = mParentContainer.findViewById(R.id.fps_calc_period_indicator);
		final ViewGroup indicatorPanel = mParentContainer.findViewById(R.id.indicator_panel);
		final ViewGroup pixelEditorToolbox = mParentContainer.findViewById(R.id.pixel_editor_toolbox);
		final ViewGroup snapshotsBar = mParentContainer.findViewById(R.id.snapshots_bar);
		final VideOSCCameraFragment cameraPreview = (VideOSCCameraFragment) mManager.findFragmentByTag("CamPreview");

		mMSButtons.bringToFront();
		// move behaviour defined in VideOSCMainActivity > onTouch()
		mMSButtons.setOnTouchListener(activity);

		ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				app.setIsMultiSliderActive(false);
				mManager.beginTransaction().remove(mFragment).commit();
				mContainer.removeView(mMSButtons);
				indicatorPanel.setVisibility(View.VISIBLE);
				pixelEditorToolbox.setVisibility(View.VISIBLE);
				snapshotsBar.setVisibility(View.VISIBLE);
				if (app.getIsFPSCalcPanelOpen())
					fpsCalcPanel.setVisibility(View.VISIBLE);
				toolsDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			}
		});

		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				app.setIsMultiSliderActive(false);

//				if (app.getColorMode().equals(RGBModes.RGB) || app.getColorMode().equals(RGBModes.R)) {
					SparseArray<Double> redResetVals = cameraPreview.getRedResetValues();
					SparseArray<Double> redResetMixVals = cameraPreview.getRedMixResetValues();
					for (int i = 0; i < redResetVals.size(); i++) {
						cameraPreview.setRedValue(redResetVals.keyAt(i), redResetVals.valueAt(i));
						cameraPreview.setRedMixValue(redResetMixVals.keyAt(i), redResetMixVals.valueAt(i));
					}
//				}
//				if (app.getColorMode().equals(RGBModes.RGB) || app.getColorMode().equals(RGBModes.G)) {
					SparseArray<Double> greenResetVals = cameraPreview.getGreenResetValues();
					SparseArray<Double> greenResetMixVals = cameraPreview.getGreenMixResetValues();
					for (int i = 0; i < greenResetVals.size(); i++) {
						cameraPreview.setGreenValue(greenResetVals.keyAt(i), greenResetVals.valueAt(i));
						cameraPreview.setGreenMixValue(greenResetMixVals.keyAt(i), greenResetMixVals.valueAt(i));
					}
//				}
//				if (app.getColorMode().equals(RGBModes.RGB) || app.getColorMode().equals(RGBModes.B)) {
					SparseArray<Double> blueResetVals = cameraPreview.getBlueResetValues();
					SparseArray<Double> blueResetMixVals = cameraPreview.getBlueMixResetValues();
					for (int i = 0; i < blueResetVals.size(); i++) {
						cameraPreview.setBlueValue(blueResetVals.keyAt(i), blueResetVals.valueAt(i));
						cameraPreview.setBlueMixValue(blueResetMixVals.keyAt(i), blueResetMixVals.valueAt(i));
					}
//				}

				mManager.beginTransaction().remove(mFragment).commit();
				mContainer.removeView(mMSButtons);
				indicatorPanel.setVisibility(View.VISIBLE);
				pixelEditorToolbox.setVisibility(View.VISIBLE);
				snapshotsBar.setVisibility(View.VISIBLE);
				if (app.getIsFPSCalcPanelOpen())
					fpsCalcPanel.setVisibility(View.VISIBLE);
				toolsDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			}
		});
	}

	public void setParentContainer(ViewGroup container) {
		this.mParentContainer = container;
	}

	protected OnCreateViewCallback createViewCallback = null;

	public void setCreateViewCallback(OnCreateViewCallback createViewCallback) {
		this.createViewCallback = createViewCallback;
	}

	public interface OnCreateViewCallback {
		void onCreateView();
	}

}
