
package com.picogram.awesomeness;

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
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;

import com.flurry.android.FlurryAgent;
import com.github.espiandev.showcaseview.ShowcaseView;
import com.picogram.awesomeness.TouchImageView.WinnerListener;
import com.stackmob.sdk.callback.StackMobModelCallback;
import com.stackmob.sdk.exception.StackMobException;

import java.util.ArrayList;

public class AdvancedGameActivity extends Activity implements OnTouchListener,
		WinnerListener, ShowcaseView.OnShowcaseEventListener {
	private static final String TAG = "AdvancedGameActivity";
	TouchImageView tiv;
	ShowcaseView sv;
	Handler handle = new Handler();
	int tutorialStep = 0;
	private static SQLiteGriddlerAdapter sql;
	int colors[];
	ArrayList<ImageView> ivs = new ArrayList<ImageView>();

	String puzzleId;

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
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
		if (name != null) {
			if (name.equals("Tutorial")) {
				// We're in a tutorial.
				if (!c.equals(s)) {
					this.showStepOne();
				}
			}
		}
		// Create colors for pallet.
		this.colors = this.getIntent().getExtras().getIntArray("colors");

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
				final Drawable drawable = this.getResources().getDrawable(R.drawable.grid);
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
		sql = new SQLiteGriddlerAdapter(this.getApplicationContext(), "Griddlers", null, 1);
	}

	public void onShowcaseViewHide(final ShowcaseView showcaseView) {

		if (this.tutorialStep == 0) {
			this.tutorialStep++;
			this.showStepTwo();
		} else if (this.tutorialStep == 1) {
			this.tutorialStep++;
			this.showStepThree();
		} else if (this.tutorialStep == 2) {
			// This is our final step, and we must wait until the top row is
			// filled to continue. Run a thread and wait until the game is
			// either finished, or they complete the top row to show the
			// next step.
			new Thread(new Runnable() {

				public void run() {
					while (true) {
						if (AdvancedGameActivity.this.tiv.gCurrent.startsWith("1111")) {
							AdvancedGameActivity.this.tutorialStep++;
							AdvancedGameActivity.this.handle.post(new Runnable() {

								public void run() {
									AdvancedGameActivity.this.showStepFour();
								}

							});
							break;
						}
					}
				}

			}).start();
		} else if (this.tutorialStep == 3) {
		}
	}

	public void onShowcaseViewShow(final ShowcaseView showcaseView) {
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

		final Intent returnIntent2 = new Intent();
		returnIntent2.putExtra("current", this.tiv.gCurrent);
		returnIntent2.putExtra("status", "1");
		returnIntent2.putExtra("ID", this.tiv.gSolution.hashCode() + "");
		this.setResult(2, returnIntent2);
		this.finish();
	}

	private void showStepFour() {
		final ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
		co.hideOnClickOutside = true;
		this.sv = ShowcaseView
				.insertShowcaseView(
						R.id.tivGame,
						this,
						"Finish her!",
						"Good, now you just gotta finish the two. If we check the side hints, we can see 1 1.  This means that we have one black, with some white space between the next.  Use the top hints to figure out where the remainding pieces go.",
						co);
		this.sv.setOnShowcaseEventListener(this);

	}

	private void showStepOne() {
		final ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
		co.hideOnClickOutside = true;
		this.sv = ShowcaseView
				.insertShowcaseView(
						R.id.llPallet,
						this,
						"Movement and Brush Color",
						"Here are your tools to use during your griddler-ing.  The Move is used to move and zoom.  You can zoom in via-pinching, and from there, you may slide around.\n\nAs you may know, Griddlers use colors to draw pictures.  This is also the location of your brushes you may use.",
						co);
		this.sv.setOnShowcaseEventListener(this);

	}

	private void showStepThree() {

		final ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
		co.hideOnClickOutside = true;
		this.sv = ShowcaseView
				.insertShowcaseView(
						R.id.tivGame,
						this,
						"First Row",
						"As you can see, the board is a 4X4.  If we see the numbers on the side, we see two rows have 4.  This means, by deduction, the whole row must be filled.  So let's finish filling up the first row. \n\nRemember, Click the black up top to be able to draw.",
						co);
		this.sv.setOnShowcaseEventListener(this);

	}

	private void showStepTwo() {

		final ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
		co.hideOnClickOutside = true;
		this.sv = ShowcaseView
				.insertShowcaseView(
						R.id.tivGame,
						this,
						"Game Board",
						"This here is the heart and soul of the Griddler game.  This is your game board.\n\nAs you can see, the side and top numbers are your hints. You can use these to figure out this board.  If you already know how to win, just play.  If not, then follow the steps.",
						co);
		this.sv.setOnShowcaseEventListener(this);

	}

	public void win() {
		final Dialog dialog = new Dialog(AdvancedGameActivity.this);
		dialog.setContentView(R.layout.ranking_dialogue);
		dialog.setTitle("Rate this Picogram");

		final RatingBar rb = (RatingBar) dialog.findViewById(R.id.rbRate);
		rb.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

			public void onRatingChanged(final RatingBar ratingBar, final float rating,
					final boolean fromUser) {
				if (fromUser)
				{
					final Griddler g = new Griddler();
					g.setID(AdvancedGameActivity.this.puzzleId);
					g.fetch(new StackMobModelCallback() {

						@Override
						public void failure(final StackMobException arg0) {

							dialog.dismiss();
							AdvancedGameActivity.this.returnIntent();
						}

						@Override
						public void success() {

							g.setRate(((Integer.parseInt(g.getRate()) * g.getNumberOfRatings()) + (int) rating)
									+ "");
							g.setNumberOfRatings(g.getNumberOfRatings() + 1);
							// TODO: If save fails, let us do it next time app is online.
							g.save();
							dialog.dismiss();
							AdvancedGameActivity.this.returnIntent();
						}

					});
				}

			}
		});

		dialog.show();
	}
}
