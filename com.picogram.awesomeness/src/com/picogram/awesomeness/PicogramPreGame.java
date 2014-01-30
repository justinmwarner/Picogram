
package com.picogram.awesomeness;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.Arrays;

public class PicogramPreGame extends FragmentActivity implements OnPageChangeListener, OnTouchListener {
	public class PreGameAdapter extends FragmentPagerAdapter {

		public PreGameFragment frag[] = new PreGameFragment[this.getCount()];

		public PreGameAdapter(final FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return PicogramPreGame.this.TITLES.size();
		}

		@Override
		public Fragment getItem(final int position) {
			final PreGameFragment result = PreGameFragment.newInstance(position);
			result.current = PicogramPreGame.this.puzzle;
			this.frag[position] = result;
			return result;
		}

		@Override
		public CharSequence getPageTitle(final int position) {
			return PicogramPreGame.this.TITLES.get(position);
		}

	}

	final ArrayList<String> TITLES = new ArrayList<String>(Arrays.asList(new String[] {
			"Actions", "Comments", "High Scores"
	}));

	private static final String TAG = "PicogramPreGame";

	private PagerSlidingTabStrip tabs;
	private ViewPager pager;
	private PreGameAdapter adapter;

	String name, solution, current, id;
	int width, height;
	String[] colors;
	Picogram puzzle = new Picogram();
	final int proportion = 10; // For how much we scale the ImageView. =).
	int xGrid = 1, yGrid = 1;

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		final SQLitePicogramAdapter sql = new SQLitePicogramAdapter(this, "Picograms", null, 1);
		if ((resultCode == Activity.RESULT_OK)
				&& (requestCode == MenuActivity.GAME_CODE)) {
			// Back button pushed or won.
			final String id = data.getStringExtra("ID");
			final String status = data.getStringExtra("status");
			final String current = data.getStringExtra("current");
			sql.updateCurrentPicogram(id, status, current);
			this.current = current;
			this.updateAndGetImageView();
			final Picogram updatedPicogram = this.adapter.frag[0].current;
			updatedPicogram.setCurrent(current);
			this.adapter.frag[0].current = updatedPicogram;
		}
		sql.close();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_picogram_pre_game);
		// Get info from bundle.
		this.id = this.getIntent().getExtras().getString("id");
		this.name = this.getIntent().getExtras().getString("name");
		this.current = this.getIntent().getExtras().getString("current");
		this.solution = this.getIntent().getExtras().getString("solution");
		this.width = Integer.parseInt(this.getIntent().getExtras().getString("width"));
		this.height = Integer.parseInt(this.getIntent().getExtras().getString("height"));
		this.colors = this.getIntent().getExtras().getString("colors").split(",");

		this.puzzle.setID(this.id);
		this.puzzle.setName(this.name);
		this.puzzle.setCurrent(this.current);
		this.puzzle.setSolution(this.solution);
		this.puzzle.setWidth(this.getIntent().getExtras().getString("width"));
		this.puzzle.setHeight(this.getIntent().getExtras().getString("height"));
		this.puzzle.setColors(this.getIntent().getExtras().getString("colors"));
		this.puzzle.nullsToValue(this);

		final SQLitePicogramAdapter sql = new SQLitePicogramAdapter(this, "Picograms", null, 1);
		this.puzzle.setHighscore(sql.getHighscore(this.puzzle.getID()));

		this.tabs = (PagerSlidingTabStrip) this.findViewById(R.id.tabsPreGame);
		this.pager = (ViewPager) this.findViewById(R.id.pagerPreGame);
		this.adapter = new PreGameAdapter(this.getSupportFragmentManager());
		this.pager.setAdapter(this.adapter);

		this.tabs.setViewPager(this.pager);
		// continued from above
		this.tabs.setOnPageChangeListener(this);

		// Add listener to the ImageView.
		final ImageView iv = (ImageView) this.findViewById(R.id.ivPreGame);
		iv.setOnTouchListener(this);
		// Draw the ImageView with current.
		this.updateAndGetImageView();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getMenuInflater().inflate(R.menu.picogram_pre_game, menu);
		return true;
	}

	public void onPageScrolled(final int arg0, final float arg1, final int arg2) {
	}

	public void onPageScrollStateChanged(final int arg0) {
	}

	public void onPageSelected(final int currentTab) {
		if (currentTab == this.TITLES.indexOf("Comments"))
		{
			this.adapter.frag[currentTab].loadComments();
		}
		else if (currentTab == this.TITLES.indexOf("High Scores")) {
			this.adapter.frag[currentTab].loadHighScores();
		}
	}

	public boolean onTouch(final View v, final MotionEvent event) {
		v.setOnTouchListener(null);
		if (event.equals(MotionEvent.ACTION_UP) && (v.getId() == R.id.ivPreGame)) {
			if ((this.width <= 25) && (this.height <= 25))
			{
				// Just start it normally.
				this.adapter.frag[0].startGame();
				return true;
			}
			Log.d(TAG, "Showing");
			this.showPartSelector();
			return true;
		}
		else if ((event.getAction() == (MotionEvent.ACTION_UP)) && (v.getId() == R.id.ivPartSelector))
		{
			// Now a part was chosen, so play it.
			return true;
		}
		// If we fail, add the listener again.
		v.setOnTouchListener(this);
		return false;
	}
	protected void showPartSelector() {
		final Dialog dialog = new Dialog(this);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(this.getLayoutInflater().inflate(R.layout.dialog_part_selector
				, null));
		dialog.show();
		final ImageView iv = (ImageView) dialog.findViewById(R.id.ivPartSelector);
		iv.setImageBitmap(this.updateAndGetImageView());
		iv.setOnTouchListener(this);
	}

	protected Bitmap updateAndGetImageView() {
		final ImageView iv = (ImageView) this.findViewById(R.id.ivPreGame);
		final int pix[] = new int[this.width * this.height];
		Bitmap bm = Bitmap.createBitmap(this.width, this.height, Config.ARGB_4444);
		bm.getPixels(pix, 0, this.width, 0, 0, this.width, this.height);
		int run = 0;
		for (int i = 0; i != this.width; ++i)
		{
			for (int j = 0; j != this.height; ++j)
			{
				int col, colNumber;
				if (this.current.charAt(run) != 'x')
				{
					colNumber = Integer.parseInt("" + this.current.charAt(run));
					col = Integer.parseInt(this.colors[colNumber]);

				}
				else {
					col = Integer.parseInt(this.colors[0]);
				}
				pix[run] = col;
				// pix[(i * this.width) + j] = col;
				run++;
			}
		}
		bm.setPixels(pix, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());
		// Draw gridlines
		// Get the number of grids on x/y.
		if (this.width > 25)
		{
			int highest = 0;
			for (int i = 20; i != 26; ++i)
			{
				if ((this.width % i) > highest)
				{
					highest = this.width % i;
					this.xGrid = i;
				}
			}
		}
		if (this.height > 25)
		{
			int highest = 0;
			for (int i = 20; i != 26; ++i)
			{
				if ((this.height % i) > highest)
				{
					highest = this.width % i;
					this.yGrid = i;
				}
			}
		}
		Log.d(TAG, "XG: " + this.xGrid + " YG: " + this.yGrid);
		// Now draw these on a Canvas and save it.
		bm = Bitmap.createScaledBitmap(bm, this.width * this.proportion, this.height * this.proportion, false);
		if ((this.xGrid != 1) && (this.yGrid != 1))
		{
			final Canvas canvas = new Canvas(bm);
			final Paint p = new Paint();
			for (int i = 0; i != this.xGrid; ++i)
			{
				// Drawing up and down.
				p.setColor(Color.BLACK);
				p.setStrokeWidth(5);
				canvas.drawLine((i + 1) * this.proportion * this.xGrid, 0, (i + 1) * this.proportion * this.xGrid, canvas.getHeight(), p);
				p.setColor(Color.GRAY);
				p.setStrokeWidth(2);
				canvas.drawLine((i + 1) * this.proportion * this.xGrid, 0, (i + 1) * this.proportion * this.xGrid, canvas.getHeight(), p);
			}
			for (int i = 0; i != this.yGrid; ++i)
			{
				// Drawing side to side.
				p.setColor(Color.BLACK);
				p.setStrokeWidth(5);
				canvas.drawLine(0, (i + 1) * this.proportion * this.yGrid, canvas.getWidth(), (i + 1) * this.proportion * this.yGrid, p);
				p.setColor(Color.GRAY);
				p.setStrokeWidth(2);
				canvas.drawLine(0, (i + 1) * this.proportion * this.yGrid, canvas.getWidth(), (i + 1) * this.proportion * this.yGrid, p);
			}
		}
		// Check if bm is smaller than 150 height, the default height so it isn't huge.
		if (bm.getHeight() < 150)
		{
			final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, bm.getHeight());
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			iv.setLayoutParams(params);
		}
		iv.setImageBitmap(bm);
		return bm;
	}

}
