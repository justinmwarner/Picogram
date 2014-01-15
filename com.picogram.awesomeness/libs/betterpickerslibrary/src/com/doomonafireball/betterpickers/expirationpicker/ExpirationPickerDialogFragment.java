package com.doomonafireball.betterpickers.expirationpicker;



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
public class ExpirationPickerDialogFragment extends DialogFragment {

	/**
	 * This interface allows objects to register for the Picker's set action.
	 */
	public interface ExpirationPickerDialogHandler {

		void onDialogExpirationSet(int reference, int year, int monthOfYear);
	}
	private static final String REFERENCE_KEY = "ExpirationPickerDialogFragment_ReferenceKey";
	private static final String THEME_RES_ID_KEY = "ExpirationPickerDialogFragment_ThemeResIdKey";
	private static final String MONTH_KEY = "ExpirationPickerDialogFragment_MonthKey";

	private static final String YEAR_KEY = "ExpirationPickerDialogFragment_YearKey";
	/**
	 * Create an instance of the Picker (used internally)
	 *
	 * @param reference an (optional) user-defined reference, helpful when tracking multiple Pickers
	 * @param themeResId the style resource ID for theming
	 * @param monthOfYear (optional) zero-indexed month of year to pre-set
	 * @param year (optional) year to pre-set
	 * @return a Picker!
	 */
	public static ExpirationPickerDialogFragment newInstance(final int reference, final int themeResId, final Integer monthOfYear,
			final Integer year) {
		final ExpirationPickerDialogFragment frag = new ExpirationPickerDialogFragment();
		final Bundle args = new Bundle();
		args.putInt(REFERENCE_KEY, reference);
		args.putInt(THEME_RES_ID_KEY, themeResId);
		if (monthOfYear != null) {
			args.putInt(MONTH_KEY, monthOfYear);
		}
		if (year != null) {
			args.putInt(YEAR_KEY, year);
		}
		frag.setArguments(args);
		return frag;
	}

	private Button mSet, mCancel;
	private ExpirationPicker mPicker;

	private int mMonthOfYear = -1;
	private int mYear = 0;
	private int mReference = -1;
	private int mTheme = -1;
	private View mDividerOne, mDividerTwo;
	private int mDividerColor;
	private ColorStateList mTextColor;
	private int mButtonBackgroundResId;

	private int mDialogBackgroundResId;

	private Vector<ExpirationPickerDialogHandler> mExpirationPickerDialogHandlers = new Vector<ExpirationPickerDialogHandler>();

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
		if ((args != null) && args.containsKey(MONTH_KEY)) {
			this.mMonthOfYear = args.getInt(MONTH_KEY);
		}
		if ((args != null) && args.containsKey(YEAR_KEY)) {
			this.mYear = args.getInt(YEAR_KEY);
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

		final View v = inflater.inflate(R.layout.expiration_picker_dialog, null);
		this.mSet = (Button) v.findViewById(R.id.set_button);
		this.mCancel = (Button) v.findViewById(R.id.cancel_button);
		this.mCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				ExpirationPickerDialogFragment.this.dismiss();
			}
		});
		this.mPicker = (ExpirationPicker) v.findViewById(R.id.expiration_picker);
		this.mPicker.setSetButton(this.mSet);

		if ((this.mMonthOfYear != -1) || (this.mYear != 0)) {
			this.mPicker.setExpiration(this.mYear, this.mMonthOfYear);
		}

		this.mSet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				for (final ExpirationPickerDialogHandler handler : ExpirationPickerDialogFragment.this.mExpirationPickerDialogHandlers) {
					handler.onDialogExpirationSet(ExpirationPickerDialogFragment.this.mReference, ExpirationPickerDialogFragment.this.mPicker.getYear(), ExpirationPickerDialogFragment.this.mPicker.getMonthOfYear());
				}
				final Activity activity = ExpirationPickerDialogFragment.this.getActivity();
				final Fragment fragment = ExpirationPickerDialogFragment.this.getTargetFragment();
				if (activity instanceof ExpirationPickerDialogHandler) {
					final ExpirationPickerDialogHandler act =
							(ExpirationPickerDialogHandler) activity;
					act.onDialogExpirationSet(ExpirationPickerDialogFragment.this.mReference, ExpirationPickerDialogFragment.this.mPicker.getYear(), ExpirationPickerDialogFragment.this.mPicker.getMonthOfYear());
				} else if (fragment instanceof ExpirationPickerDialogHandler) {
					final ExpirationPickerDialogHandler frag =
							(ExpirationPickerDialogHandler) fragment;
					frag.onDialogExpirationSet(ExpirationPickerDialogFragment.this.mReference, ExpirationPickerDialogFragment.this.mPicker.getYear(), ExpirationPickerDialogFragment.this.mPicker.getMonthOfYear());
				}
				ExpirationPickerDialogFragment.this.dismiss();
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
	public void setExpirationPickerDialogHandlers(final Vector<ExpirationPickerDialogHandler> handlers) {
		this.mExpirationPickerDialogHandlers = handlers;
	}
}