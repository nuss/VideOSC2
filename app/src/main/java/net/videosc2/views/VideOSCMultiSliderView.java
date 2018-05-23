package net.videosc2.views;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class VideOSCMultiSliderView extends ViewGroup {
	final static private String TAG = "MultiSliderView";
	private ArrayList<Integer> sliderNums;
	private Point screenDimensions;

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

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public VideOSCMultiSliderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(attrs, defStyleAttr);
	}

	private void init(AttributeSet attrs, int defStyleAttr) {

	}

	public void setSliderNums(ArrayList<Integer> sliderNums) {
		this.sliderNums = sliderNums;
	}

	public void setScreenDimensions(Point dimensions) {
		this.screenDimensions = dimensions;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		Log.d(TAG, "MultiSliderView on layout: " + left + ", " + top + ", " + right + ", " + bottom);
		addSliders();
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

		for (int i = 0; i < getChildCount(); i++) {
			Log.d(TAG, "child at " + i + ": " + getChildAt(i).getLeft() + ", " + getChildAt(i).getRight());
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

		if (result < desiredSize) {
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

	private void addSliders() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT
		);

		ArrayList<SliderBar> sliders = new ArrayList<>();

		ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) this.getLayoutParams();
		int barHeight = screenDimensions.y - lp.bottomMargin - lp.topMargin;
		int barWidth = (screenDimensions.x / 2 - lp.leftMargin - lp.rightMargin) / sliderNums.size();

		Log.d(TAG, "bar height: " + barHeight + ", bar width: " + barWidth);

		for (int num : sliderNums) {
			SliderBar bar = new SliderBar(getContext());
			bar.setNum(String.valueOf(num));
			sliders.add(bar);
//			x = x + barWidth;
//				Button testButton = new Button(getActivity());
//				testButton.setWidth(dimensions.x/2/sliderNums.size());
//				mMSViewLeft.addView(testButton);
		}

		int x = 0;
		for (SliderBar slider : sliders) {
			// TODO slider needs to know its touchY within the instance to draw its bar properly
			// maybe keep Ys in an array symmetrically to slidersLeft
			// slider.setTouchY();
//				Log.d(TAG, "mMSViewLeft: " + mMSViewLeft.getChildCount());
			this.addView(slider);
			slider.layout(x, 0, x + barWidth, barHeight);
			x += barWidth;
//				Log.d(TAG, "slider props: " + slider.getX() + ", " + slider.getY() + ", " + slider.getWidth() + ", " + slider.getHeight());
		}
	}

}

