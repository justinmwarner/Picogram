
package com.picogram.awesomeness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.flurry.android.Constants;
import com.flurry.android.FlurryAgent;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class SettingsActivity extends SherlockPreferenceActivity implements
		OnPreferenceChangeListener, OnPreferenceClickListener {
	private static final String TAG = "SettingsActivity";
	SharedPreferences prefs;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_settings);

		this.addPreferencesFromResource(R.xml.preferences);

		this.prefs = this.getSharedPreferences("Picogram", 0);
		this.findPreference("nightmode").setOnPreferenceChangeListener(this);
		this.findPreference("changelog").setOnPreferenceClickListener(this);
		this.findPreference("facebook").setOnPreferenceClickListener(this);
		this.findPreference("twitter").setOnPreferenceClickListener(this);
		this.findPreference("logoutsave").setOnPreferenceClickListener(this);
		this.findPreference("logoutdelete").setOnPreferenceClickListener(this);
		this.findPreference("email").setOnPreferenceClickListener(this);
		this.findPreference("deletedata").setOnPreferenceClickListener(this);
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
			final Intent i = this.getBaseContext().getPackageManager()
					.getLaunchIntentForPackage(this.getBaseContext().getPackageName());
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			new Thread(new Runnable() {

				public void run() {
					try {
						Thread.sleep(1000); // Wait for this to return true,
											// thus completing the change of
											// preferences.
						SettingsActivity.this.startActivity(i);
					} catch (final InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// System.exit(2);
				}

			}).start();
			Toast.makeText(this, "We're restarting the app to apply the new theme.",
					Toast.LENGTH_LONG).show();
			return true;
		}
		else if (preference.getKey().equals("nsfw"))
		{
			Crouton.makeText(
					this,
					"This isn't supported. If it becomes a problem, it will be. Message me if a Puzzle offends you.",
					Style.INFO);
		}
		else if (preference.getKey().equals("age"))
		{
			FlurryAgent.setAge(Integer.parseInt(newValue.toString()));
		}
		else if (preference.getKey().equals("gender"))
		{
			if (newValue.toString().equals("Male")) {
				FlurryAgent.setGender(Constants.MALE);
			} else {
				FlurryAgent.setGender(Constants.FEMALE);
			}
		}
		else if (preference.getKey().equals("analytics"))
		{
		}
		else if (preference.getKey().equals("logging"))
		{
		}
		else if (preference.getKey().equals(""))
		{
		}
		Crouton.makeText(this, "Changes will appear when you restart the app", Style.INFO);
		return true;
	}

	public boolean onPreferenceClick(final Preference preference) {
		Log.d(TAG, preference.getKey());
		if (preference.getKey().equals("changelog"))
		{
			// Launch change log dialog
			final ChangeLogDialog _ChangelogDialog = new ChangeLogDialog(this);
			_ChangelogDialog.show();
		}
		else if (preference.getKey().equals("deletedata"))
		{
			// Delete cache
			Util.trimCache(this);
		}
		else if (preference.getKey().equals("facebook"))
		{

		}
		else if (preference.getKey().equals("twitter"))
		{
			// Login to Twitter.
		}
		else if (preference.getKey().equals("age"))
		{
		}
		else if (preference.getKey().equals("gender"))
		{
		}
		else if (preference.getKey().equals("logoutsave"))
		{
		}
		else if (preference.getKey().equals("logoutdelete"))
		{
		}
		else if (preference.getKey().equals("email"))
		{
			final String email = "justinwarner@inboxpro.com";
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
	}

	public void optOut() {
		FlurryAgent.logEvent("UserOptOut");
	}

}
