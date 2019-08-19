package net.videosc.fragments;

import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.utilities.VideOSCUIHelpers;
import net.videosc.views.SliderBar;
import net.videosc.views.VideOSCMultiSliderView;

import java.util.ArrayList;

import androidx.annotation.NonNull;

/**
 * Created by stefan on 19.05.18, package net.videosc.views, project VideOSC22.
 */
public class VideOSCMultiSliderFragmentRGB extends VideOSCMSBaseFragment {
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
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		final VideOSCMainActivity activity = (VideOSCMainActivity) getActivity();
		assert activity != null;
		final VideOSCApplication app = (VideOSCApplication) activity.getApplication();
		final Point resolution = app.getResolution();
		final int numTotalPixels = resolution.x * resolution.y;

		mManager = getFragmentManager();

		final Bundle argsBundle = this.getArguments();
		assert argsBundle != null;
		final ArrayList<Integer> sliderNums = argsBundle.getIntegerArrayList("nums");

		final double[] redVals = argsBundle.getDoubleArray("redVals");
		final double[] redMixVals = argsBundle.getDoubleArray("redMixVals");
		final double[] greenVals = argsBundle.getDoubleArray("greenVals");
		final double[] greenMixVals = argsBundle.getDoubleArray("greenMixVals");
		final double[] blueVals = argsBundle.getDoubleArray("blueVals");
		final double[] blueMixVals = argsBundle.getDoubleArray("blueMixVals");

		View msContainer = inflater.inflate(R.layout.multislider_view_rgb, container, false);
		mMSViewRedLeft = msContainer.findViewById(R.id.multislider_view_r_left);
		mMSViewRedLeft.setValuesArray(numTotalPixels);
		mMSViewRedLeft.setContainerView(container);
		mMSViewRedRight = msContainer.findViewById(R.id.multislider_view_r_right);
		mMSViewRedRight.setValuesArray(numTotalPixels);
		mMSViewRedRight.setContainerView(container);
		mMSViewGreenLeft = msContainer.findViewById(R.id.multislider_view_g_left);
		mMSViewGreenLeft.setValuesArray(numTotalPixels);
		mMSViewGreenLeft.setContainerView(container);
		mMSViewGreenRight = msContainer.findViewById(R.id.multislider_view_g_right);
		mMSViewGreenRight.setValuesArray(numTotalPixels);
		mMSViewGreenRight.setContainerView(container);
		mMSViewBlueLeft = msContainer.findViewById(R.id.multislider_view_b_left);
		mMSViewBlueLeft.setValuesArray(numTotalPixels);
		mMSViewBlueLeft.setContainerView(container);
		mMSViewBlueRight = msContainer.findViewById(R.id.multislider_view_b_right);
		mMSViewBlueRight.setValuesArray(numTotalPixels);
		mMSViewBlueRight.setContainerView(container);

		mMSViewRedLeft.setValues(redVals);
		mMSViewRedRight.setValues(redMixVals);
		mMSViewGreenLeft.setValues(greenVals);
		mMSViewGreenRight.setValues(greenMixVals);
		mMSViewBlueLeft.setValues(blueVals);
		mMSViewBlueRight.setValues(blueMixVals);

		ViewGroup column = msContainer.findViewById(R.id.multislider_rgb_left_column);
		ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) column.getLayoutParams();
		int topMargin = lp.topMargin;
		float density = app.getScreenDensity();
		int displayHeight = app.getDimensions().y;

		mMSViewRedLeft.setParentTopMargin(topMargin);
		mMSViewRedLeft.setDisplayHeight(displayHeight);
		mMSViewRedRight.setParentTopMargin(topMargin);
		mMSViewRedRight.setDisplayHeight(displayHeight);
		mMSViewGreenLeft.setParentTopMargin(topMargin);
		mMSViewGreenLeft.setDisplayHeight(displayHeight);
		mMSViewGreenRight.setParentTopMargin(topMargin);
		mMSViewGreenRight.setDisplayHeight(displayHeight);
		mMSViewBlueLeft.setParentTopMargin(topMargin);
		mMSViewBlueLeft.setDisplayHeight(displayHeight);
		mMSViewBlueRight.setParentTopMargin(topMargin);
		mMSViewBlueRight.setDisplayHeight(displayHeight);

		mMSButtons = (ViewGroup) inflater.inflate(R.layout.multislider_buttons, container, false);

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
			// sensitive area for touch events should extend to
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
			// sensitive area for touch events should extend to
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

		VideOSCUIHelpers.addView(mMSButtons, container);

		mContainer = container;
		mFragment = this;

		return msContainer;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (createViewCallback != null) {
			createViewCallback.onCreateView();
			createViewCallback = null;
		}
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
