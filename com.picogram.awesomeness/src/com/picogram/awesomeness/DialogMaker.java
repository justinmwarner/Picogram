
package com.picogram.awesomeness;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment.NumberPickerDialogHandler;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import java.util.ArrayList;

public class DialogMaker extends DialogFragment implements View.OnClickListener {

	public interface OnDialogResultListener {
		public void onDialogResult(Bundle result);
	}
	int randomHeight, randomWidth, randomColors; // Used in random.

	TextView tvHeight, tvWidth, tvColors; // Used in random.

	OnDialogResultListener listener;

	String randomPuzzle = "";

	EditText etName, etTag;

	Handler handler = new Handler();

	private static final String TAG = "DialogMaker";

	public DialogMaker() {
		super();
	}

	private void generateRandom() {
		this.randomPuzzle = "";
		for (int i = 0; i != this.randomHeight; ++i) {
			for (int j = 0; j != this.randomWidth; ++j) {
				this.randomPuzzle += ((int) (Math.random() * this.randomColors));
			}
		}
	}

	private Bitmap[] getMenuBitmaps(final String[] colors) {
		// +3 for movement and x's and transparent.
		final Bitmap[] result = new Bitmap[colors.length + 2];
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
		final int i = Integer.parseInt(s);
		final int a = (i >> 24) & 0xff;
		final int r = (i >> 16) & 0xff;
		final int g = (i >> 8) & 0xff;
		final int b = (i & 0xff);
		return new int[] {
				a, r, g, b
		};
	}

	private Object hasAndGet(final Bundle state, final String key) {
		if (state.containsKey(key)) {
			return state.get(key);
		} else {
			return null;
		}
	}

