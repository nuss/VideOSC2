package net.videosc2.fragments;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
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
	private VideOSCApplication mApp;
	private VideOSCMultiSliderView mMSViewRight;
	private VideOSCMultiSliderView mMSViewLeft;

	// empty public constructor
	public VideOSCMultiSliderFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View mMSContainer = inflater.inflate(R.layout.multislider_view, container, false);
		mMSViewLeft = (VideOSCMultiSliderView) mMSContainer.findViewById(R.id.multislider_view_left);
		mMSViewRight = (VideOSCMultiSliderView) mMSContainer.findViewById(R.id.multislider_view_right);
		Bundle numsBundle = this.getArguments();
		ArrayList<Integer> sliderNums = numsBundle.getIntegerArrayList("nums");

		ArrayList<SliderBar> slidersLeft = new ArrayList<>();
		ArrayList<SliderBar> slidersRight = new ArrayList<>();
		assert sliderNums != null;
		for (int num : sliderNums) {
			SliderBar barLeft = new SliderBar(getActivity());
			barLeft.setNum(String.valueOf(num));
			slidersLeft.add(barLeft);
			SliderBar barRight = new SliderBar(getActivity());
			barRight.setNum(String.valueOf(num));
			slidersRight.add(barRight);
		}

		for (SliderBar slider : slidersLeft) {
			mMSViewLeft.addView(slider);
		}
		for (SliderBar slider : slidersRight) {
			mMSViewRight.addView(slider);
		}

		setSliderProps(sliderNums);

		return mMSContainer;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "activity created");
	}

	private void setSliderProps(ArrayList<Integer> sliderNums) {
		mMSViewLeft.setSliderNums(sliderNums);
		mMSViewRight.setSliderNums(sliderNums);
	}
}
