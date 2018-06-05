package net.videosc2.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by stefan on 17.05.18, package net.videosc2.views, project VideOSC2.
 */

// TODO: define a way to colorize sliderbars

public class SliderBar extends View {

	final static String TAG = "SliderBar";
	private Paint mPaint;
	private String pixelNum;
	private Typeface typeFace = Typeface.create("sans-serif-light", Typeface.NORMAL);
	private int left = 0, right, bottom;
	public int areaTop, areaBottom;
	public Rect mArea = new Rect(left, areaTop, right, areaBottom);
	private int touchY;
	public float mScreenDensity;
	private int mColor = 0x66ffffff;

	public SliderBar(Context context) {
		super(context);
		init();
	}

	public SliderBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SliderBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int top = 0;
		touchY = touchY == 0 ? touchY = 2 : getTouchY();
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeWidth(0);
		mPaint.setColor(0x99000000);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			canvas.drawRoundRect((float) left, (float) top, (float) right, (float) bottom, 5.0f * mScreenDensity, 5.0f * mScreenDensity, mPaint);
		else
			canvas.drawRect(left, top, right, bottom, mPaint);
		mArea.set(getLeft(), areaTop, getRight(), areaBottom);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setColor(mColor);
		if (touchY + 2 <= top) touchY = top + 2;
		if (touchY - 2 > bottom) touchY = bottom - 2;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			canvas.drawRoundRect((float) left + 2, (float) touchY, (float) right - 4, (float) bottom - 2, 5.0f * mScreenDensity, 5.0f * mScreenDensity, mPaint);
		else
			canvas.drawRect(left + 2, touchY, right - 4, bottom - 2, mPaint);
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setTypeface(typeFace);
		mPaint.setTextSize((float) 30);
		mPaint.setColor(0xffffffff);
		canvas.drawText(pixelNum, (right - left)/2, bottom - 20, mPaint);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		this.right = right - left;
		this.bottom = bottom;
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

	public void setColor(int color) {
		this.mColor = color;
	}
}
