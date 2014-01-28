
package com.picogram.awesomeness;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.widget.ImageView;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import java.util.ArrayList;
import java.util.Arrays;

public class PicogramPreGame extends FragmentActivity implements OnPageChangeListener {
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
			"Actions", "Comments", "High Scores", "Sharing"
	}));

	private static final String TAG = "PicogramPreGame";

	private PagerSlidingTabStrip tabs;
	private ViewPager pager;
	private PreGameAdapter adapter;

	String name, solution, current, id;
	int width, height;
	String[] colors;
	Picogram puzzle = new Picogram();

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
			this.updateImageView();
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

		// Draw the ImageView with current.
		this.updateImageView();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getMenuInflater().inflate(R.menu.picogram_pre_game, menu);
		return true;
	}

	public void onPageScrolled(final int arg0, final float arg1, final int arg2) {
		// TODO Auto-generated method stub

	}

	public void onPageScrollStateChanged(final int arg0) {
		// TODO Auto-generated method stub

	}

	public void onPageSelected(final int currentTab) {
		// TODO Auto-generated method stub
		if (currentTab == this.TITLES.indexOf("Comments"))
		{
			this.adapter.frag[currentTab].loadComments();
		}
		else if (currentTab == this.TITLES.indexOf("High Scores")) {
			this.adapter.frag[currentTab].loadHighScores();
		}
	}

	private void updateImageView() {
		final ImageView iv = (ImageView) this.findViewById(R.id.ivPreGame);
		final int pix[] = new int[this.width * this.height];
		final Bitmap bm = Bitmap.createBitmap(this.width, this.height, Config.ARGB_4444);
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
		iv.setImageBitmap(Bitmap.createScaledBitmap(Bitmap
				.createBitmap(pix, this.width, this.height, Bitmap.Config.ARGB_4444),
				this.width * 10,
				this.height * 10, false));
	}

}
