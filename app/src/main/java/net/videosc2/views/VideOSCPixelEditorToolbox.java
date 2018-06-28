package net.videosc2.views;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import net.videosc2.R;
import net.videosc2.activities.VideOSCMainActivity;

public class VideOSCPixelEditorToolbox extends LinearLayout {
	final static private String TAG = "PixelEditorToolbox";
	private ViewGroup mContainer;
	private ImageButton mDeleteEdits;
	private ImageButton mQuickEditPixels;
	private ImageButton mEditPixels;
	private ImageButton mApplySelection;

	public VideOSCPixelEditorToolbox(Context context) {
		super(context);
		init(context);
	}

	public VideOSCPixelEditorToolbox(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public VideOSCPixelEditorToolbox(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
//		this.mContainer = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.pixel_editor_toolbox, this);
//		this.mDeleteEdits = (ImageButton) findViewById(R.id.delete_edits);
//		this.mQuickEditPixels = (ImageButton) findViewById(R.id.quick_edit_pixels);
//		this.mEditPixels = (ImageButton) findViewById(R.id.edit_pixels);
//		this.mApplySelection = (ImageButton) findViewById(R.id.apply_pixel_selection);
//		Log.d(TAG, "props of delete button: " + this.getLeft() + ", " + this.getTop() + ", " + this.getRight() + ", " + this.getBottom());
	}

	/*@Override
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
	}*/

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int numChildren = getChildCount();
		Log.d(TAG, "number of children: " + numChildren + ", left: " + l + ", top: " + t + ", right: " + r + ", bottom: " + b);
		int nextX = 0;
		for (int i = 0; i < numChildren; i++) {
			ImageButton child = (ImageButton) getChildAt(i);
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			child.setLeft(lp.leftMargin + nextX);
			child.setTop(lp.topMargin);
			child.setRight(lp.leftMargin + lp.width + nextX);
			child.setBottom(lp.topMargin + lp.height);
			nextX = lp.leftMargin + lp.width + lp.rightMargin + nextX;
			Log.d(TAG, "id: " + child.getId());
			Log.d(TAG, "child at " + i + ": " + lp.width + ", " + lp.height + ", " + lp.leftMargin + ", " + lp.topMargin + ", " + lp.rightMargin + ", " + lp.bottomMargin);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		final int action = MotionEventCompat.getActionMasked(event);

		Log.d(TAG, "action: " + action);
		if (action == MotionEvent.ACTION_MOVE) {
			Log.d(TAG, "moving, moving!");
		}

		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		performClick();
		final int action = event.getAction();
		Log.d(TAG, "touch event: " + action);
		// TODO
		return true;
	}

	@Override
	public boolean performClick() {
		super.performClick();
		return false;
	}
}
