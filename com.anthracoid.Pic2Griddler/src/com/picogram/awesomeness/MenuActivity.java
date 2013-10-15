
package com.picogram.awesomeness;

import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.crashlytics.android.Crashlytics;
import com.crittercism.app.Crittercism;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.MMSDK;
import com.parse.Parse;
import com.parse.ParseAnalytics;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.UUID;

public class MenuActivity extends ActivityGroup {
    protected static final String TAG = "MenuActivity";
    private TabHost th;
    int currentTab = 0;
    private LinearLayout llAds;
    public static int THEME = R.style.Theme_Sherlock_Light;
    public static String PREFS_FILE = "com.picogram.awesomeness_preferences";

    // admob: a1516b691219c3b

    private static String uniqueID = null;

    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    public synchronized static String id(final Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(
                MenuActivity.PREFS_FILE, MODE_PRIVATE);
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

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); // Full

        final SharedPreferences prefs = this.getSharedPreferences(
                MenuActivity.PREFS_FILE, MODE_PRIVATE);
        if (prefs.getBoolean("nightmode", false)) {
            THEME = R.style.Theme_Sherlock;
        } else {
            THEME = R.style.Theme_Sherlock_Light;
        }
        // this.setTheme(THEME);
        this.setContentView(R.layout.activity_menu);
        final String user = prefs.getString("username", "N/A");
        Parse.initialize(this, "3j445kDaxQ3lelflRVMetszjtpaXo2S1mjMZYNcW",
                "zaorBzbtWhdwMdJ0sIgBJjYvowpueuCzstLTwq1A");
        ParseAnalytics.trackAppOpened(this.getIntent());

        Crittercism.setUsername(user);
        Crittercism.init(this.getApplicationContext(),
                "5132a7682d09b61bfd000020");

        this.th = (TabHost) this.findViewById(R.id.thMain);
        this.th.setup(this.getLocalActivityManager());

        final TabSpec userSpec = this.th.newTabSpec("User");
        userSpec.setIndicator("User",
                this.getResources().getDrawable(R.drawable.icon_user_tab));
        final Intent userIntent = new Intent(this, UserGriddlers.class);
        userSpec.setContent(userIntent);

        final TabSpec worldSpec = this.th.newTabSpec("World");
        worldSpec.setIndicator("World",
                this.getResources().getDrawable(R.drawable.icon_world_tab));
        final Intent worldIntent = new Intent(this, WorldGriddlers.class);
        worldSpec.setContent(worldIntent);

        final TabSpec settingsSpec = this.th.newTabSpec("Settings");
        settingsSpec.setIndicator("Settings",
                this.getResources().getDrawable(R.drawable.icon_settings_tab));
        final Intent settingsIntent = new Intent(this, SettingsActivity.class);
        settingsSpec.setContent(settingsIntent);

        if (this.th != null) {
            this.th.addTab(userSpec);
            this.th.addTab(worldSpec);
            this.th.addTab(settingsSpec);
        }

        MMSDK.initialize(this);

        // Find the ad view for reference
        final MMAdView adViewFromXml = (MMAdView) this
                .findViewById(R.id.adView);

        // Replace YOUR_APID with the APID provided by Millennial Media
        adViewFromXml.setApid("119832");

        // MMRequest object
        final MMRequest request = new MMRequest();
        adViewFromXml.setMMRequest(request);
        adViewFromXml.setTransitionType(MMAdView.TRANSITION_RANDOM);
        adViewFromXml.getAd();
        adViewFromXml.addBlackView();
        // Change ad view.
        if (prefs.getBoolean("nightmode", false)) {
            adViewFromXml.setBackgroundColor(Color.BLACK);
        } else {
            adViewFromXml.setBackgroundColor(Color.WHITE);
        }
    }

    public void switchTab(final int tab) {
        if ((tab == 1) && !isOnline())
        {
            Crouton.makeText(this, "Must be connected to the internet to use social aspects",
                    Style.INFO).show();
            return;
        }
        this.th.setCurrentTab(tab);
        this.currentTab = tab;

    }

}
