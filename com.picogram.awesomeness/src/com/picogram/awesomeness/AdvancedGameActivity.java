
package com.picogram.awesomeness;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;

import com.flurry.android.FlurryAgent;
import com.picogram.awesomeness.TouchImageView.WinnerListener;
import com.stackmob.sdk.callback.StackMobModelCallback;
import com.stackmob.sdk.exception.StackMobException;

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
		return new int[] {
				a, r, g, b
		};
	}

	@Override
	public void onBackPressed() {
		super.onPause();
		final Intent returnIntent = new Intent();
		returnIntent.putExtra("current", this.tiv.gCurrent);
		returnIntent.putExtra("status", "0");
		returnIntent.putExtra("ID", this.tiv.gSolution.hashCode() + "");
		this.setResult(2, returnIntent);
		this.finish();
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
		final String[] cols = this.getIntent().getExtras().getString("colors").split(",");
		this.colors = new int[cols.length];
		for (int i = 0; i != cols.length; ++i) {
			this.colors[i] = Integer.parseInt(cols[i]);
		}
		ImageView colorChange = new ImageView(this);
		final LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;
		final Drawable drawableBitmap = this.getResources().getDrawable(R.drawable.icon);
		final Bitmap moveBitmap = Bitmap.createScaledBitmap(
				((BitmapDrawable) drawableBitmap).getBitmap(), 100, 100, true);
		colorChange.setImageBitmap(moveBitmap);
		colorChange.setOnTouchListener(this);
		colorChange.setLayoutParams(lp);
		colorChange.setPadding(13, 13, 13, 13);
		ll.addView(colorChange);
		for (int i = 0; i != this.colors.length; ++i)
		{
			Bitmap fullColor = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
			colorChange = new ImageView(this);
			final int[] rgb = this.getRGB(this.colors[i]);
			if (rgb[0] == 0) {
				final Drawable drawable = this.getResources().getDrawable(R.drawable.light_grid);
				fullColor = Bitmap.createScaledBitmap(((BitmapDrawable) drawable).getBitmap(), 100,
						100, true);
			} else {
				for (int x = 0; x != fullColor.getWidth(); ++x) {
					for (int y = 0; y != fullColor.getHeight(); ++y) {
						if ((x < 3) || (y < 3) || (x > (fullColor.getWidth() - 3))
								|| (y > (fullColor.getHeight() - 3))) {
							fullColor.setPixel(x, y, Color.BLACK);
						}
						else
						{
							fullColor.setPixel(x, y, Color.rgb(rgb[1], rgb[2], rgb[3]));
						}
					}
				}
			}
			colorChange.setImageBitmap(fullColor);
			colorChange.setOnTouchListener(this);
			colorChange.setVisibility(View.VISIBLE);
			colorChange.setLayoutParams(lp);
			colorChange.setPadding(13, 13, 13, 13);

			ll.addView(colorChange);
			this.ivs.add(colorChange);
		}
		final LinearLayout pallet = (LinearLayout) this.findViewById(R.id.llPallet);
		pallet.addView(ll);

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
		sql.updateCurrentGriddler(this.tiv.gSolution.hashCode() + "", "0", this.tiv.gCurrent);
		sql.close();
	}

	@Override
	public void onResume() {
		super.onResume();
		Util.updateFullScreen(this);
		sql = new SQLiteGriddlerAdapter(this.getApplicationContext(), "Griddlers", null, 1);
	}



	public boolean onTouch(final View v, final MotionEvent event) {
		final int index = this.ivs.indexOf(v);
		if (index < 0) {
			this.tiv.isGameplay = false;
		} else
		{
			this.tiv.isGameplay = true;
			this.tiv.colorCharacter = (index + "").charAt(0);
		}
		return true;
	}

	private void returnIntent() {

		final Intent returnIntent = new Intent();
		returnIntent.putExtra("current", this.tiv.gCurrent);
		returnIntent.putExtra("status", "1");
		returnIntent.putExtra("ID", this.tiv.gSolution.hashCode() + "");
		this.setResult(Activity.RESULT_OK, returnIntent);
		this.finish();
	}

	public void win() {
		final Dialog dialog = new Dialog(AdvancedGameActivity.this);
		dialog.setContentView(R.layout.dialog_ranking);
		dialog.setTitle("Rate this Picogram");

		final RatingBar rb = (RatingBar) dialog.findViewById(R.id.rbRate);
		rb.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

			public void onRatingChanged(final RatingBar ratingBar, final float rating,
					final boolean fromUser) {
				if (fromUser)
				{
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
							g.setRating(((Integer.parseInt(g.getRating()) * g.getNumberOfRatings()) + (int) rating)
									+ "");
							g.setNumberOfRatings(g.getNumberOfRatings() + 1);
							// TODO: If save fails, let us do it next time app is online.
							g.save();
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
