
package com.picogram.awesomeness;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;
import com.google.android.gms.games.Player;
import com.google.example.games.basegameutils.BaseGameActivity;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LoginActivity extends BaseGameActivity implements OnClickListener {
	private class SessionStatusCallback implements Session.StatusCallback {
		public void call(final Session session, final SessionState state, final Exception exception) {
			Log.d("LoginUsingLoginFragmentActivity", String.format("New session state: %s", state.toString()));
			LoginActivity.this.updateView();
			if (state == SessionState.OPENED) {
				Util.getPreferences(LoginActivity.this.a).edit().putBoolean("hasLoggedInSuccessfully", true).commit();
			}
		}
	}
	final Activity a = this;

	private static final String TAG = "LoginActivity";
	AutoCompleteTextView editTextLogin;
	boolean hasTriedGoogle = false;
	private final Session.StatusCallback statusCallback = new SessionStatusCallback();

	Button google, facebook;

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	public void onClick(final View v) {
		if (v.getId() == R.id.bFacebookLogin) {
			Session session = Session.getActiveSession();
			if (session == null) {
				session = new Session(this);
				Session.setActiveSession(session);
				if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
					session.openForRead(new Session.OpenRequest(this).setCallback(this.statusCallback));
				}
			}
			session = Session.getActiveSession();
			if (!session.isOpened() && !session.isClosed()) {
				session.openForRead(new Session.OpenRequest(this).setCallback(this.statusCallback));
			} else {
				Session.openActiveSession(this, true, this.statusCallback);
			}
		}
		else if (v.getId() == R.id.bGoogleLogin) {
			if (this.google.getText().toString().startsWith("Log out"))
			{
				// Logging out.
				this.signOut();
				Util.getPreferences(this).edit().putBoolean("hasLoggedInGoogle", false).commit();
				this.google.setText("Log in with Google Plus");
			} else {
				this.hasTriedGoogle = true;
				this.beginUserInitiatedSignIn();
			}
		}
		else if (v.getId() == R.id.bLogin) {
			final String un = this.editTextLogin.getText().toString();
			Util.getPreferences(this).edit().putString("username", un).commit();
			Util.getPreferences(this).edit().putBoolean("hasLoggedInUsername", true).commit();
			// this.finish();
		}
	}

	private void onClickLogin() {
		final Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this).setCallback(this.statusCallback));
		} else {
			Session.openActiveSession(this, true, this.statusCallback);
		}
	}

	private void onClickLogout() {
		final Session session = Session.getActiveSession();
		if (!session.isClosed()) {
			session.closeAndClearTokenInformation();
			Util.getPreferences(this).edit().putBoolean("hasLoggedInSuccessfully", false).commit();
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Util.updateFullScreen(this);
		this.setContentView(R.layout.activity_login);

		this.facebook = (Button) this.findViewById(R.id.bFacebookLogin);
		this.google = (Button) this.findViewById(R.id.bGoogleLogin);
		final Button login = (Button) this.findViewById(R.id.bLogin);
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, this.statusCallback, savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this).setCallback(this.statusCallback));
			}
		}

		this.updateView();

		this.facebook.setOnClickListener(this);
		this.facebook.setOnClickListener(this);
		this.google.setOnClickListener(this);
		login.setOnClickListener(this);

		// Get account names on device.
		this.editTextLogin = (AutoCompleteTextView) this.findViewById(R.id.actvUsername);
		final Account[] accounts = AccountManager.get(this).getAccounts();
		final Set<String> emailSet = new HashSet<String>();
		for (final Account account : accounts) {
			String acc = account.name;
			if (acc.contains("@")) {
				acc = acc.substring(0, acc.indexOf('@'));
			}
			emailSet.add(acc.toLowerCase());
		}
		this.editTextLogin.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>(emailSet)));

		Crouton.makeText(this, "Your username is: " + Util.id(this), Style.INFO).show();

		final ActionBar ab = this.getSupportActionBar();
		if (ab != null) {
			ab.show();
			ab.setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
			case android.R.id.home:
				this.finish();
				break;
		}
		return true;
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		final Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	public void onSignInFailed() {
		if (this.hasTriedGoogle) {
			Crouton.makeText(this, "Failed to login with Google.", Style.ALERT).show();
		}
		this.google.setText("Log in with Google Plus");
		Util.getPreferences(this).edit().putBoolean("hasLoggedInGoogle", false).commit();
	}

	public void onSignInSucceeded() {
		final Player p = this.getGamesClient().getCurrentPlayer();
		Crouton.makeText(this, "Logged in with Google " + p.getDisplayName() + ".", Style.ALERT).show();
		Util.getPreferences(this).edit().putBoolean("hasLoggedInGoogle", true).commit();
		Log.d(TAG, p.getDisplayName());
		String un = "";
		final String[] names = p.getDisplayName().split(" ");
		for (int i = 0; i != names.length; ++i)
		{
			un += names[i].substring(0, names[i].length() / 2);
		}
		this.google.setText("Log out of Google Plus");
		Util.getPreferences(this).edit().putString("username", un).commit();
	}
	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(this.statusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(this.statusCallback);
	}

	private void updateView() {
		final Session session = Session.getActiveSession();
		if (session.isOpened()) {
			Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

				public void onCompleted(final GraphUser user, final Response response) {
					String un = "";
					final String[] names = user.getName().split(" ");
					for (int i = 0; i != names.length; ++i)
					{
						un += names[i].substring(0, names[i].length() / 2);
					}
					Util.getPreferences(LoginActivity.this).edit().putString("username", un).commit();
					Log.d(TAG, "LOGIN : FB : " + un);
				}
			});
			Util.getPreferences(this).edit().putBoolean("hasLoggedInFacebook", true).commit();
			this.facebook.setText("Log out of Facebook");
			this.facebook.setOnClickListener(new OnClickListener() {
				public void onClick(final View view) { LoginActivity.this.onClickLogout(); }
			});
		} else {
			Util.getPreferences(this).edit().putBoolean("hasLoggedInFacebook", false).commit();
			this.facebook.setText("Log in with Facebook");
			this.facebook.setOnClickListener(new OnClickListener() {
				public void onClick(final View view) { LoginActivity.this.onClickLogin(); }
			});
		}
	}
}
