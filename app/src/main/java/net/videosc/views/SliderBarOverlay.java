package net.videosc.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import net.videosc.VideOSCApplication;
import net.videosc.activities.VideOSCMainActivity;

public class SliderBarOverlay extends View {
    private static final String TAG = SliderBarOverlay.class.getSimpleName();

    private int mLeft, mTop, mRight, mBottom;
    final private Rect mArea = new Rect(mLeft, mTop, mRight, mBottom);
    private VideOSCApplication mApp;
    private Paint mPaint;
    private float mScreenDensity;
    private int mParentWidth;
    private int mNumSliders;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public SliderBarOverlay(Context context) {
        super(context);
        init(context);
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called
     * when a view is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.
     *
     * <p>
     * The method onFinishInflate() will be called after all children have been
     * added.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public SliderBarOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style from a
     * theme attribute. This constructor of View allows subclasses to use their
     * own base style when they are inflating. For example, a Button class's
     * constructor would call this version of the super class constructor and
     * supply <code>R.attr.buttonStyle</code> for <var>defStyleAttr</var>; this
     * allows the theme's button style to modify all of the base view attributes
     * (in particular its background) as well as the Button class's attributes.
     *
     * @param context      The Context the view is running in, through which it can
     *                     access the current theme, resources, etc.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     */
    public SliderBarOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        Log.d(TAG, "init: " + context);
        this.mApp = (VideOSCApplication) ((VideOSCMainActivity) context).getApplication();
        this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mScreenDensity = mApp.getScreenDensity();
    }

    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw: " + canvas);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5f);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setColor(0xff000000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//            canvas.drawRoundRect((float) mLeft, (float) mTop, (float) mRight, (float) mBottom, 5.0f * mScreenDensity, 5.0f * mScreenDensity, mPaint);
            canvas.drawRoundRect((float) mLeft, (float) mTop, (float) mRight, (float) mBottom, 5.0f * mScreenDensity, 5.0f * mScreenDensity, mPaint);
        else
            canvas.drawRect(mLeft, mTop, mRight, mBottom, mPaint);
        Log.d(TAG, "get left: " + getLeft() + ", get top: " + getTop() + ", get right: " + getRight() + " get bottom: " + getBottom());
    }

    /**
     * Called from layout when this view should
     * assign a size and position to each of its children.
     * <p>
     * Derived classes with children should override
     * this method and call layout on each of
     * their children.
     *
     * @param changed This is a new size or position for this view
     * @param left    Left position, relative to parent
     * @param top     Top position, relative to parent
     * @param right   Right position, relative to parent
     * @param bottom  Bottom position, relative to parent
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        Log.d(TAG, "changed: " + changed + ", left: " + left + ", top: " + top + ", right: " + right + ", bottom: " + bottom);
        this.mLeft = left;
        this.mTop = top;
        this.mRight = right;
        this.mBottom = bottom;
    }

    public void setText(String label) {
        Log.d(TAG, "label text: " + label);
    }
}
