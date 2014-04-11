
package com.picogram.awesomeness;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * http://sriramramani.wordpress.com/2013/10/14/number-tweening <br/>
 * Note that this is just an example of how you can do number morphing,
 * you'll still need to modify the View so it can support resizing,
 * correct update delay, etc. <br/>
 * -added the control points
 */
public class ColorStrengthView extends View {

	private final Paint mPaint;
	private int[] colors;

	public int[] getColors() {
		return colors;
	}

	public void setColors(int[] colors) {
		this.colors = colors;
	}

	public ColorStrengthView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// A new paint with the style as stroke.
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.BLACK);
		mPaint.setStrokeWidth(5.0f);
		mPaint.setStyle(Paint.Style.STROKE);

	}

	@Override
	public void onDraw(Canvas canvas) {
		int count = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.MATRIX_SAVE_FLAG
				| Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
				| Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG);

		super.onDraw(canvas);

		canvas.restoreToCount(count);
	}
}
