package com.pic2griddler.awesomeness;

import com.github.espiandev.showcaseview.ShowcaseView;
import com.google.analytics.tracking.android.EasyTracker;
import com.pic2griddler.awesomeness.TouchImageView.WinnerListener;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class AdvancedGameActivity extends Activity implements OnClickListener, WinnerListener, ShowcaseView.OnShowcaseEventListener {
	private static final String TAG = "AdvancedGameActivity";
	TouchImageView tiv;
	ShowcaseView sv;
	Handler handle = new Handler();
	int tutorialStep = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_advanced_game);

		Button bHand = (Button) findViewById(R.id.bToolboxHand);
		Button bWhite = (Button) findViewById(R.id.bToolboxWhite);
		Button bBlack = (Button) findViewById(R.id.bToolboxBlack);
		bHand.setOnClickListener(this);
		bWhite.setOnClickListener(this);
		bBlack.setOnClickListener(this);
		tiv = (TouchImageView) findViewById(R.id.tivGame);
		tiv.setWinListener(this);
		tiv.setGriddlerInfo(getIntent().getExtras());
		String name = getIntent().getExtras().getString("name");
		Log.d(TAG, "Name:  " + name);
		if (name != null) {
			if (name.equals("Tutorial")) {
				Log.d(TAG, "IN TUTORIAL!");
				// We're in a tutorial.
				showStepOne();
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_advanced_game, menu);
		return true;
	}

	public void onClick(View v) {
		if (v.getId() == R.id.bToolboxHand) {
			tiv.isGameplay = false;
		} else if (v.getId() == R.id.bToolboxBlack) {
			tiv.colorCharacter = '1';
			tiv.isGameplay = true;
		} else if (v.getId() == R.id.bToolboxWhite) {
			tiv.colorCharacter = '0';
			tiv.isGameplay = true;
		}
	}

	private void showStepOne() {
		ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
		co.hideOnClickOutside = true;
		sv = ShowcaseView
				.insertShowcaseView(
						R.id.llToolbox,
						this,
						"Movement and Brush Color",
						"Here are your tools to use during your griddler-ing.  The Move is used to move and zoom.  You can zoom in via-pinching, and from there, you may slide around.\n\nAs you may know, Griddlers use colors to draw pictures.  This is also the location of your brushes you may use.",
						co);
		sv.setOnShowcaseEventListener(this);

	}

	private void showStepTwo() {

		ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
		co.hideOnClickOutside = true;
		sv = ShowcaseView
				.insertShowcaseView(
						R.id.tivGame,
						this,
						"Game Board",
						"This here is the heart and soul of the Griddler game.  This is your game board.\n\nAs you can see, the side and top numbers are your hints. You can use these to figure out this board.  If you already know how to win, just play.  If not, then follow the steps.",
						co);
		sv.setOnShowcaseEventListener(this);

	}

	private void showStepThree() {

		ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
		co.hideOnClickOutside = true;
		sv = ShowcaseView
				.insertShowcaseView(
						R.id.tivGame,
						this,
						"First Row",
						"As you can see, the board is a 4X4.  If we see the numbers on the side, we see two rows have 4.  This means, by deduction, the whole row must be filled.  So let's finish filling up the first row. \n\nRemember, Click the black up top to be able to draw.",
						co);
		sv.setOnShowcaseEventListener(this);

	}

	private void showStepFour() {
		ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
		co.hideOnClickOutside = true;
		sv = ShowcaseView.insertShowcaseView(R.id.tivGame, this, "Finish her!",
				"Good, now you just gotta finish the two. If we check the side hints, we can see 1 1.  This means that we have one black, with some white space between the next.  Use the top hints to figure out where the remainding pieces go.", co);
		sv.setOnShowcaseEventListener(this);

	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this); // Add this method.
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.
	}

	@Override
	public void onBackPressed() {
		super.onPause();
		Intent returnIntent = new Intent();
		returnIntent.putExtra("current", tiv.gCurrent);
		returnIntent.putExtra("status", "0");
		returnIntent.putExtra("ID", tiv.gSolution.hashCode() + "");
		setResult(2, returnIntent);
		finish();
	}

	public void win() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Congrats! You won!").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent returnIntent = new Intent();
				returnIntent.putExtra("current", tiv.gCurrent);
				returnIntent.putExtra("status", "1");
				returnIntent.putExtra("ID", tiv.gSolution.hashCode() + "");
				setResult(2, returnIntent);
				finish();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void onShowcaseViewHide(ShowcaseView showcaseView) {

		if (tutorialStep == 0) {
			tutorialStep++;
			showStepTwo();
		} else if (tutorialStep == 1) {
			tutorialStep++;
			showStepThree();
		} else if (tutorialStep == 2) {
			// This is our final step, and we must wait until the top row is
			// filled to continue. Run a thread and wait until the game is
			// either finished, or they complete the top row to show the
			// next step.
			new Thread(new Runnable() {

				public void run() {
					while (true) {
						if (tiv.gCurrent.startsWith("1111")) {
							tutorialStep++;
							handle.post(new Runnable() {

								public void run() {
									showStepFour();
								}
								
							});
							break;
						}
					}
				}

			}).start();
		} else if (tutorialStep == 3) {
		}
	}

	public void onShowcaseViewShow(ShowcaseView showcaseView) {
		// TODO Auto-generated method stub

	}
}
