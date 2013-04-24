package com.pic2griddler.awesomeness;

import com.crittercism.app.Crittercism;
import com.millennialmedia.android.MMAd;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMException;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.MMSDK;
import com.millennialmedia.android.RequestListener;

import android.os.Bundle;
import android.app.ActivityGroup;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
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
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // Full

		final SharedPreferences prefs = getSharedPreferences(MenuActivity.PREFS_FILE, MODE_PRIVATE);
		if (prefs.getBoolean("nightmode", false)) {
			THEME = R.style.Theme_Sherlock;
		} else {
			THEME = R.style.Theme_Sherlock_Light;
		}
		setTheme(THEME);
		setContentView(R.layout.activity_menu);
		String user = prefs.getString("username", "N/A");
		Crittercism.setUsername(user);
		Crittercism.init(getApplicationContext(), "5132a7682d09b61bfd000020");

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
		final MMAdView adViewFromXml = (MMAdView) findViewById(R.id.adView);
		
		// Replace YOUR_APID with the APID provided by Millennial Media
		adViewFromXml.setApid("119832");

		// MMRequest object
		MMRequest request = new MMRequest();
		adViewFromXml.setMMRequest(request);
		adViewFromXml.setTransitionType(MMAdView.TRANSITION_RANDOM);
		adViewFromXml.getAd();
		adViewFromXml.addBlackView();
		/*adViewFromXml.getAd(new RequestListener() {

			public void MMAdOverlayLaunched(MMAd arg0) {
				// TODO Auto-generated method stub

			}

			public void MMAdRequestIsCaching(MMAd arg0) {
				// TODO Auto-generated method stub

			}

			public void requestCompleted(MMAd arg0) {
				// TODO Auto-generated method stub
				// Change ad view.
				if (prefs.getBoolean("nightmode", false)) {
					//adViewFromXml.setBackgroundColor(Color.BLACK);
				} else {
					//adViewFromXml.setBackgroundColor(Color.WHITE);
				}

			}

			public void requestFailed(MMAd arg0, MMException arg1) {
				// TODO Auto-generated method stub
				adViewFromXml.setVisibility(View.GONE);

			}

		});
		*/
		// Change ad view.
		if (prefs.getBoolean("nightmode", false)) {
			adViewFromXml.setBackgroundColor(Color.BLACK);
		} else {
			adViewFromXml.setBackgroundColor(Color.WHITE);
		}
	}

	public void switchTab(int tab) {
		th.setCurrentTab(tab);
		currentTab = tab;

	}

}
