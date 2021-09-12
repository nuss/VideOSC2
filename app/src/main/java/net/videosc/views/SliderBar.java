package net.videosc.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import net.videosc.VideOSCApplication;

/**
 * Created by stefan on 17.05.18, package net.videosc.views, project VideOSC2.
 */
public class SliderBar extends View {

	final static String TAG = "SliderBar";
	private Paint mPaint;
	private String mPixelNum;
	private final Typeface mTypeFace = Typeface.create("sans-serif-light", Typeface.NORMAL);
	private final int mLeft = 0;
	private int mRight;
	private int mBottom;
	public int mAreaTop, mAreaBottom;
	public Rect mArea = new Rect(mLeft, mAreaTop, mRight, mAreaBottom);
	private int mTouchY;
	public float mScreenDensity;
	private int mColor = 0x66ffffff;
	private double mPixelVal;
	private VideOSCApplication mApp;
	private String mLabelText;

	public SliderBar(Context context) {
		super(context);
		init(context);
	}

	public SliderBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SliderBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mApp = (VideOSCApplication) ((Activity) context).getApplication();
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int top = 0;
		mTouchY = mTouchY == 0 ? mTouchY = 2 : getTouchY();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setColor(0x99000000);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			canvas.drawRoundRect((float) mLeft, (float) top, (float) mRight, (float) mBottom, 5.0f * mScreenDensity, 5.0f * mScreenDensity, mPaint);
		else
			canvas.drawRect(mLeft, top, mRight, mBottom, mPaint);
		mArea.set(getLeft(), mAreaTop, getRight(), mAreaBottom);
		mPaint.setColor(mColor);
		if (mTouchY + 2 <= top) mTouchY = top + 2;
		if (mTouchY - 2 > mBottom) mTouchY = mBottom - 2;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			canvas.drawRoundRect(
					(float) mLeft + 2,
					(float) mTouchY,
					(float) mRight - 4,
					(float) mBottom - 2,
					5.0f * mScreenDensity,
					5.0f * mScreenDensity, mPaint
			);
		else
			canvas.drawRect(mLeft + 2, mTouchY, mRight - 4, mBottom - 2, mPaint);
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setTypeface(mTypeFace);
		mPaint.setTextSize((float) 12 * mApp.getScreenDensity());
		mPaint.setColor(0xffffffff);
		canvas.drawText(mPixelNum, mRight / 2.0f, mBottom - 7 * mApp.getScreenDensity(), mPaint);
		canvas.rotate(-90, mRight / 2f , (mAreaTop + mBottom) / 2f);
//		Log.d(TAG, "mArea: " + mArea.left + ", " + mArea.top + ", " + mArea.right + ", " + mArea.bottom + "\nmArea width: " + mArea.width() + ", height: " + mArea.height());
//		Log.d(TAG, "rotation pivot point, x: " + (mRight - mLeft) / 2f + ", y: " + (mBottom - mAreaTop) / 2f);
//		Log.d(TAG, "canvas, left: " + canvas.getClipBounds());
		mPaint.setTextAlign(Paint.Align.RIGHT);
//		Log.d(TAG, "mArea: " + mArea + "\nmArea right: " + mArea.right + ", mArea exact center y: " + mArea.exactCenterY());
//		canvas.clipRect(mArea);
		Log.d(TAG, "mBottom: " + mBottom + ", mAreaTop: " + mAreaTop);
		canvas.drawText("abcdefghijklmno", (mAreaTop + mBottom) / 2f, (mAreaTop + mBottom) / 2f + 3 * mApp.getScreenDensity(), mPaint);
//		canvas.drawText(mLabelText, (mAreaTop + mBottom) / 2f, (mAreaTop + mBottom) / 2f + 3 * mApp.getScreenDensity(), mPaint);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		this.mRight = right - left;
		this.mBottom = bottom;
		if (this.mPixelVal > 0) {
			this.mTouchY = (int) (bottom * this.mPixelVal);
		}
	}

	public void setTouchY(int touchY) {
		this.mTouchY = touchY;
	}

	public int getTouchY() {
		return this.mTouchY;
	}

	public void setNum(String num) {
		this.mPixelNum = num;
	}

	public void setLabelText(String labelText) {
		this.mLabelText = labelText;
	}

	public String getNum() {
		return this.mPixelNum;
	}

	public void setColor(int color) {
		this.mColor = color;
	}

	public int getColor() {
		return this.mColor;
	}

	public int getBarHeight() {
		return this.mBottom;
	}

	public void setPixelValue(double value) {
		this.mPixelVal = 1.0 - value;
	}
}
