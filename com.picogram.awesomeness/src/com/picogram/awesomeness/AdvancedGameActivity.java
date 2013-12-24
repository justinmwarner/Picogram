package com.picogram.awesomeness;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;

import com.capricorn.ArcMenu;
import com.capricorn.RayMenu;
import com.flurry.android.FlurryAgent;
import com.picogram.awesomeness.TouchImageView.WinnerListener;
import com.stackmob.sdk.callback.StackMobModelCallback;
import com.stackmob.sdk.exception.StackMobException;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class AdvancedGameActivity extends Activity implements OnTouchListener,
		WinnerListener {
	private static final String TAG = "AdvancedGameActivity";
	TouchImageView tiv;
	Handler handle = new Handler();
	int tutorialStep = 0;
	private static SQLiteGriddlerAdapter sql;
	int colors[];
	ArrayList<ImageView> ivs = new ArrayList<ImageView>();

	String puzzleId;

	boolean isDialogueShowing = false;

	private void doFacebookStuff() {
	}

	private void doTwitterStuff() {
	}

	private int[] getRGB(final int i) {

		final int a = (i >> 24) & 0xff;
		final int r = (i >> 16) & 0xff;
		final int g = (i >> 8) & 0xff;
		final int b = (i & 0xff);
		return new int[] { a, r, g, b };
	}

	@Override
	public void onBackPressed() {
		super.onPause();
		this.returnIntent();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_advanced_game);
		Util.setTheme(this);
		this.tiv = (TouchImageView) this.findViewById(R.id.tivGame);
		this.tiv.setWinListener(this);
		this.tiv.setGriddlerInfo(this.getIntent().getExtras());
		final String name = this.getIntent().getExtras().getString("name");
		final String c = this.getIntent().getExtras().getString("current");
		final String s = this.getIntent().getExtras().getString("solution");
		this.puzzleId = this.getIntent().getExtras().getString("id");
		FlurryAgent.logEvent("UserPlayingGame");
		// Create colors for pallet.
		final String thing = this.getIntent().getExtras().getString("colors");
		final String[] cols = this.getIntent().getExtras().getString("colors")
				.split(",");
		this.colors = new int[cols.length];
		for (int i = 0; i != cols.length; ++i) {
			this.colors[i] = Integer.parseInt(cols[i]);
		}

		Bitmap[] bmColors = getMenuBitmaps();
		// Movement, X's, transparent, then colors.
		RayMenu rayMenu = (RayMenu) findViewById(R.id.ray_menu);
		final ArrayList<View> ivs = new ArrayList();
		for (int i = 0; i < bmColors.length; i++) {
			ImageView item = new ImageView(this); 
			item.setImageBitmap(bmColors[i]);
			item.setBackgroundDrawable(this.getResources().getDrawable(
					R.drawable.dropshadows));
			ivs.add(item);
			final int position = i;
			rayMenu.addItem(item, new OnClickListener() {

				public void onClick(View v) {
					Log.d(TAG, "Clicked: " + ivs.indexOf(v));
					if (ivs.indexOf(v) == 0) {
						// Moving.
						tiv.isGameplay = false;
					} else if (ivs.indexOf(v) == 1) {
						tiv.isGameplay = true;
						tiv.colorCharacter = 'x';
					} else {
						tiv.isGameplay = true;
						// Minus 2 for the X's and movement.
						tiv.colorCharacter = ((ivs.indexOf(v) - 2) + "")
								.charAt(0);
					}
				}
			});// Add a menu item
		}
	}

	private Bitmap[] getMenuBitmaps() {
		// +2 for movement and x's.
		Bitmap[] result = new Bitmap[this.colors.length + 2];
		final Drawable moveDrawable = this.getResources().getDrawable(
				R.drawable.move);
		final Bitmap moveBitmap = Bitmap.createScaledBitmap(
				((BitmapDrawable) moveDrawable).getBitmap(), 100, 100, true);
		final Drawable xDrawable = this.getResources().getDrawable(
				R.drawable.xs);
		final Bitmap xBitmap = Bitmap.createScaledBitmap(
				((BitmapDrawable) xDrawable).getBitmap(), 100, 100, true);
		result[0] = moveBitmap;
		result[1] = xBitmap;
		for (int i = 0; i != colors.length; ++i) {
			Bitmap fullColor = Bitmap.createBitmap(1, 1,
					Bitmap.Config.ARGB_8888);
			final int[] rgb = this.getRGB(this.colors[i]);
			if (rgb[0] == 0) {// This is alpha.
				// For transparency.
				fullColor = BitmapFactory.decodeResource(this.getResources(),
						R.drawable.transparent);
			} else {
				Log.d(TAG, "Making: " + colors[i]);
				fullColor.setPixel(0, 0, Color.rgb(rgb[1], rgb[2], rgb[3]));

			}
			// +2 for the X's and movement.
			result[i + 2] = fullColor;
		}
		return result;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getMenuInflater().inflate(R.menu.activity_advanced_game, menu);
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
		sql.updateCurrentGriddler(this.tiv.gSolution.hashCode() + "", "0",
				this.tiv.gCurrent);
		sql.close();
	}

	@Override
	public void onResume() {
		super.onResume();
		Util.updateFullScreen(this);
		sql = new SQLiteGriddlerAdapter(this.getApplicationContext(),
				"Griddlers", null, 1);
	}

	public boolean onTouch(final View v, final MotionEvent event) {
		final int index = this.ivs.indexOf(v);
		if (index < 0) {
			this.tiv.isGameplay = false;
		} else {
			this.tiv.isGameplay = true;
			this.tiv.colorCharacter = (index + "").charAt(0);
		}
		return true;
	}

	private void returnIntent() {

		final Intent returnIntent = new Intent();
		returnIntent.putExtra("current", this.tiv.gCurrent);
		Log.d(TAG, "CORRECT: |" + tiv.gCurrent + "|");
		Log.d(TAG, "CORRECT: |" + tiv.gSolution + "|");
		Log.d(TAG, "CORRECT: " + (tiv.gCurrent == tiv.gSolution));
		if (tiv.gCurrent.equals(tiv.gSolution)) {
			Log.d(TAG, "WE ARE CORRECT");
			returnIntent.putExtra("status", "1");
		} else
			returnIntent.putExtra("status", "0");
		returnIntent.putExtra("ID", this.tiv.gSolution.hashCode() + "");
		this.setResult(Activity.RESULT_OK, returnIntent);
		this.finish();
	}

	public void win() {
		final Dialog dialog = new Dialog(AdvancedGameActivity.this);
		dialog.setContentView(R.layout.dialog_ranking);
		dialog.setTitle("Rate this Picogram");
		dialog.setCancelable(false);

		final RatingBar rb = (RatingBar) dialog.findViewById(R.id.rbRate);
		rb.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

			public void onRatingChanged(final RatingBar ratingBar,
					final float rating, final boolean fromUser) {
				if (fromUser) {
					Log.d(TAG, "Rating given of: " + rating);
					final GriddlerOne g = new GriddlerOne();
					g.setID(AdvancedGameActivity.this.puzzleId);
					g.fetch(new StackMobModelCallback() {

						@Override
						public void failure(final StackMobException arg0) {
							dialog.dismiss();
							AdvancedGameActivity.this.isDialogueShowing = !AdvancedGameActivity.this.isDialogueShowing;
							AdvancedGameActivity.this.returnIntent();
						}

						@Override
						public void success() {
							Log.d(TAG,
									"Got a rating from online of: "
											+ g.getRating());
							Log.d(TAG,
									"Number of ratings online: "
											+ g.getNumberOfRatings());
							double oldRating = Double.parseDouble(g.getRating())
									* g.getNumberOfRatings();
							double newRating = (oldRating + rating)
									/ (g.getNumberOfRatings() + 1);
							Log.d(TAG, "New rating:" + newRating);
							g.setRating(newRating + "");
							g.setNumberOfRatings(g.getNumberOfRatings() + 1);
							// TODO: If save fails, let us do it next time app
							// is online.
							g.save();
							Log.d(TAG, "New Rating: " + g.getRating());
							Log.d(TAG,
									"New Number of ratings online: "
											+ g.getNumberOfRatings());
							dialog.dismiss();
							AdvancedGameActivity.this.isDialogueShowing = !AdvancedGameActivity.this.isDialogueShowing;
							AdvancedGameActivity.this.returnIntent();
						}

					});
				}

			}
		});
		if (!this.isDialogueShowing) {
			dialog.show();
			this.isDialogueShowing = !this.isDialogueShowing;
		}
	}
}
