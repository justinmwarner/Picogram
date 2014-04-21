
package com.picogram.awesomeness;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.picogram.awesomeness.TouchImageView.WinnerListener;

public class TutorialActivity extends SherlockFragmentActivity {
	protected static final String TAG = "TutorialActivity";
	VideoView vv;
	TouchImageView tiv;
	TextView tv;
	int currentStep = 0;

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
		tv.setText("Fill your game below with the video to the side.  Please click the bottom game to draw.  Fill in a square by tapping once, make it an X by tapping the same spot");
		tiv.colorCharacter = '1';
		tiv.isGameplay = true;
		new Thread(new Runnable() {

			public void run() {
				while (true) {
					if (currentStep == 0 && tiv.gCurrent.contains("x") && tiv.gCurrent.contains("1"))
					{
						tiv.gCurrent = tiv.gCurrent.replaceAll("x|1", "0");
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
					} else if (tiv.gCurrent.equals("00100001001111100100001000"))
					{
						currentStep = 10;
					}
				}
			}
		}).start();
		vv.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer mp) {
				// When first video is done, go on to the second one.
				if (currentStep == 1)
				{
					vv.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tutorial_two));
					tv.setText("Now look at this column here.  We can see it says 5 at the top, that means 5 filled in blocks in a row.  Because the height is 5, we can fill in all the squares.  Please do so below.");
				}
				else if (currentStep == 2)
				{
					vv.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tutorial_three));
					tv.setText("");
				}
				else if (currentStep == 3)
				{
					vv.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tutorial_four));
					tv.setText("");
				}
				else if (currentStep == 4)
				{
					vv.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tutorial_five));
					tv.setText("");
				}
				else if (currentStep == 5)
				{
					vv.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tutorial_six));
					tv.setText("");
				}
				else if (currentStep == 6)
				{
					vv.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tutorial_seven));
					tv.setText("");
				}
				else if (currentStep == 7)
				{
					vv.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tutorial_eight));
					tv.setText("");
				}
				else if (currentStep == 8)
				{
					vv.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tutorial_nine));
					tv.setText("");
				}
				else if (currentStep == 9)
				{
					vv.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tutorial_ten));
					tv.setText("");
				}
				vv.start();
			}
		});
		vv.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					if (true)
					{
					}
					else
					{
					}
				return false;
			}
		});
	}

}
