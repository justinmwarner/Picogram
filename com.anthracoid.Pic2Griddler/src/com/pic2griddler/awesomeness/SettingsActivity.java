package com.pic2griddler.awesomeness;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;

public class SettingsActivity extends Activity
{
	private Tracker tracker;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		EasyTracker.getInstance().setContext(this);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_settings, menu);
		return true;
	}

	public void optOut()
	{
		Context mCtx = this; // Get current context.
		GoogleAnalytics myInstance = GoogleAnalytics.getInstance(mCtx.getApplicationContext());
		EasyTracker.getTracker().trackEvent("settings", "opt_out", "true", (long) 0);
		myInstance.setAppOptOut(true);
	}

	@Override
	public void onStop()
	{
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.
	}
}
