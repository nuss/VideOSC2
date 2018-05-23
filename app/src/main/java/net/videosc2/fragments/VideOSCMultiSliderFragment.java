package net.videosc2.fragments;

import android.graphics.Point;
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
public class VideOSCMultiSliderFragment extends VideOSCBaseFragment {
	private final static String TAG = "MultiSliderFragment";
	private VideOSCApplication mApp;
	private View mMSContainer;
	private VideOSCMultiSliderView mMSViewRight;
	private VideOSCMultiSliderView mMSViewLeft;
//	private int barHeight, barWidth;

	// empty public constructor
	public VideOSCMultiSliderFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
//		mApp = (VideOSCApplication) getActivity().getApplication();
		mMSContainer = inflater.inflate(R.layout.multislider_view, container, false);
		mMSViewLeft = (VideOSCMultiSliderView) mMSContainer.findViewById(R.id.multislider_view_left);
		mMSViewRight = (VideOSCMultiSliderView) mMSContainer.findViewById(R.id.multislider_view_right);
		Bundle numsBundle = this.getArguments();
		ArrayList<Integer> sliderNums = numsBundle.getIntegerArrayList("nums");

//		Log.d(TAG, "mMSContainer is " + mMSContainer.getClass());
		setSliderProps(sliderNums);

		return mMSContainer;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "activity created");
	}

	private void setSliderProps(ArrayList<Integer> sliderNums) {
		VideOSCApplication app = (VideOSCApplication) getActivity().getApplication();
		Point screenDimensions = app.getDimensions();
		mMSViewLeft.setScreenDimensions(screenDimensions);
		mMSViewRight.setScreenDimensions(screenDimensions);
		mMSViewLeft.setSliderNums(sliderNums);
		mMSViewRight.setSliderNums(sliderNums);
	}
}
