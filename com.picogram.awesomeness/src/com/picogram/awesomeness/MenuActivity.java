
package com.picogram.awesomeness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.flurry.android.FlurryAdListener;
import com.flurry.android.FlurryAdSize;
import com.flurry.android.FlurryAdType;
import com.flurry.android.FlurryAds;
import com.flurry.android.FlurryAgent;
import com.stackmob.android.sdk.common.StackMobAndroid;
import java.util.ArrayList;
import java.util.Arrays;

public class MenuActivity extends FragmentActivity implements FlurryAdListener,
OnPageChangeListener, OnClickListener {
	public class MyPagerAdapter extends FragmentPagerAdapter {

		private static final String TAG = "MainActivity";


		public MyPagerAdapter(final FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return MenuActivity.TITLES.size();
		}

		@Override
		public Fragment getItem(final int position) {
			return SuperAwesomeCardFragment.newInstance(position);
		}

		@Override
		public CharSequence getPageTitle(final int position) {
			return MenuActivity.TITLES.get(position);
		}

		public void setTag(final String tag)
		{
			SuperAwesomeCardFragment.setTag(tag);
		}

	}

	static final ArrayList<String> TITLES = new ArrayList<String>(Arrays.asList(new String[] {
			"My", "Top", "Recent", "Search", "Prefs"
	}));

	protected static final String TAG = "MenuActivity";
	public static String PREFS_FILE = "com.picogram.awesomeness_preferences";
	Handler h = new Handler();
	LinearLayout toolbar;
	SharedPreferences prefs = null;
	private PagerSlidingTabStrip tabs;
	private ViewPager pager;
	private MyPagerAdapter adapter;
	int currentTab = 0;
	Button bSearch;
	EditText etTags;
	static ListView lv;
	static GriddlerListAdapter lvAdapter;

	public static void getRecentPuzzles() {
	}

	public static void getSearchedPuzzles(final String tag) {
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{
		if (requestCode == 100) {
			// Preferences, just switch tab back to main.
			this.pager.setCurrentItem(TITLES.indexOf("My"));
		}
	}

	public void onAdClicked(final String arg0) {
	}

	public void onAdClosed(final String arg0) {
	}

	public void onAdOpened(final String arg0) {
	}

	public void onApplicationExit(final String arg0) {
	}

	public void onClick(final View v) {
		// Should be search.
		if (this.bSearch != null) {
			if (v.getId() == this.bSearch.getId()) {
				// SuperAwesomeCardFragment.getTagPuzzles(this, this.etTags.getText().toString(), true);
			}
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		lv = new ListView(this);
		lvAdapter = new GriddlerListAdapter(this, R.id.tvName);
		lv.setAdapter(lvAdapter);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		Util.setTheme(this);
		this.setContentView(R.layout.activity_menu);
		this.prefs = this.getSharedPreferences(
				MenuActivity.PREFS_FILE, MODE_PRIVATE);
		final String user = this.prefs.getString("username", "N/A");
		StackMobAndroid.init(this.getApplicationContext(), 0,
				"f077e098-c678-4256-b7a2-c3061d9ff0c2");// Change to production.

		FlurryAgent.onStartSession(this, this.getResources().getString(R.string.flurry));
		FlurryAgent.setCaptureUncaughtExceptions(true);
		FlurryAgent.setLogEnabled(!this.prefs.getBoolean("analytics", false));
		FlurryAgent.setLogEvents(!this.prefs.getBoolean("logging", false));

		FlurryAgent.logEvent("App Started");
		this.toolbar = (LinearLayout) this.findViewById(R.id.bottomToolbar);
		// allow us to get callbacks for ad events
		FlurryAds.setAdListener(this);
		FlurryAds.enableTestAds(true);


		this.tabs = (PagerSlidingTabStrip) this.findViewById(R.id.tabs);
		this.pager = (ViewPager) this.findViewById(R.id.pager);
		this.adapter = new MyPagerAdapter(this.getSupportFragmentManager());

		this.pager.setAdapter(this.adapter);

		final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, this
				.getResources()
				.getDisplayMetrics());
		this.pager.setPageMargin(pageMargin);

		this.tabs.setViewPager(this.pager);
		this.tabs.setOnPageChangeListener(this);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	public void onPageScrolled(final int arg0, final float arg1, final int arg2) {
		// TODO Auto-generated method stub
	}

	public void onPageScrollStateChanged(final int arg0) {
		// TODO Auto-generated method stub
	}

	public void onPageSelected(final int tab) {
		// Handle bottom toolbar changes.
		if (tab == TITLES.indexOf("Prefs")) {
			final Intent i = new Intent(this, SettingsActivity.class);
			this.startActivityForResult(i, 100);
		} else if (tab == TITLES.indexOf("Search"))
		{
			if (this.toolbar.getVisibility() == View.GONE) {
				this.toolbar.setVisibility(View.VISIBLE);
			}
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

			final RelativeLayout rl = new RelativeLayout(this);
			// Search, get rid of ad, and replace with search stuff.
			this.bSearch = new Button(this);
			this.bSearch.setText("Search");
			this.bSearch.setOnClickListener(this);
			rl.addView(this.bSearch, params);
			params = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.MATCH_PARENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
			params.addRule(RelativeLayout.LEFT_OF, this.bSearch.getId());
			this.etTags = new EditText(this);
			this.etTags.setHint("Tags...");
			rl.addView(this.etTags, params);
			this.toolbar.removeAllViews();
			this.toolbar.addView(rl);
		}
		else
		{
			if (this.currentTab == TITLES.indexOf("Search")) { // If previous tab was search.
				this.toolbar.removeAllViews();
				this.toolbar
				.setVisibility(!this.prefs.getBoolean("advertisements", false) ? View.VISIBLE
						: View.GONE);
				if (!this.prefs.getBoolean("advertisements", false)) {
					FlurryAds.fetchAd(this, "MainScreen", this.toolbar, FlurryAdSize.BANNER_BOTTOM);
				}
			}
		}
		this.currentTab = tab;
	}

	public void onRenderFailed(final String arg0) {
	}

	@Override
	public void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, this.getResources().getString(R.string.flurry));
		// fetch and prepare ad for this ad space. won’t render one yet
		this.toolbar.setVisibility(!this.prefs.getBoolean("advertisements", false) ? View.VISIBLE
				: View.GONE);
		if (!this.prefs.getBoolean("advertisements", false)) {
			FlurryAds.fetchAd(this, "MainScreen", this.toolbar, FlurryAdSize.BANNER_BOTTOM);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	public void onVideoCompleted(final String arg0) {
	}

	public boolean shouldDisplayAd(final String arg0, final FlurryAdType arg1) {
		return true;
	}

	public void spaceDidFailToReceiveAd(final String arg0) {
	}

	public void spaceDidReceiveAd(final String adSpace) {
		FlurryAds.displayAd(this, adSpace, this.toolbar);
	}
}
