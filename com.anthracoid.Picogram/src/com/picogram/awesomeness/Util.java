
package com.picogram.awesomeness;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.UUID;

public class Util {
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

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
        try
        {
            for (final Enumeration<NetworkInterface> enumeration = NetworkInterface
                    .getNetworkInterfaces(); enumeration.hasMoreElements();) {
                final NetworkInterface networkInterface = enumeration.nextElement();
                for (final Enumeration<InetAddress> enumIpAddress = networkInterface
                        .getInetAddresses(); enumIpAddress
                        .hasMoreElements();) {
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
}
