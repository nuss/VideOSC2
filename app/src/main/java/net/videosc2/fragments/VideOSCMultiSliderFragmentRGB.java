package net.videosc2.fragments;

import android.app.FragmentManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import net.videosc2.R;
import net.videosc2.VideOSCApplication;
import net.videosc2.utilities.VideOSCUIHelpers;
import net.videosc2.views.SliderBar;
import net.videosc2.views.VideOSCMultiSliderView;

import java.util.ArrayList;

/**
 * Created by stefan on 19.05.18, package net.videosc2.views, project VideOSC22.
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		VideOSCApplication app = (VideOSCApplication) getActivity().getApplication();
		Point resolution = app.getResolution();
		int numTotalPixels = resolution.x * resolution.y;

		mManager = getFragmentManager();

		Bundle argsBundle = this.getArguments();
		ArrayList<Integer> sliderNums = argsBundle.getIntegerArrayList("nums");
		int[] allColors = argsBundle.getIntArray("colors");

		View msContainer = inflater.inflate(R.layout.multislider_view_rgb, container, false);
		mMSViewRedLeft = (VideOSCMultiSliderView) msContainer.findViewById(R.id.multislider_view_r_left);
		mMSViewRedLeft.setValuesArray(numTotalPixels);
		mMSViewRedLeft.setSlidersTouchedArray(numTotalPixels);
		mMSViewRedLeft.setContainerView(container);
		mMSViewRedRight = (VideOSCMultiSliderView) msContainer.findViewById(R.id.multislider_view_r_right);
		mMSViewRedRight.setValuesArray(numTotalPixels);
		mMSViewRedRight.setSlidersTouchedArray(numTotalPixels);
		mMSViewRedRight.setContainerView(container);
		mMSViewGreenLeft = (VideOSCMultiSliderView) msContainer.findViewById(R.id.multislider_view_g_left);
		mMSViewGreenLeft.setValuesArray(numTotalPixels);
		mMSViewGreenLeft.setSlidersTouchedArray(numTotalPixels);
		mMSViewGreenLeft.setContainerView(container);
		mMSViewGreenRight = (VideOSCMultiSliderView) msContainer.findViewById(R.id.multislider_view_g_right);
		mMSViewGreenRight.setValuesArray(numTotalPixels);
		mMSViewGreenRight.setSlidersTouchedArray(numTotalPixels);
		mMSViewGreenRight.setContainerView(container);
		mMSViewBlueLeft = (VideOSCMultiSliderView) msContainer.findViewById(R.id.multislider_view_b_left);
		mMSViewBlueLeft.setValuesArray(numTotalPixels);
		mMSViewBlueLeft.setSlidersTouchedArray(numTotalPixels);
		mMSViewBlueLeft.setContainerView(container);
		mMSViewBlueRight = (VideOSCMultiSliderView) msContainer.findViewById(R.id.multislider_view_b_right);
		mMSViewBlueRight.setValuesArray(numTotalPixels);
		mMSViewBlueRight.setSlidersTouchedArray(numTotalPixels);
		mMSViewBlueRight.setContainerView(container);

		assert allColors != null;
		int[] reds = new int[allColors.length];
		int[] greens = new int[allColors.length];
		int[] blues = new int[allColors.length];
		for (int i = 0; i < allColors.length; i++) {
			reds[i] = (allColors[i] >> 16) & 0xFF;
			greens[i] = (allColors[i] >> 8) & 0xFF;
			blues[i] = allColors[i] & 0xFF;
		}

		mMSViewRedLeft.setColors(reds);
		mMSViewGreenLeft.setColors(greens);
		mMSViewBlueLeft.setColors(blues);

		ViewGroup column = (ViewGroup) msContainer.findViewById(R.id.multislider_rgb_left_column);
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
		mOkCancel = (ViewGroup) inflater.inflate(R.layout.cancel_ok_buttons, container, false);

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
		VideOSCUIHelpers.addView(mOkCancel, container);
		Resources res = getResources();
		Drawable shape = res.getDrawable(R.drawable.black_rounded_rect);
		mOkCancel.setBackground(shape);

		mContainer = container;
		mFragment = this;

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
