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

package com.doomonafireball.betterpickers.radialtimepicker;



import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;

import java.text.DateFormatSymbols;

import com.doomonafireball.betterpickers.R;
/**
 * Draw the two smaller AM and PM circles next to where the larger circle will be.
 */
public class AmPmCirclesView extends View {

	private static final String TAG = "AmPmCirclesView";

	// Alpha level of blue color for selected circle.
	private static final int SELECTED_ALPHA = 51;
	// Alpha level of blue color for pressed circle.
	private static final int PRESSED_ALPHA = 175;

	private final Paint mPaint = new Paint();
	private int mWhite;
	private int mAmPmTextColor;
	private int mBlue;
	private float mCircleRadiusMultiplier;
	private float mAmPmCircleRadiusMultiplier;
	private String mAmText;
	private String mPmText;
	private boolean mIsInitialized;

	private static final int AM = RadialTimePickerDialog.AM;
	private static final int PM = RadialTimePickerDialog.PM;

	private boolean mDrawValuesReady;
	private int mAmPmCircleRadius;
	private int mAmXCenter;
	private int mPmXCenter;
	private int mAmPmYCenter;
	private int mAmOrPm;
	private int mAmOrPmPressed;

	public AmPmCirclesView(final Context context) {
		super(context);
		this.mIsInitialized = false;
	}

	/**
	 * Calculate whether the coordinates are touching the AM or PM circle.
	 */
	public int getIsTouchingAmOrPm(final float xCoord, final float yCoord) {
		if (!this.mDrawValuesReady) {
			return -1;
		}

		final int squaredYDistance = (int) ((yCoord - this.mAmPmYCenter) * (yCoord - this.mAmPmYCenter));

		final int distanceToAmCenter =
				(int) Math.sqrt(((xCoord - this.mAmXCenter) * (xCoord - this.mAmXCenter)) + squaredYDistance);
		if (distanceToAmCenter <= this.mAmPmCircleRadius) {
			return AM;
		}

		final int distanceToPmCenter =
				(int) Math.sqrt(((xCoord - this.mPmXCenter) * (xCoord - this.mPmXCenter)) + squaredYDistance);
		if (distanceToPmCenter <= this.mAmPmCircleRadius) {
			return PM;
		}

		// Neither was close enough.
		return -1;
	}

	public void initialize(final Context context, final int amOrPm) {
		if (this.mIsInitialized) {
			Log.e(TAG, "AmPmCirclesView may only be initialized once.");
			return;
		}

		final Resources res = context.getResources();
		this.mWhite = res.getColor(R.color.white);
		this.mAmPmTextColor = res.getColor(R.color.ampm_text_color);
		this.mBlue = res.getColor(R.color.blue);
		final String typefaceFamily = res.getString(R.string.sans_serif);
		final Typeface tf = Typeface.create(typefaceFamily, Typeface.NORMAL);
		this.mPaint.setTypeface(tf);
		this.mPaint.setAntiAlias(true);
		this.mPaint.setTextAlign(Align.CENTER);

		this.mCircleRadiusMultiplier =
				Float.parseFloat(res.getString(R.string.circle_radius_multiplier));
		this.mAmPmCircleRadiusMultiplier =
				Float.parseFloat(res.getString(R.string.ampm_circle_radius_multiplier));
		final String[] amPmTexts = new DateFormatSymbols().getAmPmStrings();
		this.mAmText = amPmTexts[0];
		this.mPmText = amPmTexts[1];

		this.setAmOrPm(amOrPm);
		this.mAmOrPmPressed = -1;

		this.mIsInitialized = true;
	}

	@Override
	public void onDraw(final Canvas canvas) {
		final int viewWidth = this.getWidth();
		if ((viewWidth == 0) || !this.mIsInitialized) {
			return;
		}

		if (!this.mDrawValuesReady) {
			final int layoutXCenter = this.getWidth() / 2;
			final int layoutYCenter = this.getHeight() / 2;
			final int circleRadius =
					(int) (Math.min(layoutXCenter, layoutYCenter) * this.mCircleRadiusMultiplier);
			this.mAmPmCircleRadius = (int) (circleRadius * this.mAmPmCircleRadiusMultiplier);
			final int textSize = (this.mAmPmCircleRadius * 3) / 4;
			this.mPaint.setTextSize(textSize);

			// Line up the vertical center of the AM/PM circles with the bottom of the main circle.
			this.mAmPmYCenter = (layoutYCenter - (this.mAmPmCircleRadius / 2)) + circleRadius;
			// Line up the horizontal edges of the AM/PM circles with the horizontal edges
			// of the main circle.
			this.mAmXCenter = (layoutXCenter - circleRadius) + this.mAmPmCircleRadius;
			this.mPmXCenter = (layoutXCenter + circleRadius) - this.mAmPmCircleRadius;

			this.mDrawValuesReady = true;
		}

		// We'll need to draw either a lighter blue (for selection), a darker blue (for touching)
		// or white (for not selected).
		int amColor = this.mWhite;
		int amAlpha = 255;
		int pmColor = this.mWhite;
		int pmAlpha = 255;
		if (this.mAmOrPm == AM) {
			amColor = this.mBlue;
			amAlpha = SELECTED_ALPHA;
		} else if (this.mAmOrPm == PM) {
			pmColor = this.mBlue;
			pmAlpha = SELECTED_ALPHA;
		}
		if (this.mAmOrPmPressed == AM) {
			amColor = this.mBlue;
			amAlpha = PRESSED_ALPHA;
		} else if (this.mAmOrPmPressed == PM) {
			pmColor = this.mBlue;
			pmAlpha = PRESSED_ALPHA;
		}

		// Draw the two circles.
		this.mPaint.setColor(amColor);
		this.mPaint.setAlpha(amAlpha);
		canvas.drawCircle(this.mAmXCenter, this.mAmPmYCenter, this.mAmPmCircleRadius, this.mPaint);
		this.mPaint.setColor(pmColor);
		this.mPaint.setAlpha(pmAlpha);
		canvas.drawCircle(this.mPmXCenter, this.mAmPmYCenter, this.mAmPmCircleRadius, this.mPaint);

		// Draw the AM/PM texts on top.
		this.mPaint.setColor(this.mAmPmTextColor);
		final int textYCenter = this.mAmPmYCenter - ((int) (this.mPaint.descent() + this.mPaint.ascent()) / 2);
		canvas.drawText(this.mAmText, this.mAmXCenter, textYCenter, this.mPaint);
		canvas.drawText(this.mPmText, this.mPmXCenter, textYCenter, this.mPaint);
	}

	public void setAmOrPm(final int amOrPm) {
		this.mAmOrPm = amOrPm;
	}

	public void setAmOrPmPressed(final int amOrPmPressed) {
		this.mAmOrPmPressed = amOrPmPressed;
	}
}
