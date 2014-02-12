
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
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.flurry.android.FlurryAgent;

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
	int cellWidth = 1, cellHeight = 1;
	double xCellNum = 1, yCellNum = 1;

	private char[][] getLineIn2D(final String line) {
		final char[][] current2D = new char[this.height][this.width];
		int run = 0;
		for (int i = 0; i != current2D.length; ++i) {
			for (int j = 0; j != current2D[i].length; ++j) {
				current2D[i][j] = line.charAt(run);
				run++;
			}
		}
		return current2D;
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		final SQLitePicogramAdapter sql = new SQLitePicogramAdapter(this, "Picograms", null, 1);
		if ((resultCode == Activity.RESULT_OK)
				&& (requestCode == MenuActivity.GAME_CODE)) {

			Log.d(TAG, "Cur: " + this.current);
			final String id = data.getStringExtra("ID");
			final Picogram puzzle = sql.getPicogram(id);
			Log.d(TAG, "ID: " + id);
			Log.d(TAG, "Puzzle: " + puzzle);
			this.current = puzzle.getCurrent();
			if ((data.getIntExtra("row", 0) != 0) || (data.getIntExtra("column", 0) != 0))
			{
				final int row = data.getIntExtra("row", 0);
				final int column = data.getIntExtra("col", 0);
				final String newCurrent = data.getStringExtra("current");
				// If we have a row/column, then we want to alter that spot in the current.
				// Make it 2D
				final char[][] current2D = this.getLineIn2D(this.current); // Gets 2d current.
				int run = 0;
				for (int i = 0; i != current2D.length; ++i)
				{
					for (int j = 0; j != current2D[i].length; ++j)
					{
						if ((j < (this.cellWidth * (row))) && (j >= (this.cellWidth * (row - 1))))
						{
							if ((i < (this.cellHeight * column)) && (i >= (this.cellHeight * (column - 1))))
							{
								current2D[i][j] = newCurrent.charAt(run);
								run++;
							}
						}
					}
				}
				this.current = "";
				run = 0;
				//Now rebuild current.
				for(int i = 0; i !=  current2D.length; ++i)
				{
					for(int j = 0;j != current2D[i].length; ++j)
					{
						this.current += current2D[i][j];
						run++;
					}
				}
			}
			else
			{
				// Back button pushed
				this.current = data.getStringExtra("current");
			}
			// Check if we won:
			if (this.current.replaceAll("x|X", "0").equals(this.solution))
			{
				puzzle.setStatus("1");
				// If we won, do the winning stuff. TODO
			}
			puzzle.setCurrent(this.current);
			Log.d(TAG, "New Current ( " + this.current.length() + ") : " + this.current);

			// sql.updateCurrentPicogram(id, status, current);
			// this.current = current;
			// this.updateAndGetImageView();
			// final Picogram updatedPicogram = this.adapter.frag[0].current;
			// updatedPicogram.setCurrent(current);
			// this.adapter.frag[0].current = updatedPicogram;
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
		Log.d(TAG, "OnTouch: " + event.getAction() + " " + v.getId() + " " + R.id.ivPartSelector);
		if (event.equals(MotionEvent.ACTION_DOWN) && (v.getId() == R.id.ivPreGame)) {
			if ((this.width <= 25) && (this.height <= 25))
			{
				// Just start it normally.
				this.adapter.frag[0].startGame();
				return true;
			}
			Log.d(TAG, "OnTouch Showing");
			this.showPartSelector();
			return true;
		}
		else if ((event.getAction() == (MotionEvent.ACTION_DOWN)) && (v.getId() == R.id.ivPartSelector))
		{
			// Get row and column tapped.

			final double alteredCellWidth = (v.getWidth()) / this.xCellNum;
			final double alteredCellHeight = (v.getHeight()) / this.yCellNum;

			final int row = (int) Math.ceil(event.getY() / alteredCellHeight);
			final int col = (int) Math.ceil(event.getX() / alteredCellWidth);

			// Now a part was chosen, so play it.
			final char[][] current2D = this.getLineIn2D(this.current);
			final char[][] solution2D = this.getLineIn2D(this.solution);
			String newGame = "", newSolution = "";
			int highWidth = 0, highHeight = 0, lowHeight = Integer.MAX_VALUE, lowWidth = Integer.MAX_VALUE;
			for(int i = 0; i != current2D.length; ++i)
			{
				for(int j = 0; j != current2D[i].length; ++j)
				{
					if ((j < (this.cellWidth * row)) && (j >= (this.cellWidth * (row - 1)))) // Height
					{
						if ((i < (this.cellHeight * col)) && (i >= (this.cellHeight * (col - 1)))) // Width
						{
							newGame += current2D[i][j];
							newSolution += solution2D[i][j];
							if (highHeight < i) {
								highHeight = i;
							}
							if (lowHeight > i) {
								lowHeight = i;
							}
							if (highWidth < j) {
								highWidth = j;
							}
							if (lowWidth > j) {
								lowWidth = j;
							}
						}
					}
				}
			}
			final int newWidth = (highWidth - lowWidth) + 1, newHeight = (highHeight - lowHeight) + 1;
			Log.d(TAG, "NW: " + newWidth + " NH: " + newHeight + " HW: " + highWidth + " HH: " + highHeight + " LW: " + lowWidth + " LH: " + lowHeight + " LEN: " + newGame.length());
			final Picogram puzzle = this.puzzle;
			puzzle.setSolution(newSolution);
			puzzle.setCurrent(newGame);
			puzzle.setWidth(highWidth + "");
			puzzle.setHeight(highHeight + "");
			this.startGame(puzzle, row, col, newWidth, newHeight);
			return true;
		}
		// If we fail, add the listener again.
		v.setOnTouchListener(this);
		return false;
	}

	protected void showPartSelector() {
		final Dialog dialog = new Dialog(this);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(this.getLayoutInflater().inflate(R.layout.dialog_part_selector, null));
		dialog.show();
		final ImageView iv = (ImageView) dialog.findViewById(R.id.ivPartSelector);
		iv.setImageBitmap(this.updateAndGetImageView());
		iv.setBackgroundResource(R.drawable.light_grid);
		iv.setOnTouchListener(this);
	}


	protected void startGame(final Picogram go, final int row, final int col, final int cw, final int ch) {
		FlurryAgent.logEvent("UserPlayGame");
		final Intent gameIntent = new Intent(this, AdvancedGameActivity.class);
		gameIntent.putExtra("name", go.getName());
		gameIntent.putExtra("solution", go.getSolution());
		gameIntent.putExtra("current", go.getCurrent());
		gameIntent.putExtra("width", cw + ""); // Use cell values if we start here.
		gameIntent.putExtra("height", ch + ""); // Use cell values if we start here.
		gameIntent.putExtra("id", go.getID());
		gameIntent.putExtra("status", go.getStatus());
		gameIntent.putExtra("colors", go.getColors());
		gameIntent.putExtra("row", row);
		gameIntent.putExtra("column", col);
		this.startActivityForResult(gameIntent, MenuActivity.GAME_CODE);
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
				if ((this.width % i) == 0)
				{
					this.cellWidth = i;
					break;
				}
				if ((this.width % i) > highest)
				{
					highest = this.width % i;
					this.cellWidth = i;
				}
			}
		}
		if (this.height > 25)
		{
			int highest = 0;
			for (int i = 20; i != 26; ++i)
			{
				if ((this.height % i) == 0)
				{
					this.cellHeight = i;
					break;
				}
				if ((this.height % i) > highest)
				{
					highest = this.width % i;
					this.cellHeight = i;
				}
			}
		}
		this.xCellNum = Math.ceil((double) bm.getWidth() / (double) this.cellWidth);
		this.yCellNum = Math.ceil((double) bm.getHeight() / (double) this.cellHeight);

		Log.d(TAG, "XG: " + this.cellWidth + " YG: " + this.cellHeight);
		// Now draw these on a Canvas and save it.
		bm = Bitmap.createScaledBitmap(bm, this.width * this.proportion, this.height * this.proportion, false);
		if ((this.cellWidth != 1) && (this.cellHeight != 1))
		{
			final Canvas canvas = new Canvas(bm);
			final Paint p = new Paint();
			for (int i = 0; i != this.cellWidth; ++i)
			{
				// Drawing up and down.
				p.setColor(Color.BLACK);
				p.setStrokeWidth(5);
				canvas.drawLine((i + 1) * this.proportion * this.cellWidth, 0, (i + 1) * this.proportion * this.cellWidth, canvas.getHeight(), p);
				p.setColor(Color.GRAY);
				p.setStrokeWidth(2);
				canvas.drawLine((i + 1) * this.proportion * this.cellWidth, 0, (i + 1) * this.proportion * this.cellWidth, canvas.getHeight(), p);
			}
			for (int i = 0; i != this.cellHeight; ++i)
			{
				// Drawing side to side.
				p.setColor(Color.BLACK);
				p.setStrokeWidth(5);
				canvas.drawLine(0, (i + 1) * this.proportion * this.cellHeight, canvas.getWidth(), (i + 1) * this.proportion * this.cellHeight, p);
				p.setColor(Color.GRAY);
				p.setStrokeWidth(2);
				canvas.drawLine(0, (i + 1) * this.proportion * this.cellHeight, canvas.getWidth(), (i + 1) * this.proportion * this.cellHeight, p);
			}
		}
		// Check if bm is smaller than 150 height, the default height so it isn't huge.
		if (bm.getHeight() < 150)
		{
			final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, bm.getHeight());
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			iv.setLayoutParams(params);
		}
		iv.setImageBitmap(bm);
		return bm;
	}

}
