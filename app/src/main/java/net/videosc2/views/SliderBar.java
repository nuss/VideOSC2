package net.videosc2.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Created by stefan on 17.05.18, package net.videosc2.views, project VideOSC22.
 */
public class SliderBar extends View {

	final static String TAG = "SliderBar";
	Paint mPaint;
	Canvas mCanvas;
	TextView mPixelNum;
	int left, top, right, bottom;
	Rect mArea = new Rect(left, top, right, bottom);
	int touchY;

	public SliderBar(Context context, int left, int top, int right, int bottom, int touchY) {
		super(context);
		setFocusable(true);
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		this.touchY = touchY;

		mPaint = new Paint();
		mCanvas = new Canvas();
		mPixelNum = new TextView(context);
	}

	public SliderBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SliderBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		mPaint.setAntiAlias(true);
//		mPaint.setDither(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeWidth(6);
		mPaint.setColor(Color.parseColor("#66ffffff"));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			canvas.drawRoundRect((float) left, (float) top, (float) right, (float) bottom, (float) 10.0, (float) 10.0, mPaint);
		else
			canvas.drawRect(left, top, right, bottom, mPaint);
		mArea.set(left, top, right, bottom);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setColor(Color.parseColor("#66000000"));
		if (touchY <= top) touchY = top;
		if (touchY > bottom) touchY = bottom;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			canvas.drawRoundRect((float) left, (float) touchY, (float) right, (float) bottom, (float) 10.0, (float) 10.0, mPaint);
		else
			canvas.drawRect(left, touchY, right, bottom, mPaint);
	}

	/* @Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		Log.d(TAG, "window focus changed");
		mArea = new Rect(this.left, this.top, this.right, this.bottom);
	} */

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		performClick();
		this.getParent().requestDisallowInterceptTouchEvent(true);

		int tempTouchX = (int) event.getX();
		int tempTouchY = (int) event.getY();

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

}
