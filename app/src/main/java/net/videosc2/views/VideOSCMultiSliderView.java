package net.videosc2.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class VideOSCMultiSliderView extends ViewGroup {
	final static private String TAG = "MultiSliderView";

	public VideOSCMultiSliderView(Context context) {
		super(context);
	}

	public VideOSCMultiSliderView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public VideOSCMultiSliderView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public VideOSCMultiSliderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
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

