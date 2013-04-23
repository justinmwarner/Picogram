package com.pic2griddler.awesomeness;

import com.crittercism.app.Crittercism;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.MMSDK;

import android.os.Bundle;
import android.app.ActivityGroup;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MenuActivity extends ActivityGroup {
	protected static final String TAG = "MenuActivity";
	private TabHost th;
	int currentTab = 0;
	private LinearLayout llAds;
	public static int THEME = R.style.Theme_Sherlock_Light;
	public static String PREFS_FILE = "com.pic2griddler.awesomeness_preferences";

	// admob: a1516b691219c3b

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(THEME);
		final MenuActivity c = this;
		SharedPreferences preferences = getSharedPreferences(MenuActivity.PREFS_FILE, MODE_PRIVATE);
		String user = preferences.getString("username", "N/A");
		Crittercism.setUsername(user);
		Crittercism.init(getApplicationContext(), "5132a7682d09b61bfd000020");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // Full
																														// screen.
		setContentView(R.layout.activity_menu);

		th = (TabHost) findViewById(R.id.thMain);
		th.setup(getLocalActivityManager());

		TabSpec userSpec = th.newTabSpec("User");
		userSpec.setIndicator("User", getResources().getDrawable(R.drawable.icon_user_tab));
		Intent userIntent = new Intent(this, UserGriddlers.class);
		userSpec.setContent(userIntent);

		TabSpec worldSpec = th.newTabSpec("World");
		worldSpec.setIndicator("World", getResources().getDrawable(R.drawable.icon_world_tab));
		Intent worldIntent = new Intent(this, WorldGriddlers.class);
		worldSpec.setContent(worldIntent);

		TabSpec settingsSpec = th.newTabSpec("Settings");
		settingsSpec.setIndicator("Settings", getResources().getDrawable(R.drawable.icon_settings_tab));
		Intent settingsIntent = new Intent(this, SettingsActivity.class);
		settingsSpec.setContent(settingsIntent);

		if (th != null) {
			th.addTab(userSpec);
			th.addTab(worldSpec);
			th.addTab(settingsSpec);
		}

		MMSDK.initialize(this);
		// Find the ad view for reference
		MMAdView adViewFromXml = (MMAdView) findViewById(R.id.adView);

		// Replace YOUR_APID with the APID provided by Millennial Media
		adViewFromXml.setApid("119832");

		// MMRequest object
		MMRequest request = new MMRequest();

		adViewFromXml.setMMRequest(request);
		adViewFromXml.setTransitionType(MMAdView.TRANSITION_RANDOM);
		adViewFromXml.getAd();

	}

	public void switchTab(int tab) {
		th.setCurrentTab(tab);
		currentTab = tab;

	}

}
