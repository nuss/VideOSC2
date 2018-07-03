package net.videosc2.fragments;

import android.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import net.videosc2.R;
import net.videosc2.VideOSCApplication;
import net.videosc2.activities.VideOSCMainActivity;

public class VideOSCMSBaseFragment extends VideOSCBaseFragment {
	final private static String TAG = "VideOSCMSBaseFragment";
	protected ViewGroup mParentContainer;
	protected ViewGroup mContainer;
	protected ViewGroup mOkButton;
	protected FragmentManager mManager;
	protected VideOSCBaseFragment mFragment;

	public VideOSCMSBaseFragment() {}

	@Override
	public void onStart() {
		super.onStart();
		final VideOSCMainActivity activity = (VideOSCMainActivity) getActivity();
		final VideOSCApplication app = (VideOSCApplication) activity.getApplication();
		final DrawerLayout toolsDrawer = activity.mToolsDrawerLayout;
		ImageButton ok = (ImageButton) mOkButton.findViewById(R.id.ok_button);
		final ViewGroup fpsCalcPanel = (ViewGroup) mParentContainer.findViewById(R.id.fps_calc_period_indicator);
		final ViewGroup indicatorPanel = (ViewGroup) mParentContainer.findViewById(R.id.indicator_panel);
		final ViewGroup pixelEditorToolbox = (ViewGroup) mParentContainer.findViewById(R.id.pixel_editor_toolbox);
		final ViewGroup snapshotsBar = (ViewGroup) mParentContainer.findViewById(R.id.snapshots_bar);

		mOkButton.bringToFront();

		ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				app.setIsMultiSliderActive(false);
				mManager.beginTransaction().remove(mFragment).commit();
				mContainer.removeView(mOkButton);
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
