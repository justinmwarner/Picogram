
package com.picogram.awesomeness;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment.NumberPickerDialogHandler;
import com.edmodo.cropper.CropImageView;
import com.edmodo.rangebar.RangeBar;
import com.edmodo.rangebar.RangeBar.OnRangeBarChangeListener;

import java.util.ArrayList;
import java.util.Vector;

public class CreateFragment extends Fragment implements OnClickListener, OnRangeBarChangeListener {

	private static final String ARG_POSITION = "position";
	private static final String TAG = "SuperAwesomeCardFragment";

	public static CreateFragment newInstance(final int position) {
		final CreateFragment f = new CreateFragment();
		final Bundle b = new Bundle();
		b.putInt(ARG_POSITION, position);
		f.setArguments(b);
		return f;
	}

	ImageView ivInitial;
	Button bWidthChange, bHeightChange, bColorChange, bColorSelector;
	TextView tvWidth, tvHeight, tvColor, tvTags, tvInstructions;
	CropImageView cropper;
	TouchImageView tivGameOne, tivGameTwo, tivGameThree, tivGameFour;
	EditText etName;
	Spinner spinDifficulty;
	MultiAutoCompleteTextView autoTags;
	int selectedColor, width = 20, height = 20, numColor = 2;
	RangeBar rangeColor;
	Bitmap original;

	private int position;
	int currentChangedColorIndex = 0;
	Handler handler;
	private final Vector<AlertDialog> dialogs = new Vector<AlertDialog>();

