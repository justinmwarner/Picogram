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

import com.doomonafireball.betterpickers.TouchExplorationHelper;
import com.doomonafireball.betterpickers.Utils;
import com.doomonafireball.betterpickers.calendardatepicker.SimpleMonthAdapter.CalendarDay;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * A calendar-like view displaying a specified month and the appropriate selectable day numbers within the specified
 * month.
 */
public class SimpleMonthView extends View {

	/**
	 * Provides a virtual view hierarchy for interfacing with an accessibility service.
	 */
	private class MonthViewNodeProvider extends TouchExplorationHelper<CalendarDay> {

		private final SparseArray<CalendarDay> mCachedItems = new SparseArray<CalendarDay>();
		private final Rect mTempRect = new Rect();

		Calendar recycle;

		public MonthViewNodeProvider(final Context context, final View parent) {
			super(context, parent);
		}

		@Override
		protected int getIdForItem(final CalendarDay item) {
			return item.day;
		}

		@Override
		protected CalendarDay getItemAt(final float x, final float y) {
			return SimpleMonthView.this.getDayFromLocation(x, y);
		}

		/**
		 * Calculates the bounding rectangle of a given time object.
		 *
		 * @param item The time object to calculate bounds for
		 * @param rect The rectangle in which to store the bounds
		 */
		private void getItemBounds(final CalendarDay item, final Rect rect) {
			final int offsetX = SimpleMonthView.this.mPadding;
			final int offsetY = sMonthHeaderSize;
			final int cellHeight = SimpleMonthView.this.mRowHeight;
			final int cellWidth = ((SimpleMonthView.this.mWidth - (2 * SimpleMonthView.this.mPadding)) / SimpleMonthView.this.mNumDays);
			final int index = ((item.day - 1) + SimpleMonthView.this.findDayOffset());
			final int row = (index / SimpleMonthView.this.mNumDays);
			final int column = (index % SimpleMonthView.this.mNumDays);
			final int x = (offsetX + (column * cellWidth));
			final int y = (offsetY + (row * cellHeight));

			rect.set(x, y, (x + cellWidth), (y + cellHeight));
		}

		/**
		 * Generates a description for a given time object. Since this description will be spoken, the components are
		 * ordered by descending specificity as DAY MONTH YEAR.
		 *
		 * @param item The time object to generate a description for
		 * @return A description of the time object
		 */
		private CharSequence getItemDescription(final CalendarDay item) {
			if (this.recycle == null) {
				this.recycle = Calendar.getInstance();
			}
			this.recycle.set(item.year, item.month, item.day);
			final CharSequence date = DateFormat.format("dd MMMM yyyy", this.recycle.getTimeInMillis());

			if (item.day == SimpleMonthView.this.mSelectedDay) {
				return SimpleMonthView.this.getContext().getString(R.string.item_is_selected, date);
			}

			return date;
		}

		@Override
		protected CalendarDay getItemForId(final int id) {
			if ((id < 1) || (id > SimpleMonthView.this.mNumCells)) {
				return null;
			}

			final CalendarDay item;
			if (this.mCachedItems.indexOfKey(id) >= 0) {
				item = this.mCachedItems.get(id);
			} else {
				item = new CalendarDay(SimpleMonthView.this.mYear, SimpleMonthView.this.mMonth, id);
				this.mCachedItems.put(id, item);
			}

			return item;
		}

		@Override
		protected void getVisibleItems(final List<CalendarDay> items) {
			// TODO: Optimize, only return items visible within parent bounds.
			for (int day = 1; day <= SimpleMonthView.this.mNumCells; day++) {
				items.add(this.getItemForId(day));
			}
		}

		@Override
		public void invalidateItem(final CalendarDay item) {
			super.invalidateItem(item);
			this.mCachedItems.delete(this.getIdForItem(item));
		}

		@Override
		public void invalidateParent() {
			super.invalidateParent();
			this.mCachedItems.clear();
		}

		@Override
		protected boolean performActionForItem(final CalendarDay item, final int action, final Bundle arguments) {
			switch (action) {
				case AccessibilityNodeInfoCompat.ACTION_CLICK:
					SimpleMonthView.this.onDayClick(item);
					return true;
			}

			return false;
		}

