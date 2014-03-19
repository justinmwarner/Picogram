
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.edmodo.cropper.CropImageView;
import com.flurry.android.FlurryAgent;

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
			"Picture", "Crop", "Game", "Colors", "Fine Tune", "Name", "Submit"
	}));

	private PagerSlidingTabStrip tabs;

	private ViewPager pager;
	private MyPagerAdapter adapter;
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

	protected Bundle alterPhoto() {
		this.updateValuesFromFragments();
		if (this.width == 0) {
			this.width = 20;
		}
		if (this.height == 0) {
			this.height = 20;
		}
		if (this.numColors == 0) {
			this.numColors = 2;
		}
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
			// TODO do we need this?
			// this.bmCropped.setHasAlpha(true);

			final Bundle bundle = new Bundle();
			// current height width id solution colors(,)
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
		return null;
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
			((CreateFragment) this.adapter.getItem(0)).setOriginalImage(photo, this);
		} else if (requestCode == FILE_SELECT_CODE) {
			FlurryAgent.logEvent("CreateFromFile");
			if (resultCode == Activity.RESULT_OK) {
				final Uri uri = data.getData();
				final Bitmap bi = this.readBitmap(uri);
				((CreateFragment) CreateActivity.this.adapter.getItem(0)).setOriginalImage(bi, this);
			}
		}
	}

	public void onClick(final View v) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_menu);// We use the same exact layout as the main menu. =)
		this.handler = new Handler();
		this.tabs = (PagerSlidingTabStrip) this.findViewById(R.id.tabs);
		this.pager = (ViewPager) this.findViewById(R.id.pager);
		this.adapter = new MyPagerAdapter(this.getSupportFragmentManager());
		this.pager.setPageTransformer(true, new DepthPageTransformer());
		this.pager.setAdapter(this.adapter);
		this.tabs.setOnPageChangeListener(this);
		final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, this
				.getResources()
				.getDisplayMetrics());
		this.pager.setPageMargin(pageMargin);

		this.tabs.setViewPager(this.pager);
		final ActionBar actionBar = this.getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		final Intent intent = this.getIntent();
		final String action = intent.getAction();
		final String type = intent.getType();
		if (Intent.ACTION_SEND.equals(action) && (type != null)) {
			if (type.startsWith("image/")) {
				final Uri uri = (Uri) intent
						.getParcelableExtra(Intent.EXTRA_STREAM);
				final Bitmap bi = this.readBitmap(uri);
				((CreateFragment) CreateActivity.this.adapter.getItem(0)).setOriginalImage(bi, this);
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
		final ActionBar ab = this.getSupportActionBar();
		final ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(this, R.array.listCreatePicture,
				R.layout.sherlock_spinner_item);

		if (list != null) {
			ab.setDisplayShowTitleEnabled(false);
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
			ab.setListNavigationCallbacks(list, this);
		}

		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowTitleEnabled(false);
		ab.setDisplayUseLogoEnabled(false);
		this.invalidateOptionsMenu();
		return true;
	}

	@SuppressLint("NewApi")
	public boolean onNavigationItemSelected(final int itemPosition, final long itemId) {
		if (itemPosition == 0) {
			// Custom
		}
		else if (itemPosition == 1) {
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
		} else if (itemPosition == 2) {
			// Website
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
		} else if (itemPosition == 3)
		{
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
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		if (item.getItemId() == android.R.id.home)
		{
			if (this.pager.getCurrentItem() == this.TITLES.indexOf("Picture"))
			{
				this.finish();
			}
			else {
				this.pager.setCurrentItem(this.TITLES.indexOf("Picture"));
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void onPageScrolled(final int arg0, final float arg1, final int arg2) {
		// TODO Auto-generated method stub

	}

	public void onPageScrollStateChanged(final int arg0) {
		// TODO Auto-generated method stub

	}

	public void onPageSelected(final int tab) {
		if (this.currentTab == 0)
		{
			// Store the initial image.
			final ImageView iv = ((ImageView) this.findViewById(R.id.ivPrimaryPreview));
			iv.buildDrawingCache();
			this.bmInitial = Bitmap.createBitmap(iv.getDrawingCache());
			if ((this.bmInitial == null) || ((this.bmInitial.getWidth() + this.bmInitial.getHeight()) <= 0))
			{
				// Make sure we've received an image.
				this.pager.setCurrentItem(0);
				return;
			}
		}
		else if (this.currentTab == 1)
		{
			// Store the cropped image.
			this.bmCropped = ((CropImageView) this.findViewById(R.id.cropImageView)).getCroppedImage();
		}
		if (tab > 1)
		{
			if (this.bmCropped == null) {
				this.pager.setCurrentItem(0);
			}
		}
		this.currentTab = tab;
		if (this.currentTab == 0)
		{
			if (this.bmInitial != null) {
				((ImageView) this.findViewById(R.id.ivPrimaryPreview)).setImageBitmap(this.bmInitial);
			}
		} else if (this.currentTab == 1)
		{
			if (this.bmInitial != null) {
				((CropImageView) this.findViewById(R.id.cropImageView)).setImageBitmap(this.bmInitial);
			}
		}
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
					((CreateFragment) CreateActivity.this.adapter.getItem(0)).setOriginalImage(bm, a);
					// TODO Stop the progress bar.
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
				((CreateFragment) this.adapter.getItem(0)).setOriginalImage(bm, this);
				fileDescriptor.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return bm;
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
