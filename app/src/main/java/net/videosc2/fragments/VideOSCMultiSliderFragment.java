package net.videosc2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import net.videosc2.R;
import net.videosc2.VideOSCApplication;
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
		View msContainer = inflater.inflate(R.layout.multislider_view, container, false);
		mMSViewLeft = (VideOSCMultiSliderView) msContainer.findViewById(R.id.multislider_view_left);
		mMSViewRight = (VideOSCMultiSliderView) msContainer.findViewById(R.id.multislider_view_right);
		ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mMSViewLeft.getLayoutParams();
		int topMargin = lp.topMargin;
		Bundle numsBundle = this.getArguments();
		ArrayList<Integer> sliderNums = numsBundle.getIntegerArrayList("nums");
		VideOSCApplication app = (VideOSCApplication) getActivity().getApplication();
		float density = app.getScreenDensity();

		assert sliderNums != null;
		for (int num : sliderNums) {
			SliderBar barLeft = new SliderBar(getActivity());
			// sensitive area for touch events should extent to
			// full screenheight, otherwise it's hard to set sliders to
			// minimum or maximum
			mMSViewLeft.setParentTopMargin(topMargin);
			mMSViewLeft.setDisplayHeight(app.getDimensions().y);
			barLeft.mScreenDensity = density;
			barLeft.setNum(String.valueOf(num));
			mMSViewLeft.mBars.add(barLeft);
			mMSViewLeft.addView(barLeft);
			SliderBar barRight = new SliderBar(getActivity());
			mMSViewRight.setParentTopMargin(topMargin);
			mMSViewRight.setDisplayHeight(app.getDimensions().y);
			barRight.mScreenDensity = density;
			barRight.setNum(String.valueOf(num));
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
