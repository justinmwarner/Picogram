package com.picogram.awesomeness;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.cameraview.awesomeness.CameraView;
import com.cameraview.awesomeness.CameraView.OnPictureTakenListener;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment.NumberPickerDialogHandler;
import com.flurry.android.FlurryAgent;
import com.picogram.awesomeness.DialogMaker.OnDialogResultListener;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class CreatePicogramActivity extends FragmentActivity implements
		OnClickListener, OnTouchListener, OnPictureTakenListener {
	private static final int CAMERA_REQUEST_CODE = 1888,
			FILE_SELECT_CODE = 1337;
	private static final String TAG = "CreatePicogramActivity";
	static String url = "";

	private Bitmap bmOriginal, bmNew;
	Button bURL, bGallery, bCustom, bPicture, bSearch, bDone, bColors, bWidth,
			bHeight, bSwitch, bBack;
	int currentView = 0;
	CameraView cv;

	Handler handler = new Handler();
	ImageView ivOriginal, ivNew;
	int originalColors[] = { Color.TRANSPARENT, Color.BLACK, Color.RED,
			Color.YELLOW, Color.GRAY, Color.GREEN, Color.CYAN, Color.MAGENTA,
			Color.DKGRAY, Color.LTGRAY, Color.WHITE };
	int newColors[] = originalColors;
	private int numColors = 2, yNum = 20, xNum = 20;
	long oldTime = 0;

	private String solution = "";

	TouchImageView tivGame;

	private Bitmap addBorder(Bitmap bmp, int borderSize) {
		return bmp;
		/*
		 * Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() +
		 * borderSize 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
		 * Canvas canvas = new Canvas(bmpWithBorder); Paint paint = new Paint();
		 * paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(borderSize *
		 * 2); paint.setColor(Color.BLUE); canvas.drawBitmap(bmp, borderSize,
		 * borderSize, null); canvas.drawRect((borderSize / 2), (borderSize /
		 * 2), bmp.getWidth() + (borderSize), bmp.getHeight() + (borderSize),
		 * paint); return bmpWithBorder;
		 */
	}

	private void alterPhoto() {
		if (this.bmOriginal != null) {
			// Subarray with the number.
			newColors = Arrays.copyOfRange(originalColors, 0, numColors);
			// Touch this up. It's a bit messy.
			this.solution = ""; // Change back to nothing.
			Bitmap alter = this.bmOriginal.copy(Bitmap.Config.ARGB_8888, true);
			alter = Bitmap.createScaledBitmap(this.bmOriginal, this.xNum,
					this.yNum, false);
			// Set pixels = to each pixel in the scaled image (Easier to find
			// values, and smaller!)
			final int pixels[] = new int[this.xNum * this.yNum];
			alter.getPixels(pixels, 0, alter.getWidth(), 0, 0,
					alter.getWidth(), alter.getHeight());
			for (int i = 0; i < pixels.length; i++) {
				final int rgb[] = this.getRGB(pixels[i]);
				pixels[i] = (rgb[0] + rgb[1] + rgb[2]) / 3; // Greyscale
			}
			alter = alter.copy(Bitmap.Config.ARGB_8888, true);
			alter.setPixels(pixels, 0, alter.getWidth(), 0, 0,
					alter.getWidth(), alter.getHeight());

			final int pix[][] = new int[this.yNum][this.xNum];
			int run = 0;
			for (int i = 0; i < pix.length; i++) {
				for (int j = 0; j < pix[i].length; j++) {
					pix[i][j] = pixels[run++];
				}
			}

			run = 0;
			alter = alter.copy(Bitmap.Config.ARGB_8888, true);
			final char[] sol = new char[alter.getWidth() * alter.getHeight()];
			for (int i = 0; i != alter.getHeight(); ++i) {
				for (int j = 0; j != alter.getWidth(); ++j) {
					for (int k = 0; k <= this.numColors; ++k) {
						if (pix[i][j] <= ((256 * (k + 1)) / (this.numColors))) {
							// pix[i][j] = this.colors[k];
							alter.setPixel(j, i, this.newColors[k]);
							sol[(i * alter.getWidth()) + j] = (k + " ")
									.charAt(0);
							break;
						}
					}
				}
			}
			// Set up "solution" for when it's submitted, this requires us to go
			for (int i = 0; i < pix.length; i++) {
				for (int j = 0; j < pix[i].length; j++) {
					// sol[(i * j) + j] = pix[i][j];
				}
			}
			this.solution = new String(sol);
			alter = alter.copy(Bitmap.Config.ARGB_8888, true);
			alter = Bitmap.createScaledBitmap(alter, this.xNum * 10,
					this.yNum * 10, false);
			this.bmNew = alter.copy(Bitmap.Config.ARGB_8888, true);
			this.bmNew.setHasAlpha(true);
			this.bmNew = alter;
			this.bmNew.setHasAlpha(true);
			this.bmOriginal.setHasAlpha(true);
			ivNew.setImageBitmap(addBorder(bmNew, 2));
			ivOriginal
					.setImageBitmap(addBorder(Bitmap.createScaledBitmap(
							bmOriginal, xNum, yNum, false), 2));
			Bundle bundle = new Bundle();
			// current height width id solution colors(,)
			bundle.putString("current", solution);
			bundle.putString("height", yNum + "");
			bundle.putString("width", xNum + "");
			bundle.putString("id", solution.hashCode() + "");
			bundle.putString("solution", solution);
			String cols = "";
			for (int i : newColors)
				cols += i + ",";
			cols = cols.substring(0, cols.length() - 1);
			bundle.putString("colors", cols);
			tivGame.isRefreshing = true;
			tivGame.setPicogramInfo(bundle);
		} else {
			Crouton.makeText(this, "=( We need a picture first.", Style.INFO)
					.show();
		}
	}

	public void colorChanged(final String key, final int color) {
		// TODO Auto-generated method stub
	}

	private void doDone() {
		LayoutInflater inflater = getLayoutInflater();
		final View dialoglayout = inflater.inflate(
				R.layout.dialog_save_picogram, (ViewGroup) getCurrentFocus());
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(dialoglayout);
		final Activity a = this;
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// get user input and set it to result
				// edit text
				EditText name = (EditText) dialoglayout
						.findViewById(R.id.etRandomName);
				EditText tags = (EditText) dialoglayout
						.findViewById(R.id.etRandomTags);
				RadioGroup difficulty = (RadioGroup) dialoglayout
						.findViewById(R.id.radioDifficulty);

				int radioButtonID = difficulty.getCheckedRadioButtonId();
				RadioButton radioButton = (RadioButton) difficulty
						.findViewById(radioButtonID);
				String diff = radioButton.getText().toString();
				if (userValuesValid()) {
					final String puzzleId = solution.hashCode() + "";
					String cols = "";
					for (final int color : newColors) {
						cols += color + ",";
					}
					final Intent returnIntent = new Intent();
					returnIntent.putExtra("author", Util.id(a));
					returnIntent.putExtra("colors",
							cols.substring(0, cols.length() - 1));
					returnIntent.putExtra("difficulty", diff);
					returnIntent.putExtra("height", yNum + "");
					returnIntent.putExtra("name", name.getText().toString());
					returnIntent.putExtra("numberColors", numColors + "");
					returnIntent.putExtra("numRank", 1 + "");
					returnIntent.putExtra("id", puzzleId);
					returnIntent.putExtra("rank", 5 + "");
					returnIntent.putExtra("solution",
							tivGame.gCurrent.replaceAll("x", "0"));
					returnIntent.putExtra("width", xNum + "");
					returnIntent.putExtra("tags", tags.getText().toString());
					resultAndFinish(returnIntent);
					dialog.dismiss();
				}
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		}).show();

	}

	private int[] getRGB(final int i) {

		final int r = (i >> 16) & 0xff;
		final int g = (i >> 8) & 0xff;
		final int b = (i & 0xff);
		return new int[] { r, g, b };
	}

	@Override
	protected void onActivityResult(final int request, final int result,
			final Intent data) {
		if (request == FILE_SELECT_CODE) {
			FlurryAgent.logEvent("CreateFromFile");
			if (result == Activity.RESULT_OK) {
				final Uri uri = data.getData();
				final Bitmap bi = this.readBitmap(uri);
				this.bmOriginal = bi;
				this.updateBottomHolder(1);
			}
		}
		if (result != Activity.RESULT_OK) {
			Crouton.makeText(this, "Awww, we wanted your picture ='(",
					Style.INFO).show();
		}
	}

	public void onClick(final View v) {
		// We're getting a photo to edit.
		if (v.getId() == R.id.bCustom) {
			// If we're making our own, just set main
			// to the drawing screen.
			this.xNum = 25;
			this.yNum = 25;
			this.numColors = 3;
			this.solution = "";
			for (int i = 0; i != (xNum * yNum); ++i)
				solution += "0";
			Bitmap bm = Bitmap
					.createBitmap(xNum, yNum, Bitmap.Config.ARGB_8888);
			bmNew = bmOriginal = bm;
			updateBottomHolder(1);
			this.updateMainView();
		} else if (v.getId() == R.id.bGallery) {
			// File stuff.
			final Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
			fileIntent.setType("image/*");
			fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
			// Shows openable files! =)
			this.startActivityForResult(fileIntent, FILE_SELECT_CODE);
		} else if (v.getId() == R.id.bPicture) {
			// This is only here for commenting purposes.
			// The onClick is now in the library.
			// The Listener will take care of all the stuff
			// that needs to happen.
		} else if (v.getId() == R.id.bLink) {
			final EditText input = new EditText(this);
			input.setText("http://static.tumblr.com/b81c7dadf0e20919a038a85c933062a6/4w8r7zy/2ifmvnsrg/tumblr_static_tumblr_static_tumblr_inline_mkqs46qvu21qz4rgp.gif");
			new AlertDialog.Builder(CreatePicogramActivity.this)
					.setTitle("URL Sumittion")
					.setMessage("Url Link to Image File...")
					.setView(input)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									Editable value = input.getText();
									CreatePicogramActivity.url = value
											.toString();
									handler.post(new Runnable() {

										public void run() {
											processURL();
										}
									});

								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Do nothing.
								}
							}).show();
		} else if (v.getId() == R.id.bSearch) {
			Crouton.makeText(this, "This isn't supported due to limitations.",
					Style.INFO).show();
		} else if (v.getId() == R.id.ibToolsCreate) {
			Bundle bundle = new Bundle();
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			String strColors[] = new String[this.numColors];
			for (int i = 0; i != numColors; ++i)
				strColors[i] = "" + newColors[i];
			bundle.putInt("layoutId", R.layout.dialog_color_choice);
			bundle.putStringArray("colors", strColors);
			final DialogMaker newFragment = new DialogMaker();
			newFragment.setArguments(bundle);
			newFragment.setOnDialogResultListner(new OnDialogResultListener() {

				public void onDialogResult(Bundle result) {
					tivGame.isGameplay = result.getBoolean("isGameplay");
					tivGame.colorCharacter = result.getChar("colorCharacter");
					// tivGame.gridlinesColor = Color.BLACK;
					newFragment.dismiss();
				}
			});
			newFragment.show(ft, "dialog");
			return;

		}
		// We're altering the sizes and stuff.
		if (v.getId() == R.id.bWidth) {
			showNumberDialog(v.getId());
		} else if (v.getId() == R.id.bHeight) {
			showNumberDialog(v.getId());
		} else if (v.getId() == R.id.bColors) {
			showNumberDialog(v.getId());
		} else if (v.getId() == R.id.bSwitch) {
			// Change
			updateMainView();
		} else if (v.getId() == R.id.bDone) {
			// Done.
			doDone();
		} else if (v.getId() == R.id.bBack) {
			currentView = -1;// Used for main view.
			this.updateMainView();
			this.updateBottomHolder(0);
		}

		// TODO Tell user they can change colors by clicking on it.

	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setContentView(R.layout.activity_create_advanced);
		Util.setTheme(this);
		this.updateBottomHolder(0);
		// Check if we're getting data from a share.
		final Intent intent = this.getIntent();
		final String action = intent.getAction();
		final String type = intent.getType();
		if (Intent.ACTION_SEND.equals(action) && (type != null)) {
			if (type.startsWith("image/")) {
				final Uri uri = (Uri) intent
						.getParcelableExtra(Intent.EXTRA_STREAM);
				final Bitmap bi = this.readBitmap(uri);
				this.bmOriginal = bi;
				this.ivOriginal.setImageBitmap(this.bmOriginal);
			} else if (type.startsWith("text/")) {
				String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
				if (sharedText != null) {
					url = sharedText;
					processURL();
				}
			}
		}
		FlurryAgent.logEvent("CreatingPuzzle");
		ib = (ImageButton) findViewById(R.id.ibToolsCreate);
		ib.setOnClickListener(this);
	}

	private Bitmap[] getMenuBitmaps() {
		Bitmap[] result = new Bitmap[this.numColors];
		for (int i = 0; i != numColors; ++i) {
			Bitmap fullColor = Bitmap.createBitmap(1, 1,
					Bitmap.Config.ARGB_8888);
			final int[] rgb = this.getRGB(this.newColors[i]);
			if (rgb[0] == 0) {// This is alpha.
				// For transparency.
				fullColor = BitmapFactory.decodeResource(this.getResources(),
						R.drawable.transparent);
			} else {
				fullColor.setPixel(0, 0, Color.rgb(rgb[0], rgb[1], rgb[2]));
			}
			// +2 for the X's and movement.
			result[i] = fullColor;
		}
		return result;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		// Workaround until there's a way to detach the Activity from Crouton
		// while there are still some in the Queue.
		Crouton.clearCroutonsForActivity(this);
		super.onDestroy();
	}

	public void onNothingSelected(final AdapterView<?> arg0) {
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public void onPictureTaken(Bitmap bm) {
		this.bmOriginal = bm;
		this.updateBottomHolder(1);

	}

	public void onProgressChanged(final SeekBar seekBar, final int progress,
			final boolean fromUser) {
		// TODO Transparency
		// TODO Height
		// TODO Width
		// TODO Number of colors.
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	public boolean onTouch(final View v, final MotionEvent event) {
		if (v.getId() == this.ivNew.getId()) {
			// We're changing a color.
			if ((this.bmNew != null)
					&& (event.getAction() == MotionEvent.ACTION_DOWN)) {
				final float eventX = event.getX();
				final float eventY = event.getY();
				final float[] eventXY = new float[] { eventX, eventY };

				ImageView ivAlter = ((ivNew.getVisibility() == View.VISIBLE) ? ivNew
						: tivGame);
				final Matrix invertMatrix = new Matrix();
				ivAlter.getImageMatrix().invert(invertMatrix);

				invertMatrix.mapPoints(eventXY);
				int x = Integer.valueOf((int) eventXY[0]);
				int y = Integer.valueOf((int) eventXY[1]);

				final Drawable imgDrawable = ivAlter.getDrawable();
				final Bitmap bitmap = ((BitmapDrawable) imgDrawable)
						.getBitmap();

				// Limit x, y range within bitmap
				if (x < 0) {
					x = 0;
				} else if (x > (bitmap.getWidth() - 1)) {
					x = bitmap.getWidth() - 1;
				}

				if (y < 0) {
					y = 0;
				} else if (y > (bitmap.getHeight() - 1)) {
					y = bitmap.getHeight() - 1;
				}

				final int touchedRGB = bitmap.getPixel(x, y);
				if (touchedRGB == Color.TRANSPARENT)
					return true; // Ignore if we're touching a transparent
									// color.
				// initialColor is the initially-selected color to be shown in
				// the
				// rectangle on the left of the arrow.
				// for example, 0xff000000 is black, 0xff0000ff is blue. Please
				// be
				// aware
				// of the initial 0xff which is the alpha.
				final AmbilWarnaDialog dialog = new AmbilWarnaDialog(this,
						touchedRGB, new OnAmbilWarnaListener() {

							public void onCancel(final AmbilWarnaDialog dialog) {
								// TODO Auto-generated method stub

							}

							public void onOk(final AmbilWarnaDialog dialog,
									int color) {
								String cols = "";
								for (int i : newColors)
									cols += i + " ";
								// Change the value in the colors array.
								for (int i = 0; i != CreatePicogramActivity.this.newColors.length; ++i) {
									if (CreatePicogramActivity.this.newColors[i] == touchedRGB) {
										// Make sure this color doesn't already
										// exist, if it does, tweak the new
										// color
										// just a little bit.
										/*
										 * //TODO If color already exists, don't
										 * let it happen. for (int j = 0; j !=
										 * CreatePicogramActivity
										 * .this.newColors.length; ++j) { if
										 * (CreatePicogramActivity
										 * .this.newColors[j] == touchedRGB) {
										 * final int[] rgb =
										 * CreatePicogramActivity.this
										 * .getRGB(color); if (rgb[0] == 255) {
										 * rgb[0] = rgb[0] - 1; } else { rgb[0]
										 * = rgb[0] + 1; } color =
										 * Color.rgb(rgb[0], rgb[1], rgb[2]);
										 * break; } Log.d(TAG,
										 * "Didn't find it =/ " + color + " " +
										 * newColors[j]); }
										 */
										CreatePicogramActivity.this.newColors[i] = color;
										break;
									}
								}
								// Change values in the original colors too.
								for (int i = 0; i != originalColors.length; ++i) {
									if (originalColors[i] == touchedRGB)
										originalColors[i] = color;
								}
								// Update photo
								cols = "";
								for (int i : newColors)
									cols += i + " ";
								CreatePicogramActivity.this.alterPhoto();
							}
						});

				dialog.show();
				return true;
			} else {
				return false;
			}
		} else if (v.getId() == tivGame.getId()) {

		}
		return true;
	}

	// Read bitmap - From
	// http://tutorials-android.blogspot.co.il/2011/11/outofmemory-exception-when-decoding.html
	private void processURL() {
		Crouton.makeText(this, "Valid, we're working.", Style.INFO).show();
		FlurryAgent.logEvent("CreateURLProcess");
		final Activity a = this;
		new Thread(new Runnable() {
			public void run() {
				try {
					final URL url = new URL(CreatePicogramActivity.url);
					final HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setDoInput(true);
					conn.connect();
					InputStream in;
					in = conn.getInputStream();
					final Bitmap bm = BitmapFactory.decodeStream(in);
					CreatePicogramActivity.this.bmOriginal = bm;
					handler.post(new Runnable() {
						public void run() {
							bmOriginal = bm;
							updateBottomHolder(1);
						}

					});
				} catch (final IOException e) {
					Crouton.makeText(a, "Failed to get image.", Style.ALERT)
							.show();
					e.printStackTrace();
				}
			}

		}).start();
	}

	public Bitmap readBitmap(final Uri selectedImage) {
		Bitmap bm = null;
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 5;
		AssetFileDescriptor fileDescriptor = null;
		try {
			fileDescriptor = this.getContentResolver().openAssetFileDescriptor(
					selectedImage, "r");
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				bm = BitmapFactory.decodeFileDescriptor(
						fileDescriptor.getFileDescriptor(), null, options);
				fileDescriptor.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return bm;
	}

	public void resultAndFinish(Intent i) {
		setResult(RESULT_OK, i);
		finish();
	}

	private void showNumberDialog(final int id) {
		NumberPickerBuilder npb = new NumberPickerBuilder()
				.setStyleResId(R.style.MyCustomBetterPickerTheme)
				.setFragmentManager(this.getSupportFragmentManager())
				.setPlusMinusVisibility(View.INVISIBLE)
				.setDecimalVisibility(View.INVISIBLE);
		if (id == R.id.bColors) {
			npb.setMinNumber(2);
			npb.setMaxNumber(10);
		} else {
			if (id == R.id.bWidth)
				Crouton.makeText(this, "Give us a new width.", Style.INFO)
						.show();
			else
				Crouton.makeText(this, "Give us a new height.", Style.INFO)
						.show();
			npb.setMaxNumber(25);
			npb.setMinNumber(1);
		}
		npb.addNumberPickerDialogHandler(new NumberPickerDialogHandler() {

			public void onDialogNumberSet(int reference, final int number,
					double decimal, boolean isNegative, double fullNumber) {
				Util.log("Reference:  " + reference + " Number: " + number
						+ " Decimal: " + decimal + " Neg: " + isNegative
						+ " FullNum: " + fullNumber);
				handler.post(new Runnable() {
					public void run() {
						if (id == R.id.bColors) {
							numColors = number;
						} else if (id == R.id.bWidth) {
							xNum = number;
						} else if (id == R.id.bHeight) {
							yNum = number;
						}
						alterPhoto();
						setGameViewInfo();
					}
				});
			}
		});
		npb.show();
	}

	private void updateBottomHolder(int step) {
		if (step == 0) {
			// We're getting a picture.
			// Load the picture include.
			LayoutInflater inflater = (LayoutInflater) this
					.getSystemService(LAYOUT_INFLATER_SERVICE);
			View childLayout = inflater.inflate(R.layout.include_picture_input,
					(ViewGroup) findViewById(R.layout.include_picture_input));

			RelativeLayout ll = (RelativeLayout) findViewById(R.id.buttonBottomHolder);
			ll.removeAllViews();
			ll.addView(childLayout);
			// Set up listeners and everything.

			cv = (CameraView) findViewById(R.id.cvPreview);
			bURL = (Button) findViewById(R.id.bLink);
			bGallery = (Button) findViewById(R.id.bGallery);
			bPicture = (Button) findViewById(R.id.bPicture);
			bCustom = (Button) findViewById(R.id.bCustom);
			bSearch = (Button) findViewById(R.id.bSearch);

			cv.setButton(bPicture);
			cv.setOnPictureTakenListner(this);
			bURL.setOnClickListener(this);
			bGallery.setOnClickListener(this);
			bCustom.setOnClickListener(this);
			bSearch.setOnClickListener(this);

			cv.setVisibility(View.VISIBLE);
			// this.setContentView(R.layout.activity_create_advanced);
		} else if (step == 1) {
			// We're altering the photo.
			LayoutInflater inflater = (LayoutInflater) this
					.getSystemService(LAYOUT_INFLATER_SERVICE);
			View childLayout = inflater
					.inflate(
							R.layout.include_width_height_num,
							(ViewGroup) findViewById(R.layout.include_width_height_num));

			RelativeLayout ll = (RelativeLayout) findViewById(R.id.buttonBottomHolder);
			ll.removeAllViews();
			ll.addView(childLayout);
			// Hide CameraView, change the bottom bar, and update the new image.
			cv.setVisibility(View.INVISIBLE);

			bDone = (Button) findViewById(R.id.bDone);
			bSwitch = (Button) findViewById(R.id.bSwitch);
			bWidth = (Button) findViewById(R.id.bWidth);
			bHeight = (Button) findViewById(R.id.bHeight);
			bColors = (Button) findViewById(R.id.bColors);
			bBack = (Button) findViewById(R.id.bBack);

			bDone.setOnClickListener(this);
			bSwitch.setOnClickListener(this);
			bWidth.setOnClickListener(this);
			bHeight.setOnClickListener(this);
			bColors.setOnClickListener(this);
			bBack.setOnClickListener(this);

			// Add in ImageViews and GameView
			ivNew = (ImageView) findViewById(R.id.ivNew);
			ivOriginal = (ImageView) findViewById(R.id.ivOriginal);
			tivGame = (TouchImageView) findViewById(R.id.tivGame);

			ivNew.setOnTouchListener(this);
			// tivGame.setOnTouchListener(this);

			ivNew.setVisibility(View.VISIBLE);
			ivOriginal.setVisibility(View.INVISIBLE);
			tivGame.setVisibility(View.INVISIBLE);
			// Update via AlterPhoto
			this.alterPhoto();
			ivOriginal.setImageBitmap(this.bmOriginal);
			ivNew.setImageBitmap(this.bmNew);
			this.bmNew.setHasAlpha(true);
			this.bmOriginal.setHasAlpha(true);

			this.alterPhoto();
		}
	}

	ImageButton ib;

	private void updateMainView() {
		if (tivGame != null)
			if (tivGame.gCurrent != null)
				this.solution = tivGame.gCurrent;
		ib.setVisibility(View.INVISIBLE);
		if (currentView == -1) {
			// We're changing back to the get picture screen.
			ivOriginal.setVisibility(View.INVISIBLE);
			ivNew.setVisibility(View.INVISIBLE);
			tivGame.setVisibility(View.INVISIBLE);
			this.setContentView(R.layout.activity_create_advanced);
			cv = (CameraView) findViewById(R.id.cvPreview);
			currentView = 0; // Go to the normal view after.
		} else if (currentView == 2) {
			// Show Original
			ivOriginal.setVisibility(View.VISIBLE);
			ivNew.setVisibility(View.INVISIBLE);
			tivGame.setVisibility(View.INVISIBLE);
			currentView = 1;
		} else if (currentView == 1) {
			// Show New without grid.
			// Update from solution.
			ivOriginal.setVisibility(View.INVISIBLE);
			ivNew.setVisibility(View.VISIBLE);
			tivGame.setVisibility(View.INVISIBLE);
			currentView = 0;
			Log.d(TAG, "Solution: " + tivGame.gCurrent);
			updatePictureFromTIV(tivGame.gCurrent);
		} else if (currentView == 0) {
			// Show Gameboard
			ivOriginal.setVisibility(View.INVISIBLE);
			ivNew.setVisibility(View.INVISIBLE);
			tivGame.setVisibility(View.VISIBLE);
			tivGame.gridlinesColor = Color.BLACK;
			tivGame.gHeight = this.yNum;
			tivGame.gWidth = this.xNum;
			tivGame.gCurrent = this.solution;
			tivGame.bitmapFromCurrent();
			currentView = 2;
			Log.d(TAG, ib.getVisibility() + " VISIBLE");
			ib.setVisibility(View.VISIBLE);
			ib.getParent().bringChildToFront(ib);
			Log.d(TAG, ib.getVisibility() + " VISIBLE");
			Crouton.makeText(this, "Draw on screen to edit", Style.INFO).show();
			// current width height id solution colors(string,)
		} else {
			currentView = 0; // Reset if problems.
		}
	}

	private void updatePictureFromTIV(String gSolution) {
		bmNew = Bitmap.createBitmap(xNum, yNum, Bitmap.Config.ARGB_4444);
		int[] pixels = new int[xNum * yNum];
		for (int i = 0; i != gSolution.length(); ++i) {
			if (gSolution.charAt(i) == 'x')
				pixels[i] = 0;
			else {
				int color = Integer.parseInt("" + gSolution.charAt(i));
				pixels[i] = newColors[color];
			}
		}
		bmNew.setPixels(pixels, 0, xNum, 0, 0, xNum, yNum);
		bmNew = Bitmap.createScaledBitmap(bmNew, xNum * 10, yNum * 10, false);
		ivNew.setImageBitmap(bmNew);
	}

	private void setGameViewInfo() {
		Bundle bundle = new Bundle();
		bundle.putString("current", this.solution);
		bundle.putString("width", "" + xNum);
		bundle.putString("height", "" + yNum);
		bundle.putString("id", "" + this.solution.hashCode());
		bundle.putString("solution", this.solution);
		String cols = "";
		for (Integer color : newColors)
			cols += color + ",";
		bundle.putString("colors", cols.substring(0, cols.length() - 1));
		tivGame.setPicogramInfo(bundle);
	}

	private boolean userValuesValid() {
		// TODO redo this method.
		return true;
	}

}