
package com.picogram.awesomeness;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
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

	public Bitmap fastblur(final Bitmap sentBitmap, final int radius) {
		final Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

		if (radius < 1) {
			return (null);
		}

		final int w = bitmap.getWidth();
		final int h = bitmap.getHeight();

		final int[] pix = new int[w * h];
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		final int wm = w - 1;
		final int hm = h - 1;
		final int wh = w * h;
		final int div = radius + radius + 1;

		final int r[] = new int[wh];
		final int g[] = new int[wh];
		final int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		final int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		final int dv[] = new int[256 * divsum];
		for (i = 0; i < (256 * divsum); i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		final int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		final int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = (stackpointer - radius) + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = (stackpointer - radius) + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}

		bitmap.setPixels(pix, 0, w, 0, 0, w, h);

		return (bitmap);
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
		boolean isBlur = true;
		final int layoutId = li;
		final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this.getActivity(), R.style.DialogTheme));
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
					// Add positive button for random. Others don't need this.
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
									dialog.dismiss();
								}
							});
			this.setupRandom(v);
		} else if (layoutId == R.layout.dialog_ranking) {
			// Don't need action buttons.
			this.setupRating(v);
		} else if (layoutId == R.layout.dialog_save_picogram) {
			this.setupCreate(v);
		} else if (layoutId == R.layout.dialog_color_choice) {
			isBlur = false;
			this.setupColorChoice(v, colors);
		} else if (layoutId == R.layout.dialog_tutorial) {
			this.setupTutorial(v);
		}
		final Dialog result = builder.create();
		result.getWindow().getAttributes().windowAnimations = R.style.FadeDialogAnimation;
		if (isBlur) {

			/*
			 * Activity a = this.getActivity();
			 * while (a.getParent() != null) {
			 * a = a.getParent();
			 * }
			 * final Bitmap ss = this.takeScreenShot(a);
			 * final Activity aa = a;
			 * // Bitmap draw = Bitmap.createScaledBitmap(ss, ss.getWidth() / 3, ss.getHeight() / 3, true);
			 * // draw = Bitmap.createScaledBitmap(draw, ss.getWidth(), ss.getHeight(), true);
			 * // result.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			 * new Thread(new Runnable() {
			 * 
			 * public void run() {
			 * final Bitmap blur =
			 * DialogMaker.this.fastblur(ss, 7);
			 * aa.runOnUiThread(new Runnable() {
			 * 
			 * public void run() {
			 * // TODO Auto-generated method stub
			 * final WindowManager.LayoutParams WMLP = result.getWindow().getAttributes();
			 * 
			 * WMLP.gravity = Gravity.RIGHT;
			 * 
			 * 
			 * result.getWindow().setBackgroundDrawable(new BitmapDrawable(DialogMaker.this.getResources(), blur));
			 * result.getWindow().setAttributes(WMLP);
			 * }
			 * });
			 * 
			 * }
			 * }).start();
			 */

		}
		// result.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		return result;
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

	private void setupTutorial(final View v) {
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

	private Bitmap takeScreenShot(final Activity activity)
	{
		final View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		final Bitmap b1 = view.getDrawingCache();
		final Rect frame = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		final int statusBarHeight = frame.top;
		final int width = activity.getWindowManager().getDefaultDisplay().getWidth();
		final int height = activity.getWindowManager().getDefaultDisplay().getHeight();

		final Bitmap b = Bitmap.createBitmap(b1, 0, 0, b1.getWidth(), b1.getHeight());
		view.destroyDrawingCache();
		return b;
	}

	public void updateViews() {
		this.tvHeight.setText(this.randomHeight + "");
		this.tvWidth.setText(this.randomWidth + "");
		this.tvColors.setText(this.randomColors + "");
		this.generateRandom();
		this.etName.setText("Random: #" + this.randomPuzzle.hashCode());
	}
}
