
package com.pic2griddler.awesomeness;

import android.app.ActivityGroup;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.crittercism.app.Crittercism;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.MMSDK;

public class MenuActivity extends ActivityGroup {
    protected static final String TAG = "MenuActivity";
    private TabHost th;
    int currentTab = 0;
    private LinearLayout llAds;
    public static int THEME = R.style.Theme_Sherlock_Light;
    public static String PREFS_FILE = "com.pic2griddler.awesomeness_preferences";

    // admob: a1516b691219c3b

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        /*
         * adViewFromXml.getAd(new RequestListener() { public void
         * MMAdOverlayLaunched(MMAd arg0) { // TODO Auto-generated method stub }
         * public void MMAdRequestIsCaching(MMAd arg0) { // TODO Auto-generated
         * method stub } public void requestCompleted(MMAd arg0) { // TODO
         * Auto-generated method stub // Change ad view. if
         * (prefs.getBoolean("nightmode", false)) {
         * //adViewFromXml.setBackgroundColor(Color.BLACK); } else {
         * //adViewFromXml.setBackgroundColor(Color.WHITE); } } public void
         * requestFailed(MMAd arg0, MMException arg1) { // TODO Auto-generated
         * method stub adViewFromXml.setVisibility(View.GONE); } });
         */
        // Change ad view.
        if (prefs.getBoolean("nightmode", false)) {
            adViewFromXml.setBackgroundColor(Color.BLACK);
        } else {
            adViewFromXml.setBackgroundColor(Color.WHITE);
        }
    }

    public void switchTab(final int tab) {
        this.th.setCurrentTab(tab);
        this.currentTab = tab;

    }

}
