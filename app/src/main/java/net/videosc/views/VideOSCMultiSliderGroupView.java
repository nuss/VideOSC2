package net.videosc.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.videosc.R;

import java.util.ArrayList;

public class VideOSCMultiSliderGroupView extends LinearLayout {
	final static private String TAG = VideOSCMultiSliderGroupView.class.getSimpleName();
	public ArrayList<SliderBar> mBars = new ArrayList<>();
	private ArrayList<Integer> mSliderNums;
	private Double[] mValuesArray;
	private Double[] mRedValuesArray, mGreenValuesArray, mBlueValuesArray;
	private int mDisplayHeight;
	private int mParentTopMargin;
	private double[] mValues;
	private ViewGroup mContainer;

	public VideOSCMultiSliderGroupView(Context context) {
		super(context);
		init();
	}

	public VideOSCMultiSliderGroupView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public VideOSCMultiSliderGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
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
		final int numSliders = mSliderNums.size();
		final int marginsTotal = numSliders + 1; // 1 pixel spacing between each slider
		// FIXME - after switching camera numSliders may be 0 because mNumSliders is empty
		final int barWidth = (getMeasuredWidth() - marginsTotal) / numSliders;
		final int barHeight = getMeasuredHeight();
		int x = 0;
		for (int i = 0; i < getChildCount(); i++) {
			SliderBar child = (SliderBar) getChildAt(i);
			int index = Integer.parseInt(child.getNum(), 10) - 1;
			child.mAreaTop = -getTop() - mParentTopMargin - 1;
			child.mAreaBottom = mDisplayHeight;
			if (mValues != null) {
				child.setPixelValue(mValues[i]);
				// get value immediately on create
				switch (child.getColor()) {
					case 0x99ff0000:
						this.mRedValuesArray[index] = mValues[i];
						break;
					case 0x9900ff00:
						this.mGreenValuesArray[index] = mValues[i];
						break;
					case 0x990000ff:
						this.mBlueValuesArray[index] = mValues[i];
				}
//				this.mValuesArray[index] = mValues[i];
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

		for (int i = 0; i < mBars.size(); i++) {
			SliderBar bar = mBars.get(i);
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
//				mValuesArray[index] = sliderValue;
				switch (bar.getColor()) {
					case 0x99ff0000:
						this.mRedValuesArray[index] = sliderValue;
						break;
					case 0x9900ff00:
						this.mGreenValuesArray[index] = sliderValue;
						break;
					case 0x990000ff:
						this.mBlueValuesArray[index] = sliderValue;
				}

//				Log.d(TAG, " \nred: " + Arrays.toString(mRedValuesArray) + "\ngreen: " + Arrays.toString(mGreenValuesArray) + "\nblue: " + Arrays.toString(mBlueValuesArray));
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

	public void setValuesArrays(int numPixels) {
		this.mRedValuesArray = new Double[numPixels];
		this.mGreenValuesArray = new Double[numPixels];
		this.mBlueValuesArray = new Double[numPixels];
	}

	// we need a different logic for slider groups as there can be more than one slider bar at one index
	public ArrayList<Double> getGroupSliderValuesAt(int index) {
		final SparseArray<ArrayList<SliderBar>> bars = getSliderGroupBars();
		final ArrayList<Double> res = new ArrayList<>();
		if (bars.get(index) != null) {
			res.add(mRedValuesArray[index]);
			res.add(mGreenValuesArray[index]);
			res.add(mBlueValuesArray[index]);
		}
		return res;
	}

	// slider group: return a SparseArray with pixel indices as keys and an ArrayList of bars as values
	private SparseArray<ArrayList<SliderBar>> getSliderGroupBars() {
		final SparseArray<ArrayList<SliderBar>> bars = new SparseArray<>();
		for (SliderBar bar : mBars) {
			// pixel numbering starts at 1, we want the true index
			int barIndex = Integer.parseInt(bar.getNum(), 10) - 1;
			if (bars.get(barIndex) == null) {
				bars.put(barIndex, new ArrayList<>());
			}
			bars.get(barIndex).add(bar);
		}

		return bars;
	}

	public void setValues(double[] values) {
		this.mValues = values;
	}

	public void setContainerView(ViewGroup container) {
		this.mContainer = container;
	}
}

