
package com.picogram.awesomeness;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.Arrays;

public class MultiStepCreateActivity extends SherlockFragmentActivity implements ActionBar.OnNavigationListener, OnClickListener {

	public class MyPagerAdapter extends FragmentPagerAdapter {

		private static final String TAG = "MultiStepCreateActivity";

		public MyPagerAdapter(final FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return MultiStepCreateActivity.this.TITLES.size();
		}

		@Override
		public Fragment getItem(final int position) {
			Log.d(TAG, "GetItem: " + position);
			return SuperAwesomeCardFragment.newInstance(position);
		}

		@Override
		public CharSequence getPageTitle(final int position) {
			return MultiStepCreateActivity.this.TITLES.get(position);
		}

	}

	final ArrayList<String> TITLES = new ArrayList<String>(Arrays.asList(new String[] {
			"Picture", "Game", "Colors", "Fine Tune", "Name", "Submit"
	}));

	private PagerSlidingTabStrip tabs;
	private ViewPager pager;
	private MyPagerAdapter adapter;

	public void onClick(final View v) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_menu);// We use the same exact layout as the main menu. =)

		this.tabs = (PagerSlidingTabStrip) this.findViewById(R.id.tabs);
		this.pager = (ViewPager) this.findViewById(R.id.pager);
		this.adapter = new MyPagerAdapter(this.getSupportFragmentManager());

		this.pager.setAdapter(this.adapter);

		final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, this
				.getResources()
				.getDisplayMetrics());
		this.pager.setPageMargin(pageMargin);

		this.tabs.setViewPager(this.pager);
		final ActionBar actionBar = this.getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		this.updateBottomBar();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final ActionBar ab = this.getSupportActionBar();
		final SpinnerAdapter mSpinnerAdapter;
		final ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(this, R.array.listCreatePicture,
				R.layout.sherlock_spinner_item);

		if (list != null) {
			ab.setDisplayShowTitleEnabled(false);
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
			ab.setListNavigationCallbacks(list, this);
		}

		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowTitleEnabled(false);
		ab.setDisplayUseLogoEnabled(false);
		this.invalidateOptionsMenu();
		return true;
	}

	public boolean onNavigationItemSelected(final int itemPosition, final long itemId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		if (item.getItemId() == android.R.id.home)
		{
			if (this.pager.getCurrentItem() == this.TITLES.indexOf("Picture"))
			{
				this.finish();
			}
			else {
				this.pager.setCurrentItem(this.TITLES.indexOf("Picture"));
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private void updateBottomBar() {
		final LinearLayout bottom = (LinearLayout) this.findViewById(R.id.bottomToolbar);
		bottom.setVisibility(View.VISIBLE);
		bottom.removeAllViewsInLayout();
		if (this.pager.getCurrentItem() == this.TITLES.indexOf("Picture"))
		{
			bottom.setVisibility(View.INVISIBLE);
		} else if (this.pager.getCurrentItem() == this.TITLES.indexOf("Game"))
		{
			Button button = new Button(this);
			button.setText("Width");
			button.setOnClickListener(this);
			bottom.addView(button);
			button = new Button(this);
			button.setText("Height");
			button.setOnClickListener(this);
			bottom.addView(button);
			button = new Button(this);
			button.setText("Colors");
			button.setOnClickListener(this);
			bottom.addView(button);
		} else if (this.pager.getCurrentItem() == this.TITLES.indexOf("Colors"))
		{

		} else if (this.pager.getCurrentItem() == this.TITLES.indexOf("Fine Tune"))
		{

		} else if (this.pager.getCurrentItem() == this.TITLES.indexOf("Name"))
		{

		} else if (this.pager.getCurrentItem() == this.TITLES.indexOf("Submit"))
		{

		}
		bottom.invalidate();
	}
}
