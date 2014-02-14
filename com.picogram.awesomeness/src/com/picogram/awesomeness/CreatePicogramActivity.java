
package com.picogram.awesomeness;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.cameraview.awesomeness.CameraView;
import com.cameraview.awesomeness.CameraView.OnPictureTakenListener;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment.NumberPickerDialogHandler;
import com.flurry.android.FlurryAgent;
import com.picogram.awesomeness.DialogMaker.OnDialogResultListener;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class CreatePicogramActivity extends FragmentActivity implements
OnClickListener, OnPictureTakenListener {
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
	int originalColors[] = {
			Color.TRANSPARENT, Color.BLACK, Color.RED,
			Color.YELLOW, Color.GRAY, Color.GREEN, Color.CYAN, Color.MAGENTA,
			Color.DKGRAY, Color.LTGRAY, Color.WHITE
	};
	int newColors[] = this.originalColors;
	private int numColors = 2, yNum = 20, xNum = 20;
	long oldTime = 0;

	private String solution = "";

	TouchImageView tivGame;

	boolean isOriginalShowing = false;

	ImageButton ib;

	boolean continueMusic = true;

	private void alterPhoto() {
		if (this.bmOriginal != null) {
			// Subarray with the number.
			this.newColors = Arrays.copyOfRange(this.originalColors, 0, this.numColors);
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
				// Greyscale and inverse.
				pixels[i] = 255 - ((rgb[0] + rgb[1] + rgb[2]) / 3);
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
							sol[(i * alter.getWidth()) + j] = (k + " ")
									.charAt(0);
							break;
						}
					}
				}
			}

			this.solution = new String(sol);
			this.bmOriginal.setHasAlpha(true);

			final Bundle bundle = new Bundle();
			// current height width id solution colors(,)
			bundle.putString("current", this.solution);
			bundle.putString("height", this.yNum + "");
			bundle.putString("width", this.xNum + "");
			bundle.putString("id", this.solution.hashCode() + "");
			bundle.putString("solution", this.solution);
			String cols = "";

			for (final int i : this.newColors) {
				cols += i + ",";
			}

			cols = cols.substring(0, cols.length() - 1);
			bundle.putString("colors", cols);
			this.tivGame.isRefreshing = true;

			this.tivGame.gridlinesColor = Color.rgb(191, 191, 191);
			this.tivGame.setPicogramInfo(bundle);

		} else {
			Crouton.makeText(this, "=( We need a picture first.", Style.INFO)
			.show();
		}

	}

	public void colorChanged(final String key, final int color) {
		// TODO Auto-generated method stub
	}

	private void doDone() {
		final LayoutInflater inflater = this.getLayoutInflater();
		final View dialoglayout = inflater.inflate(
				R.layout.dialog_save_picogram, (ViewGroup) this.getCurrentFocus());
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(dialoglayout);
		final Activity a = this;
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				// get user input and set it to result
				// edit text
				final EditText name = (EditText) dialoglayout
						.findViewById(R.id.etRandomName);
				final EditText tags = (EditText) dialoglayout
						.findViewById(R.id.etRandomTags);
				final RadioGroup difficulty = (RadioGroup) dialoglayout
						.findViewById(R.id.radioDifficulty);

				final int radioButtonID = difficulty.getCheckedRadioButtonId();
				final RadioButton radioButton = (RadioButton) difficulty
						.findViewById(radioButtonID);
				final String diff = radioButton.getText().toString();
				if (CreatePicogramActivity.this.userValuesValid()) {
					final String puzzleId = CreatePicogramActivity.this.solution.hashCode() + "";
					String cols = "";
					for (final int color : CreatePicogramActivity.this.newColors) {
						cols += color + ",";
					}
					final Intent returnIntent = new Intent();
					returnIntent.putExtra("author", Util.id(a));
					returnIntent.putExtra("colors",
							cols.substring(0, cols.length() - 1));
					returnIntent.putExtra("difficulty", diff);
					returnIntent.putExtra("height", CreatePicogramActivity.this.yNum + "");
					returnIntent.putExtra("name", name.getText().toString());
					returnIntent.putExtra("numberColors", CreatePicogramActivity.this.numColors
							+ "");
					returnIntent.putExtra("numRank", 1 + "");
					returnIntent.putExtra("id", puzzleId);
					returnIntent.putExtra("rank", 5 + "");
					returnIntent.putExtra("solution",
							CreatePicogramActivity.this.tivGame.gCurrent.replaceAll("x", "0"));
					returnIntent.putExtra("width", CreatePicogramActivity.this.xNum + "");
					returnIntent.putExtra("tags", tags.getText().toString());
					CreatePicogramActivity.this.resultAndFinish(returnIntent);
					dialog.dismiss();
				}
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.cancel();
			}
		}).show();

	}

	private Bitmap[] getMenuBitmaps() {
		final Bitmap[] result = new Bitmap[this.numColors];
		for (int i = 0; i != this.numColors; ++i) {
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

	private int[] getRGB(final int i) {

		final int r = (i >> 16) & 0xff;
		final int g = (i >> 8) & 0xff;
		final int b = (i & 0xff);
		return new int[] {
				r, g, b
		};
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
			for (int i = 0; i != (this.xNum * this.yNum); ++i) {
				this.solution += "0";
			}
			final Bitmap bm = Bitmap
					.createBitmap(this.xNum, this.yNum, Bitmap.Config.ARGB_8888);
			this.bmNew = this.bmOriginal = bm;
			this.updateBottomHolder(1);
			this.updateMainView();
			final Bundle bundle = new Bundle();
			bundle.putString("current", null);
			bundle.putString("width", "" + this.xNum);
			bundle.putString("height", "" + this.yNum);
			String cols = "";
			for (int i = 0; i != this.newColors.length; ++i) {
				cols += this.newColors[i] + ",";
			}
			bundle.putString("colors", cols);

			this.tivGame.setPicogramInfo(bundle);

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
				public void onClick(final DialogInterface dialog,
						final int whichButton) {
					final Editable value = input.getText();
					CreatePicogramActivity.url = value
							.toString();
					CreatePicogramActivity.this.handler.post(new Runnable() {

						public void run() {
							CreatePicogramActivity.this.processURL();
						}
					});

				}
			})
			.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog,
						final int whichButton) {
					// Do nothing.
				}
			}).show();
		} else if (v.getId() == R.id.bSearch) {
			Crouton.makeText(this, "This isn't supported due to limitations.",
					Style.INFO).show();
		} else if (v.getId() == R.id.ibTools) {
			final Bundle bundle = new Bundle();
			final FragmentTransaction ft = this.getSupportFragmentManager()
					.beginTransaction();
			final String strColors[] = new String[this.numColors];
			for (int i = 0; i != this.numColors; ++i) {
				strColors[i] = "" + this.newColors[i];
			}
			bundle.putInt("layoutId", R.layout.dialog_color_choice);
			bundle.putStringArray("colors", strColors);
			final DialogMaker newFragment = new DialogMaker();
			newFragment.setArguments(bundle);
			newFragment.setOnDialogResultListner(new OnDialogResultListener() {

				public void onDialogResult(final Bundle result) {
					CreatePicogramActivity.this.tivGame.isGameplay = result
							.getBoolean("isGameplay");
					CreatePicogramActivity.this.tivGame.colorCharacter = result
							.getChar("colorCharacter");
					// tivGame.gridlinesColor = Color.BLACK;
					newFragment.dismiss();
				}
			});
			newFragment.show(ft, "dialog");
			return;

		}
		// We're altering the sizes and stuff.
		if (v.getId() == R.id.bWidth) {
			this.showNumberDialog(v.getId());
		} else if (v.getId() == R.id.bHeight) {
			this.showNumberDialog(v.getId());
		} else if (v.getId() == R.id.bColors) {
			this.showNumberDialog(v.getId());
		} else if (v.getId() == R.id.bSwitch) {
			// Show the original image on top of tivGame.
			if (this.isOriginalShowing) {
				this.tivGame.isRefreshing = true;
				this.tivGame.bitmapFromCurrent();
				this.isOriginalShowing = false;
			} else {
				final Bitmap bm = Bitmap.createScaledBitmap(this.bmOriginal,
						this.tivGame.gWidth * this.tivGame.cellWidth, this.tivGame.gHeight
						* this.tivGame.cellHeight, false);
				this.tivGame.canvasBitmap.drawBitmap(bm, this.tivGame.longestSide
						* this.tivGame.cellWidth, this.tivGame.longestTop
						* this.tivGame.cellHeight, this.tivGame.paintBitmap);
				this.tivGame.invalidate();
				this.isOriginalShowing = true;
			}
		} else if (v.getId() == R.id.bDone) {
			// Done.
			this.doDone();
		} else if (v.getId() == R.id.bBack) {
			this.currentView = -1;// Used for main view.
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
			} else if (type.startsWith("text/")) {
				final String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
				if (sharedText != null) {
					url = sharedText;
					this.processURL();
				}
			}
		}
		FlurryAgent.logEvent("CreatingPuzzle");
		this.ib = (ImageButton) this.findViewById(R.id.ibTools);
		this.ib.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// this.getMenuInflater().inflate(R.menu.activity_main, menu);
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
		if (!this.continueMusic) {
			MusicManager.pause();
		}
	}

	public void onPictureTaken(final Bitmap bm) {
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
		this.continueMusic = false;
		MusicManager.start(this);

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
					CreatePicogramActivity.this.handler.post(new Runnable() {
						public void run() {
							CreatePicogramActivity.this.bmOriginal = bm;
							CreatePicogramActivity.this.updateBottomHolder(1);
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

	public void resultAndFinish(final Intent i) {
		this.setResult(RESULT_OK, i);
		this.finish();
	}

	private void setGameViewInfo() {
		final Bundle bundle = new Bundle();
		bundle.putString("current", this.solution);
		bundle.putString("width", "" + this.xNum);
		bundle.putString("height", "" + this.yNum);
		bundle.putString("id", "" + this.solution.hashCode());
		bundle.putString("solution", this.solution);
		String cols = "";
		for (final Integer color : this.newColors) {
			cols += color + ",";
		}
		bundle.putString("colors", cols.substring(0, cols.length() - 1));
		this.tivGame.setPicogramInfo(bundle);
	}

	private void showNumberDialog(final int id) {
		final NumberPickerBuilder npb = new NumberPickerBuilder()
		.setStyleResId(R.style.MyCustomBetterPickerTheme)
		.setFragmentManager(this.getSupportFragmentManager())
		.setPlusMinusVisibility(View.INVISIBLE)
		.setDecimalVisibility(View.INVISIBLE);
		if (id == R.id.bColors) {
			npb.setMinNumber(2);
			npb.setMaxNumber(10);
		} else {
			if (id == R.id.bWidth) {
				Crouton.makeText(this, "Give us a new width.", Style.INFO)
				.show();
			} else {
				Crouton.makeText(this, "Give us a new height.", Style.INFO)
				.show();
			}
			npb.setMaxNumber(100);
			npb.setMinNumber(1);
		}
		npb.addNumberPickerDialogHandler(new NumberPickerDialogHandler() {

			public void onDialogNumberSet(final int reference, final int number,
					final double decimal, final boolean isNegative, final double fullNumber) {
				Util.log("Reference:  " + reference + " Number: " + number
						+ " Decimal: " + decimal + " Neg: " + isNegative
						+ " FullNum: " + fullNumber);
				CreatePicogramActivity.this.handler.post(new Runnable() {
					public void run() {
						if (id == R.id.bColors) {
							CreatePicogramActivity.this.numColors = number;
						} else if (id == R.id.bWidth) {
							CreatePicogramActivity.this.xNum = number;
						} else if (id == R.id.bHeight) {
							CreatePicogramActivity.this.yNum = number;
						}
						CreatePicogramActivity.this.alterPhoto();
						CreatePicogramActivity.this.setGameViewInfo();
					}
				});
			}
		});
		npb.show();
	}

	private void updateBottomHolder(final int step) {
		if (step == 0) {
			// We're getting a picture.
			// Load the picture include.
			final LayoutInflater inflater = (LayoutInflater) this
					.getSystemService(LAYOUT_INFLATER_SERVICE);
			final View childLayout = inflater.inflate(R.layout.include_picture_input,
					(ViewGroup) this.findViewById(R.layout.include_picture_input));

			final RelativeLayout ll = (RelativeLayout) this.findViewById(R.id.buttonBottomHolder);
			ll.removeAllViews();
			ll.addView(childLayout);
			// Set up listeners and everything.

			this.cv = (CameraView) this.findViewById(R.id.cvPreview);
			this.bURL = (Button) this.findViewById(R.id.bLink);
			this.bGallery = (Button) this.findViewById(R.id.bGallery);
			this.bPicture = (Button) this.findViewById(R.id.bPicture);
			this.bCustom = (Button) this.findViewById(R.id.bCustom);
			this.bSearch = (Button) this.findViewById(R.id.bSearch);

			this.cv.setButton(this.bPicture);
			this.cv.setOnPictureTakenListner(this);
			this.bURL.setOnClickListener(this);
			this.bGallery.setOnClickListener(this);
			this.bCustom.setOnClickListener(this);
			this.bSearch.setOnClickListener(this);

			this.cv.setVisibility(View.VISIBLE);
			// this.setContentView(R.layout.activity_create_advanced);
		} else if (step == 1) {
			// We're altering the photo.
			final LayoutInflater inflater = (LayoutInflater) this
					.getSystemService(LAYOUT_INFLATER_SERVICE);
			final View childLayout = inflater
					.inflate(
							R.layout.include_width_height_num,
							(ViewGroup) this.findViewById(R.layout.include_width_height_num));

			final RelativeLayout ll = (RelativeLayout) this.findViewById(R.id.buttonBottomHolder);
			ll.removeAllViews();
			ll.addView(childLayout);
			// Hide CameraView, change the bottom bar, and update the new image.
			this.cv.setVisibility(View.INVISIBLE);

			this.bDone = (Button) this.findViewById(R.id.bDone);
			this.bSwitch = (Button) this.findViewById(R.id.bSwitch);
			this.bWidth = (Button) this.findViewById(R.id.bWidth);
			this.bHeight = (Button) this.findViewById(R.id.bHeight);
			this.bColors = (Button) this.findViewById(R.id.bColors);
			this.bBack = (Button) this.findViewById(R.id.bBack);

			this.bDone.setOnClickListener(this);
			this.bSwitch.setOnClickListener(this);
			this.bWidth.setOnClickListener(this);
			this.bHeight.setOnClickListener(this);
			this.bColors.setOnClickListener(this);
			this.bBack.setOnClickListener(this);

			// Add in ImageViews and GameView
			this.tivGame = (TouchImageView) this.findViewById(R.id.tivGame);
			this.tivGame.setVisibility(View.VISIBLE);
			this.ib.setVisibility(View.VISIBLE);
			// Update via AlterPhoto

			this.alterPhoto();
		}
	}

	private void updateMainView() {
		if (this.tivGame != null) {
			if (this.tivGame.gCurrent != null) {
				this.solution = this.tivGame.gCurrent;
			}
		}
		this.ib.setVisibility(View.INVISIBLE);
		this.ib.setVisibility(View.INVISIBLE);
		if (this.currentView == -1) {
			// We're changing back to the get picture screen.
			this.tivGame.setVisibility(View.INVISIBLE);
			this.setContentView(R.layout.activity_create_advanced);
			this.cv = (CameraView) this.findViewById(R.id.cvPreview);
			this.currentView = 0; // Go to the normal view after.
		} else if (this.currentView == 2) {
			// Show Original
			this.tivGame.setVisibility(View.INVISIBLE);
			this.currentView = 1;
		} else if (this.currentView == 1) {
			// Show New without grid.
			// Update from solution.
			this.tivGame.setVisibility(View.INVISIBLE);
			this.currentView = 0;
		} else if (this.currentView == 0) {
			// Show Gameboard
			this.tivGame.setVisibility(View.VISIBLE);
			this.tivGame.gridlinesColor = Color.BLACK;
			this.tivGame.gHeight = this.yNum;
			this.tivGame.gWidth = this.xNum;
			this.tivGame.gCurrent = this.solution;
			this.tivGame.bitmapFromCurrent();
			this.currentView = 2;
			this.ib.setVisibility(View.VISIBLE);
			Crouton.makeText(this, "Draw on screen to edit", Style.INFO).show();
			// current width height id solution colors(string,)
		} else {
			this.currentView = 0; // Reset if problems.
		}
	}

	private boolean userValuesValid() {
		// TODO redo this method.
		return true;
	}

}
