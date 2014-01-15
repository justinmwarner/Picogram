package com.doomonafireball.betterpickers.numberpicker;


import com.doomonafireball.betterpickers.widget.ZeroTopPaddingTextView;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.doomonafireball.betterpickers.R;
public class NumberView extends LinearLayout {

	private ZeroTopPaddingTextView mNumber, mDecimal;
	private ZeroTopPaddingTextView mDecimalSeperator;
	private ZeroTopPaddingTextView mMinusLabel;
	private final Typeface mAndroidClockMonoThin;
	private Typeface mOriginalNumberTypeface;

	private ColorStateList mTextColor;

	/**
	 * Instantiate a NumberView
	 *
	 * @param context the Context in which to inflate the View
	 */
	public NumberView(final Context context) {
		this(context, null);
	}

	/**
	 * Instantiate a NumberView
	 *
	 * @param context the Context in which to inflate the View
	 * @param attrs attributes that define the title color
	 */
	public NumberView(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		this.mAndroidClockMonoThin =
				Typeface.createFromAsset(context.getAssets(), "fonts/AndroidClockMono-Thin.ttf");

		// Init defaults
		this.mTextColor = this.getResources().getColorStateList(R.color.dialog_text_color_holo_dark);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		this.mNumber = (ZeroTopPaddingTextView) this.findViewById(R.id.number);
		this.mDecimal = (ZeroTopPaddingTextView) this.findViewById(R.id.decimal);
		this.mDecimalSeperator = (ZeroTopPaddingTextView) this.findViewById(R.id.decimal_separator);
		this.mMinusLabel = (ZeroTopPaddingTextView) this.findViewById(R.id.minus_label);
		if (this.mNumber != null) {
			this.mOriginalNumberTypeface = this.mNumber.getTypeface();
		}
		// Set the lowest time unit with thin font
		if (this.mNumber != null) {
			this.mNumber.setTypeface(this.mAndroidClockMonoThin);
			this.mNumber.updatePadding();
		}
		if (this.mDecimal != null) {
			this.mDecimal.setTypeface(this.mAndroidClockMonoThin);
			this.mDecimal.updatePadding();
		}

		this.restyleViews();
	}

	private void restyleViews() {
		if (this.mNumber != null) {
			this.mNumber.setTextColor(this.mTextColor);
		}
		if (this.mDecimal != null) {
			this.mDecimal.setTextColor(this.mTextColor);
		}
		if (this.mDecimalSeperator != null) {
			this.mDecimalSeperator.setTextColor(this.mTextColor);
		}
		if (this.mMinusLabel != null) {
			this.mMinusLabel.setTextColor(this.mTextColor);
		}
	}

	/**
	 * Set the number shown
	 *
	 * @param numbersDigit the non-decimal digits
	 * @param decimalDigit the decimal digits
	 * @param showDecimal whether it's a decimal or not
	 * @param isNegative whether it's positive or negative
	 */
	public void setNumber(final String numbersDigit, final String decimalDigit, final boolean showDecimal,
			final boolean isNegative) {
		this.mMinusLabel.setVisibility(isNegative ? View.VISIBLE : View.GONE);
		if (this.mNumber != null) {
			if (numbersDigit.equals("")) {
				// Set to -
				this.mNumber.setText("-");
				this.mNumber.setTypeface(this.mAndroidClockMonoThin);
				this.mNumber.setEnabled(false);
				this.mNumber.updatePadding();
				this.mNumber.setVisibility(View.VISIBLE);
			} else if (showDecimal) {
				// Set to bold
				this.mNumber.setText(numbersDigit);
				this.mNumber.setTypeface(this.mOriginalNumberTypeface);
				this.mNumber.setEnabled(true);
				this.mNumber.updatePadding();
				this.mNumber.setVisibility(View.VISIBLE);
			} else {
				// Set to thin
				this.mNumber.setText(numbersDigit);
				this.mNumber.setTypeface(this.mAndroidClockMonoThin);
				this.mNumber.setEnabled(true);
				this.mNumber.updatePadding();
				this.mNumber.setVisibility(View.VISIBLE);
			}
		}
		if (this.mDecimal != null) {
			// Hide digit
			if (decimalDigit.equals("")) {
				this.mDecimal.setVisibility(View.GONE);
			} else {
				this.mDecimal.setText(decimalDigit);
				this.mDecimal.setTypeface(this.mAndroidClockMonoThin);
				this.mDecimal.setEnabled(true);
				this.mDecimal.updatePadding();
				this.mDecimal.setVisibility(View.VISIBLE);
			}
		}
		if (this.mDecimalSeperator != null) {
			// Hide separator
			this.mDecimalSeperator.setVisibility(showDecimal ? View.VISIBLE : View.GONE);
		}
	}

	/**
	 * Set a theme and restyle the views. This View will change its title color.
	 *
	 * @param themeResId the resource ID for theming
	 */
	public void setTheme(final int themeResId) {
		if (themeResId != -1) {
			final TypedArray a = this.getContext().obtainStyledAttributes(themeResId, R.styleable.BetterPickersDialogFragment);

			this.mTextColor = a.getColorStateList(R.styleable.BetterPickersDialogFragment_bpTextColor);
		}

		this.restyleViews();
	}
}