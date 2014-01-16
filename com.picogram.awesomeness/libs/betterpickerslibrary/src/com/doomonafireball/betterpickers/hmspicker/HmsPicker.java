package com.doomonafireball.betterpickers.hmspicker;



import android.content.Context;
import android.content.res.ColorStateList;
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

import com.doomonafireball.betterpickers.R;

public class HmsPicker extends LinearLayout implements Button.OnClickListener, Button.OnLongClickListener {

	private static class SavedState extends BaseSavedState {

		int mInputPointer;
		int[] mInput;
		int mAmPmState;

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
	protected int mInputSize = 5;
	protected final Button mNumbers[] = new Button[10];
	protected int mInput[] = new int[this.mInputSize];
	protected int mInputPointer = -1;
	protected ImageButton mDelete;
	protected Button mLeft, mRight;
	protected HmsView mEnteredHms;

	protected final Context mContext;
	private TextView mHoursLabel, mMinutesLabel, mSecondsLabel;

	private Button mSetButton;
	protected View mDivider;
	private ColorStateList mTextColor;
	private int mKeyBackgroundResId;
	private int mButtonBackgroundResId;
	private int mDividerColor;
	private int mDeleteDrawableSrcResId;

	private int mTheme = -1;

	/**
	 * Instantiates an HmsPicker object
	 *
	 * @param context the Context required for creation
	 */
	 public HmsPicker(final Context context) {
		 this(context, null);
	 }

	/**
	 * Instantiates an HmsPicker object
	 *
	 * @param context the Context required for creation
	 * @param attrs additional attributes that define custom colors, selectors, and backgrounds.
	 */
	 public HmsPicker(final Context context, final AttributeSet attrs) {
		 super(context, attrs);
		 this.mContext = context;
		 final LayoutInflater layoutInflater =
				 (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		 layoutInflater.inflate(this.getLayoutId(), this);

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

	 protected void doOnClick(final View v) {
		 final Integer val = (Integer) v.getTag(R.id.numbers_key);
		 // A number was pressed
		 if (val != null) {
			 this.addClickedNumber(val);
		 } else if (v == this.mDelete) {
			 if (this.mInputPointer >= 0) {
				 for (int i = 0; i < this.mInputPointer; i++) {
					 this.mInput[i] = this.mInput[i + 1];
				 }
				 this.mInput[this.mInputPointer] = 0;
				 this.mInputPointer--;
			 }
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

		 this.mSetButton.setEnabled(this.mInputPointer >= 0);
	 }

	 /**
	  * Returns the hours as currently inputted by the user.
	  *
	  * @return the inputted hours
	  */
	 public int getHours() {
		 final int hours = this.mInput[4];
		 return hours;
	 }

	 protected int getLayoutId() {
		 return R.layout.hms_picker_view;
	 }

	 /**
	  * Returns the minutes as currently inputted by the user.
	  *
	  * @return the inputted minutes
	  */
	 public int getMinutes() {
		 return (this.mInput[3] * 10) + this.mInput[2];
	 }

	 /**
	  * Return the seconds as currently inputted by the user.
	  *
	  * @return the inputted seconds
	  */
	 public int getSeconds() {
		 return (this.mInput[1] * 10) + this.mInput[0];
	 }

	 /**
	  * Returns the time in seconds
	  *
	  * @return an int representing the time in seconds
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
		 this.mEnteredHms = (HmsView) this.findViewById(R.id.hms_text);
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
		 this.updateHms();

		 this.mHoursLabel = (TextView) this.findViewById(R.id.hours_label);
		 this.mMinutesLabel = (TextView) this.findViewById(R.id.minutes_label);
		 this.mSecondsLabel = (TextView) this.findViewById(R.id.seconds_label);
		 this.mDivider = this.findViewById(R.id.divider);

		 this.restyleViews();
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

		 this.mInputPointer = savedState.mInputPointer;
		 this.mInput = savedState.mInput;
		 if (this.mInput == null) {
			 this.mInput = new int[this.mInputSize];
			 this.mInputPointer = -1;
		 }
		 this.updateKeypad();
	 }

	 @Override
	 public Parcelable onSaveInstanceState() {
		 final Parcelable parcel = super.onSaveInstanceState();
		 final SavedState state = new SavedState(parcel);
		 state.mInput = this.mInput;
		 state.mInputPointer = this.mInputPointer;
		 return state;
	 }

	 /**
	  * Reset all inputs and the hours:minutes:seconds.
	  */
	  public void reset() {
		 for (int i = 0; i < this.mInputSize; i++) {
			 this.mInput[i] = 0;
		 }
		 this.mInputPointer = -1;
		 this.updateHms();
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
			  this.updateHms();
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
		  if (this.mHoursLabel != null) {
			  this.mHoursLabel.setTextColor(this.mTextColor);
			  this.mHoursLabel.setBackgroundResource(this.mKeyBackgroundResId);
		  }
		  if (this.mMinutesLabel != null) {
			  this.mMinutesLabel.setTextColor(this.mTextColor);
			  this.mMinutesLabel.setBackgroundResource(this.mKeyBackgroundResId);
		  }
		  if (this.mSecondsLabel != null) {
			  this.mSecondsLabel.setTextColor(this.mTextColor);
			  this.mSecondsLabel.setBackgroundResource(this.mKeyBackgroundResId);
		  }
		  if (this.mDelete != null) {
			  this.mDelete.setBackgroundResource(this.mButtonBackgroundResId);
			  this.mDelete.setImageDrawable(this.getResources().getDrawable(this.mDeleteDrawableSrcResId));
		  }
		  if (this.mEnteredHms != null) {
			  this.mEnteredHms.setTheme(this.mTheme);
		  }
	  }

	  public void saveEntryState(final Bundle outState, final String key) {
		  outState.putIntArray(key, this.mInput);
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

	   /**
	    * Update the delete button to determine whether it is able to be clicked.
	    */
	   public void updateDeleteButton() {
		   final boolean enabled = this.mInputPointer != -1;
		   if (this.mDelete != null) {
			   this.mDelete.setEnabled(enabled);
		   }
	   }

	   /**
	    * Update the time displayed in the picker:
	    *
	    * Put "-" in digits that was not entered by passing -1
	    *
	    * Hide digit by passing -2 (for highest hours digit only);
	    */
	   protected void updateHms() {
		   this.mEnteredHms.setTime(this.mInput[4], this.mInput[3], this.mInput[2], this.mInput[1], this.mInput[0]);
	   }

	   private void updateKeypad() {
		   // Update the h:m:s
		   this.updateHms();
		   // enable/disable the "set" key
		   this.enableSetButton();
		   // Update the backspace button
		   this.updateDeleteButton();

	   }
}
