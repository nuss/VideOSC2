package net.videosc2.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import java.util.ArrayList;

public class VideOSCMultiSliderView extends LinearLayout {
	final static private String TAG = "MultiSliderView";
	public ArrayList<SliderBar> mBars = new ArrayList<>();
	private ArrayList<Integer> mSliderNums;
	private int mDisplayHeight;
	private int mParentTopMargin;

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
		int barWidth = (getMeasuredWidth() - marginsTotal)/numSliders;
		int barHeight = getMeasuredHeight();
		int x = 0;
		for (int i = 0; i < getChildCount(); i++) {
			SliderBar child = (SliderBar) getChildAt(i);
			child.areaTop = 0 - getTop() - mParentTopMargin - 1;
			child.areaBottom = mDisplayHeight;
			child.layout(x, 0, x + barWidth, barHeight);
			x += (barWidth + 1);
		}
	}

	// TODO: get values of sliders (0.0-1.0) and make them available in CameraView
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		performClick();
		this.getParent().requestDisallowInterceptTouchEvent(true);

		int tempTouchX = (int) event.getX();
		int tempTouchY = (int) event.getY();

		for (int i = 0; i < mBars.size(); i++) {
			SliderBar bar = mBars.get(i);
			if (bar.mArea.contains(tempTouchX, tempTouchY)) {
				bar.setTouchY(tempTouchY);
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

	public double getSliderValueAt(int index) {
		// TODO

		return index;
	}
}

