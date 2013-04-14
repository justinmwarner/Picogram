package com.pic2griddler.awesomeness;

import com.google.analytics.tracking.android.EasyTracker;
import com.pic2griddler.awesomeness.TouchImageView.WinnerListener;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AdvancedGameActivity extends Activity implements OnClickListener, WinnerListener {
	TouchImageView tiv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_advanced_game);

		tiv = (TouchImageView) findViewById(R.id.tivGame);
		tiv.setWinListener(this);
		tiv.setGriddlerInfo(getIntent().getExtras());
		Button bHand = (Button) findViewById(R.id.bToolboxHand);
		Button bWhite = (Button) findViewById(R.id.bToolboxWhite);
		Button bBlack = (Button) findViewById(R.id.bToolboxBlack);
		bHand.setOnClickListener(this);
		bWhite.setOnClickListener(this);
		bBlack.setOnClickListener(this);

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
}
