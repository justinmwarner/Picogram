
package com.picogram.awesomeness;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.picogram.awesomeness.TouchImageView.WinnerListener;

public class TutorialActivity extends SherlockFragmentActivity {
	protected static final String TAG = "TutorialActivity";
	VideoView vv;
	TouchImageView tiv;
	TextView tv;
	int currentStep = 0;
	String tutorials[] = new String[] {
			"Fill your game below with the video to the side.  Please click the bottom game to draw.  Fill in a square by tapping once, make it an X by tapping the same spot, and again to clear it.",
			"Now look at this column here.  We can see it says 5 at the top, that means 5 filled in blocks in a row.  Because the height is 5, we can fill in all the squares.  Please do so below.",
			"In this row, the same rule applies. 5 width with a 5 in the row hint, means the whole row is filled in.  Again, do so below.",
			"If we look at this column, it says 1.  We already have 1 filled in square in this column, so we know the column is finished.  Fill the rest of the column up with X's by tapping each square twice.",
			"Looking at this row, we see 1 1.  We know that two spots are filled, but they're not connecting.  So we have one of those spots, so we can put an X on the other side, because it's not connecting.",
			"We can again fill the rest of this column up with X's.  But if we look, this leaves the first row with one spot left, so we know that that spot is filled, which makes the row finished.",
			"A common strategy is overlapping.  If we see this column has a 3, we notice 4 spots.  Take the blue, it goes down 3 from the remaining top (Not including the X).  The yellow from the bottom to the top.  These overlap, green, and that spot can be filled in.",
			"This column is similar to the 5 column.  We know that some amount of blank space is between the 1 and 3, the minimum amount being 1.  So if we add that spot to the filled in, 1 + 1 + 3 = 5.  The height is 5, so we knew this column from the start.",
			"Looking at this column, we can conclude the final space is the top.  The last row is already fullfilled, but a X isn't necessary to win.  So fill in the last spot to finish!",
			"CONGRATS!  You made a guy with an umbrella!"
	};
	int videos[] = new int[] {
			R.raw.tutorial_one,
			R.raw.tutorial_two,
			R.raw.tutorial_three,
			R.raw.tutorial_four,
			R.raw.tutorial_five,
			R.raw.tutorial_six,
			R.raw.tutorial_seven,
			R.raw.tutorial_eight,
			R.raw.tutorial_nine,
			R.raw.tutorial_ten,
	};
	boolean didX = false, didDraw = false, didClear = false;
	Handler handle = new Handler();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.updateFullScreen(this);
		this.setContentView(R.layout.activity_tutorial);

		Bundle b = new Bundle();
		b.putString("name", "Tutorial");
		b.putString("width", "5");
		b.putString("height", "5");
		b.putString("solution", "0010101100111110110100101");
		b.putString("current", "0000000000000000000000000");
		b.putString("colors", Color.TRANSPARENT + "," + Color.BLACK);
		tiv = (TouchImageView) findViewById(R.id.tivTutorial);
		tiv.gridlinesColor = Color.BLACK;
		tiv.setPicogramInfo(b);
		tv = (TextView) findViewById(R.id.tvTutorial);
		vv = (VideoView) findViewById(R.id.vvTutorial);
		vv.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tutorial_one));
		vv.setMediaController(new MediaController(this));
		vv.start();
		vv.requestFocus();
		vv.setMediaController(null);
		tv.setText(tutorials[currentStep]);
		tiv.colorCharacter = '1';
		tiv.isGameplay = true;
		new Thread(new Runnable() {

			public void run() {
				while (true) {
					int initStep = currentStep;
					if (currentStep == 0)
					{
						if (tiv.gCurrent.contains("x"))
							didX = true;
						else if (tiv.gCurrent.contains("1"))
							didDraw = true;
						if (didX && didDraw && !tiv.gCurrent.contains("x|1"))
							currentStep = 1;
					}
					if (tiv.gCurrent.equals("0010000100001000010000100"))
					{
						// Finished step one.
						currentStep = 2;
					} else if (tiv.gCurrent.equals("0010000100111110010000100"))
					{
						currentStep = 3;
					} else if (tiv.gCurrent.equals("001x0001x011111001x0001x0"))
					{
						currentStep = 4;
					} else if (tiv.gCurrent.equals("0x1x0001x011111001x0001x0"))
					{
						currentStep = 5;
					} else if (tiv.gCurrent.equals("xx1x1x01x011111x01x0x01x0"))
					{
						currentStep = 6;
					} else if (tiv.gCurrent.equals("xx1x1x01x011111x11x0x01x0"))
					{
						currentStep = 7;
					} else if (tiv.gCurrent.equals("xx1x1x01xx11111x11x1x01x1"))
					{
						currentStep = 8;
					} else if (tiv.gCurrent.equals("xx1x1x11xx11111x11x1x01x1"))
					{
						currentStep = 9;
					} else if (tiv.gCurrent.equals("0x1000x100111110x1000x100"))
					{
						// TODO: Track if the user ever gets here.
						handle.post(new Runnable() {

							public void run() {
								tv.setText("Please note!  Your game board is different from the video's game!");
							}
						});
					} else if (tiv.gCurrent.equals("xx1x1x01x011111x11x1x01x1"))
					{
						// TODO Track.
						handle.post(new Runnable() {

							public void run() {
								tv.setText("Don't forget to put an X for this tutorial (This isn't necessary outside of the tutorial)");
							}
						});
					}
					if (initStep != currentStep)
					{
						handle.post(new Runnable() {

							public void run() {
								tv.setText(tutorials[currentStep]);
								vv.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + videos[currentStep]));
								vv.start();
								if(currentStep == 9)
									tiv.gCurrent.replaceAll("x|X", "0");
							}
						});
					}
				}
			}
		}).start();
		vv.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer mp) {
				// When first video is done, go on to the second one.
				tv.setText(tutorials[currentStep]);
				vv.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + videos[currentStep]));
				vv.start();
			}
		});
		vv.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {

				return false;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		this.getSupportActionBar().setTitle("Tutorial");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
			case android.R.id.home:
				this.finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
