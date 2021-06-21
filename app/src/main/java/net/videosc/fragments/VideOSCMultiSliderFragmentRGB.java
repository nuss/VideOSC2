package net.videosc.fragments;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;
import net.videosc.utilities.VideOSCUIHelpers;
import net.videosc.views.SliderBar;
import net.videosc.views.VideOSCMultiSliderView;

import java.util.ArrayList;

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
	public VideOSCMultiSliderFragmentRGB() { }

	public VideOSCMultiSliderFragmentRGB(Context context) {
		this.mActivity = (VideOSCMainActivity) context;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		this.mContainer = container;
		this.mInflater = inflater;
		return inflater.inflate(R.layout.multislider_view_rgb, container, false);
	}

	/**
	 * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
	 * has returned, but before any saved state has been restored in to the view.
	 * This gives subclasses a chance to initialize themselves once
	 * they know their view hierarchy has been completely created.  The fragment's
	 * view hierarchy is not however attached to its parent at this point.
	 *
	 * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
	 * @param savedInstanceState If non-null, this fragment is being re-constructed
	 */
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mLabelsView = mInflater.inflate(R.layout.multislider_labels, mContainer, false);

		final VideOSCApplication app = (VideOSCApplication) mActivity.getApplication();
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

		mMSViewRedLeft = view.findViewById(R.id.multislider_view_r_left);
		mMSViewRedLeft.setValuesArray(numTotalPixels);
		mMSViewRedLeft.setContainerView(mContainer);
		mMSViewRedRight = view.findViewById(R.id.multislider_view_r_right);
		mMSViewRedRight.setValuesArray(numTotalPixels);
		mMSViewRedRight.setContainerView(mContainer);
		mMSViewGreenLeft = view.findViewById(R.id.multislider_view_g_left);
		mMSViewGreenLeft.setValuesArray(numTotalPixels);
		mMSViewGreenLeft.setContainerView(mContainer);
		mMSViewGreenRight = view.findViewById(R.id.multislider_view_g_right);
		mMSViewGreenRight.setValuesArray(numTotalPixels);
		mMSViewGreenRight.setContainerView(mContainer);
		mMSViewBlueLeft = view.findViewById(R.id.multislider_view_b_left);
		mMSViewBlueLeft.setValuesArray(numTotalPixels);
		mMSViewBlueLeft.setContainerView(mContainer);
		mMSViewBlueRight = view.findViewById(R.id.multislider_view_b_right);
		mMSViewBlueRight.setValuesArray(numTotalPixels);
		mMSViewBlueRight.setContainerView(mContainer);

		mMSViewRedLeft.setValues(redVals);
		mMSViewRedRight.setValues(redMixVals);
		mMSViewGreenLeft.setValues(greenVals);
		mMSViewGreenRight.setValues(greenMixVals);
		mMSViewBlueLeft.setValues(blueVals);
		mMSViewBlueRight.setValues(blueMixVals);

		ViewGroup column = view.findViewById(R.id.multislider_rgb_left_column);
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

		mMSButtons = mInflater.inflate(R.layout.multislider_buttons, mContainer, false);

		assert sliderNums != null;
		for (int num : sliderNums) {
			SliderBar barRedLeft = new SliderBar(mActivity);
			// sensitive area for touch events should extent to
			// full screenheight, otherwise it's hard to set sliders to
			// minimum or maximum
			barRedLeft.setColor(0x99ff0000);
			barRedLeft.mScreenDensity = density;
			barRedLeft.setNum(String.valueOf(num));
			mMSViewRedLeft.mBars.add(barRedLeft);
			mMSViewRedLeft.addView(barRedLeft);
			SliderBar barRedRight = new SliderBar(mActivity);
			barRedRight.setColor(0x99ff0000);
			barRedRight.mScreenDensity = density;
			barRedRight.setNum(String.valueOf(num));
			mMSViewRedRight.mBars.add(barRedRight);
			mMSViewRedRight.addView(barRedRight);

			SliderBar barGreenLeft = new SliderBar(mActivity);
			// sensitive area for touch events should extend to
			// full screenheight, otherwise it's hard to set sliders to
			// minimum or maximum
			barGreenLeft.setColor(0x9900ff00);
			barGreenLeft.mScreenDensity = density;
			barGreenLeft.setNum(String.valueOf(num));
			mMSViewGreenLeft.mBars.add(barGreenLeft);
			mMSViewGreenLeft.addView(barGreenLeft);
			SliderBar barGreenRight = new SliderBar(mActivity);
			barGreenRight.setColor(0x9900ff00);
			barGreenRight.mScreenDensity = density;
			barGreenRight.setNum(String.valueOf(num));
			mMSViewGreenRight.mBars.add(barGreenRight);
			mMSViewGreenRight.addView(barGreenRight);

			SliderBar barBlueLeft = new SliderBar(mActivity);
			// sensitive area for touch events should extend to
			// full screenheight, otherwise it's hard to set sliders to
			// minimum or maximum
			barBlueLeft.setColor(0x990000ff);
			barBlueLeft.mScreenDensity = density;
			barBlueLeft.setNum(String.valueOf(num));
			mMSViewBlueLeft.mBars.add(barBlueLeft);
			mMSViewBlueLeft.addView(barBlueLeft);
			SliderBar barBlueRight = new SliderBar(mActivity);
			barBlueRight.setColor(0x990000ff);
			barBlueRight.mScreenDensity = density;
			barBlueRight.setNum(String.valueOf(num));
			mMSViewBlueRight.mBars.add(barBlueRight);
			mMSViewBlueRight.addView(barBlueRight);
		}

		setSliderProps(sliderNums);

		VideOSCUIHelpers.addView(mMSButtons, mContainer);
		VideOSCUIHelpers.addView(mLabelsView, mContainer);

		mFragment = this;

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mCreateViewCallback != null) {
			mCreateViewCallback.onCreateView();
			mCreateViewCallback = null;
		}
	}

	/**
	 * Called when the fragment is no longer attached to its activity.  This
	 * is called after {@link #onDestroy()}.
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		this.mActivity = null;
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
