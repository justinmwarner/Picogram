
package com.picogram.awesomeness;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.flurry.android.FlurryAgent;

public class SettingsActivity extends SherlockPreferenceActivity implements
		OnPreferenceChangeListener, OnPreferenceClickListener {
	private static final String TAG = "SettingsActivity";
	SharedPreferences prefs;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_settings);

		this.addPreferencesFromResource(R.xml.preferences);

		this.prefs = this.getSharedPreferences("Picogram", 0);
		this.findPreference("nightmode").setOnPreferenceChangeListener(this);
		this.findPreference("changelog").setOnPreferenceClickListener(this);
		FlurryAgent.logEvent("PreferencesOpened");
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		/*
		 * Add stuff to action bar...
		 */
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onPause() {
		super.onStop();
	}

	public boolean onPreferenceChange(final Preference preference, final Object newValue) {
		if (preference.getKey().equals("nightmode")) {
			// Restart the app so that effects are in place...
			final PendingIntent intent = PendingIntent.getActivity(this.getParent(), 0, this
					.getParent().getIntent(), 0);
			final AlarmManager manager = (AlarmManager) this
					.getSystemService(Context.ALARM_SERVICE);
			manager.set(AlarmManager.RTC, System.currentTimeMillis() + 3000, intent);
			new Thread(new Runnable() {

				public void run() {
					try {
						Thread.sleep(1000); // Wait for this to return true,
											// thus completing the change of
											// preferences.
					} catch (final InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.exit(2);
				}

			}).start();
			Toast.makeText(this, "We're restarting the app to apply the new theme.",
					Toast.LENGTH_LONG).show();
		}
		else if (preference.getKey().equals(""))
		{}
		else if (preference.getKey().equals(""))
		{}
		else if (preference.getKey().equals(""))
		{}
		else if (preference.getKey().equals(""))
		{}
		else if (preference.getKey().equals(""))
		{}
		else if (preference.getKey().equals(""))
		{}
		return true;
	}

	public boolean onPreferenceClick(final Preference preference) {
		if (preference.getKey().equals("changelog"))
		{
			// Launch change log dialog
			final ChangeLogDialog _ChangelogDialog = new ChangeLogDialog(this);
			_ChangelogDialog.show();
		}
		else if (preference.getKey().equals("wonvisible"))
		{
			//Currently no need to do anything.
		}
		else if (preference.getKey().equals("rating"))
		{
			//Currently no need to do anything.
		}		
		else if (preference.getKey().equals("username"))
		{
			//Set username on StackMob
		}
		else if (preference.getKey().equals("password"))
		{
			//Set password on StackMob
		}
		else if (preference.getKey().equals("deletedata"))
		{
			//Delete cache
		}
		else if (preference.getKey().equals("advertisements"))
		{}
		else if (preference.getKey().equals("analytics"))
		{}
		else if (preference.getKey().equals("logging"))
		{}
		else if (preference.getKey().equals("facebook"))
		{
			//Login to Facebook.
		}
		else if (preference.getKey().equals("twitter"))
		{
			//Login to Twitter.
		}
		else if (preference.getKey().equals("age"))
		{}
		else if (preference.getKey().equals("gender"))
		{}
		else if (preference.getKey().equals("logoutsave"))
		{}
		else if (preference.getKey().equals("logoutdelete"))
		{}
		else if (preference.getKey().equals("contact"))
		{
			//Contact me.
		}
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void optOut() {
		FlurryAgent.logEvent("UserOptOut");
	}

}
