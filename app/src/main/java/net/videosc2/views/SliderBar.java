package net.videosc2.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

/**
 * Created by stefan on 17.05.18, package net.videosc2.views, project VideOSC22.
 */
public class SliderBar extends View {

	Paint paint;
	Canvas canvas;
	Rect rect;
	int left, top, width, height;

	public SliderBar(Context context, int left, int top, int width, int height) {
		super(context);
		setFocusable(true);
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		paint.setAntiAlias(true);
//		paint.setDither(true);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeWidth(5);
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.parseColor("#000000FF"));
	}
}
