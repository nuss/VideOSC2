package net.videosc2.fragments;

import android.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import net.videosc2.R;
import net.videosc2.VideOSCApplication;

public class VideOSCMSBaseFragment extends VideOSCBaseFragment {
	final private static String TAG = "VideOSCMSBaseFragment";
	protected ViewGroup mParentContainer;
	protected ViewGroup mContainer;
	protected ViewGroup mOkCancel;
	protected FragmentManager mManager;
	protected VideOSCBaseFragment mFragment;

	public VideOSCMSBaseFragment() {}

	@Override
	public void onStart() {
		super.onStart();
		final VideOSCApplication app = (VideOSCApplication) getActivity().getApplication();
		ImageButton ok = (ImageButton) mOkCancel.findViewById(R.id.ok);
		ImageButton cancel = (ImageButton) mOkCancel.findViewById(R.id.cancel);
		final ViewGroup fpsCalcPanel = (ViewGroup) mParentContainer.findViewById(R.id.fps_calc_period_indicator);
		final ViewGroup indicatorPanel = (ViewGroup) mParentContainer.findViewById(R.id.indicator_panel);

		mOkCancel.bringToFront();

		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				app.setIsMultiSliderActive(false);
				mManager.beginTransaction().remove(mFragment).commit();
				mContainer.removeView(mOkCancel);
				indicatorPanel.setVisibility(View.VISIBLE);
				if (app.getIsFPSCalcPanelOpen())
					fpsCalcPanel.setVisibility(View.VISIBLE);
				// TODO: reset pixels that have been fixed from within this multislider view
			}
		});

		ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				app.setIsMultiSliderActive(false);
				mManager.beginTransaction().remove(mFragment).commit();
				mContainer.removeView(mOkCancel);
				indicatorPanel.setVisibility(View.VISIBLE);
				if (app.getIsFPSCalcPanelOpen())
					fpsCalcPanel.setVisibility(View.VISIBLE);
			}
		});
	}


	public void setParentContainer(ViewGroup container) {
		this.mParentContainer = container;
	}

}
