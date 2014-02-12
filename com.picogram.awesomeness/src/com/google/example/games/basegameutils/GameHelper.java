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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;

import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.plus.PlusClient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

public class GameHelper implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener {

	/** Listener for sign-in success or failure events. */
	public interface GameHelperListener {
		/**
		 * Called when sign-in fails. As a result, a "Sign-In" button can be
		 * shown to the user; when that button is clicked, call
		 * @link{GamesHelper#beginUserInitiatedSignIn}. Note that not all calls to this
		 * method mean an error; it may be a result of the fact that automatic
		 * sign-in could not proceed because user interaction was required
		 * (consent dialogs). So implementations of this method should NOT
		 * display an error message unless a call to @link{GamesHelper#hasSignInError}
		 * indicates that an error indeed occurred.
		 */
		void onSignInFailed();

		/** Called when sign-in succeeds. */
		void onSignInSucceeded();
	}

	// Represents the reason for a sign-in failure
	public static class SignInFailureReason {
		public static final int NO_ACTIVITY_RESULT_CODE = -100;
		int mServiceErrorCode = 0;
		int mActivityResultCode = NO_ACTIVITY_RESULT_CODE;

		public SignInFailureReason(final int serviceErrorCode) {
			this(serviceErrorCode, NO_ACTIVITY_RESULT_CODE);
		}

		public SignInFailureReason(final int serviceErrorCode, final int activityResultCode) {
			this.mServiceErrorCode = serviceErrorCode;
			this.mActivityResultCode = activityResultCode;
		}

		public int getActivityResultCode() {
			return this.mActivityResultCode;
		}

		public int getServiceErrorCode() {
			return this.mServiceErrorCode;
		}

		@Override
		public String toString() {
			return "SignInFailureReason(serviceErrorCode:" +
					errorCodeToString(this.mServiceErrorCode) +
					((this.mActivityResultCode == NO_ACTIVITY_RESULT_CODE) ? ")" :
						(",activityResultCode:" +
								activityResponseCodeToString(this.mActivityResultCode) + ")"));
		}
	}
	// States we can be in
	public static final int STATE_UNCONFIGURED = 0;
	public static final int STATE_DISCONNECTED = 1;
	public static final int STATE_CONNECTING = 2;

	public static final int STATE_CONNECTED = 3;

	// State names (for debug logging, etc)
	public static final String[] STATE_NAMES = {
		"UNCONFIGURED", "DISCONNECTED", "CONNECTING", "CONNECTED"
	};

	// State we are in right now
	int mState = STATE_UNCONFIGURED;

	// Are we expecting the result of a resolution flow?
	boolean mExpectingResolution = false;

	/**
	 * The Activity we are bound to. We need to keep a reference to the Activity
	 * because some games methods require an Activity (a Context won't do). We
	 * are careful not to leak these references: we release them on onStop().
	 */
	Activity mActivity = null;

	// OAuth scopes required for the clients. Initialized in setup().
	String mScopes[];

	// Request code we use when invoking other Activities to complete the
	// sign-in flow.
	final static int RC_RESOLVE = 9001;

	// Request code when invoking Activities whose result we don't care about.
	final static int RC_UNUSED = 9002;
	// Client objects we manage. If a given client is not enabled, it is null.
	GamesClient mGamesClient = null;
	PlusClient mPlusClient = null;

	AppStateClient mAppStateClient = null;
	// What clients we manage (OR-able values, can be combined as flags)
	public final static int CLIENT_NONE = 0x00;
	public final static int CLIENT_GAMES = 0x01;
	public final static int CLIENT_PLUS = 0x02;
	public final static int CLIENT_APPSTATE = 0x04;

	public final static int CLIENT_ALL = CLIENT_GAMES | CLIENT_PLUS | CLIENT_APPSTATE;

	// What clients were requested? (bit flags)
	int mRequestedClients = CLIENT_NONE;

	// What clients are currently connected? (bit flags)
	int mConnectedClients = CLIENT_NONE;

	// What client are we currently connecting?
	int mClientCurrentlyConnecting = CLIENT_NONE;

	// Whether to automatically try to sign in on onStart().
	boolean mAutoSignIn = true;

	/*
	 * Whether user has specifically requested that the sign-in process begin. If
	 * mUserInitiatedSignIn is false, we're in the automatic sign-in attempt that we try once the
	 * Activity is started -- if true, then the user has already clicked a "Sign-In" button or
	 * something similar
	 */
	boolean mUserInitiatedSignIn = false;

	// The connection result we got from our last attempt to sign-in.
	ConnectionResult mConnectionResult = null;

	// The error that happened during sign-in.
	SignInFailureReason mSignInFailureReason = null;
	// Print debug logs?
	boolean mDebugLog = false;

	String mDebugTag = "GameHelper";

	/*
	 * If we got an invitation id when we connected to the games client, it's here. Otherwise, it's
	 * null.
	 */
	String mInvitationId;

	/*
	 * If we got turn-based match when we connected to the games client, it's here. Otherwise, it's
	 * null.
	 */
	TurnBasedMatch mTurnBasedMatch;

	// Listener
	GameHelperListener mListener = null;

	static private final int TYPE_DEVELOPER_ERROR = 1001;
	static private final int TYPE_GAMEHELPER_BUG = 1002;

