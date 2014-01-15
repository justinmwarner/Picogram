package com.doomonafireball.betterpickers.timepicker;



import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormatSymbols;

import com.doomonafireball.betterpickers.R;
public class TimePicker extends LinearLayout implements Button.OnClickListener, Button.OnLongClickListener {

	private static class SavedState extends BaseSavedState {

		int mInputPointer;
		int[] mInput;
		int mAmPmState;

		public static final Parcelable.Creator<SavedState> CREATOR
		= new Parcelable.Creator<SavedState>() {
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
			this.mInputPointer = in.readInt();
			in.readIntArray(this.mInput);
			this.mAmPmState = in.readInt();
		}

		public SavedState(final Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(final Parcel dest, final int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(this.mInputPointer);
			dest.writeIntArray(this.mInput);
			dest.writeInt(this.mAmPmState);
		}
	}
	protected int mInputSize = 4;
	protected final Button mNumbers[] = new Button[10];
	protected int mInput[] = new int[this.mInputSize];
	protected int mInputPointer = -1;
	protected Button mLeft, mRight;
	protected ImageButton mDelete;
	protected TimerView mEnteredTime;

	protected final Context mContext;
	private TextView mAmPmLabel;
	private String[] mAmpm;
	private final String mNoAmPmLabel;
	private int mAmPmState;
	private Button mSetButton;

	private boolean mIs24HoursMode = false;
	private static final int AMPM_NOT_SELECTED = 0;
	private static final int PM_SELECTED = 1;
	private static final int AM_SELECTED = 2;

	private static final int HOURS24_MODE = 3;
	private static final String TIME_PICKER_SAVED_BUFFER_POINTER =
			"timer_picker_saved_buffer_pointer";
	private static final String TIME_PICKER_SAVED_INPUT = "timer_picker_saved_input";

	private static final String TIME_PICKER_SAVED_AMPM = "timer_picker_saved_ampm";
	/**
	 * Return whether it is currently 24-hour mode on the system
	 *
	 * @param context a required Context
	 * @return true or false whether it is 24-hour mode or not
	 */
	public static boolean get24HourMode(final Context context) {
		return android.text.format.DateFormat.is24HourFormat(context);
	}
	protected View mDivider;
	private ColorStateList mTextColor;
	private int mKeyBackgroundResId;
	private int mButtonBackgroundResId;
	private int mDividerColor;

	private int mDeleteDrawableSrcResId;

	private int mTheme = -1;

	/**
	 * Instantiates a TimePicker object
	 *
	 * @param context the Context required for creation
	 */
	public TimePicker(final Context context) {
		this(context, null);
	}

