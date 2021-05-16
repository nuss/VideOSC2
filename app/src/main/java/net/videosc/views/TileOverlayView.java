package net.videosc.views;

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
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;

import androidx.annotation.Nullable;

import net.videosc.R;
import net.videosc.VideOSCApplication;
import net.videosc.utilities.VideOSCOscHandler;
import net.videosc.utilities.enums.InteractionModes;
import net.videosc.utilities.enums.RGBModes;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class TileOverlayView extends View {
    final private static String TAG = "TileOverlayView";

    final private Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private BitmapShader mShaderSelected;
    final private Typeface mTypeFace = Typeface.create("sans-serif-light", Typeface.NORMAL);
    private ArrayList<Rect> mSelectPixels = new ArrayList<>();
    private ArrayList<Double> mRedMixValues, mGreenMixValues, mBlueMixValues;
    private Bitmap mRCorner, mGCorner, mBCorner, mRGCorner, mGBCorner, mRBCorner, mRGBCorner;
    private Point mCornerDimensions;
    private VideOSCApplication mApp;
    private Point mResolution;
    private Point mPixelSize;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public TileOverlayView(Context context) {
        super(context);
        final WeakReference<Context> contextRef = new WeakReference<>(context);
        init(contextRef);
    }

    /**
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public TileOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        final WeakReference<Context> contextRef = new WeakReference<>(context);
        init(contextRef);
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
        final WeakReference<Context> contextRef = new WeakReference<>(context);
        init(contextRef);
    }

    private void init(WeakReference<Context> contextRef) {
        Resources res = contextRef.get().getResources();
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
        mApp = (VideOSCApplication) ((Activity) contextRef.get()).getApplication();
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
        mResolution = mApp.getResolution();
        mPixelSize = mApp.getPixelSize();
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
        mPaint.setTextSize(12f * mApp.getScreenDensity());
        final int numPixels = mResolution.x * mResolution.y;
        boolean oscFeedbackActivated = mApp.getOSCFeedbackActivated();

        if (mRedMixValues != null && mGreenMixValues != null && mBlueMixValues != null) {
            for (int i = 0; i < numPixels; i++) {
                if (interactionMode.equals(InteractionModes.SINGLE_PIXEL)) {
                    mPaint.setShadowLayer(5.0f, 2.5f, 2.5f, 0xff000000);
                    canvas.drawText(
                            String.valueOf(i + 1),
                            i % mResolution.x * mPixelSize.x + 3.5f * mApp.getScreenDensity(),
                            (float) (i / mResolution.x) * mPixelSize.y + mPixelSize.y - 3.5f * mApp.getScreenDensity(),
                            mPaint
                    );
                }
                mPaint.clearShadowLayer();

                // protect against IndexOutOfBounds errors (crashes) first
                if (mRedMixValues.size() > i && mGreenMixValues.size() > i && mBlueMixValues.size() > i) {
                    switch (colorMode) {
                        case R:
                            if (mRedMixValues.get(i) != null && mRedMixValues.get(i) > 0.0) {
                                // draw bitmap - RGB corner (white)
                                bitmap = mRGBCorner;
                                drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                            }
                            break;
                        case G:
                            if (mGreenMixValues.get(i) != null && mGreenMixValues.get(i) > 0.0) {
                                // draw bitmap - RGB corner (white)
                                bitmap = mRGBCorner;
                                drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                            }
                            break;
                        case B:
                            if (mBlueMixValues.get(i) != null && mBlueMixValues.get(i) > 0.0) {
                                // draw bitmap - RGB corner (white)
                                bitmap = mRGBCorner;
                                drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                            }
                            break;
                        default: // RGB
                            if (mRedMixValues.get(i) != null && mGreenMixValues.get(i) == null && mBlueMixValues.get(i) == null && mRedMixValues.get(i) > 0.0) {
                                // draw red corner bitmap
                                bitmap = mRCorner;
                                drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                            } else if (mRedMixValues.get(i) == null && mGreenMixValues.get(i) != null && mBlueMixValues.get(i) == null && mGreenMixValues.get(i) > 0.0) {
                                // draw green corner bitmap
                                bitmap = mGBCorner;
                                drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                            } else if (mRedMixValues.get(i) == null && mGreenMixValues.get(i) == null && mBlueMixValues.get(i) != null && mBlueMixValues.get(i) > 0.0) {
                                // draw blue corner bitmap
                                bitmap = mBCorner;
                                drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                            } else if (mRedMixValues.get(i) != null && mGreenMixValues.get(i) != null && mBlueMixValues.get(i) == null) {
                                if (mRedMixValues.get(i) > 0.0 && mGreenMixValues.get(i) > 0.0) {
                                    // draw yellow corner bitmap (rg)
                                    bitmap = mRGCorner;
                                    drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                                } else if (mRedMixValues.get(i) == 0.0 && mGreenMixValues.get(i) > 0.0) {
                                    // draw green corner bitmap
                                    bitmap = mGCorner;
                                    drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                                } else if (mRedMixValues.get(i) > 0.0 && mGreenMixValues.get(i) == 0.0) {
                                    // draw red corner bitmap
                                    bitmap = mRCorner;
                                    drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                                } // else both values are 0.0 - do nothing
                            } else if (mRedMixValues.get(i) == null && mGreenMixValues.get(i) != null && mBlueMixValues.get(i) != null) {
                                if (mGreenMixValues.get(i) > 0.0 && mBlueMixValues.get(i) > 0.0) {
                                    // draw light blue corner bitmap
                                    bitmap = mGBCorner;
                                    drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                                } else if (mGreenMixValues.get(i) == 0.0 && mBlueMixValues.get(i) > 0.0) {
                                    // draw blue corner bitmap
                                    bitmap = mBCorner;
                                    drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                                } else if (mGreenMixValues.get(i) > 0.0 && mBlueMixValues.get(i) == 0.0) {
                                    // draw green corner bitmap
                                    bitmap = mGCorner;
                                    drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                                } // do nothing
                            } else if (mRedMixValues.get(i) != null && mGreenMixValues.get(i) == null && mBlueMixValues.get(i) != null) {
                                if (mRedMixValues.get(i) > 0.0 && mBlueMixValues.get(i) > 0.0) {
                                    // draw magenta corner bitmap
                                    bitmap = mRBCorner;
                                    drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                                } else if (mRedMixValues.get(i) == 0.0 && mBlueMixValues.get(i) > 0.0) {
                                    // draw blue corner bitmap
                                    bitmap = mBCorner;
                                    drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                                } else if (mRedMixValues.get(i) > 0.0 && mBlueMixValues.get(i) == 0.0) {
                                    // draw red corner bitmap
                                    bitmap = mRCorner;
                                    drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                                } // do nothing
                            } else if (mRedMixValues.get(i) != null && mGreenMixValues.get(i) != null && mBlueMixValues.get(i) != null) {
                                if (mRedMixValues.get(i) > 0.0 && mGreenMixValues.get(i) > 0.0 && mBlueMixValues.get(i) > 0.0) {
                                    // draw white corner bitmap (RGB)
                                    bitmap = mRGBCorner;
                                    drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                                } else if (mRedMixValues.get(i) > 0.0 && mGreenMixValues.get(i) > 0.0 && mBlueMixValues.get(i) == 0.0) {
                                    // draw yellow corner bitmap (rg)
                                    bitmap = mRGCorner;
                                    drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                                } else if (mRedMixValues.get(i) == 0.0 && mGreenMixValues.get(i) > 0.0 && mBlueMixValues.get(i) > 0.0) {
                                    // draw light blue corner bitmap (gb)
                                    bitmap = mGBCorner;
                                    drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                                } else if (mRedMixValues.get(i) > 0.0 && mGreenMixValues.get(i) == 0.0 && mBlueMixValues.get(i) > 0.0) {
                                    // draw magenta corner bitmap
                                    bitmap = mRBCorner;
                                    drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                                } else if (mRedMixValues.get(i) > 0.0 && mGreenMixValues.get(i) == 0.0 && mBlueMixValues.get(i) == 0.0) {
                                    // draw red corner bitmap
                                    bitmap = mRCorner;
                                    drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                                } else if (mRedMixValues.get(i) == 0.0 && mGreenMixValues.get(i) > 0.0 && mBlueMixValues.get(i) == 0.0) {
                                    // draw green corner bitmap
                                    bitmap = mGCorner;
                                    drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                                } else if (mRedMixValues.get(i) == 0.0 && mGreenMixValues.get(i) == 0.0 && mBlueMixValues.get(i) > 0.0) {
                                    // draw blue corner bitmap
                                    bitmap = mBCorner;
                                    drawCornerBitmap(canvas, i, bitmap, mResolution, mPixelSize);
                                } // else do nothing
                            } // else all values == null - do nothing
                    }
                }
            }
        }

        if (oscFeedbackActivated) {
            VideOSCOscHandler oscHelper = mApp.getOscHelper();
            mPaint.setTextSize(15f * mApp.getScreenDensity());
            float nextY = mPaint.getTextSize() - 2 * mApp.getScreenDensity();

            final ArrayList<SparseArray<String>> redFeedbackStrings = oscHelper.getRedFeedbackStrings();
            final ArrayList<SparseArray<String>> greenFeedbackStrings = oscHelper.getGreenFeedbackStrings();
            final ArrayList<SparseArray<String>> blueFeedbackStrings = oscHelper.getBlueFeedbackStrings();

            final ArrayList<SparseIntArray> redThreshes = oscHelper.getRedThresholds();
            final ArrayList<SparseIntArray> greenThreshes = oscHelper.getGreenThresholds();
            final ArrayList<SparseIntArray> blueThreshes = oscHelper.getBlueThresholds();

            for (int i = 0; i < numPixels; i++) {
                if (redFeedbackStrings.size() == numPixels && redFeedbackStrings.get(i) != null) {
                    final SparseArray<String> fbStrings = redFeedbackStrings.get(i);
                    final SparseIntArray threshes = redThreshes.get(i);
                    final int numFbStrings = threshes.size();

                    // if we're in RGB mode set text color to the corresponding color channel
                    // otherwise text should be white
                    if (mApp.getColorMode().equals(RGBModes.RGB))
                        mPaint.setColor(0xffff0000);

                    nextY = printOrRemoveFeedback(canvas, fbStrings, threshes, numFbStrings, i, nextY, RGBModes.R, 0xffff0000);
                }

                if (greenFeedbackStrings.size() == numPixels && greenFeedbackStrings.get(i) != null) {
                    final SparseArray<String> fbStrings = greenFeedbackStrings.get(i);
                    final SparseIntArray threshes = greenThreshes.get(i);
                    final int numFbStrings = threshes.size();

                    if (mApp.getColorMode().equals(RGBModes.RGB))
                        // make green background a bit darker for better readability
                        mPaint.setColor(0xff00aa00);

                    nextY = printOrRemoveFeedback(canvas, fbStrings, threshes, numFbStrings, i, nextY, RGBModes.G, 0xff00aa00);
                }

                if (blueFeedbackStrings.size() == numPixels && blueFeedbackStrings.get(i) != null) {
                    final SparseArray<String> fbStrings = blueFeedbackStrings.get(i);
                    final SparseIntArray threshes = blueThreshes.get(i);
                    final int numFbStrings = threshes.size();

                    if (mApp.getColorMode().equals(RGBModes.RGB))
                        mPaint.setColor(0xff0000ff);

                    printOrRemoveFeedback(canvas, fbStrings, threshes, numFbStrings, i, nextY, RGBModes.B, 0xff0000ff);
                }

                // reset nextY
                nextY = mPaint.getTextSize() - 2 * mApp.getScreenDensity();
            }

            mPaint.clearShadowLayer();
        }
    }

    /**
     * This is called when the view is detached from a window.  At this point it
     * no longer has a surface for drawing.
     *
     * @see #onAttachedToWindow()
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private float printOrRemoveFeedback(Canvas canvas, SparseArray<String> fbStrings, SparseIntArray threshes, int numFbStrings, int pixel, float nextY, RGBModes mode, int resetColor) {
        for (int i = 0; i < numFbStrings; i++) {
            if (threshes.size() > i) {
                final int thresh = threshes.valueAt(i);
                final int threshKey = threshes.keyAt(i);
                final String fbString = fbStrings.get(threshKey);
                threshes.put(threshKey, thresh - 1);
                // if threshold is at least 0 print feedback
                // feedback might not necessarily have come in with last OSC message
                // but is still cached
                if (thresh >= 0 && fbString != null && (mApp.getColorMode().equals(RGBModes.RGB) || mApp.getColorMode().equals(mode))) {
                    drawFeedbackStrings(canvas, pixel, fbString, mResolution, mPixelSize, nextY, resetColor);
                    // increment Y position by the number of lines already written
                    nextY += mPaint.getTextSize();
                } else {
                    fbStrings.delete(threshKey);
                    threshes.delete(threshKey);
                }
            }
        }

        return nextY;
    }

    private void drawCornerBitmap(Canvas canvas, int pixIndex, Bitmap bitmap, Point resolution, Point pixelSize) {
        canvas.drawBitmap(
                bitmap,
                (float) (pixIndex % resolution.x * pixelSize.x + pixelSize.x - mCornerDimensions.x),
                (float) (pixIndex / resolution.x * pixelSize.y + pixelSize.y - mCornerDimensions.y),
                mPaint
        );
    }

    private void drawFeedbackStrings(Canvas canvas, int pixIndex, String text, Point resolution, Point pixelSize, float nextY, int resetColor) {
        final float left = pixIndex % resolution.x * pixelSize.x + 3.5f * mApp.getScreenDensity();
        final float top = (float) (pixIndex / resolution.x) * pixelSize.y + 3.5f * mApp.getScreenDensity() + nextY;

        if (mApp.getColorMode().equals(RGBModes.RGB)) {
            canvas.drawRect(left - 3, top - 12f * mApp.getScreenDensity() - 3, left + mPaint.measureText(text) + 3, top + 3, mPaint);
            mPaint.setColor(0xffffffff);
        }
        canvas.drawText(text, left, top, mPaint);
        mPaint.setColor(resetColor);
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
