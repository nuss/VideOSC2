package net.videosc2.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import net.videosc2.R;
import net.videosc2.utilities.enums.RGBModes;

import java.util.ArrayList;

public class TileOverlayView extends View {
	final private static String TAG = "TileOverlayView";

	final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
	private BitmapShader mShaderSelected;
	private ArrayList<Rect> mSelectPixels = new ArrayList<>();
	private Point mParentResolution;
	private Double[] mRedMixValues, mGreenMixValues, mBlueMixValues;
	private RGBModes mColorMode;
	private Bitmap mRCorner, mGCorner, mBCorner, mRGCorner, mGBCorner, mRBCorner, mRGBCorner;
	private Point mPixelSize;
	private Point mCornerDimensions;

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
		Resources res = getContext().getResources();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inMutable = true;
		Bitmap patSrc = BitmapFactory.decodeResource(res, R.drawable.hover_rect_tile, options);
		mShaderSelected = new BitmapShader(patSrc, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		mRCorner = BitmapFactory.decodeResource(res, R.drawable.r_corner);
		mBCorner = BitmapFactory.decodeResource(res, R.drawable.b_corner);
		mGCorner = BitmapFactory.decodeResource(res, R.drawable.g_corner)
;		mRGCorner = BitmapFactory.decodeResource(res, R.drawable.rg_corner);
		mRBCorner = BitmapFactory.decodeResource(res, R.drawable.rb_corner);
		mGBCorner = BitmapFactory.decodeResource(res, R.drawable.gb_corner);
		mRGBCorner = BitmapFactory.decodeResource(res, R.drawable.rgb_corner);
		mCornerDimensions = new Point(mRGBCorner.getWidth(), mRGBCorner.getHeight());
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
		Log.d(TAG, "overlay onDraw");
		Bitmap bitmap;
		mPaint.setColor(0xff000000);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setShader(mShaderSelected);
		for (Rect rect : mSelectPixels) {
			canvas.drawRect(rect, mPaint);
		}
		if (mParentResolution != null
				&& mRedMixValues != null
				&& mGreenMixValues != null
				&& mBlueMixValues != null
				&& mColorMode != null) {
			int numPixels = mParentResolution.x * mParentResolution.y;
			for (int i = 0; i < numPixels; i++) {
				switch (mColorMode) {
					case R:
						if (mRedMixValues[i] != null && mRedMixValues[i] > 0.0) {
							// draw bitmap - RGB corner (white)
							bitmap = mRGBCorner;
							drawCornerBitmap(canvas, i, bitmap);
						}
						break;
					case G:
						if (mGreenMixValues[i] != null && mGreenMixValues[i] > 0.0) {
							// draw bitmap - RGB corner (white)
							bitmap = mRGBCorner;
							drawCornerBitmap(canvas, i, bitmap);
						}
						break;
					case B:
						if (mBlueMixValues[i] != null && mBlueMixValues[i] > 0.0) {
							// draw bitmap - RGB corner (white)
							bitmap = mRGBCorner;
							drawCornerBitmap(canvas, i, bitmap);
						}
						break;
					default: // RGB
						if (mRedMixValues[i] != null && mGreenMixValues[i] == null && mBlueMixValues[i] == null && mRedMixValues[i] > 0.0) {
							// draw red corner bitmap
							bitmap = mRCorner;
							drawCornerBitmap(canvas, i, bitmap);
						} else if (mRedMixValues[i] == null && mGreenMixValues[i] != null && mBlueMixValues[i] == null && mGreenMixValues[i] > 0.0) {
							// draw green corner bitmap
							bitmap = mGBCorner;
							drawCornerBitmap(canvas, i, bitmap);
						} else if (mRedMixValues[i] == null && mGreenMixValues[i] == null && mBlueMixValues[i] != null && mBlueMixValues[i] > 0.0) {
							// draw blue corner bitmap
							bitmap = mBCorner;
							drawCornerBitmap(canvas, i, bitmap);
						} else if (mRedMixValues[i] != null && mGreenMixValues[i] != null && mBlueMixValues[i] == null) {
							if (mRedMixValues[i] > 0.0 && mGreenMixValues[i] > 0.0) {
								// draw yellow corner bitmap (rg)
								bitmap = mRGCorner;
								drawCornerBitmap(canvas, i, bitmap);
							} else if (mRedMixValues[i] == 0.0 && mGreenMixValues[i] > 0.0) {
								// draw green corner bitmap
								bitmap = mGCorner;
								drawCornerBitmap(canvas, i, bitmap);
							} else if (mRedMixValues[i] > 0.0 && mGreenMixValues[i] > 0.0) {
								// draw red corner bitmap
								bitmap = mRCorner;
								drawCornerBitmap(canvas, i, bitmap);
							} // else both values are 0.0 - do nothing
						} else if (mRedMixValues[i] == null && mGreenMixValues[i] != null && mBlueMixValues[i] != null) {
							if (mGreenMixValues[i] > 0.0 && mBlueMixValues[i] > 0.0) {
								// draw light blue corner bitmap
								bitmap = mGBCorner;
								drawCornerBitmap(canvas, i, bitmap);
							} else if (mGreenMixValues[i] == 0.0 && mBlueMixValues[i] > 0.0) {
								// draw blue corner bitmap
								bitmap = mBCorner;
								drawCornerBitmap(canvas, i, bitmap);
							} else if (mGreenMixValues[i] > 0.0 && mBlueMixValues[i] == 0.0) {
								// draw green corner bitmap
								bitmap = mGCorner;
								drawCornerBitmap(canvas, i, bitmap);
							} // do nothing
						} else if (mRedMixValues[i] != null && mGreenMixValues[i] == null && mBlueMixValues[i] != null) {
							if (mRedMixValues[i] > 0.0 && mBlueMixValues[i] > 0.0) {
								// draw magenta corner bitmap
								bitmap = mRBCorner;
								drawCornerBitmap(canvas, i, bitmap);
							} else if (mRedMixValues[i] == 0.0 && mBlueMixValues[i] > 0.0) {
								// draw blue corner bitmap
								bitmap = mBCorner;
								drawCornerBitmap(canvas, i, bitmap);
							} else if (mRedMixValues[i] > 0.0 && mBlueMixValues[i] == 0.0) {
								// draw red corner bitmap
								bitmap = mRCorner;
								drawCornerBitmap(canvas, i, bitmap);
							} // do nothing
						} else if (mRedMixValues[i] != null && mGreenMixValues[i] != null && mBlueMixValues[i] != null) {
							if (mRedMixValues[i] > 0.0 && mGreenMixValues[i] > 0.0 && mBlueMixValues[i] > 0.0) {
								// draw white corner bitmap (RGB)
								bitmap = mRGBCorner;
								drawCornerBitmap(canvas, i, bitmap);
							} else if (mRedMixValues[i] > 0.0 && mGreenMixValues[i] > 0.0 && mBlueMixValues[i] == 0.0) {
								// draw yellow corner bitmap (rg)
								bitmap = mRGCorner;
								drawCornerBitmap(canvas, i, bitmap);
							} else if (mRedMixValues[i] == 0.0 && mGreenMixValues[i] > 0.0 && mBlueMixValues[i] > 0.0) {
								// draw light blue corner bitmap (gb)
								bitmap = mGBCorner;
								drawCornerBitmap(canvas, i, bitmap);
							} else if (mRedMixValues[i] > 0.0 && mGreenMixValues[i] == 0.0 && mBlueMixValues[i] > 0.0) {
								// draw magenta corner bitmap
								bitmap = mRBCorner;
								drawCornerBitmap(canvas, i, bitmap);
							} else if (mRedMixValues[i] > 0.0 && mGreenMixValues[i] == 0.0 && mBlueMixValues[i] == 0.0) {
								// draw red corner bitmap
								bitmap = mRCorner;
								drawCornerBitmap(canvas, i, bitmap);
							} else if (mRedMixValues[i] == 0.0 && mGreenMixValues[i] > 0.0 && mBlueMixValues[i] == 0.0) {
								// draw green corner bitmap
								bitmap = mGCorner;
								drawCornerBitmap(canvas, i, bitmap);
							} else if (mRedMixValues[i] == 0.0 && mGreenMixValues[i] == 0.0 && mBlueMixValues[i] > 0.0) {
								// draw blue corner bitmap
								bitmap = mBCorner;
								drawCornerBitmap(canvas, i, bitmap);
							} // else do nothing
						} // else all values == null - do nothing
				}
			}
		}
	}

	private void drawCornerBitmap(Canvas canvas, int pixIndex, Bitmap bitmap) {
		canvas.drawBitmap(
				bitmap,
				(float) (pixIndex % mParentResolution.x * mPixelSize.x + mPixelSize.x - mCornerDimensions.x),
				(float) (pixIndex / mParentResolution.x * mPixelSize.y + mPixelSize.y - mCornerDimensions.y),
				mPaint
		);
	}

	public void setSelectedRects(ArrayList<Rect> rects) {
		this.mSelectPixels = rects;
	}

	public void setParentResolution(Point resolution) {
		this.mParentResolution = resolution;
	}

	public void setRedMixValues(Double[] values) {
		this.mRedMixValues = values;
	}

	public void setGreenMixValues(Double[] values) {
		this.mGreenMixValues = values;
	}

	public void setBlueMixValues(Double[] values) {
		this.mBlueMixValues = values;
	}

	public void setColorMode(RGBModes mode) {
		this.mColorMode = mode;
	}

	public void setPixelSize(Point size) {
		this.mPixelSize = size;
	}
}
