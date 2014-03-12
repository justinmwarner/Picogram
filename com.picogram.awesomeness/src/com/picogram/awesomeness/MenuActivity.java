
package com.picogram.awesomeness;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.crittercism.app.Crittercism;
import com.flurry.android.FlurryAdListener;
import com.flurry.android.FlurryAdSize;
import com.flurry.android.FlurryAdType;
import com.flurry.android.FlurryAds;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.Player;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.kopfgeldjaeger.ratememaybe.RateMeMaybe;
import com.kopfgeldjaeger.ratememaybe.RateMeMaybe.OnRMMUserChoiceListener;
import com.parse.Parse;
import com.picogram.awesomeness.DialogMaker.OnDialogResultListener;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.util.ArrayList;
import java.util.Arrays;

public class MenuActivity extends BaseGameActivity implements
FlurryAdListener, OnPageChangeListener, OnClickListener, OnRMMUserChoiceListener, ActionBar.OnNavigationListener, ConnectionCallbacks, OnConnectionFailedListener {
	public class MyPagerAdapter extends FragmentPagerAdapter {

		public MenuFragment frag[] = new MenuFragment[this.getCount()];

		public MyPagerAdapter(final FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return MenuActivity.TITLES.size();
		}

		@Override
		public Fragment getItem(final int position) {
			this.frag[position] = MenuFragment
					.newInstance(position);
			return this.frag[position];
		}

		@Override
		public CharSequence getPageTitle(final int position) {
			return MenuActivity.TITLES.get(position);
		}

	}

	private static final String TAG = "MainActivity";

	static final ArrayList<String> TITLES = new ArrayList<String>(
			Arrays.asList(new String[] {
					"My", "Packs", "Top", "Recent",
					"Search", "Prefs"
			}));

	public static final int CREATE_CODE = 8008;
	public static final int GAME_CODE = 1337;
	public static final int PREFERENCES_CODE = 69;
	public static String PREFS_FILE = "com.picogram.awesomeness_preferences";

	Handler h = new Handler();
	LinearLayout toolbar;
	SharedPreferences prefs = null;
	private PagerSlidingTabStrip tabs;
	private ViewPager pager;
	private MyPagerAdapter adapter;
	int currentTab = 0;
	static ListView lv;
	static PicogramListAdapter lvAdapter;
	SQLitePicogramAdapter sql = null;
	boolean continueMusic = true;
	GamesClient client;
	Dialog dialog;

	public void handleNegative() {
		// Don't do it again.
		Crouton.makeText(this,
				"If you ever want to rate, go to the preferences.", Style.INFO)
				.show();
	}

	public void handleNeutral() {
		// Remind again later on.
		Crouton.makeText(this, "We'll remind you later on.", Style.INFO).show();
	}

	public void handlePositive() {
		// Goto app store.
		// TODO fix this when we publish.
		this.startActivity(new Intent(Intent.ACTION_VIEW,
				Uri.parse("market://details?id=Picogram")));
		this.prefs.edit().putBoolean("apprate", true).commit();
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		if (this.sql == null) {
			this.sql = new SQLitePicogramAdapter(this, "Picograms", null, 1);
		}
		if ((requestCode == PREFERENCES_CODE)) {
			// Preferences, just switch tab back to main.
			this.pager.setCurrentItem(TITLES.indexOf("My"));
		} else if ((resultCode == Activity.RESULT_OK)
				&& (requestCode == CREATE_CODE)) {
			// New Girddler, add to database.
			final String colors = data.getStringExtra("colors");
			final String id = data.getStringExtra("solution").hashCode() + "";
			final String status = "0";
			final String author = data.getStringExtra("author");
			final String difficulty = data.getStringExtra("difficulty");
			final String height = data.getStringExtra("height");
			final String name = data.getStringExtra("name");
			final int numberOfColors = colors.split(",").length;
			final String rank = data.getStringExtra("rank");
			final String solution = data.getStringExtra("solution");
			final String width = data.getStringExtra("width");
			final Picogram g = new Picogram(id, status, name, difficulty,
					rank, 1, author, width, height, solution, null,
					numberOfColors, colors);
			this.sql.addUserPicogram(g);
			g.setID(id);
			// TODO Check if Picogram already exists. If it does, just add that
			// to the users sql database.
			g.save();
			// Add this Picogram to the rating table as a 5.
			final SQLiteRatingAdapter sra = new SQLiteRatingAdapter(this, "Rating",
					null, 2);
			sra.insertCreate(id);
			sra.close();
			// Update current tab.
			this.currentTab = 0;
		} else if ((resultCode == Activity.RESULT_OK)
				&& (requestCode == GAME_CODE)) {
			// Back button pushed or won.
			final String id = data.getStringExtra("ID");
			final String status = data.getStringExtra("status");
			final String current = data.getStringExtra("current");
			this.sql.updateCurrentPicogram(id, status, current);
			MenuActivity.lvAdapter.updateCurrentById(id, current, status);
			MenuActivity.lvAdapter.notifyDataSetChanged();
		}
		this.sql.close();
		this.updateCurrentTab();
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
	}

	public void onConnected(final Bundle arg0) {
		// TODO Auto-generated method stub

	}

	public void onConnectionFailed(final ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Parse.initialize(this, "3j445kDaxQ3lelflRVMetszjtpaXo2S1mjMZYNcW", "zaorBzbtWhdwMdJ0sIgBJjYvowpueuCzstLTwq1A");
		this.prefs = this.getSharedPreferences(MenuActivity.PREFS_FILE,
				MODE_PRIVATE);
		if (!this.prefs.getBoolean("crashes", false)) {
			Crittercism.initialize(this.getApplicationContext(),
					"52b6411c4002051525000002");
		}
		lv = new ListView(this);
		lv.setDivider(this.getResources().getDrawable(R.drawable.one));
		// lv.setDividerHeight(100);
		lvAdapter = new PicogramListAdapter(this, R.id.tvName);
		lv.setAdapter(lvAdapter);
		Util.setTheme(this);
		this.setContentView(R.layout.activity_menu);
		this.tabs = (PagerSlidingTabStrip) this.findViewById(R.id.tabs);
		this.pager = (ViewPager) this.findViewById(R.id.pager);
		this.adapter = new MyPagerAdapter(this.getSupportFragmentManager());
		this.pager.setAdapter(this.adapter);
		final int pageMargin = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 4, this.getResources()
				.getDisplayMetrics());
		this.pager.setPageMargin(pageMargin);

		this.tabs.setViewPager(this.pager);
		this.tabs.setOnPageChangeListener(this);
		this.toolbar = (LinearLayout) this.findViewById(R.id.bottomToolbar);
		if (!Debug.isDebuggerConnected()) {
			this.setUpAds();
		}
		this.setUpRater();
		if (!Util.isOnline()) {
			// Remove ads bar if offline.
			this.toolbar.setVisibility(View.GONE);
		}
		this.updateActionBar(0);

		// Google Sign-in stuff.
		if (!this.isSignedIn())
		{
			// Ask user to login if they haven't before.
			this.showSignInDialog();
		}

		// Show beta.
		this.showBetaDialog();

		this.updateCurrentTab(); // Update current tab ;).
	}
	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		if (this.currentTab == TITLES.indexOf("Search"))
		{

			this.getSupportMenuInflater().inflate(R.menu.activity_menu, menu);

			final SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);

			final SearchView abSearch = (SearchView) menu.findItem(R.id.menu_search).getActionView();
			if (null != abSearch)
			{
				abSearch.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));
				abSearch.setIconifiedByDefault(true);
			}

			final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener()
			{
				public boolean onQueryTextChange(final String newText)
				{
					// this is your adapter that will be filtered
					return true;
				}

				public boolean onQueryTextSubmit(final String query)
				{
					// this is your adapter that will be filtered
					Log.d(TAG, "ACTIONBAR SEARCH " + query);
					MenuActivity.this.adapter.frag[MenuActivity.this.currentTab].getTagPuzzles(
							MenuActivity.this.adapter.frag[MenuActivity.this.currentTab].getActivity(),
							query);
					return true;
				}
			};
			abSearch.setOnQueryTextListener(queryTextListener);

		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (this.sql != null) {
			this.sql.close();
		}
	}

	public void onDisconnected() {
		// TODO Auto-generated method stub
	}

	public boolean onNavigationItemSelected(final int itemPosition, final long itemId) {
		if (this.currentTab == TITLES.indexOf("My")) {
			Util.getPreferences(this).edit().putInt("mySetting", itemPosition).commit();
		} else if (this.currentTab == TITLES.indexOf("Packs")) {
			Util.getPreferences(this).edit().putInt("packsSetting", itemPosition).commit();
		} else if (this.currentTab == TITLES.indexOf("Top")) {
			Util.getPreferences(this).edit().putLong("lastTopUpdate", 0).commit();
			Util.getPreferences(this).edit().putInt("topSetting", itemPosition).commit();
		} else if (this.currentTab == TITLES.indexOf("Recent")) {
			Util.getPreferences(this).edit().putLong("lastRecentUpdate", 0).commit();
			Util.getPreferences(this).edit().putInt("recentSetting", itemPosition).commit();
		} else if (this.currentTab == TITLES.indexOf("Search")) {
			Util.getPreferences(this).edit().putInt("searchSetting", itemPosition).commit();
		}
		this.updateCurrentTab();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == android.R.id.home)
		{
			this.pager.setCurrentItem(TITLES.indexOf("My"));
			return true;
		}
		return false;
	}
	public void onPageScrolled(final int arg0, final float arg1, final int arg2) {
		// TODO Auto-generated method stub
	}

	public void onPageScrollStateChanged(final int arg0) {
		// TODO Auto-generated method stub
	}

	public void onPageSelected(final int tab) {// Handle bottom toolbar changes.
		if (tab == TITLES.indexOf("Prefs")) {
			final Intent i = new Intent(this, SettingsActivity.class);
			this.startActivityForResult(i, PREFERENCES_CODE);
		}
		this.currentTab = tab;
		this.updateCurrentTab();
		if (tab != TITLES.indexOf("Search")) {// Hide keyboard
			final InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(this.findViewById(android.R.id.content)
					.getWindowToken(), 0);
		}
		this.updateActionBar(tab);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (!this.continueMusic) {
			MusicManager.pause();
		}
	}

	public void onRenderFailed(final String arg0) {
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.continueMusic = false;
		MusicManager.start(this);

		if (Util.getPreferences(this).getBoolean("isFirst", true)) {
			final Bundle bundle = new Bundle();
			final FragmentTransaction ft = this.getSupportFragmentManager()
					.beginTransaction();
			bundle.putInt("layoutId", R.layout.dialog_tutorial);
			final DialogMaker newFragment = new DialogMaker();
			newFragment.setArguments(bundle);
			newFragment.setOnDialogResultListner(new OnDialogResultListener() {

				public void onDialogResult(final Bundle result) {
					// No results needed.
				}
			});
			newFragment.show(ft, "dialog");
		}
	}

	public void onSignInFailed() {
		Crouton.makeText(this, "Sign in failed. Try again next time.", Style.ALERT).show();
		this.dialog.show();
	}

	public void onSignInSucceeded() {
		final Player p = this.getGamesClient().getCurrentPlayer();
		this.dialog.hide();
		Log.d(TAG, p.getDisplayName() + " " + p.toString());
		// Add to preferences that user has logged in successfully.
		Util.getPreferences(this).edit().putBoolean("hasLoggedInSuccessfully", true).commit();
	}

	protected void onSkipSignIn() {
		Crouton.makeText(this, "We require sign in for various activities.", Style.INFO).show();
		Util.getPreferences(this).edit().putBoolean("hasLoggedInSuccessfully", false).commit();
		this.showSignInDialog();
	}

	@Override
	public void onStart() {
		super.onStart();
		Util.updateFullScreen(this);
		if (!Debug.isDebuggerConnected()) {
			FlurryAgent.onStartSession(this,
					this.getResources().getString(R.string.flurry));
		}
		// fetch and prepare ad for this ad space. won�t render one yet
		this.toolbar.setVisibility(!this.prefs.getBoolean("advertisements",
				false) ? View.VISIBLE : View.GONE);
		if (!this.prefs.getBoolean("advertisements", false)) {
			if (!Debug.isDebuggerConnected()) {
				FlurryAds.fetchAd(this, "MainScreen", this.toolbar,
						FlurryAdSize.BANNER_BOTTOM);
			}
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (!Debug.isDebuggerConnected()) {
			FlurryAgent.onEndSession(this);
		}
	}

	public void onVideoCompleted(final String arg0) {
	}

	private void setUpAds() {
		FlurryAgent.onStartSession(this,
				this.getResources().getString(R.string.flurry));
		FlurryAgent.setCaptureUncaughtExceptions(true);
		FlurryAgent.setLogEnabled(!this.prefs.getBoolean("analytics", false));
		FlurryAgent.setLogEvents(!this.prefs.getBoolean("logging", false));

		FlurryAgent.logEvent("App Started");
		// allow us to get callbacks for ad events
		FlurryAds.setAdListener(this);
		FlurryAds.enableTestAds(true);

	}

	private void setUpRater() {
		// Rate me Maybe
		final RateMeMaybe rmm = new RateMeMaybe(this);
		rmm.setPromptMinimums(10, 1, 3, 7);
		rmm.setRunWithoutPlayStore(false);
		rmm.setDialogMessage("You really seem to like this app, "
				+ "since you have already used it %totalLaunchCount% times! "
				+ "It would be great if you took a moment to rate it.");
		rmm.setDialogTitle("Rate Picogram!");
		rmm.run();
	}
	public boolean shouldDisplayAd(final String arg0, final FlurryAdType arg1) {
		return true;
	}
	private void showBetaDialog() {
		final Activity a = this;
		AlertDialog dialog;
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Hello.\n\nThis is a timed message. Please read while you wait.\n\nThis app is currently in beta.  We thank you for downloading and using this app." +
				"  Currently, we're collecting as much information on crashes and bugs, so please feel free to email us.  " +
				"If you have any features you'd like to see, we're activly developing it.  So please let us know as well.  " +
				"We hope you enjoy.\n\nThanks!\nJustin Warner\nwarner.73@wright.edu")
				.setPositiveButton("Okay",new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						Util.getPreferences(a).edit().putBoolean("hasSeenBetaDialog", true).commit();
						dialog.cancel();
					}
				});
		// Create the AlertDialog object and return it
		dialog = builder.create();
		final boolean isLoggedInBefore = Util.getPreferences(this).getBoolean("hasSeenBetaDialog", false);
		if (!isLoggedInBefore) {
			// Only show if we've never logged in successfully yet.
			dialog.setCancelable(false);
			dialog.show();
			final Button b = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
			b.setEnabled(false);
			Runnable mRunnable;
			final Handler mHandler = new Handler();

			mRunnable = new Runnable() {

				public void run() {
					// TODO Auto-generated method stub
					b.setEnabled(true);
				}
			};
			mHandler.removeCallbacks(mRunnable);
			mHandler.postDelayed(mRunnable, 10000);
		}
	}

	private void showSignInDialog() {
		this.dialog = new Dialog(this);
		this.dialog.setContentView(R.layout.dialog_login);
		// set the custom dialog components - text, image and button
		this.dialog.findViewById(R.id.bSignIn).setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				MenuActivity.this.beginUserInitiatedSignIn();
				MenuActivity.this.dialog.hide();
			}

		});
		this.dialog.findViewById(R.id.bSkipSignIn).setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				MenuActivity.this.onSkipSignIn();
				MenuActivity.this.dialog.hide();
			}

		});
		final boolean isLoggedInBefore = Util.getPreferences(this).getBoolean("hasLoggedInSuccessfully", false);
		if (!isLoggedInBefore) {
			// Only show if we've never logged in successfully yet.
			this.dialog.show();
		}
	}

	public void spaceDidFailToReceiveAd(final String arg0) {
	}

	public void spaceDidReceiveAd(final String adSpace) {
		FlurryAds.displayAd(this, adSpace, this.toolbar);
	}

	private void updateActionBar(final int tab) {
		// Drop down spinner update.
		this.invalidateOptionsMenu();
		final ActionBar ab = this.getSupportActionBar();
		if (ab == null) {
			return;
		}
		ab.show();
		ArrayAdapter<CharSequence> list = null;
		if (tab == TITLES.indexOf("My")) {
			list = ArrayAdapter.createFromResource(this, R.array.listMy,
					R.layout.sherlock_spinner_item);
		} else if (tab == TITLES.indexOf("Packs")) {
			list = ArrayAdapter.createFromResource(this, R.array.listPacks,
					R.layout.sherlock_spinner_item);
		} else if (tab == TITLES.indexOf("Top")) {
			list = ArrayAdapter.createFromResource(this, R.array.listTop,
					R.layout.sherlock_spinner_item);
		} else if (tab == TITLES.indexOf("Recent")) {
			// Recent always returns the most recent. It's nothing special.
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		} else if (tab == TITLES.indexOf("Search")) {
			list = ArrayAdapter.createFromResource(this, R.array.listSearch,
					R.layout.sherlock_spinner_item);
		} else {
			return;
		}
		if (list != null) {
			ab.setDisplayShowTitleEnabled(false);
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
			ab.setListNavigationCallbacks(list, this);
		}

		ab.setDisplayHomeAsUpEnabled(tab != TITLES.indexOf("My"));
		ab.setDisplayShowTitleEnabled(false);
		ab.setDisplayUseLogoEnabled(false);
		this.invalidateOptionsMenu();
	}

	public void updateCurrentTab() {
		if (this.adapter.frag[this.currentTab] == null) {
			return;
		}
		if (this.adapter.frag[this.currentTab] != null) {
			// May need to be uncommented. Will see.
			// this.adapter.frag[this.currentTab].clearAdapter();
		}
		if (this.currentTab == MenuActivity.TITLES.indexOf("My")) {
			this.adapter.frag[this.currentTab]
					.getMyPuzzles(this.adapter.frag[this.currentTab]
							.getActivity());
		} else if (this.currentTab == MenuActivity.TITLES.indexOf("Top")) {
			this.adapter.frag[this.currentTab].getTopPuzzles(
					this.adapter.frag[this.currentTab].getActivity());
		} else if (this.currentTab == MenuActivity.TITLES.indexOf("Recent")) {
			this.adapter.frag[this.currentTab].getRecentPuzzles(this);
		} else if (this.currentTab == MenuActivity.TITLES.indexOf("Search")) {
			// Done in CreateOptionsMenu
		} else if (this.currentTab == MenuActivity.TITLES.indexOf("Prefs")) {
			this.adapter.frag[this.currentTab]
					.getMyPuzzles(this.adapter.frag[this.currentTab]
							.getActivity());
		} else if (this.currentTab == MenuActivity.TITLES.indexOf("Packs")) {
			this.adapter.frag[this.currentTab].getPackPuzzles();
		}
		this.adapter.frag[this.currentTab].myAdapter.notifyDataSetChanged();
	}
}
