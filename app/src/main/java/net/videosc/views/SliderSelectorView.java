package net.videosc.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import net.videosc.activities.VideOSCMainActivity;

public class SliderSelectorView extends View {
    private static final String TAG = SliderSelectorView.class.getSimpleName();

    final private VideOSCMainActivity mActivity;
    private int mPixelNum;
    private SparseArray<String> mStrings;
    private Paint mPaint;
    private int mColor = 0xff000000;
    private float mLeft, mTop, mRight, mBottom;
    private float mScreenDensity;

    public SliderSelectorView(Context context) {
        super(context);
        this.mActivity = (VideOSCMainActivity) context;
        init();
    }

    public SliderSelectorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mActivity = (VideOSCMainActivity) context;
        init();
    }

    public SliderSelectorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mActivity = (VideOSCMainActivity) context;
        init();
    }

    private void init() {
        this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int top = 0;
        int left = 0;
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setColor(mColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            canvas.drawRoundRect((float) left, (float) top, mRight, (float) mBottom, 5.0f * mScreenDensity, 5.0f * mScreenDensity, mPaint);
        else
            canvas.drawRect(mLeft, top, mRight, mBottom, mPaint);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d(TAG, "dimensions: " + left + ", top: " + top + ", right: " + right + ", bottom: " + bottom);
        this.mLeft = left;
        this.mTop = top;
        this.mRight = right;
        this.mBottom = bottom;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setNum(int id) {
        this.mPixelNum = id;
    }

    public void setStrings(SparseArray<String> stringSparseArray) {
        this.mStrings = stringSparseArray;
    }

    public void setColor(int color) {
        this.mColor = color;
    }
}
