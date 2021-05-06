package net.videosc.fragments;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;

public class VideOSCMSBaseFragment extends VideOSCBaseFragment {
	final private static String TAG = "VideOSCMSBaseFragment";
	private ViewGroup mParentContainer;
	View mMSButtons;
	View mLabelsView;
	FragmentManager mManager;
	VideOSCBaseFragment mFragment;

	public VideOSCMSBaseFragment() { }

	public VideOSCMSBaseFragment(Context context) {
		this.mActivity = (VideOSCMainActivity) context;
	}

	@Override
	public void onStart() {
		super.onStart();

		final VideOSCApplication app = (VideOSCApplication) mActivity.getApplication();
		final DrawerLayout toolsDrawer = mActivity.mToolsDrawerLayout;
		ImageButton ok = mMSButtons.findViewById(R.id.ok);
		ImageButton cancel = mMSButtons.findViewById(R.id.cancel);
		final ViewGroup fpsCalcPanel = mParentContainer.findViewById(R.id.fps_calc_period_indicator);
		final ViewGroup indicatorPanel = mParentContainer.findViewById(R.id.indicator_panel);
		final ViewGroup pixelEditorToolbox = mParentContainer.findViewById(R.id.pixel_editor_toolbox);
		final ViewGroup snapshotsBar = mParentContainer.findViewById(R.id.snapshots_bar);
		final VideOSCCameraFragment cameraPreview = (VideOSCCameraFragment) mManager.findFragmentByTag("CamPreview");

		mMSButtons.bringToFront();
		mLabelsView.bringToFront();
		// move behaviour defined in VideOSCMainActivity > onTouch()
		mMSButtons.setOnTouchListener(mActivity);

		ok.setOnClickListener(v -> {
			app.setIsMultiSliderActive(false);
			mManager.beginTransaction().remove(mFragment).commit();
			mContainer.removeView(mMSButtons);
			mContainer.removeView(mLabelsView);
			indicatorPanel.setVisibility(View.VISIBLE);
			pixelEditorToolbox.setVisibility(View.VISIBLE);
			snapshotsBar.setVisibility(View.VISIBLE);
			if (app.getIsFPSCalcPanelOpen())
				fpsCalcPanel.setVisibility(View.VISIBLE);
			toolsDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		});

		cancel.setOnClickListener(v -> {
			app.setIsMultiSliderActive(false);

			assert cameraPreview != null;
			SparseArray<Double> redResetVals = cameraPreview.getRedResetValues();
			SparseArray<Double> redResetMixVals = cameraPreview.getRedMixResetValues();
			for (int i = 0; i < redResetVals.size(); i++) {
				cameraPreview.setRedValue(redResetVals.keyAt(i), redResetVals.valueAt(i));
				cameraPreview.setRedMixValue(redResetMixVals.keyAt(i), redResetMixVals.valueAt(i));
			}
			SparseArray<Double> greenResetVals = cameraPreview.getGreenResetValues();
			SparseArray<Double> greenResetMixVals = cameraPreview.getGreenMixResetValues();
			for (int i = 0; i < greenResetVals.size(); i++) {
				cameraPreview.setGreenValue(greenResetVals.keyAt(i), greenResetVals.valueAt(i));
				cameraPreview.setGreenMixValue(greenResetMixVals.keyAt(i), greenResetMixVals.valueAt(i));
			}
			SparseArray<Double> blueResetVals = cameraPreview.getBlueResetValues();
			SparseArray<Double> blueResetMixVals = cameraPreview.getBlueMixResetValues();
			for (int i = 0; i < blueResetVals.size(); i++) {
				cameraPreview.setBlueValue(blueResetVals.keyAt(i), blueResetVals.valueAt(i));
				cameraPreview.setBlueMixValue(blueResetMixVals.keyAt(i), blueResetMixVals.valueAt(i));
			}

			mManager.beginTransaction().remove(mFragment).commit();
			mContainer.removeView(mMSButtons);
			mContainer.removeView(mLabelsView);
			indicatorPanel.setVisibility(View.VISIBLE);
			pixelEditorToolbox.setVisibility(View.VISIBLE);
			snapshotsBar.setVisibility(View.VISIBLE);
			if (app.getIsFPSCalcPanelOpen())
				fpsCalcPanel.setVisibility(View.VISIBLE);
			toolsDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		});
	}

	void setParentContainer(ViewGroup container) {
		this.mParentContainer = container;
	}

	OnCreateViewCallback createViewCallback = null;

	void setCreateViewCallback(OnCreateViewCallback createViewCallback) {
		this.createViewCallback = createViewCallback;
	}

	public interface OnCreateViewCallback {
		void onCreateView();
	}

}
