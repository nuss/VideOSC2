package net.videosc2.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import java.util.ArrayList;

public class VideOSCMultiSliderView extends LinearLayout {
	final static private String TAG = "MultiSliderView";
	public ArrayList<SliderBar> bars = new ArrayList<>();

	private ArrayList<Integer> sliderNums;

	public VideOSCMultiSliderView(Context context) {
		super(context);
		init(null, 0);
	}

	public VideOSCMultiSliderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public VideOSCMultiSliderView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(attrs, defStyleAttr);
	}

	private void init(AttributeSet attrs, int defStyleAttr) {
		Log.d(TAG, "MultiSliderView init - attrs: " + attrs + ", orientation: " + this.getOrientation());
		this.setOrientation(LinearLayout.HORIZONTAL);
	}

	public void setSliderNums(ArrayList<Integer> sliderNums) {
		this.sliderNums = sliderNums;
	}

	/*public void setScreenDimensions(Point dimensions) {
		this.screenDimensions = dimensions;
	}*/

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
		int numSliders = sliderNums.size();
		int marginsTotal = numSliders + 1; // 1 pixel spacing between each slider
		int barWidth = (getMeasuredWidth() - marginsTotal)/numSliders;
		Log.d(TAG, "numSliders: " + numSliders + ", marginsTotal: " + marginsTotal + ", barWidth: " + barWidth);
		int barHeight = getMeasuredHeight();
		int x = 0;
		for (int i = 0; i < getChildCount(); i++) {
			SliderBar child = (SliderBar) getChildAt(i);
			child.layout(x, 0, x + barWidth, barHeight);
			x += (barWidth + 1);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		performClick();
		this.getParent().requestDisallowInterceptTouchEvent(true);

		int tempTouchX = (int) event.getX();
		int tempTouchY = (int) event.getY();

		Log.d(TAG, "touch position: " + tempTouchX + ", " + tempTouchY);

		for (int i = 0; i < bars.size(); i++) {
			SliderBar bar = bars.get(i);
//			Log.d(TAG, "mArea: " + bar.mArea.left + ", " + bar.mArea.top + ", " + bar.mArea.right + ", " + bar.mArea.bottom);
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
}