	public void onClick(final View v) {
		final Bundle bundle = new Bundle();
		switch (v.getId()) {
			case R.id.bSubmitNewRank:
				bundle.putInt("resultInt", 0);
				this.listener.onDialogResult(bundle);
				this.getDialog().dismiss();
				break;
			case R.id.bClearPuzzle:
				bundle.putInt("resultInt", 1);
				this.listener.onDialogResult(bundle);
				this.getDialog().dismiss();
				break;
			case R.id.bDeletePuzzle:
				bundle.putInt("resultInt", 2);
				this.listener.onDialogResult(bundle);
				this.getDialog().dismiss();
				break;
			case R.id.bRandomHeight:
			case R.id.bRandomWidth:
			case R.id.bRandomNumColors:
				this.showNumberDialog(v.getId());
				break;
			default:
				// Ignore if doesn't exist.
				break;
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		savedInstanceState = this.getArguments();
		int li = 0;
		String[] colors = null;
		if (savedInstanceState != null) {
			li = (Integer) this.hasAndGet(savedInstanceState, "layoutId");
			colors = (String[]) this.hasAndGet(savedInstanceState, "colors");
		}
		final int layoutId = li;
		final AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		// Get the layout inflater
		final LayoutInflater inflater = this.getActivity().getLayoutInflater();
		final View v = inflater.inflate(layoutId, null);

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(v);

		// Setup listeners and attributes of dialog UI elements.
		if (layoutId == R.layout.dialog_listview_contextmenu) {
			this.setupLongClick(v);
		} else if (layoutId == R.layout.dialog_random_picogram) {
			builder
			// Add action buttons
			.setPositiveButton("Done", new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog, final int id) {
					final Bundle bundle = new Bundle();
					if (layoutId == R.layout.dialog_random_picogram) {
						bundle.putInt("width", DialogMaker.this.randomWidth);
						bundle.putInt("height", DialogMaker.this.randomHeight);
						bundle.putInt("numColors", DialogMaker.this.randomColors);
						bundle.putString("solution", DialogMaker.this.randomPuzzle);
						bundle.putString("name",
								"Random #" + DialogMaker.this.randomPuzzle.hashCode());
						bundle.putString("tags", "random");
					}
					DialogMaker.this.listener.onDialogResult(bundle);
				}
			}).setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog, final int id) {
					dialog.cancel();
				}
			});
			this.setupRandom(v);
		} else if (layoutId == R.layout.dialog_ranking) {
			// Don't need action buttons.
			this.setupRating(v);
		} else if (layoutId == R.layout.dialog_save_picogram) {
			this.setupCreate(v);
		} else if (layoutId == R.layout.dialog_color_choice) {
			this.setupColorChoice(v, colors);
		} else if (layoutId == R.layout.dialog_tutorial) {
			final ImageView iv = (ImageView) v
					.findViewById(R.id.ivInstructions);
			final TextView tv = (TextView) v.findViewById(R.id.tvInstructions);
			final int resources[] = {
					R.drawable.tutorial_step_one,
					R.drawable.tutorial_step_two,
					R.drawable.tutorial_step_three,
					R.drawable.tutorial_step_four,
					R.drawable.tutorial_step_five,
					R.drawable.tutorial_step_six,
					R.drawable.tutorial_step_seven
			};
			final String[] prompts = {
					"The 3 represents three consequtive blocks in that row, as the 1's represent a block in the column.",
					"Thus we get...  Notice the columns are all one and have one filled in block per column.  We can have any amount of white space on either side of conseutive blocks.",
					"Now on this puzzle, 2 2 means we have two consequtive blocks with any amount of white space between, then two more consequtive blocks.  If space permit, we could have any amount of white space on either side.",
					"However, the puzzle is small enough so that only one white spot remains.",
					"This is also a valid solution. X's are ignored as white space (Although they're black colored in game).  Notice, also, the colors. Grey goes with grey, thus this is valid.",
					"This is just an example of a full puzzle.",
					"This is another but with colors. Notice the order the colors are in and the numbers going with it."
			};
			tv.setText(prompts[0]);
			iv.setImageBitmap(BitmapFactory.decodeResource(this.getResources(),
					resources[0]));
			// iv.setBackgroundResource(R.drawable.spaceman);
			// AnimationDrawable progressAnimation = (AnimationDrawable) iv
			// .getBackground();
			// progressAnimation.start();

			// Add action buttons
			((Button) v.findViewById(R.id.bNext))
			.setOnClickListener(new View.OnClickListener() {
				public void onClick(final View view) {
					if (((Button) view).getText().toString()
							.equals("Done")) {
						Crouton.makeText(
								DialogMaker.this.getActivity(),
								"You can view this again in the Prefs tab.  Good luck!",
								Style.INFO).show();
						DialogMaker.this.getDialog().dismiss();
						Util.getPreferences(DialogMaker.this.getActivity()).edit()
						.putBoolean("isFirst", false).commit();
						return;
					}
					int currentStep = -1;
					for (int i = 0; i != prompts.length; ++i) {
						if (prompts[i].equals(tv.getText().toString())) {
							currentStep = i + 1;
						}
					}
					if (currentStep == -1) {
						currentStep = 0;
					}
					iv.setImageBitmap(BitmapFactory.decodeResource(
							DialogMaker.this.getResources(), resources[currentStep]));
					tv.setText(prompts[currentStep]);
					currentStep++;
					if (currentStep == prompts.length) {
						((Button) view).setText("Done");

					}
					((Button) v.findViewById(R.id.bLater))
					.setText("Previous");
				}
			});

			((Button) v.findViewById(R.id.bNever))
			.setOnClickListener(new View.OnClickListener() {
				public void onClick(final View view) {

					Crouton.makeText(
							DialogMaker.this.getActivity(),
							"You can view this in the Prefs tab.  Good luck!",
							Style.INFO).show();
					Util.getPreferences(DialogMaker.this.getActivity()).edit()
					.putBoolean("isFirst", false).commit();
					DialogMaker.this.getDialog().dismiss();
				}
			});

			((Button) v.findViewById(R.id.bLater))
			.setOnClickListener(new View.OnClickListener() {

				public void onClick(final View view) {
					if (((Button) view).getText().toString()
							.equals("Previous")) {
						((Button) v.findViewById(R.id.bNext))
						.setText("Next");
						int currentStep = -1;
						for (int i = 0; i != prompts.length; ++i) {
							if (prompts[i].equals(tv.getText()
									.toString())) {
								currentStep = i - 1;
							}
						}
						tv.setText(prompts[currentStep]);
						iv.setImageBitmap(BitmapFactory.decodeResource(
								DialogMaker.this.getResources(), resources[currentStep]));
						if (currentStep == 0) {
							((Button) v.findViewById(R.id.bLater))
							.setText("Later");
						}

					} else {
						Crouton.makeText(
								DialogMaker.this.getActivity(),
								"We'll show this next app start. You can also view this in Prefs",
								Style.INFO).show();
						Util.getPreferences(DialogMaker.this.getActivity()).edit()
						.putBoolean("isFirst", true).commit();
						DialogMaker.this.getDialog().dismiss();
					}
				}
			});
		}
		return builder.create();
	}

	public void setOnDialogResultListner(final OnDialogResultListener odrl) {
		this.listener = odrl;
	}

	public void setupColorChoice(final View v, final String[] colors) {
		final LinearLayout llFirst = (LinearLayout) v.findViewById(R.id.llColorFirst);
		final LinearLayout llSecond = (LinearLayout) v
				.findViewById(R.id.llColorSecond);
		final LinearLayout llThird = (LinearLayout) v.findViewById(R.id.llColorThird);
		final LinearLayout llFourth = (LinearLayout) v
				.findViewById(R.id.llColorFourth);
		final Activity a = this.getActivity();
		// Layout
		// llFirst [MOVE] [Xs] [Transparent]
		// llSecond [Color] [Color] [Color]
		// llThird [Color] [Color] [Color]
		// llFourth [Color] [Color] [Color]
		final Bitmap[] bmColors = this.getMenuBitmaps(colors);
		final ArrayList<View> ivs = new ArrayList<View>();
		for (int i = 0; i != bmColors.length; ++i) {
			final ImageView item = new ImageView(this.getActivity());

			item.setImageBitmap(bmColors[i]);
			if (i > 2) {
				item.setBackgroundDrawable(this.getResources().getDrawable(
						R.drawable.dropshadows));
			}
			final LinearLayout ll = new LinearLayout(this.getActivity());
			ll.setGravity(Gravity.CENTER);
			ll.addView(item);
			ll.setLayoutParams(new LinearLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
			ivs.add(ll);
			final int position = i;
			item.setOnClickListener(new View.OnClickListener() {

				public void onClick(final View v) {
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
					final Bundle bundle = new Bundle();
					bundle.putBoolean("isGameplay", isGameplay);
					bundle.putChar("colorCharacter", colorCharacter);
					DialogMaker.this.listener.onDialogResult(bundle);
				}

			});
			if (i > 2) {
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
									final int color) {
								final Bundle bundle = new Bundle();
								final int[] cols = new int[colors.length];
								for (int i = 0; i != cols.length; ++i) {
									cols[i] = Integer
											.parseInt(colors[i]);
								}
								cols[ivs.indexOf(v.getParent()) - 2] = color;
								bundle.putIntArray("colors", cols);
								bundle.putInt("color",
										ivs.indexOf(v.getParent()) - 2);
								DialogMaker.this.listener.onDialogResult(bundle);
							}
						});
						dialog.show();
						return true;
					}
				});
			}
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

	public void setupCreate(final View v) {
	}

	public void setupLongClick(final View v) {
		final Button bClear = (Button) v.findViewById(R.id.bClearPuzzle);
		final Button bDelete = (Button) v.findViewById(R.id.bDeletePuzzle);
		final Button bNewRate = (Button) v.findViewById(R.id.bSubmitNewRank);
		bClear.setOnClickListener(this);
		bDelete.setOnClickListener(this);
		bNewRate.setOnClickListener(this);
	}

	public void setupRandom(final View v) {
		final Button bHeight = (Button) v.findViewById(R.id.bRandomHeight);
		final Button bWidth = (Button) v.findViewById(R.id.bRandomWidth);
		final Button bColors = (Button) v.findViewById(R.id.bRandomNumColors);

		bHeight.setOnClickListener(this);
		bWidth.setOnClickListener(this);
		bColors.setOnClickListener(this);

		this.tvHeight = (TextView) v.findViewById(R.id.tvRandomHeight);
		this.tvWidth = (TextView) v.findViewById(R.id.tvRandomWidth);
		this.tvColors = (TextView) v.findViewById(R.id.tvRandomNumColors);

		this.randomHeight = (int) Math.floor((Math.random() * 24)) + 2;
		this.randomWidth = (int) Math.floor((Math.random() * 24)) + 2;
		this.randomColors = (int) Math.floor((Math.random() * 8)) + 2;

		this.etName = (EditText) v.findViewById(R.id.etRandomName);
		this.etTag = (EditText) v.findViewById(R.id.etRandomTags);
		this.etTag.setText("Random");
		this.etName.setOnKeyListener(null);
		this.etTag.setOnKeyListener(null);

		this.updateViews();
	}

	public void setupRating(final View v) {
	}

	private void showNumberDialog(final int id) {
		final NumberPickerBuilder npb = new NumberPickerBuilder()
		.setStyleResId(R.style.MyCustomBetterPickerTheme)
		.setFragmentManager(this.getFragmentManager())
		.setPlusMinusVisibility(View.INVISIBLE)
		.setDecimalVisibility(View.INVISIBLE);
		if (id == R.id.bRandomNumColors) {
			npb.setMinNumber(2);
			npb.setMaxNumber(10);
		} else {
			if (id == R.id.bRandomWidth) {
				Crouton.makeText(this.getActivity(), "Give us a new width.",
						Style.INFO).show();
			} else {
				Crouton.makeText(this.getActivity(), "Give us a new height.",
						Style.INFO).show();
			}
			npb.setMaxNumber(100);
			npb.setMinNumber(1);
		}
		npb.addNumberPickerDialogHandler(new NumberPickerDialogHandler() {

			public void onDialogNumberSet(final int reference, final int number,
					final double decimal, final boolean isNegative, final double fullNumber) {
				DialogMaker.this.handler.post(new Runnable() {
					public void run() {
						if (id == R.id.bRandomNumColors) {
							DialogMaker.this.randomColors = number;
						} else if (id == R.id.bRandomWidth) {
							DialogMaker.this.randomWidth = number;
						} else if (id == R.id.bRandomHeight) {
							DialogMaker.this.randomHeight = number;
						}
						DialogMaker.this.updateViews();
					}
				});
			}
		});
		npb.show();
	}

	public void updateViews() {
		this.tvHeight.setText(this.randomHeight + "");
		this.tvWidth.setText(this.randomWidth + "");
		this.tvColors.setText(this.randomColors + "");
		this.generateRandom();
		this.etName.setText("Random: #" + this.randomPuzzle.hashCode());
	}
}
