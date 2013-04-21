package com.pic2griddler.awesomeness;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SettingsActivity extends SherlockPreferenceActivity {
	private static final String TAG = "SettingsActivity";
	private Tracker tracker;
	SharedPreferences prefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		addPreferencesFromResource(R.xml.preferences);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/*
		 * Add stuff to action bar...
		 */
		menu.add("Save").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return super.onCreateOptionsMenu(menu);
	}

	public void optOut() {
		Context mCtx = this; // Get current context.
		GoogleAnalytics myInstance = GoogleAnalytics.getInstance(mCtx.getApplicationContext());
		EasyTracker.getTracker().trackEvent("settings", "opt_out", "true", (long) 0);
		myInstance.setAppOptOut(true);
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().setContext(this);
		Log.d(TAG, "User Before: " + prefs.getString("username", "N/A"));

	}

	@Override
	public void onPause() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.
		Log.d(TAG, "User After: " + prefs.getString("username", "N/A"));

	}
}
