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
		mApp = (VideOSCApplication) getActivity().getApplication();
		mMSContainer = inflater.inflate(R.layout.multislider_view, container, false);
		mMSViewLeft = (VideOSCMultiSliderView) mMSContainer.findViewById(R.id.multislider_view_left);
		mMSViewRight = (VideOSCMultiSliderView) mMSContainer.findViewById(R.id.multislider_view_right);
//		Log.d(TAG, "mMSContainer is " + mMSContainer.getClass());

		return mMSContainer;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "activity created");
		addSliders();
	}

	private void addSliders() {
		if (mMSContainer.findViewById(R.id.multislider_view) != null) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT
			);

			Point dimensions = mApp.getDimensions();
			Bundle numsBundle = this.getArguments();
			ArrayList<Integer> sliderNums = numsBundle.getIntegerArrayList("nums");
			ArrayList<SliderBar> slidersLeft = new ArrayList<>();
			ArrayList<SliderBar> slidersRight = new ArrayList<>();

			assert sliderNums != null;
//		ViewGroup left = (ViewGroup) mMSContainer.findViewById(R.id.multislider_view_left);
//		ViewGroup right = (ViewGroup) mMSContainer.findViewById(R.id.multislider_view_right);

			ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mMSViewLeft.getLayoutParams();
			int barHeight = dimensions.y - lp.bottomMargin - lp.topMargin;
			int barWidth = (dimensions.x / 2 - lp.leftMargin - lp.rightMargin) / sliderNums.size();

//			Log.d(TAG, "bar height: " + barHeight + ", bar width: " + barWidth);

			for (int num : sliderNums) {
				SliderBar barLeft = new SliderBar(getActivity());
				barLeft.setNum(String.valueOf(num));
				SliderBar barRight = new SliderBar(getActivity());
				barRight.setNum(String.valueOf(num));
				slidersLeft.add(barLeft);
				slidersRight.add(barRight);
//			x = x + barWidth;
//				Button testButton = new Button(getActivity());
//				testButton.setWidth(dimensions.x/2/sliderNums.size());
//				mMSViewLeft.addView(testButton);
			}

			int x = 0;
			for (SliderBar slider : slidersLeft) {
				// TODO slider needs to know its touchY within the instance to draw its bar properly
				// maybe keep Ys in an array symmetrically to slidersLeft
				// slider.setTouchY();
//				Log.d(TAG, "mMSViewLeft: " + mMSViewLeft.getChildCount());
				mMSViewLeft.addView(slider);
				slider.layout(x, 0, x + barWidth, barHeight);
				x += barWidth;
//				Log.d(TAG, "slider props: " + slider.getX() + ", " + slider.getY() + ", " + slider.getWidth() + ", " + slider.getHeight());
			}
			x = 0;
			for (SliderBar slider : slidersRight) {
				mMSViewRight.addView(slider);
				slider.layout(x, 0, x + barWidth, barHeight);
				x += barWidth;
			}
		}
	}

}
