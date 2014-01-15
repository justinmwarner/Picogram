package com.doomonafireball.betterpickers.expirationpicker;

import com.doomonafireball.betterpickers.widget.PickerLinearLayout;

import com.doomonafireball.betterpickers.widget.UnderlinePageIndicatorPicker;
import com.doomonafireball.betterpickers.widget.ZeroTopPaddingTextView;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.doomonafireball.betterpickers.R;
public class ExpirationView extends PickerLinearLayout {

	private ZeroTopPaddingTextView mMonth;
	private ZeroTopPaddingTextView mYearLabel;
	private final Typeface mAndroidClockMonoThin;
	private final Typeface mOriginalNumberTypeface;
	private UnderlinePageIndicatorPicker mUnderlinePageIndicatorPicker;

	private ZeroTopPaddingTextView mSeperator;
	private ColorStateList mTitleColor;

	/**
	 * Instantiate an ExpirationView
	 *
	 * @param context the Context in which to inflate the View
	 */
	public ExpirationView(final Context context) {
		this(context, null);
	}

	/**
	 * Instantiate an ExpirationView
	 *
	 * @param context the Context in which to inflate the View
	 * @param attrs attributes that define the title color
	 */
	public ExpirationView(final Context context, final AttributeSet attrs) {
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
	 * Get the month TextView
	 *
	 * @return the month TextView
	 */
	public ZeroTopPaddingTextView getMonth() {
		return this.mMonth;
	}

	@Override
	public View getViewAt(final int index) {
		final int actualIndex[] = {0, 2};

		if (index > actualIndex.length) {
			return null;
		} else {
			return this.getChildAt(actualIndex[index]);
		}
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
		this.mYearLabel = (ZeroTopPaddingTextView) this.findViewById(R.id.year_label);
		this.mSeperator = (ZeroTopPaddingTextView) this.findViewById(R.id.expiration_seperator);

		// Set both TextViews with thin font (for hyphen)
		if (this.mMonth != null) {
			this.mMonth.setTypeface(this.mAndroidClockMonoThin);
			this.mMonth.updatePadding();
		}
		if (this.mYearLabel != null) {
			this.mYearLabel.setTypeface(this.mAndroidClockMonoThin);
		}
		if (this.mSeperator != null) {
			this.mSeperator.setTypeface(this.mAndroidClockMonoThin);
		}

		this.restyleViews();
	}

	private void restyleViews() {
		if (this.mMonth != null) {
			this.mMonth.setTextColor(this.mTitleColor);
		}
		if (this.mYearLabel != null) {
			this.mYearLabel.setTextColor(this.mTitleColor);
		}
		if (this.mSeperator != null) {
			this.mSeperator.setTextColor(this.mTitleColor);
		}
	}

	/**
	 * Set the date shown
	 *
	 * @param month a String representing the month of year
	 * @param year an int representing the year
	 */
	public void setExpiration(final String month, final int year) {
		if (this.mMonth != null) {
			if (month.equals("")) {
				this.mMonth.setText("--");
				this.mMonth.setEnabled(false);
				this.mMonth.updatePadding();
			} else {
				this.mMonth.setText(month);
				this.mMonth.setEnabled(true);
				this.mMonth.updatePadding();
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
					yearString = yearString + "-";
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