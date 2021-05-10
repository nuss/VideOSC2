package net.videosc.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class VideOSCSliderGroupView extends LinearLayout {
    private final static String TAG = VideOSCSliderGroupView.class.getSimpleName();
    private ArrayList<Integer> mSliderNums;

    public VideOSCSliderGroupView(Context context) {
        super(context);
    }

    public VideOSCSliderGroupView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VideOSCSliderGroupView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int numSliders = mSliderNums.size();
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

    public void setSliderNums(ArrayList<Integer> sliderNums) {
        this.mSliderNums = sliderNums;
    }
}
