package com.doomonafireball.betterpickers.numberpicker;



import android.app.Activity;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Vector;

import com.doomonafireball.betterpickers.R;
/**
 * Dialog to set alarm time.
 */
public class NumberPickerDialogFragment extends DialogFragment {

	/**
	 * This interface allows objects to register for the Picker's set action.
	 */
	public interface NumberPickerDialogHandler {

		void onDialogNumberSet(int reference, int number, double decimal, boolean isNegative, double fullNumber);
	}
	private static final String REFERENCE_KEY = "NumberPickerDialogFragment_ReferenceKey";
	private static final String THEME_RES_ID_KEY = "NumberPickerDialogFragment_ThemeResIdKey";
	private static final String MIN_NUMBER_KEY = "NumberPickerDialogFragment_MinNumberKey";
	private static final String MAX_NUMBER_KEY = "NumberPickerDialogFragment_MaxNumberKey";
	private static final String PLUS_MINUS_VISIBILITY_KEY = "NumberPickerDialogFragment_PlusMinusVisibilityKey";
	private static final String DECIMAL_VISIBILITY_KEY = "NumberPickerDialogFragment_DecimalVisibilityKey";

	private static final String LABEL_TEXT_KEY = "NumberPickerDialogFragment_LabelTextKey";
	/**
	 * Create an instance of the Picker (used internally)
	 *
	 * @param reference an (optional) user-defined reference, helpful when tracking multiple Pickers
	 * @param themeResId the style resource ID for theming
	 * @param minNumber (optional) the minimum possible number
	 * @param maxNumber (optional) the maximum possible number
	 * @param plusMinusVisibility (optional) View.VISIBLE, View.INVISIBLE, or View.GONE
	 * @param decimalVisibility (optional) View.VISIBLE, View.INVISIBLE, or View.GONE
	 * @param labelText (optional) text to add as a label
	 * @return a Picker!
	 */
	public static NumberPickerDialogFragment newInstance(final int reference, final int themeResId, final Integer minNumber,
			final Integer maxNumber, final Integer plusMinusVisibility, final Integer decimalVisibility, final String labelText) {
		final NumberPickerDialogFragment frag = new NumberPickerDialogFragment();
		final Bundle args = new Bundle();
		args.putInt(REFERENCE_KEY, reference);
		args.putInt(THEME_RES_ID_KEY, themeResId);
		if (minNumber != null) {
			args.putInt(MIN_NUMBER_KEY, minNumber);
		}
		if (maxNumber != null) {
			args.putInt(MAX_NUMBER_KEY, maxNumber);
		}
		if (plusMinusVisibility != null) {
			args.putInt(PLUS_MINUS_VISIBILITY_KEY, plusMinusVisibility);
		}
		if (decimalVisibility != null) {
			args.putInt(DECIMAL_VISIBILITY_KEY, decimalVisibility);
		}
		if (labelText != null) {
			args.putString(LABEL_TEXT_KEY, labelText);
		}
		frag.setArguments(args);
		return frag;
	}

	private Button mSet, mCancel;
	private NumberPicker mPicker;
	private View mDividerOne, mDividerTwo;
	private int mReference = -1;
	private int mTheme = -1;
	private int mDividerColor;
	private ColorStateList mTextColor;
	private String mLabelText = "";

	private int mButtonBackgroundResId;
	private int mDialogBackgroundResId;
	private Integer mMinNumber = null;
	private Integer mMaxNumber = null;
	private int mPlusMinusVisibility = View.VISIBLE;

	private int mDecimalVisibility = View.VISIBLE;

	private Vector<NumberPickerDialogHandler> mNumberPickerDialogHandlers = new Vector<NumberPickerDialogHandler>();

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle args = this.getArguments();
		if ((args != null) && args.containsKey(REFERENCE_KEY)) {
			this.mReference = args.getInt(REFERENCE_KEY);
		}
		if ((args != null) && args.containsKey(THEME_RES_ID_KEY)) {
			this.mTheme = args.getInt(THEME_RES_ID_KEY);
		}
		if ((args != null) && args.containsKey(PLUS_MINUS_VISIBILITY_KEY)) {
			this.mPlusMinusVisibility = args.getInt(PLUS_MINUS_VISIBILITY_KEY);
		}
		if ((args != null) && args.containsKey(DECIMAL_VISIBILITY_KEY)) {
			this.mDecimalVisibility = args.getInt(DECIMAL_VISIBILITY_KEY);
		}
		if ((args != null) && args.containsKey(MIN_NUMBER_KEY)) {
			this.mMinNumber = args.getInt(MIN_NUMBER_KEY);
		}
		if ((args != null) && args.containsKey(MAX_NUMBER_KEY)) {
			this.mMaxNumber = args.getInt(MAX_NUMBER_KEY);
		}
		if ((args != null) && args.containsKey(LABEL_TEXT_KEY)) {
			this.mLabelText = args.getString(LABEL_TEXT_KEY);
		}

		this.setStyle(DialogFragment.STYLE_NO_TITLE, 0);

		// Init defaults
		this.mTextColor = this.getResources().getColorStateList(R.color.dialog_text_color_holo_dark);
		this.mButtonBackgroundResId = R.drawable.button_background_dark;
		this.mDividerColor = this.getResources().getColor(R.color.default_divider_color_dark);
		this.mDialogBackgroundResId = R.drawable.dialog_full_holo_dark;

