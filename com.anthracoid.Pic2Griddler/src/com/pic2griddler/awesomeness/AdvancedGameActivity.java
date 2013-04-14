package com.pic2griddler.awesomeness;

import com.pic2griddler.awesomeness.TouchImageView.WinnerListener;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
		} else {
			tiv.isGameplay = true;

		}
	}

	public void win() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Congrats! You won!").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				finish();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
