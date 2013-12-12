
package com.picogram.awesomeness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.Window;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.flurry.android.FlurryAgent;

public class SettingsActivity extends SherlockPreferenceActivity implements
OnPreferenceChangeListener, OnPreferenceClickListener {
	private static final String TAG = "SettingsActivity";
	SharedPreferences prefs;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_settings);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		this.addPreferencesFromResource(R.xml.preferences);

		this.prefs = this.getSharedPreferences("Picogram", 0);
		this.findPreference("wonvisible").setOnPreferenceChangeListener(this);
		this.findPreference("decorations").setOnPreferenceClickListener(this);
		this.findPreference("email").setOnPreferenceClickListener(this);
		this.findPreference("advertisements").setOnPreferenceClickListener(this);
		this.findPreference("analytics").setOnPreferenceClickListener(this);
		this.findPreference("logging").setOnPreferenceClickListener(this);
		FlurryAgent.logEvent("PreferencesOpened");
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onPause() {
		super.onStop();
	}

	public boolean onPreferenceChange(final Preference preference, final Object newValue) {
		return true;
	}

	public boolean onPreferenceClick(final Preference preference) {
		if (preference.getKey().equals("changelog"))
		{
			// Launch change log dialog
			final ChangeLogDialog _ChangelogDialog = new ChangeLogDialog(this);
			_ChangelogDialog.show();
		}
		else if (preference.getKey().equals("email"))
		{
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
			this.startActivity(Intent.createChooser(emailIntent, "Send Mail Using :"));
		}
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
		Util.updateFullScreen(this);
	}

	public void optOut() {
		FlurryAgent.logEvent("UserOptOut");
	}

}