	static String activityResponseCodeToString(final int respCode) {
		switch (respCode) {
			case Activity.RESULT_OK:
				return "RESULT_OK";
			case Activity.RESULT_CANCELED:
				return "RESULT_CANCELED";
			case GamesActivityResultCodes.RESULT_APP_MISCONFIGURED:
				return "RESULT_APP_MISCONFIGURED";
			case GamesActivityResultCodes.RESULT_LEFT_ROOM:
				return "RESULT_LEFT_ROOM";
			case GamesActivityResultCodes.RESULT_LICENSE_FAILED:
				return "RESULT_LICENSE_FAILED";
			case GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED:
				return "RESULT_RECONNECT_REQUIRED";
			case GamesActivityResultCodes.RESULT_SIGN_IN_FAILED:
				return "SIGN_IN_FAILED";
			default:
				return String.valueOf(respCode);
		}
	}

	static String errorCodeToString(final int errorCode) {
		switch (errorCode) {
			case ConnectionResult.DEVELOPER_ERROR:
				return "DEVELOPER_ERROR(" + errorCode + ")";
			case ConnectionResult.INTERNAL_ERROR:
				return "INTERNAL_ERROR(" + errorCode + ")";
			case ConnectionResult.INVALID_ACCOUNT:
				return "INVALID_ACCOUNT(" + errorCode + ")";
			case ConnectionResult.LICENSE_CHECK_FAILED:
				return "LICENSE_CHECK_FAILED(" + errorCode + ")";
			case ConnectionResult.NETWORK_ERROR:
				return "NETWORK_ERROR(" + errorCode + ")";
			case ConnectionResult.RESOLUTION_REQUIRED:
				return "RESOLUTION_REQUIRED(" + errorCode + ")";
			case ConnectionResult.SERVICE_DISABLED:
				return "SERVICE_DISABLED(" + errorCode + ")";
			case ConnectionResult.SERVICE_INVALID:
				return "SERVICE_INVALID(" + errorCode + ")";
			case ConnectionResult.SERVICE_MISSING:
				return "SERVICE_MISSING(" + errorCode + ")";
			case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
				return "SERVICE_VERSION_UPDATE_REQUIRED(" + errorCode + ")";
			case ConnectionResult.SIGN_IN_REQUIRED:
				return "SIGN_IN_REQUIRED(" + errorCode + ")";
			case ConnectionResult.SUCCESS:
				return "SUCCESS(" + errorCode + ")";
			default:
				return "Unknown error code " + errorCode;
		}
	}

	/**
	 * Construct a GameHelper object, initially tied to the given Activity.
	 * After constructing this object, call @link{setup} from the onCreate()
	 * method of your Activity.
	 */
	public GameHelper(final Activity activity) {
		this.mActivity = activity;
	}

	void addToScope(final StringBuilder scopeStringBuilder, final String scope) {
		if (scopeStringBuilder.length() == 0) {
			scopeStringBuilder.append("oauth2:");
		} else {
			scopeStringBuilder.append(" ");
		}
		scopeStringBuilder.append(scope);
	}

	void assertConfigured(final String operation) {
		if (this.mState == STATE_UNCONFIGURED) {
			final String error = "GameHelper error: Operation attempted without setup: " + operation +
					". The setup() method must be called before attempting any other operation.";
			this.logError(error);
			throw new IllegalStateException(error);
		}
	}

	/**
	 * Starts a user-initiated sign-in flow. This should be called when the user
	 * clicks on a "Sign In" button. As a result, authentication/consent dialogs
	 * may show up. At the end of the process, the GameHelperListener's
	 * onSignInSucceeded() or onSignInFailed() methods will be called.
	 */
	public void beginUserInitiatedSignIn() {
		if (this.mState == STATE_CONNECTED) {
			// nothing to do
			this.logWarn("beginUserInitiatedSignIn() called when already connected. " +
					"Calling listener directly to notify of success.");
			this.notifyListener(true);
			return;
		} else if (this.mState == STATE_CONNECTING) {
			this.logWarn("beginUserInitiatedSignIn() called when already connecting. " +
					"Be patient! You can only call this method after you get an " +
					"onSignInSucceeded() or onSignInFailed() callback. Suggestion: disable " +
					"the sign-in button on startup and also when it's clicked, and re-enable " +
					"when you get the callback.");
			// ignore call (listener will get a callback when the connection process finishes)
			return;
		}

		this.debugLog("Starting USER-INITIATED sign-in flow.");

		// sign in automatically on onStart()
		this.mAutoSignIn = true;

		// Is Google Play services available?
		final int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getContext());
		this.debugLog("isGooglePlayServicesAvailable returned " + result);
		if (result != ConnectionResult.SUCCESS) {
			// Google Play services is not available.
			this.debugLog("Google Play services not available. Show error dialog.");
			this.mSignInFailureReason = new SignInFailureReason(result, 0);
			this.showFailureDialog();
			this.notifyListener(false);
			return;
		}

		// indicate that user is actively trying to sign in (so we know to resolve
		// connection problems by showing dialogs)
		this.mUserInitiatedSignIn = true;

