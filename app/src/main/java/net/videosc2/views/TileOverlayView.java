package net.videosc2.views;

import android.app.Activity;
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
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import net.videosc2.R;
import net.videosc2.VideOSCApplication;
import net.videosc2.utilities.enums.InteractionModes;
import net.videosc2.utilities.enums.RGBModes;

import java.util.ArrayList;

public class TileOverlayView extends View {
	final private static String TAG = "TileOverlayView";

	final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
	private BitmapShader mShaderSelected;
	private Typeface mTypeFace = Typeface.create("sans-serif-light", Typeface.NORMAL);
	private ArrayList<Rect> mSelectPixels = new ArrayList<>();
	private ArrayList<Double> mRedMixValues, mGreenMixValues, mBlueMixValues;
	private Bitmap mRCorner, mGCorner, mBCorner, mRGCorner, mGBCorner, mRBCorner, mRGBCorner;
	private Point mCornerDimensions;
	private VideOSCApplication mApp;

	/**
	 * Simple constructor to use when creating a view from code.
	 *
	 * @param context The Context the view is running in, through which it can
	 *                access the current theme, resources, etc.
	 */
	public TileOverlayView(Context context) {
		super(context);
		init(context);
	}

	/**
	 * @param context The Context the view is running in, through which it can
	 *                access the current theme, resources, etc.
	 * @param attrs   The attributes of the XML tag that is inflating the view.
	 */
	public TileOverlayView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
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
		init(context);
	}

	private void init(Context context) {
		Resources res = context.getResources();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inMutable = true;
		Bitmap patSrc = BitmapFactory.decodeResource(res, R.drawable.hover_rect_tile, options);
		mShaderSelected = new BitmapShader(patSrc, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		mRCorner = BitmapFactory.decodeResource(res, R.drawable.r_corner);
		mBCorner = BitmapFactory.decodeResource(res, R.drawable.b_corner);
		mGCorner = BitmapFactory.decodeResource(res, R.drawable.g_corner);
		mRGCorner = BitmapFactory.decodeResource(res, R.drawable.rg_corner);
		mRBCorner = BitmapFactory.decodeResource(res, R.drawable.rb_corner);
		mGBCorner = BitmapFactory.decodeResource(res, R.drawable.gb_corner);
		mRGBCorner = BitmapFactory.decodeResource(res, R.drawable.rgb_corner);
		mCornerDimensions = new Point(mRGBCorner.getWidth(), mRGBCorner.getHeight());
		mApp = (VideOSCApplication) ((Activity) context).getApplication();
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
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Bitmap bitmap;
		Point resolution = mApp.getResolution();
		Point pixelSize = mApp.getPixelSize();
		InteractionModes interactionMode = mApp.getInteractionMode();
		RGBModes colorMode = mApp.getColorMode();

		mPaint.setColor(0xff000000);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setShader(mShaderSelected);
		for (Rect rect : mSelectPixels)
			canvas.drawRect(rect, mPaint);
		mPaint.setShader(null);
		mPaint.setColor(0xffffffff);
		mPaint.setTextAlign(Paint.Align.LEFT);
		mPaint.setTypeface(mTypeFace);
		mPaint.setTextSize((float) 30);
		int numPixels = resolution.x * resolution.y;
		if (mRedMixValues != null && mGreenMixValues != null && mBlueMixValues != null) {
			for (int i = 0; i < numPixels; i++) {
				if (interactionMode.equals(InteractionModes.SINGLE_PIXEL)) {
					mPaint.setShadowLayer(5.0f, 2.5f, 2.5f, 0xff000000);
					canvas.drawText(
							String.valueOf(i + 1),
							i % resolution.x * pixelSize.x + 10,
							i / resolution.x * pixelSize.y + pixelSize.y - 10,
							mPaint
					);
				}
				mPaint.clearShadowLayer();
				switch (colorMode) {
					case R:
						if (mRedMixValues.get(i) != null && mRedMixValues.get(i) > 0.0) {
							// draw bitmap - RGB corner (white)
							bitmap = mRGBCorner;
							drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
						}
						break;
					case G:
						if (mGreenMixValues.get(i) != null && mGreenMixValues.get(i) > 0.0) {
							// draw bitmap - RGB corner (white)
							bitmap = mRGBCorner;
							drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
						}
						break;
					case B:
						if (mBlueMixValues.get(i) != null && mBlueMixValues.get(i) > 0.0) {
							// draw bitmap - RGB corner (white)
							bitmap = mRGBCorner;
							drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
						}
						break;
					default: // RGB
						if (mRedMixValues.get(i) != null && mGreenMixValues.get(i) == null && mBlueMixValues.get(i) == null && mRedMixValues.get(i) > 0.0) {
							// draw red corner bitmap
							bitmap = mRCorner;
							drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
						} else if (mRedMixValues.get(i) == null && mGreenMixValues.get(i) != null && mBlueMixValues.get(i) == null && mGreenMixValues.get(i) > 0.0) {
							// draw green corner bitmap
							bitmap = mGBCorner;
							drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
						} else if (mRedMixValues.get(i) == null && mGreenMixValues.get(i) == null && mBlueMixValues.get(i) != null && mBlueMixValues.get(i) > 0.0) {
							// draw blue corner bitmap
							bitmap = mBCorner;
							drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
						} else if (mRedMixValues.get(i) != null && mGreenMixValues.get(i) != null && mBlueMixValues.get(i) == null) {
							if (mRedMixValues.get(i) > 0.0 && mGreenMixValues.get(i) > 0.0) {
								// draw yellow corner bitmap (rg)
								bitmap = mRGCorner;
								drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
							} else if (mRedMixValues.get(i) == 0.0 && mGreenMixValues.get(i) > 0.0) {
								// draw green corner bitmap
								bitmap = mGCorner;
								drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
							} else if (mRedMixValues.get(i) > 0.0 && mGreenMixValues.get(i) > 0.0) {
								// draw red corner bitmap
								bitmap = mRCorner;
								drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
							} // else both values are 0.0 - do nothing
						} else if (mRedMixValues.get(i) == null && mGreenMixValues.get(i) != null && mBlueMixValues.get(i) != null) {
							if (mGreenMixValues.get(i) > 0.0 && mBlueMixValues.get(i) > 0.0) {
								// draw light blue corner bitmap
								bitmap = mGBCorner;
								drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
							} else if (mGreenMixValues.get(i) == 0.0 && mBlueMixValues.get(i) > 0.0) {
								// draw blue corner bitmap
								bitmap = mBCorner;
								drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
							} else if (mGreenMixValues.get(i) > 0.0 && mBlueMixValues.get(i) == 0.0) {
								// draw green corner bitmap
								bitmap = mGCorner;
								drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
							} // do nothing
						} else if (mRedMixValues.get(i) != null && mGreenMixValues.get(i) == null && mBlueMixValues.get(i) != null) {
							if (mRedMixValues.get(i) > 0.0 && mBlueMixValues.get(i) > 0.0) {
								// draw magenta corner bitmap
								bitmap = mRBCorner;
								drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
							} else if (mRedMixValues.get(i) == 0.0 && mBlueMixValues.get(i) > 0.0) {
								// draw blue corner bitmap
								bitmap = mBCorner;
								drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
							} else if (mRedMixValues.get(i) > 0.0 && mBlueMixValues.get(i) == 0.0) {
								// draw red corner bitmap
								bitmap = mRCorner;
								drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
							} // do nothing
						} else if (mRedMixValues.get(i) != null && mGreenMixValues.get(i) != null && mBlueMixValues.get(i) != null) {
							if (mRedMixValues.get(i) > 0.0 && mGreenMixValues.get(i) > 0.0 && mBlueMixValues.get(i) > 0.0) {
								// draw white corner bitmap (RGB)
								bitmap = mRGBCorner;
								drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
							} else if (mRedMixValues.get(i) > 0.0 && mGreenMixValues.get(i) > 0.0 && mBlueMixValues.get(i) == 0.0) {
								// draw yellow corner bitmap (rg)
								bitmap = mRGCorner;
								drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
							} else if (mRedMixValues.get(i) == 0.0 && mGreenMixValues.get(i) > 0.0 && mBlueMixValues.get(i) > 0.0) {
								// draw light blue corner bitmap (gb)
								bitmap = mGBCorner;
								drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
							} else if (mRedMixValues.get(i) > 0.0 && mGreenMixValues.get(i) == 0.0 && mBlueMixValues.get(i) > 0.0) {
								// draw magenta corner bitmap
								bitmap = mRBCorner;
								drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
							} else if (mRedMixValues.get(i) > 0.0 && mGreenMixValues.get(i) == 0.0 && mBlueMixValues.get(i) == 0.0) {
								// draw red corner bitmap
								bitmap = mRCorner;
								drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
							} else if (mRedMixValues.get(i) == 0.0 && mGreenMixValues.get(i) > 0.0 && mBlueMixValues.get(i) == 0.0) {
								// draw green corner bitmap
								bitmap = mGCorner;
								drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
							} else if (mRedMixValues.get(i) == 0.0 && mGreenMixValues.get(i) == 0.0 && mBlueMixValues.get(i) > 0.0) {
								// draw blue corner bitmap
								bitmap = mBCorner;
								drawCornerBitmap(canvas, i, bitmap, resolution, pixelSize);
							} // else do nothing
						} // else all values == null - do nothing
				}
			}
		}
	}

	private void drawCornerBitmap(Canvas canvas, int pixIndex, Bitmap bitmap, Point resolution, Point pixelSize) {
		canvas.drawBitmap(
				bitmap,
				(float) (pixIndex % resolution.x * pixelSize.x + pixelSize.x - mCornerDimensions.x),
				(float) (pixIndex / resolution.x * pixelSize.y + pixelSize.y - mCornerDimensions.y),
				mPaint
		);
	}

	public void setSelectedRects(ArrayList<Rect> rects) {
		this.mSelectPixels = rects;
	}

	public void setRedMixValues(ArrayList<Double> values) {
		this.mRedMixValues = values;
	}

	public void setGreenMixValues(ArrayList<Double> values) {
		this.mGreenMixValues = values;
	}

	public void setBlueMixValues(ArrayList<Double> values) {
		this.mBlueMixValues = values;
	}
}
