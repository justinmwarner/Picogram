
package com.picogram.awesomeness;

import android.app.ActivityGroup;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.crashlytics.android.Crashlytics;
import com.crittercism.app.Crittercism;
import com.flurry.android.FlurryAdListener;
import com.flurry.android.FlurryAdSize;
import com.flurry.android.FlurryAdType;
import com.flurry.android.FlurryAds;
import com.flurry.android.FlurryAgent;
import com.parse.Parse;
import com.parse.ParseAnalytics;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MenuActivity extends ActivityGroup implements FlurryAdListener {
    protected static final String TAG = "MenuActivity";
    private TabHost th;
    int currentTab = 0;
    private LinearLayout llAds;
    public static int THEME = R.style.Theme_Sherlock_Light;
    public static String PREFS_FILE = "com.picogram.awesomeness_preferences";
    private static String uniqueID = null;

    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    Handler h = new Handler();

    FrameLayout mBanner;

    public void onAdClicked(final String arg0) {
    }

    public void onAdClosed(final String arg0) {
    }

    public void onAdOpened(final String arg0) {
    }

    public void onApplicationExit(final String arg0) {
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
        this.setTheme(THEME);
        this.setContentView(R.layout.activity_menu);
        final String user = prefs.getString("username", "N/A");
        Parse.initialize(this, "3j445kDaxQ3lelflRVMetszjtpaXo2S1mjMZYNcW",
                "zaorBzbtWhdwMdJ0sIgBJjYvowpueuCzstLTwq1A");
        ParseAnalytics.trackAppOpened(this.getIntent());

        Crittercism.init(this.getApplicationContext(),
                "5132a7682d09b61bfd000020");
        Crittercism.setUsername(user);

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
        FlurryAgent.onStartSession(this, this.getResources().getString(R.string.flurry));
        FlurryAgent.setCaptureUncaughtExceptions(true);
        FlurryAgent.setLogEnabled(true);
        FlurryAgent.setLogEvents(true);

        FlurryAgent.logEvent("App Started");
        this.mBanner = (FrameLayout) this.findViewById(R.id.flurryBanner);
        // allow us to get callbacks for ad events
        FlurryAds.setAdListener(this);
        FlurryAds.enableTestAds(false);

    }

    public void onRenderFailed(final String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, this.getResources().getString(R.string.flurry));
        // fetch and prepare ad for this ad space. won’t render one yet
        FlurryAds.fetchAd(this, "MainScreen", this.mBanner, FlurryAdSize.BANNER_BOTTOM);
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    public void onVideoCompleted(final String arg0) {
    }

    public boolean shouldDisplayAd(final String arg0, final FlurryAdType arg1) {
        return true;
    }

    public void spaceDidFailToReceiveAd(final String arg0) {
    }

    public void spaceDidReceiveAd(final String adSpace) {
        FlurryAds.displayAd(this, adSpace, this.mBanner);
    }

    public void switchTab(final int tab) {
        FlurryAgent.logEvent("Switched to tab " + tab);
        if ((tab == 1) && !Util.isOnline())
        {
            Crouton.makeText(this, "Must be connected to the internet to use social aspects",
                    Style.INFO).show();

            return;
        }
        this.th.setCurrentTab(tab);
        this.currentTab = tab;

    }

}
