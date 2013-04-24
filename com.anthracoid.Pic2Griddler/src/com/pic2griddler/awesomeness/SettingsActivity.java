package com.pic2griddler.awesomeness;

import java.util.Map;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SettingsActivity extends SherlockPreferenceActivity implements OnPreferenceChangeListener {
	private static final String TAG = "SettingsActivity";
	private Tracker tracker;
	SharedPreferences prefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		addPreferencesFromResource(R.xml.preferences);

		prefs = getSharedPreferences("Pic2Griddler", 0);
		findPreference("nightmode").setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/*
		 * Add stuff to action bar...
		 */
		return super.onCreateOptionsMenu(menu);
	}

	public void optOut() {
		Context mCtx = this; // Get current context.
		GoogleAnalytics myInstance = GoogleAnalytics.getInstance(mCtx.getApplicationContext());
		EasyTracker.getTracker().trackEvent("settings", "opt_out", "true", (long) 0);
		myInstance.setAppOptOut(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		EasyTracker.getInstance().setContext(this);
	}

	@Override
	public void onPause() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.

	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals("nightmode")) {
			Toast.makeText(this, "You must restart the app to change the theme.", Toast.LENGTH_LONG).show();
		}
		return false;
	}
}
