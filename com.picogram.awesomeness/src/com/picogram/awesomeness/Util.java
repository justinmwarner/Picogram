package com.picogram.awesomeness;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;
import android.view.WindowManager;

public class Util {
	private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
	public static String PREFS_FILE = "com.picogram.awesomeness_preferences";
	public static int THEME = R.style.Theme_Sherlock_Light;

	public static boolean deleteDir(final File dir) {
		if ((dir != null) && dir.isDirectory()) {
			final String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				final boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

	static Context mContext = null;

	public static SharedPreferences getPreferences(final Context a) {
		if (mContext == null)
			mContext = a;
		return mContext.getSharedPreferences(MenuActivity.PREFS_FILE,
				Context.MODE_PRIVATE);
	}

	public synchronized static String id(final Context context) {
		String uniqueID;
		final SharedPreferences prefs = context.getSharedPreferences(
				MenuActivity.PREFS_FILE, Context.MODE_PRIVATE);
		if (prefs.contains("username")) {
			uniqueID = prefs.getString("username", "DEFAULT_USER");
			if (!uniqueID.equals("DEFAULT_USER")) {
				return uniqueID;
			}
		}

		final SharedPreferences sharedPrefs = context.getSharedPreferences(
				PREF_UNIQUE_ID, Context.MODE_PRIVATE);
		uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
		if (uniqueID == null) {
			uniqueID = UUID.randomUUID().toString();
			final Editor editor = sharedPrefs.edit();
			editor.putString(PREF_UNIQUE_ID, uniqueID);
			editor.commit();
		}
		return uniqueID;
	}

	public static boolean isOnline() {
		try {
			for (final Enumeration<NetworkInterface> enumeration = NetworkInterface
					.getNetworkInterfaces(); enumeration.hasMoreElements();) {
				final NetworkInterface networkInterface = enumeration
						.nextElement();
				for (final Enumeration<InetAddress> enumIpAddress = networkInterface
						.getInetAddresses(); enumIpAddress.hasMoreElements();) {
					final InetAddress iNetAddress = enumIpAddress.nextElement();
					if (!iNetAddress.isLoopbackAddress()) {
						return true;
					}
				}
			}
			return false;
		} catch (final Exception e) {
			return false;
		}
	}

	// http://stackoverflow.com/questions/5832368/tablet-or-phone-android
	public static boolean isTabletDevice(final Resources resources) {
		final int screenLayout = resources.getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK;
		final boolean isScreenLarge = (screenLayout == Configuration.SCREENLAYOUT_SIZE_LARGE);
		final boolean isScreenXlarge = (screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE);
		return (isScreenLarge || isScreenXlarge);
	}

	public static void log(final String msg) {
		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		Log.d(ste[3].getClassName(), msg);
	}

	public static void setTheme(final Activity a) {

		final SharedPreferences prefs = a.getSharedPreferences(
				MenuActivity.PREFS_FILE, Context.MODE_PRIVATE);
		if (prefs.getBoolean("nightmode", false)) {
			THEME = R.style.Theme_Sherlock;
		} else {
			THEME = R.style.Theme_Sherlock_Light;
		}
		a.setTheme(THEME);
	}

	public static void updateFullScreen(final Activity a) {
		if (!Util.getPreferences(a).getBoolean("decorations", false)) {
			a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			a.getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		} else {
			a.getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			a.getWindow()
					.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		(a.findViewById(android.R.id.content)).requestLayout();
	}
}
