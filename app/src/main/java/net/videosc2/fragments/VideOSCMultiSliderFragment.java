package net.videosc2.fragments;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import net.videosc2.R;
import net.videosc2.VideOSCApplication;
import net.videosc2.views.SliderBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by stefan on 19.05.18, package net.videosc2.views, project VideOSC22.
 */
public class VideOSCMultiSliderFragment extends VideOSCBaseFragment {
	private final static String TAG = "MultiSliderFragment";
	private VideOSCApplication mApp;
	private View mMSContainer;
	private MultiSliderView mMSViewRight;
	private MultiSliderView mMSViewLeft;
	private int barHeight, barWidth;

	// empty public constructor
	public VideOSCMultiSliderFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		mApp = (VideOSCApplication) getActivity().getApplication();
		mMSViewLeft = new MultiSliderView(getActivity());
		mMSViewRight = new MultiSliderView(getActivity());
		mMSContainer = inflater.inflate(R.layout.multislider_view, container, false);
		Log.d(TAG, "mMSContainer is " + mMSContainer.getClass());

		return mMSContainer;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		addSliders();
	}

	private void addSliders() {
		ViewGroup left;
		ViewGroup right;
		if (mMSContainer.findViewById(R.id.multislider_view) != null) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT
			);
			left = (ViewGroup) mMSContainer.findViewById(R.id.multislider_view_left);
			left.addView(mMSViewLeft, params);
			right = (ViewGroup) mMSContainer.findViewById(R.id.multislider_view_right);
			right.addView(mMSViewRight, params);

			Point dimensions = mApp.getDimensions();
			Bundle numsBundle = this.getArguments();
			ArrayList<Integer> sliderNums = numsBundle.getIntegerArrayList("nums");
			ArrayList<SliderBar> slidersLeft = new ArrayList<>();
			ArrayList<SliderBar> slidersRight = new ArrayList<>();

			assert sliderNums != null;
//		ViewGroup left = (ViewGroup) mMSContainer.findViewById(R.id.multislider_view_left);
//		ViewGroup right = (ViewGroup) mMSContainer.findViewById(R.id.multislider_view_right);

			ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) left.getLayoutParams();
			barHeight = dimensions.y - lp.bottomMargin - lp.topMargin;
			barWidth = (dimensions.x / 2 - lp.leftMargin - lp.rightMargin) / sliderNums.size();

			Log.d(TAG, "bar height: " + barHeight + ", bar width: " + barWidth);

			int x = 0;
			for (int num : sliderNums) {
				slidersLeft.add(new SliderBar(getActivity(), x, 0, x + barWidth, barHeight, 0, String.valueOf(num)));
				slidersRight.add(new SliderBar(getActivity(), x, 0, x + barWidth, barHeight, 0, String.valueOf(num)));
//			x = x + barWidth;
//				Button testButton = new Button(getActivity());
//				testButton.setWidth(dimensions.x/2/sliderNums.size());
//				mMSViewLeft.addView(testButton);
			}

			for (SliderBar slider : slidersLeft) {
				mMSViewLeft.addView(slider);
//				Log.d(TAG, "slider props: " + slider.getX() + ", " + slider.getY() + ", " + slider.getWidth() + ", " + slider.getHeight());
			}
			for (SliderBar slider : slidersRight) {
				mMSViewRight.addView(slider);
			}
		}
	}

	public class MultiSliderView extends ViewGroup {
		private LayoutParams params = new LayoutParams(barWidth, barHeight);

		public MultiSliderView(Context context) {
			super(context);
		}

		@Override
		protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
			Log.d(TAG, "MultiSliderView on layout: " + left + ", " + top + ", " + right + ", " + bottom);

		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			Log.d(TAG, "number of children: " + getChildCount());
			Log.v(TAG, "width measure spec: " + MeasureSpec.toString(widthMeasureSpec));
			Log.v(TAG, "height measure spec: " + MeasureSpec.toString(heightMeasureSpec));

			int desiredWidth = getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight();
			int desiredHeight = getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom();

			int measureWidth = measureDimension(desiredWidth, widthMeasureSpec);
			int measureHeight = measureDimension(desiredHeight, heightMeasureSpec);
			setMeasuredDimension(measureWidth, measureHeight);

			for(int i = 0; i < getChildCount(); i++) {
				getChildAt(i).setLayoutParams(params);
				Log.d(TAG, "child at " + i + ": " + getChildAt(i).getWidth() + ", " + getChildAt(i).getHeight());
			}

		}

		private int measureDimension(int desiredSize, int measureSpec) {
			int result;
			int specMode = MeasureSpec.getMode(measureSpec);
			int specSize = MeasureSpec.getSize(measureSpec);

			if (specMode == MeasureSpec.EXACTLY) {
				result = specSize;
			} else {
				result = desiredSize;
				if (specMode == MeasureSpec.AT_MOST) {
					result = Math.min(result, specSize);
				}
			}

			if (result < desiredSize){
				Log.e(TAG, "The view is too small, the content might get cut");
			}
			return result;
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			performClick();
			this.getParent().requestDisallowInterceptTouchEvent(true);

			int tempTouchX = (int) event.getX();
			int tempTouchY = (int) event.getY();

//			Log.d(TAG, "touch position: " + tempTouchX + ", " + tempTouchY);

//			if (mArea.contains(tempTouchX, tempTouchY)) {
//				touchY = tempTouchY;
//			}

			invalidate();
			return true;
		}

		@Override
		public boolean performClick() {
			super.performClick();
			return false;
		}
	}
}
