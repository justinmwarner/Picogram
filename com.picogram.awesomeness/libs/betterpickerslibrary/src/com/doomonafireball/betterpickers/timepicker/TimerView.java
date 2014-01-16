package com.doomonafireball.betterpickers.timepicker;


import com.doomonafireball.betterpickers.widget.ZeroTopPaddingTextView;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.doomonafireball.betterpickers.R;
public class TimerView extends LinearLayout {

	private ZeroTopPaddingTextView mHoursOnes, mMinutesOnes;
	private ZeroTopPaddingTextView mHoursTens, mMinutesTens;
	private final Typeface mAndroidClockMonoThin;
	private Typeface mOriginalHoursTypeface;

	private ZeroTopPaddingTextView mHoursSeperator;
	private ColorStateList mTextColor;

	/**
	 * Instantiates a TimerView
	 *
	 * @param context the Context in which to inflate the View
	 */
	public TimerView(final Context context) {
		this(context, null);
	}

	/**
	 * Instantiates a TimerView
	 *
	 * @param context the Context in which to inflate the View
	 * @param attrs attributes that define the text color
	 */
	public TimerView(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		this.mAndroidClockMonoThin =
				Typeface.createFromAsset(context.getAssets(), "fonts/AndroidClockMono-Thin.ttf");

		// Init defaults
		this.mTextColor = this.getResources().getColorStateList(R.color.dialog_text_color_holo_dark);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		this.mHoursTens = (ZeroTopPaddingTextView) this.findViewById(R.id.hours_tens);
		this.mMinutesTens = (ZeroTopPaddingTextView) this.findViewById(R.id.minutes_tens);
		this.mHoursOnes = (ZeroTopPaddingTextView) this.findViewById(R.id.hours_ones);
		this.mMinutesOnes = (ZeroTopPaddingTextView) this.findViewById(R.id.minutes_ones);
		this.mHoursSeperator = (ZeroTopPaddingTextView) this.findViewById(R.id.hours_seperator);
		if (this.mHoursOnes != null) {
			this.mOriginalHoursTypeface = this.mHoursOnes.getTypeface();
		}
		// Set the lowest time unit with thin font (excluding hundredths)
		if (this.mMinutesTens != null) {
			this.mMinutesTens.setTypeface(this.mAndroidClockMonoThin);
			this.mMinutesTens.updatePadding();
		}
		if (this.mMinutesOnes != null) {
			this.mMinutesOnes.setTypeface(this.mAndroidClockMonoThin);
			this.mMinutesOnes.updatePadding();
		}
	}

	private void restyleViews() {
		if (this.mHoursOnes != null) {
			this.mHoursOnes.setTextColor(this.mTextColor);
		}
		if (this.mMinutesOnes != null) {
			this.mMinutesOnes.setTextColor(this.mTextColor);
		}
		if (this.mHoursTens != null) {
			this.mHoursTens.setTextColor(this.mTextColor);
		}
		if (this.mMinutesTens != null) {
			this.mMinutesTens.setTextColor(this.mTextColor);
		}
		if (this.mHoursSeperator != null) {
			this.mHoursSeperator.setTextColor(this.mTextColor);
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

	/**
	 * Set the time shown
	 *
	 * @param hoursTensDigit the tens digit of the hours
	 * @param hoursOnesDigit the ones digit of the hours
	 * @param minutesTensDigit the tens digit of the minutes
	 * @param minutesOnesDigit the ones digit of the minutes
	 */
	public void setTime(final int hoursTensDigit, final int hoursOnesDigit, final int minutesTensDigit,
			final int minutesOnesDigit) {
		if (this.mHoursTens != null) {
			// Hide digit
			if (hoursTensDigit == -2) {
				this.mHoursTens.setVisibility(View.INVISIBLE);
			} else if (hoursTensDigit == -1) {
				this.mHoursTens.setText("-");
				this.mHoursTens.setTypeface(this.mAndroidClockMonoThin);
				this.mHoursTens.setEnabled(false);
				this.mHoursTens.updatePadding();
				this.mHoursTens.setVisibility(View.VISIBLE);
			} else {
				this.mHoursTens.setText(String.format("%d", hoursTensDigit));
				this.mHoursTens.setTypeface(this.mOriginalHoursTypeface);
				this.mHoursTens.setEnabled(true);
				this.mHoursTens.updatePadding();
				this.mHoursTens.setVisibility(View.VISIBLE);
			}
		}
		if (this.mHoursOnes != null) {
			if (hoursOnesDigit == -1) {
				this.mHoursOnes.setText("-");
				this.mHoursOnes.setTypeface(this.mAndroidClockMonoThin);
				this.mHoursOnes.setEnabled(false);
				this.mHoursOnes.updatePadding();
			} else {
				this.mHoursOnes.setText(String.format("%d", hoursOnesDigit));
				this.mHoursOnes.setTypeface(this.mOriginalHoursTypeface);
				this.mHoursOnes.setEnabled(true);
				this.mHoursOnes.updatePadding();
			}
		}
		if (this.mMinutesTens != null) {
			if (minutesTensDigit == -1) {
				this.mMinutesTens.setText("-");
				this.mMinutesTens.setEnabled(false);
			} else {
				this.mMinutesTens.setEnabled(true);
				this.mMinutesTens.setText(String.format("%d", minutesTensDigit));
			}
		}
		if (this.mMinutesOnes != null) {
			if (minutesOnesDigit == -1) {
				this.mMinutesOnes.setText("-");
				this.mMinutesOnes.setEnabled(false);
			} else {
				this.mMinutesOnes.setText(String.format("%d", minutesOnesDigit));
				this.mMinutesOnes.setEnabled(true);
			}
		}
	}
}