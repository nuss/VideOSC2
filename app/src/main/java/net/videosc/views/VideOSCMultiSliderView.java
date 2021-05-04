package net.videosc.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.videosc.R;

import java.util.ArrayList;

public class VideOSCMultiSliderView extends LinearLayout {
	final static private String TAG = "MultiSliderView";
	public ArrayList<SliderBar> mBars = new ArrayList<>();
	private ArrayList<Integer> mSliderNums;
	private Double[] mValuesArray;
	private int mDisplayHeight;
	private int mParentTopMargin;
	private double[] mValues;
	private ViewGroup mContainer;

	public VideOSCMultiSliderView(Context context) {
		super(context);
		init();
	}

	public VideOSCMultiSliderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public VideOSCMultiSliderView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		this.setOrientation(LinearLayout.HORIZONTAL);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int desiredWidth = getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight();
		int desiredHeight = getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom();

		int measureWidth = measureDimension(desiredWidth, widthMeasureSpec);
		int measureHeight = measureDimension(desiredHeight, heightMeasureSpec);
		setMeasuredDimension(measureWidth, measureHeight);
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

		if (result < desiredSize) {
			Log.e(TAG, "The view is too small, the content might get cut");
		}
		return result;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int numSliders = mSliderNums.size();
		int marginsTotal = numSliders + 1; // 1 pixel spacing between each slider
		// FIXME - after switching camera numSliders may be 0 because mNumSliders is empty
		int barWidth = (getMeasuredWidth() - marginsTotal) / numSliders;
		int barHeight = getMeasuredHeight();
		int x = 0;
		for (int i = 0; i < getChildCount(); i++) {
			SliderBar child = (SliderBar) getChildAt(i);
			int index = Integer.parseInt(child.getNum(), 10) - 1;
			child.mAreaTop = -getTop() - mParentTopMargin - 1;
			child.mAreaBottom = mDisplayHeight;
			if (mValues != null) {
				child.setPixelValue(mValues[i]);
				// get value immediately on create
				this.mValuesArray[index] = mValues[i];
			}
			child.layout(x, 0, x + barWidth, barHeight);
			x += (barWidth + 1);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		ViewGroup tools = mContainer.findViewById(R.id.multislider_buttons);
		ViewGroup labels = mContainer.findViewById(R.id.multislider_labels);

		if (event.getAction() == MotionEvent.ACTION_UP) {
			tools.setVisibility(View.VISIBLE);
			labels.setVisibility(View.VISIBLE);
		}

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			tools.setVisibility(View.INVISIBLE);
			labels.setVisibility(View.INVISIBLE);
		}

		double sliderValue;
		performClick();
		this.getParent().requestDisallowInterceptTouchEvent(true);

		int tempTouchX = (int) event.getX();
		int tempTouchY = (int) event.getY();

		for (SliderBar bar : mBars) {
			if (bar.mArea.contains(tempTouchX, tempTouchY)) {
				int barHeight = bar.getBarHeight();
				bar.setTouchY(tempTouchY);

				// pixel index
				int index = Integer.parseInt(bar.getNum(), 10) - 1;
				// the value of the slider
				if (tempTouchY <= 0)
					sliderValue = 1.0;
				else if (tempTouchY >= barHeight)
					sliderValue = 0.0;
				else
					sliderValue = ((double) barHeight - (double) tempTouchY) / (double) barHeight;
				mValuesArray[index] = sliderValue;
				bar.invalidate();
			}
		}

		invalidate();
		return true;
	}

	@Override
	public boolean performClick() {
		super.performClick();
		return false;
	}

	public void setDisplayHeight(int height) {
		this.mDisplayHeight = height;
	}

	public void setParentTopMargin(int topMargin) {
		this.mParentTopMargin = topMargin;
	}

	public void setSliderNums(ArrayList<Integer> sliderNums) {
		this.mSliderNums = sliderNums;
	}

	public void setValuesArray(int numPixels) {
		this.mValuesArray = new Double[numPixels];
	}

	public Double getSliderValueAt(int index) {
		return this.mValuesArray[index];
	}

	public void setValues(double[] values) {
		this.mValues = values;
	}

	public void setContainerView(ViewGroup container) {
		this.mContainer = container;
	}
}

