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
import com.doomonafireball.betterpickers.Utils;
import com.nineoldandroids.animation.ObjectAnimator;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

/**
 * Dialog allowing users to select a date.
 */
public class CalendarDatePickerDialog extends DialogFragment implements
OnClickListener, CalendarDatePickerController {

	/**
	 * The callback used to notify other date picker components of a change in selected date.
	 */
	interface OnDateChangedListener {

		public void onDateChanged();
	}

	/**
	 * The callback used to indicate the user is done filling in the date.
	 */
	public interface OnDateSetListener {

		/**
		 * @param DatePickerDialog The view associated with this listener.
		 * @param year The year that was set.
		 * @param monthOfYear The month that was set (0-11) for compatibility with {@link java.util.Calendar}.
		 * @param dayOfMonth The day of the month that was set.
		 */
		void onDateSet(CalendarDatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth);
	}
	private static final String TAG = "DatePickerDialog";
	private static final int UNINITIALIZED = -1;

	private static final int MONTH_AND_DAY_VIEW = 0;
	private static final int YEAR_VIEW = 1;
	private static final String KEY_SELECTED_YEAR = "year";
	private static final String KEY_SELECTED_MONTH = "month";
	private static final String KEY_SELECTED_DAY = "day";
	private static final String KEY_LIST_POSITION = "list_position";
	private static final String KEY_WEEK_START = "week_start";
	private static final String KEY_YEAR_START = "year_start";
	private static final String KEY_YEAR_END = "year_end";

	private static final String KEY_CURRENT_VIEW = "current_view";
	private static final String KEY_LIST_POSITION_OFFSET = "list_position_offset";

	private static final int DEFAULT_START_YEAR = 1900;
	private static final int DEFAULT_END_YEAR = 2100;

	private static final int ANIMATION_DURATION = 300;
	private static final int ANIMATION_DELAY = 500;

	private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy", Locale.getDefault());
	private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("dd", Locale.getDefault());
	/**
	 * @param callBack How the parent is notified that the date is set.
	 * @param year The initial year of the dialog.
	 * @param monthOfYear The initial month of the dialog.
	 * @param dayOfMonth The initial day of the dialog.
	 */
	public static CalendarDatePickerDialog newInstance(final OnDateSetListener callBack, final int year,
			final int monthOfYear,
			final int dayOfMonth) {
		final CalendarDatePickerDialog ret = new CalendarDatePickerDialog();
		ret.initialize(callBack, year, monthOfYear, dayOfMonth);
		return ret;
	}

	private final Calendar mCalendar = Calendar.getInstance();

	private OnDateSetListener mCallBack;
	private final HashSet<OnDateChangedListener> mListeners = new HashSet<OnDateChangedListener>();
	private AccessibleDateAnimator mAnimator;
	private TextView mDayOfWeekView;
	private LinearLayout mMonthAndDayView;
	private TextView mSelectedMonthTextView;
	private TextView mSelectedDayTextView;
	private TextView mYearView;

	private DayPickerView mDayPickerView;

	private YearPickerView mYearPickerView;
	private Button mDoneButton;
	private int mCurrentView = UNINITIALIZED;

	private int mWeekStart = this.mCalendar.getFirstDayOfWeek();
	private int mMinYear = DEFAULT_START_YEAR;

	private int mMaxYear = DEFAULT_END_YEAR;

	private Vibrator mVibrator;
	private long mLastVibrate;
	private boolean mDelayAnimation = true;
	// Accessibility strings.
	private String mDayPickerDescription;

	private String mSelectDay;

	private String mYearPickerDescription;


	private String mSelectYear;

	public CalendarDatePickerDialog() {
		// Empty constructor required for dialog fragment.
	}

	// If the newly selected month / year does not contain the currently selected day number,
	// change the selected day number to the last day of the selected month or year.
	//      e.g. Switching from Mar to Apr when Mar 31 is selected -> Apr 30
	//      e.g. Switching from 2012 to 2013 when Feb 29, 2012 is selected -> Feb 28, 2013
	private void adjustDayInMonthIfNeeded(final int month, final int year) {
		final int day = this.mCalendar.get(Calendar.DAY_OF_MONTH);
		final int daysInMonth = Utils.getDaysInMonth(month, year);
		if (day > daysInMonth) {
			this.mCalendar.set(Calendar.DAY_OF_MONTH, daysInMonth);
		}
	}

	@Override
	public int getFirstDayOfWeek() {
		return this.mWeekStart;
	}

	@Override
	public int getMaxYear() {
		return this.mMaxYear;
	}

	@Override
	public int getMinYear() {
		return this.mMinYear;
	}

	@Override
	public SimpleMonthAdapter.CalendarDay getSelectedDay() {
		return new SimpleMonthAdapter.CalendarDay(this.mCalendar);
	}

	public void initialize(final OnDateSetListener callBack, final int year, final int monthOfYear, final int dayOfMonth) {
		this.mCallBack = callBack;
		this.mCalendar.set(Calendar.YEAR, year);
		this.mCalendar.set(Calendar.MONTH, monthOfYear);
		this.mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
	}

	@Override
	public void onClick(final View v) {
		this.tryVibrate();
		if (v.getId() == R.id.date_picker_year) {
			this.setCurrentView(YEAR_VIEW);
		} else if (v.getId() == R.id.date_picker_month_and_day) {
			this.setCurrentView(MONTH_AND_DAY_VIEW);
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Activity activity = this.getActivity();
		activity.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		this.mVibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
		if (savedInstanceState != null) {
			this.mCalendar.set(Calendar.YEAR, savedInstanceState.getInt(KEY_SELECTED_YEAR));
			this.mCalendar.set(Calendar.MONTH, savedInstanceState.getInt(KEY_SELECTED_MONTH));
			this.mCalendar.set(Calendar.DAY_OF_MONTH, savedInstanceState.getInt(KEY_SELECTED_DAY));
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: ");
		this.getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		final View view = inflater.inflate(R.layout.calendar_date_picker_dialog, null);

		this.mDayOfWeekView = (TextView) view.findViewById(R.id.date_picker_header);
		this.mMonthAndDayView = (LinearLayout) view.findViewById(R.id.date_picker_month_and_day);
		this.mMonthAndDayView.setOnClickListener(this);
		this.mSelectedMonthTextView = (TextView) view.findViewById(R.id.date_picker_month);
		this.mSelectedDayTextView = (TextView) view.findViewById(R.id.date_picker_day);
		this.mYearView = (TextView) view.findViewById(R.id.date_picker_year);
		this.mYearView.setOnClickListener(this);

		int listPosition = -1;
		int listPositionOffset = 0;
		int currentView = MONTH_AND_DAY_VIEW;
		if (savedInstanceState != null) {
			this.mWeekStart = savedInstanceState.getInt(KEY_WEEK_START);
			this.mMinYear = savedInstanceState.getInt(KEY_YEAR_START);
			this.mMaxYear = savedInstanceState.getInt(KEY_YEAR_END);
			currentView = savedInstanceState.getInt(KEY_CURRENT_VIEW);
			listPosition = savedInstanceState.getInt(KEY_LIST_POSITION);
			listPositionOffset = savedInstanceState.getInt(KEY_LIST_POSITION_OFFSET);
		}

		final Activity activity = this.getActivity();
		this.mDayPickerView = new DayPickerView(activity, this);
		this.mYearPickerView = new YearPickerView(activity, this);

		final Resources res = this.getResources();
		this.mDayPickerDescription = res.getString(R.string.day_picker_description);
		this.mSelectDay = res.getString(R.string.select_day);
		this.mYearPickerDescription = res.getString(R.string.year_picker_description);
		this.mSelectYear = res.getString(R.string.select_year);

		this.mAnimator = (AccessibleDateAnimator) view.findViewById(R.id.animator);
		this.mAnimator.addView(this.mDayPickerView);
		this.mAnimator.addView(this.mYearPickerView);
		this.mAnimator.setDateMillis(this.mCalendar.getTimeInMillis());
		// TODO: Replace with animation decided upon by the design team.
		final Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(ANIMATION_DURATION);
		this.mAnimator.setInAnimation(animation);
		// TODO: Replace with animation decided upon by the design team.
		final Animation animation2 = new AlphaAnimation(1.0f, 0.0f);
		animation2.setDuration(ANIMATION_DURATION);
		this.mAnimator.setOutAnimation(animation2);

		this.mDoneButton = (Button) view.findViewById(R.id.done);
		this.mDoneButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				CalendarDatePickerDialog.this.tryVibrate();
				if (CalendarDatePickerDialog.this.mCallBack != null) {
					CalendarDatePickerDialog.this.mCallBack.onDateSet(CalendarDatePickerDialog.this, CalendarDatePickerDialog.this.mCalendar.get(Calendar.YEAR),
							CalendarDatePickerDialog.this.mCalendar.get(Calendar.MONTH), CalendarDatePickerDialog.this.mCalendar.get(Calendar.DAY_OF_MONTH));
				}
				CalendarDatePickerDialog.this.dismiss();
			}
		});

		this.updateDisplay(false);
		this.setCurrentView(currentView);

		if (listPosition != -1) {
			if (currentView == MONTH_AND_DAY_VIEW) {
				this.mDayPickerView.postSetSelection(listPosition);
			} else if (currentView == YEAR_VIEW) {
				this.mYearPickerView.postSetSelectionFromTop(listPosition, listPositionOffset);
			}
		}
		return view;
	}

	@Override
	public void onDayOfMonthSelected(final int year, final int month, final int day) {
		this.mCalendar.set(Calendar.YEAR, year);
		this.mCalendar.set(Calendar.MONTH, month);
		this.mCalendar.set(Calendar.DAY_OF_MONTH, day);
		this.updatePickers();
		this.updateDisplay(true);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_SELECTED_YEAR, this.mCalendar.get(Calendar.YEAR));
		outState.putInt(KEY_SELECTED_MONTH, this.mCalendar.get(Calendar.MONTH));
		outState.putInt(KEY_SELECTED_DAY, this.mCalendar.get(Calendar.DAY_OF_MONTH));
		outState.putInt(KEY_WEEK_START, this.mWeekStart);
		outState.putInt(KEY_YEAR_START, this.mMinYear);
		outState.putInt(KEY_YEAR_END, this.mMaxYear);
		outState.putInt(KEY_CURRENT_VIEW, this.mCurrentView);
		int listPosition = -1;
		if (this.mCurrentView == MONTH_AND_DAY_VIEW) {
			listPosition = this.mDayPickerView.getMostVisiblePosition();
		} else if (this.mCurrentView == YEAR_VIEW) {
			listPosition = this.mYearPickerView.getFirstVisiblePosition();
			outState.putInt(KEY_LIST_POSITION_OFFSET, this.mYearPickerView.getFirstPositionOffset());
		}
		outState.putInt(KEY_LIST_POSITION, listPosition);
	}

	@Override
	public void onYearSelected(final int year) {
		this.adjustDayInMonthIfNeeded(this.mCalendar.get(Calendar.MONTH), year);
		this.mCalendar.set(Calendar.YEAR, year);
		this.updatePickers();
		this.setCurrentView(MONTH_AND_DAY_VIEW);
		this.updateDisplay(true);
	}

	@Override
	public void registerOnDateChangedListener(final OnDateChangedListener listener) {
		this.mListeners.add(listener);
	}

	private void setCurrentView(final int viewIndex) {
		final long millis = this.mCalendar.getTimeInMillis();

		switch (viewIndex) {
			case MONTH_AND_DAY_VIEW:
				ObjectAnimator pulseAnimator = Utils.getPulseAnimator(this.mMonthAndDayView, 0.9f,
						1.05f);
				if (this.mDelayAnimation) {
					pulseAnimator.setStartDelay(ANIMATION_DELAY);
					this.mDelayAnimation = false;
				}
				this.mDayPickerView.onDateChanged();
				if (this.mCurrentView != viewIndex) {
					this.mMonthAndDayView.setSelected(true);
					this.mYearView.setSelected(false);
					this.mAnimator.setDisplayedChild(MONTH_AND_DAY_VIEW);
					this.mCurrentView = viewIndex;
				}
				pulseAnimator.start();

				final int flags = DateUtils.FORMAT_SHOW_DATE;
				final String dayString = DateUtils.formatDateTime(this.getActivity(), millis, flags);
				this.mAnimator.setContentDescription(this.mDayPickerDescription + ": " + dayString);
				Utils.tryAccessibilityAnnounce(this.mAnimator, this.mSelectDay);
				break;
			case YEAR_VIEW:
				pulseAnimator = Utils.getPulseAnimator(this.mYearView, 0.85f, 1.1f);
				if (this.mDelayAnimation) {
					pulseAnimator.setStartDelay(ANIMATION_DELAY);
					this.mDelayAnimation = false;
				}
				this.mYearPickerView.onDateChanged();
				if (this.mCurrentView != viewIndex) {
					this.mMonthAndDayView.setSelected(false);
					this.mYearView.setSelected(true);
					this.mAnimator.setDisplayedChild(YEAR_VIEW);
					this.mCurrentView = viewIndex;
				}
				pulseAnimator.start();

				final CharSequence yearString = YEAR_FORMAT.format(millis);
				this.mAnimator.setContentDescription(this.mYearPickerDescription + ": " + yearString);
				Utils.tryAccessibilityAnnounce(this.mAnimator, this.mSelectYear);
				break;
		}
	}


	public void setFirstDayOfWeek(final int startOfWeek) {
		if ((startOfWeek < Calendar.SUNDAY) || (startOfWeek > Calendar.SATURDAY)) {
			throw new IllegalArgumentException("Value must be between Calendar.SUNDAY and " +
					"Calendar.SATURDAY");
		}
		this.mWeekStart = startOfWeek;
		if (this.mDayPickerView != null) {
			this.mDayPickerView.onChange();
		}
	}

	public void setOnDateSetListener(final OnDateSetListener listener) {
		this.mCallBack = listener;
	}

	public void setYearRange(final int startYear, final int endYear) {
		if (endYear <= startYear) {
			throw new IllegalArgumentException("Year end must be larger than year start");
		}
		this.mMinYear = startYear;
		this.mMaxYear = endYear;
		if (this.mDayPickerView != null) {
			this.mDayPickerView.onChange();
		}
	}

	/**
	 * Try to vibrate. To prevent this becoming a single continuous vibration, nothing will happen if we have vibrated
	 * very recently.
	 */
	@Override
	public void tryVibrate() {
		if (this.mVibrator != null) {
			final long now = SystemClock.uptimeMillis();
			// We want to try to vibrate each individual tick discretely.
			if ((now - this.mLastVibrate) >= 125) {
				this.mVibrator.vibrate(5);
				this.mLastVibrate = now;
			}
		}
	}

	@Override
	public void unregisterOnDateChangedListener(final OnDateChangedListener listener) {
		this.mListeners.remove(listener);
	}

	private void updateDisplay(final boolean announce) {
		if (this.mDayOfWeekView != null) {
			this.mDayOfWeekView.setText(this.mCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
					Locale.getDefault()).toUpperCase(Locale.getDefault()));
		}

		this.mSelectedMonthTextView.setText(this.mCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
				Locale.getDefault()).toUpperCase(Locale.getDefault()));
		this.mSelectedDayTextView.setText(DAY_FORMAT.format(this.mCalendar.getTime()));
		this.mYearView.setText(YEAR_FORMAT.format(this.mCalendar.getTime()));

		// Accessibility.
		final long millis = this.mCalendar.getTimeInMillis();
		this.mAnimator.setDateMillis(millis);
		int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR;
		final String monthAndDayText = DateUtils.formatDateTime(this.getActivity(), millis, flags);
		this.mMonthAndDayView.setContentDescription(monthAndDayText);

		if (announce) {
			flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
			final String fullDateText = DateUtils.formatDateTime(this.getActivity(), millis, flags);
			Utils.tryAccessibilityAnnounce(this.mAnimator, fullDateText);
		}
	}

	private void updatePickers() {
		final Iterator<OnDateChangedListener> iterator = this.mListeners.iterator();
		while (iterator.hasNext()) {
			iterator.next().onDateChanged();
		}
	}
}
