/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.doomonafireball.betterpickers.radialtimepicker;


import com.doomonafireball.betterpickers.Utils;
import com.doomonafireball.betterpickers.radialtimepicker.RadialPickerLayout.OnValueSelectedListener;
import com.nineoldandroids.animation.ObjectAnimator;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import com.doomonafireball.betterpickers.R;
/**
 * Dialog to set a time.
 */
public class RadialTimePickerDialog extends DialogFragment implements OnValueSelectedListener {

	private class KeyboardListener implements OnKeyListener {

		@Override
		public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_UP) {
				return RadialTimePickerDialog.this.processKeyUp(keyCode);
			}
			return false;
		}
	}

	/**
	 * Simple node class to be used for traversal to check for legal times. mLegalKeys represents the keys that can be
	 * typed to get to the node. mChildren are the children that can be reached from this node.
	 */
	private class Node {

		private final int[] mLegalKeys;
		private final ArrayList<Node> mChildren;

		public Node(final int... legalKeys) {
			this.mLegalKeys = legalKeys;
			this.mChildren = new ArrayList<Node>();
		}

		public void addChild(final Node child) {
			this.mChildren.add(child);
		}

		public Node canReach(final int key) {
			if (this.mChildren == null) {
				return null;
			}
			for (final Node child : this.mChildren) {
				if (child.containsKey(key)) {
					return child;
				}
			}
			return null;
		}

		public boolean containsKey(final int key) {
			for (int i = 0; i < this.mLegalKeys.length; i++) {
				if (this.mLegalKeys[i] == key) {
					return true;
				}
			}
			return false;
		}
	}
	/**
	 * The callback interface used to indicate the user is done filling in the time (they clicked on the 'Set' button).
	 */
	public interface OnTimeSetListener {

		/**
		 * @param view The view associated with this listener.
		 * @param hourOfDay The hour that was set.
		 * @param minute The minute that was set.
		 */
		void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute);
	}
	private static final String TAG = "TimePickerDialog";
	private static final String KEY_HOUR_OF_DAY = "hour_of_day";
	private static final String KEY_MINUTE = "minute";
	private static final String KEY_IS_24_HOUR_VIEW = "is_24_hour_view";

	private static final String KEY_CURRENT_ITEM_SHOWING = "current_item_showing";
	private static final String KEY_IN_KB_MODE = "in_kb_mode";
	private static final String KEY_TYPED_TIMES = "typed_times";
	public static final int HOUR_INDEX = 0;
	public static final int MINUTE_INDEX = 1;
	// NOT a real index for the purpose of what's showing.
	public static final int AMPM_INDEX = 2;

	// Also NOT a real index, just used for keyboard mode.
	public static final int ENABLE_PICKER_INDEX = 3;

	public static final int AM = 0;

	public static final int PM = 1;
	// Delay before starting the pulse animation, in ms.
	private static final int PULSE_ANIMATOR_DELAY = 300;
	public static RadialTimePickerDialog newInstance(final OnTimeSetListener callback,
			final int hourOfDay, final int minute, final boolean is24HourMode) {
		final RadialTimePickerDialog ret = new RadialTimePickerDialog();
		ret.initialize(callback, hourOfDay, minute, is24HourMode);
		return ret;
	}
	private OnTimeSetListener mCallback;
	private TextView mDoneButton;
	private TextView mHourView;
	private TextView mHourSpaceView;
	private TextView mMinuteView;

	private TextView mMinuteSpaceView;
	private TextView mAmPmTextView;
	private View mAmPmHitspace;
	private RadialPickerLayout mTimePicker;

	private int mBlue;
	private int mBlack;
	private String mAmText;
	private String mPmText;

	private boolean mAllowAutoAdvance;
	private int mInitialHourOfDay;
	private int mInitialMinute;
	private boolean mIs24HourMode;
	// For hardware IME input.
	private char mPlaceholderText;
	private String mDoublePlaceholderText;
	private String mDeletedKeyFormat;
	private boolean mInKbMode;

	private ArrayList<Integer> mTypedTimes;
	private Node mLegalTimesTree;
	private int mAmKeyCode;
	private int mPmKeyCode;

	// Accessibility strings.
	private String mHourPickerDescription;

	private String mSelectHours;

	private String mMinutePickerDescription;

	private String mSelectMinutes;

	public RadialTimePickerDialog() {
		// Empty constructor required for dialog fragment.
	}

	public RadialTimePickerDialog(final Context context, final int theme, final OnTimeSetListener callback,
			final int hourOfDay, final int minute, final boolean is24HourMode) {
		// Empty constructor required for dialog fragment.
	}

	private boolean addKeyIfLegal(final int keyCode) {
		// If we're in 24hour mode, we'll need to check if the input is full. If in AM/PM mode,
		// we'll need to see if AM/PM have been typed.
		if ((this.mIs24HourMode && (this.mTypedTimes.size() == 4)) ||
				(!this.mIs24HourMode && this.isTypedTimeFullyLegal())) {
			return false;
		}

		this.mTypedTimes.add(keyCode);
		if (!this.isTypedTimeLegalSoFar()) {
			this.deleteLastTypedKey();
			return false;
		}

		final int val = this.getValFromKeyCode(keyCode);
		Utils.tryAccessibilityAnnounce(this.mTimePicker, String.format("%d", val));
		// Automatically fill in 0's if AM or PM was legally entered.
		if (this.isTypedTimeFullyLegal()) {
			if (!this.mIs24HourMode && (this.mTypedTimes.size() <= 3)) {
				this.mTypedTimes.add(this.mTypedTimes.size() - 1, KeyEvent.KEYCODE_0);
				this.mTypedTimes.add(this.mTypedTimes.size() - 1, KeyEvent.KEYCODE_0);
			}
			this.mDoneButton.setEnabled(true);
		}

		return true;
	}

	private int deleteLastTypedKey() {
		final int deleted = this.mTypedTimes.remove(this.mTypedTimes.size() - 1);
		if (!this.isTypedTimeFullyLegal()) {
			this.mDoneButton.setEnabled(false);
		}
		return deleted;
	}

	/**
	 * Get out of keyboard mode. If there is nothing in typedTimes, revert to TimePicker's time.
	 *
	 * @param changeDisplays If true, update the displays with the relevant time.
	 */
	private void finishKbMode(final boolean updateDisplays) {
		this.mInKbMode = false;
		if (!this.mTypedTimes.isEmpty()) {
			final int values[] = this.getEnteredTime(null);
			this.mTimePicker.setTime(values[0], values[1]);
			if (!this.mIs24HourMode) {
				this.mTimePicker.setAmOrPm(values[2]);
			}
			this.mTypedTimes.clear();
		}
		if (updateDisplays) {
			this.updateDisplay(false);
			this.mTimePicker.trySettingInputEnabled(true);
		}
	}

	/**
	 * Create a tree for deciding what keys can legally be typed.
	 */
	private void generateLegalTimesTree() {
		// Create a quick cache of numbers to their keycodes.
		final int k0 = KeyEvent.KEYCODE_0;
		final int k1 = KeyEvent.KEYCODE_1;
		final int k2 = KeyEvent.KEYCODE_2;
		final int k3 = KeyEvent.KEYCODE_3;
		final int k4 = KeyEvent.KEYCODE_4;
		final int k5 = KeyEvent.KEYCODE_5;
		final int k6 = KeyEvent.KEYCODE_6;
		final int k7 = KeyEvent.KEYCODE_7;
		final int k8 = KeyEvent.KEYCODE_8;
		final int k9 = KeyEvent.KEYCODE_9;

		// The root of the tree doesn't contain any numbers.
		this.mLegalTimesTree = new Node();
		if (this.mIs24HourMode) {
			// We'll be re-using these nodes, so we'll save them.
			final Node minuteFirstDigit = new Node(k0, k1, k2, k3, k4, k5);
			final Node minuteSecondDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
			// The first digit must be followed by the second digit.
			minuteFirstDigit.addChild(minuteSecondDigit);

			// The first digit may be 0-1.
			Node firstDigit = new Node(k0, k1);
			this.mLegalTimesTree.addChild(firstDigit);

			// When the first digit is 0-1, the second digit may be 0-5.
			Node secondDigit = new Node(k0, k1, k2, k3, k4, k5);
			firstDigit.addChild(secondDigit);
			// We may now be followed by the first minute digit. E.g. 00:09, 15:58.
			secondDigit.addChild(minuteFirstDigit);

			// When the first digit is 0-1, and the second digit is 0-5, the third digit may be 6-9.
			final Node thirdDigit = new Node(k6, k7, k8, k9);
			// The time must now be finished. E.g. 0:55, 1:08.
			secondDigit.addChild(thirdDigit);

			// When the first digit is 0-1, the second digit may be 6-9.
			secondDigit = new Node(k6, k7, k8, k9);
			firstDigit.addChild(secondDigit);
			// We must now be followed by the first minute digit. E.g. 06:50, 18:20.
			secondDigit.addChild(minuteFirstDigit);

			// The first digit may be 2.
			firstDigit = new Node(k2);
			this.mLegalTimesTree.addChild(firstDigit);

			// When the first digit is 2, the second digit may be 0-3.
			secondDigit = new Node(k0, k1, k2, k3);
			firstDigit.addChild(secondDigit);
			// We must now be followed by the first minute digit. E.g. 20:50, 23:09.
			secondDigit.addChild(minuteFirstDigit);

			// When the first digit is 2, the second digit may be 4-5.
			secondDigit = new Node(k4, k5);
			firstDigit.addChild(secondDigit);
			// We must now be followd by the last minute digit. E.g. 2:40, 2:53.
			secondDigit.addChild(minuteSecondDigit);

			// The first digit may be 3-9.
			firstDigit = new Node(k3, k4, k5, k6, k7, k8, k9);
			this.mLegalTimesTree.addChild(firstDigit);
			// We must now be followed by the first minute digit. E.g. 3:57, 8:12.
			firstDigit.addChild(minuteFirstDigit);
		} else {
			// We'll need to use the AM/PM node a lot.
			// Set up AM and PM to respond to "a" and "p".
			final Node ampm = new Node(this.getAmOrPmKeyCode(AM), this.getAmOrPmKeyCode(PM));

			// The first hour digit may be 1.
			Node firstDigit = new Node(k1);
			this.mLegalTimesTree.addChild(firstDigit);
			// We'll allow quick input of on-the-hour times. E.g. 1pm.
			firstDigit.addChild(ampm);

			// When the first digit is 1, the second digit may be 0-2.
			Node secondDigit = new Node(k0, k1, k2);
			firstDigit.addChild(secondDigit);
			// Also for quick input of on-the-hour times. E.g. 10pm, 12am.
			secondDigit.addChild(ampm);

			// When the first digit is 1, and the second digit is 0-2, the third digit may be 0-5.
			Node thirdDigit = new Node(k0, k1, k2, k3, k4, k5);
			secondDigit.addChild(thirdDigit);
			// The time may be finished now. E.g. 1:02pm, 1:25am.
			thirdDigit.addChild(ampm);

			// When the first digit is 1, the second digit is 0-2, and the third digit is 0-5,
			// the fourth digit may be 0-9.
			final Node fourthDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
			thirdDigit.addChild(fourthDigit);
			// The time must be finished now. E.g. 10:49am, 12:40pm.
			fourthDigit.addChild(ampm);

			// When the first digit is 1, and the second digit is 0-2, the third digit may be 6-9.
			thirdDigit = new Node(k6, k7, k8, k9);
			secondDigit.addChild(thirdDigit);
			// The time must be finished now. E.g. 1:08am, 1:26pm.
			thirdDigit.addChild(ampm);

			// When the first digit is 1, the second digit may be 3-5.
			secondDigit = new Node(k3, k4, k5);
			firstDigit.addChild(secondDigit);

			// When the first digit is 1, and the second digit is 3-5, the third digit may be 0-9.
			thirdDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
			secondDigit.addChild(thirdDigit);
			// The time must be finished now. E.g. 1:39am, 1:50pm.
			thirdDigit.addChild(ampm);

			// The hour digit may be 2-9.
			firstDigit = new Node(k2, k3, k4, k5, k6, k7, k8, k9);
			this.mLegalTimesTree.addChild(firstDigit);
			// We'll allow quick input of on-the-hour-times. E.g. 2am, 5pm.
			firstDigit.addChild(ampm);

			// When the first digit is 2-9, the second digit may be 0-5.
			secondDigit = new Node(k0, k1, k2, k3, k4, k5);
			firstDigit.addChild(secondDigit);

			// When the first digit is 2-9, and the second digit is 0-5, the third digit may be 0-9.
			thirdDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
			secondDigit.addChild(thirdDigit);
			// The time must be finished now. E.g. 2:57am, 9:30pm.
			thirdDigit.addChild(ampm);
		}
	}

	/**
	 * Get the keycode value for AM and PM in the current language.
	 */
	private int getAmOrPmKeyCode(final int amOrPm) {
		// Cache the codes.
		if ((this.mAmKeyCode == -1) || (this.mPmKeyCode == -1)) {
			// Find the first character in the AM/PM text that is unique.
			final KeyCharacterMap kcm = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
			char amChar;
			char pmChar;
			for (int i = 0; i < Math.max(this.mAmText.length(), this.mPmText.length()); i++) {
				amChar = this.mAmText.toLowerCase(Locale.getDefault()).charAt(i);
				pmChar = this.mPmText.toLowerCase(Locale.getDefault()).charAt(i);
				if (amChar != pmChar) {
					final KeyEvent[] events = kcm.getEvents(new char[]{amChar, pmChar});
					// There should be 4 events: a down and up for both AM and PM.
					if ((events != null) && (events.length == 4)) {
						this.mAmKeyCode = events[0].getKeyCode();
						this.mPmKeyCode = events[2].getKeyCode();
					} else {
						Log.e(TAG, "Unable to find keycodes for AM and PM.");
					}
					break;
				}
			}
		}
		if (amOrPm == AM) {
			return this.mAmKeyCode;
		} else if (amOrPm == PM) {
			return this.mPmKeyCode;
		}

		return -1;
	}

	/**
	 * Get the currently-entered time, as integer values of the hours and minutes typed.
	 *
	 * @param enteredZeros A size-2 boolean array, which the caller should initialize, and which may then be used for
	 * the caller to know whether zeros had been explicitly entered as either hours of minutes. This is helpful for
	 * deciding whether to show the dashes, or actual 0's.
	 * @return A size-3 int array. The first value will be the hours, the second value will be the minutes, and the
	 *         third will be either TimePickerDialog.AM or TimePickerDialog.PM.
	 */
	private int[] getEnteredTime(final Boolean[] enteredZeros) {
		int amOrPm = -1;
		int startIndex = 1;
		if (!this.mIs24HourMode && this.isTypedTimeFullyLegal()) {
			final int keyCode = this.mTypedTimes.get(this.mTypedTimes.size() - 1);
			if (keyCode == this.getAmOrPmKeyCode(AM)) {
				amOrPm = AM;
			} else if (keyCode == this.getAmOrPmKeyCode(PM)) {
				amOrPm = PM;
			}
			startIndex = 2;
		}
		int minute = -1;
		int hour = -1;
		for (int i = startIndex; i <= this.mTypedTimes.size(); i++) {
			final int val = this.getValFromKeyCode(this.mTypedTimes.get(this.mTypedTimes.size() - i));
			if (i == startIndex) {
				minute = val;
			} else if (i == (startIndex + 1)) {
				minute += 10 * val;
				if ((enteredZeros != null) && (val == 0)) {
					enteredZeros[1] = true;
				}
			} else if (i == (startIndex + 2)) {
				hour = val;
			} else if (i == (startIndex + 3)) {
				hour += 10 * val;
				if ((enteredZeros != null) && (val == 0)) {
					enteredZeros[0] = true;
				}
			}
		}

		final int[] ret = {hour, minute, amOrPm};
		return ret;
	}

	private int getValFromKeyCode(final int keyCode) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_0:
				return 0;
			case KeyEvent.KEYCODE_1:
				return 1;
			case KeyEvent.KEYCODE_2:
				return 2;
			case KeyEvent.KEYCODE_3:
				return 3;
			case KeyEvent.KEYCODE_4:
				return 4;
			case KeyEvent.KEYCODE_5:
				return 5;
			case KeyEvent.KEYCODE_6:
				return 6;
			case KeyEvent.KEYCODE_7:
				return 7;
			case KeyEvent.KEYCODE_8:
				return 8;
			case KeyEvent.KEYCODE_9:
				return 9;
			default:
				return -1;
		}
	}

	public void initialize(final OnTimeSetListener callback,
			final int hourOfDay, final int minute, final boolean is24HourMode) {
		this.mCallback = callback;

		this.mInitialHourOfDay = hourOfDay;
		this.mInitialMinute = minute;
		this.mIs24HourMode = is24HourMode;
		this.mInKbMode = false;
	}

	/**
	 * Check if the time that has been typed so far is completely legal, as is.
	 */
	 private boolean isTypedTimeFullyLegal() {
		 if (this.mIs24HourMode) {
			 // For 24-hour mode, the time is legal if the hours and minutes are each legal. Note:
				 // getEnteredTime() will ONLY call isTypedTimeFullyLegal() when NOT in 24hour mode.
			 final int[] values = this.getEnteredTime(null);
			 return ((values[0] >= 0) && (values[1] >= 0) && (values[1] < 60));
		 } else {
			 // For AM/PM mode, the time is legal if it contains an AM or PM, as those can only be
			 // legally added at specific times based on the tree's algorithm.
			 return (this.mTypedTimes.contains(this.getAmOrPmKeyCode(AM)) ||
					 this.mTypedTimes.contains(this.getAmOrPmKeyCode(PM)));
		 }
	 }

	 /**
	  * Traverse the tree to see if the keys that have been typed so far are legal as is, or may become legal as more
	  * keys are typed (excluding backspace).
	  */
	 private boolean isTypedTimeLegalSoFar() {
		 Node node = this.mLegalTimesTree;
		 for (final int keyCode : this.mTypedTimes) {
			 node = node.canReach(keyCode);
			 if (node == null) {
				 return false;
			 }
		 }
		 return true;
	 }

	 @Override
	 public void onCreate(final Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_HOUR_OF_DAY)
				 && savedInstanceState.containsKey(KEY_MINUTE)
				 && savedInstanceState.containsKey(KEY_IS_24_HOUR_VIEW)) {
			 this.mInitialHourOfDay = savedInstanceState.getInt(KEY_HOUR_OF_DAY);
			 this.mInitialMinute = savedInstanceState.getInt(KEY_MINUTE);
			 this.mIs24HourMode = savedInstanceState.getBoolean(KEY_IS_24_HOUR_VIEW);
			 this.mInKbMode = savedInstanceState.getBoolean(KEY_IN_KB_MODE);
		 }
	 }

	 @Override
	 public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			 final Bundle savedInstanceState) {
		 this.getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		 final View view = inflater.inflate(R.layout.radial_time_picker_dialog, null);
		 final KeyboardListener keyboardListener = new KeyboardListener();
		 view.findViewById(R.id.time_picker_dialog).setOnKeyListener(keyboardListener);

		 final Resources res = this.getResources();
		 this.mHourPickerDescription = res.getString(R.string.hour_picker_description);
		 this.mSelectHours = res.getString(R.string.select_hours);
		 this.mMinutePickerDescription = res.getString(R.string.minute_picker_description);
		 this.mSelectMinutes = res.getString(R.string.select_minutes);
		 this.mBlue = res.getColor(R.color.blue);
		 this.mBlack = res.getColor(R.color.numbers_text_color);

		 this.mHourView = (TextView) view.findViewById(R.id.hours);
		 this.mHourView.setOnKeyListener(keyboardListener);
		 this.mHourSpaceView = (TextView) view.findViewById(R.id.hour_space);
		 this.mMinuteSpaceView = (TextView) view.findViewById(R.id.minutes_space);
		 this.mMinuteView = (TextView) view.findViewById(R.id.minutes);
		 this.mMinuteView.setOnKeyListener(keyboardListener);
		 this.mAmPmTextView = (TextView) view.findViewById(R.id.ampm_label);
		 this.mAmPmTextView.setOnKeyListener(keyboardListener);
		 final String[] amPmTexts = new DateFormatSymbols().getAmPmStrings();
		 this.mAmText = amPmTexts[0];
		 this.mPmText = amPmTexts[1];

		 this.mTimePicker = (RadialPickerLayout) view.findViewById(R.id.time_picker);
		 this.mTimePicker.setOnValueSelectedListener(this);
		 this.mTimePicker.setOnKeyListener(keyboardListener);
		 this.mTimePicker.initialize(this.getActivity(), this.mInitialHourOfDay, this.mInitialMinute, this.mIs24HourMode);
		 int currentItemShowing = HOUR_INDEX;
		 if ((savedInstanceState != null) &&
				 savedInstanceState.containsKey(KEY_CURRENT_ITEM_SHOWING)) {
			 currentItemShowing = savedInstanceState.getInt(KEY_CURRENT_ITEM_SHOWING);
		 }
		 this.setCurrentItemShowing(currentItemShowing, false, true, true);
		 this.mTimePicker.invalidate();

		 this.mHourView.setOnClickListener(new OnClickListener() {
			 @Override
			 public void onClick(final View v) {
				 RadialTimePickerDialog.this.setCurrentItemShowing(HOUR_INDEX, true, false, true);
				 RadialTimePickerDialog.this.mTimePicker.tryVibrate();
			 }
		 });
		 this.mMinuteView.setOnClickListener(new OnClickListener() {
			 @Override
			 public void onClick(final View v) {
				 RadialTimePickerDialog.this.setCurrentItemShowing(MINUTE_INDEX, true, false, true);
				 RadialTimePickerDialog.this.mTimePicker.tryVibrate();
			 }
		 });

		 this.mDoneButton = (TextView) view.findViewById(R.id.done_button);
		 this.mDoneButton.setOnClickListener(new OnClickListener() {
			 @Override
			 public void onClick(final View v) {
				 if (RadialTimePickerDialog.this.mInKbMode && RadialTimePickerDialog.this.isTypedTimeFullyLegal()) {
					 RadialTimePickerDialog.this.finishKbMode(false);
				 } else {
					 RadialTimePickerDialog.this.mTimePicker.tryVibrate();
				 }
				 if (RadialTimePickerDialog.this.mCallback != null) {
					 RadialTimePickerDialog.this.mCallback.onTimeSet(RadialTimePickerDialog.this.mTimePicker,
							 RadialTimePickerDialog.this.mTimePicker.getHours(), RadialTimePickerDialog.this.mTimePicker.getMinutes());
				 }
				 RadialTimePickerDialog.this.dismiss();
			 }
		 });
		 this.mDoneButton.setOnKeyListener(keyboardListener);

		 // Enable or disable the AM/PM view.
		 this.mAmPmHitspace = view.findViewById(R.id.ampm_hitspace);
		 if (this.mIs24HourMode) {
			 this.mAmPmTextView.setVisibility(View.GONE);

			 final RelativeLayout.LayoutParams paramsSeparator = new RelativeLayout.LayoutParams(
					 LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			 paramsSeparator.addRule(RelativeLayout.CENTER_IN_PARENT);
			 final TextView separatorView = (TextView) view.findViewById(R.id.separator);
			 separatorView.setLayoutParams(paramsSeparator);
		 } else {
			 this.mAmPmTextView.setVisibility(View.VISIBLE);
			 this.updateAmPmDisplay(this.mInitialHourOfDay < 12 ? AM : PM);
			 this.mAmPmHitspace.setOnClickListener(new OnClickListener() {
				 @Override
				 public void onClick(final View v) {
					 RadialTimePickerDialog.this.mTimePicker.tryVibrate();
					 int amOrPm = RadialTimePickerDialog.this.mTimePicker.getIsCurrentlyAmOrPm();
					 if (amOrPm == AM) {
						 amOrPm = PM;
					 } else if (amOrPm == PM) {
						 amOrPm = AM;
					 }
					 RadialTimePickerDialog.this.updateAmPmDisplay(amOrPm);
					 RadialTimePickerDialog.this.mTimePicker.setAmOrPm(amOrPm);
				 }
			 });
		 }

		 this.mAllowAutoAdvance = true;
		 this.setHour(this.mInitialHourOfDay, true);
		 this.setMinute(this.mInitialMinute);

		 // Set up for keyboard mode.
		 this.mDoublePlaceholderText = res.getString(R.string.time_placeholder);
				 this.mDeletedKeyFormat = res.getString(R.string.deleted_key);
				 this.mPlaceholderText = this.mDoublePlaceholderText.charAt(0);
				 this.mAmKeyCode = this.mPmKeyCode = -1;
				 this.generateLegalTimesTree();
				 if (this.mInKbMode) {
					 this.mTypedTimes = savedInstanceState.getIntegerArrayList(KEY_TYPED_TIMES);
					 this.tryStartingKbMode(-1);
					 this.mHourView.invalidate();
				 } else if (this.mTypedTimes == null) {
					 this.mTypedTimes = new ArrayList<Integer>();
				 }

				 return view;
	 }

	 @Override
	 public void onSaveInstanceState(final Bundle outState) {
		 if (this.mTimePicker != null) {
			 outState.putInt(KEY_HOUR_OF_DAY, this.mTimePicker.getHours());
			 outState.putInt(KEY_MINUTE, this.mTimePicker.getMinutes());
			 outState.putBoolean(KEY_IS_24_HOUR_VIEW, this.mIs24HourMode);
			 outState.putInt(KEY_CURRENT_ITEM_SHOWING, this.mTimePicker.getCurrentItemShowing());
			 outState.putBoolean(KEY_IN_KB_MODE, this.mInKbMode);
			 if (this.mInKbMode) {
				 outState.putIntegerArrayList(KEY_TYPED_TIMES, this.mTypedTimes);
			 }
		 }
	 }

	 /**
	  * Called by the picker for updating the header display.
	  */
	 @Override
	 public void onValueSelected(final int pickerIndex, final int newValue, final boolean autoAdvance) {
		 if (pickerIndex == HOUR_INDEX) {
			 this.setHour(newValue, false);
			 String announcement = String.format("%d", newValue);
			 if (this.mAllowAutoAdvance && autoAdvance) {
				 this.setCurrentItemShowing(MINUTE_INDEX, true, true, false);
				 announcement += ". " + this.mSelectMinutes;
			 }
			 Utils.tryAccessibilityAnnounce(this.mTimePicker, announcement);
		 } else if (pickerIndex == MINUTE_INDEX) {
			 this.setMinute(newValue);
		 } else if (pickerIndex == AMPM_INDEX) {
			 this.updateAmPmDisplay(newValue);
		 } else if (pickerIndex == ENABLE_PICKER_INDEX) {
			 if (!this.isTypedTimeFullyLegal()) {
				 this.mTypedTimes.clear();
			 }
			 this.finishKbMode(true);
		 }
	 }

	 /**
	  * For keyboard mode, processes key events.
	  *
	  * @param keyCode the pressed key.
	  * @return true if the key was successfully processed, false otherwise.
	  */
	 private boolean processKeyUp(final int keyCode) {
		 if ((keyCode == KeyEvent.KEYCODE_ESCAPE) || (keyCode == KeyEvent.KEYCODE_BACK)) {
			 this.dismiss();
			 return true;
		 } else if (keyCode == KeyEvent.KEYCODE_TAB) {
			 if (this.mInKbMode) {
				 if (this.isTypedTimeFullyLegal()) {
					 this.finishKbMode(true);
				 }
				 return true;
			 }
		 } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
			 if (this.mInKbMode) {
				 if (!this.isTypedTimeFullyLegal()) {
					 return true;
				 }
				 this.finishKbMode(false);
			 }
			 if (this.mCallback != null) {
				 this.mCallback.onTimeSet(this.mTimePicker,
						 this.mTimePicker.getHours(), this.mTimePicker.getMinutes());
			 }
			 this.dismiss();
			 return true;
		 } else if (keyCode == KeyEvent.KEYCODE_DEL) {
			 if (this.mInKbMode) {
				 if (!this.mTypedTimes.isEmpty()) {
					 final int deleted = this.deleteLastTypedKey();
					 String deletedKeyStr;
					 if (deleted == this.getAmOrPmKeyCode(AM)) {
						 deletedKeyStr = this.mAmText;
					 } else if (deleted == this.getAmOrPmKeyCode(PM)) {
						 deletedKeyStr = this.mPmText;
					 } else {
						 deletedKeyStr = String.format("%d", this.getValFromKeyCode(deleted));
					 }
					 Utils.tryAccessibilityAnnounce(this.mTimePicker,
							 String.format(this.mDeletedKeyFormat, deletedKeyStr));
					 this.updateDisplay(true);
				 }
			 }
		 } else if ((keyCode == KeyEvent.KEYCODE_0) || (keyCode == KeyEvent.KEYCODE_1)
				 || (keyCode == KeyEvent.KEYCODE_2) || (keyCode == KeyEvent.KEYCODE_3)
				 || (keyCode == KeyEvent.KEYCODE_4) || (keyCode == KeyEvent.KEYCODE_5)
				 || (keyCode == KeyEvent.KEYCODE_6) || (keyCode == KeyEvent.KEYCODE_7)
				 || (keyCode == KeyEvent.KEYCODE_8) || (keyCode == KeyEvent.KEYCODE_9)
				 || (!this.mIs24HourMode &&
						 ((keyCode == this.getAmOrPmKeyCode(AM)) || (keyCode == this.getAmOrPmKeyCode(PM))))) {
			 if (!this.mInKbMode) {
				 if (this.mTimePicker == null) {
					 // Something's wrong, because time picker should definitely not be null.
					 Log.e(TAG, "Unable to initiate keyboard mode, TimePicker was null.");
					 return true;
				 }
				 this.mTypedTimes.clear();
				 this.tryStartingKbMode(keyCode);
				 return true;
			 }
			 // We're already in keyboard mode.
			 if (this.addKeyIfLegal(keyCode)) {
				 this.updateDisplay(false);
			 }
			 return true;
		 }
		 return false;
	 }

	 // Show either Hours or Minutes.
	 private void setCurrentItemShowing(final int index, final boolean animateCircle, final boolean delayLabelAnimate,
			 final boolean announce) {
		 this.mTimePicker.setCurrentItemShowing(index, animateCircle);

		 TextView labelToAnimate;
		 if (index == HOUR_INDEX) {
			 int hours = this.mTimePicker.getHours();
			 if (!this.mIs24HourMode) {
				 hours = hours % 12;
			 }
			 this.mTimePicker.setContentDescription(this.mHourPickerDescription + ": " + hours);
			 if (announce) {
				 Utils.tryAccessibilityAnnounce(this.mTimePicker, this.mSelectHours);
			 }
			 labelToAnimate = this.mHourView;
		 } else {
			 final int minutes = this.mTimePicker.getMinutes();
			 this.mTimePicker.setContentDescription(this.mMinutePickerDescription + ": " + minutes);
			 if (announce) {
				 Utils.tryAccessibilityAnnounce(this.mTimePicker, this.mSelectMinutes);
			 }
			 labelToAnimate = this.mMinuteView;
		 }

		 final int hourColor = (index == HOUR_INDEX) ? this.mBlue : this.mBlack;
		 final int minuteColor = (index == MINUTE_INDEX) ? this.mBlue : this.mBlack;
		 this.mHourView.setTextColor(hourColor);
		 this.mMinuteView.setTextColor(minuteColor);

		 final ObjectAnimator pulseAnimator = Utils.getPulseAnimator(labelToAnimate, 0.85f, 1.1f);
		 if (delayLabelAnimate) {
			 pulseAnimator.setStartDelay(PULSE_ANIMATOR_DELAY);
		 }
		 pulseAnimator.start();
	 }

	 private void setHour(int value, final boolean announce) {
		 String format;
		 if (this.mIs24HourMode) {
			 format = "%02d";
		 } else {
			 format = "%d";
			 value = value % 12;
			 if (value == 0) {
				 value = 12;
			 }
		 }

		 final CharSequence text = String.format(format, value);
		 this.mHourView.setText(text);
		 this.mHourSpaceView.setText(text);
		 if (announce) {
			 Utils.tryAccessibilityAnnounce(this.mTimePicker, text);
		 }
	 }

	 private void setMinute(int value) {
		 if (value == 60) {
			 value = 0;
		 }
		 final CharSequence text = String.format(Locale.getDefault(), "%02d", value);
		 Utils.tryAccessibilityAnnounce(this.mTimePicker, text);
		 this.mMinuteView.setText(text);
		 this.mMinuteSpaceView.setText(text);
	 }

	 public void setOnTimeSetListener(final OnTimeSetListener callback) {
		 this.mCallback = callback;
	 }

	 public void setStartTime(final int hourOfDay, final int minute) {
		 this.mInitialHourOfDay = hourOfDay;
		 this.mInitialMinute = minute;
		 this.mInKbMode = false;
	 }

	 /**
	  * Try to start keyboard mode with the specified key, as long as the timepicker is not in the middle of a
	  * touch-event.
	  *
	  * @param keyCode The key to use as the first press. Keyboard mode will not be started if the key is not legal to
	  * start with. Or, pass in -1 to get into keyboard mode without a starting key.
	  */
	 private void tryStartingKbMode(final int keyCode) {
		 if (this.mTimePicker.trySettingInputEnabled(false) &&
				 ((keyCode == -1) || this.addKeyIfLegal(keyCode))) {
			 this.mInKbMode = true;
			 this.mDoneButton.setEnabled(false);
			 this.updateDisplay(false);
		 }
	 }

	 private void updateAmPmDisplay(final int amOrPm) {
		 if (amOrPm == AM) {
			 this.mAmPmTextView.setText(this.mAmText);
			 Utils.tryAccessibilityAnnounce(this.mTimePicker, this.mAmText);
			 this.mAmPmHitspace.setContentDescription(this.mAmText);
		 } else if (amOrPm == PM) {
			 this.mAmPmTextView.setText(this.mPmText);
			 Utils.tryAccessibilityAnnounce(this.mTimePicker, this.mPmText);
			 this.mAmPmHitspace.setContentDescription(this.mPmText);
		 } else {
			 this.mAmPmTextView.setText(this.mDoublePlaceholderText);
		 }
	 }

	 /**
	  * Update the hours, minutes, and AM/PM displays with the typed times. If the typedTimes is empty, either show an
	  * empty display (filled with the placeholder text), or update from the timepicker's values.
	  *
	  * @param allowEmptyDisplay if true, then if the typedTimes is empty, use the placeholder text. Otherwise, revert to
	  * the timepicker's values.
	  */
	 private void updateDisplay(final boolean allowEmptyDisplay) {
		 if (!allowEmptyDisplay && this.mTypedTimes.isEmpty()) {
			 final int hour = this.mTimePicker.getHours();
			 final int minute = this.mTimePicker.getMinutes();
			 this.setHour(hour, true);
			 this.setMinute(minute);
			 if (!this.mIs24HourMode) {
				 this.updateAmPmDisplay(hour < 12 ? AM : PM);
			 }
			 this.setCurrentItemShowing(this.mTimePicker.getCurrentItemShowing(), true, true, true);
			 this.mDoneButton.setEnabled(true);
		 } else {
			 final Boolean[] enteredZeros = {false, false};
			 final int[] values = this.getEnteredTime(enteredZeros);
			 final String hourFormat = enteredZeros[0] ? "%02d" : "%2d";
			 final String minuteFormat = (enteredZeros[1]) ? "%02d" : "%2d";
			 final String hourStr = (values[0] == -1) ? this.mDoublePlaceholderText :
				 String.format(hourFormat, values[0]).replace(' ', this.mPlaceholderText);
			 final String minuteStr = (values[1] == -1) ? this.mDoublePlaceholderText :
				 String.format(minuteFormat, values[1]).replace(' ', this.mPlaceholderText);
			 this.mHourView.setText(hourStr);
			 this.mHourSpaceView.setText(hourStr);
			 this.mHourView.setTextColor(this.mBlack);
			 this.mMinuteView.setText(minuteStr);
			 this.mMinuteSpaceView.setText(minuteStr);
			 this.mMinuteView.setTextColor(this.mBlack);
			 if (!this.mIs24HourMode) {
				 this.updateAmPmDisplay(values[2]);
			 }
		 }
	 }
}
