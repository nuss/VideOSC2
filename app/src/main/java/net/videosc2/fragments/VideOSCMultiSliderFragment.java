package net.videosc2.fragments;

import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import net.videosc2.R;
import net.videosc2.VideOSCApplication;
import net.videosc2.utilities.enums.RGBModes;
import net.videosc2.views.SliderBar;
import net.videosc2.views.VideOSCMultiSliderView;
import java.util.ArrayList;

/**
 * Created by stefan on 19.05.18, package net.videosc2.views, project VideOSC22.
 */
public class VideOSCMultiSliderFragment extends VideOSCBaseFragment {
	private final static String TAG = "MultiSliderFragment";
	private VideOSCMultiSliderView mMSViewRight;
	private VideOSCMultiSliderView mMSViewLeft;

	// empty public constructor
	public VideOSCMultiSliderFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		int color = 0x66ffffff;
		VideOSCApplication app = (VideOSCApplication) getActivity().getApplication();
		Point resolution = app.getResolution();
		int numTotalPixels = resolution.x * resolution.y;
		View msContainer = inflater.inflate(R.layout.multislider_view, container, false);
		mMSViewLeft = (VideOSCMultiSliderView) msContainer.findViewById(R.id.multislider_view_left);
		mMSViewLeft.setValuesArray(numTotalPixels);
		mMSViewRight = (VideOSCMultiSliderView) msContainer.findViewById(R.id.multislider_view_right);
		mMSViewRight.setValuesArray(numTotalPixels);
		ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mMSViewLeft.getLayoutParams();
		int topMargin = lp.topMargin;
		Bundle numsBundle = this.getArguments();
		ArrayList<Integer> sliderNums = numsBundle.getIntegerArrayList("nums");
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
		for (int num : sliderNums) {
			SliderBar barLeft = new SliderBar(getActivity());
			// sensitive area for touch events should extent to
			// full screenheight, otherwise it's hard to set sliders to
			// minimum or maximum
			barLeft.mScreenDensity = density;
			barLeft.setNum(String.valueOf(num));
			barLeft.setColor(color);
			mMSViewLeft.mBars.add(barLeft);
			mMSViewLeft.addView(barLeft);
			SliderBar barRight = new SliderBar(getActivity());
			barRight.mScreenDensity = density;
			barRight.setNum(String.valueOf(num));
			barRight.setColor(color);
			mMSViewRight.mBars.add(barRight);
			mMSViewRight.addView(barRight);
		}

		setSliderProps(sliderNums);

		return msContainer;
	}

	private void setSliderProps(ArrayList<Integer> sliderNums) {
		mMSViewLeft.setSliderNums(sliderNums);
		mMSViewRight.setSliderNums(sliderNums);
	}
}
