/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.doomonafireball.betterpickers.calendardatepicker;


import com.doomonafireball.betterpickers.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * A text view which, when pressed or activated, displays a blue circle around the text.
 */
public class TextViewWithCircularIndicator extends TextView {

	private static final int SELECTED_CIRCLE_ALPHA = 60;

	Paint mCirclePaint = new Paint();

	private final int mRadius;
	private final int mCircleColor;
	private final String mItemIsSelectedText;

	private boolean mDrawCircle;

	public TextViewWithCircularIndicator(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		final Resources res = context.getResources();
		this.mCircleColor = res.getColor(R.color.blue);
		this.mRadius = res.getDimensionPixelOffset(R.dimen.month_select_circle_radius);
		this.mItemIsSelectedText = context.getResources().getString(R.string.item_is_selected);

		this.init();
	}

	public void drawIndicator(final boolean drawCircle) {
		this.mDrawCircle = drawCircle;
	}

	@Override
	public CharSequence getContentDescription() {
		final CharSequence itemText = this.getText();
		if (this.mDrawCircle) {
			return String.format(this.mItemIsSelectedText, itemText);
		} else {
			return itemText;
		}
	}

	private void init() {
		this.mCirclePaint.setFakeBoldText(true);
		this.mCirclePaint.setAntiAlias(true);
		this.mCirclePaint.setColor(this.mCircleColor);
		this.mCirclePaint.setTextAlign(Align.CENTER);
		this.mCirclePaint.setStyle(Style.FILL);
		this.mCirclePaint.setAlpha(SELECTED_CIRCLE_ALPHA);
	}

	@Override
	public void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		if (this.mDrawCircle) {
			final int width = this.getWidth();
			final int height = this.getHeight();
			final int radius = Math.min(width, height) / 2;
			canvas.drawCircle(width / 2, height / 2, radius, this.mCirclePaint);
		}
	}
}