	/**
	 * Instantiates a TimePicker object
	 *
	 * @param context the Context required for creation
	 * @param attrs additional attributes that define custom colors, selectors, and backgrounds.
	 */
	public TimePicker(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		this.mIs24HoursMode = get24HourMode(this.mContext);
		final LayoutInflater layoutInflater =
				(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(this.getLayoutId(), this);
		this.mNoAmPmLabel = context.getResources().getString(R.string.time_picker_ampm_label);

		// Init defaults
		this.mTextColor = this.getResources().getColorStateList(R.color.dialog_text_color_holo_dark);
		this.mKeyBackgroundResId = R.drawable.key_background_dark;
		this.mButtonBackgroundResId = R.drawable.button_background_dark;
		this.mDividerColor = this.getResources().getColor(R.color.default_divider_color_dark);
		this.mDeleteDrawableSrcResId = R.drawable.ic_backspace_dark;
	}

	private void addClickedNumber(final int val) {
		if (this.mInputPointer < (this.mInputSize - 1)) {
			for (int i = this.mInputPointer; i >= 0; i--) {
				this.mInput[i + 1] = this.mInput[i];
			}
			this.mInputPointer++;
			this.mInput[0] = val;
		}
	}

	/**
	 * Checks if the user allowed to click on the left or right button that enters "00" or "30"
	 *
	 * @return true or false whether a user is allowed to click on the left or right
	 */
	private boolean canAddDigits() {
		final int time = this.getEnteredTime();
		// For AM/PM mode , can add "00" if an hour between 1 and 12 was entered
		if (!this.mIs24HoursMode) {
			return ((time >= 1) && (time <= 12));
		}
		// For 24 hours mode , can add "00"/"30" if an hour between 0 and 23 was entered
		return ((time >= 0) && (time <= 23) && (this.mInputPointer > -1) && (this.mInputPointer < 2));
	}

	protected void doOnClick(final View v) {
		final Integer val = (Integer) v.getTag(R.id.numbers_key);
		// A number was pressed
		if (val != null) {
			this.addClickedNumber(val);
		} else if (v == this.mDelete) {
			// Pressing delete when AM or PM is selected, clears the AM/PM
			// selection
			if (!this.mIs24HoursMode && (this.mAmPmState != AMPM_NOT_SELECTED)) {
				this.mAmPmState = AMPM_NOT_SELECTED;
			} else if (this.mInputPointer >= 0) {
				for (int i = 0; i < this.mInputPointer; i++) {
					this.mInput[i] = this.mInput[i + 1];
				}
				this.mInput[this.mInputPointer] = 0;
				this.mInputPointer--;
			}
		} else if (v == this.mLeft) {
			this.onLeftClicked();
		} else if (v == this.mRight) {
			this.onRightClicked();
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

		// Nothing entered - disable
		if (this.mInputPointer == -1) {
			this.mSetButton.setEnabled(false);
			return;
		}
		// If the user entered 3 digits or more but not 060 to 095
		// it is a legal time and the set key should be enabled.
		if (this.mIs24HoursMode) {
			final int time = this.getEnteredTime();
			this.mSetButton.setEnabled((this.mInputPointer >= 2) && ((time < 60) || (time > 95)));
		} else {
			// If AM/PM mode , enable the set button if AM/PM was selected
			this.mSetButton.setEnabled(this.mAmPmState != AMPM_NOT_SELECTED);
		}
	}

	/**
	 * Returns the time already entered in decimal representation. if time is H1 H2 : M1 M2 the value retured is
	 * H1*1000+H2*100+M1*10+M2
	 *
	 * @return the time already entered in decimal representation
	 */
	private int getEnteredTime() {
		return (this.mInput[3] * 1000) + (this.mInput[2] * 100) + (this.mInput[1] * 10) + this.mInput[0];
	}

	/**
	 * Get the hours as currently inputted by the user.
	 *
	 * @return the inputted hours
	 */
	public int getHours() {
		final int hours = (this.mInput[3] * 10) + this.mInput[2];
		if (hours == 12) {
			switch (this.mAmPmState) {
				case PM_SELECTED:
					return 12;
				case AM_SELECTED:
					return 0;
				case HOURS24_MODE:
					return hours;
				default:
					break;
			}
		}
		return hours + (this.mAmPmState == PM_SELECTED ? 12 : 0);
	}

	protected int getLayoutId() {
		return R.layout.time_picker_view;
	}

	/**
	 * Get the minutes as currently inputted by the user
	 *
	 * @return the inputted minutes
	 */
	public int getMinutes() {
		return (this.mInput[1] * 10) + this.mInput[0];
	}

	/**
	 * Get the time currently inputted by the user
	 *
	 * @return an int representing the current time
	 */
	public int getTime() {
		return (this.mInput[4] * 3600) + (this.mInput[3] * 600) + (this.mInput[2] * 60) + (this.mInput[1] * 10) + this.mInput[0];
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

		final View v1 = this.findViewById(R.id.first);
		final View v2 = this.findViewById(R.id.second);
		final View v3 = this.findViewById(R.id.third);
		final View v4 = this.findViewById(R.id.fourth);
		this.mEnteredTime = (TimerView) this.findViewById(R.id.timer_time_text);
		this.mDelete = (ImageButton) this.findViewById(R.id.delete);
		this.mDelete.setOnClickListener(this);
		this.mDelete.setOnLongClickListener(this);

		this.mNumbers[1] = (Button) v1.findViewById(R.id.key_left);
		this.mNumbers[2] = (Button) v1.findViewById(R.id.key_middle);
		this.mNumbers[3] = (Button) v1.findViewById(R.id.key_right);

		this.mNumbers[4] = (Button) v2.findViewById(R.id.key_left);
		this.mNumbers[5] = (Button) v2.findViewById(R.id.key_middle);
		this.mNumbers[6] = (Button) v2.findViewById(R.id.key_right);

		this.mNumbers[7] = (Button) v3.findViewById(R.id.key_left);
		this.mNumbers[8] = (Button) v3.findViewById(R.id.key_middle);
		this.mNumbers[9] = (Button) v3.findViewById(R.id.key_right);

		this.mLeft = (Button) v4.findViewById(R.id.key_left);
		this.mNumbers[0] = (Button) v4.findViewById(R.id.key_middle);
		this.mRight = (Button) v4.findViewById(R.id.key_right);
		this.setLeftRightEnabled(false);

		for (int i = 0; i < 10; i++) {
			this.mNumbers[i].setOnClickListener(this);
			this.mNumbers[i].setText(String.format("%d", i));
			this.mNumbers[i].setTag(R.id.numbers_key, new Integer(i));
		}
		this.updateTime();

		final Resources res = this.mContext.getResources();
		this.mAmpm = new DateFormatSymbols().getAmPmStrings();

		if (this.mIs24HoursMode) {
			this.mLeft.setText(res.getString(R.string.time_picker_00_label));
			this.mRight.setText(res.getString(R.string.time_picker_30_label));
		} else {
			this.mLeft.setText(this.mAmpm[0]);
			this.mRight.setText(this.mAmpm[1]);
		}
		this.mLeft.setOnClickListener(this);
		this.mRight.setOnClickListener(this);
		this.mAmPmLabel = (TextView) this.findViewById(R.id.ampm_label);
		this.mAmPmState = AMPM_NOT_SELECTED;
		this.mDivider = this.findViewById(R.id.divider);

		this.restyleViews();
		this.updateKeypad();
	}

	/**
	 * Clicking on the bottom left button will add "00" to the time
	 *
	 * In AM/PM mode is will also set the time to AM.
	 */
	private void onLeftClicked() {
		final int time = this.getEnteredTime();
		if (!this.mIs24HoursMode) {
			if (this.canAddDigits()) {
				this.addClickedNumber(0);
				this.addClickedNumber(0);
			}
			this.mAmPmState = AM_SELECTED;
		} else if (this.canAddDigits()) {
			this.addClickedNumber(0);
			this.addClickedNumber(0);
		}
	}

	@Override
	public boolean onLongClick(final View v) {
		v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
		if (v == this.mDelete) {
			this.mDelete.setPressed(false);

			this.mAmPmState = AMPM_NOT_SELECTED;
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

		this.mInputPointer = savedState.mInputPointer;
		this.mInput = savedState.mInput;
		if (this.mInput == null) {
			this.mInput = new int[this.mInputSize];
			this.mInputPointer = -1;
		}
		this.mAmPmState = savedState.mAmPmState;
		this.updateKeypad();
	}

	/**
	 * Clicking on the bottom right button will add "00" to the time in AM/PM mode and "30" is 24 hours mode.
	 *
	 * In AM/PM mode is will also set the time to PM.
	 */
	 private void onRightClicked() {
		 final int time = this.getEnteredTime();
		 if (!this.mIs24HoursMode) {
			 if (this.canAddDigits()) {
				 this.addClickedNumber(0);
				 this.addClickedNumber(0);
			 }
			 this.mAmPmState = PM_SELECTED;
		 } else {
			 if (this.canAddDigits()) {
				 this.addClickedNumber(3);
				 this.addClickedNumber(0);
			 }
		 }
	 }

	 @Override
	 public Parcelable onSaveInstanceState() {
		 final Parcelable parcel = super.onSaveInstanceState();
		 final SavedState state = new SavedState(parcel);
		 state.mInput = this.mInput;
		 state.mAmPmState = this.mAmPmState;
		 state.mInputPointer = this.mInputPointer;
		 return state;
	 }

	 /**
	  * Reset all inputs .
	  */
	 public void reset() {
		 for (int i = 0; i < this.mInputSize; i++) {
			 this.mInput[i] = 0;
		 }
		 this.mInputPointer = -1;
		 this.updateTime();
	 }

	 public void restoreEntryState(final Bundle inState, final String key) {
		 final int[] input = inState.getIntArray(key);
		 if ((input != null) && (this.mInputSize == input.length)) {
			 for (int i = 0; i < this.mInputSize; i++) {
				 this.mInput[i] = input[i];
				 if (this.mInput[i] != 0) {
					 this.mInputPointer = i;
				 }
			 }
			 this.updateTime();
		 }
	 }

	 private void restyleViews() {
		 for (final Button number : this.mNumbers) {
			 if (number != null) {
				 number.setTextColor(this.mTextColor);
				 number.setBackgroundResource(this.mKeyBackgroundResId);
			 }
		 }
		 if (this.mDivider != null) {
			 this.mDivider.setBackgroundColor(this.mDividerColor);
		 }
		 if (this.mLeft != null) {
			 this.mLeft.setTextColor(this.mTextColor);
			 this.mLeft.setBackgroundResource(this.mKeyBackgroundResId);
		 }
		 if (this.mAmPmLabel != null) {
			 this.mAmPmLabel.setTextColor(this.mTextColor);
			 this.mAmPmLabel.setBackgroundResource(this.mKeyBackgroundResId);
		 }
		 if (this.mRight != null) {
			 this.mRight.setTextColor(this.mTextColor);
			 this.mRight.setBackgroundResource(this.mKeyBackgroundResId);
		 }
		 if (this.mDelete != null) {
			 this.mDelete.setBackgroundResource(this.mButtonBackgroundResId);
			 this.mDelete.setImageDrawable(this.getResources().getDrawable(this.mDeleteDrawableSrcResId));
		 }
		 if (this.mEnteredTime != null) {
			 this.mEnteredTime.setTheme(this.mTheme);
		 }
	 }

	 public void saveEntryState(final Bundle outState, final String key) {
		 outState.putIntArray(key, this.mInput);
	 }

	 /**
	  * enables a range of numeric keys from zero to maxKey. The rest of the keys will be disabled
	  *
	  * @param maxKey the maximum key that can be pressed
	  */
	  private void setKeyRange(final int maxKey) {
		 for (int i = 0; i < this.mNumbers.length; i++) {
			 this.mNumbers[i].setEnabled(i <= maxKey);
		 }
	  }

	  protected void setLeftRightEnabled(final boolean enabled) {
		  this.mLeft.setEnabled(enabled);
		  this.mRight.setEnabled(enabled);
		  if (!enabled) {
			  this.mLeft.setContentDescription(null);
			  this.mRight.setContentDescription(null);
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
			  this.mDividerColor = a.getColor(R.styleable.BetterPickersDialogFragment_bpDividerColor, this.mDividerColor);
			  this.mDeleteDrawableSrcResId = a.getResourceId(R.styleable.BetterPickersDialogFragment_bpDeleteIcon,
					  this.mDeleteDrawableSrcResId);
		  }

		  this.restyleViews();
	  }

	  private void showAmPm() {
		  if (!this.mIs24HoursMode) {
			  switch (this.mAmPmState) {
				  case AMPM_NOT_SELECTED:
					  this.mAmPmLabel.setText(this.mNoAmPmLabel);
					  break;
				  case AM_SELECTED:
					  this.mAmPmLabel.setText(this.mAmpm[0]);
					  break;
				  case PM_SELECTED:
					  this.mAmPmLabel.setText(this.mAmpm[1]);
					  break;
				  default:
					  break;
			  }
		  } else {
			  this.mAmPmLabel.setVisibility(View.INVISIBLE);
			  this.mAmPmState = HOURS24_MODE;
		  }
	  }

	  /**
	   * Update the delete button to determine whether it is able to be clicked.
	   */
	   public void updateDeleteButton() {
		  final boolean enabled = this.mInputPointer != -1;
		  if (this.mDelete != null) {
			  this.mDelete.setEnabled(enabled);
		  }
	   }

	   private void updateKeypad() {
		   // Update state of keypad
		   // Set the correct AM/PM state
		   this.showAmPm();
		   // Update the time
		   this.updateLeftRightButtons();
		   this.updateTime();
		   // enable/disable numeric keys according to the numbers entered already
		   this.updateNumericKeys();
		   // enable/disable the "set" key
		   this.enableSetButton();
		   // Update the backspace button
		   this.updateDeleteButton();

	   }

	   private void updateLeftRightButtons() {
		   final int time = this.getEnteredTime();
		   if (this.mIs24HoursMode) {
			   final boolean enable = this.canAddDigits();
			   this.mLeft.setEnabled(enable);
			   this.mRight.setEnabled(enable);
		   } else {
			   // You can use the AM/PM if time entered is 0 to 12 or it is 3 digits or more
			   if (((time > 12) && (time < 100)) || (time == 0) || (this.mAmPmState != AMPM_NOT_SELECTED)) {
				   this.mLeft.setEnabled(false);
				   this.mRight.setEnabled(false);
			   } else {
				   this.mLeft.setEnabled(true);
				   this.mRight.setEnabled(true);
			   }
		   }
	   }

	   /**
	    * Enable/disable keys in the numeric key pad according to the data entered
	    */
	   private void updateNumericKeys() {
		   final int time = this.getEnteredTime();
		   if (this.mIs24HoursMode) {
			   if (this.mInputPointer >= 3) {
				   this.setKeyRange(-1);
			   } else if (time == 0) {
				   if ((this.mInputPointer == -1) || (this.mInputPointer == 0) || (this.mInputPointer == 2)) {
					   this.setKeyRange(9);
				   } else if (this.mInputPointer == 1) {
					   this.setKeyRange(5);
				   } else {
					   this.setKeyRange(-1);
				   }
			   } else if (time == 1) {
				   if ((this.mInputPointer == 0) || (this.mInputPointer == 2)) {
					   this.setKeyRange(9);
				   } else if (this.mInputPointer == 1) {
					   this.setKeyRange(5);
				   } else {
					   this.setKeyRange(-1);
				   }
			   } else if (time == 2) {
				   if ((this.mInputPointer == 2) || (this.mInputPointer == 1)) {
					   this.setKeyRange(9);
				   } else if (this.mInputPointer == 0) {
					   this.setKeyRange(3);
				   } else {
					   this.setKeyRange(-1);
				   }
			   } else if (time <= 5) {
				   this.setKeyRange(9);
			   } else if (time <= 9) {
				   this.setKeyRange(5);
			   } else if ((time >= 10) && (time <= 15)) {
				   this.setKeyRange(9);
			   } else if ((time >= 16) && (time <= 19)) {
				   this.setKeyRange(5);
			   } else if ((time >= 20) && (time <= 25)) {
				   this.setKeyRange(9);
			   } else if ((time >= 26) && (time <= 29)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 30) && (time <= 35)) {
				   this.setKeyRange(9);
			   } else if ((time >= 36) && (time <= 39)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 40) && (time <= 45)) {
				   this.setKeyRange(9);
			   } else if ((time >= 46) && (time <= 49)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 50) && (time <= 55)) {
				   this.setKeyRange(9);
			   } else if ((time >= 56) && (time <= 59)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 60) && (time <= 65)) {
				   this.setKeyRange(9);
			   } else if ((time >= 70) && (time <= 75)) {
				   this.setKeyRange(9);
			   } else if ((time >= 80) && (time <= 85)) {
				   this.setKeyRange(9);
			   } else if ((time >= 90) && (time <= 95)) {
				   this.setKeyRange(9);
			   } else if ((time >= 100) && (time <= 105)) {
				   this.setKeyRange(9);
			   } else if ((time >= 106) && (time <= 109)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 110) && (time <= 115)) {
				   this.setKeyRange(9);
			   } else if ((time >= 116) && (time <= 119)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 120) && (time <= 125)) {
				   this.setKeyRange(9);
			   } else if ((time >= 126) && (time <= 129)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 130) && (time <= 135)) {
				   this.setKeyRange(9);
			   } else if ((time >= 136) && (time <= 139)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 140) && (time <= 145)) {
				   this.setKeyRange(9);
			   } else if ((time >= 146) && (time <= 149)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 150) && (time <= 155)) {
				   this.setKeyRange(9);
			   } else if ((time >= 156) && (time <= 159)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 160) && (time <= 165)) {
				   this.setKeyRange(9);
			   } else if ((time >= 166) && (time <= 169)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 170) && (time <= 175)) {
				   this.setKeyRange(9);
			   } else if ((time >= 176) && (time <= 179)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 180) && (time <= 185)) {
				   this.setKeyRange(9);
			   } else if ((time >= 186) && (time <= 189)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 190) && (time <= 195)) {
				   this.setKeyRange(9);
			   } else if ((time >= 196) && (time <= 199)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 200) && (time <= 205)) {
				   this.setKeyRange(9);
			   } else if ((time >= 206) && (time <= 209)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 210) && (time <= 215)) {
				   this.setKeyRange(9);
			   } else if ((time >= 216) && (time <= 219)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 220) && (time <= 225)) {
				   this.setKeyRange(9);
			   } else if ((time >= 226) && (time <= 229)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 230) && (time <= 235)) {
				   this.setKeyRange(9);
			   } else if (time >= 236) {
				   this.setKeyRange(-1);
			   }
		   } else {
			   // Selecting AM/PM disabled the keypad
			   if (this.mAmPmState != AMPM_NOT_SELECTED) {
				   this.setKeyRange(-1);
			   } else if (time == 0) {
				   this.setKeyRange(9);
				   // If 0 was entered as the first digit in AM/PM mode, do not allow a second 0
				   //        if (mInputPointer == 0) {
					   this.mNumbers[0].setEnabled(false);
					   //      }
			   } else if (time <= 9) {
				   this.setKeyRange(5);
			   } else if (time <= 95) {
				   this.setKeyRange(9);
			   } else if ((time >= 100) && (time <= 105)) {
				   this.setKeyRange(9);
			   } else if ((time >= 106) && (time <= 109)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 110) && (time <= 115)) {
				   this.setKeyRange(9);
			   } else if ((time >= 116) && (time <= 119)) {
				   this.setKeyRange(-1);
			   } else if ((time >= 120) && (time <= 125)) {
				   this.setKeyRange(9);
			   } else if (time >= 126) {
				   this.setKeyRange(-1);
			   }
		   }
	   }

	   /**
	    * Update the time displayed in the picker:
	    *
	    * Special cases:
	    *
	    * 1. show "-" for digits not entered yet.
	    *
	    * 2. hide the hours digits when it is not relevant
	    */
	   protected void updateTime() {
		   // Put "-" in digits that was not entered by passing -1
		   // Hide digit by passing -2 (for highest hours digit only);

		   int hours1 = -1;
		   final int time = this.getEnteredTime();
		   // If the user entered 2 to 9 or 13 to 15 , there is no need for a 4th digit (AM/PM mode)
		   // If the user entered 3 to 9 or 24 to 25 , there is no need for a 4th digit (24 hours mode)
		   if (this.mInputPointer > -1) {
			   // Test to see if the highest digit is 2 to 9 for AM/PM or 3 to 9 for 24 hours mode
			   if (this.mInputPointer >= 0) {
				   final int digit = this.mInput[this.mInputPointer];
				   if ((this.mIs24HoursMode && (digit >= 3) && (digit <= 9)) ||
						   (!this.mIs24HoursMode && (digit >= 2) && (digit <= 9))) {
					   hours1 = -2;
				   }
			   }
			   // Test to see if the 2 highest digits are 13 to 15 for AM/PM or 24 to 25 for 24 hours
			   // mode
			   if ((this.mInputPointer > 0) && (this.mInputPointer < 3) && (hours1 != -2)) {
				   final int digits = (this.mInput[this.mInputPointer] * 10) + this.mInput[this.mInputPointer - 1];
				   if ((this.mIs24HoursMode && (digits >= 24) && (digits <= 25)) ||
						   (!this.mIs24HoursMode && (digits >= 13) && (digits <= 15))) {
					   hours1 = -2;
				   }
			   }
			   // If we have a digit show it
			   if (this.mInputPointer == 3) {
				   hours1 = this.mInput[3];
			   }
		   } else {
			   hours1 = -1;
		   }
		   final int hours2 = (this.mInputPointer < 2) ? -1 : this.mInput[2];
		   final int minutes1 = (this.mInputPointer < 1) ? -1 : this.mInput[1];
		   final int minutes2 = (this.mInputPointer < 0) ? -1 : this.mInput[0];
		   this.mEnteredTime.setTime(hours1, hours2, minutes1, minutes2);
	   }
}
