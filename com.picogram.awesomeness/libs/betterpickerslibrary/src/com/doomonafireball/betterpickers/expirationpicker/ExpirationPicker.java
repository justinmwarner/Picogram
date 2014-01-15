package com.doomonafireball.betterpickers.expirationpicker;


import com.doomonafireball.betterpickers.widget.UnderlinePageIndicatorPicker;
import com.doomonafireball.betterpickers.datepicker.DatePicker;

import android.annotation.SuppressLint;
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

import java.util.Calendar;


import com.doomonafireball.betterpickers.R;
public class ExpirationPicker extends LinearLayout implements Button.OnClickListener,
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
			return 2;
		}

		/**
		 * Based on the Locale, inflate the month, or year keyboard
		 *
		 * @param collection the ViewPager collection group
		 * @param position the position within the ViewPager
		 * @return an inflated View representing the keyboard for this position
		 */
		@Override
		public Object instantiateItem(final ViewGroup collection, final int position) {
			View view;
			final Resources res = ExpirationPicker.this.mContext.getResources();
			if (position == EXPIRATION_MONTH_POSITION) {
				// Months
				sMonthKeyboardPosition = position;
				view = this.mInflater.inflate(R.layout.keyboard_text, null);
				final View v1 = view.findViewById(R.id.first);
				final View v2 = view.findViewById(R.id.second);
				final View v3 = view.findViewById(R.id.third);
				final View v4 = view.findViewById(R.id.fourth);

				ExpirationPicker.this.mMonths[0] = (Button) v1.findViewById(R.id.key_left);
				ExpirationPicker.this.mMonths[1] = (Button) v1.findViewById(R.id.key_middle);
				ExpirationPicker.this.mMonths[2] = (Button) v1.findViewById(R.id.key_right);

				ExpirationPicker.this.mMonths[3] = (Button) v2.findViewById(R.id.key_left);
				ExpirationPicker.this.mMonths[4] = (Button) v2.findViewById(R.id.key_middle);
				ExpirationPicker.this.mMonths[5] = (Button) v2.findViewById(R.id.key_right);

				ExpirationPicker.this.mMonths[6] = (Button) v3.findViewById(R.id.key_left);
				ExpirationPicker.this.mMonths[7] = (Button) v3.findViewById(R.id.key_middle);
				ExpirationPicker.this.mMonths[8] = (Button) v3.findViewById(R.id.key_right);

				ExpirationPicker.this.mMonths[9] = (Button) v4.findViewById(R.id.key_left);
				ExpirationPicker.this.mMonths[10] = (Button) v4.findViewById(R.id.key_middle);
				ExpirationPicker.this.mMonths[11] = (Button) v4.findViewById(R.id.key_right);

				for (int i = 0; i < 12; i++) {
					ExpirationPicker.this.mMonths[i].setOnClickListener(ExpirationPicker.this);
					//mMonths[i].setText(mMonthAbbreviations[i]);
					ExpirationPicker.this.mMonths[i].setText(String.format("%02d", i + 1));
					ExpirationPicker.this.mMonths[i].setTextColor(ExpirationPicker.this.mTextColor);
					ExpirationPicker.this.mMonths[i].setBackgroundResource(ExpirationPicker.this.mKeyBackgroundResId);
					ExpirationPicker.this.mMonths[i].setTag(R.id.date_keyboard, KEYBOARD_MONTH);
					ExpirationPicker.this.mMonths[i].setTag(R.id.date_month_int, i + 1);
				}
			} else if (position == EXPIRATION_YEAR_POSITION) {
				// Year
				sYearKeyboardPosition = position;
				view = this.mInflater.inflate(R.layout.keyboard, null);
				final View v1 = view.findViewById(R.id.first);
				final View v2 = view.findViewById(R.id.second);
				final View v3 = view.findViewById(R.id.third);
				final View v4 = view.findViewById(R.id.fourth);

				ExpirationPicker.this.mYearNumbers[1] = (Button) v1.findViewById(R.id.key_left);
				ExpirationPicker.this.mYearNumbers[2] = (Button) v1.findViewById(R.id.key_middle);
				ExpirationPicker.this.mYearNumbers[3] = (Button) v1.findViewById(R.id.key_right);

				ExpirationPicker.this.mYearNumbers[4] = (Button) v2.findViewById(R.id.key_left);
				ExpirationPicker.this.mYearNumbers[5] = (Button) v2.findViewById(R.id.key_middle);
				ExpirationPicker.this.mYearNumbers[6] = (Button) v2.findViewById(R.id.key_right);

				ExpirationPicker.this.mYearNumbers[7] = (Button) v3.findViewById(R.id.key_left);
				ExpirationPicker.this.mYearNumbers[8] = (Button) v3.findViewById(R.id.key_middle);
				ExpirationPicker.this.mYearNumbers[9] = (Button) v3.findViewById(R.id.key_right);

				ExpirationPicker.this.mYearLeft = (Button) v4.findViewById(R.id.key_left);
				ExpirationPicker.this.mYearLeft.setTextColor(ExpirationPicker.this.mTextColor);
				ExpirationPicker.this.mYearLeft.setBackgroundResource(ExpirationPicker.this.mKeyBackgroundResId);
				ExpirationPicker.this.mYearNumbers[0] = (Button) v4.findViewById(R.id.key_middle);
				ExpirationPicker.this.mYearRight = (Button) v4.findViewById(R.id.key_right);
				ExpirationPicker.this.mYearRight.setTextColor(ExpirationPicker.this.mTextColor);
				ExpirationPicker.this.mYearRight.setBackgroundResource(ExpirationPicker.this.mKeyBackgroundResId);

				for (int i = 0; i < 10; i++) {
					ExpirationPicker.this.mYearNumbers[i].setOnClickListener(ExpirationPicker.this);
					ExpirationPicker.this.mYearNumbers[i].setText(String.format("%d", i));
					ExpirationPicker.this.mYearNumbers[i].setTextColor(ExpirationPicker.this.mTextColor);
					ExpirationPicker.this.mYearNumbers[i].setBackgroundResource(ExpirationPicker.this.mKeyBackgroundResId);
					ExpirationPicker.this.mYearNumbers[i].setTag(R.id.date_keyboard, KEYBOARD_YEAR);
					ExpirationPicker.this.mYearNumbers[i].setTag(R.id.numbers_key, i);
				}
			} else {
				view = new View(ExpirationPicker.this.mContext);
			}
			ExpirationPicker.this.setLeftRightEnabled();
			ExpirationPicker.this.updateExpiration();
			ExpirationPicker.this.updateKeypad();
			collection.addView(view, 0);

			return view;
		}

		@Override
		public boolean isViewFromObject(final View view, final Object o) {
			return view == o;
		}
	}
	private static class SavedState extends BaseSavedState {

		int mYearInputPointer;
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
			this.mYearInputPointer = in.readInt();
			in.readIntArray(this.mYearInput);
			this.mMonthInput = in.readInt();
		}

		public SavedState(final Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(final Parcel dest, final int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(this.mYearInputPointer);
			dest.writeIntArray(this.mYearInput);
			dest.writeInt(this.mMonthInput);
		}
	}

	private final static int EXPIRATION_MONTH_POSITION = 0;
	private final static int EXPIRATION_YEAR_POSITION = 1;
	protected int mYearInputSize = 4;
	protected int mMonthInput = -1;
	protected int mYearInput[] = new int[this.mYearInputSize];
	protected int mYearInputPointer = -1;
	protected final int mCurrentYear;
	protected final Button mMonths[] = new Button[12];
	protected final Button mYearNumbers[] = new Button[10];
	protected Button mYearLeft, mYearRight;
	protected UnderlinePageIndicatorPicker mKeyboardIndicator;
	protected ViewPager mKeyboardPager;
	protected KeyboardPagerAdapter mKeyboardPagerAdapter;
	protected ImageButton mDelete;
	protected ExpirationView mEnteredExpiration;
	protected String[] mMonthAbbreviations;

	protected final Context mContext;
	private final char[] mDateFormatOrder;

	private static final String KEYBOARD_MONTH = "month";
	private static final String KEYBOARD_YEAR = "year";

	private static int sMonthKeyboardPosition = -1;

	private static int sYearKeyboardPosition = -1;
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
	 * Instantiates an ExpirationPicker object
	 *
	 * @param context the Context required for creation
	 */
	public ExpirationPicker(final Context context) {
		this(context, null);
	}

	/**
	 * Instantiates an ExpirationPicker object
	 *
	 * @param context the Context required for creation
	 * @param attrs additional attributes that define custom colors, selectors, and backgrounds.
	 */
	public ExpirationPicker(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		this.mDateFormatOrder = DateFormat.getDateFormatOrder(this.mContext);
		this.mMonthAbbreviations = DatePicker.makeLocalizedMonthAbbreviations();
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

		this.mCurrentYear = Calendar.getInstance().get(Calendar.YEAR);
	}

	private void addClickedYearNumber(final int val) {
		if (this.mYearInputPointer < (this.mYearInputSize - 1)) {
			for (int i = this.mYearInputPointer; i >= 0; i--) {
				this.mYearInput[i + 1] = this.mYearInput[i];
			}
			this.mYearInputPointer++;
			this.mYearInput[0] = val;
		}
		if (this.mKeyboardPager.getCurrentItem() < 2) {
			this.mKeyboardPager.setCurrentItem(this.mKeyboardPager.getCurrentItem() + 1, true);
		}
	}

	protected void doOnClick(final View v) {
		if (v == this.mDelete) {
			// Delete is dependent on which keyboard
			switch (this.mKeyboardPager.getCurrentItem()) {
				case EXPIRATION_MONTH_POSITION:
					if (this.mMonthInput != -1) {
						this.mMonthInput = -1;
					}
					break;
				case EXPIRATION_YEAR_POSITION:
					if (this.mYearInputPointer >= 2) {
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
		} else if (v == this.mEnteredExpiration.getMonth()) {
			this.mKeyboardPager.setCurrentItem(sMonthKeyboardPosition);
		} else if (v == this.mEnteredExpiration.getYear()) {
			this.mKeyboardPager.setCurrentItem(sYearKeyboardPosition);
		} else if (v.getTag(R.id.date_keyboard).equals(KEYBOARD_MONTH)) {
			// A month was pressed
			this.mMonthInput = (Integer) v.getTag(R.id.date_month_int);
			if (this.mKeyboardPager.getCurrentItem() < 2) {
				this.mKeyboardPager.setCurrentItem(this.mKeyboardPager.getCurrentItem() + 1, true);
			}
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
		this.mSetButton.setEnabled((this.getYear() >= this.mCurrentYear) && (this.getMonthOfYear() > 0));
	}

	protected int getLayoutId() {
		return R.layout.expiration_picker_view;
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

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		this.mDivider = this.findViewById(R.id.divider);

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

		this.mEnteredExpiration = (ExpirationView) this.findViewById(R.id.date_text);
		this.mEnteredExpiration.setTheme(this.mTheme);
		this.mEnteredExpiration.setUnderlinePage(this.mKeyboardIndicator);
		this.mEnteredExpiration.setOnClick(this);

		this.mDelete = (ImageButton) this.findViewById(R.id.delete);
		this.mDelete.setOnClickListener(this);
		this.mDelete.setOnLongClickListener(this);

		this.addClickedYearNumber(this.mCurrentYear / 1000);
		this.addClickedYearNumber((this.mCurrentYear % 1000) / 100);
		this.mKeyboardPager.setCurrentItem(this.mKeyboardPager.getCurrentItem() - 1, true);

		this.setLeftRightEnabled();
		this.updateExpiration();
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

		this.mYearInputPointer = savedState.mYearInputPointer;
		this.mYearInput = savedState.mYearInput;
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
		state.mYearInput = this.mYearInput;
		state.mYearInputPointer = this.mYearInputPointer;
		return state;
	}

	/**
	 * Reset all inputs and dates, and scroll to the first shown keyboard.
	 */
	 public void reset() {
		for (int i = 0; i < this.mYearInputSize; i++) {
			this.mYearInput[i] = 0;
		}
		this.mYearInputPointer = -1;
		this.mMonthInput = -1;
		this.mKeyboardPager.setCurrentItem(0, true);
		this.updateExpiration();
	}

	private void restyleViews() {
		for (final Button month : this.mMonths) {
			if (month != null) {
				month.setTextColor(this.mTextColor);
				month.setBackgroundResource(this.mKeyBackgroundResId);
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
		if (this.mEnteredExpiration != null) {
			this.mEnteredExpiration.setTheme(this.mTheme);
		}
	}

	/**
	 * Set the expiration shown in the date picker
	 *
	 * @param year the new year to set
	 * @param monthOfYear the new zero-indexed month to set
	 */
	 public void setExpiration(final int year, final int monthOfYear) {
		if ((year != 0) && (year < this.mCurrentYear)) {
			throw new IllegalArgumentException("Past years are not allowed. Specify " + this.mCurrentYear + " or above.");
		}

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
		for (int i = 0; i < this.mDateFormatOrder.length; i++) {
			final char c = this.mDateFormatOrder[i];
			if ((c == DateFormat.MONTH) && (monthOfYear == -1)) {
				this.mKeyboardPager.setCurrentItem(i, true);
				break;
			} else if ((c == DateFormat.YEAR) && (year <= 0)) {
				this.mKeyboardPager.setCurrentItem(i, true);
				break;
			}
		}
		this.updateKeypad();
	 }

	 protected void setLeftRightEnabled() {
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

	 /**
	  * Update the delete button to determine whether it is able to be clicked.
	  */
	 public void updateDeleteButton() {
		 final boolean enabled = (this.mMonthInput != -1) || (this.mYearInputPointer != -1);
		 if (this.mDelete != null) {
			 this.mDelete.setEnabled(enabled);
		 }
	 }

	 @SuppressLint("DefaultLocale")
	 protected void updateExpiration() {
		 String month;
		 if (this.mMonthInput < 0) {
			 month = "";
		 } else {
			 // month = mMonthAbbreviations[mMonthInput];
			 month = String.format("%02d", this.mMonthInput);
		 }
		 this.mEnteredExpiration.setExpiration(month, this.getYear());
	 }

	 private void updateKeypad() {
		 // Update state of keypad
		 // Update the number
		 this.updateExpiration();
		 // enable/disable the "set" key
		 this.enableSetButton();
		 // Update the backspace button
		 this.updateDeleteButton();
		 this.updateMonthKeys();
		 this.updateYearKeys();
	 }

	 /**
	  * Enable/disable keys on the month key pad according to the data entered
	  */
	 private void updateMonthKeys() {
		 for (int i = 0; i < this.mMonths.length; i++) {
			 if (this.mMonths[i] != null) {
				 this.mMonths[i].setEnabled(true);
			 }
		 }
	 }

	 /**
	  * Enable/disable keys on the year key pad according to the data entered
	  */
	 private void updateYearKeys() {
		 if (this.mYearInputPointer == 1) {
			 this.setYearMinKeyRange((this.mCurrentYear % 100) / 10);
		 } else if (this.mYearInputPointer == 2) {
			 this.setYearMinKeyRange(Math.max(0, (this.mCurrentYear % 100) - (this.mYearInput[0] * 10)));
		 } else if (this.mYearInputPointer == 3) {
			 this.setYearKeyRange(-1);
		 }
	 }
}
