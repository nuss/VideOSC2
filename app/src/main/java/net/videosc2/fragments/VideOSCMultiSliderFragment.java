package net.videosc2.fragments;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.videosc2.R;
import net.videosc2.VideOSCApplication;
import net.videosc2.views.SliderBar;

import java.util.ArrayList;

/**
 * Created by stefan on 19.05.18, package net.videosc2.views, project VideOSC22.
 */
public class VideOSCMultiSliderFragment extends VideOSCBaseFragment {
	private final static String TAG = "MultiSliderFragment";
	private VideOSCApplication mApp;
	private View mMSContainer;
	private MultiSliderView mMSViewRight;
	private MultiSliderView mMSViewLeft;
//	private int barHeight, barWidth;

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

	public class MultiSliderView extends ViewGroup {
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

			/*for(int i = 0; i < getChildCount(); i++) {
				Log.d(TAG, "child at " + i + ": " + getChildAt(i).getLeft() + ", " + getChildAt(i).getRight());
			}*/

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

			Log.d(TAG, "touch position: " + tempTouchX + ", " + tempTouchY);

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
