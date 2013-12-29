package com.picogram.awesomeness;

import java.util.ArrayList;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;

import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment.NumberPickerDialogHandler;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class DialogMaker extends DialogFragment implements View.OnClickListener {

	int randomHeight, randomWidth, randomColors; // Used in random.
	TextView tvHeight, tvWidth, tvColors; // Used in random.

	public DialogMaker() {
		super();
	}

	public interface OnDialogResultListener {
		public void onDialogResult(Bundle result);
	}

	OnDialogResultListener listener;

	public void setOnDialogResultListner(OnDialogResultListener odrl) {
		Log.d(TAG, "SETTING IT NOW");
		listener = odrl;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		savedInstanceState = getArguments();
		int li = 0;
		String[] colors = null;
		if (savedInstanceState != null) {
			li = (Integer) hasAndGet(savedInstanceState, "layoutId");
			colors = (String[]) hasAndGet(savedInstanceState, "colors");
		}
		final int layoutId = li;
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View v = inflater.inflate(layoutId, null);

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(v);

		// Setup listeners and attributes of dialog UI elements.
		if (layoutId == R.layout.dialog_listview_contextmenu) {
			setupLongClick(v);
		} else if (layoutId == R.layout.dialog_random_picogram) {
			builder
			// Add action buttons
			.setPositiveButton("Done", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Bundle bundle = new Bundle();
					if (layoutId == R.layout.dialog_random_picogram) {
						bundle.putInt("width", randomWidth);
						bundle.putInt("height", randomHeight);
						bundle.putInt("numColors", randomColors);
						bundle.putString("solution", randomPuzzle);
						bundle.putString("name",
								"Random #" + randomPuzzle.hashCode());
						bundle.putString("tags", "random");
					}
					listener.onDialogResult(bundle);
				}
			}).setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			setupRandom(v);
		} else if (layoutId == R.layout.dialog_ranking) {
			// Don't need action buttons.
			setupRating(v);
		} else if (layoutId == R.layout.dialog_save_picogram) {
			setupCreate(v);
		} else if (layoutId == R.layout.dialog_color_choice) {
			setupColorChoice(v, colors);
		}

		return builder.create();
	}

	private Object hasAndGet(Bundle state, String key) {
		if (state.containsKey(key))
			return state.get(key);
		else
			return null;
	}

	public void setupColorChoice(View v, final String[] colors) {
		LinearLayout llFirst = (LinearLayout) v.findViewById(R.id.llColorFirst);
		LinearLayout llSecond = (LinearLayout) v
				.findViewById(R.id.llColorSecond);
		LinearLayout llThird = (LinearLayout) v.findViewById(R.id.llColorThird);
		LinearLayout llFourth = (LinearLayout) v
				.findViewById(R.id.llColorFourth);
		final Activity a = this.getActivity();
		// Layout
		// llFirst [MOVE] [Xs] [Transparent]
		// llSecond [Color] [Color] [Color]
		// llThird [Color] [Color] [Color]
		// llFourth [Color] [Color] [Color]
		Bitmap[] bmColors = getMenuBitmaps(colors);
		final ArrayList<View> ivs = new ArrayList<View>();
		for (int i = 0; i != bmColors.length; ++i) {
			ImageView item = new ImageView(this.getActivity());

			item.setImageBitmap(bmColors[i]);
			if (i > 2)
				item.setBackgroundDrawable(this.getResources().getDrawable(
						R.drawable.dropshadows));
			LinearLayout ll = new LinearLayout(this.getActivity());
			ll.setGravity(Gravity.CENTER);
			ll.addView(item);
			ll.setLayoutParams(new LinearLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
			ivs.add(ll);
			final int position = i;
			item.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					// 2 is always transparent, don't worry ;).\
					boolean isGameplay = true;
					char colorCharacter = 'x';
					if (ivs.indexOf(v.getParent()) == 0) {
						// Moving.
						isGameplay = false;
					} else if (ivs.indexOf(v.getParent()) == 1) {
						isGameplay = true;
						colorCharacter = 'x';
					} else {
						isGameplay = true;
						// Minus 2 for the X's and movement.
						colorCharacter = ((ivs.indexOf(v.getParent()) - 2) + "")
								.charAt(0);
					}
					Bundle bundle = new Bundle();
					Log.d(TAG, "IndexOf: " + (ivs.indexOf(v.getParent()))
							+ " Char: " + colorCharacter + " Game: "
							+ isGameplay);
					bundle.putBoolean("isGameplay", isGameplay);
					bundle.putChar("colorCharacter", colorCharacter);
					Log.d(TAG, "COLOR: " + colorCharacter);
					listener.onDialogResult(bundle);
				}

			});
			if (i > 2)
				item.setOnLongClickListener(new View.OnLongClickListener() {

					public boolean onLongClick(final View v) {
						final AmbilWarnaDialog dialog = new AmbilWarnaDialog(a,
								Integer.parseInt(colors[ivs.indexOf(v
										.getParent()) - 2]),
								new OnAmbilWarnaListener() {

									public void onCancel(
											final AmbilWarnaDialog dialog) {
										// Do nothing.
									}

									public void onOk(
											final AmbilWarnaDialog dialog,
											int color) {
										Bundle bundle = new Bundle();
										int[] cols = new int[colors.length];
										for (int i = 0; i != cols.length; ++i)
											cols[i] = Integer
													.parseInt(colors[i]);
										cols[ivs.indexOf(v.getParent()) - 2] = color;
										bundle.putIntArray("colors", cols);
										bundle.putInt("color",
												ivs.indexOf(v.getParent()) - 2);
										listener.onDialogResult(bundle);
									}
								});
						dialog.show();
						return true;
					}
				});
		}
		// All ImageViews are setup. Now add them.
		for (int i = 0; i != ivs.size(); ++i) {
			if (i < 3) {
				llFirst.addView(ivs.get(i));
			} else if (i < 6) {
				llSecond.addView(ivs.get(i));
			} else if (i < 9) {
				llThird.addView(ivs.get(i));
			} else {
				llFourth.addView(ivs.get(i));
			}
		}
	}

	private Bitmap[] getMenuBitmaps(String[] colors) {
		// +3 for movement and x's and transparent.
		Bitmap[] result = new Bitmap[colors.length + 2];
		result[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
				this.getResources(), R.drawable.move), 100, 100, true);
		result[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
				this.getResources(), R.drawable.xs), 100, 100, true);
		for (int i = 0; i != colors.length; ++i) {
			Bitmap fullColor = Bitmap.createBitmap(1, 1,
					Bitmap.Config.ARGB_8888);
			final int[] rgb = this.getRGB(colors[i]);
			if (rgb[0] == 0) {// This is alpha.
				// For transparency.
				fullColor = BitmapFactory.decodeResource(this.getResources(),
						R.drawable.transparent);
			} else {
				fullColor.setPixel(0, 0, Color.rgb(rgb[1], rgb[2], rgb[3]));

			}
			fullColor = Bitmap.createScaledBitmap(fullColor, 100, 100, true);
			// +2 for the X's and movement and transparent.
			result[i + 2] = fullColor;
		}
		return result;
	}

	private int[] getRGB(final String s) {
		int i = Integer.parseInt(s);
		final int a = (i >> 24) & 0xff;
		final int r = (i >> 16) & 0xff;
		final int g = (i >> 8) & 0xff;
		final int b = (i & 0xff);
		return new int[] { a, r, g, b };
	}

	public void setupCreate(View v) {
	}

	public void setupLongClick(View v) {
		Button bClear = (Button) v.findViewById(R.id.bClearPuzzle);
		Button bDelete = (Button) v.findViewById(R.id.bDeletePuzzle);
		Button bNewRate = (Button) v.findViewById(R.id.bSubmitNewRank);
		bClear.setOnClickListener(this);
		bDelete.setOnClickListener(this);
		bNewRate.setOnClickListener(this);
	}

	public void setupRating(View v) {
	}

	public void setupRandom(View v) {
		Button bHeight = (Button) v.findViewById(R.id.bRandomHeight);
		Button bWidth = (Button) v.findViewById(R.id.bRandomWidth);
		Button bColors = (Button) v.findViewById(R.id.bRandomNumColors);

		bHeight.setOnClickListener(this);
		bWidth.setOnClickListener(this);
		bColors.setOnClickListener(this);

		tvHeight = (TextView) v.findViewById(R.id.tvRandomHeight);
		tvWidth = (TextView) v.findViewById(R.id.tvRandomWidth);
		tvColors = (TextView) v.findViewById(R.id.tvRandomNumColors);

		randomHeight = (int) Math.floor((Math.random() * 24)) + 2;
		randomWidth = (int) Math.floor((Math.random() * 24)) + 2;
		randomColors = (int) Math.floor((Math.random() * 8)) + 2;

		etName = (EditText) v.findViewById(R.id.etRandomName);
		etTag = (EditText) v.findViewById(R.id.etRandomTags);
		etTag.setText("Random");
		etName.setOnKeyListener(null);
		etTag.setOnKeyListener(null);

		updateViews();
	}

	public void updateViews() {
		tvHeight.setText(randomHeight + "");
		tvWidth.setText(randomWidth + "");
		tvColors.setText(randomColors + "");
		generateRandom();
		etName.setText("Random: #" + randomPuzzle.hashCode());
	}

	String randomPuzzle = "";

	private void generateRandom() {
		randomPuzzle = "";
		for (int i = 0; i != randomHeight; ++i) {
			for (int j = 0; j != randomWidth; ++j) {
				randomPuzzle += ((int) (Math.random() * randomColors));
			}
		}
	}

	EditText etName, etTag;

	public void onClick(View v) {
		Bundle bundle = new Bundle();
		switch (v.getId()) {
		case R.id.bSubmitNewRank:
			bundle.putInt("resultInt", 0);
			listener.onDialogResult(bundle);
			this.getDialog().dismiss();
			break;
		case R.id.bClearPuzzle:
			bundle.putInt("resultInt", 1);
			listener.onDialogResult(bundle);
			this.getDialog().dismiss();
			break;
		case R.id.bDeletePuzzle:
			bundle.putInt("resultInt", 2);
			listener.onDialogResult(bundle);
			this.getDialog().dismiss();
			break;
		case R.id.bRandomHeight:
		case R.id.bRandomWidth:
		case R.id.bRandomNumColors:
			showNumberDialog(v.getId());
			break;
		default:
			// Ignore if doesn't exist.
			break;
		}
	}

	Handler handler = new Handler();

	private void showNumberDialog(final int id) {
		NumberPickerBuilder npb = new NumberPickerBuilder()
				.setStyleResId(R.style.MyCustomBetterPickerTheme)
				.setFragmentManager(this.getFragmentManager())
				.setPlusMinusVisibility(View.INVISIBLE)
				.setDecimalVisibility(View.INVISIBLE);
		if (id == R.id.bRandomNumColors) {
			npb.setMinNumber(2);
			npb.setMaxNumber(10);
		} else {
			if (id == R.id.bRandomWidth)
				Crouton.makeText(this.getActivity(), "Give us a new width.",
						Style.INFO).show();
			else
				Crouton.makeText(this.getActivity(), "Give us a new height.",
						Style.INFO).show();
			npb.setMaxNumber(25);
			npb.setMinNumber(1);
		}
		npb.addNumberPickerDialogHandler(new NumberPickerDialogHandler() {

			public void onDialogNumberSet(int reference, final int number,
					double decimal, boolean isNegative, double fullNumber) {
				handler.post(new Runnable() {
					public void run() {
						if (id == R.id.bRandomNumColors) {
							randomColors = number;
						} else if (id == R.id.bRandomWidth) {
							randomWidth = number;
						} else if (id == R.id.bRandomHeight) {
							randomHeight = number;
						}
						updateViews();
					}
				});
			}
		});
		npb.show();
	}

	private static final String TAG = "DialogMaker";
}