	public void onClick(final View v) {
		switch (v.getId())
		{
			case R.id.bWidthChange:
			case R.id.bHeightChange:
			case R.id.bColorChange:
				this.showNumberDialog(v.getId());
				break;
			case R.id.bColorSelector:
				// Prompt user to change color.
				this.bColorSelector.setText("");
				final LayoutInflater inflater = this.getActivity().getLayoutInflater();
				final CreateActivity msca = (CreateActivity) this.getActivity();
				final View dialoglayout = inflater.inflate(R.layout.dialog_color_choice, (ViewGroup) this.getActivity().getCurrentFocus());
				final ArrayList<Button> buttons = new ArrayList();
				for (int i = 0; i != msca.numColors; ++i)
				{
					final Button b = new Button(this.getActivity());
					final int color = msca.newColors[i];
					final int j = i;
					buttons.add(b);
					b.setOnClickListener(new View.OnClickListener() {

						public void onClick(final View v) {
							CreateFragment.this.handler.post(new Runnable() {

								public void run() {
									Log.d(TAG, "Click click");
									CreateFragment.this.bColorSelector.setBackgroundColor(color);
									CreateFragment.this.currentChangedColorIndex = j;
									CreateFragment.this.rangeColor.setVisibility(View.VISIBLE);

									for (final AlertDialog dialog : CreateFragment.this.dialogs) {
										if (dialog.isShowing()) {
											dialog.dismiss();
										}
									}
								}
							});
						}
					});
					b.setBackgroundColor(color);
					if (i < 3) {
						((LinearLayout) dialoglayout.findViewById(R.id.llColorFirst)).addView(b);
					} else if (i < 6)
					{
						((LinearLayout) dialoglayout.findViewById(R.id.llColorSecond)).addView(b);
					} else if (i < 9) {
						((LinearLayout) dialoglayout.findViewById(R.id.llColorThird)).addView(b);
					}
					else {
						((LinearLayout) dialoglayout.findViewById(R.id.llColorFourth)).addView(b);
					}
				}
				final AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
				builder.setTitle("Select color to change strength");
				builder.setView(dialoglayout);
				this.dialogs.add(builder.show());
				break;
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.position = this.getArguments().getInt(ARG_POSITION);
		this.handler = new Handler();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {
		final LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		final FrameLayout fl = new FrameLayout(this.getActivity());
		fl.setLayoutParams(params);
		View view;
		if (this.position == 6)
		{
			// TODO We're finished creating puzzle, make sure everything is in order and if so, finish. Else, change to bad page.
			view = null;
			this.getActivity().finish();

		}
		else if (this.position == 5)
		{
			view = inflater.inflate(R.layout.include_create_step_six, null);
			this.etName = (EditText) view.findViewById(R.id.etName);
			this.spinDifficulty = (Spinner) view.findViewById(R.id.spinDifficulty);
			this.autoTags = (MultiAutoCompleteTextView) view.findViewById(R.id.mactvTags);
			this.tvTags = (TextView) view.findViewById(R.id.tvTags);
			this.tivGameFour = (TouchImageView) view.findViewById(R.id.tivGameFour);
			fl.addView(view);
		} else if (this.position == 4)
		{
			view = inflater.inflate(R.layout.include_create_step_five, null);
			this.tivGameThree = (TouchImageView) view.findViewById(R.id.tivGameThree);
			fl.addView(view);
		} else if (this.position == 3)
		{
			view = inflater.inflate(R.layout.include_create_step_four, null);
			this.bColorSelector = (Button) view.findViewById(R.id.bColorSelector);
			this.bColorSelector.setOnClickListener(this);
			this.bColorSelector.setText("Click me");
			this.rangeColor = (RangeBar) view.findViewById(R.id.rb);
			this.rangeColor.setOnRangeBarChangeListener(this);
			this.rangeColor.setTickCount(256 / ((CreateActivity) this.getActivity()).numColors);
			this.rangeColor.setVisibility(View.INVISIBLE);
			this.tivGameTwo = (TouchImageView) view.findViewById(R.id.tivGameTwo);
			fl.addView(view);
		} else if (this.position == 2)
		{
			view = inflater.inflate(R.layout.include_create_step_three, null);
			this.bWidthChange = (Button) view.findViewById(R.id.bWidthChange);
			this.bHeightChange = (Button) view.findViewById(R.id.bHeightChange);
			this.bColorChange = (Button) view.findViewById(R.id.bColorChange);

			this.bWidthChange.setOnClickListener(this);
			this.bHeightChange.setOnClickListener(this);
			this.bColorChange.setOnClickListener(this);
			this.tivGameOne = (TouchImageView) view.findViewById(R.id.tivGameOne);
			this.updateAllToucImageViews();
			fl.addView(view);
		} else if (this.position == 1)
		{
			view = inflater.inflate(R.layout.include_create_step_two, null);
			this.cropper = (CropImageView) view.findViewById(R.id.cropImageView);
			this.cropper.setGuidelines(2);
			fl.addView(view);
		} else if (this.position == 0)
		{
			view = inflater.inflate(R.layout.include_create_step_one, null);
			this.ivInitial = (ImageView) view.findViewById(R.id.ivPrimaryPreview);
			this.tvInstructions = (TextView) view.findViewById(R.id.tvInstructions);
			fl.addView(view);
		}
		else
		{
			final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, this
					.getResources()
					.getDisplayMetrics());
			view = new TextView(this.getActivity());
			params.setMargins(margin, margin, margin, margin);
			view.setLayoutParams(params);
			view.setLayoutParams(params);
			((TextView) view).setGravity(Gravity.CENTER);
			view.setBackgroundResource(R.drawable.background_card);
			((TextView) view).setText("Internal error.  Please report");
		}
		fl.removeAllViews();
		fl.addView(view);
		return fl;
	}

	public void onIndexChangeListener(final RangeBar rangeBar, final int leftThumbIndex, final int rightThumbIndex) {
		// TODO Auto-generated method stub

	}

	public void setOriginalImage(final Bitmap original, final Activity a) {
		this.original = original;
		new Thread(new Runnable() {

			public void run() {
				a.runOnUiThread(new Runnable() {

					public void run() {
						while (CreateFragment.this.ivInitial == null) {
							if (a != null) {
								CreateFragment.this.ivInitial = (ImageView) a.findViewById(R.id.ivPrimaryPreview);
								CreateFragment.this.tvInstructions = (TextView) a.findViewById(R.id.tvInstructions);
								CreateFragment.this.cropper = (CropImageView) a.findViewById(R.id.cropImageView);
							}
						}
						CreateFragment.this.tvInstructions.setText("Swipe for next step");
						CreateFragment.this.ivInitial.setImageBitmap(original);
						CreateFragment.this.cropper.setImageBitmap(original);
					}
				});
			}
		}).start();
	}

	private void showNumberDialog(final int id) {
		final NumberPickerBuilder npb = new NumberPickerBuilder()
		.setStyleResId(R.style.MyCustomBetterPickerTheme)
		.setFragmentManager(this.getActivity().getSupportFragmentManager())
		.setPlusMinusVisibility(View.INVISIBLE)
		.setDecimalVisibility(View.INVISIBLE);
		if (id == R.id.bColorChange) {
			npb.setMinNumber(2);
			npb.setMaxNumber(10);
		} else {
			npb.setMaxNumber(100);
			npb.setMinNumber(1);
		}
		npb.addNumberPickerDialogHandler(new NumberPickerDialogHandler() {

			public void onDialogNumberSet(final int reference, final int number,
					final double decimal, final boolean isNegative, final double fullNumber) {
				Util.log("Reference:  " + reference + " Number: " + number
						+ " Decimal: " + decimal + " Neg: " + isNegative
						+ " FullNum: " + fullNumber);
				CreateFragment.this.handler.post(new Runnable() {
					public void run() {
						final CreateActivity a = (CreateActivity) CreateFragment.this.getActivity();
						if (id == R.id.bColorChange) {
							CreateFragment.this.numColor = number;
							a.numColors = number;
						} else if (id == R.id.bWidthChange) {
							CreateFragment.this.width = number;
							a.width = number;
						} else if (id == R.id.bHeightChange) {
							CreateFragment.this.height = number;
							a.height = number;
						}
						CreateFragment.this.updateAllToucImageViews();
					}
				});
			}
		});
		npb.show();
	}

	private void updateAllToucImageViews() {
		final Bundle b = ((CreateActivity) this.getActivity()).alterPhoto();
		if (b != null) {
			b.putString("current", b.getString("solution"));
			b.putBoolean("refresh", true);
		}
		if (this.tivGameOne != null) {
			this.tivGameOne.setPicogramInfo(b);
		}
		if (this.tivGameTwo != null) {
			this.tivGameTwo.setPicogramInfo(b);
		}
		if (this.tivGameThree != null) {
			this.tivGameThree.setPicogramInfo(b);
		}
		if (this.tivGameFour != null) {
			this.tivGameFour.setPicogramInfo(b);
		}
	}

}
