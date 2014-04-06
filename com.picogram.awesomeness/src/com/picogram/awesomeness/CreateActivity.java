
package com.picogram.awesomeness;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.edmodo.cropper.CropImageView;
import com.flurry.android.FlurryAgent;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class CreateActivity extends SherlockFragmentActivity implements ActionBar.OnNavigationListener, OnClickListener, OnPageChangeListener {

	public class MyPagerAdapter extends FragmentPagerAdapter {

		public MyPagerAdapter(final FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return CreateActivity.this.TITLES.size();
		}

		@Override
		public Fragment getItem(final int position) {
			return CreateFragment.newInstance(position);
		}

		@Override
		public CharSequence getPageTitle(final int position) {
			return CreateActivity.this.TITLES.get(position);
		}

	}

	private static final String TAG = "MultiStepCreateActivity";
	private static final int CAMERA_REQUEST = 1337, FILE_SELECT_CODE = 8008;

	final ArrayList<String> TITLES = new ArrayList<String>(Arrays.asList(new String[] {
			"Picture", "Crop", "Game", "Colors", "Fine Tune", "Name"
	}));

	PagerSlidingTabStrip tabs;

	ViewPager pager;

	MyPagerAdapter adapter;
	Handler handler;
	Bitmap bmCropped, bmInitial;
	int currentTab = 0;
	int width, height, numColors;
	String solution, name, tags, difficulty;
	int originalColors[] = {
			Color.TRANSPARENT, Color.BLACK, Color.RED,
			Color.YELLOW, Color.GRAY, Color.GREEN, Color.CYAN, Color.MAGENTA,
			Color.DKGRAY, Color.LTGRAY, Color.WHITE
	};
	int newColors[] = this.originalColors;

	SmoothProgressBar spb;

	boolean hasFineTuned = false;

	String fineTunedSolution = "";

	protected Bundle alterPhoto() {
		if (this.width == 0) {
			this.width = 3;
		}
		if (this.height == 0) {
			this.height = 2;
		}
		if (this.numColors == 0) {
			this.numColors = 2;
		}
		Log.d(TAG, "W: " + this.width + " H: " + this.height);
		if (!((this.bmCropped == null) || (this.width == 0) || (this.height == 0) || (this.numColors == 0))) {
			// Subarray with the number.
			this.newColors = Arrays.copyOfRange(this.originalColors, 0, this.numColors);
			// Touch this up. It's a bit messy.
			this.solution = ""; // Change back to nothing.
			Bitmap alter = this.bmCropped.copy(Bitmap.Config.ARGB_8888, true);
			alter = Bitmap.createScaledBitmap(this.bmCropped, this.width, this.height, false);
			// Set pixels = to each pixel in the scaled image (Easier to find
			// values, and smaller!)
			final int pixels[] = new int[this.width * this.height];
			alter.getPixels(pixels, 0, alter.getWidth(), 0, 0,
					alter.getWidth(), alter.getHeight());
			for (int i = 0; i < pixels.length; i++) {
				final int rgb[] = this.getRGB(pixels[i]);
				// Greyscale and inverse.
				pixels[i] = 255 - ((rgb[0] + rgb[1] + rgb[2]) / 3);
			}

			double lowerBounds = pixels[0];
			double upperBounds = pixels[0];
			String out = "";
			for (int i = 1; i < pixels.length; i++)
			{
				out += pixels[i] + ",";
				if (pixels[i] > upperBounds) {
					upperBounds = pixels[i];
				} else if (pixels[i] < lowerBounds) {
					lowerBounds = pixels[i];
				}
			}
			Log.d(TAG, out);
			final double diff = upperBounds - lowerBounds;
			alter = alter.copy(Bitmap.Config.ARGB_8888, true);
			alter.setPixels(pixels, 0, alter.getWidth(), 0, 0,
					alter.getWidth(), alter.getHeight());

			final int pix[][] = new int[this.height][this.width];
			int run = 0;
			for (int i = 0; i < pix.length; i++) {
				for (int j = 0; j < pix[i].length; j++) {
					pix[i][j] = pixels[run++];
				}
			}

			run = 0;
			alter = alter.copy(Bitmap.Config.ARGB_8888, true);
			final char[] sol = new char[this.width * this.height];
			for (int i = 0; i != this.height; ++i) {
				for (int j = 0; j != this.width; ++j) {
					for (int k = 0; k != this.numColors; ++k)
					{
						Log.d(TAG, pix[i][j] + " " + lowerBounds + " " + Math.ceil(diff / this.numColors) + " " + (Math.ceil(diff / this.numColors) * (k + 1)));
						Log.d(TAG, pix[i][j] + " <= " + (lowerBounds + (Math.ceil((diff / this.numColors)) * (k + 1))) + " = " + (pix[i][j] <= (lowerBounds + (Math.ceil((diff / this.numColors)) * (k + 1)))));
						if (pix[i][j] <= (lowerBounds + (Math.ceil((diff / this.numColors)) * (k + 1))))
						{
							Log.d(TAG, "adding: " + k + " for " + pix[i][j]);
							sol[(i * alter.getWidth()) + j] = (k + "").charAt(0);
							break;
						}
						else
						{
							Log.d(TAG, "AHhhhhh");
						}
					}
				}
			}
			this.solution = new String(sol);
			Log.d(TAG, "Total: " + this.solution);
			// TODO do we need this?
			// this.bmCropped.setHasAlpha(true);

			final Bundle bundle = new Bundle();
			bundle.putString("current", this.solution);
			bundle.putString("height", this.height + "");
			bundle.putString("width", this.width + "");
			bundle.putString("id", this.solution.hashCode() + "");
			bundle.putString("solution", this.solution);
			String cols = "";
			// TODO: Switch transparency to the end.
			// this.newColors[0] = this.newColors[this.numColors - 1];
			// this.newColors[this.numColors - 1] = Color.TRANSPARENT;
			// Now build the string of colors.
			for (final int i : this.newColors) {
				cols += i + ",";
			}
			cols = cols.substring(0, cols.length() - 1);
			bundle.putString("colors", cols);
			return bundle;
		}
		else
		{
			final Bundle bundle = new Bundle();
			// current height width id solution colors(,)
			bundle.putString("current", this.solution);
			bundle.putString("height", this.height + "");
			bundle.putString("width", this.width + "");
			bundle.putString("id", this.solution.hashCode() + "");
			bundle.putString("solution", this.solution);
			String cols = "";
			for (final int i : this.newColors) {
				cols += i + ",";
			}
			cols = cols.substring(0, cols.length() - 1);
			bundle.putString("colors", cols);
			return bundle;
		}
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
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

		if ((requestCode == CAMERA_REQUEST) && (resultCode == RESULT_OK)) {
			final Bitmap photo = (Bitmap) data.getExtras().get("data");
			this.bmInitial = photo;
		} else if (requestCode == FILE_SELECT_CODE) {
			FlurryAgent.logEvent("CreateFromFile");
			if (resultCode == Activity.RESULT_OK) {
				final Uri uri = data.getData();
				final Bitmap bi = this.readBitmap(uri);
				this.bmInitial = bi;
			}
		}
		if (this.bmInitial != null) {
			if (CreateActivity.this.pager == null) {
				this.pager = (ViewPager) this.findViewById(R.id.pager);
			}
			this.pager.setCurrentItem(1, true);
		}
	}

	public void onClick(final View v) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_menu);// We use the same exact layout as the main menu. =)
		this.getSupportActionBar().setTitle(Html.fromHtml("<font color='#000000'>Select a Source Image</font>"));
		this.handler = new Handler();
		this.tabs = (PagerSlidingTabStrip) this.findViewById(R.id.tabs);
		this.pager = (ViewPager) this.findViewById(R.id.pager);
		this.adapter = new MyPagerAdapter(this.getSupportFragmentManager());
		this.pager.setPageTransformer(true, new DepthPageTransformer());
		this.pager.setAdapter(this.adapter);
		this.tabs.setOnPageChangeListener(this);
		this.spb = (SmoothProgressBar) this.findViewById(R.id.spbLoad);
		this.spb.setVisibility(View.INVISIBLE);
		final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, this
				.getResources()
				.getDisplayMetrics());
		this.pager.setPageMargin(pageMargin);

		this.tabs.setViewPager(this.pager);
		final Intent intent = this.getIntent();
		final String action = intent.getAction();
		final String type = intent.getType();
		if (Intent.ACTION_SEND.equals(action) && (type != null)) {
			if (type.startsWith("image/")) {
				final Uri uri = (Uri) intent
						.getParcelableExtra(Intent.EXTRA_STREAM);
				final Bitmap bi = this.readBitmap(uri);
			} else if (type.startsWith("text/")) {
				final String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
				if (sharedText != null) {
					this.processURL(sharedText);
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(this.getResources().getColor(R.color.light_yellow)));
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onNavigationItemSelected(final int itemPosition, final long itemId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
			case android.R.id.home:
				this.pager.setCurrentItem(this.TITLES.indexOf("Picture"));
				break;
		}
		return true;
	}

	public void onPageScrolled(final int arg0, final float arg1, final int arg2) {
		// TODO Auto-generated method stub

	}

	public void onPageScrollStateChanged(final int arg0) {
		// TODO Auto-generated method stub

	}

	public void onPageSelected(final int tab) {
		// currentTab = previous tab
		if (this.currentTab == 0)
		{
			// Store the initial image.
			// final ImageView iv = ((ImageView) this.findViewById(R.id.ivPrimaryPreview));
			// if (iv != null) {
			// iv.buildDrawingCache();
			// this.bmInitial = Bitmap.createBitmap(iv.getDrawingCache());
			// }
			if ((this.bmInitial == null) || ((this.bmInitial.getWidth() + this.bmInitial.getHeight()) <= 0))
			{
				// Make sure we've received an image.
				this.pager.setCurrentItem(0);
				return;
			}
			this.bmCropped = this.bmInitial;
		}
		else if (this.currentTab == 1)
		{
			Log.d(TAG, "LEAVING CROP");
			// Store the cropped image.
			this.bmCropped = ((CropImageView) this.findViewById(R.id.cropImageView)).getCroppedImage();
			this.solution = "";
			this.alterPhoto();
			if (((TouchImageView) this.findViewById(R.id.tivGameOne)) != null) {
				((TouchImageView) this.findViewById(R.id.tivGameOne)).gCurrent = this.solution;
			}
			if (((TouchImageView) this.findViewById(R.id.tivGameTwo)) != null) {
				((TouchImageView) this.findViewById(R.id.tivGameTwo)).gCurrent = this.solution;
			}
			if (((TouchImageView) this.findViewById(R.id.tivGameThree)) != null) {
				((TouchImageView) this.findViewById(R.id.tivGameThree)).gCurrent = this.solution;
			}
			if (((TouchImageView) this.findViewById(R.id.tivGameFour)) != null) {
				((TouchImageView) this.findViewById(R.id.tivGameFour)).gCurrent = this.solution;
			}
		}
		if (tab > 1)
		{
			if (this.bmCropped == null) {
				this.pager.setCurrentItem(0);
			}
			if (this.currentTab == this.TITLES.indexOf("Fine Tune"))
			{
				this.solution = ((TouchImageView) this.findViewById(R.id.tivGameThree)).gCurrent;
			}
		}
		else
		{
			// We're back at cropping or getting new image, so remove fine tune settings.
			this.hasFineTuned = false;
			this.fineTunedSolution = "";
		}
		if (this.currentTab == 4)
		{
			// Leaving fine tune, so it must be "special"

			this.fineTunedSolution = ((TouchImageView) this.findViewById(R.id.tivGameThree)).gCurrent;
		}
		this.currentTab = tab; // currentTab = tab being switched to.
		if (this.currentTab == 0)
		{
			this.getSupportActionBar().setTitle(Html.fromHtml("<font color='#000000'>Select a Source Image</font>"));
			if (this.bmInitial != null) {
				// ((ImageView) this.findViewById(R.id.ivPrimaryPreview)).setImageBitmap(this.bmInitial);
			}
		} else if (this.currentTab == 1)
		{
			this.getSupportActionBar().setTitle(Html.fromHtml("<font color='#000000'>Crop the Image</font>"));
			if (this.bmInitial != null) {
				((CropImageView) this.findViewById(R.id.cropImageView)).setImageBitmap(this.bmInitial);
			}
		}
		else if (this.currentTab == 2)
		{
			this.getSupportActionBar().setTitle(Html.fromHtml("<font color='#000000'>Set Size of Puzzle</font>"));
		}
		else if (this.currentTab == 3)
		{
			this.getSupportActionBar().setTitle(Html.fromHtml("<font color='#000000'>Change the Colors</font>"));
		}
		else if (this.currentTab == 4)
		{
			this.getSupportActionBar().setTitle(Html.fromHtml("<font color='#000000'>Fine Tune the Puzzle</font>"));
		}
		else if (this.currentTab == 5)
		{
			this.getSupportActionBar().setTitle(Html.fromHtml("<font color='#000000'>Name, tag, and submit it.</font>"));
		}

		this.updateAllTouchImageViews();
		this.invalidateOptionsMenu();
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	// Read bitmap - From
	// http://tutorials-android.blogspot.co.il/2011/11/outofmemory-exception-when-decoding.html
	private void processURL(final String url) {
		// TODO Start the progress bar.
		FlurryAgent.logEvent("CreateURLProcess");
		final Activity a = this;
		new Thread(new Runnable() {
			public void run() {
				try {
					final URL uri = new URL(url);
					final HttpURLConnection conn = (HttpURLConnection) uri
							.openConnection();
					conn.setDoInput(true);
					conn.connect();
					InputStream in;
					in = conn.getInputStream();
					final Bitmap bm = BitmapFactory.decodeStream(in);
					CreateActivity.this.bmInitial = bm;
					a.runOnUiThread(new Runnable() {

						public void run() {
							if (CreateActivity.this.bmInitial != null)
							{
								if (CreateActivity.this.pager == null) {
									CreateActivity.this.pager = (ViewPager) CreateActivity.this.findViewById(R.id.pager);
								}
								CreateActivity.this.pager.setCurrentItem(1, true);
								// TODO Stop the progress bar.
							}
							CreateActivity.this.spb.setVisibility(View.INVISIBLE);
						}

					});
				} catch (final IOException e) {
					Crouton.makeText(a, "Failed to get image.", Style.ALERT)
					.show();
					e.printStackTrace();
					CreateActivity.this.spb.setVisibility(View.INVISIBLE);
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

	@SuppressLint("NewApi")
	public void runCamera() {
		final Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

			final ActivityOptions opts = ActivityOptions.makeCustomAnimation(
					this, R.anim.fadein, R.anim.fadeout);
			this.startActivityForResult(cameraIntent, CAMERA_REQUEST, opts.toBundle());
		}
		else
		{
			this.startActivityForResult(cameraIntent, CAMERA_REQUEST);
		}
	}

	@SuppressLint("NewApi")
	public void runFile() {
		// File
		final Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
		fileIntent.setType("image/*");
		fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			final ActivityOptions opts = ActivityOptions.makeCustomAnimation(
					this, R.anim.fadein, R.anim.fadeout);
			this.startActivityForResult(fileIntent, FILE_SELECT_CODE, opts.toBundle());
		} else {
			this.startActivityForResult(fileIntent, FILE_SELECT_CODE);
		}
	}

	public void runURL() {
		// Website
		this.spb.setVisibility(View.VISIBLE);
		final EditText input = new EditText(this);
		input.setText("http://upload.wikimedia.org/wikipedia/commons/a/ab/Monarch_Butterfly_Showy_Male_3000px.jpg");
		new AlertDialog.Builder(this)
		.setTitle("URL Sumittion")
		.setMessage("Url Link to Image File...")
		.setView(input)
		.setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog,
					final int whichButton) {
				final Editable value = input.getText();
				CreateActivity.this.handler.post(new Runnable() {

					public void run() {
						CreateActivity.this.processURL(value.toString());
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
	}

	public void updateAllTouchImageViews() {
		this.spb.setVisibility(View.VISIBLE);
		Bundle b = null;
		try {
			b = this.alterPhoto();
		} catch (final Exception e) {
			Log.d(TAG, "Error: " + e);
		}
		if (b != null) {
			TouchImageView tivGameFour = null, tivGameThree = null, tivGameTwo = null, tivGameOne = null;
			b.putString("current", b.getString("solution"));
			b.putString("solution", b.getString("solution"));
			String cols = "";
			for (final int col : this.newColors) {
				cols += col + ",";
			}
			b.putString("colors", cols);
			b.putBoolean("refresh", true);
			if (tivGameOne == null) {
				tivGameOne = (TouchImageView) this.findViewById(R.id.tivGameOne);
			}
			if (tivGameOne != null) {
				if (this.hasFineTuned && (!this.fineTunedSolution.isEmpty())) {
					b.putString("solution", this.fineTunedSolution);
					b.putString("current", this.fineTunedSolution);
				}
				tivGameOne.setPicogramInfo(b);
			}
			if (tivGameTwo == null) {
				tivGameTwo = (TouchImageView) this.findViewById(R.id.tivGameTwo);
			}
			if (tivGameTwo != null) {
				if (this.hasFineTuned && (!this.fineTunedSolution.isEmpty())) {
					b.putString("solution", this.fineTunedSolution);
					b.putString("current", this.fineTunedSolution);
				}
				tivGameTwo.setPicogramInfo(b);
			}
			if (tivGameThree == null) {
				tivGameThree = (TouchImageView) this.findViewById(R.id.tivGameThree);
				// Save new "solution".
				if (tivGameThree != null) {
					if (!this.solution.equals(tivGameThree.gCurrent))
					{
						Log.d(TAG, "New new new!");
						this.solution = tivGameThree.gCurrent;
						this.hasFineTuned = true;
					}
				}
			}
			if (tivGameThree != null) {
				b.putBoolean("refresh", false);
				if (this.hasFineTuned && (!this.fineTunedSolution.isEmpty())) {
					b.putString("solution", this.fineTunedSolution);
					b.putString("current", this.fineTunedSolution);
				}
				tivGameThree.setPicogramInfo(b);
			}
			if (tivGameFour == null) {
				tivGameFour = (TouchImageView) this.findViewById(R.id.tivGameFour);
			}
			if (tivGameFour != null) {
				if (this.hasFineTuned && (!this.fineTunedSolution.isEmpty())) {
					b.putString("solution", this.fineTunedSolution);
					b.putString("current", this.fineTunedSolution);
				}

				tivGameFour.setPicogramInfo(b);
			}
		}
		this.spb.setVisibility(View.INVISIBLE);
	}

	private void updateValuesFromFragments() {
		if (this.currentTab == 2)
		{
			final CreateFragment cm = ((CreateFragment) CreateActivity.this.adapter.getItem(this.pager.getCurrentItem()));
			// this.width = cm.width;
			// this.height = ((CreateFragment) MultiStepCreateActivity.this.adapter.getItem(2)).height;
			// this.numColors = ((CreateFragment) MultiStepCreateActivity.this.adapter.getItem(2)).numColor;
		}
		else
		{
			this.name = "";
			this.tags = "";
			this.difficulty = "";
		}
	}
}
