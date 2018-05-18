package net.videosc2.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import net.videosc2.R;

import java.util.ArrayList;

/**
 * Created by stefan on 17.05.18, package net.videosc2.views, project VideOSC22.
 */
public class SliderBar extends View {

	final static String TAG = "SliderBar";
	Paint mPaint;
	Canvas mCanvas;
	int left, top, width, height;
	Rect mArea = new Rect(left, top, width, height);
	int touchY;

	public SliderBar(Context context, int left, int top, int width, int height, int touchY) {
		super(context);
		Log.d(TAG, "constructor SliderBar");
		setFocusable(true);
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
		this.touchY = touchY;

		mPaint = new Paint();
		mCanvas = new Canvas();

		/* this.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				performClick();
				v.getParent().requestDisallowInterceptTouchEvent(true);
				Log.d(TAG, "me: " + this + ", action: " + event.getActionMasked());
				if (event.getActionMasked() == (MotionEvent.ACTION_HOVER_ENTER|MotionEvent.ACTION_HOVER_MOVE|MotionEvent.ACTION_HOVER_EXIT)) {
					Log.d(TAG, "touch position: " + event.getX() + ", " + event.getY());
				}
//				return SliderBar.super.onTouchEvent(event);
				return true;
			}
		}); */
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
		mPaint.setColor(Color.parseColor("#66000000"));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			canvas.drawRoundRect((float) left, (float) top, (float) width, (float) height, (float) 10.0, (float) 10.0, mPaint);
		else
			canvas.drawRect(left, top, width, height, mPaint);
		mArea.set(left, top, width, height);
		Log.d(TAG, "onDraw");
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(Color.parseColor("#66000000"));
		int currHeight = height + 20 - touchY;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			canvas.drawRoundRect((float) left, (float) touchY, (float) width, (float) currHeight, (float) 10.0, (float) 10.0, mPaint);
		else
			canvas.drawRect(left, top, width, currHeight, mPaint);
	}

	/* @Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		Log.d(TAG, "window focus changed");
		mArea = new Rect(this.left, this.top, this.width, this.height);
	} */

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		performClick();
		this.getParent().requestDisallowInterceptTouchEvent(true);
		int eventAction = event.getActionMasked();

		int touchX = (int) event.getX();
		touchY = (int) event.getY();
		int pointerCount = event.getPointerCount();

		if (mArea.contains(touchX, touchY))
			Log.d(TAG, "me: " + this + ", eventAction: " + eventAction + ", x: " + touchX + ", y: " + touchY + ", pointerCount: " + pointerCount);

		invalidate();
		return true;
	}

	@Override
	public boolean performClick() {
		super.performClick();
		return false;
	}

}
