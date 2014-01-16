package com.doomonafireball.betterpickers.numberpicker;



import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
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

import java.math.BigDecimal;

import com.doomonafireball.betterpickers.R;
public class NumberPicker extends LinearLayout implements Button.OnClickListener,
Button.OnLongClickListener {

	private static class SavedState extends BaseSavedState {

		int mInputPointer;
		int[] mInput;
		int mSign;

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
			this.mInputPointer = in.readInt();
			in.readIntArray(this.mInput);
			this.mSign = in.readInt();
		}

		public SavedState(final Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(final Parcel dest, final int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(this.mInputPointer);
			dest.writeIntArray(this.mInput);
			dest.writeInt(this.mSign);
		}
	}
	protected int mInputSize = 20;
	protected final Button mNumbers[] = new Button[10];
	protected int mInput[] = new int[this.mInputSize];
	protected int mInputPointer = -1;
	protected Button mLeft, mRight;
	protected ImageButton mDelete;
	protected NumberView mEnteredNumber;

	protected final Context mContext;
	private TextView mLabel;
	private NumberPickerErrorTextView mError;
	private int mSign;
	private String mLabelText = "";
	private Button mSetButton;

	private static final int CLICKED_DECIMAL = 10;
	private static final int SIGN_POSITIVE = 0;

	private static final int SIGN_NEGATIVE = 1;
	protected View mDivider;
	private ColorStateList mTextColor;
	private int mKeyBackgroundResId;
	private int mButtonBackgroundResId;
	private int mDividerColor;
	private int mDeleteDrawableSrcResId;

	private int mTheme = -1;
	private Integer mMinNumber = null;

	private Integer mMaxNumber = null;

	/**
	 * Instantiates a NumberPicker object
	 *
	 * @param context the Context required for creation
	 */
	public NumberPicker(final Context context) {
		this(context, null);
	}

	/**
	 * Instantiates a NumberPicker object
	 *
	 * @param context the Context required for creation
	 * @param attrs additional attributes that define custom colors, selectors, and backgrounds.
	 */
	public NumberPicker(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		final LayoutInflater layoutInflater =
				(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(this.getLayoutId(), this);

		// Init defaults
		this.mTextColor = this.getResources().getColorStateList(R.color.dialog_text_color_holo_dark);
		this.mKeyBackgroundResId = R.drawable.key_background_dark;
		this.mButtonBackgroundResId = R.drawable.button_background_dark;
		this.mDeleteDrawableSrcResId = R.drawable.ic_backspace_dark;
		this.mDividerColor = this.getResources().getColor(R.color.default_divider_color_dark);
	}

	private void addClickedNumber(final int val) {
		if (this.mInputPointer < (this.mInputSize - 1)) {
			// For 0 we need to check if we have a value of zero or not
			if ((this.mInput[0] == 0) && (this.mInput[1] == -1) && !this.containsDecimal() && (val != CLICKED_DECIMAL)) {
				this.mInput[0] = val;
			} else {
				for (int i = this.mInputPointer; i >= 0; i--) {
					this.mInput[i + 1] = this.mInput[i];
				}
				this.mInputPointer++;
				this.mInput[0] = val;
			}
		}
	}

	/**
	 * Checks if the user allowed to click on the right button.
	 *
	 * @return true or false if the user is able to add a decimal or not
	 */
	private boolean canAddDecimal() {
		return !this.containsDecimal();
	}

	private boolean containsDecimal() {
		boolean containsDecimal = false;
		for (final int i : this.mInput) {
			if (i == 10) {
				containsDecimal = true;
			}
		}
		return containsDecimal;
	}

	protected void doOnClick(final View v) {
		final Integer val = (Integer) v.getTag(R.id.numbers_key);
		if (val != null) {
			// A number was pressed
			this.addClickedNumber(val);
		} else if (v == this.mDelete) {
			if (this.mInputPointer >= 0) {
				for (int i = 0; i < this.mInputPointer; i++) {
					this.mInput[i] = this.mInput[i + 1];
				}
				this.mInput[this.mInputPointer] = -1;
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

		// If the user entered 1 digits or more
		this.mSetButton.setEnabled(this.mInputPointer >= 0);
	}

	/**
	 * Returns the decimal following the number
	 *
	 * @return a double representation of the decimal value
	 */
	public double getDecimal() {
		final double decimal = BigDecimal.valueOf(this.getEnteredNumber()).divideAndRemainder(BigDecimal.ONE)[1].doubleValue();
		return decimal;
	}

	/**
	 * Returns the number inputted by the user
	 *
	 * @return a double representing the entered number
	 */
	public double getEnteredNumber() {
		String value = "0";
		for (int i = this.mInputPointer; i >= 0; i--) {
			if (this.mInput[i] == -1) {
				break;
			} else if (this.mInput[i] == CLICKED_DECIMAL) {
				value += ".";
			} else {
				value += this.mInput[i];
			}
		}
		if (this.mSign == SIGN_NEGATIVE) {
			value = "-" + value;
		}
		return Double.parseDouble(value);
	}

	private String getEnteredNumberString() {
		String value = "";
		for (int i = this.mInputPointer; i >= 0; i--) {
			if (this.mInput[i] == -1) {
				// Don't add
			} else if (this.mInput[i] == CLICKED_DECIMAL) {
				value += ".";
			} else {
				value += this.mInput[i];
			}
		}
		return value;
	}

	/**
	 * Expose the NumberView in order to set errors
	 *
	 * @return the NumberView
	 */
	public NumberPickerErrorTextView getErrorView() {
		return this.mError;
	}

	/**
	 * Returns whether the number is positive or negative
	 *
	 * @return true or false whether the number is positive or negative
	 */
	public boolean getIsNegative() {
		return this.mSign == SIGN_NEGATIVE;
	}

	protected int getLayoutId() {
		return R.layout.number_picker_view;
	}

	/**
	 * Returns the number as currently inputted by the user
	 *
	 * @return an int representation of the number with no decimal
	 */
	public int getNumber() {
		final String numberString = Double.toString(this.getEnteredNumber());
		final String[] split = numberString.split("\\.");
		return Integer.parseInt(split[0]);
	}

	@Override
	public void onClick(final View v) {
		v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		this.mError.hideImmediately();
		this.doOnClick(v);
		this.updateDeleteButton();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		this.mDivider = this.findViewById(R.id.divider);
		this.mError = (NumberPickerErrorTextView) this.findViewById(R.id.error);

		for (int i = 0; i < this.mInput.length; i++) {
			this.mInput[i] = -1;
		}

		final View v1 = this.findViewById(R.id.first);
		final View v2 = this.findViewById(R.id.second);
		final View v3 = this.findViewById(R.id.third);
		final View v4 = this.findViewById(R.id.fourth);
		this.mEnteredNumber = (NumberView) this.findViewById(R.id.number_text);
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
		this.setLeftRightEnabled();

		for (int i = 0; i < 10; i++) {
			this.mNumbers[i].setOnClickListener(this);
			this.mNumbers[i].setText(String.format("%d", i));
			this.mNumbers[i].setTag(R.id.numbers_key, new Integer(i));
		}
		this.updateNumber();

		final Resources res = this.mContext.getResources();
		this.mLeft.setText(res.getString(R.string.number_picker_plus_minus));
		this.mRight.setText(res.getString(R.string.number_picker_seperator));
		this.mLeft.setOnClickListener(this);
		this.mRight.setOnClickListener(this);
		this.mLabel = (TextView) this.findViewById(R.id.label);
		this.mSign = SIGN_POSITIVE;

		// Set the correct label state
		this.showLabel();

		this.restyleViews();
		this.updateKeypad();
	}

	/**
	 * Clicking on the bottom left button will toggle the sign.
	 */
	private void onLeftClicked() {
		if (this.mSign == SIGN_POSITIVE) {
			this.mSign = SIGN_NEGATIVE;
		} else {
			this.mSign = SIGN_POSITIVE;
		}
	}

	@Override
	public boolean onLongClick(final View v) {
		v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
		this.mError.hideImmediately();
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

		this.mInputPointer = savedState.mInputPointer;
		this.mInput = savedState.mInput;
		if (this.mInput == null) {
			this.mInput = new int[this.mInputSize];
			this.mInputPointer = -1;
		}
		this.mSign = savedState.mSign;
		this.updateKeypad();
	}

	/**
	 * Clicking on the bottom right button will add a decimal point.
	 */
	 private void onRightClicked() {
		if (this.canAddDecimal()) {
			this.addClickedNumber(CLICKED_DECIMAL);
		}
	 }

	 @Override
	 public Parcelable onSaveInstanceState() {
		 final Parcelable parcel = super.onSaveInstanceState();
		 final SavedState state = new SavedState(parcel);
		 state.mInput = this.mInput;
		 state.mSign = this.mSign;
		 state.mInputPointer = this.mInputPointer;
		 return state;
	 }

	 /**
	  * Reset all inputs.
	  */
	 public void reset() {
		 for (int i = 0; i < this.mInputSize; i++) {
			 this.mInput[i] = -1;
		 }
		 this.mInputPointer = -1;
		 this.updateNumber();
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
		 if (this.mRight != null) {
			 this.mRight.setTextColor(this.mTextColor);
			 this.mRight.setBackgroundResource(this.mKeyBackgroundResId);
		 }
		 if (this.mDelete != null) {
			 this.mDelete.setBackgroundResource(this.mButtonBackgroundResId);
			 this.mDelete.setImageDrawable(this.getResources().getDrawable(this.mDeleteDrawableSrcResId));
		 }
		 if (this.mEnteredNumber != null) {
			 this.mEnteredNumber.setTheme(this.mTheme);
		 }
		 if (this.mLabel != null) {
			 this.mLabel.setTextColor(this.mTextColor);
		 }
	 }

	 /**
	  * Using View.GONE, View.VISIBILE, or View.INVISIBLE, set the visibility of the decimal indicator
	  *
	  * @param visiblity an int using Android's View.* convention
	  */
	 public void setDecimalVisibility(final int visiblity) {
		 if (this.mRight != null) {
			 this.mRight.setVisibility(visiblity);
		 }
	 }

	 /**
	  * Set the text displayed in the small label
	  *
	  * @param labelText the String to set as the label
	  */
	 public void setLabelText(final String labelText) {
		 this.mLabelText = labelText;
		 this.showLabel();
	 }

	 protected void setLeftRightEnabled() {
		 this.mLeft.setEnabled(true);
		 this.mRight.setEnabled(this.canAddDecimal());
		 if (!this.canAddDecimal()) {
			 this.mRight.setContentDescription(null);
		 }
	 }

	 /**
	  * Set a maximum required number
	  *
	  * @param max the maximum required number
	  */
	 public void setMax(final int max) {
		 this.mMaxNumber = max;
	 }

	 /**
	  * Set a minimum required number
	  *
	  * @param min the minimum required number
	  */
	 public void setMin(final int min) {
		 this.mMinNumber = min;
	 }

	 /**
	  * Using View.GONE, View.VISIBILE, or View.INVISIBLE, set the visibility of the plus/minus indicator
	  *
	  * @param visiblity an int using Android's View.* convention
	  */
	 public void setPlusMinusVisibility(final int visiblity) {
		 if (this.mLeft != null) {
			 this.mLeft.setVisibility(visiblity);
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

	 private void showLabel() {
		 if (this.mLabel != null) {
			 this.mLabel.setText(this.mLabelText);
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
		 // Update the number
		 this.updateLeftRightButtons();
		 this.updateNumber();
		 // enable/disable the "set" key
		 this.enableSetButton();
		 // Update the backspace button
		 this.updateDeleteButton();
	 }

	 private void updateLeftRightButtons() {
		 this.mRight.setEnabled(this.canAddDecimal());
	 }

	 // Update the number displayed in the picker:
	 protected void updateNumber() {
		 String numberString = this.getEnteredNumberString();
		 numberString = numberString.replaceAll("\\-", "");
		 final String[] split = numberString.split("\\.");
		 if (split.length >= 2) {
			 if (split[0].equals("")) {
				 this.mEnteredNumber.setNumber("0", split[1], this.containsDecimal(),
						 this.mSign == SIGN_NEGATIVE);
			 } else {
				 this.mEnteredNumber.setNumber(split[0], split[1], this.containsDecimal(),
						 this.mSign == SIGN_NEGATIVE);
			 }
		 } else if (split.length == 1) {
			 this.mEnteredNumber.setNumber(split[0], "", this.containsDecimal(),
					 this.mSign == SIGN_NEGATIVE);
		 } else if (numberString.equals(".")) {
			 this.mEnteredNumber.setNumber("0", "", true, this.mSign == SIGN_NEGATIVE);
		 }
	 }
}
