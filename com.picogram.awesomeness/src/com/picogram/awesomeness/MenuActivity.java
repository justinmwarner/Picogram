
package com.picogram.awesomeness;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.MatrixCursor;
import android.graphics.Color;
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
import android.support.v4.widget.SimpleCursorAdapter;
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
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.actionbarsherlock.widget.SearchView.OnSuggestionListener;
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
import com.google.example.games.basegameutils.BaseGameActivity;
import com.kopfgeldjaeger.ratememaybe.RateMeMaybe;
import com.kopfgeldjaeger.ratememaybe.RateMeMaybe.OnRMMUserChoiceListener;
import com.parse.Parse;
import com.picogram.awesomeness.DialogMaker.OnDialogResultListener;

import de.keyboardsurfer.android.widget.crouton.Configuration;
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

	ActionMode actionMode;

	private static final String TAG = "MainActivity";

	static final ArrayList<String> TITLES = new ArrayList<String>(
			Arrays.asList(new String[] {
					"My", "Packs", "Top", "Recent",
					"Search"
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
	Dialog dialog;

	SearchView mSearchView;
	MenuItem searchItem;

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

	@SuppressLint("NewApi")
	public void handlePositive() {
		// Goto app store.
		// TODO fix this when we publish.
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			final ActivityOptions opts = ActivityOptions.makeCustomAnimation(
					this, R.anim.fadein, R.anim.fadeout);
			this.startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse("market://details?id=Picogram")), opts.toBundle());
		} else {
			this.startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse("market://details?id=Picogram")));
		}
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
			this.updateCurrentTab();
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
		lv.setBackgroundColor(Color.TRANSPARENT);
		lv.setDivider(this.getResources().getDrawable(R.drawable.one));
		// lv.setDividerHeight(100);
		lvAdapter = new PicogramListAdapter(this, R.id.tvName);
		lv.setAdapter(lvAdapter);
		Util.setTheme(this);
		this.setContentView(R.layout.activity_menu);
		this.tabs = (PagerSlidingTabStrip) this.findViewById(R.id.tabs);
		this.pager = (ViewPager) this.findViewById(R.id.pager);
		this.pager.setPageTransformer(true, new DepthPageTransformer());
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
		if (Util.getPreferences(this).getBoolean("hasLoggedInGoogle", false) ||
				Util.getPreferences(this).getBoolean("hasLoggedInUsername", false) ||
				Util.getPreferences(this).getBoolean("hasLoggedInFacebook", false))
		{
			// TODO fix, not big deal.
		} else {
			final Configuration croutonConfiguration = new Configuration.Builder().setDuration(5000).build();
			final Crouton c = Crouton.makeText(this, "You're not logged in.  Click here to login.", Style.INFO);
			c.setConfiguration(croutonConfiguration);
			c.setOnClickListener(new OnClickListener() {

				public void onClick(final View v) {
					MenuActivity.this.startLoginActivity();
				}
			});
			c.show();
			// Ask user to login if they haven't before.
			// this.showSignInDialog();
		}

		// Show beta.
		this.showBetaDialog();

		this.updateCurrentTab(); // Update current tab ;).
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		final MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.activity_menu, menu);
		this.searchItem = menu.findItem(R.id.menu_search);
		this.mSearchView = (SearchView) this.searchItem.getActionView();
		this.setupSearchView(this.searchItem);
		this.searchItem.setVisible(false);
		if (this.currentTab == TITLES.indexOf("Search"))
		{
			this.searchItem.setVisible(true);
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

	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
			case android.R.id.home:
				this.pager.setCurrentItem(TITLES.indexOf("My"));
				break;
			case R.id.menuTutorial:
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
				break;

			case R.id.menuLogin:
				this.startLoginActivity();
				break;

			case R.id.menuPrefs:
				final Intent i = new Intent(this, SettingsActivity.class);
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					final ActivityOptions opts = ActivityOptions.makeCustomAnimation(
							this, R.anim.fadein, R.anim.fadeout);
					this.startActivityForResult(i, PREFERENCES_CODE, opts.toBundle());
				}
				else
				{
					this.startActivityForResult(i, PREFERENCES_CODE);
				}
				break;
			case R.id.menuFeedback:
				this.startFeedbackActivity();
				break;
		}
		return true;

	}

	public void onPageScrolled(final int arg0, final float arg1, final int arg2) {
		// TODO Auto-generated method stub
	}

	public void onPageScrollStateChanged(final int arg0) {
		// TODO Auto-generated method stub
	}

	public void onPageSelected(final int tab) {// Handle bottom toolbar changes.
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
		this.updateCurrentTab();
	}

	public void onSignInFailed() {
	}

	public void onSignInSucceeded() {
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
		// fetch and prepare ad for this ad space. won’t render one yet
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

	private void setupSearchView(final MenuItem searchItem) {

		searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		this.mSearchView.setOnQueryTextListener(new OnQueryTextListener() {

			public boolean onQueryTextChange(final String newText) {
				final String[] columnNames = {
						"_id", "text"
				};
				final MatrixCursor cursor = new MatrixCursor(columnNames);
				final SQLiteTagAdapter tagSql = new SQLiteTagAdapter(MenuActivity.this, "Tags", null, 1);
				final String[] array = tagSql.getTags();
				tagSql.close();
				final String[] temp = new String[2];
				int id = 0;
				for (final String item : array) {
					Log.d(TAG, "Adding " + item);
					temp[0] = Integer.toString(id++);
					temp[1] = item.toLowerCase();
					if (item.toLowerCase().startsWith(newText.toLowerCase())) {
						cursor.addRow(temp);
					}
				}
				final String[] from = {
						"text"
				};

				final int[] to = new int[] {
						android.R.id.text1
				};

				MenuActivity.this.mSearchView.setSuggestionsAdapter(new SimpleCursorAdapter(MenuActivity.this, R.layout.sherlock_spinner_dropdown_item, cursor, from, to));
				MenuActivity.this.mSearchView.setOnSuggestionListener(new OnSuggestionListener() {

					public boolean onSuggestionClick(final int position) {
						cursor.moveToPosition(position);
						MenuActivity.this.mSearchView.setQuery(cursor.getString(cursor.getColumnIndex("text")), true);
						return true;
					}

					public boolean onSuggestionSelect(final int position) {

						return false;
					}
				});
				return false;
			}

			public boolean onQueryTextSubmit(final String query) {
				final SQLiteTagAdapter tagSql = new SQLiteTagAdapter(MenuActivity.this, "Tags", null, 1);
				tagSql.insertCreate(query);
				tagSql.close();
				Log.d(TAG, "puzzles query : " + query);
				MenuActivity.this.adapter.frag[MenuActivity.this.currentTab].getTagPuzzles(
						MenuActivity.this.adapter.frag[MenuActivity.this.currentTab].getActivity(), query);
				MenuActivity.this.mSearchView.clearFocus();
				searchItem.collapseActionView();
				return false;
			}
		});
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
				.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
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

	@SuppressLint("NewApi")
	private void startFeedbackActivity() {
		final String email = "warner.73+Picogram@wright.edu";
		final String subject = "Picogram - <SUBJECT>";
		final String message = "Picogram,\n\n<MESSAGE>";
		// Contact me.
		final Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {
				email
		});
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, message);
		emailIntent.setType("message/rfc822");
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			final ActivityOptions opts = ActivityOptions.makeCustomAnimation(
					this, R.anim.fadein, R.anim.fadeout);
			this.startActivity(Intent.createChooser(emailIntent,
					"Send Mail Using :"), opts.toBundle());
		} else {
			this.startActivity(Intent.createChooser(emailIntent,
					"Send Mail Using :"));
		}
	}

	@SuppressLint("NewApi")
	private void startLoginActivity() {
		final Intent loginIntent = new Intent(this, LoginActivity.class);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			final ActivityOptions opts = ActivityOptions.makeCustomAnimation(
					this, R.anim.fadein, R.anim.fadeout);
			this.startActivityForResult(loginIntent, PREFERENCES_CODE, opts.toBundle());
		}
		else
		{
			this.startActivityForResult(loginIntent, PREFERENCES_CODE);
		}
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

			final String[] subtitle = new String[list.getCount()];
			final String[] title = new String[list.getCount()];
			for (int i = 0; i != subtitle.length; ++i) {
				title[i] = "Picogram";
				subtitle[i] = (String) list.getItem(i);
			}
			final ActionBarAdapter adapter = new ActionBarAdapter(this, title, subtitle, null);
			ab.setDisplayShowTitleEnabled(false);
			ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
			// ab.setListNavigationCallbacks(list, this);
			ab.setListNavigationCallbacks(adapter, this);
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
		} else if (this.currentTab == MenuActivity.TITLES.indexOf("Packs")) {
			this.adapter.frag[this.currentTab].getPackPuzzles();
		}
		this.adapter.frag[this.currentTab].myAdapter.notifyDataSetChanged();
	}

}
