
package com.picogram.awesomeness;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.viewpagerindicator.CirclePageIndicator;

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

	Button google, facebook, login;

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	public void onClick(final View v) {
		if (v.getId() == R.id.bLogin) {
			final String un = this.editTextLogin.getText().toString();
			Util.getPreferences(this).edit().putString("username", un).commit();
			Util.getPreferences(this).edit().putBoolean("hasLoggedInUsername", true).commit();
			editTextLogin.setText("");
			login.setText("Current username: " + un);
		} else if (v.getId() == R.id.bFacebookLogin) {
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
		login = (Button) this.findViewById(R.id.bLogin);
		tvSeparator = (TextView) findViewById(R.id.tvDisplay);
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

		final ActionBar ab = this.getSupportActionBar();
		if (ab != null) {
			ab.show();
			ab.setDisplayHomeAsUpEnabled(true);
		}
		// Hide everything but the login with username.
		// This may be changed, so keep the code.
		// As for now, we're not interested in the users logging in with social networks.
		// The sharing works without this implementation.
		google.setVisibility(View.GONE);
		facebook.setVisibility(View.GONE);
		tvSeparator.setVisibility(View.GONE);
		if (Util.getPreferences(this).getBoolean("hasLoggedInUsername", false))
		{
			login.setText("Current username: " + Util.getPreferences(this).getString("username", ""));
		}
		TestFragmentAdapter mAdapter = new TestFragmentAdapter(getSupportFragmentManager());

		ViewPager pager = (ViewPager) findViewById(R.id.pagerLogin);
		pager.setAdapter(mAdapter);

		CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.indicatorLogin);
		indicator.setViewPager(pager);
		final float density = getResources().getDisplayMetrics().density;
		indicator.setBackgroundColor(0x00000000);
		indicator.setRadius(10 * density);
		indicator.setPageColor(getResources().getColor(R.color.bad));
		indicator.setFillColor(getResources().getColor(R.color.good));
		indicator.setStrokeColor(0xFF000000);
		indicator.setStrokeWidth(2 * density);

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
		final Player p = Games.Players.getCurrentPlayer(this.getApiClient());
		Util.getPreferences(this).edit().putBoolean("hasLoggedInGoogle", true).commit();
		String un = "";
		final String[] names = p.getDisplayName().split(" ");
		for (int i = 0; i != names.length; ++i)
		{
			un += names[i].substring(0, names[i].length() / 2);
		}
		this.google.setText("Log out as " + Util.getPreferences(this).getString("username", "G+ ERROR"));
		Util.getPreferences(this).edit().putString("username", un).commit();
		facebook.setVisibility(View.INVISIBLE);
		editTextLogin.setVisibility(View.INVISIBLE);
		login.setVisibility(View.INVISIBLE);
		tvSeparator.setVisibility(View.INVISIBLE);
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

	@SuppressWarnings("deprecation")
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
					String username = Util.getPreferences(LoginActivity.this).getString("username", "");
					if (username.isEmpty())
					{
						facebook.setText("Log out of Facebook");
					}
					else
					{
						facebook.setText("Log out as " + username);
					}
					google.setVisibility(View.INVISIBLE);
					editTextLogin.setVisibility(View.INVISIBLE);
					login.setVisibility(View.INVISIBLE);
					tvSeparator.setVisibility(View.INVISIBLE);

				}
			});
			Util.getPreferences(this).edit().putBoolean("hasLoggedInFacebook", true).commit();
			this.facebook.setOnClickListener(new OnClickListener() {
				public void onClick(final View view) {
					LoginActivity.this.onClickLogout();
				}
			});
		} else {
			Util.getPreferences(this).edit().putBoolean("hasLoggedInFacebook", false).commit();
			this.facebook.setText("Log in with Facebook");
			this.facebook.setOnClickListener(new OnClickListener() {
				public void onClick(final View view) {
					LoginActivity.this.onClickLogin();
				}
			});
		}
	}

	TextView tvSeparator;

}