		@Override
		protected void populateEventForItem(final CalendarDay item, final AccessibilityEvent event) {
			event.setContentDescription(this.getItemDescription(item));
		}

		@Override
		protected void populateNodeForItem(final CalendarDay item, final AccessibilityNodeInfoCompat node) {
			this.getItemBounds(item, this.mTempRect);

			node.setContentDescription(this.getItemDescription(item));
			node.setBoundsInParent(this.mTempRect);
			node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK);

			if (item.day == SimpleMonthView.this.mSelectedDay) {
				node.setSelected(true);
			}
		}
	}

	/**
	 * Handles callbacks when the user clicks on a time object.
	 */
	public interface OnDayClickListener {

		public void onDayClick(SimpleMonthView view, CalendarDay day);
	}
	private static final String TAG = "SimpleMonthView";
	/**
	 * These params can be passed into the view to control how it appears.
	 * {@link #VIEW_PARAMS_WEEK} is the only required field, though the default
	 * values are unlikely to fit most layouts correctly.
	 */
	/**
	 * This sets the height of this week in pixels
	 */
	public static final String VIEW_PARAMS_HEIGHT = "height";
	/**
	 * This specifies the position (or weeks since the epoch) of this week, calculated using {@link
	 * Utils#getWeeksSinceEpochFromJulianDay}
	 */
	public static final String VIEW_PARAMS_MONTH = "month";
	/**
	 * This specifies the position (or weeks since the epoch) of this week, calculated using {@link
	 * Utils#getWeeksSinceEpochFromJulianDay}
	 */
	public static final String VIEW_PARAMS_YEAR = "year";
	/**
	 * This sets one of the days in this view as selected {@link android.text.format.Time#SUNDAY} through {@link
	 * android.text.format.Time#SATURDAY}.
	 */
	public static final String VIEW_PARAMS_SELECTED_DAY = "selected_day";
	/**
	 * Which day the week should start on. {@link android.text.format.Time#SUNDAY} through {@link
	 * android.text.format.Time#SATURDAY}.
	 */
	public static final String VIEW_PARAMS_WEEK_START = "week_start";
	/**
	 * How many days to display at a time. Days will be displayed starting with {@link #mWeekStart}.
	 */
	public static final String VIEW_PARAMS_NUM_DAYS = "num_days";

	/**
	 * Which month is currently in focus, as defined by {@link android.text.format.Time#month} [0-11].
	 */
	public static final String VIEW_PARAMS_FOCUS_MONTH = "focus_month";
	/**
	 * If this month should display week numbers. false if 0, true otherwise.
	 */
	public static final String VIEW_PARAMS_SHOW_WK_NUM = "show_wk_num";
	protected static final int DEFAULT_HEIGHT = 32;
	protected static final int MIN_HEIGHT = 10;
	protected static final int DEFAULT_SELECTED_DAY = -1;
	protected static final int DEFAULT_WEEK_START = Calendar.SUNDAY;
	protected static final int DEFAULT_NUM_DAYS = 7;
	protected static final int DEFAULT_SHOW_WK_NUM = 0;
	protected static final int DEFAULT_FOCUS_MONTH = -1;

	protected static final int DEFAULT_NUM_ROWS = 6;

	protected static final int MAX_NUM_ROWS = 6;
	private static final int SELECTED_CIRCLE_ALPHA = 60;
	protected static final int DAY_SEPARATOR_WIDTH = 1;
	protected static int sMiniDayNumberTextSize;
	protected static int sMonthLabelTextSize;
	protected static int sMonthDayLabelTextSize;

	protected static int sMonthHeaderSize;

	protected static int sDaySelectedCircleSize;

	// used for scaling to the device density
	protected static float mScale = 0;
	// affects the padding on the sides of this view
	protected int mPadding = 0;

	private final String mDayOfWeekTypeface;
	private final String mMonthTitleTypeface;
	protected Paint mMonthNumPaint;
	protected Paint mMonthTitlePaint;
	protected Paint mMonthTitleBGPaint;

	protected Paint mSelectedCirclePaint;
	protected Paint mMonthDayLabelPaint;

	private final Formatter mFormatter;
	private final StringBuilder mStringBuilder;
	// The Julian day of the first day displayed by this item
	protected int mFirstJulianDay = -1;

	// The month of the first day in this week
	protected int mFirstMonth = -1;

	// The month of the last day in this week
	protected int mLastMonth = -1;
	protected int mMonth;
	protected int mYear;
	// Quick reference to the width of this view, matches parent
	protected int mWidth;
	// The height this view should draw at in pixels, set by height param
	protected int mRowHeight = DEFAULT_HEIGHT;
	// If this view contains the today
	protected boolean mHasToday = false;
	// Which day is selected [0-6] or -1 if no day is selected
	protected int mSelectedDay = -1;
	// Which day is today [0-6] or -1 if no day is today
	protected int mToday = DEFAULT_SELECTED_DAY;
	// Which day of the week to start on [0-6]
	protected int mWeekStart = DEFAULT_WEEK_START;
	// How many days to display
	protected int mNumDays = DEFAULT_NUM_DAYS;
	// The number of days + a spot for week number if it is displayed
	protected int mNumCells = this.mNumDays;

	// The left edge of the selected day
	protected int mSelectedLeft = -1;
	// The right edge of the selected day
	protected int mSelectedRight = -1;
	private final Calendar mCalendar;

	private final Calendar mDayLabelCalendar;

	private final MonthViewNodeProvider mNodeProvider;
	private int mNumRows = DEFAULT_NUM_ROWS;

	// Optional listener for handling day click actions
	private OnDayClickListener mOnDayClickListener;
	// Whether to prevent setting the accessibility delegate
	private final boolean mLockAccessibilityDelegate;
	protected int mDayTextColor;
	protected int mTodayNumberColor;

	protected int mMonthTitleColor;

	protected int mMonthTitleBGColor;

	private int mDayOfWeekStart = 0;

	/* Removed for backwards compatibility with Gingerbread
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        // First right-of-refusal goes the touch exploration helper.
        if (mNodeProvider.onHover(this, event)) {
            return true;
        }
        return super.onHoverEvent(event);
    }*/

	public SimpleMonthView(final Context context) {
		super(context);

		final Resources res = context.getResources();

		this.mDayLabelCalendar = Calendar.getInstance();
		this.mCalendar = Calendar.getInstance();

		this.mDayOfWeekTypeface = res.getString(R.string.day_of_week_label_typeface);
		this.mMonthTitleTypeface = res.getString(R.string.sans_serif);

		this.mDayTextColor = res.getColor(R.color.date_picker_text_normal);
		this.mTodayNumberColor = res.getColor(R.color.blue);
		this.mMonthTitleColor = res.getColor(R.color.white);
		this.mMonthTitleBGColor = res.getColor(R.color.circle_background);

		this.mStringBuilder = new StringBuilder(50);
		this.mFormatter = new Formatter(this.mStringBuilder, Locale.getDefault());

		sMiniDayNumberTextSize = res.getDimensionPixelSize(R.dimen.day_number_size);
		sMonthLabelTextSize = res.getDimensionPixelSize(R.dimen.month_label_size);
		sMonthDayLabelTextSize = res.getDimensionPixelSize(R.dimen.month_day_label_text_size);
		sMonthHeaderSize = res.getDimensionPixelOffset(R.dimen.month_list_item_header_height);
		sDaySelectedCircleSize = res
				.getDimensionPixelSize(R.dimen.day_number_select_circle_radius);

		this.mRowHeight = (res.getDimensionPixelOffset(R.dimen.date_picker_view_animator_height)
				- sMonthHeaderSize) / MAX_NUM_ROWS;

		// Set up accessibility components.
		this.mNodeProvider = new MonthViewNodeProvider(context, this);
		ViewCompat.setAccessibilityDelegate(this, this.mNodeProvider.getAccessibilityDelegate());
		ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
		this.mLockAccessibilityDelegate = true;

		// Sets up any standard paints that will be used
		this.initView();
	}

	private int calculateNumRows() {
		final int offset = this.findDayOffset();
		final int dividend = (offset + this.mNumCells) / this.mNumDays;
		final int remainder = (offset + this.mNumCells) % this.mNumDays;
		return (dividend + (remainder > 0 ? 1 : 0));
	}

	/**
	 * Clears accessibility focus within the view. No-op if the view does not contain accessibility focus.
	 */
	public void clearAccessibilityFocus() {
		this.mNodeProvider.clearFocusedItem();
	}

	private void drawMonthDayLabels(final Canvas canvas) {
		final int y = sMonthHeaderSize - (sMonthDayLabelTextSize / 2);
		final int dayWidthHalf = (this.mWidth - (this.mPadding * 2)) / (this.mNumDays * 2);

		for (int i = 0; i < this.mNumDays; i++) {
			final int calendarDay = (i + this.mWeekStart) % this.mNumDays;
			final int x = (((2 * i) + 1) * dayWidthHalf) + this.mPadding;
			this.mDayLabelCalendar.set(Calendar.DAY_OF_WEEK, calendarDay);
			canvas.drawText(this.mDayLabelCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT,
					Locale.getDefault()).toUpperCase(Locale.getDefault()), x, y,
					this.mMonthDayLabelPaint);
		}
	}

	/**
	 * Draws the week and month day numbers for this week. Override this method if you need different placement.
	 *
	 * @param canvas The canvas to draw on
	 */
	protected void drawMonthNums(final Canvas canvas) {
		int y = (((this.mRowHeight + sMiniDayNumberTextSize) / 2) - DAY_SEPARATOR_WIDTH)
				+ sMonthHeaderSize;
		final int dayWidthHalf = (this.mWidth - (this.mPadding * 2)) / (this.mNumDays * 2);
		int j = this.findDayOffset();
		for (int dayNumber = 1; dayNumber <= this.mNumCells; dayNumber++) {
			final int x = (((2 * j) + 1) * dayWidthHalf) + this.mPadding;
			if (this.mSelectedDay == dayNumber) {
				canvas.drawCircle(x, y - (sMiniDayNumberTextSize / 3), sDaySelectedCircleSize,
						this.mSelectedCirclePaint);
			}

			if (this.mHasToday && (this.mToday == dayNumber)) {
				this.mMonthNumPaint.setColor(this.mTodayNumberColor);
			} else {
				this.mMonthNumPaint.setColor(this.mDayTextColor);
			}
			canvas.drawText(String.format("%d", dayNumber), x, y, this.mMonthNumPaint);
			j++;
			if (j == this.mNumDays) {
				j = 0;
				y += this.mRowHeight;
			}
		}
	}

	private void drawMonthTitle(final Canvas canvas) {
		final int x = (this.mWidth + (2 * this.mPadding)) / 2;
		final int y = ((sMonthHeaderSize - sMonthDayLabelTextSize) / 2) + (sMonthLabelTextSize / 3);
		canvas.drawText(this.getMonthAndYearString(), x, y, this.mMonthTitlePaint);
	}

	private int findDayOffset() {
		return (this.mDayOfWeekStart < this.mWeekStart ? (this.mDayOfWeekStart + this.mNumDays) : this.mDayOfWeekStart)
				- this.mWeekStart;
	}

	/**
	 * @return The date that has accessibility focus, or {@code null} if no date has focus
	 */
	public CalendarDay getAccessibilityFocus() {
		return this.mNodeProvider.getFocusedItem();
	}

	/**
	 * Calculates the day that the given x position is in, accounting for week number. Returns a Time referencing that
	 * day or null if
	 *
	 * @param x The x position of the touch event
	 * @return A time object for the tapped day or null if the position wasn't in a day
	 */
	public CalendarDay getDayFromLocation(final float x, final float y) {
		final int dayStart = this.mPadding;
		if ((x < dayStart) || (x > (this.mWidth - this.mPadding))) {
			return null;
		}
		// Selection is (x - start) / (pixels/day) == (x -s) * day / pixels
		final int row = (int) (y - sMonthHeaderSize) / this.mRowHeight;
		final int column = (int) (((x - dayStart) * this.mNumDays) / (this.mWidth - dayStart - this.mPadding));

		int day = (column - this.findDayOffset()) + 1;
		day += row * this.mNumDays;
		if ((day < 1) || (day > this.mNumCells)) {
			return null;
		}
		return new CalendarDay(this.mYear, this.mMonth, day);
	}

	private String getMonthAndYearString() {
		final int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
				| DateUtils.FORMAT_NO_MONTH_DAY;
		this.mStringBuilder.setLength(0);
		final long millis = this.mCalendar.getTimeInMillis();
		return DateUtils.formatDateRange(this.getContext(), this.mFormatter, millis, millis, flags,
				Time.getCurrentTimezone()).toString();
	}

	/**
	 * Sets up the text and style properties for painting. Override this if you want to use a different paint.
	 */
	protected void initView() {
		this.mMonthTitlePaint = new Paint();
		this.mMonthTitlePaint.setFakeBoldText(true);
		this.mMonthTitlePaint.setAntiAlias(true);
		this.mMonthTitlePaint.setTextSize(sMonthLabelTextSize);
		this.mMonthTitlePaint.setTypeface(Typeface.create(this.mMonthTitleTypeface, Typeface.BOLD));
		this.mMonthTitlePaint.setColor(this.mDayTextColor);
		this.mMonthTitlePaint.setTextAlign(Align.CENTER);
		this.mMonthTitlePaint.setStyle(Style.FILL);

		this.mMonthTitleBGPaint = new Paint();
		this.mMonthTitleBGPaint.setFakeBoldText(true);
		this.mMonthTitleBGPaint.setAntiAlias(true);
		this.mMonthTitleBGPaint.setColor(this.mMonthTitleBGColor);
		this.mMonthTitleBGPaint.setTextAlign(Align.CENTER);
		this.mMonthTitleBGPaint.setStyle(Style.FILL);

		this.mSelectedCirclePaint = new Paint();
		this.mSelectedCirclePaint.setFakeBoldText(true);
		this.mSelectedCirclePaint.setAntiAlias(true);
		this.mSelectedCirclePaint.setColor(this.mTodayNumberColor);
		this.mSelectedCirclePaint.setTextAlign(Align.CENTER);
		this.mSelectedCirclePaint.setStyle(Style.FILL);
		this.mSelectedCirclePaint.setAlpha(SELECTED_CIRCLE_ALPHA);

		this.mMonthDayLabelPaint = new Paint();
		this.mMonthDayLabelPaint.setAntiAlias(true);
		this.mMonthDayLabelPaint.setTextSize(sMonthDayLabelTextSize);
		this.mMonthDayLabelPaint.setColor(this.mDayTextColor);
		this.mMonthDayLabelPaint.setTypeface(Typeface.create(this.mDayOfWeekTypeface, Typeface.NORMAL));
		this.mMonthDayLabelPaint.setStyle(Style.FILL);
		this.mMonthDayLabelPaint.setTextAlign(Align.CENTER);
		this.mMonthDayLabelPaint.setFakeBoldText(true);

		this.mMonthNumPaint = new Paint();
		this.mMonthNumPaint.setAntiAlias(true);
		this.mMonthNumPaint.setTextSize(sMiniDayNumberTextSize);
		this.mMonthNumPaint.setStyle(Style.FILL);
		this.mMonthNumPaint.setTextAlign(Align.CENTER);
		this.mMonthNumPaint.setFakeBoldText(false);
	}

	/**
	 * Called when the user clicks on a day. Handles callbacks to the {@link OnDayClickListener} if one is set.
	 *
	 * @param day A time object representing the day that was clicked
	 */
	private void onDayClick(final CalendarDay day) {
		if (this.mOnDayClickListener != null) {
			this.mOnDayClickListener.onDayClick(this, day);
		}

		// This is a no-op if accessibility is turned off.
		this.mNodeProvider.sendEventForItem(day, AccessibilityEvent.TYPE_VIEW_CLICKED);
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		this.drawMonthTitle(canvas);
		this.drawMonthDayLabels(canvas);
		this.drawMonthNums(canvas);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		this.setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (this.mRowHeight * this.mNumRows)
				+ sMonthHeaderSize);
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		this.mWidth = w;

		// Invalidate cached accessibility information.
		this.mNodeProvider.invalidateParent();
	}


	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				final CalendarDay day = this.getDayFromLocation(event.getX(), event.getY());
				if (day != null) {
					this.onDayClick(day);
				}
				break;
		}
		return true;
	}

	/**
	 * Attempts to restore accessibility focus to the specified date.
	 *
	 * @param day The date which should receive focus
	 * @return {@code false} if the date is not valid for this month view, or {@code true} if the date received focus
	 */
	public boolean restoreAccessibilityFocus(final CalendarDay day) {
		if ((day.year != this.mYear) || (day.month != this.mMonth) || (day.day > this.mNumCells)) {
			return false;
		}

		this.mNodeProvider.setFocusedItem(day);
		return true;
	}

	public void reuse() {
		this.mNumRows = DEFAULT_NUM_ROWS;
		this.requestLayout();
	}

	private boolean sameDay(final int day, final Time today) {
		return (this.mYear == today.year) &&
				(this.mMonth == today.month) &&
				(day == today.monthDay);
	}

	@Override
	public void setAccessibilityDelegate(final AccessibilityDelegate delegate) {
		// Workaround for a JB MR1 issue where accessibility delegates on
		// top-level ListView items are overwritten.
		if (!this.mLockAccessibilityDelegate) {
			super.setAccessibilityDelegate(delegate);
		}
	}

	/**
	 * Sets all the parameters for displaying this week. The only required parameter is the week number. Other
	 * parameters have a default value and will only update if a new value is included, except for focus month, which
	 * will always default to no focus month if no value is passed in. See {@link #VIEW_PARAMS_HEIGHT} for more info on
	 * parameters.
	 *
	 * @param params A map of the new parameters, see {@link #VIEW_PARAMS_HEIGHT}
	 * @param tz The time zone this view should reference times in
	 */
	public void setMonthParams(final HashMap<String, Integer> params) {
		if (!params.containsKey(VIEW_PARAMS_MONTH) && !params.containsKey(VIEW_PARAMS_YEAR)) {
			throw new InvalidParameterException("You must specify the month and year for this view");
		}
		this.setTag(params);
		// We keep the current value for any params not present
		if (params.containsKey(VIEW_PARAMS_HEIGHT)) {
			this.mRowHeight = params.get(VIEW_PARAMS_HEIGHT);
			if (this.mRowHeight < MIN_HEIGHT) {
				this.mRowHeight = MIN_HEIGHT;
			}
		}
		if (params.containsKey(VIEW_PARAMS_SELECTED_DAY)) {
			this.mSelectedDay = params.get(VIEW_PARAMS_SELECTED_DAY);
		}

		// Allocate space for caching the day numbers and focus values
		this.mMonth = params.get(VIEW_PARAMS_MONTH);
		this.mYear = params.get(VIEW_PARAMS_YEAR);

		// Figure out what day today is
		final Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		this.mHasToday = false;
		this.mToday = -1;

		this.mCalendar.set(Calendar.MONTH, this.mMonth);
		this.mCalendar.set(Calendar.YEAR, this.mYear);
		this.mCalendar.set(Calendar.DAY_OF_MONTH, 1);
		this.mDayOfWeekStart = this.mCalendar.get(Calendar.DAY_OF_WEEK);

		if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
			this.mWeekStart = params.get(VIEW_PARAMS_WEEK_START);
		} else {
			this.mWeekStart = this.mCalendar.getFirstDayOfWeek();
		}

		this.mNumCells = Utils.getDaysInMonth(this.mMonth, this.mYear);
		for (int i = 0; i < this.mNumCells; i++) {
			final int day = i + 1;
			if (this.sameDay(day, today)) {
				this.mHasToday = true;
				this.mToday = day;
			}
		}
		this.mNumRows = this.calculateNumRows();

		// Invalidate cached accessibility information.
		this.mNodeProvider.invalidateParent();
	}

	public void setOnDayClickListener(final OnDayClickListener listener) {
		this.mOnDayClickListener = listener;
	}
}
