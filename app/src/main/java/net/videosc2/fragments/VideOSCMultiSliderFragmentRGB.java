package net.videosc2.fragments;

import android.os.Bundle;
import android.util.Log;
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
public class VideOSCMultiSliderFragmentRGB extends VideOSCBaseFragment {
	private final static String TAG = "MultiSliderFragmentRGB";
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

		mMSViewRedLeft.setParentTopMargin(topMargin);
		mMSViewRedLeft.setDisplayHeight(app.getDimensions().y);
		mMSViewRedRight.setParentTopMargin(topMargin);
		mMSViewRedRight.setDisplayHeight(app.getDimensions().y);
		mMSViewGreenLeft.setParentTopMargin(topMargin);
		mMSViewGreenLeft.setDisplayHeight(app.getDimensions().y);
		mMSViewGreenRight.setParentTopMargin(topMargin);
		mMSViewGreenRight.setDisplayHeight(app.getDimensions().y);
		mMSViewBlueLeft.setParentTopMargin(topMargin);
		mMSViewBlueLeft.setDisplayHeight(app.getDimensions().y);
		mMSViewBlueRight.setParentTopMargin(topMargin);
		mMSViewBlueRight.setDisplayHeight(app.getDimensions().y);

		assert sliderNums != null;
		for (int num : sliderNums) {
			SliderBar barRedLeft = new SliderBar(getActivity());
			// sensitive area for touch events should extent to
			// full screenheight, otherwise it's hard to set sliders to
			// minimum or maximum
			barRedLeft.setColor(0x99ff0000);
			barRedLeft.mScreenDensity = density;
			barRedLeft.setNum(String.valueOf(num));
			mMSViewRedLeft.mBars.add(barRedLeft);
			mMSViewRedLeft.addView(barRedLeft);
			SliderBar barRedRight = new SliderBar(getActivity());
			barRedRight.setColor(0x99ff0000);
			barRedRight.mScreenDensity = density;
			barRedRight.setNum(String.valueOf(num));
			mMSViewRedRight.mBars.add(barRedRight);
			mMSViewRedRight.addView(barRedRight);

			SliderBar barGreenLeft = new SliderBar(getActivity());
			// sensitive area for touch events should extent to
			// full screenheight, otherwise it's hard to set sliders to
			// minimum or maximum
			barGreenLeft.setColor(0x9900ff00);
			barGreenLeft.mScreenDensity = density;
			barGreenLeft.setNum(String.valueOf(num));
			mMSViewGreenLeft.mBars.add(barGreenLeft);
			mMSViewGreenLeft.addView(barGreenLeft);
			SliderBar barGreenRight = new SliderBar(getActivity());
			barGreenRight.setColor(0x9900ff00);
			barGreenRight.mScreenDensity = density;
			barGreenRight.setNum(String.valueOf(num));
			mMSViewGreenRight.mBars.add(barGreenRight);
			mMSViewGreenRight.addView(barGreenRight);

			SliderBar barBlueLeft = new SliderBar(getActivity());
			// sensitive area for touch events should extent to
			// full screenheight, otherwise it's hard to set sliders to
			// minimum or maximum
			barBlueLeft.setColor(0x990000ff);
			barBlueLeft.mScreenDensity = density;
			barBlueLeft.setNum(String.valueOf(num));
			mMSViewBlueLeft.mBars.add(barBlueLeft);
			mMSViewBlueLeft.addView(barBlueLeft);
			SliderBar barBlueRight = new SliderBar(getActivity());
			barBlueRight.setColor(0x990000ff);
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
