package net.videosc2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.videosc2.R;
import net.videosc2.VideOSCApplication;
import net.videosc2.views.SliderBar;
import net.videosc2.views.VideOSCMultiSliderView;

import java.util.ArrayList;

/**
 * Created by stefan on 19.05.18, package net.videosc2.views, project VideOSC22.
 */

// TODO: define a way to colorize sliderbars

public class VideOSCMultiSliderFragmentRGB extends VideOSCBaseFragment {
	private final static String TAG = "MultiSliderFragment";
	private VideOSCMultiSliderView mMSViewRedRight;
	private VideOSCMultiSliderView mMSViewRedLeft;
	private VideOSCMultiSliderView mMSViewGreenRight;
	private VideOSCMultiSliderView mMSViewGreenLeft;
	private VideOSCMultiSliderView mMSViewBlueRight;
	private VideOSCMultiSliderView mMSViewBlueLeft;

	// empty public constructor
	public VideOSCMultiSliderFragmentRGB() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View msContainer = inflater.inflate(R.layout.multislider_view_rgb, container, false);
		mMSViewRedLeft = (VideOSCMultiSliderView) msContainer.findViewById(R.id.multislider_view_r_left);
		mMSViewRedRight = (VideOSCMultiSliderView) msContainer.findViewById(R.id.multislider_view_r_right);
		mMSViewGreenLeft = (VideOSCMultiSliderView) msContainer.findViewById(R.id.multislider_view_g_left);
		mMSViewGreenRight = (VideOSCMultiSliderView) msContainer.findViewById(R.id.multislider_view_g_right);
		mMSViewBlueLeft = (VideOSCMultiSliderView) msContainer.findViewById(R.id.multislider_view_b_left);
		mMSViewBlueRight = (VideOSCMultiSliderView) msContainer.findViewById(R.id.multislider_view_b_right);

		ViewGroup column = (ViewGroup) msContainer.findViewById(R.id.multislider_rgb_left_column);
		ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) column.getLayoutParams();
		int topMargin = lp.topMargin;
		Bundle numsBundle = this.getArguments();
		ArrayList<Integer> sliderNums = numsBundle.getIntegerArrayList("nums");
		VideOSCApplication app = (VideOSCApplication) getActivity().getApplication();
		float density = app.getScreenDensity();

		assert sliderNums != null;
		for (int num : sliderNums) {
			SliderBar barRedLeft = new SliderBar(getActivity());
			// sensitive area for touch events should extent to
			// full screenheight, otherwise it's hard to set sliders to
			// minimum or maximum
			barRedLeft.areaTop = 0 - topMargin;
			barRedLeft.areaBottom = app.getDimensions().y;
			barRedLeft.mScreenDensity = density;
			barRedLeft.setNum(String.valueOf(num));
			mMSViewRedLeft.mBars.add(barRedLeft);
			mMSViewRedLeft.addView(barRedLeft);
			SliderBar barRedRight = new SliderBar(getActivity());
			barRedRight.areaTop = 0 - topMargin;
			barRedRight.areaBottom = app.getDimensions().y;
			barRedRight.mScreenDensity = density;
			barRedRight.setNum(String.valueOf(num));
			mMSViewRedRight.mBars.add(barRedRight);
			mMSViewRedRight.addView(barRedRight);

			SliderBar barGreenLeft = new SliderBar(getActivity());
			// sensitive area for touch events should extent to
			// full screenheight, otherwise it's hard to set sliders to
			// minimum or maximum
			// FIXME
			barGreenLeft.areaTop = 0 - topMargin; // ??? should be topMargin - barGreenLeft.getTop()
			barGreenLeft.areaBottom = app.getDimensions().y;
			barGreenLeft.mScreenDensity = density;
			barGreenLeft.setNum(String.valueOf(num));
			mMSViewGreenLeft.mBars.add(barGreenLeft);
			mMSViewGreenLeft.addView(barGreenLeft);
			SliderBar barGreenRight = new SliderBar(getActivity());
			// FIXME
			barGreenRight.areaTop = 0 - topMargin;
			barGreenRight.areaBottom = app.getDimensions().y;
			barGreenRight.mScreenDensity = density;
			barGreenRight.setNum(String.valueOf(num));
			mMSViewGreenRight.mBars.add(barGreenRight);
			mMSViewGreenRight.addView(barGreenRight);

			SliderBar barBlueLeft = new SliderBar(getActivity());
			// sensitive area for touch events should extent to
			// full screenheight, otherwise it's hard to set sliders to
			// minimum or maximum
			// FIXME
			barBlueLeft.areaTop = 0 - topMargin;
			barBlueLeft.areaBottom = app.getDimensions().y;
			barBlueLeft.mScreenDensity = density;
			barBlueLeft.setNum(String.valueOf(num));
			mMSViewBlueLeft.mBars.add(barBlueLeft);
			mMSViewBlueLeft.addView(barBlueLeft);
			SliderBar barBlueRight = new SliderBar(getActivity());
			// FIXME
			barBlueRight.areaTop = 0 - topMargin;
			barBlueRight.areaBottom = app.getDimensions().y;
			barBlueRight.mScreenDensity = density;
			barBlueRight.setNum(String.valueOf(num));
			mMSViewBlueRight.mBars.add(barBlueRight);
			mMSViewBlueRight.addView(barBlueRight);
		}

		setSliderProps(sliderNums);

		return msContainer;
	}

	private void setSliderProps(ArrayList<Integer> sliderNums) {
		mMSViewRedLeft.setSliderNums(sliderNums);
		mMSViewRedRight.setSliderNums(sliderNums);
		mMSViewGreenLeft.setSliderNums(sliderNums);
		mMSViewGreenRight.setSliderNums(sliderNums);
		mMSViewBlueLeft.setSliderNums(sliderNums);
		mMSViewBlueRight.setSliderNums(sliderNums);
	}
}
