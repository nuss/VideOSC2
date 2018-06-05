package net.videosc2.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import net.videosc2.R;

import java.util.ArrayList;

public class TileOverlayView extends View {
	final private static String TAG = "TileOverlayView";

	final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
	private BitmapShader mShaderSelected;
	private ArrayList<Rect> mSelectPixels = new ArrayList<>();
	/**
	 * Simple constructor to use when creating a view from code.
	 *
	 * @param context The Context the view is running in, through which it can
	 *                access the current theme, resources, etc.
	 */
	public TileOverlayView(Context context) {
		super(context);
		init();
	}

	/**
	 * @param context The Context the view is running in, through which it can
	 *                access the current theme, resources, etc.
	 * @param attrs   The attributes of the XML tag that is inflating the view.
	 */
	public TileOverlayView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 *
	 * @param context      The Context the view is running in, through which it can
	 *                     access the current theme, resources, etc.
	 * @param attrs        The attributes of the XML tag that is inflating the view.
	 * @param defStyleAttr An attribute in the current theme that contains a
	 *                     reference to a style resource that supplies default values for
	 *                     the view. Can be 0 to not look for defaults.
	 */
	public TileOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inMutable = true;
		Bitmap patSrc = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.hover_rect_tile, options);
		mShaderSelected = new BitmapShader(patSrc, Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);
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
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {}

	@Override
	protected void onDraw(Canvas canvas) {
		mPaint.setColor(0xff000000);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setShader(mShaderSelected);
		for (Rect rect : mSelectPixels) {
			canvas.drawRect(rect, mPaint);
		}
	}

	public void setSelectedRects(ArrayList<Rect> rects) {
		this.mSelectPixels = rects;
	}
}