		if (this.mConnectionResult != null) {
			// We have a pending connection result from a previous failure, so
			// start with that.
			this.debugLog("beginUserInitiatedSignIn: continuing pending sign-in flow.");
			this.setState(STATE_CONNECTING);
			this.resolveConnectionResult();
		} else {
			// We don't have a pending connection result, so start anew.
			this.debugLog("beginUserInitiatedSignIn: starting new sign-in flow.");
			this.startConnections();
		}
	}

	void byteToString(final StringBuilder sb, final byte b) {
		final int unsigned_byte = b < 0 ? b + 256 : b;
		final int hi = unsigned_byte / 16;
		final int lo = unsigned_byte % 16;
		sb.append("0123456789ABCDEF".substring(hi, hi + 1));
		sb.append("0123456789ABCDEF".substring(lo, lo + 1));
	}

	boolean checkState(final int type, final String operation, final String warning, final int... expectedStates) {
		for (final int expectedState : expectedStates) {
			if (this.mState == expectedState) {
				return true;
			}
		}
		final StringBuilder sb = new StringBuilder();
		if (type == TYPE_DEVELOPER_ERROR) {
			sb.append("GameHelper: you attempted an operation at an invalid. ");
		} else {
			sb.append("GameHelper: bug detected. Please report it at our bug tracker ");
			sb.append("https://github.com/playgameservices/android-samples/issues. ");
			sb.append("Please include the last couple hundred lines of logcat output ");
			sb.append("and describe the operation that caused this. ");
		}
		sb.append("Explanation: ").append(warning);
		sb.append("Operation: ").append(operation).append(". ");
		sb.append("State: ").append(STATE_NAMES[this.mState]).append(". ");
		if (expectedStates.length == 1) {
			sb.append("Expected state: ").append(STATE_NAMES[expectedStates[0]]).append(".");
		} else {
			sb.append("Expected states:");
			for (final int expectedState : expectedStates) {
				sb.append(" ").append(STATE_NAMES[expectedState]);
			}
			sb.append(".");
		}

		this.logWarn(sb.toString());
		return false;
	}

	void connectCurrentClient() {
		if (this.mState == STATE_DISCONNECTED) {
			// we got disconnected during the connection process, so abort
			this.logWarn("GameHelper got disconnected during connection process. Aborting.");
			return;
		}
		if (!this.checkState(TYPE_GAMEHELPER_BUG, "connectCurrentClient", "connectCurrentClient " +
				"should only get called when connecting.", STATE_CONNECTING)) {
			return;
		}

		switch (this.mClientCurrentlyConnecting) {
			case CLIENT_GAMES:
				this.mGamesClient.connect();
				break;
			case CLIENT_APPSTATE:
				this.mAppStateClient.connect();
				break;
			case CLIENT_PLUS:
				this.mPlusClient.connect();
				break;
		}
	}

	void connectNextClient() {
		// do we already have all the clients we need?
		this.debugLog("connectNextClient: requested clients: " + this.mRequestedClients +
				", connected clients: " + this.mConnectedClients);

		// failsafe, in case we somehow lost track of what clients are connected or not.
		if ((this.mGamesClient != null) && this.mGamesClient.isConnected() &&
				(0 == (this.mConnectedClients & CLIENT_GAMES))) {
			this.logWarn("GamesClient was already connected. Fixing.");
			this.mConnectedClients |= CLIENT_GAMES;
		}
		if ((this.mPlusClient != null) && this.mPlusClient.isConnected() &&
				(0 == (this.mConnectedClients & CLIENT_PLUS))) {
			this.logWarn("PlusClient was already connected. Fixing.");
			this.mConnectedClients |= CLIENT_PLUS;
		}
		if ((this.mAppStateClient != null) && this.mAppStateClient.isConnected() &&
				(0 == (this.mConnectedClients & CLIENT_APPSTATE))) {
			this.logWarn("AppStateClient was already connected. Fixing");
			this.mConnectedClients |= CLIENT_APPSTATE;
		}

		final int pendingClients = this.mRequestedClients & ~this.mConnectedClients;
		this.debugLog("Pending clients: " + pendingClients);

		if (pendingClients == 0) {
			this.debugLog("All clients now connected. Sign-in successful!");
			this.succeedSignIn();
			return;
		}

		// which client should be the next one to connect?
		if ((this.mGamesClient != null) && (0 != (pendingClients & CLIENT_GAMES))) {
			this.debugLog("Connecting GamesClient.");
			this.mClientCurrentlyConnecting = CLIENT_GAMES;
		} else if ((this.mPlusClient != null) && (0 != (pendingClients & CLIENT_PLUS))) {
			this.debugLog("Connecting PlusClient.");
			this.mClientCurrentlyConnecting = CLIENT_PLUS;
		} else if ((this.mAppStateClient != null) && (0 != (pendingClients & CLIENT_APPSTATE))) {
			this.debugLog("Connecting AppStateClient.");
			this.mClientCurrentlyConnecting = CLIENT_APPSTATE;
		} else {
			// hmmm, getting here would be a bug.
			throw new AssertionError("Not all clients connected, yet no one is next. R="
					+ this.mRequestedClients + ", C=" + this.mConnectedClients);
		}

		this.connectCurrentClient();
	}

	void debugLog(final String message) {
		if (this.mDebugLog) {
			Log.d(this.mDebugTag, "GameHelper: " + message);
		}
	}

	/** Enables debug logging */
	public void enableDebugLog(final boolean enabled, final String tag) {
		this.mDebugLog = enabled;
		this.mDebugTag = tag;
		if (enabled) {
			this.debugLog("Debug log enabled, tag: " + tag);
		}
	}

	String getAppIdFromResource() {
		try {
			final Resources res = this.getContext().getResources();
			final String pkgName = this.getContext().getPackageName();
			final int res_id = res.getIdentifier("app_id", "string", pkgName);
			return res.getString(res_id);
		} catch (final Exception ex) {
			ex.printStackTrace();
			return "??? (failed to retrieve APP ID)";
		}
	}

	/**
	 * Returns the AppStateClient object. In order to call this method, you must have
	 * called @link{#setup} with a set of clients that includes CLIENT_APPSTATE.
	 */
	public AppStateClient getAppStateClient() {
		if (this.mAppStateClient == null) {
			throw new IllegalStateException("No AppStateClient. Did you request it at setup?");
		}
		return this.mAppStateClient;
	}

	Context getContext() {
		return this.mActivity;
	}

	/**
	 * Returns the GamesClient object. In order to call this method, you must have
	 * called @link{setup} with a set of clients that includes CLIENT_GAMES.
	 */
	public GamesClient getGamesClient() {
		if (this.mGamesClient == null) {
			throw new IllegalStateException("No GamesClient. Did you request it at setup?");
		}
		return this.mGamesClient;
	}

	/**
	 * Returns the invitation ID received through an invitation notification.
	 * This should be called from your GameHelperListener's
	 * @link{GameHelperListener#onSignInSucceeded} method, to check if there's an
	 * invitation available. In that case, accept the invitation.
	 * @return The id of the invitation, or null if none was received.
	 */
	public String getInvitationId() {
		if (!this.checkState(TYPE_DEVELOPER_ERROR, "getInvitationId",
				"Invitation ID is only available when connected " +
						"(after getting the onSignInSucceeded callback).", STATE_CONNECTED)) {
			return null;
		}
		return this.mInvitationId;
	}

	/**
	 * Returns the PlusClient object. In order to call this method, you must have
	 * called @link{#setup} with a set of clients that includes CLIENT_PLUS.
	 */
	public PlusClient getPlusClient() {
		if (this.mPlusClient == null) {
			throw new IllegalStateException("No PlusClient. Did you request it at setup?");
		}
		return this.mPlusClient;
	}

	/**
	 * Returns the current requested scopes. This is not valid until setup() has
	 * been called.
	 *
	 * @return the requested scopes, including the oauth2: prefix
	 */
	public String getScopes() {
		final StringBuilder scopeStringBuilder = new StringBuilder();
		if (null != this.mScopes) {
			for (final String scope : this.mScopes) {
				this.addToScope(scopeStringBuilder, scope);
			}
		}
		return scopeStringBuilder.toString();
	}

	/**
	 * Returns an array of the current requested scopes. This is not valid until
	 * setup() has been called
	 *
	 * @return the requested scopes, including the oauth2: prefix
	 */
	public String[] getScopesArray() {
		return this.mScopes;
	}

	String getSHA1CertFingerprint() {
		try {
			final Signature[] sigs = this.getContext().getPackageManager().getPackageInfo(
					this.getContext().getPackageName(), PackageManager.GET_SIGNATURES).signatures;
			if (sigs.length == 0) {
				return "ERROR: NO SIGNATURE.";
			} else if (sigs.length > 1) {
				return "ERROR: MULTIPLE SIGNATURES";
			}
			final byte[] digest = MessageDigest.getInstance("SHA1").digest(sigs[0].toByteArray());
			final StringBuilder hexString = new StringBuilder();
			for (int i = 0; i < digest.length; ++i) {
				if (i > 0) {
					hexString.append(":");
				}
				this.byteToString(hexString, digest[i]);
			}
			return hexString.toString();

		} catch (final PackageManager.NameNotFoundException ex) {
			ex.printStackTrace();
			return "(ERROR: package not found)";
		} catch (final NoSuchAlgorithmException ex) {
			ex.printStackTrace();
			return "(ERROR: SHA1 algorithm not found)";
		}
	}

	/**
	 * Returns the error that happened during the sign-in process, null if no
	 * error occurred.
	 */
	public SignInFailureReason getSignInError() {
		return this.mSignInFailureReason;
	}

	/**
	 * Returns the tbmp match received through an invitation notification. This
	 * should be called from your GameHelperListener's
	 * @link{GameHelperListener#onSignInSucceeded} method, to check if there's a
	 * match available.
	 * @return The match, or null if none was received.
	 */
	public TurnBasedMatch getTurnBasedMatch() {
		if (!this.checkState(TYPE_DEVELOPER_ERROR, "getTurnBasedMatch",
				"TurnBasedMatch is only available when connected "
						+ "(after getting the onSignInSucceeded callback).",
						STATE_CONNECTED)) {
			return null;
		}
		return this.mTurnBasedMatch;
	}

	/**
	 * Give up on signing in due to an error. Shows the appropriate error
	 * message to the user, using a standard error dialog as appropriate to the
	 * cause of the error. That dialog will indicate to the user how the problem
	 * can be solved (for example, re-enable Google Play Services, upgrade to a
	 * new version, etc).
	 */
	void giveUp(final SignInFailureReason reason) {
		this.checkState(TYPE_GAMEHELPER_BUG, "giveUp", "giveUp should only be called when " +
				"connecting. Proceeding anyway.", STATE_CONNECTING);
		this.mAutoSignIn = false;
		this.killConnections();
		this.mSignInFailureReason = reason;
		this.showFailureDialog();
		this.notifyListener(false);
	}

	/**
	 * Returns whether or not there was a (non-recoverable) error during the
	 * sign-in process.
	 */
	public boolean hasSignInError() {
		return this.mSignInFailureReason != null;
	}

	/** Returns whether or not the user is signed in. */
	public boolean isSignedIn() {
		return this.mState == STATE_CONNECTED;
	}

	void killConnections() {
		if (!this.checkState(TYPE_GAMEHELPER_BUG, "killConnections", "killConnections() should only " +
				"get called while connected or connecting.", STATE_CONNECTED, STATE_CONNECTING)) {
			return;
		}
		this.debugLog("killConnections: killing connections.");

		this.mConnectionResult = null;
		this.mSignInFailureReason = null;

		if ((this.mGamesClient != null) && this.mGamesClient.isConnected()) {
			this.debugLog("Disconnecting GamesClient.");
			this.mGamesClient.disconnect();
		}
		if ((this.mPlusClient != null) && this.mPlusClient.isConnected()) {
			this.debugLog("Disconnecting PlusClient.");
			this.mPlusClient.disconnect();
		}
		if ((this.mAppStateClient != null) && this.mAppStateClient.isConnected()) {
			this.debugLog("Disconnecting AppStateClient.");
			this.mAppStateClient.disconnect();
		}
		this.mConnectedClients = CLIENT_NONE;
		this.debugLog("killConnections: all clients disconnected.");
		this.setState(STATE_DISCONNECTED);
	}

	void logError(final String message) {
		Log.e(this.mDebugTag, "*** GameHelper ERROR: " + message);
	}

	void logWarn(final String message) {
		Log.w(this.mDebugTag, "!!! GameHelper WARNING: " + message);
	}

	Dialog makeSimpleDialog(final String text) {
		return (new AlertDialog.Builder(this.getContext())).setMessage(text)
				.setNeutralButton(android.R.string.ok, null).create();
	}

	void notifyListener(final boolean success) {
		this.debugLog("Notifying LISTENER of sign-in " + (success ? "SUCCESS" :
			this.mSignInFailureReason != null ? "FAILURE (error)" : "FAILURE (no error)"));
		if (this.mListener != null) {
			if (success) {
				this.mListener.onSignInSucceeded();
			} else {
				this.mListener.onSignInFailed();
			}
		}
	}

	/**
	 * Handle activity result. Call this method from your Activity's
	 * onActivityResult callback. If the activity result pertains to the sign-in
	 * process, processes it appropriately.
	 */
	public void onActivityResult(final int requestCode, final int responseCode, final Intent intent) {
		this.debugLog("onActivityResult: req=" + (requestCode == RC_RESOLVE ? "RC_RESOLVE" :
			String.valueOf(requestCode)) + ", resp=" +
			activityResponseCodeToString(responseCode));
		if (requestCode != RC_RESOLVE) {
			this.debugLog("onActivityResult: request code not meant for us. Ignoring.");
			return;
		}

		// no longer expecting a resolution
		this.mExpectingResolution = false;

		if (this.mState != STATE_CONNECTING) {
			this.debugLog("onActivityResult: ignoring because state isn't STATE_CONNECTING (" +
					"it's " + STATE_NAMES[this.mState] + ")");
			return;
		}

		// We're coming back from an activity that was launched to resolve a
		// connection problem. For example, the sign-in UI.
		if (responseCode == Activity.RESULT_OK) {
			// Ready to try to connect again.
			this.debugLog("onAR: Resolution was RESULT_OK, so connecting current client again.");
			this.connectCurrentClient();
		} else if (responseCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
			this.debugLog("onAR: Resolution was RECONNECT_REQUIRED, so reconnecting.");
			this.connectCurrentClient();
		} else if (responseCode == Activity.RESULT_CANCELED) {
			// User cancelled.
			this.debugLog("onAR: Got a cancellation result, so disconnecting.");
			this.mAutoSignIn = false;
			this.mUserInitiatedSignIn = false;
			this.mSignInFailureReason = null; // cancelling is not a failure!
			this.killConnections();
			this.notifyListener(false);
		} else {
			// Whatever the problem we were trying to solve, it was not
			// solved. So give up and show an error message.
			this.debugLog("onAR: responseCode=" + activityResponseCodeToString(responseCode) +
					", so giving up.");
			this.giveUp(new SignInFailureReason(this.mConnectionResult.getErrorCode(), responseCode));
		}
	}

	/** Called when we successfully obtain a connection to a client. */
	public void onConnected(final Bundle connectionHint) {

		this.debugLog("onConnected: connected! client=" + this.mClientCurrentlyConnecting);

		// Mark the current client as connected
		this.mConnectedClients |= this.mClientCurrentlyConnecting;
		this.debugLog("Connected clients updated to: " + this.mConnectedClients);

		// If this was the games client and it came with an invite, store it for
		// later retrieval.
		if ((this.mClientCurrentlyConnecting == CLIENT_GAMES)
				&& (connectionHint != null)) {
			this.debugLog("onConnected: connection hint provided. Checking for invite.");
			final Invitation inv = connectionHint
					.getParcelable(GamesClient.EXTRA_INVITATION);
			if ((inv != null) && (inv.getInvitationId() != null)) {
				// accept invitation
				this.debugLog("onConnected: connection hint has a room invite!");
				this.mInvitationId = inv.getInvitationId();
				this.debugLog("Invitation ID: " + this.mInvitationId);
			}

			this.debugLog("onConnected: connection hint provided. Checking for TBMP game.");

			this.mTurnBasedMatch = connectionHint
					.getParcelable(GamesClient.EXTRA_TURN_BASED_MATCH);

		}

		// connect the next client in line, if any.
		this.connectNextClient();
	}

	/** Handles a connection failure reported by a client. */
	public void onConnectionFailed(final ConnectionResult result) {
		// save connection result for later reference
		this.debugLog("onConnectionFailed");

		this.mConnectionResult = result;
		this.debugLog("Connection failure:");
		this.debugLog("   - code: " + errorCodeToString(this.mConnectionResult.getErrorCode()));
		this.debugLog("   - resolvable: " + this.mConnectionResult.hasResolution());
		this.debugLog("   - details: " + this.mConnectionResult.toString());

		if (!this.mUserInitiatedSignIn) {
			// If the user didn't initiate the sign-in, we don't try to resolve
			// the connection problem automatically -- instead, we fail and wait
			// for the user to want to sign in. That way, they won't get an
			// authentication (or other) popup unless they are actively trying
			// to
			// sign in.
			this.debugLog("onConnectionFailed: since user didn't initiate sign-in, failing now.");
			this.mConnectionResult = result;
			this.setState(STATE_DISCONNECTED);
			this.notifyListener(false);
			return;
		}

		this.debugLog("onConnectionFailed: since user initiated sign-in, resolving problem.");

		// Resolve the connection result. This usually means showing a dialog or
		// starting an Activity that will allow the user to give the appropriate
		// consents so that sign-in can be successful.
		this.resolveConnectionResult();
	}

	/** Called when we are disconnected from a client. */
	public void onDisconnected() {
		this.debugLog("onDisconnected.");
		if (this.mState == STATE_DISCONNECTED) {
			// This is expected.
			this.debugLog("onDisconnected is expected, so no action taken.");
			return;
		}

		// Unexpected disconnect (rare!)
		this.logWarn("Unexpectedly disconnected. Severing remaining connections.");

		// kill the other connections too, and revert to DISCONNECTED state.
		this.killConnections();
		this.mSignInFailureReason = null;

		// call the sign in failure callback
		this.debugLog("Making extraordinary call to onSignInFailed callback");
		this.notifyListener(false);
	}

	/** Call this method from your Activity's onStart(). */
	public void onStart(final Activity act) {
		this.mActivity = act;

		this.debugLog("onStart, state = " + STATE_NAMES[this.mState]);
		this.assertConfigured("onStart");

		switch (this.mState) {
			case STATE_DISCONNECTED:
				// we are not connected, so attempt to connect
				if (this.mAutoSignIn) {
					this.debugLog("onStart: Now connecting clients.");
					this.startConnections();
				} else {
					this.debugLog("onStart: Not connecting (user specifically signed out).");
				}
				break;
			case STATE_CONNECTING:
				// connection process is in progress; no action required
				this.debugLog("onStart: connection process in progress, no action taken.");
				break;
			case STATE_CONNECTED:
				// already connected (for some strange reason). No complaints :-)
				this.debugLog("onStart: already connected (unusual, but ok).");
				break;
			default:
				final String msg = "onStart: BUG: unexpected state " + STATE_NAMES[this.mState];
				this.logError(msg);
				throw new IllegalStateException(msg);
		}
	}

	/** Call this method from your Activity's onStop(). */
	public void onStop() {
		this.debugLog("onStop, state = " + STATE_NAMES[this.mState]);
		this.assertConfigured("onStop");
		switch (this.mState) {
			case STATE_CONNECTED:
			case STATE_CONNECTING:
				// kill connections
				this.debugLog("onStop: Killing connections");
				this.killConnections();
				break;
			case STATE_DISCONNECTED:
				this.debugLog("onStop: not connected, so no action taken.");
				break;
			default:
				final String msg = "onStop: BUG: unexpected state " + STATE_NAMES[this.mState];
				this.logError(msg);
				throw new IllegalStateException(msg);
		}

		// let go of the Activity reference
		this.mActivity = null;
	}

	void printMisconfiguredDebugInfo() {
		this.debugLog("****");
		this.debugLog("****");
		this.debugLog("**** APP NOT CORRECTLY CONFIGURED TO USE GOOGLE PLAY GAME SERVICES");
		this.debugLog("**** This is usually caused by one of these reasons:");
		this.debugLog("**** (1) Your package name and certificate fingerprint do not match");
		this.debugLog("****     the client ID you registered in Developer Console.");
		this.debugLog("**** (2) Your App ID was incorrectly entered.");
		this.debugLog("**** (3) Your game settings have not been published and you are ");
		this.debugLog("****     trying to log in with an account that is not listed as");
		this.debugLog("****     a test account.");
		this.debugLog("****");
		final Context ctx = this.getContext();
		if (ctx == null) {
			this.debugLog("*** (no Context, so can't print more debug info)");
			return;
		}

		this.debugLog("**** To help you debug, here is the information about this app");
		this.debugLog("**** Package name         : " + this.getContext().getPackageName());
		this.debugLog("**** Cert SHA1 fingerprint: " + this.getSHA1CertFingerprint());
		this.debugLog("**** App ID from          : " + this.getAppIdFromResource());
		this.debugLog("****");
		this.debugLog("**** Check that the above information matches your setup in ");
		this.debugLog("**** Developer Console. Also, check that you're logging in with the");
		this.debugLog("**** right account (it should be listed in the Testers section if");
		this.debugLog("**** your project is not yet published).");
		this.debugLog("****");
		this.debugLog("**** For more information, refer to the troubleshooting guide:");
		this.debugLog("****   http://developers.google.com/games/services/android/troubleshooting");
	}

	/**
	 * Disconnects the indicated clients, then connects them again.
	 * @param whatClients Indicates which clients to reconnect.
	 */
	public void reconnectClients(final int whatClients) {
		this.checkState(TYPE_DEVELOPER_ERROR, "reconnectClients", "reconnectClients should " +
				"only be called when connected. Proceeding anyway.", STATE_CONNECTED);
		boolean actuallyReconnecting = false;

		if (((whatClients & CLIENT_GAMES) != 0) && (this.mGamesClient != null)
				&& this.mGamesClient.isConnected()) {
			this.debugLog("Reconnecting GamesClient.");
			actuallyReconnecting = true;
			this.mConnectedClients &= ~CLIENT_GAMES;
			this.mGamesClient.reconnect();
		}
		if (((whatClients & CLIENT_APPSTATE) != 0) && (this.mAppStateClient != null)
				&& this.mAppStateClient.isConnected()) {
			this.debugLog("Reconnecting AppStateClient.");
			actuallyReconnecting = true;
			this.mConnectedClients &= ~CLIENT_APPSTATE;
			this.mAppStateClient.reconnect();
		}
		if (((whatClients & CLIENT_PLUS) != 0) && (this.mPlusClient != null)
				&& this.mPlusClient.isConnected()) {
			// PlusClient doesn't need reconnections.
			this.logWarn("GameHelper is ignoring your request to reconnect " +
					"PlusClient because this is unnecessary.");
		}

		if (actuallyReconnecting) {
			this.setState(STATE_CONNECTING);
		} else {
			// No reconnections are to take place, so for consistency we call the listener
			// as if sign in had just succeeded.
			this.debugLog("No reconnections needed, so behaving as if sign in just succeeded");
			this.notifyListener(true);
		}
	}

	/**
	 * Attempts to resolve a connection failure. This will usually involve
	 * starting a UI flow that lets the user give the appropriate consents
	 * necessary for sign-in to work.
	 */
	void resolveConnectionResult() {
		// Try to resolve the problem
		this.checkState(
				TYPE_GAMEHELPER_BUG,
				"resolveConnectionResult",
				"resolveConnectionResult should only be called when connecting. Proceeding anyway.",
				STATE_CONNECTING);

		if (this.mExpectingResolution) {
			this.debugLog("We're already expecting the result of a previous resolution.");
			return;
		}

		this.debugLog("resolveConnectionResult: trying to resolve result: " + this.mConnectionResult);
		if (this.mConnectionResult.hasResolution()) {
			// This problem can be fixed. So let's try to fix it.
			this.debugLog("Result has resolution. Starting it.");
			try {
				// launch appropriate UI flow (which might, for example, be the
				// sign-in flow)
				this.mExpectingResolution = true;
				this.mConnectionResult.startResolutionForResult(this.mActivity, RC_RESOLVE);
			} catch (final SendIntentException e) {
				// Try connecting again
				this.debugLog("SendIntentException, so connecting again.");
				this.connectCurrentClient();
			}
		} else {
			// It's not a problem what we can solve, so give up and show an
			// error.
			this.debugLog("resolveConnectionResult: result has no resolution. Giving up.");
			this.giveUp(new SignInFailureReason(this.mConnectionResult.getErrorCode()));
		}
	}

	void setState(final int newState) {
		final String oldStateName = STATE_NAMES[this.mState];
		final String newStateName = STATE_NAMES[newState];
		this.mState = newState;
		this.debugLog("State change " + oldStateName + " -> " + newStateName);
	}

	/**
	 * Same as calling @link{setup(GameHelperListener, int)} requesting only the
	 * CLIENT_GAMES client.
	 */
	public void setup(final GameHelperListener listener) {
		this.setup(listener, CLIENT_GAMES);
	}

	/**
	 * Performs setup on this GameHelper object. Call this from the onCreate()
	 * method of your Activity. This will create the clients and do a few other
	 * initialization tasks. Next, call @link{#onStart} from the onStart()
	 * method of your Activity.
	 *
	 * @param listener The listener to be notified of sign-in events.
	 * @param clientsToUse The clients to use. Use a combination of
	 *            CLIENT_GAMES, CLIENT_PLUS and CLIENT_APPSTATE, or CLIENT_ALL
	 *            to request all clients.
	 * @param additionalScopes Any scopes to be used that are outside of the ones defined
	 *            in the Scopes class.
	 *            I.E. for YouTube uploads one would add
	 *            "https://www.googleapis.com/auth/youtube.upload"
	 */
	public void setup(final GameHelperListener listener, final int clientsToUse, final String... additionalScopes) {
		if (this.mState != STATE_UNCONFIGURED) {
			final String error = "GameHelper: you called GameHelper.setup() twice. You can only call " +
					"it once.";
			this.logError(error);
			throw new IllegalStateException(error);
		}
		this.mListener = listener;
		this.mRequestedClients = clientsToUse;

		this.debugLog("Setup: requested clients: " + this.mRequestedClients);

		final Vector<String> scopesVector = new Vector<String>();
		if (0 != (clientsToUse & CLIENT_GAMES)) {
			scopesVector.add(Scopes.GAMES);
		}
		if (0 != (clientsToUse & CLIENT_PLUS)) {
			scopesVector.add(Scopes.PLUS_LOGIN);
		}
		if (0 != (clientsToUse & CLIENT_APPSTATE)) {
			scopesVector.add(Scopes.APP_STATE);
		}

		if (null != additionalScopes) {
			for (final String scope : additionalScopes) {
				scopesVector.add(scope);
			}
		}

		this.mScopes = new String[scopesVector.size()];
		scopesVector.copyInto(this.mScopes);

		this.debugLog("setup: scopes:");
		for (final String scope : this.mScopes) {
			this.debugLog("  - " + scope);
		}

		if (0 != (clientsToUse & CLIENT_GAMES)) {
			this.debugLog("setup: creating GamesClient");

			// If you want to suppress the signin interstitial, set setShowConnectingPopup to false.
			this.mGamesClient = new GamesClient.Builder(this.getContext(), this, this)
			.setGravityForPopups(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
			.setScopes(this.mScopes)
			.setShowConnectingPopup(true)
			.create();

		}

		if (0 != (clientsToUse & CLIENT_PLUS)) {
			this.debugLog("setup: creating GamesPlusClient");
			this.mPlusClient = new PlusClient.Builder(this.getContext(), this, this)
			.setScopes(this.mScopes)
			.build();
		}

		if (0 != (clientsToUse & CLIENT_APPSTATE)) {
			this.debugLog("setup: creating AppStateClient");
			this.mAppStateClient = new AppStateClient.Builder(this.getContext(), this, this)
			.setScopes(this.mScopes)
			.create();
		}
		this.setState(STATE_DISCONNECTED);
	}

	/** Convenience method to show an alert dialog. */
	public void showAlert(final String message) {
		(new AlertDialog.Builder(this.getContext())).setMessage(message)
		.setNeutralButton(android.R.string.ok, null).create().show();
	}

	/** Convenience method to show an alert dialog. */
	public void showAlert(final String title, final String message) {
		(new AlertDialog.Builder(this.getContext())).setTitle(title).setMessage(message)
		.setNeutralButton(android.R.string.ok, null).create().show();
	}

	/** Shows an error dialog that's appropriate for the failure reason. */
	void showFailureDialog() {
		final Context ctx = this.getContext();
		if (ctx == null) {
			this.debugLog("*** No context. Can't show failure dialog.");
			return;
		}
		this.debugLog("Making error dialog for failure: " + this.mSignInFailureReason);
		Dialog errorDialog = null;
		final int errorCode = this.mSignInFailureReason.getServiceErrorCode();
		final int actResp = this.mSignInFailureReason.getActivityResultCode();

		switch (actResp) {
			case GamesActivityResultCodes.RESULT_APP_MISCONFIGURED:
				errorDialog = this.makeSimpleDialog("App Misconfigured");
				this.printMisconfiguredDebugInfo();
				break;
			case GamesActivityResultCodes.RESULT_SIGN_IN_FAILED:
				errorDialog = this.makeSimpleDialog("Sign in failed");
				break;
			case GamesActivityResultCodes.RESULT_LICENSE_FAILED:
				errorDialog = this.makeSimpleDialog("License failed.");
				break;
			default:
				// No meaningful Activity response code, so generate default Google
				// Play services dialog
				errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this.mActivity,
						RC_UNUSED, null);
				if (errorDialog == null) {
					// get fallback dialog
					this.debugLog("No standard error dialog available. Making fallback dialog.");
					errorDialog = this.makeSimpleDialog("Some random error. Lol.");
				}
		}

		this.debugLog("Showing error dialog.");
		errorDialog.show();
	}

	/** Sign out and disconnect from the APIs. */
	public void signOut() {
		if (this.mState == STATE_DISCONNECTED) {
			// nothing to do
			this.debugLog("signOut: state was already DISCONNECTED, ignoring.");
			return;
		}

		// for the PlusClient, "signing out" means clearing the default account and
		// then disconnecting
		if ((this.mPlusClient != null) && this.mPlusClient.isConnected()) {
			this.debugLog("Clearing default account on PlusClient.");
			this.mPlusClient.clearDefaultAccount();
		}

		// For the games client, signing out means calling signOut and disconnecting
		if ((this.mGamesClient != null) && this.mGamesClient.isConnected()) {
			this.debugLog("Signing out from GamesClient.");
			this.mGamesClient.signOut();
		}

		// Ready to disconnect
		this.debugLog("Proceeding with disconnection.");
		this.killConnections();
	}

	void startConnections() {
		if (!this.checkState(TYPE_GAMEHELPER_BUG, "startConnections", "startConnections should " +
				"only get called when disconnected.", STATE_DISCONNECTED)) {
			return;
		}
		this.debugLog("Starting connections.");
		this.setState(STATE_CONNECTING);
		this.mInvitationId = null;
		this.mTurnBasedMatch = null;
		this.connectNextClient();
	}

	void succeedSignIn() {
		this.checkState(TYPE_GAMEHELPER_BUG, "succeedSignIn", "succeedSignIn should only " +
				"get called in the connecting or connected state. Proceeding anyway.",
				STATE_CONNECTING, STATE_CONNECTED);
		this.debugLog("All requested clients connected. Sign-in succeeded!");
		this.setState(STATE_CONNECTED);
		this.mSignInFailureReason = null;
		this.mAutoSignIn = true;
		this.mUserInitiatedSignIn = false;
		this.notifyListener(true);
	}

}