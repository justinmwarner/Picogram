
package com.picogram.awesomeness;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.games.GamesClient;
import com.kopfgeldjaeger.ratememaybe.RateMeMaybe;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import de.psdev.licensesdialog.LicensesDialog;

public class SettingsActivity extends SherlockPreferenceActivity implements
		OnPreferenceChangeListener, OnPreferenceClickListener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
	private static final String TAG = "SettingsActivity";
	SharedPreferences prefs;
	boolean continueMusic = true;

	GamesClient gc;

	public void onConnected(final Bundle arg0) {
		this.gc.signOut();
		Crouton.makeText(this, "Logout successful.", Style.INFO).show();
	}

	public void onConnectionFailed(final ConnectionResult cr) {
		Crouton.makeText(this, "Logout failed " + cr.getErrorCode() + ".", Style.INFO).show();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_settings);

		this.addPreferencesFromResource(R.xml.preferences);

		this.prefs = this.getSharedPreferences("Picogram", 0);
		this.findPreference("wonvisible").setOnPreferenceChangeListener(this);
		this.findPreference("decorations").setOnPreferenceClickListener(this);
		this.findPreference("music").setOnPreferenceChangeListener(this);
		this.findPreference("email").setOnPreferenceClickListener(this);
		this.findPreference("advertisements").setOnPreferenceClickListener(this);
		this.findPreference("changelog").setOnPreferenceClickListener(this);
		this.findPreference("analytics").setOnPreferenceClickListener(this);
		this.findPreference("logging").setOnPreferenceClickListener(this);
		this.findPreference("crashes").setOnPreferenceClickListener(this);
		this.findPreference("licenses").setOnPreferenceClickListener(this);
		this.findPreference("rateapp").setOnPreferenceClickListener(this);
		this.findPreference("logout").setOnPreferenceClickListener(this);

		FlurryAgent.logEvent("PreferencesOpened");
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPause() {
		super.onPause();
		if (!this.continueMusic) {
			MusicManager.pause();
		}
	}

	public boolean onPreferenceChange(final Preference preference,
			final Object newValue) {
		Log.d(TAG, "Changing 1 " + preference.getKey());
		if (preference.getKey().equals("music")) {
			Log.d(TAG, "Changing 2 music " + newValue.toString());
			this.prefs.edit().putString("music", newValue.toString()).commit();
			MusicManager.start(this, newValue.toString(), true);
		}
		return true;
	}

	@SuppressLint("NewApi")
	public boolean onPreferenceClick(final Preference preference) {
		if (preference.getKey().equals("changelog")) {
			// Launch change log dialog
			Log.d(TAG, "Changelog");
			final ChangeLogDialog _ChangelogDialog = new ChangeLogDialog(this);
			_ChangelogDialog.show();
		} else if (preference.getKey().equals("licenses")) {
			// Launch the licenses stuff.
			new LicensesDialog(this, R.raw.licenses, false, false).show();
		} else if (preference.getKey().equals("email")) {
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
		} else if (preference.getKey().equals("rateapp")) {
			// TODO fix this when we publish.
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				final ActivityOptions opts = ActivityOptions.makeCustomAnimation(
						this, R.anim.fadein, R.anim.fadeout);
				this.startActivity(new Intent(Intent.ACTION_VIEW,
						Uri.parse("market://details?id=Picogram")), opts.toBundle());
			}
			else {
				this.startActivity(new Intent(Intent.ACTION_VIEW,
						Uri.parse("market://details?id=Picogram")));
			}

			final Editor editor = this.prefs.edit();
			editor.putBoolean(RateMeMaybe.PREF.DONT_SHOW_AGAIN, true);
			editor.commit();
		}
		else if (preference.getKey().equals("logout"))
		{
			final GamesClient.Builder builder = new GamesClient.Builder(this.getBaseContext(), this, this);
			this.gc = builder.create();
			this.gc.connect();
		}
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
		Util.updateFullScreen(this);
		this.continueMusic = false;
		Log.d(TAG, "Change resume 3");
		MusicManager.start(this);
	}

	public void optOut() {
		FlurryAgent.logEvent("UserOptOut");
	}

}
