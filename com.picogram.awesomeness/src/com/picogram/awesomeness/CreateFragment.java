
package com.picogram.awesomeness;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment.NumberPickerDialogHandler;
import com.edmodo.cropper.CropImageView;
import com.edmodo.rangebar.RangeBar;
import com.edmodo.rangebar.RangeBar.OnRangeBarChangeListener;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;

import java.util.ArrayList;
import java.util.Arrays;
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
	Button bWidthChange, bHeightChange, bColorChange, bColorSelector, bSubmit, bToolbox, bCustom, bFile, bURL, bCamera;
	TextView tvWidth, tvHeight, tvColor, tvTags, tvInstructions;
	CropImageView cropper;
	TouchImageView tivGameOne, tivGameTwo, tivGameThree, tivGameFour;
	EditText etName;
	Spinner spinDifficulty;
	ChipsMultiAutoCompleteTextview autoTags;
	int selectedColor, width = 3, height = 3, numColor = 2;
	RangeBar rangeColor;
	Bitmap original;

	private int position;
	int currentChangedColorIndex = 0;
	Handler handler;
	private final Vector<AlertDialog> dialogs = new Vector<AlertDialog>();

	public void onClick(final View v) {
		final CreateActivity msca = (CreateActivity) this.getActivity();
		final AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		for (final AlertDialog ad : this.dialogs) {
			ad.dismiss();
		}
		switch (v.getId())
		{
			case R.id.bCamera:
				msca.runCamera();
				break;
			case R.id.bURL:
				msca.runURL();
				break;
			case R.id.bFile:
				msca.runFile();
				break;
			case R.id.bCustom:
				msca.bmInitial = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_4444);
				msca.bmCropped = msca.bmInitial;
				msca.pager.setCurrentItem(2, true);
				break;
			case R.id.bSubmit:
				final Picogram p = new Picogram();
				p.setAuthor(Util.id(this.getActivity()));
				final TouchImageView tiv = (TouchImageView) msca.findViewById(R.id.tivGameFour);
				String cols = "";
				for (final int i : msca.newColors) {
					cols += i + ",";
				}
				p.setColors(cols);
				p.setDiff(this.spinDifficulty.getSelectedItemPosition() + "");
				p.setHeight(tiv.gHeight + "");
				p.setName(this.etName.getText().toString());
				p.setNumberOfColors(msca.numColors + "");
				p.setNumberOfRatings(1);
				p.setRating(1 + "");
				p.setSolution(msca.fineTunedSolution);
				p.setStatus(0 + "");
				p.setWidth(tiv.gWidth + "");
				p.nullsToValue(this.getActivity());
				p.save();
				final SQLitePicogramAdapter sql = new SQLitePicogramAdapter(this.getActivity(), "Picograms", null, 1);
				sql.addUserPicogram(p);
				sql.close();
				final SQLiteTagAdapter tagSql = new SQLiteTagAdapter(this.getActivity(), "Tags", null, 1);
				for (final String tag : this.autoTags.getText().toString().split(",")) {
					if (tag.trim().length() != 0)
					{
						tagSql.insertCreate(tag.trim());
						final PicogramTag pt = new PicogramTag();
						pt.setID(p.getID());
						pt.setTag(tag.trim());
						pt.save();
					}
				}
				tagSql.close();
				this.getActivity().finish();
				break;
			case R.id.bToolbox:
				LinearLayout ll = new LinearLayout(this.getActivity());
				ll.setOrientation(LinearLayout.HORIZONTAL);
				for (int i = 0; i != msca.numColors; ++i)
				{
					final Button b = new Button(this.getActivity());
					final int color = msca.newColors[i];
					final int j = i;
					b.setOnClickListener(new View.OnClickListener() {

						public void onClick(final View v) {
							((TouchImageView) CreateFragment.this.getActivity().findViewById(R.id.tivGameThree)).isGameplay = true;
							((TouchImageView) CreateFragment.this.getActivity().findViewById(R.id.tivGameThree)).colorCharacter = ("" + j).charAt(0);
							for (final AlertDialog ad : CreateFragment.this.dialogs) {
								ad.dismiss();
							}
						}
					});
					b.setBackgroundDrawable(this.getActivity().getResources().getDrawable(R.drawable.drop_shadow));
					if (color == Color.TRANSPARENT) {
						// b.setBackgroundResource(R.drawable.transparent);
					} else {
						b.setBackgroundColor(color);
					}
					ll.addView(b);
				}
				builder.setTitle("Select color to draw");
				builder.setView(ll);
				this.dialogs.add(builder.show());
				break;
			case R.id.bWidthChange:
			case R.id.bHeightChange:
			case R.id.bColorChange:
				this.showNumberDialog(v.getId());
				break;
			case R.id.bColorSelector:
				// Prompt user to change color.
				this.bColorSelector.setText("");
				ll = new LinearLayout(this.getActivity());
				ll.setOrientation(LinearLayout.VERTICAL);
				final ArrayList<Button> buttons = new ArrayList();
				final ArrayList<RangeBar> bars = new ArrayList();
				final int []pc = Arrays.copyOfRange(msca.newColors, 0, msca.newColors.length);
				for (int i = 0; i != msca.numColors; ++i)
				{
					final Button b = new Button(this.getActivity());
					final RangeBar rb = new RangeBar(this.getActivity());
					rb.setTickCount(256);

					buttons.add(b);
					bars.add(rb);
					int color = msca.newColors[i];
					final int j = i;
					b.setOnClickListener(new View.OnClickListener() {

						public void onClick(final View v) {
							int tempColor = msca.newColors[j];
							// TODO: This should let you change the color.
							final AmbilWarnaDialog dialog = new AmbilWarnaDialog(CreateFragment.this.getActivity(),
									pc[j],
									new OnAmbilWarnaListener() {

								public void onCancel(
										final AmbilWarnaDialog dialog) {
									// Do nothing.
								}

								public void onOk(
										final AmbilWarnaDialog dialog,
										final int color) {
									msca.originalColors[j] = color;
									msca.newColors[j] = color;
									pc[j] = color;
									((CreateActivity) CreateFragment.this.getActivity()).updateAllTouchImageViews();
									buttons.get(j).setBackgroundColor(color);
									b.setBackgroundColor(color);
									Log.d(TAG, "New Color: " + color + " " + j);
								}
							});
							Log.d(TAG, "Changing color: " + tempColor + " " + j);
							dialog.show();
						}
					});
					b.setBackgroundDrawable(this.getActivity().getResources().getDrawable(R.drawable.drop_shadow));
					if (color == Color.TRANSPARENT) {
						color = Color.WHITE;
					}
					b.setBackgroundColor(color);
					// rb.setThumbColorNormal(color);
					// rb.setConnectingLineColor(color);
					// rb.setThumbIndices(i * (256 / msca.newColors.length), (i + 1) * (int) Math.floor((256 / msca.newColors.length)));
					if (j == msca.numColors) {
						// Make sure we're going to the full 256.
						rb.setThumbIndices(i * (256 / msca.newColors.length), 256);
					}
					final OnRangeBarChangeListener listen = new OnRangeBarChangeListener() {

						public void onIndexChangeListener(final RangeBar rangeBar, final int leftThumbIndex, final int rightThumbIndex) {
							if (j == 0)
							{
								// Only change the one below it
								rangeBar.setOnRangeBarChangeListener(null);
								rangeBar.setThumbIndices(0, rightThumbIndex);
								rangeBar.setOnRangeBarChangeListener(this);
							}
							else if (j == msca.numColors) {
								// Only change the one above it.
								rangeBar.setThumbIndices(leftThumbIndex, 256);
							}
							else
							{
								// Change both.
							}
						}
					};

					rb.setOnRangeBarChangeListener(listen);
					rb.setVisibility(View.GONE); // Make invisible. Not needed for MVP.
					final LinearLayout row = new LinearLayout(this.getActivity());
					row.setOrientation(LinearLayout.HORIZONTAL);
					row.addView(b);
					row.addView(rb);
					ll.addView(row);
					// TODO Make the range bar have the colors.
				}
				builder.setTitle("Select color to change strength");
				builder.setView(ll);
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
		final LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);

		final FrameLayout fl = new FrameLayout(this.getActivity());
		fl.setLayoutParams(params);
		View view;
		if (this.position == 5)
		{
			view = inflater.inflate(R.layout.include_create_step_six, null);
			this.etName = (EditText) view.findViewById(R.id.etName);
			this.spinDifficulty = (Spinner) view.findViewById(R.id.spinDifficulty);
			this.autoTags = (ChipsMultiAutoCompleteTextview) view.findViewById(R.id.cmactv);
			this.tvTags = (TextView) view.findViewById(R.id.tvTags);
			this.tivGameFour = (TouchImageView) view.findViewById(R.id.tivGameFour);
			this.bSubmit = (Button) view.findViewById(R.id.bSubmit);
			this.bSubmit.setOnClickListener(this);
			this.setupAutoTags();

		} else if (this.position == 4)
		{
			view = inflater.inflate(R.layout.include_create_step_five, null);
			this.tivGameThree = (TouchImageView) view.findViewById(R.id.tivGameThree);
			this.bToolbox = (Button) view.findViewById(R.id.bToolbox);
			this.bToolbox.setOnClickListener(this);
		} else if (this.position == 3)
		{
			view = inflater.inflate(R.layout.include_create_step_four, null);
			this.bColorSelector = (Button) view.findViewById(R.id.bColorSelector);
			this.bColorSelector.setOnClickListener(this);
			this.bColorSelector.setText("Click me");

			this.tivGameTwo = (TouchImageView) view.findViewById(R.id.tivGameTwo);

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
			// this.updateAllTouchImageViews((CreateActivity) this.getActivity());

		} else if (this.position == 1)
		{
			view = inflater.inflate(R.layout.include_create_step_two, null);
			this.cropper = (CropImageView) view.findViewById(R.id.cropImageView);
			this.cropper.setGuidelines(2);

		} else if (this.position == 0)
		{
			view = inflater.inflate(R.layout.include_create_step_one, null);
			this.bCamera = (Button) view.findViewById(R.id.bCamera);
			this.bFile = (Button) view.findViewById(R.id.bFile);
			this.bCustom = (Button) view.findViewById(R.id.bCustom);
			this.bURL = (Button) view.findViewById(R.id.bURL);
			this.tvInstructions = (TextView) view.findViewById(R.id.tvInstructions);

			this.bCamera.setOnClickListener(this);
			this.bFile.setOnClickListener(this);
			this.bURL.setOnClickListener(this);
			this.bCustom.setOnClickListener(this);

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
		// this.updateAllTouchImageViews((CreateActivity) this.getActivity());
		return fl;
	}

	public void onIndexChangeListener(final RangeBar rangeBar, final int leftThumbIndex, final int rightThumbIndex) {
	}

	private void setupAutoTags() {

		final SQLiteTagAdapter tagSql = new SQLiteTagAdapter(this.getActivity(), "Tags", null, 1);
		final String[] array = tagSql.getTags();
		tagSql.close();

		this.autoTags.setAdapter(new ArrayAdapter<String>(this.getActivity(),
				android.R.layout.simple_dropdown_item_1line, array));
		this.autoTags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
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
						a.hasFineTuned = false;
						a.fineTunedSolution = "";
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
						a.updateAllTouchImageViews();
					}
				});
			}
		});
		npb.show();
	}
}
