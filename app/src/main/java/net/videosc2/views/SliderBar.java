package net.videosc2.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.videosc2.VideOSCApplication;

import java.lang.reflect.Type;

/**
 * Created by stefan on 17.05.18, package net.videosc2.views, project VideOSC22.
 */
public class SliderBar extends View {

	final static String TAG = "SliderBar";
	Paint mPaint;
	Canvas mCanvas;
	String pixelNum;
	Typeface typeFace = Typeface.create("sans-serif-light", Typeface.NORMAL);
	int left, top, right, bottom;
	Rect mArea = new Rect(left, top, right, bottom);
	int touchY;

	public SliderBar(Context context) {
		super(context);
//		Log.d(TAG, "new SliderBar instance");
		setFocusable(true);

		mPaint = new Paint();
		mCanvas = new Canvas();

//		this.setMinimumWidth(right);
//		this.setMinimumHeight(bottom);
	}

	public SliderBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SliderBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/*@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		measure(MeasureSpec.AT_MOST, MeasureSpec.AT_MOST);
	}*/

	@Override
	protected void onDraw(Canvas canvas) {
		Log.d(TAG, "slider bar on draw");
		mPaint.setAntiAlias(true);
//		mPaint.setDither(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeWidth(6);
		mPaint.setColor(0x66ffffff);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			canvas.drawRoundRect((float) left, (float) top, (float) right, (float) bottom, (float) 10.0, (float) 10.0, mPaint);
		else
			canvas.drawRect(left, top, right, bottom, mPaint);
		mArea.set(left, top, right, bottom);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setColor(0x66000000);
		if (touchY <= top) touchY = top;
		if (touchY > bottom) touchY = bottom;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			canvas.drawRoundRect((float) left, (float) touchY, (float) right, (float) bottom, (float) 10.0, (float) 10.0, mPaint);
		else
			canvas.drawRect(left, touchY, right, bottom, mPaint);
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setTypeface(typeFace);
		mPaint.setTextSize((float) 30);
		mPaint.setColor(0xffffffff);
		canvas.drawText(pixelNum, right/2, bottom - 20, mPaint);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//		Log.d(TAG, "slider bar on layout: " + left + ", " + top + ", " + right + ", " + bottom);
//		this.setLeft(left);
//		this.setTop(top);
//		this.setRight(right);
//		this.setBottom(bottom);
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	/* @Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		Log.d(TAG, "window focus changed");
		mArea = new Rect(this.left, this.top, this.right, this.bottom);
	} */

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//		Log.v(TAG, "width measure spec: " + MeasureSpec.toString(widthMeasureSpec));
//		Log.v(TAG, "height measure spec: " + MeasureSpec.toString(heightMeasureSpec));

		int desiredWidth = getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight();
		int desiredHeight = getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom();

		Log.d(TAG, "desired width: " + desiredWidth + ", desired height: " + desiredHeight);

		setMeasuredDimension(measureDimension(desiredWidth, widthMeasureSpec),
				measureDimension(desiredHeight, heightMeasureSpec));
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
		Log.d(TAG, "touched!");
		performClick();
//		this.getParent().requestDisallowInterceptTouchEvent(true);

		int tempTouchX = (int) event.getX();
		int tempTouchY = (int) event.getY();

		Log.d(TAG, "touch position: " + tempTouchX + ", " + tempTouchY);

		if (mArea.contains(tempTouchX, tempTouchY)) {
			touchY = tempTouchY;
		}

		invalidate();
		return true;
	}

	@Override
	public boolean performClick() {
		super.performClick();
		return false;
	}

	public void setTouchY(int touchY) {
		this.touchY = touchY;
	}

	public int getTouchY() {
		return this.touchY;
	}

	public void setNum(String num) {
		this.pixelNum = num;
	}

	public String getNum() {
		return this.pixelNum;
	}
}
