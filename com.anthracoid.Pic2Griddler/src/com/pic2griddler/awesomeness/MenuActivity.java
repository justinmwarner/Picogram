package com.pic2griddler.awesomeness;

import com.crittercism.app.Crittercism;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMAd;
import com.millennialmedia.android.MMBroadcastReceiver;
import com.millennialmedia.android.MMInterstitial;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.MMSDK;
import com.millennialmedia.android.RequestListener.RequestListenerImpl;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MenuActivity extends ActivityGroup implements AdListener {
	protected static final String TAG = "MenuActivity";
	private TabHost th;
	private AdView adView;
	int currentTab = 0;
	private LinearLayout llAds;

	// admob: a1516b691219c3b

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final MenuActivity c = this;

		Crittercism.setUsername("justinwarner");
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
		final MMInterstitial interstitial = new MMInterstitial(this);

		// Set your metadata in the MMRequest object
		MMRequest request = new MMRequest();

		// Add metadata here.

		// Add the MMRequest object to your MMInterstitial.
		interstitial.setMMRequest(request);
		interstitial.setApid("119832");

		interstitial.fetch();

		interstitial.setListener(new RequestListenerImpl() {

			@Override
			public void requestCompleted(MMAd mmAd) {
				interstitial.display();
			}
		});

		// Find the ad view for reference
		MMAdView adViewFromXml = (MMAdView) findViewById(R.id.adView);

		// Replace YOUR_APID with the APID provided by Millennial Media
		adViewFromXml.setApid("119832");

		// MMRequest object
		request = new MMRequest();

		adViewFromXml.setMMRequest(request);
		adViewFromXml.setTransitionType(MMAdView.TRANSITION_RANDOM);
		adViewFromXml.getAd();
	}

	public void switchTab(int tab) {
		th.setCurrentTab(tab);
		currentTab = tab;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}

	public void onDismissScreen(Ad arg0) {
	}

	public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
	}

	public void onLeaveApplication(Ad arg0) {
	}

	public void onPresentScreen(Ad arg0) {
	}

	// This is sloppy as balls. Please help D=.
	public void onReceiveAd(Ad arg0) {
	}

}
