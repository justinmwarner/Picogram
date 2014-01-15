package com.doomonafireball.betterpickers.datepicker;


import com.doomonafireball.betterpickers.widget.UnderlinePageIndicatorPicker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.doomonafireball.betterpickers.R;

public class DatePicker extends LinearLayout implements Button.OnClickListener,
Button.OnLongClickListener {

	private class KeyboardPagerAdapter extends PagerAdapter {

		private final LayoutInflater mInflater;

		public KeyboardPagerAdapter(final LayoutInflater inflater) {
			super();
			this.mInflater = inflater;
		}

		@Override
		public void destroyItem(final ViewGroup container, final int position, final Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return 3;
		}

		/**
		 * Based on the Locale, inflate the day, month, or year keyboard
		 *
		 * @param collection the ViewPager collection group
		 * @param position the position within the ViewPager
		 * @return an inflated View representing the keyboard for this position
		 */
		@Override
		public Object instantiateItem(final ViewGroup collection, final int position) {
			View view;
			final Resources res = DatePicker.this.mContext.getResources();
			if (DatePicker.this.mDateFormatOrder[position] == DateFormat.MONTH) {
				// Months
				sMonthKeyboardPosition = position;
				view = this.mInflater.inflate(R.layout.keyboard_text_with_header, null);
				final View v1 = view.findViewById(R.id.first);
				final View v2 = view.findViewById(R.id.second);
				final View v3 = view.findViewById(R.id.third);
				final View v4 = view.findViewById(R.id.fourth);
				final TextView header = (TextView) view.findViewById(R.id.header);

				header.setText(R.string.month_c);

				DatePicker.this.mMonths[0] = (Button) v1.findViewById(R.id.key_left);
				DatePicker.this.mMonths[1] = (Button) v1.findViewById(R.id.key_middle);
				DatePicker.this.mMonths[2] = (Button) v1.findViewById(R.id.key_right);

				DatePicker.this.mMonths[3] = (Button) v2.findViewById(R.id.key_left);
				DatePicker.this.mMonths[4] = (Button) v2.findViewById(R.id.key_middle);
				DatePicker.this.mMonths[5] = (Button) v2.findViewById(R.id.key_right);

				DatePicker.this.mMonths[6] = (Button) v3.findViewById(R.id.key_left);
				DatePicker.this.mMonths[7] = (Button) v3.findViewById(R.id.key_middle);
				DatePicker.this.mMonths[8] = (Button) v3.findViewById(R.id.key_right);

				DatePicker.this.mMonths[9] = (Button) v4.findViewById(R.id.key_left);
				DatePicker.this.mMonths[10] = (Button) v4.findViewById(R.id.key_middle);
				DatePicker.this.mMonths[11] = (Button) v4.findViewById(R.id.key_right);

				for (int i = 0; i < 12; i++) {
					DatePicker.this.mMonths[i].setOnClickListener(DatePicker.this);
					DatePicker.this.mMonths[i].setText(DatePicker.this.mMonthAbbreviations[i]);
					DatePicker.this.mMonths[i].setTextColor(DatePicker.this.mTextColor);
					DatePicker.this.mMonths[i].setBackgroundResource(DatePicker.this.mKeyBackgroundResId);
					DatePicker.this.mMonths[i].setTag(R.id.date_keyboard, KEYBOARD_MONTH);
					DatePicker.this.mMonths[i].setTag(R.id.date_month_int, i);
				}
			} else if (DatePicker.this.mDateFormatOrder[position] == DateFormat.DATE) {
				// Date
				sDateKeyboardPosition = position;
				view = this.mInflater.inflate(R.layout.keyboard_right_drawable_with_header, null);
				final View v1 = view.findViewById(R.id.first);
				final View v2 = view.findViewById(R.id.second);
				final View v3 = view.findViewById(R.id.third);
				final View v4 = view.findViewById(R.id.fourth);
				final TextView header = (TextView) view.findViewById(R.id.header);

				header.setText(R.string.day_c);

				DatePicker.this.mDateNumbers[1] = (Button) v1.findViewById(R.id.key_left);
				DatePicker.this.mDateNumbers[2] = (Button) v1.findViewById(R.id.key_middle);
				DatePicker.this.mDateNumbers[3] = (Button) v1.findViewById(R.id.key_right);

				DatePicker.this.mDateNumbers[4] = (Button) v2.findViewById(R.id.key_left);
				DatePicker.this.mDateNumbers[5] = (Button) v2.findViewById(R.id.key_middle);
				DatePicker.this.mDateNumbers[6] = (Button) v2.findViewById(R.id.key_right);

				DatePicker.this.mDateNumbers[7] = (Button) v3.findViewById(R.id.key_left);
				DatePicker.this.mDateNumbers[8] = (Button) v3.findViewById(R.id.key_middle);
				DatePicker.this.mDateNumbers[9] = (Button) v3.findViewById(R.id.key_right);

				DatePicker.this.mDateLeft = (Button) v4.findViewById(R.id.key_left);
				DatePicker.this.mDateLeft.setTextColor(DatePicker.this.mTextColor);
				DatePicker.this.mDateLeft.setBackgroundResource(DatePicker.this.mKeyBackgroundResId);
				DatePicker.this.mDateNumbers[0] = (Button) v4.findViewById(R.id.key_middle);
				DatePicker.this.mDateRight = (ImageButton) v4.findViewById(R.id.key_right);

				for (int i = 0; i < 10; i++) {
					DatePicker.this.mDateNumbers[i].setOnClickListener(DatePicker.this);
					DatePicker.this.mDateNumbers[i].setText(String.format("%d", i));
					DatePicker.this.mDateNumbers[i].setTextColor(DatePicker.this.mTextColor);
					DatePicker.this.mDateNumbers[i].setBackgroundResource(DatePicker.this.mKeyBackgroundResId);
					DatePicker.this.mDateNumbers[i].setTag(R.id.date_keyboard, KEYBOARD_DATE);
					DatePicker.this.mDateNumbers[i].setTag(R.id.numbers_key, i);
				}

				DatePicker.this.mDateRight.setImageDrawable(res.getDrawable(DatePicker.this.mCheckDrawableSrcResId));
				DatePicker.this.mDateRight.setBackgroundResource(DatePicker.this.mKeyBackgroundResId);
				DatePicker.this.mDateRight.setOnClickListener(DatePicker.this);
			} else if (DatePicker.this.mDateFormatOrder[position] == DateFormat.YEAR) {
				// Year
				sYearKeyboardPosition = position;
				view = this.mInflater.inflate(R.layout.keyboard_with_header, null);
				final View v1 = view.findViewById(R.id.first);
				final View v2 = view.findViewById(R.id.second);
				final View v3 = view.findViewById(R.id.third);
				final View v4 = view.findViewById(R.id.fourth);
				final TextView header = (TextView) view.findViewById(R.id.header);

				header.setText(R.string.year_c);

				DatePicker.this.mYearNumbers[1] = (Button) v1.findViewById(R.id.key_left);
				DatePicker.this.mYearNumbers[2] = (Button) v1.findViewById(R.id.key_middle);
				DatePicker.this.mYearNumbers[3] = (Button) v1.findViewById(R.id.key_right);

				DatePicker.this.mYearNumbers[4] = (Button) v2.findViewById(R.id.key_left);
				DatePicker.this.mYearNumbers[5] = (Button) v2.findViewById(R.id.key_middle);
				DatePicker.this.mYearNumbers[6] = (Button) v2.findViewById(R.id.key_right);

				DatePicker.this.mYearNumbers[7] = (Button) v3.findViewById(R.id.key_left);
				DatePicker.this.mYearNumbers[8] = (Button) v3.findViewById(R.id.key_middle);
				DatePicker.this.mYearNumbers[9] = (Button) v3.findViewById(R.id.key_right);

				DatePicker.this.mYearLeft = (Button) v4.findViewById(R.id.key_left);
				DatePicker.this.mYearLeft.setTextColor(DatePicker.this.mTextColor);
				DatePicker.this.mYearLeft.setBackgroundResource(DatePicker.this.mKeyBackgroundResId);
				DatePicker.this.mYearNumbers[0] = (Button) v4.findViewById(R.id.key_middle);
				DatePicker.this.mYearRight = (Button) v4.findViewById(R.id.key_right);
				DatePicker.this.mYearRight.setTextColor(DatePicker.this.mTextColor);
				DatePicker.this.mYearRight.setBackgroundResource(DatePicker.this.mKeyBackgroundResId);

				for (int i = 0; i < 10; i++) {
					DatePicker.this.mYearNumbers[i].setOnClickListener(DatePicker.this);
					DatePicker.this.mYearNumbers[i].setText(String.format("%d", i));
					DatePicker.this.mYearNumbers[i].setTextColor(DatePicker.this.mTextColor);
					DatePicker.this.mYearNumbers[i].setBackgroundResource(DatePicker.this.mKeyBackgroundResId);
					DatePicker.this.mYearNumbers[i].setTag(R.id.date_keyboard, KEYBOARD_YEAR);
					DatePicker.this.mYearNumbers[i].setTag(R.id.numbers_key, i);
				}
			} else {
				view = new View(DatePicker.this.mContext);
			}
			DatePicker.this.setLeftRightEnabled();
			DatePicker.this.updateDate();
			DatePicker.this.updateKeypad();
			collection.addView(view, 0);

			return view;
		}

		@Override
		public boolean isViewFromObject(final View view, final Object o) {
			return view == o;
		}
	}
	private static class SavedState extends BaseSavedState {

		int mDateInputPointer;
		int mYearInputPointer;
		int[] mDateInput;
		int[] mYearInput;
		int mMonthInput;

		public static final Creator<SavedState> CREATOR
		= new Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(final Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(final int size) {
				return new SavedState[size];
			}
		};

		private SavedState(final Parcel in) {
			super(in);
			this.mDateInputPointer = in.readInt();
			this.mYearInputPointer = in.readInt();
			in.readIntArray(this.mDateInput);
			in.readIntArray(this.mYearInput);
			this.mMonthInput = in.readInt();
		}

		public SavedState(final Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(final Parcel dest, final int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(this.mDateInputPointer);
			dest.writeInt(this.mYearInputPointer);
			dest.writeIntArray(this.mDateInput);
			dest.writeIntArray(this.mYearInput);
			dest.writeInt(this.mMonthInput);
		}
	}
	protected int mDateInputSize = 2;
	protected int mYearInputSize = 4;
	protected int mMonthInput = -1;
	protected int mDateInput[] = new int[this.mDateInputSize];
	protected int mYearInput[] = new int[this.mYearInputSize];
	protected int mDateInputPointer = -1;
	protected int mYearInputPointer = -1;
	protected final Button mMonths[] = new Button[12];
	protected final Button mDateNumbers[] = new Button[10];
	protected final Button mYearNumbers[] = new Button[10];
	protected Button mDateLeft;
	protected Button mYearLeft, mYearRight;
	protected ImageButton mDateRight;
	protected UnderlinePageIndicatorPicker mKeyboardIndicator;
	protected ViewPager mKeyboardPager;
	protected KeyboardPagerAdapter mKeyboardPagerAdapter;
	protected ImageButton mDelete;
	protected DateView mEnteredDate;
	protected String[] mMonthAbbreviations;

	protected final Context mContext;
	private final char[] mDateFormatOrder;
	private static final String KEYBOARD_MONTH = "month";

	private static final String KEYBOARD_DATE = "date";
	private static final String KEYBOARD_YEAR = "year";
	private static int sMonthKeyboardPosition = -1;

	private static int sDateKeyboardPosition = -1;

	private static int sYearKeyboardPosition = -1;
	/**
	 * Create a String array with all the months abbreviations localized with the default Locale.
	 *
	 * @return a String array with all localized month abbreviations like JAN, FEB, etc.
	 */
	public static String[] makeLocalizedMonthAbbreviations() {
		return makeLocalizedMonthAbbreviations(Locale.getDefault());
	}
	/**
	 * Create a String array with all the months abbreviations localized with the specified Locale.
	 *
	 * @param locale the Locale to use for localization, or null to use the default one
	 * @return a String array with all localized month abbreviations like JAN, FEB, etc.
	 */
	public static String[] makeLocalizedMonthAbbreviations(final Locale locale) {
		final boolean hasLocale = locale != null;
		final SimpleDateFormat monthAbbreviationFormat = hasLocale ? new SimpleDateFormat("MMM", locale)
		: new SimpleDateFormat("MMM");
		final Calendar date = hasLocale ? new GregorianCalendar(locale)
		: new GregorianCalendar();
		date.set(Calendar.YEAR, 0);
		date.set(Calendar.DAY_OF_MONTH, 1);
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);

		final String[] months = new String[12];
		for (int i = 0; i < months.length; i++) {
			date.set(Calendar.MONTH, i);
			months[i] = monthAbbreviationFormat.format(date.getTime()).toUpperCase();
		}
		return months;
	}
	private Button mSetButton;
	protected View mDivider;
	private ColorStateList mTextColor;
	private int mKeyBackgroundResId;
	private int mButtonBackgroundResId;
	private int mTitleDividerColor;

	private int mKeyboardIndicatorColor;

	private int mCheckDrawableSrcResId;

	private int mDeleteDrawableSrcResId;

	private int mTheme = -1;

	/**
	 * Instantiates a DatePicker object
	 *
	 * @param context the Context required for creation
	 */
	public DatePicker(final Context context) {
		this(context, null);
	}

	/**
	 * Instantiates a DatePicker object
	 *
	 * @param context the Context required for creation
	 * @param attrs additional attributes that define custom colors, selectors, and backgrounds.
	 */
	public DatePicker(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		this.mDateFormatOrder = DateFormat.getDateFormatOrder(this.mContext);
		this.mMonthAbbreviations = makeLocalizedMonthAbbreviations();
		final LayoutInflater layoutInflater =
				(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(this.getLayoutId(), this);

		// Init defaults
		this.mTextColor = this.getResources().getColorStateList(R.color.dialog_text_color_holo_dark);
		this.mKeyBackgroundResId = R.drawable.key_background_dark;
		this.mButtonBackgroundResId = R.drawable.button_background_dark;
		this.mTitleDividerColor = this.getResources().getColor(R.color.default_divider_color_dark);
		this.mKeyboardIndicatorColor = this.getResources().getColor(R.color.default_keyboard_indicator_color_dark);
		this.mDeleteDrawableSrcResId = R.drawable.ic_backspace_dark;
		this.mCheckDrawableSrcResId = R.drawable.ic_check_dark;
	}

	private void addClickedDateNumber(final int val) {
		if (this.mDateInputPointer < (this.mDateInputSize - 1)) {
			for (int i = this.mDateInputPointer; i >= 0; i--) {
				this.mDateInput[i + 1] = this.mDateInput[i];
			}
			this.mDateInputPointer++;
			this.mDateInput[0] = val;
		}
		if ((this.getDayOfMonth() >= 4) || ((this.getMonthOfYear() == 1) && (this.getDayOfMonth() >= 3))) {
			if (this.mKeyboardPager.getCurrentItem() < 2) {
				this.mKeyboardPager.setCurrentItem(this.mKeyboardPager.getCurrentItem() + 1, true);
			}
		}
	}

	private void addClickedYearNumber(final int val) {
		if (this.mYearInputPointer < (this.mYearInputSize - 1)) {
			for (int i = this.mYearInputPointer; i >= 0; i--) {
				this.mYearInput[i + 1] = this.mYearInput[i];
			}
			this.mYearInputPointer++;
			this.mYearInput[0] = val;
		}
		// Move to the next keyboard if the year is >= 1000 (not in every case)
		if ((this.getYear() >= 1000) && (this.mKeyboardPager.getCurrentItem() < 2)) {
			this.mKeyboardPager.setCurrentItem(this.mKeyboardPager.getCurrentItem() + 1, true);
		}
	}

	/**
	 * Check if a user can move to the year keyboard
	 *
	 * @return true or false whether the user can move to the year keyboard
	 */
	private boolean canGoToYear() {
		return this.getDayOfMonth() > 0;
	}

	protected void doOnClick(final View v) {
		if (v == this.mDelete) {
			// Delete is dependent on which keyboard
			switch (this.mDateFormatOrder[this.mKeyboardPager.getCurrentItem()]) {
				case DateFormat.MONTH:
					if (this.mMonthInput != -1) {
						this.mMonthInput = -1;
					}
					break;
				case DateFormat.DATE:
					if (this.mDateInputPointer >= 0) {
						for (int i = 0; i < this.mDateInputPointer; i++) {
							this.mDateInput[i] = this.mDateInput[i + 1];
						}
						this.mDateInput[this.mDateInputPointer] = 0;
						this.mDateInputPointer--;
					} else if (this.mKeyboardPager.getCurrentItem() > 0) {
						this.mKeyboardPager.setCurrentItem(this.mKeyboardPager.getCurrentItem() - 1, true);
					}
					break;
				case DateFormat.YEAR:
					if (this.mYearInputPointer >= 0) {
						for (int i = 0; i < this.mYearInputPointer; i++) {
							this.mYearInput[i] = this.mYearInput[i + 1];
						}
						this.mYearInput[this.mYearInputPointer] = 0;
						this.mYearInputPointer--;
					} else if (this.mKeyboardPager.getCurrentItem() > 0) {
						this.mKeyboardPager.setCurrentItem(this.mKeyboardPager.getCurrentItem() - 1, true);
					}
					break;
			}
		} else if (v == this.mDateRight) {
			this.onDateRightClicked();
		} else if (v == this.mEnteredDate.getDate()) {
			this.mKeyboardPager.setCurrentItem(sDateKeyboardPosition);
		} else if (v == this.mEnteredDate.getMonth()) {
			this.mKeyboardPager.setCurrentItem(sMonthKeyboardPosition);
		} else if (v == this.mEnteredDate.getYear()) {
			this.mKeyboardPager.setCurrentItem(sYearKeyboardPosition);
		} else if (v.getTag(R.id.date_keyboard).equals(KEYBOARD_MONTH)) {
			// A month was pressed
			this.mMonthInput = (Integer) v.getTag(R.id.date_month_int);
			if (this.mKeyboardPager.getCurrentItem() < 2) {
				this.mKeyboardPager.setCurrentItem(this.mKeyboardPager.getCurrentItem() + 1, true);
			}
		} else if (v.getTag(R.id.date_keyboard).equals(KEYBOARD_DATE)) {
			// A date number was pressed
			this.addClickedDateNumber((Integer) v.getTag(R.id.numbers_key));
		} else if (v.getTag(R.id.date_keyboard).equals(KEYBOARD_YEAR)) {
			// A year number was pressed
			this.addClickedYearNumber((Integer) v.getTag(R.id.numbers_key));
		}
		this.updateKeypad();
	}

	/**
	 * Enable/disable the "Set" button
	 */
	private void enableSetButton() {
		if (this.mSetButton == null) {
			return;
		}
		this.mSetButton.setEnabled((this.getDayOfMonth() > 0) && (this.getYear() > 0) && (this.getMonthOfYear() >= 0));
	}

	/**
	 * Returns the day of month as currently inputted by the user.
	 *
	 * @return the inputted day of month
	 */
	public int getDayOfMonth() {
		return (this.mDateInput[1] * 10) + this.mDateInput[0];
	}

	protected int getLayoutId() {
		return R.layout.date_picker_view;
	}

	/**
	 * Returns the zero-indexed month of year as currently inputted by the user.
	 *
	 * @return the zero-indexed inputted month
	 */
	public int getMonthOfYear() {
		return this.mMonthInput;
	}

	/**
	 * Returns the year as currently inputted by the user.
	 *
	 * @return the inputted year
	 */
	public int getYear() {
		return (this.mYearInput[3] * 1000) + (this.mYearInput[2] * 100) + (this.mYearInput[1] * 10) + this.mYearInput[0];
	}

	@Override
	public void onClick(final View v) {
		v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		this.doOnClick(v);
		this.updateDeleteButton();
	}

	/**
	 * Clicking on the date right button advances
	 */
	private void onDateRightClicked() {
		if (this.mKeyboardPager.getCurrentItem() < 2) {
			this.mKeyboardPager.setCurrentItem(this.mKeyboardPager.getCurrentItem() + 1, true);
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		this.mDivider = this.findViewById(R.id.divider);

		for (int i = 0; i < this.mDateInput.length; i++) {
			this.mDateInput[i] = 0;
		}
		for (int i = 0; i < this.mYearInput.length; i++) {
			this.mYearInput[i] = 0;
		}

		this.mKeyboardIndicator = (UnderlinePageIndicatorPicker) this.findViewById(R.id.keyboard_indicator);
		this.mKeyboardPager = (ViewPager) this.findViewById(R.id.keyboard_pager);
		this.mKeyboardPager.setOffscreenPageLimit(2);
		this.mKeyboardPagerAdapter = new KeyboardPagerAdapter(
				(LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		this.mKeyboardPager.setAdapter(this.mKeyboardPagerAdapter);
		this.mKeyboardIndicator.setViewPager(this.mKeyboardPager);
		this.mKeyboardPager.setCurrentItem(0);

		this.mEnteredDate = (DateView) this.findViewById(R.id.date_text);
		this.mEnteredDate.setTheme(this.mTheme);
		this.mEnteredDate.setUnderlinePage(this.mKeyboardIndicator);
		this.mEnteredDate.setOnClick(this);

		this.mDelete = (ImageButton) this.findViewById(R.id.delete);
		this.mDelete.setOnClickListener(this);
		this.mDelete.setOnLongClickListener(this);

		this.setLeftRightEnabled();
		this.updateDate();
		this.updateKeypad();
	}

	@Override
	public boolean onLongClick(final View v) {
		v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
		if (v == this.mDelete) {
			this.mDelete.setPressed(false);
			this.reset();
			this.updateKeypad();
			return true;
		}
		return false;
	}

	@Override
	protected void onRestoreInstanceState(final Parcelable state) {
		if (!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}

		final SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		this.mDateInputPointer = savedState.mDateInputPointer;
		this.mYearInputPointer = savedState.mYearInputPointer;
		this.mDateInput = savedState.mDateInput;
		this.mYearInput = savedState.mYearInput;
		if (this.mDateInput == null) {
			this.mDateInput = new int[this.mDateInputSize];
			this.mDateInputPointer = -1;
		}
		if (this.mYearInput == null) {
			this.mYearInput = new int[this.mYearInputSize];
			this.mYearInputPointer = -1;
		}
		this.mMonthInput = savedState.mMonthInput;
		this.updateKeypad();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		final Parcelable parcel = super.onSaveInstanceState();
		final SavedState state = new SavedState(parcel);
		state.mMonthInput = this.mMonthInput;
		state.mDateInput = this.mDateInput;
		state.mDateInputPointer = this.mDateInputPointer;
		state.mYearInput = this.mYearInput;
		state.mYearInputPointer = this.mYearInputPointer;
		return state;
	}

	/**
	 * Reset all inputs and dates, and scroll to the first shown keyboard.
	 */
	 public void reset() {
		for (int i = 0; i < this.mDateInputSize; i++) {
			this.mDateInput[i] = 0;
		}
		for (int i = 0; i < this.mYearInputSize; i++) {
			this.mYearInput[i] = 0;
		}
		this.mDateInputPointer = -1;
		this.mYearInputPointer = -1;
		this.mMonthInput = -1;
		this.mKeyboardPager.setCurrentItem(0, true);
		this.updateDate();
	}

	private void restyleViews() {
		for (final Button month : this.mMonths) {
			if (month != null) {
				month.setTextColor(this.mTextColor);
				month.setBackgroundResource(this.mKeyBackgroundResId);
			}
		}
		for (final Button dateNumber : this.mDateNumbers) {
			if (dateNumber != null) {
				dateNumber.setTextColor(this.mTextColor);
				dateNumber.setBackgroundResource(this.mKeyBackgroundResId);
			}
		}
		for (final Button yearNumber : this.mYearNumbers) {
			if (yearNumber != null) {
				yearNumber.setTextColor(this.mTextColor);
				yearNumber.setBackgroundResource(this.mKeyBackgroundResId);
			}
		}
		if (this.mKeyboardIndicator != null) {
			this.mKeyboardIndicator.setSelectedColor(this.mKeyboardIndicatorColor);
		}
		if (this.mDivider != null) {
			this.mDivider.setBackgroundColor(this.mTitleDividerColor);
		}
		if (this.mDateLeft != null) {
			this.mDateLeft.setTextColor(this.mTextColor);
			this.mDateLeft.setBackgroundResource(this.mKeyBackgroundResId);
		}
		if (this.mDateRight != null) {
			this.mDateRight.setBackgroundResource(this.mKeyBackgroundResId);
			this.mDateRight.setImageDrawable(this.getResources().getDrawable(this.mCheckDrawableSrcResId));
		}
		if (this.mDelete != null) {
			this.mDelete.setBackgroundResource(this.mButtonBackgroundResId);
			this.mDelete.setImageDrawable(this.getResources().getDrawable(this.mDeleteDrawableSrcResId));
		}
		if (this.mYearLeft != null) {
			this.mYearLeft.setTextColor(this.mTextColor);
			this.mYearLeft.setBackgroundResource(this.mKeyBackgroundResId);
		}
		if (this.mYearRight != null) {
			this.mYearRight.setTextColor(this.mTextColor);
			this.mYearRight.setBackgroundResource(this.mKeyBackgroundResId);
		}
		if (this.mEnteredDate != null) {
			this.mEnteredDate.setTheme(this.mTheme);
		}
	}

	/**
	 * Set the date shown in the date picker
	 *
	 * @param year the new year to set
	 * @param monthOfYear the new zero-indexed month to set
	 * @param dayOfMonth the new day of month to set
	 */
	 public void setDate(final int year, final int monthOfYear, final int dayOfMonth) {
		this.mMonthInput = monthOfYear;
		this.mYearInput[3] = year / 1000;
		this.mYearInput[2] = (year % 1000) / 100;
		this.mYearInput[1] = (year % 100) / 10;
		this.mYearInput[0] = year % 10;
		if (year >= 1000) {
			this.mYearInputPointer = 3;
		} else if (year >= 100) {
			this.mYearInputPointer = 2;
		} else if (year >= 10) {
			this.mYearInputPointer = 1;
		} else if (year > 0) {
			this.mYearInputPointer = 0;
		}
		this.mDateInput[1] = dayOfMonth / 10;
		this.mDateInput[0] = dayOfMonth % 10;
		if (dayOfMonth >= 10) {
			this.mDateInputPointer = 1;
		} else if (dayOfMonth > 0) {
			this.mDateInputPointer = 0;
		}
		for (int i = 0; i < this.mDateFormatOrder.length; i++) {
			final char c = this.mDateFormatOrder[i];
			if ((c == DateFormat.MONTH) && (monthOfYear == -1)) {
				this.mKeyboardPager.setCurrentItem(i, true);
				break;
			} else if ((c == DateFormat.DATE) && (dayOfMonth <= 0)) {
				this.mKeyboardPager.setCurrentItem(i, true);
				break;
			} else if ((c == DateFormat.YEAR) && (year <= 0)) {
				this.mKeyboardPager.setCurrentItem(i, true);
				break;
			}
		}
		this.updateKeypad();
	 }

	 /**
	  * Enables a range of numeric keys from zero to maxKey. The rest of the keys will be disabled
	  *
	  * @param maxKey the maximum key number that can be pressed
	  */
	 private void setDateKeyRange(final int maxKey) {
		 for (int i = 0; i < this.mDateNumbers.length; i++) {
			 if (this.mDateNumbers[i] != null) {
				 this.mDateNumbers[i].setEnabled(i <= maxKey);
			 }
		 }
	 }

	 /**
	  * Enables a range of numeric keys from minKey up. The rest of the keys will be disabled
	  *
	  * @param minKey the minimum key number that can be pressed
	  */
	 private void setDateMinKeyRange(final int minKey) {
		 for (int i = 0; i < this.mDateNumbers.length; i++) {
			 if (this.mDateNumbers[i] != null) {
				 this.mDateNumbers[i].setEnabled(i >= minKey);
			 }
		 }
	 }

	 protected void setLeftRightEnabled() {
		 if (this.mDateLeft != null) {
			 this.mDateLeft.setEnabled(false);
		 }
		 if (this.mDateRight != null) {
			 this.mDateRight.setEnabled(this.canGoToYear());
		 }
		 if (this.mYearLeft != null) {
			 this.mYearLeft.setEnabled(false);
		 }
		 if (this.mYearRight != null) {
			 this.mYearRight.setEnabled(false);
		 }
	 }

	 /**
	  * Expose the set button to allow communication with the parent Fragment.
	  *
	  * @param b the parent Fragment's "Set" button
	  */
	 public void setSetButton(final Button b) {
		 this.mSetButton = b;
		 this.enableSetButton();
	 }

	 /**
	  * Change the theme of the Picker
	  *
	  * @param themeResId the resource ID of the new style
	  */
	 public void setTheme(final int themeResId) {
		 this.mTheme = themeResId;
		 if (this.mTheme != -1) {
			 final TypedArray a = this.getContext().obtainStyledAttributes(themeResId, R.styleable.BetterPickersDialogFragment);

			 this.mTextColor = a.getColorStateList(R.styleable.BetterPickersDialogFragment_bpTextColor);
			 this.mKeyBackgroundResId = a.getResourceId(R.styleable.BetterPickersDialogFragment_bpKeyBackground,
					 this.mKeyBackgroundResId);
			 this.mButtonBackgroundResId = a.getResourceId(R.styleable.BetterPickersDialogFragment_bpButtonBackground,
					 this.mButtonBackgroundResId);
			 this.mCheckDrawableSrcResId = a.getResourceId(R.styleable.BetterPickersDialogFragment_bpCheckIcon,
					 this.mCheckDrawableSrcResId);
			 this.mTitleDividerColor = a
					 .getColor(R.styleable.BetterPickersDialogFragment_bpTitleDividerColor, this.mTitleDividerColor);
			 this.mKeyboardIndicatorColor = a
					 .getColor(R.styleable.BetterPickersDialogFragment_bpKeyboardIndicatorColor,
							 this.mKeyboardIndicatorColor);
			 this.mDeleteDrawableSrcResId = a.getResourceId(R.styleable.BetterPickersDialogFragment_bpDeleteIcon,
					 this.mDeleteDrawableSrcResId);
		 }

		 this.restyleViews();
	 }

	 /**
	  * Enables a range of numeric keys from zero to maxKey. The rest of the keys will be disabled
	  *
	  * @param maxKey the maximum key that can be pressed
	  */
	 private void setYearKeyRange(final int maxKey) {
		 for (int i = 0; i < this.mYearNumbers.length; i++) {
			 if (this.mYearNumbers[i] != null) {
				 this.mYearNumbers[i].setEnabled(i <= maxKey);
			 }
		 }
	 }

	 /**
	  * Enables a range of numeric keys from minKey up. The rest of the keys will be disabled
	  *
	  * @param minKey the minimum key that can be pressed
	  */
	 private void setYearMinKeyRange(final int minKey) {
		 for (int i = 0; i < this.mYearNumbers.length; i++) {
			 if (this.mYearNumbers[i] != null) {
				 this.mYearNumbers[i].setEnabled(i >= minKey);
			 }
		 }
	 }

	 protected void updateDate() {
		 String month;
		 if (this.mMonthInput < 0) {
			 month = "";
		 } else {
			 month = this.mMonthAbbreviations[this.mMonthInput];
		 }
		 this.mEnteredDate.setDate(month, this.getDayOfMonth(), this.getYear());
	 }

	 /**
	  * Enable/disable keys on the date key pad according to the data entered
	  */
	 private void updateDateKeys() {
		 final int date = this.getDayOfMonth();
		 if (date >= 4) {
			 this.setDateKeyRange(-1);
		 } else if (date >= 3) {
			 if (this.mMonthInput == 1) {
				 // February
				 this.setDateKeyRange(-1);
			 } else if ((this.mMonthInput == 3) || (this.mMonthInput == 5) || (this.mMonthInput == 8) || (this.mMonthInput == 10)) {
				 // April, June, September, Novemeber have 30 days
				 this.setDateKeyRange(0);
			 } else {
				 this.setDateKeyRange(1);
			 }
		 } else if (date >= 2) {
			 this.setDateKeyRange(9);
		 } else if (date >= 1) {
			 this.setDateKeyRange(9);
		 } else {
			 this.setDateMinKeyRange(1);
		 }
	 }

	 /**
	  * Update the delete button to determine whether it is able to be clicked.
	  */
	 public void updateDeleteButton() {
		 final boolean enabled = (this.mMonthInput != -1) || (this.mDateInputPointer != -1) || (this.mYearInputPointer != -1);
		 if (this.mDelete != null) {
			 this.mDelete.setEnabled(enabled);
		 }
	 }

	 private void updateKeypad() {
		 // Update state of keypad
		 // Update the number
		 this.updateLeftRightButtons();
		 this.updateDate();
		 // enable/disable the "set" key
		 this.enableSetButton();
		 // Update the backspace button
		 this.updateDeleteButton();
		 this.updateMonthKeys();
		 this.updateDateKeys();
		 this.updateYearKeys();
	 }

	 private void updateLeftRightButtons() {
		 if (this.mDateRight != null) {
			 this.mDateRight.setEnabled(this.canGoToYear());
		 }
	 }

	 /**
	  * Enable/disable keys on the month key pad according to the data entered
	  */
	 private void updateMonthKeys() {
		 final int date = this.getDayOfMonth();
		 for (int i = 0; i < this.mMonths.length; i++) {
			 if (this.mMonths[i] != null) {
				 this.mMonths[i].setEnabled(true);
			 }
		 }
		 if (date > 29) {
			 // Disable February
			 if (this.mMonths[1] != null) {
				 this.mMonths[1].setEnabled(false);
			 }
		 }
		 if (date > 30) {
			 // Disable April, June, September, November
			 if (this.mMonths[3] != null) {
				 this.mMonths[3].setEnabled(false);
			 }
			 if (this.mMonths[5] != null) {
				 this.mMonths[5].setEnabled(false);
			 }
			 if (this.mMonths[8] != null) {
				 this.mMonths[8].setEnabled(false);
			 }
			 if (this.mMonths[10] != null) {
				 this.mMonths[10].setEnabled(false);
			 }
		 }
	 }

	 /**
	  * Enable/disable keys on the year key pad according to the data entered
	  */
	 private void updateYearKeys() {
		 final int year = this.getYear();
		 if (year >= 1000) {
			 this.setYearKeyRange(-1);
		 } else if (year >= 1) {
			 this.setYearKeyRange(9);
		 } else {
			 this.setYearMinKeyRange(1);
		 }
	 }
}