		if (this.mTheme != -1) {
			final TypedArray a = this.getActivity().getApplicationContext()
					.obtainStyledAttributes(this.mTheme, R.styleable.BetterPickersDialogFragment);

			this.mTextColor = a.getColorStateList(R.styleable.BetterPickersDialogFragment_bpTextColor);
			this.mButtonBackgroundResId = a.getResourceId(R.styleable.BetterPickersDialogFragment_bpButtonBackground,
					this.mButtonBackgroundResId);
			this.mDividerColor = a.getColor(R.styleable.BetterPickersDialogFragment_bpDividerColor, this.mDividerColor);
			this.mDialogBackgroundResId = a
					.getResourceId(R.styleable.BetterPickersDialogFragment_bpDialogBackground, this.mDialogBackgroundResId);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.number_picker_dialog, null);
		this.mSet = (Button) v.findViewById(R.id.set_button);
		this.mCancel = (Button) v.findViewById(R.id.cancel_button);
		this.mCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				NumberPickerDialogFragment.this.dismiss();
			}
		});
		this.mPicker = (NumberPicker) v.findViewById(R.id.number_picker);
		this.mPicker.setSetButton(this.mSet);
		this.mSet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				final double number = NumberPickerDialogFragment.this.mPicker.getEnteredNumber();
				if ((NumberPickerDialogFragment.this.mMinNumber != null) && (NumberPickerDialogFragment.this.mMaxNumber != null) && ((number < NumberPickerDialogFragment.this.mMinNumber) || (number > NumberPickerDialogFragment.this.mMaxNumber))) {
					final String errorText = String.format(NumberPickerDialogFragment.this.getString(R.string.min_max_error), NumberPickerDialogFragment.this.mMinNumber, NumberPickerDialogFragment.this.mMaxNumber);
					NumberPickerDialogFragment.this.mPicker.getErrorView().setText(errorText);
					NumberPickerDialogFragment.this.mPicker.getErrorView().show();
					return;
				} else if ((NumberPickerDialogFragment.this.mMinNumber != null) && (number < NumberPickerDialogFragment.this.mMinNumber)) {
					final String errorText = String.format(NumberPickerDialogFragment.this.getString(R.string.min_error), NumberPickerDialogFragment.this.mMinNumber);
					NumberPickerDialogFragment.this.mPicker.getErrorView().setText(errorText);
					NumberPickerDialogFragment.this.mPicker.getErrorView().show();
					return;
				} else if ((NumberPickerDialogFragment.this.mMaxNumber != null) && (number > NumberPickerDialogFragment.this.mMaxNumber)) {
					final String errorText = String.format(NumberPickerDialogFragment.this.getString(R.string.max_error), NumberPickerDialogFragment.this.mMaxNumber);
					NumberPickerDialogFragment.this.mPicker.getErrorView().setText(errorText);
					NumberPickerDialogFragment.this.mPicker.getErrorView().show();
					return;
				}
				for (final NumberPickerDialogHandler handler : NumberPickerDialogFragment.this.mNumberPickerDialogHandlers) {
					handler.onDialogNumberSet(NumberPickerDialogFragment.this.mReference, NumberPickerDialogFragment.this.mPicker.getNumber(), NumberPickerDialogFragment.this.mPicker.getDecimal(),
							NumberPickerDialogFragment.this.mPicker.getIsNegative(), number);
				}
				final Activity activity = NumberPickerDialogFragment.this.getActivity();
				final Fragment fragment = NumberPickerDialogFragment.this.getTargetFragment();
				if (activity instanceof NumberPickerDialogHandler) {
					final NumberPickerDialogHandler act =
							(NumberPickerDialogHandler) activity;
					act.onDialogNumberSet(NumberPickerDialogFragment.this.mReference, NumberPickerDialogFragment.this.mPicker.getNumber(), NumberPickerDialogFragment.this.mPicker.getDecimal(),
							NumberPickerDialogFragment.this.mPicker.getIsNegative(), number);
				} else if (fragment instanceof NumberPickerDialogHandler) {
					final NumberPickerDialogHandler frag = (NumberPickerDialogHandler) fragment;
					frag.onDialogNumberSet(NumberPickerDialogFragment.this.mReference, NumberPickerDialogFragment.this.mPicker.getNumber(), NumberPickerDialogFragment.this.mPicker.getDecimal(),
							NumberPickerDialogFragment.this.mPicker.getIsNegative(), number);
				}
				NumberPickerDialogFragment.this.dismiss();
			}
		});

		this.mDividerOne = v.findViewById(R.id.divider_1);
		this.mDividerTwo = v.findViewById(R.id.divider_2);
		this.mDividerOne.setBackgroundColor(this.mDividerColor);
		this.mDividerTwo.setBackgroundColor(this.mDividerColor);
		this.mSet.setTextColor(this.mTextColor);
		this.mSet.setBackgroundResource(this.mButtonBackgroundResId);
		this.mCancel.setTextColor(this.mTextColor);
		this.mCancel.setBackgroundResource(this.mButtonBackgroundResId);
		this.mPicker.setTheme(this.mTheme);
		this.getDialog().getWindow().setBackgroundDrawableResource(this.mDialogBackgroundResId);

		this.mPicker.setDecimalVisibility(this.mDecimalVisibility);
		this.mPicker.setPlusMinusVisibility(this.mPlusMinusVisibility);
		this.mPicker.setLabelText(this.mLabelText);
		if (this.mMinNumber != null) {
			this.mPicker.setMin(this.mMinNumber);
		}
		if (this.mMaxNumber != null) {
			this.mPicker.setMax(this.mMaxNumber);
		}

		return v;
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	/**
	 * Attach a Vector of handlers to be notified in addition to the Fragment's Activity and target Fragment.
	 *
	 * @param handlers a Vector of handlers
	 */
	public void setNumberPickerDialogHandlers(final Vector<NumberPickerDialogHandler> handlers) {
		this.mNumberPickerDialogHandlers = handlers;
	}
}