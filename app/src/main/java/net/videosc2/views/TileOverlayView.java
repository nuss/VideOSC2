package net.videosc2.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import net.videosc2.R;

import java.util.ArrayList;

public class TileOverlayView extends View {
	final private static String TAG = "TileOverlayView";
	private Context mContext;
	private Point mDimensions = new Point(0, 0);

	final Bitmap.Config mInPreferredConfig = Bitmap.Config.ARGB_8888;
//	final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
	final Paint mPaint = new Paint();
	private Bitmap mBmp;
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
		init(context);
	}

	private void init(Context context) {
		Log.d(TAG, "init");
		mContext = context;
		int pixelSurface = 100;
		int[] colors = new int[pixelSurface];
		for (int i = 0; i < pixelSurface; i++)
			colors[i] = 0x00000000;
//		Drawable tmpDrawable = getResources().getDrawable(R.id.selected_pixel_rect_bitmap);
//		mBmp = ((BitmapDrawable) tmpDrawable).getBitmap();
//		mBmp = Bitmap.createBitmap(colors, 10, 10, mInPreferredConfig);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inMutable = true;
		// FIXME: why can't a bitmap not get scaled up?
		mBmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.empty, options);
//		mBmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
//		tmpDrawable = getResources().getDrawable(R.id.tile_selected);
//		Bitmap patSrc = ((BitmapDrawable) tmpDrawable).getBitmap();
		Bitmap patSrc = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.hover_rect_tile, options);
		Log.d(TAG, "mBmp: " + mBmp + ", patSrc: " + patSrc);
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
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		Log.d(TAG, "onLayout");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			if (!mSelectPixels.isEmpty()) {
				mBmp.reconfigure(
						mSelectPixels.get(0).left - mSelectPixels.get(0).right,
						mSelectPixels.get(0).top - mSelectPixels.get(0).bottom,
						mInPreferredConfig
				);
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// FIXME: drawing never gets triggered
		Log.d(TAG, "should be drawing now");
		for (Rect rect : mSelectPixels) {
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeWidth(2);
			mPaint.setColor(0xFFCC0000);
			canvas.drawRect(rect, mPaint);
//			mPaint.setColor(0x00000000);
//			mPaint.setShader(mShaderSelected);
//			canvas.drawBitmap(mBmp, rect, rect, mPaint);
			Log.d(TAG, "onDraw: " + rect);
		}

	}

	public void setSelectedRects(ArrayList<Rect> rects) {
		this.mSelectPixels = rects;
	}
}
