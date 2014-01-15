package com.doomonafireball.betterpickers.hmspicker;



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
public class HmsPickerDialogFragment extends DialogFragment {

	/**
	 * This interface allows objects to register for the Picker's set action.
	 */
	public interface HmsPickerDialogHandler {

		void onDialogHmsSet(int reference, int hours, int minutes, int seconds);
	}
	private static final String REFERENCE_KEY = "HmsPickerDialogFragment_ReferenceKey";

	private static final String THEME_RES_ID_KEY = "HmsPickerDialogFragment_ThemeResIdKey";
	/**
	 * Create an instance of the Picker (used internally)
	 *
	 * @param reference an (optional) user-defined reference, helpful when tracking multiple Pickers
	 * @param themeResId the style resource ID for theming
	 * @return a Picker!
	 */
	public static HmsPickerDialogFragment newInstance(final int reference, final int themeResId) {
		final HmsPickerDialogFragment frag = new HmsPickerDialogFragment();
		final Bundle args = new Bundle();
		args.putInt(REFERENCE_KEY, reference);
		args.putInt(THEME_RES_ID_KEY, themeResId);
		frag.setArguments(args);
		return frag;
	}

	private Button mSet, mCancel;
	private HmsPicker mPicker;
	private int mReference = -1;
	private int mTheme = -1;
	private View mDividerOne, mDividerTwo;
	private int mDividerColor;
	private ColorStateList mTextColor;
	private int mButtonBackgroundResId;

	private int mDialogBackgroundResId;

	private Vector<HmsPickerDialogHandler> mHmsPickerDialogHandlers = new Vector<HmsPickerDialogHandler>();

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

		final View v = inflater.inflate(R.layout.hms_picker_dialog, null);
		this.mSet = (Button) v.findViewById(R.id.set_button);
		this.mCancel = (Button) v.findViewById(R.id.cancel_button);
		this.mCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				HmsPickerDialogFragment.this.dismiss();
			}
		});
		this.mPicker = (HmsPicker) v.findViewById(R.id.hms_picker);
		this.mPicker.setSetButton(this.mSet);
		this.mSet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				for (final HmsPickerDialogHandler handler : HmsPickerDialogFragment.this.mHmsPickerDialogHandlers) {
					handler.onDialogHmsSet(HmsPickerDialogFragment.this.mReference, HmsPickerDialogFragment.this.mPicker.getHours(), HmsPickerDialogFragment.this.mPicker.getMinutes(), HmsPickerDialogFragment.this.mPicker.getSeconds());
				}
				final Activity activity = HmsPickerDialogFragment.this.getActivity();
				final Fragment fragment = HmsPickerDialogFragment.this.getTargetFragment();
				if (activity instanceof HmsPickerDialogHandler) {
					final HmsPickerDialogHandler act =
							(HmsPickerDialogHandler) activity;
					act.onDialogHmsSet(HmsPickerDialogFragment.this.mReference, HmsPickerDialogFragment.this.mPicker.getHours(), HmsPickerDialogFragment.this.mPicker.getMinutes(), HmsPickerDialogFragment.this.mPicker.getSeconds());
				} else if (fragment instanceof HmsPickerDialogHandler) {
					final HmsPickerDialogHandler frag =
							(HmsPickerDialogHandler) fragment;
					frag.onDialogHmsSet(HmsPickerDialogFragment.this.mReference, HmsPickerDialogFragment.this.mPicker.getHours(), HmsPickerDialogFragment.this.mPicker.getMinutes(), HmsPickerDialogFragment.this.mPicker.getSeconds());
				}
				HmsPickerDialogFragment.this.dismiss();
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
	public void setHmsPickerDialogHandlers(final Vector<HmsPickerDialogHandler> handlers) {
		this.mHmsPickerDialogHandlers = handlers;
	}
}