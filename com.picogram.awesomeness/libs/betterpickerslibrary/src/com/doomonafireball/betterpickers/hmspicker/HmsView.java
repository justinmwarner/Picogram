package com.doomonafireball.betterpickers.hmspicker;


import com.doomonafireball.betterpickers.widget.ZeroTopPaddingTextView;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.doomonafireball.betterpickers.R;
public class HmsView extends LinearLayout {

	private ZeroTopPaddingTextView mHoursOnes;
	private ZeroTopPaddingTextView mMinutesOnes, mMinutesTens;
	private ZeroTopPaddingTextView mSecondsOnes, mSecondsTens;
	private final Typeface mAndroidClockMonoThin;
	private Typeface mOriginalHoursTypeface;

	private ColorStateList mTextColor;

	/**
	 * Instantiate an HmsView
	 *
	 * @param context the Context in which to inflate the View
	 */
	public HmsView(final Context context) {
		this(context, null);
	}

	/**
	 * Instantiate an HmsView
	 *
	 * @param context the Context in which to inflate the View
	 * @param attrs attributes that define the title color
	 */
	public HmsView(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		this.mAndroidClockMonoThin =
				Typeface.createFromAsset(context.getAssets(), "fonts/AndroidClockMono-Thin.ttf");

		// Init defaults
		this.mTextColor = this.getResources().getColorStateList(R.color.dialog_text_color_holo_dark);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		this.mHoursOnes = (ZeroTopPaddingTextView) this.findViewById(R.id.hours_ones);
		this.mMinutesTens = (ZeroTopPaddingTextView) this.findViewById(R.id.minutes_tens);
		this.mMinutesOnes = (ZeroTopPaddingTextView) this.findViewById(R.id.minutes_ones);
		this.mSecondsTens = (ZeroTopPaddingTextView) this.findViewById(R.id.seconds_tens);
		this.mSecondsOnes = (ZeroTopPaddingTextView) this.findViewById(R.id.seconds_ones);
		if (this.mHoursOnes != null) {
			this.mOriginalHoursTypeface = this.mHoursOnes.getTypeface();
		}
		// Set the lowest time unit with thin font (excluding hundredths)
		if (this.mSecondsTens != null) {
			this.mSecondsTens.setTypeface(this.mAndroidClockMonoThin);
			this.mSecondsTens.updatePadding();
		}
		if (this.mSecondsOnes != null) {
			this.mSecondsOnes.setTypeface(this.mAndroidClockMonoThin);
			this.mSecondsOnes.updatePadding();
		}
	}

	private void restyleViews() {
		if (this.mHoursOnes != null) {
			this.mHoursOnes.setTextColor(this.mTextColor);
		}
		if (this.mMinutesOnes != null) {
			this.mMinutesOnes.setTextColor(this.mTextColor);
		}
		if (this.mMinutesTens != null) {
			this.mMinutesTens.setTextColor(this.mTextColor);
		}
		if (this.mSecondsOnes != null) {
			this.mSecondsOnes.setTextColor(this.mTextColor);
		}
		if (this.mSecondsTens != null) {
			this.mSecondsTens.setTextColor(this.mTextColor);
		}
	}

	/**
	 * Set a theme and restyle the views. This View will change its text color.
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
	 * @param hoursOnesDigit the ones digit of the hours TextView
	 * @param minutesTensDigit the tens digit of the minutes TextView
	 * @param minutesOnesDigit the ones digit of the minutes TextView
	 * @param secondsTensDigit the tens digit of the seconds TextView
	 * @param secondsOnesDigit the ones digit of the seconds TextView
	 */
	public void setTime(final int hoursOnesDigit, final int minutesTensDigit, final int minutesOnesDigit, final int secondsTensDigit,
			final int secondsOnesDigit) {
		if (this.mHoursOnes != null) {
			this.mHoursOnes.setText(String.format("%d", hoursOnesDigit));
		}
		if (this.mMinutesTens != null) {
			this.mMinutesTens.setText(String.format("%d", minutesTensDigit));
		}
		if (this.mMinutesOnes != null) {
			this.mMinutesOnes.setText(String.format("%d", minutesOnesDigit));
		}
		if (this.mSecondsTens != null) {
			this.mSecondsTens.setText(String.format("%d", secondsTensDigit));
		}
		if (this.mSecondsOnes != null) {
			this.mSecondsOnes.setText(String.format("%d", secondsOnesDigit));
		}
	}
}