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
public class VideOSCMultiSliderFragment extends VideOSCMSBaseFragment {
	private final static String TAG = "MultiSliderFragment";
	private VideOSCMultiSliderView mMSViewRight;
	private VideOSCMultiSliderView mMSViewLeft;

	// empty public constructor
	public VideOSCMultiSliderFragment() {
		super();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		int color = 0x66ffffff;
		double[] vals = new double[]{};
		double[] mixVals = new double[]{};
		final VideOSCMainActivity activity = (VideOSCMainActivity) getActivity();
		assert activity != null;
		final VideOSCApplication app = (VideOSCApplication) activity.getApplication();
		final Point resolution = app.getResolution();
		final int numTotalPixels = resolution.x * resolution.y;

		mManager = getFragmentManager();

		final Bundle argsBundle = this.getArguments();
		assert argsBundle != null;
		final ArrayList<Integer> sliderNums = argsBundle.getIntegerArrayList("nums");
		switch (app.getColorMode()) {
			case R:
				vals = argsBundle.getDoubleArray("redVals");
				mixVals = argsBundle.getDoubleArray("redMixVals");
				break;
			case G:
				vals = argsBundle.getDoubleArray("greenVals");
				mixVals = argsBundle.getDoubleArray("greenMixVals");
				break;
			case B:
				vals = argsBundle.getDoubleArray("blueVals");
				mixVals = argsBundle.getDoubleArray("blueMixVals");
				break;
		}

		View msContainer = inflater.inflate(R.layout.multislider_view, container, false);
		mMSViewLeft = msContainer.findViewById(R.id.multislider_view_left);
		mMSViewLeft.setValuesArray(numTotalPixels);
		mMSViewLeft.setContainerView(container);

		mMSButtons = (ViewGroup) inflater.inflate(R.layout.multislider_buttons, container, false);

		mMSViewRight = msContainer.findViewById(R.id.multislider_view_right);
		mMSViewRight.setValuesArray(numTotalPixels);
		mMSViewRight.setContainerView(container);

		// colors are determining slider positions on the left
		mMSViewLeft.setValues(vals);
		mMSViewRight.setValues(mixVals);

		ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mMSViewLeft.getLayoutParams();
		int topMargin = lp.topMargin;
		float density = app.getScreenDensity();

		int displayHeight = app.getDimensions().y;
		mMSViewLeft.setParentTopMargin(topMargin);
		mMSViewLeft.setDisplayHeight(displayHeight);
		mMSViewRight.setParentTopMargin(topMargin);
		mMSViewRight.setDisplayHeight(displayHeight);

		switch (app.getColorMode()) {
			case R:
				color = 0x99ff0000;
				break;
			case G:
				color = 0x9900ff00;
				break;
			case B:
				color = 0x990000ff;
				break;
		}

		assert sliderNums != null;

		for (int i = 0; i < sliderNums.size(); i++) {
			SliderBar barLeft = new SliderBar(getActivity());
			// sensitive area for touch events should extent to
			// full screenheight, otherwise it's hard to set sliders to
			// minimum or maximum
			barLeft.mScreenDensity = density;
			barLeft.setNum(String.valueOf(sliderNums.get(i)));
			barLeft.setColor(color);
			mMSViewLeft.mBars.add(barLeft);
			mMSViewLeft.addView(barLeft);
			SliderBar barRight = new SliderBar(getActivity());
			barRight.mScreenDensity = density;
			barRight.setNum(String.valueOf(sliderNums.get(i)));
			barRight.setColor(color);
			mMSViewRight.mBars.add(barRight);
			mMSViewRight.addView(barRight);
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
		mMSViewLeft.setSliderNums(sliderNums);
		mMSViewRight.setSliderNums(sliderNums);
	}
}
