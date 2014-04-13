
package com.picogram.awesomeness;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.picogram.awesomeness.DialogMaker.OnDialogResultListener;
import com.picogram.awesomeness.TouchImageView.HistoryListener;
import com.picogram.awesomeness.TouchImageView.WinnerListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AdvancedGameActivity extends FragmentActivity implements
		OnTouchListener, WinnerListener, View.OnClickListener,
		OnSeekBarChangeListener {
	class RefreshHandler extends Handler {
		Activity activity;

		@Override
		public void handleMessage(final Message msg) {
			// Do the time
			final DateFormat df = new SimpleDateFormat("hh:mm:ss");
			final String curDateTime = df.format(Calendar.getInstance().getTime());
			((TextView) this.activity.findViewById(R.id.tvTime))
					.setText(curDateTime);
			// Do the battery.
			AdvancedGameActivity.this.registerReceiver(AdvancedGameActivity.this.mBatInfoReceiver,
					new IntentFilter(
							Intent.ACTION_BATTERY_CHANGED));
			this.sleep(100, this.activity);
		}

		public void sleep(final long delayMillis, final Activity a) {
			this.removeMessages(0);
			this.activity = a;
			this.sendMessageDelayed(this.obtainMessage(0), delayMillis);
		}
	}

	private static final String TAG = "AdvancedGameActivity";
	TouchImageView tiv;
	Handler handle = new Handler();
	int tutorialStep = 0;
	private static SQLitePicogramAdapter sql;
	int colors[], part;
	String strColors[];
	ArrayList<ImageView> ivs = new ArrayList<ImageView>();

	private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context arg0, final Intent intent) {
			// this will give you battery current status
			final ImageView ivBattery = (ImageView) AdvancedGameActivity.this
					.findViewById(R.id.ivBattery);
			final int level = intent.getIntExtra("level", 0);
			final int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
			final boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING)
					|| (status == BatteryManager.BATTERY_STATUS_FULL);
			if (isCharging) {
				if (AdvancedGameActivity.this.isLight) {
					ivBattery.setImageBitmap(BitmapFactory.decodeResource(
							AdvancedGameActivity.this.getResources(),
							R.drawable.batterychargingdark));
				} else {
					ivBattery.setImageBitmap(BitmapFactory.decodeResource(
							AdvancedGameActivity.this.getResources(),
							R.drawable.batterycharginglight));
				}
			} else {
				if (level >= 95) {
					if (AdvancedGameActivity.this.isLight) {
						ivBattery.setImageBitmap(BitmapFactory.decodeResource(
								AdvancedGameActivity.this.getResources(),
								R.drawable.batteryfulldark));
					} else {
						ivBattery.setImageBitmap(BitmapFactory
								.decodeResource(AdvancedGameActivity.this.getResources(),
										R.drawable.batterycharginglight));
					}
				} else if (level >= 30) {
					if (AdvancedGameActivity.this.isLight) {
						ivBattery.setImageBitmap(BitmapFactory.decodeResource(
								AdvancedGameActivity.this.getResources(),
								R.drawable.batteryhalfdark));
					} else {
						ivBattery.setImageBitmap(BitmapFactory.decodeResource(
								AdvancedGameActivity.this.getResources(),
								R.drawable.batteryhalflight));
					}
				} else if (level >= 5) {
					if (AdvancedGameActivity.this.isLight) {
						ivBattery
								.setImageBitmap(BitmapFactory.decodeResource(
										AdvancedGameActivity.this.getResources(),
										R.drawable.batterylowdark));
					} else {
						ivBattery.setImageBitmap(BitmapFactory.decodeResource(
								AdvancedGameActivity.this.getResources(),
								R.drawable.batterylowlight));
					}
				} else {
					if (AdvancedGameActivity.this.isLight) {
						ivBattery.setImageBitmap(BitmapFactory.decodeResource(
								AdvancedGameActivity.this.getResources(),
								R.drawable.batteryemptydark));
					} else {
						ivBattery.setImageBitmap(BitmapFactory.decodeResource(
								AdvancedGameActivity.this.getResources(),
								R.drawable.batteryemptylight));
					}
				}
			}
		}
	};

	private final RefreshHandler mRedrawHandler = new RefreshHandler();

	boolean isLight;

	boolean isFirstUndo = true;
	boolean continueMusic = true;

	HistoryListener historyListener;

	SeekBar sbHistory;

	long score, startTime;

	@Override
	public void onBackPressed() {
		super.onPause();
		this.returnIntent(null);
	}

	public void onClick(final View v) {
		final char[] curr = this.tiv.gCurrent.toCharArray();
		if (v.getId() == R.id.bUndo) {
			if (this.sbHistory.getProgress() == 0) {
				return;
			}
			if (this.isFirstUndo) {
				this.isFirstUndo = false;

				this.tiv.history.add(this.tiv.gCurrent);
				this.sbHistory.setMax(this.sbHistory.getMax() + 1);

				this.sbHistory.setProgress(this.sbHistory.getProgress() - 1);
				this.tiv.gCurrent = this.tiv.history.get(this.sbHistory.getProgress());
			}
		} else if (v.getId() == R.id.bRedo) {
			if (this.sbHistory.getProgress() == (this.sbHistory.getMax() - 1)) {
				return;
			}
			this.tiv.gCurrent = this.tiv.history.get(this.sbHistory.getProgress() + 1);
			this.sbHistory.setProgress(this.sbHistory.getProgress() + 1);
		} else if (v.getId() == R.id.ibTools) {
			final Bundle bundle = new Bundle();
			final FragmentTransaction ft = this.getSupportFragmentManager()
					.beginTransaction();
			bundle.putInt("layoutId", R.layout.dialog_color_choice);
			bundle.putStringArray("colors", this.strColors);
			final DialogMaker newFragment = new DialogMaker();
			newFragment.setArguments(bundle);
			newFragment.setOnDialogResultListner(new OnDialogResultListener() {

				public void onDialogResult(final Bundle result) {
					if (result.containsKey("colors")) {
						AdvancedGameActivity.this.tiv.gColors = result.getIntArray("colors");
						AdvancedGameActivity.this.tiv.isFirstTime = true;
						AdvancedGameActivity.this.tiv.bitmapFromCurrent();
						final String[] cols = new String[AdvancedGameActivity.this.tiv.gColors.length];
						for (int i = 0; i != cols.length; ++i) {
							cols[i] = "" + AdvancedGameActivity.this.tiv.gColors[i];
						}
						AdvancedGameActivity.this.strColors = cols;
						AdvancedGameActivity.this.tiv.colorCharacter = (result.getInt("color") + "")
								.charAt(0);
						AdvancedGameActivity.this.tiv.isGameplay = true;
						sql.updateColorsById(AdvancedGameActivity.this.tiv.gId,
								AdvancedGameActivity.this.strColors);
						newFragment.dismiss();
					} else {
						AdvancedGameActivity.this.tiv.isGameplay = result.getBoolean("isGameplay");
						AdvancedGameActivity.this.tiv.colorCharacter = result
								.getChar("colorCharacter");
						if (Character.isDigit(AdvancedGameActivity.this.tiv.colorCharacter)) {
							((ImageButton) v)
									.setBackgroundColor(AdvancedGameActivity.this.tiv.gColors[Integer
											.parseInt(""
													+ AdvancedGameActivity.this.tiv.colorCharacter)]);

						} else {
							((ImageButton) v).setBackgroundColor(Color.WHITE);
						}
						if (AdvancedGameActivity.this.tiv.colorCharacter == 'x')
						{
							if (AdvancedGameActivity.this.tiv.isGameplay)
							{
								// Drawing x's
								((ImageButton) v).setBackgroundDrawable(AdvancedGameActivity.this.getResources().getDrawable(R.drawable.xs));
							} else
							{
								// Movement
								((ImageButton) v).setBackgroundDrawable(AdvancedGameActivity.this.getResources().getDrawable(R.drawable.move));
							}
						} else if (AdvancedGameActivity.this.tiv.colorCharacter == '0')
						{
							// Transparent.
							((ImageButton) v).setBackgroundDrawable(AdvancedGameActivity.this.getResources().getDrawable(R.drawable.transparent));
						}

						newFragment.dismiss();
					}
				}
			});
			newFragment.show(ft, "dialog");
			return;
		}
		this.tiv.bitmapFromCurrent();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.updateFullScreen(this);
		this.setContentView(R.layout.activity_advanced_game);
		this.getActionBar().hide();
		this.tiv = (TouchImageView) this.findViewById(R.id.tivGame);
		// Do background stuff.
		final RelativeLayout rlMain = (RelativeLayout) this
				.findViewById(R.id.rlGameActivity);
		final String bg = Util.getPreferences(this)
				.getString("background", "bgWhite");
		String line = Util.getPreferences(this).getString("lines", "Auto");
		boolean isAnimating = false;
		if (bg.equals("bgWhite")) {
			rlMain.setBackgroundResource(android.R.color.transparent);
			rlMain.setBackgroundColor(Color.rgb(191, 191, 191));
			line = Color.rgb(64, 64, 64) + "";
		} else if (bg.equals("bgBlack")) {
			rlMain.setBackgroundResource(android.R.color.transparent);
			rlMain.setBackgroundColor(Color.rgb(64, 64, 64));
			line = Color.rgb(191, 191, 191) + "";
		} else if (bg.equals("skywave")) {
			isAnimating = true;
			line = Color.rgb(64, 64, 64) + "";
			rlMain.setBackgroundResource(R.drawable.skywave);
		} else if (bg.equals("darkbridge")) {
			isAnimating = true;
			rlMain.setBackgroundResource(R.drawable.darkbridge);
			line = Color.rgb(191, 191, 191) + "";
		} else if (bg.equals("spaceman")) {
			isAnimating = true;
			rlMain.setBackgroundResource(R.drawable.spaceman);
			line = Color.rgb(191, 191, 191) + "";
		} else {
			rlMain.setBackgroundResource(android.R.color.transparent);
			rlMain.setBackgroundColor(Color.rgb(191, 191, 191));
			line = Color.rgb(64, 64, 64) + "";
		}
		if (Util.getPreferences(this).getString("lines", "Auto").equals("Auto")) {
			this.tiv.gridlinesColor = Integer.parseInt(line);
			if (line.equals("" + Color.WHITE)) {
				this.isLight = false;
			} else {
				this.isLight = true;
			}
		} else {
			if (Util.getPreferences(this).getString("lines", "Auto")
					.equals("Light")) {
				this.tiv.gridlinesColor = Color.rgb(64, 64, 64);
				this.isLight = true;
			} else {
				this.tiv.gridlinesColor = Color.rgb(191, 191, 191);
				this.isLight = false;
			}
		}

		if (isAnimating) {
			final AnimationDrawable progressAnimation = (AnimationDrawable) this.findViewById(
					R.id.rlGameActivity).getBackground();
			progressAnimation.start();
		}
		Util.setTheme(this);

		// Time
		if (Util.getPreferences(this).getString("decorations", "None").equals("Custom")) {
			this.mRedrawHandler.sleep(100, this);
		}
		else
		{
			this.findViewById(R.id.tvTime).setVisibility(View.GONE);
			this.findViewById(R.id.ivBattery).setVisibility(View.GONE);
		}
		if (this.isLight) {
			((TextView) this.findViewById(R.id.tvTime)).setTextColor(Color.BLACK);
		} else {
			((TextView) this.findViewById(R.id.tvTime)).setTextColor(Color.WHITE);
		}

		this.tiv.setWinListener(this);
		this.tiv.setPicogramInfo(this.getIntent().getExtras());

		// Create colors for pallet.
		this.strColors = this.getIntent().getExtras().getString("colors").split(",");
		this.colors = new int[this.strColors.length];
		for (int i = 0; i != this.strColors.length; ++i) {
			this.colors[i] = Integer.parseInt(this.strColors[i]);
		}
		// History Bar.
		this.sbHistory = (SeekBar) this.findViewById(R.id.sbHistory);
		this.sbHistory.setOnSeekBarChangeListener(this);
		final Button bUndo = (Button) this.findViewById(R.id.bUndo);
		final Button bRedo = (Button) this.findViewById(R.id.bRedo);
		bUndo.setOnClickListener(this);
		bRedo.setOnClickListener(this);

		this.part = this.getIntent().getExtras().getInt("part");

		final Vibrator myVib = (Vibrator) this
				.getSystemService(VIBRATOR_SERVICE);
		this.historyListener = new HistoryListener() {

			public void action(final String curr) {
				myVib.vibrate(40);
				if (AdvancedGameActivity.this.sbHistory.getProgress() != AdvancedGameActivity.this.sbHistory
						.getMax()) {
					for (int i = AdvancedGameActivity.this.sbHistory.getProgress(); i != AdvancedGameActivity.this.sbHistory
							.getMax(); ++i) {
						AdvancedGameActivity.this.tiv.history
								.remove(AdvancedGameActivity.this.tiv.history.size() - 1);
					}
					AdvancedGameActivity.this.sbHistory.setMax(AdvancedGameActivity.this.sbHistory
							.getProgress());
				}
				AdvancedGameActivity.this.sbHistory.setMax(AdvancedGameActivity.this.sbHistory
						.getMax() + 1);
				AdvancedGameActivity.this.sbHistory.setProgress(AdvancedGameActivity.this.sbHistory
						.getMax());
				AdvancedGameActivity.this.tiv.history.add(curr);
				AdvancedGameActivity.this.isFirstUndo = true;
			}
		};
		this.tiv.setHistoryListener(this.historyListener);

		final ImageButton tools = (ImageButton) this.findViewById(R.id.ibTools);
		tools.setBackgroundColor(Color.WHITE);
		tools.setOnClickListener(this);

		// TODO Check for multiple solutions. If they exist tell the user as a
		// heads up.
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
		sql.updateCurrentPicogram(this.tiv.gSolution.hashCode() + "", "0",
				this.tiv.gCurrent);

		if (!this.continueMusic) {
			MusicManager.pause();
		}
		// Highscore management.
		if (this.score >= 0)
		{
			// If the score isn't negative, we want to add update the score in the preferences
			// and to the SQL database.
			final long newScore = this.score + (System.currentTimeMillis() - this.startTime);
			sql.updateScore(this.tiv.gId, newScore);
		}
		sql.close();
	}

	public void onProgressChanged(final SeekBar seekBar, final int progress,
			final boolean fromUser) {
		// Ignore if done programmatically.
		if (fromUser) {
			if (!(progress >= this.tiv.history.size())) {
				this.tiv.gCurrent = this.tiv.history.get(progress);
				this.tiv.bitmapFromCurrent();
			}
		}
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		this.tiv.gCurrent = savedInstanceState.getString("current");
		this.tiv.colorCharacter = savedInstanceState.getChar("drawCharacter");
		this.tiv.isGameplay = savedInstanceState.getBoolean("isGame");
		this.tiv.history = savedInstanceState.getStringArrayList("history");
		this.sbHistory.setMax(savedInstanceState.getInt("sbMax"));
		this.sbHistory.setProgress(savedInstanceState.getInt("sbProgress"));
		this.startTime = savedInstanceState.getLong("startTime");
		this.tiv.bitmapFromCurrent();
	}

	@Override
	public void onResume() {
		super.onResume();
		Util.updateFullScreen(this);
		sql = new SQLitePicogramAdapter(this.getApplicationContext(),
				"Picograms", null, 1);
		this.continueMusic = false;
		MusicManager.start(this);
		// Score stuff. Get the previous score, if the status is "done", make it negative.
		// Else, keep the time going like normal until we're onPause.
		if (this.tiv.gSolution.equals(this.tiv.gCurrent))
		{
			this.score = -1 * sql.getHighscore(this.tiv.gId);
		} else {
			this.startTime = System.currentTimeMillis();
			this.score = sql.getHighscore(this.tiv.gId);
		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("current", this.tiv.gCurrent);
		outState.putChar("drawCharacter", this.tiv.colorCharacter);
		outState.putBoolean("isGame", this.tiv.isGameplay);
		outState.putStringArrayList("history", this.tiv.history);
		outState.putInt("sbMax", this.sbHistory.getMax());
		outState.putInt("sbProgress", this.sbHistory.getProgress());
		outState.putLong("startTime", this.startTime);
	}

	public void onStartTrackingTouch(final SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	public void onStopTrackingTouch(final SeekBar seekBar) {
		// TODO Auto-generated method stub

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

	private void returnIntent(final Dialog d) {
		if (d != null) {
			d.dismiss();
		}
		final Intent returnIntent = new Intent();
		returnIntent.putExtra("current", this.tiv.gCurrent);
		if (this.tiv.gCurrent.equals(this.tiv.gSolution)) {
			returnIntent.putExtra("status", "1");
		} else {
			returnIntent.putExtra("status", "0");
		}
		returnIntent.putExtra("ID", this.tiv.gId);
		returnIntent.putExtra("part", this.part);

		this.setResult(Activity.RESULT_OK, returnIntent);
		try {
			this.unregisterReceiver(this.mBatInfoReceiver);
		} catch (final Exception e)
		{
		}
		this.finish();
	}

	public void win() {
		// Do stuff if we win... Maybe make the gridlines go away and show the whole image?
		this.returnIntent(null);
	}
}
