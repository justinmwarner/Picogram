/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.example.games.basegameutils;

import android.content.Intent;
import android.os.Bundle;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.plus.PlusClient;

/**
 * Example base class for games. This implementation takes care of setting up
 * the GamesClient object and managing its lifecycle. Subclasses only need to
 * override the @link{#onSignInSucceeded} and @link{#onSignInFailed} abstract
 * methods. To initiate the sign-in flow when the user clicks the sign-in
 * button, subclasses should call @link{#beginUserInitiatedSignIn}. By default,
 * this class only instantiates the GamesClient object. If the PlusClient or
 * AppStateClient objects are also wanted, call the BaseGameActivity(int)
 * constructor and specify the requested clients. For example, to request
 * PlusClient and GamesClient, use BaseGameActivity(CLIENT_GAMES | CLIENT_PLUS).
 * To request all available clients, use BaseGameActivity(CLIENT_ALL).
 * Alternatively, you can also specify the requested clients via
 * @link{#setRequestedClients}, but you must do so before @link{#onCreate}
 * gets called, otherwise the call will have no effect.
 *
 * @author Bruno Oliveira (Google)
 */
public abstract class BaseGameActivity extends SherlockFragmentActivity implements
GameHelper.GameHelperListener {

	// The game helper object. This class is mainly a wrapper around this object.
	protected GameHelper mHelper;

	// We expose these constants here because we don't want users of this class
	// to have to know about GameHelper at all.
	public static final int CLIENT_GAMES = GameHelper.CLIENT_GAMES;
	public static final int CLIENT_APPSTATE = GameHelper.CLIENT_APPSTATE;
	public static final int CLIENT_PLUS = GameHelper.CLIENT_PLUS;
	public static final int CLIENT_ALL = GameHelper.CLIENT_ALL;

	// Requested clients. By default, that's just the games client.
	protected int mRequestedClients = CLIENT_GAMES;

	// stores any additional scopes.
	private String[] mAdditionalScopes;

	protected String mDebugTag = "BaseGameActivity";
	protected boolean mDebugLog = false;

	/** Constructs a BaseGameActivity with default client (GamesClient). */
	protected BaseGameActivity() {
		super();
		this.mHelper = new GameHelper(this);
	}

	/**
	 * Constructs a BaseGameActivity with the requested clients.
	 * @param requestedClients The requested clients (a combination of CLIENT_GAMES,
	 *         CLIENT_PLUS and CLIENT_APPSTATE).
	 */
	protected BaseGameActivity(final int requestedClients) {
		super();
		this.setRequestedClients(requestedClients);
	}

	protected void beginUserInitiatedSignIn() {
		this.mHelper.beginUserInitiatedSignIn();
	}

	protected void enableDebugLog(final boolean enabled, final String tag) {
		this.mDebugLog = true;
		this.mDebugTag = tag;
		if (this.mHelper != null) {
			this.mHelper.enableDebugLog(enabled, tag);
		}
	}

	protected AppStateClient getAppStateClient() {
		return this.mHelper.getAppStateClient();
	}

	protected GamesClient getGamesClient() {
		return this.mHelper.getGamesClient();
	}

	protected String getInvitationId() {
		return this.mHelper.getInvitationId();
	}

	protected PlusClient getPlusClient() {
		return this.mHelper.getPlusClient();
	}

	protected String getScopes() {
		return this.mHelper.getScopes();
	}

	protected String[] getScopesArray() {
		return this.mHelper.getScopesArray();
	}

	protected GameHelper.SignInFailureReason getSignInError() {
		return this.mHelper.getSignInError();
	}

	protected boolean hasSignInError() {
		return this.mHelper.hasSignInError();
	}

	protected boolean isSignedIn() {
		return this.mHelper.isSignedIn();
	}

	@Override
	protected void onActivityResult(final int request, final int response, final Intent data) {
		super.onActivityResult(request, response, data);
		this.mHelper.onActivityResult(request, response, data);
	}

	@Override
	protected void onCreate(final Bundle b) {
		super.onCreate(b);
		this.mHelper = new GameHelper(this);
		if (this.mDebugLog) {
			this.mHelper.enableDebugLog(this.mDebugLog, this.mDebugTag);
		}
		this.mHelper.setup(this, this.mRequestedClients, this.mAdditionalScopes);
	}

	@Override
	protected void onStart() {
		super.onStart();
		this.mHelper.onStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		this.mHelper.onStop();
	}

	protected void reconnectClients(final int whichClients) {
		this.mHelper.reconnectClients(whichClients);
	}

	/**
	 * Sets the requested clients. The preferred way to set the requested clients is
	 * via the constructor, but this method is available if for some reason your code
	 * cannot do this in the constructor. This must be called before onCreate in order to
	 * have any effect. If called after onCreate, this method is a no-op.
	 *
	 * @param requestedClients A combination of the flags CLIENT_GAMES, CLIENT_PLUS
	 *         and CLIENT_APPSTATE, or CLIENT_ALL to request all available clients.
	 * @param additionalScopes.  Scopes that should also be requested when the auth
	 *         request is made.
	 */
	protected void setRequestedClients(final int requestedClients, final String... additionalScopes) {
		this.mRequestedClients = requestedClients;
		this.mAdditionalScopes = additionalScopes;
	}

	protected void showAlert(final String message) {
		this.mHelper.showAlert(message);
	}

	protected void showAlert(final String title, final String message) {
		this.mHelper.showAlert(title, message);
	}

	protected void signOut() {
		this.mHelper.signOut();
	}
}