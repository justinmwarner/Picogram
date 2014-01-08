package com.picogram.awesomeness;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.crittercism.app.Crittercism;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment.NumberPickerDialogHandler;
import com.flurry.android.FlurryAdListener;
import com.flurry.android.FlurryAdSize;
import com.flurry.android.FlurryAdType;
import com.flurry.android.FlurryAds;
import com.flurry.android.FlurryAgent;
import com.kopfgeldjaeger.ratememaybe.RateMeMaybe;
import com.kopfgeldjaeger.ratememaybe.RateMeMaybe.OnRMMUserChoiceListener;
import com.picogram.awesomeness.DialogMaker.OnDialogResultListener;
import com.stackmob.android.sdk.common.StackMobAndroid;
import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.callback.StackMobModelCallback;
import com.stackmob.sdk.exception.StackMobException;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MenuActivity extends FragmentActivity implements FlurryAdListener,
		OnPageChangeListener, OnClickListener, OnRMMUserChoiceListener {
	private static final String TAG = "MainActivity";

	public class MyPagerAdapter extends FragmentPagerAdapter {

		public SuperAwesomeCardFragment frag[] = new SuperAwesomeCardFragment[this
				.getCount()];

		public MyPagerAdapter(final FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return MenuActivity.TITLES.size();
		}

		@Override
		public Fragment getItem(final int position) {
			this.frag[position] = SuperAwesomeCardFragment
					.newInstance(position);
			return this.frag[position];
		}

		@Override
		public CharSequence getPageTitle(final int position) {
			return MenuActivity.TITLES.get(position);
		}

	}

	static final ArrayList<String> TITLES = new ArrayList<String>(
			Arrays.asList(new String[] { "My", "Packs", "Top", "Recent",
					"Search", "Prefs" }));

	public static final int CREATE_CODE = 8008;
	public static final int GAME_CODE = 1337;
	public static final int PREFERENCES_CODE = 69;
	public static String PREFS_FILE = "com.picogram.awesomeness_preferences";

	public static void getRecentPuzzles() {
	}

	public static void getSearchedPuzzles(final String tag) {
	}

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

	static PicogramListAdapter lvAdapter;

	SQLitePicogramAdapter sql = null;

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		updateBottomBar();
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
			final GriddlerOne g = new GriddlerOne(id, status, name, difficulty,
					rank, 1, author, width, height, solution, null,
					numberOfColors, colors, "0", "5");
			g.setIsUploaded("0");
			sql.addUserPicogram(g);
			g.setID(id);
			// TODO Check if Picogram already exists. If it does, just add that
			// to the users sql database.
			g.save(new StackMobModelCallback() {

				@Override
				public void failure(final StackMobException arg0) {
				}

				@Override
				public void success() {
					g.setIsUploaded("1");
					sql.updateUploadedPicogram(g.getID(), "1");
				}
			});
			final String[] tags = data.getStringExtra("tags").split(" ");
			for (final String tag : tags) {
				final GriddlerTag gt = new GriddlerTag(tag.toLowerCase());
				gt.setID(id);
				gt.save();
			}
			// Add this Picogram to the rating table as a 5.
			SQLiteRatingAdapter sra = new SQLiteRatingAdapter(this, "Rating",
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
		// Should be search.
		if (v.getId() == this.bSearch.getId()) {
			this.updateCurrentTab();
			// Hide keyboard.
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(this.etTags.getWindowToken(), 0);
			final InputMethodManager inputManager = (InputMethodManager) this
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (inputManager != null)
				inputManager.hideSoftInputFromWindow(this.getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.prefs = this.getSharedPreferences(MenuActivity.PREFS_FILE,
				MODE_PRIVATE);
		if (!prefs.getBoolean("crashes", false)) {
			Crittercism.initialize(getApplicationContext(),
					"52b6411c4002051525000002");
		}
		lv = new ListView(this);
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
		if (!Debug.isDebuggerConnected())
			setUpAds();
		setUpRater();
		StackMobAndroid.init(this.getApplicationContext(), 0,
				"f077e098-c678-4256-b7a2-c3061d9ff0c2");// Change to production.
		if (Util.isOnline())
			updateFromOffline();
		else {
			// Remove ads bar if offline.
			toolbar.setVisibility(View.GONE);
		}

	}

	private void updateFromOffline() {
		if (this.sql == null) {
			this.sql = new SQLitePicogramAdapter(this, "Picograms", null, 1);
		}
		String[][] offline = sql.getUnUploadedPicograms();
		if (offline != null) {
			for (String[] off : offline) {
				final GriddlerOne go = new GriddlerOne(off);
				go.save(new StackMobCallback() {

					@Override
					public void failure(StackMobException arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void success(String arg0) {
						sql.updateUploadedPicogram(go.getID(), "1");
					}
				});
			}

		}

		// Update new rankings.
		final SQLiteRatingAdapter sorh = new SQLiteRatingAdapter(this,
				"Rating", null, 2);
		offline = sorh.getAllNeededUpdates();
		if (offline != null) {
			Log.d(TAG, "Offline crap: " + offline.length);
			for (String[] o : offline) {
				final String id = o[0];
				final int oldRate = Integer.parseInt(o[1]);
				final int newRate = Integer.parseInt(o[2]);
				final GriddlerOne go = new GriddlerOne();
				go.setID(id);
				go.fetch(new StackMobCallback() {

					@Override
					public void failure(StackMobException arg0) {
						// Still can't upload, no big deal, do nothing.
						return;
					}

					@Override
					public void success(String arg0) {
						// Now we got it, remove the old rating.
						double onlineRatingTotal = Double.parseDouble(go
								.getRating()) * go.getNumberOfRatings();
						onlineRatingTotal = onlineRatingTotal - oldRate;
						// Add the new rating.
						double newRating = (onlineRatingTotal + newRate)
								/ (go.getNumberOfRatings());
						go.setRating(newRating + "");
						if (newRate == 0) // Only if it's a new rate
											// increase
											// the num by 1.
							go.setNumberOfRatings(go.getNumberOfRatings() + 1);

						// TODO: If save fails, let us do it next time app
						// is online.
						go.save(new StackMobCallback() {

							@Override
							public void failure(StackMobException arg0) {
								// Ignore, just wait until next time.
							}

							@Override
							public void success(String arg0) {
								// Remove this item from the offline sql
								// database. Use 0 so that we don't have any
								// more updates needed.
								sorh.updateRecord(id, newRate + "", "0");
							}
						});
					}
				});
			}
		}
		sorh.close();
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

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (!continueMusic) {
			MusicManager.pause();
		}
	}

	boolean continueMusic = true;

	@Override
	protected void onResume() {
		super.onResume();
		continueMusic = false;
		MusicManager.start(this, (int) (Math.random() * 1000));

		if (Util.getPreferences(this).getBoolean("isFirst", true)) {
			Bundle bundle = new Bundle();
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			bundle.putInt("layoutId", R.layout.dialog_tutorial);
			final DialogMaker newFragment = new DialogMaker();
			newFragment.setArguments(bundle);
			newFragment.setOnDialogResultListner(new OnDialogResultListener() {

				public void onDialogResult(Bundle result) {
					// No results needed.
				}
			});
			newFragment.show(ft, "dialog");
		}
	}

	private void setUpRater() {
		// Rate me Maybe
		RateMeMaybe rmm = new RateMeMaybe(this);
		rmm.setPromptMinimums(10, 1, 3, 7);
		rmm.setRunWithoutPlayStore(false);
		rmm.setDialogMessage("You really seem to like this app, "
				+ "since you have already used it %totalLaunchCount% times! "
				+ "It would be great if you took a moment to rate it.");
		rmm.setDialogTitle("Rate Picogram!");
		rmm.run();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.sql.close();
	}

	public void onPageScrolled(final int arg0, final float arg1, final int arg2) {
		// TODO Auto-generated method stub
	}

	public void onPageScrollStateChanged(final int arg0) {
		// TODO Auto-generated method stub
	}

	public void onPageSelected(final int tab) {
		// Handle bottom toolbar changes.
		if (tab != TITLES.indexOf("My") && !Util.isOnline()) {
			Crouton.makeText(this, "You can't use these offline!", Style.ALERT)
					.show();
			return;
		}
		if (tab == TITLES.indexOf("Prefs")) {
			final Intent i = new Intent(this, SettingsActivity.class);
			this.startActivityForResult(i, PREFERENCES_CODE);
		} else if (tab == TITLES.indexOf("Search")) {
			if (this.toolbar.getVisibility() == View.GONE) {
				this.toolbar.setVisibility(View.VISIBLE);
			}
			RelativeLayout.LayoutParams paramsButton = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			paramsButton.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
					RelativeLayout.TRUE);

			final RelativeLayout rl = new RelativeLayout(this);
			// Search, get rid of ad, and replace with search stuff.
			this.bSearch = new Button(this);
			this.bSearch.setText("Search");
			this.bSearch.setOnClickListener(this);
			RelativeLayout.LayoutParams paramsEditText = new RelativeLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			paramsEditText.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
					RelativeLayout.TRUE);
			paramsEditText
					.addRule(RelativeLayout.LEFT_OF, this.bSearch.getId());
			this.etTags = new EditText(this);
			this.etTags.setOnEditorActionListener(new OnEditorActionListener() {

				public boolean onEditorAction(final TextView v,
						final int actionId, final KeyEvent event) {
					if ((actionId == EditorInfo.IME_NULL)
							&& (event.getAction() == KeyEvent.ACTION_DOWN)) {
						return MenuActivity.this.bSearch.performClick();
					}

					return false;
				}
			});
			this.etTags.setHint("Tags...");
			rl.addView(this.etTags, paramsEditText);
			rl.addView(this.bSearch, paramsButton);
			this.toolbar.removeAllViews();
			this.toolbar.addView(rl);
			// Show keyboard
			etTags.requestFocus();
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		} else {
			updateBottomBar();
		}
		this.currentTab = tab;
		this.updateCurrentTab();
		if (tab != TITLES.indexOf("Search")) {// Hide keyboard
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(findViewById(android.R.id.content)
					.getWindowToken(), 0);
		}
	}

	private void updateBottomBar() {
		if (this.currentTab == TITLES.indexOf("Search")
				|| currentTab == TITLES.indexOf("Prefs")) { // If previous tab
															// was search or
															// prefs.
			this.toolbar.removeAllViews();
			this.toolbar.setVisibility(!this.prefs.getBoolean("advertisements",
					false) ? View.VISIBLE : View.GONE);
			if (!this.prefs.getBoolean("advertisements", false)) {
				if (!Debug.isDebuggerConnected())
					FlurryAds.fetchAd(this, "MainScreen", this.toolbar,
							FlurryAdSize.BANNER_BOTTOM);
			}
		}
	}

	public void onRenderFailed(final String arg0) {
	}

	@Override
	public void onStart() {
		super.onStart();
		Util.updateFullScreen(this);
		if (!Debug.isDebuggerConnected())
			FlurryAgent.onStartSession(this,
					this.getResources().getString(R.string.flurry));
		// fetch and prepare ad for this ad space. won’t render one yet
		this.toolbar.setVisibility(!this.prefs.getBoolean("advertisements",
				false) ? View.VISIBLE : View.GONE);
		if (!this.prefs.getBoolean("advertisements", false)) {
			if (!Debug.isDebuggerConnected())
				FlurryAds.fetchAd(this, "MainScreen", this.toolbar,
						FlurryAdSize.BANNER_BOTTOM);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (!Debug.isDebuggerConnected())
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

	public void updateCurrentTab() {
		if (this.adapter.frag[this.currentTab] != null)
			this.adapter.frag[this.currentTab].clearAdapter();
		if (this.currentTab == MenuActivity.TITLES.indexOf("My")) {
			Log.d(TAG,
					"Up: "
							+ this.adapter.frag[this.currentTab].myAdapter
									.getCount());
			this.adapter.frag[this.currentTab]
					.getMyPuzzles(this.adapter.frag[this.currentTab]
							.getActivity());
			Log.d(TAG,
					"Up: "
							+ this.adapter.frag[this.currentTab].myAdapter
									.getCount());
		} else if (this.currentTab == MenuActivity.TITLES.indexOf("Top")) {
			this.adapter.frag[this.currentTab].getSortedPuzzles(
					this.adapter.frag[this.currentTab].getActivity(), "rate");
		} else if (this.currentTab == MenuActivity.TITLES.indexOf("Recent")) {
			this.adapter.frag[this.currentTab].getSortedPuzzles(
					this.adapter.frag[this.currentTab].getActivity(),
					"createddate");
		} else if (this.currentTab == MenuActivity.TITLES.indexOf("Search")) {
			this.adapter.frag[this.currentTab].getTagPuzzles(
					this.adapter.frag[this.currentTab].getActivity(),
					this.etTags.getText().toString(), true);
		} else if (this.currentTab == MenuActivity.TITLES.indexOf("Prefs")) {
			this.adapter.frag[this.currentTab]
					.getMyPuzzles(this.adapter.frag[this.currentTab]
							.getActivity());
		} else if (this.currentTab == MenuActivity.TITLES.indexOf("Packs")) {
			this.adapter.frag[this.currentTab].getPackPuzzles();
		}
		this.adapter.frag[this.currentTab].myAdapter.notifyDataSetChanged();
		Log.d(TAG,
				"Up: "
						+ this.adapter.frag[this.currentTab].myAdapter
								.getCount());
	}

	public void handlePositive() {
		// Goto app store.
		// TODO fix this when we publish.
		startActivity(new Intent(Intent.ACTION_VIEW,
				Uri.parse("market://details?id=Picogram")));
		prefs.edit().putBoolean("apprate", true).commit();
	}

	public void handleNeutral() {
		// Remind again later on.
		Crouton.makeText(this, "We'll remind you later on.", Style.INFO).show();
	}

	public void handleNegative() {
		// Don't do it again.
		Crouton.makeText(this,
				"If you ever want to rate, go to the preferences.", Style.INFO)
				.show();
	}
}
