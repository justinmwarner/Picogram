package com.doomonafireball.betterpickers.datepicker;

import com.doomonafireball.betterpickers.widget.PickerLinearLayout;

import com.doomonafireball.betterpickers.widget.UnderlinePageIndicatorPicker;
import com.doomonafireball.betterpickers.widget.ZeroTopPaddingTextView;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;

import com.doomonafireball.betterpickers.R;
public class DateView extends PickerLinearLayout {

	private ZeroTopPaddingTextView mMonth;
	private ZeroTopPaddingTextView mDate;
	private ZeroTopPaddingTextView mYearLabel;
	private final Typeface mAndroidClockMonoThin;
	private final Typeface mOriginalNumberTypeface;
	private UnderlinePageIndicatorPicker mUnderlinePageIndicatorPicker;

	private ColorStateList mTitleColor;

	/**
	 * Instantiate a DateView
	 *
	 * @param context the Context in which to inflate the View
	 */
	public DateView(final Context context) {
		this(context, null);
	}

	/**
	 * Instantiate a DateView
	 *
	 * @param context the Context in which to inflate the View
	 * @param attrs attributes that define the title color
	 */
	public DateView(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		this.mAndroidClockMonoThin =
				Typeface.createFromAsset(context.getAssets(), "fonts/AndroidClockMono-Thin.ttf");
		this.mOriginalNumberTypeface =
				Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");

		// Init defaults
		this.mTitleColor = this.getResources().getColorStateList(R.color.dialog_text_color_holo_dark);

		this.setWillNotDraw(false);
	}

	/**
	 * Get the date TextView
	 *
	 * @return the date TextView
	 */
	public ZeroTopPaddingTextView getDate() {
		return this.mDate;
	}

	/**
	 * Get the month TextView
	 *
	 * @return the month TextView
	 */
	public ZeroTopPaddingTextView getMonth() {
		return this.mMonth;
	}

	@Override
	public View getViewAt(final int index) {
		return this.getChildAt(index);
	}

	/**
	 * Get the year TextView
	 *
	 * @return the year TextView
	 */
	public ZeroTopPaddingTextView getYear() {
		return this.mYearLabel;
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);

		this.mUnderlinePageIndicatorPicker.setTitleView(this);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		this.mMonth = (ZeroTopPaddingTextView) this.findViewById(R.id.month);
		this.mDate = (ZeroTopPaddingTextView) this.findViewById(R.id.date);
		this.mYearLabel = (ZeroTopPaddingTextView) this.findViewById(R.id.year_label);
		// Reorder based on locale
		final char[] dateFormatOrder = DateFormat.getDateFormatOrder(this.getContext());
		this.removeAllViews();
		for (int i = 0; i < dateFormatOrder.length; i++) {
			switch (dateFormatOrder[i]) {
				case DateFormat.DATE:
					this.addView(this.mDate);
					break;
				case DateFormat.MONTH:
					this.addView(this.mMonth);
					break;
				case DateFormat.YEAR:
					this.addView(this.mYearLabel);
					break;
			}
		}

		if (this.mMonth != null) {
			//mOriginalNumberTypeface = mMonth.getTypeface();
		}
		// Set both TextViews with thin font (for hyphen)
		if (this.mDate != null) {
			this.mDate.setTypeface(this.mAndroidClockMonoThin);
			this.mDate.updatePadding();
		}
		if (this.mMonth != null) {
			this.mMonth.setTypeface(this.mAndroidClockMonoThin);
			this.mMonth.updatePadding();
		}

		this.restyleViews();
	}

	private void restyleViews() {
		if (this.mMonth != null) {
			this.mMonth.setTextColor(this.mTitleColor);
		}
		if (this.mDate != null) {
			this.mDate.setTextColor(this.mTitleColor);
		}
		if (this.mYearLabel != null) {
			this.mYearLabel.setTextColor(this.mTitleColor);
		}
	}

	/**
	 * Set the date shown
	 *
	 * @param month a String representing the month of year
	 * @param dayOfMonth an int representing the day of month
	 * @param year an int representing the year
	 */
	public void setDate(final String month, final int dayOfMonth, final int year) {
		if (this.mMonth != null) {
			if (month.equals("")) {
				this.mMonth.setText("-");
				this.mMonth.setTypeface(this.mAndroidClockMonoThin);
				this.mMonth.setEnabled(false);
				this.mMonth.updatePadding();
			} else {
				this.mMonth.setText(month);
				this.mMonth.setTypeface(this.mOriginalNumberTypeface);
				this.mMonth.setEnabled(true);
				this.mMonth.updatePaddingForBoldDate();
			}
		}
		if (this.mDate != null) {
			if (dayOfMonth <= 0) {
				this.mDate.setText("-");
				this.mDate.setEnabled(false);
				this.mDate.updatePadding();
			} else {
				this.mDate.setText(Integer.toString(dayOfMonth));
				this.mDate.setEnabled(true);
				this.mDate.updatePadding();
			}
		}
		if (this.mYearLabel != null) {
			if (year <= 0) {
				this.mYearLabel.setText("----");
				this.mYearLabel.setEnabled(false);
				this.mYearLabel.updatePadding();
			} else {
				String yearString = Integer.toString(year);
				// Pad to 4 digits
				while (yearString.length() < 4) {
					yearString = "-" + yearString;
				}
				this.mYearLabel.setText(yearString);
				this.mYearLabel.setEnabled(true);
				this.mYearLabel.updatePadding();
			}
		}
	}

	/**
	 * Set an onClickListener for notification
	 *
	 * @param mOnClickListener an OnClickListener from the parent
	 */
	public void setOnClick(final OnClickListener mOnClickListener) {
		this.mDate.setOnClickListener(mOnClickListener);
		this.mMonth.setOnClickListener(mOnClickListener);
		this.mYearLabel.setOnClickListener(mOnClickListener);
	}

	/**
	 * Set a theme and restyle the views. This View will change its title color.
	 *
	 * @param themeResId the resource ID for theming
	 */
	public void setTheme(final int themeResId) {
		if (themeResId != -1) {
			final TypedArray a = this.getContext().obtainStyledAttributes(themeResId, R.styleable.BetterPickersDialogFragment);

			this.mTitleColor = a.getColorStateList(R.styleable.BetterPickersDialogFragment_bpTitleColor);
		}

		this.restyleViews();
	}

	/**
	 * Allow attachment of the UnderlinePageIndicator
	 *
	 * @param indicator the indicator to attach
	 */
	public void setUnderlinePage(final UnderlinePageIndicatorPicker indicator) {
		this.mUnderlinePageIndicatorPicker = indicator;
	}
}