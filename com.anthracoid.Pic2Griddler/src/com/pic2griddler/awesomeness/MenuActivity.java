package com.pic2griddler.awesomeness;

import android.os.Bundle;
import android.app.ActivityGroup;
import android.content.Intent;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MenuActivity extends ActivityGroup {
	protected static final String TAG = "MenuActivity";
	private TabHost th;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN); // Full
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
		settingsSpec.setIndicator("Settings",
				getResources().getDrawable(R.drawable.icon_settings_tab));
		Intent settingsIntent = new Intent(this, SettingsActivity.class);
		settingsSpec.setContent(settingsIntent);

		if (th != null) {
			th.addTab(userSpec);
			th.addTab(worldSpec);
			th.addTab(settingsSpec);
		}
	}

	public void switchTab(int tab) {
		th.setCurrentTab(tab);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}

}
