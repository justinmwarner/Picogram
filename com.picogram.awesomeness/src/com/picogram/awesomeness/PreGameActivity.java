
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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.plus.PlusShare;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Arrays;

public class PreGameActivity extends FragmentActivity implements OnPageChangeListener, OnTouchListener {
	public class PreGameAdapter extends FragmentPagerAdapter {

		public PreGameFragment frag[] = new PreGameFragment[this.getCount()];

		public PreGameAdapter(final FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return PreGameActivity.this.TITLES.size();
		}

		@Override
		public Fragment getItem(final int position) {
			final PreGameFragment result = PreGameFragment.newInstance(position);
			result.current = PreGameActivity.this.puzzle;
			this.frag[position] = result;
			return result;
		}

		@Override
		public CharSequence getPageTitle(final int position) {
			return PreGameActivity.this.TITLES.get(position);
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

	boolean hasLoadedComments = false, hasLoadedHighscores = false;

	/**
	 * For fixed "column" height. "Blank cells" will be left, if the two arrays have different "width"
	 */
	char[][] appendArrayHorizontal(final char[][] array1, final char[][] array2) {
		final int a = array1[0].length, b = array2[0].length;

		final char[][] result = new char[Math.max(array1.length,array2.length)][a+b];

		//append the rows, where both arrays have information
		int i;
		for (i = 0; (i < array1.length) && (i < array2.length); i++) {
			if((array1[i].length != a) || (array2[i].length != b)){
				throw new IllegalArgumentException("Column height doesn't match at index: " + i);
			}
			System.arraycopy(array1[i], 0, result[i], 0, a);
			System.arraycopy(array2[i], 0, result[i], a, b);
		}

		//Fill out the rest
		//only one of the following loops will actually run.
		for (; i < array1.length; i++) {
			if(array1[i].length != a){
				throw new IllegalArgumentException("Column height doesn't match at index: " + i);
			}
			System.arraycopy(array1[i], 0, result[i], 0, a);
		}

		for (; i < array2.length; i++) {
			if(array2[i].length != b){
				throw new IllegalArgumentException("Column height doesn't match at index: " + i);
			}
			System.arraycopy(array2[i], 0, result[i], a, b);
		}

		return result;
	}

	char[][] appendArrayVertical(final char[][] array1, final char[][] array2) {
		final char[][] ret = new char[array1.length + array2.length][];
		int i = 0;
		for (; i < array1.length; i++) {
			ret[i] = array1[i];
		}
		for (int j = 0; j < array2.length; j++) {
			ret[i++] = array2[j];
		}
		return ret;
	}
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

			final String id = data.getStringExtra("ID");
			final int part = data.getIntExtra("part", -1);
			if (part != -1)
			{
				final int w = Integer.parseInt(this.puzzle.getWidth());
				final int h = Integer.parseInt(this.puzzle.getHeight());
				String newCurrent = data.getStringExtra("current");
				this.adapter.frag[0].current.setCurrent(this.current);
				final PicogramPart[] singleParts = this.adapter.frag[0].getParts();
				singleParts[part].setCurrent(newCurrent);
				newCurrent = "";
				final PicogramPart[][] parts = new PicogramPart[(int) this.yCellNum][(int) this.xCellNum];
				int run = 0;
				for (int i = 0; i != parts.length; ++i)
				{
					for (int j = 0; j != parts[i].length; ++j)
					{
						parts[i][j] = singleParts[run];
						run++;
					}
				}
				// Reconstruction
				final ArrayList<char[][]> rows = new ArrayList<char[][]>();
				for (int i = 0; i != parts.length; ++i)
				{
					final PicogramPart[] row = new PicogramPart[parts[i].length];
					for (int j = 0; j != parts[i].length; ++j)
					{
						row[j] = parts[i][j];
					}
					char[][] row2D = null;
					for (int j = 1; j != row.length; ++j)
					{
						if (row2D == null)
						{
							Log.d(TAG, row[j - 1].toString());
							Log.d(TAG, row[j].toString());
							row2D = this.appendArrayHorizontal(row[j - 1].get2D(), row[j].get2D());
						} else {
							row2D = this.appendArrayHorizontal(row2D, row[j].get2D());
						}
					}
					rows.add(row2D);
				}
				char[][] full2D = null;
				for (int i = 1; i != rows.size(); ++i)
				{
					if (full2D == null) {
						full2D = this.appendArrayVertical(rows.get(i - 1), rows.get(i));
					} else {
						full2D = this.appendArrayVertical(full2D, rows.get(i));
					}
				}
				for (int i = 0; i != full2D.length; ++i) {
					newCurrent += new String(full2D[i]);
				}
				Log.d(TAG, "NEWCUR " + newCurrent);
				this.current = newCurrent;
			}
			else
			{
				// Back button pushed
				this.current = data.getStringExtra("current");
			}
			// Check if we won:
			if (this.current.replaceAll("x|X", "0").equals(this.solution))
			{
				this.puzzle.setStatus("1");
				// If we won, do the winning stuff. TODO
			}
			// sql.updateCurrentPicogram(id, this.puzzle.getStatus(), this.current);
			this.updateAndGetImageView();
			final Picogram updatedPicogram = this.adapter.frag[0].current;
			updatedPicogram.setCurrent(this.current);
			this.adapter.frag[0].current = updatedPicogram;
			this.showRatingDialog(sql);
		}
		sql.close();
	}
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_picogram_pre_game);
		//Did we get this through a deep link?
		final String deepLinkId = PlusShare.getDeepLinkId(this.getIntent());
		this.parseDeepLinkId(deepLinkId);
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
		this.showRatingDialog(sql);
		sql.close();
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
			if (!this.hasLoadedComments)
			{
				this.adapter.frag[currentTab].loadComments();
				this.hasLoadedComments = true;
			}
		}
		else if (currentTab == this.TITLES.indexOf("High Scores")) {
			if (!this.hasLoadedHighscores)
			{
				this.adapter.frag[currentTab].loadHighScores();
				this.hasLoadedHighscores = true;
			}
		}
	}
	public boolean onTouch(final View v, final MotionEvent event) {
		v.setOnTouchListener(null);
		/*
		 * Log.d(TAG, "OnTouch: " + event.getAction() + " " + v.getId() + " " + R.id.ivPartSelector);
		 * if (event.equals(MotionEvent.ACTION_DOWN) && (v.getId() == R.id.ivPreGame)) {
		 * if ((this.width <= 25) && (this.height <= 25))
		 * {
		 * // Just start it normally.
		 * this.adapter.frag[0].startGame();
		 * return true;
		 * }
		 * Log.d(TAG, "OnTouch Showing");
		 * this.showPartSelector();
		 * return true;
		 * }
		 * else if ((event.getAction() == (MotionEvent.ACTION_DOWN)) && (v.getId() == R.id.ivPartSelector))
		 * {
		 * // Get row and column tapped.
		 * 
		 * final double alteredCellWidth = (v.getWidth()) / this.xCellNum;
		 * final double alteredCellHeight = (v.getHeight()) / this.yCellNum;
		 * 
		 * final int row = (int) Math.ceil(event.getY() / alteredCellHeight);
		 * final int col = (int) Math.ceil(event.getX() / alteredCellWidth);
		 * 
		 * // Now a part was chosen, so play it.
		 * final char[][] current2D = this.getLineIn2D(this.current);
		 * final char[][] solution2D = this.getLineIn2D(this.solution);
		 * String newGame = "", newSolution = "";
		 * int highWidth = 0, highHeight = 0, lowHeight = Integer.MAX_VALUE, lowWidth = Integer.MAX_VALUE;
		 * for(int i = 0; i != current2D.length; ++i)
		 * {
		 * for(int j = 0; j != current2D[i].length; ++j)
		 * {
		 * if ((j < (this.cellWidth * row)) && (j >= (this.cellWidth * (row - 1)))) // Height
		 * {
		 * if ((i < (this.cellHeight * col)) && (i >= (this.cellHeight * (col - 1)))) // Width
		 * {
		 * newGame += current2D[i][j];
		 * newSolution += solution2D[i][j];
		 * if (highHeight < i) {
		 * highHeight = i;
		 * }
		 * if (lowHeight > i) {
		 * lowHeight = i;
		 * }
		 * if (highWidth < j) {
		 * highWidth = j;
		 * }
		 * if (lowWidth > j) {
		 * lowWidth = j;
		 * }
		 * }
		 * }
		 * }
		 * }
		 * final int newWidth = (highWidth - lowWidth) + 1, newHeight = (highHeight - lowHeight) + 1;
		 * Log.d(TAG, "NW: " + newWidth + " NH: " + newHeight + " HW: " + highWidth + " HH: " + highHeight + " LW: " + lowWidth + " LH: " + lowHeight + " LEN: " + newGame.length());
		 * final Picogram puzzle = this.puzzle;
		 * puzzle.setSolution(newSolution);
		 * puzzle.setCurrent(newGame);
		 * puzzle.setWidth(highWidth + "");
		 * puzzle.setHeight(highHeight + "");
		 * this.startGame(puzzle, row, col, newWidth, newHeight);
		 * return true;
		 * }
		 * // If we fail, add the listener again.
		 * v.setOnTouchListener(this);
		 */
		return false;
	}

	private void parseDeepLinkId(final String deepLinkId) {
		final Intent route = new Intent();
		if(deepLinkId != null) {
			Log.d(TAG, "TEST " + deepLinkId);
		}
		//if ("/pages/create".equals(deepLinkId)) {
		//    route.setClass(getApplicationContext(), CreatePageActivity.class);
		//} else {
		// Fallback to the MainActivity in your app.
		//    route.setClass(getApplicationContext(), MainActivity.class);
		//}
		//return route;
	}

	public void showRatingDialog( final SQLitePicogramAdapter sql)
	{
		// Check if user needs to rate puzzle.
		if (Util.isOnline() && this.puzzle.getCurrent().replaceAll("x|X", "0").equals(this.puzzle.getSolution()) && (sql.getValueByColumn(this.puzzle.getID(), SQLitePicogramAdapter.pRank) == 0))
		{
			// Prompt.
			final Activity a = this;
			final Dialog dialog = new Dialog(this);
			dialog.setTitle("Rate " + this.puzzle.getName());
			dialog.setContentView(R.layout.dialog_ranking);
			final View.OnClickListener ocl = new OnClickListener() {

				public void onClick(final View v) {
					new Thread(new Runnable() {

						public void run() {
							final ParseQuery<ParseObject> query = ParseQuery.getQuery("Picogram");
							query.whereEqualTo("puzzleId", PreGameActivity.this.puzzle.getID());
							try {
								final ParseObject po = query.getFirst();
								if (v.getId() == R.id.bHappy) {
									po.increment("rate", 1);
									sql.addPersonalRank(PreGameActivity.this.puzzle.getID(), 1);
								} else if (v.getId() == R.id.bSad) {
									po.increment("rate", -1);
									sql.addPersonalRank(PreGameActivity.this.puzzle.getID(), -1);
								}
								po.saveEventually();
							} catch (final ParseException e) {
								e.printStackTrace();
							}
							a.runOnUiThread(new Runnable() {

								public void run() {
									sql.close();
									dialog.hide();
								}

							});
						}
					}).start();
				}
			};
			final Button bHappy = (Button) dialog.findViewById(R.id.bHappy);
			bHappy.setOnClickListener(ocl);
			final Button bSad = (Button) dialog.findViewById(R.id.bSad);
			bSad.setOnClickListener(ocl);
			dialog.show();
		}
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
