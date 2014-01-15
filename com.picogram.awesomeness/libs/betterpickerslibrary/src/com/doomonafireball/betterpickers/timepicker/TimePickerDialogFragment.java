package com.doomonafireball.betterpickers.timepicker;



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
public class TimePickerDialogFragment extends DialogFragment {

	/**
	 * This interface allows objects to register for the Picker's set action.
	 */
	public interface TimePickerDialogHandler {

		void onDialogTimeSet(int reference, int hourOfDay, int minute);
	}
	private static final String REFERENCE_KEY = "TimePickerDialogFragment_ReferenceKey";

	private static final String THEME_RES_ID_KEY = "TimePickerDialogFragment_ThemeResIdKey";
	/**
	 * Create an instance of the Picker (used internally)
	 *
	 * @param reference an (optional) user-defined reference, helpful when tracking multiple Pickers
	 * @param themeResId the style resource ID for theming
	 * @return a Picker!
	 */
	public static TimePickerDialogFragment newInstance(final int reference, final int themeResId) {
		final TimePickerDialogFragment frag = new TimePickerDialogFragment();
		final Bundle args = new Bundle();
		args.putInt(REFERENCE_KEY, reference);
		args.putInt(THEME_RES_ID_KEY, themeResId);
		frag.setArguments(args);
		return frag;
	}

	private Button mSet, mCancel;
	private TimePicker mPicker;
	private int mReference = -1;
	private int mTheme = -1;
	private View mDividerOne, mDividerTwo;
	private int mDividerColor;
	private ColorStateList mTextColor;
	private int mButtonBackgroundResId;

	private int mDialogBackgroundResId;

	private Vector<TimePickerDialogHandler> mTimePickerDialogHandlers = new Vector<TimePickerDialogHandler>();

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

		final View v = inflater.inflate(R.layout.time_picker_dialog, null);
		this.mSet = (Button) v.findViewById(R.id.set_button);
		this.mCancel = (Button) v.findViewById(R.id.cancel_button);
		this.mCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				TimePickerDialogFragment.this.dismiss();
			}
		});
		this.mPicker = (TimePicker) v.findViewById(R.id.time_picker);
		this.mPicker.setSetButton(this.mSet);
		this.mSet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				for (final TimePickerDialogHandler handler : TimePickerDialogFragment.this.mTimePickerDialogHandlers) {
					handler.onDialogTimeSet(TimePickerDialogFragment.this.mReference, TimePickerDialogFragment.this.mPicker.getHours(), TimePickerDialogFragment.this.mPicker.getMinutes());
				}
				final Activity activity = TimePickerDialogFragment.this.getActivity();
				final Fragment fragment = TimePickerDialogFragment.this.getTargetFragment();
				if (activity instanceof TimePickerDialogHandler) {
					final TimePickerDialogHandler act =
							(TimePickerDialogHandler) activity;
					act.onDialogTimeSet(TimePickerDialogFragment.this.mReference, TimePickerDialogFragment.this.mPicker.getHours(), TimePickerDialogFragment.this.mPicker.getMinutes());
				} else if (fragment instanceof TimePickerDialogHandler) {
					final TimePickerDialogHandler frag =
							(TimePickerDialogHandler) fragment;
					frag.onDialogTimeSet(TimePickerDialogFragment.this.mReference, TimePickerDialogFragment.this.mPicker.getHours(), TimePickerDialogFragment.this.mPicker.getMinutes());
				}
				TimePickerDialogFragment.this.dismiss();
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
	public void setTimePickerDialogHandlers(final Vector<TimePickerDialogHandler> handlers) {
		this.mTimePickerDialogHandlers = handlers;
	}
}