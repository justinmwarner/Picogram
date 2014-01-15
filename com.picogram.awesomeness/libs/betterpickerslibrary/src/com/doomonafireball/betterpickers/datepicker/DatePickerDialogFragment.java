package com.doomonafireball.betterpickers.datepicker;



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
public class DatePickerDialogFragment extends DialogFragment {

	/**
	 * This interface allows objects to register for the Picker's set action.
	 */
	public interface DatePickerDialogHandler {

		void onDialogDateSet(int reference, int year, int monthOfYear, int dayOfMonth);
	}
	private static final String REFERENCE_KEY = "DatePickerDialogFragment_ReferenceKey";
	private static final String THEME_RES_ID_KEY = "DatePickerDialogFragment_ThemeResIdKey";
	private static final String MONTH_KEY = "DatePickerDialogFragment_MonthKey";
	private static final String DAY_KEY = "DatePickerDialogFragment_DayKey";

	private static final String YEAR_KEY = "DatePickerDialogFragment_YearKey";
	/**
	 * Create an instance of the Picker (used internally)
	 *
	 * @param reference an (optional) user-defined reference, helpful when tracking multiple Pickers
	 * @param themeResId the style resource ID for theming
	 * @param monthOfYear (optional) zero-indexed month of year to pre-set
	 * @param dayOfMonth (optional) day of month to pre-set
	 * @param year (optional) year to pre-set
	 * @return a Picker!
	 */
	public static DatePickerDialogFragment newInstance(final int reference, final int themeResId, final Integer monthOfYear,
			final Integer dayOfMonth, final Integer year) {
		final DatePickerDialogFragment frag = new DatePickerDialogFragment();
		final Bundle args = new Bundle();
		args.putInt(REFERENCE_KEY, reference);
		args.putInt(THEME_RES_ID_KEY, themeResId);
		if (monthOfYear != null) {
			args.putInt(MONTH_KEY, monthOfYear);
		}
		if (dayOfMonth != null) {
			args.putInt(DAY_KEY, dayOfMonth);
		}
		if (year != null) {
			args.putInt(YEAR_KEY, year);
		}
		frag.setArguments(args);
		return frag;
	}

	private Button mSet, mCancel;
	private DatePicker mPicker;
	private int mMonthOfYear = -1;

	private int mDayOfMonth = 0;
	private int mYear = 0;
	private int mReference = -1;
	private int mTheme = -1;
	private View mDividerOne, mDividerTwo;
	private int mDividerColor;
	private ColorStateList mTextColor;
	private int mButtonBackgroundResId;

	private int mDialogBackgroundResId;

	private Vector<DatePickerDialogHandler> mDatePickerDialogHandlers = new Vector<DatePickerDialogHandler>();

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
		if ((args != null) && args.containsKey(DAY_KEY)) {
			this.mDayOfMonth = args.getInt(DAY_KEY);
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

		final View v = inflater.inflate(R.layout.date_picker_dialog, null);
		this.mSet = (Button) v.findViewById(R.id.set_button);
		this.mCancel = (Button) v.findViewById(R.id.cancel_button);
		this.mCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				DatePickerDialogFragment.this.dismiss();
			}
		});
		this.mPicker = (DatePicker) v.findViewById(R.id.date_picker);
		this.mPicker.setSetButton(this.mSet);
		this.mPicker.setDate(this.mYear, this.mMonthOfYear, this.mDayOfMonth);
		this.mSet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				for (final DatePickerDialogHandler handler : DatePickerDialogFragment.this.mDatePickerDialogHandlers) {
					handler.onDialogDateSet(DatePickerDialogFragment.this.mReference, DatePickerDialogFragment.this.mPicker.getYear(), DatePickerDialogFragment.this.mPicker.getMonthOfYear(),
							DatePickerDialogFragment.this.mPicker.getDayOfMonth());
				}
				final Activity activity = DatePickerDialogFragment.this.getActivity();
				final Fragment fragment = DatePickerDialogFragment.this.getTargetFragment();
				if (activity instanceof DatePickerDialogHandler) {
					final DatePickerDialogHandler act =
							(DatePickerDialogHandler) activity;
					act.onDialogDateSet(DatePickerDialogFragment.this.mReference, DatePickerDialogFragment.this.mPicker.getYear(), DatePickerDialogFragment.this.mPicker.getMonthOfYear(),
							DatePickerDialogFragment.this.mPicker.getDayOfMonth());
				} else if (fragment instanceof DatePickerDialogHandler) {
					final DatePickerDialogHandler frag =
							(DatePickerDialogHandler) fragment;
					frag.onDialogDateSet(DatePickerDialogFragment.this.mReference, DatePickerDialogFragment.this.mPicker.getYear(), DatePickerDialogFragment.this.mPicker.getMonthOfYear(),
							DatePickerDialogFragment.this.mPicker.getDayOfMonth());
				}
				DatePickerDialogFragment.this.dismiss();
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
	public void setDatePickerDialogHandlers(final Vector<DatePickerDialogHandler> handlers) {
		this.mDatePickerDialogHandlers = handlers;
	}
}