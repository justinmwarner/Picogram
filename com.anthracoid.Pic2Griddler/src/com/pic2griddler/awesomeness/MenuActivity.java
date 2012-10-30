package com.pic2griddler.awesomeness;

import android.os.Bundle;
import android.app.TabActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MenuActivity extends TabActivity implements OnClickListener {


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);	//Full screen.
		setContentView(R.layout.activity_menu);
        TabHost th = getTabHost();
		
		TabSpec userSpec = th.newTabSpec("User");
		userSpec.setIndicator("User", getResources().getDrawable(R.drawable.icon_user_tab));
		Intent userIntent =  new Intent(this, UserGriddlers.class);
		userSpec.setContent(userIntent);
		
		TabSpec worldSpec = th.newTabSpec("World");
		worldSpec.setIndicator("World", getResources().getDrawable(R.drawable.icon_world_tab));
		Intent worldIntent =  new Intent(this, WorldGriddlers.class);
		worldSpec.setContent(worldIntent);
		
		TabSpec settingsSpec = th.newTabSpec("Settings");
		settingsSpec.setIndicator("Settings", getResources().getDrawable(R.drawable.icon_settings_tab));
		Intent settingsIntent =  new Intent(this, SettingsActivity.class);
		settingsSpec.setContent(settingsIntent);
		
		th.addTab(userSpec);
		th.addTab(worldSpec);
		th.addTab(settingsSpec);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}

	public void onClick(View v) {
		//Keeping for the puzzles I made to test.
		/*
		if (v.getId() == R.id.bCreate) {
			// Creating a new Griddler.
			Intent createIntent = new Intent(this, CreateGriddlerActivity.class);
			startActivity(createIntent);
		} else if (v.getId() == R.id.bAbout) {
			// Load about me, not important. Use as testing GameActivity.

			String temp = "5 3 111101000000000 111111011011000";
			Intent myIntent = new Intent(this, GameActivity.class);
			myIntent.putExtra("info", temp);
			startActivity(myIntent);
		} else if (v.getId() == R.id.bGit) {
			// Open browser to github.
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("https://github.com/justinmwarner/Pic2Griddler"));
			startActivity(browserIntent);
		} else {
			String temp = "20 20 0100100100100000011101110110000101101110011101000010000001110100011011110010000001101000011000010111011001100101001000000110000101101110011000010110110000100000011100110110010101111000001000000111011101101001011101000110100000100000010001000110000101101110011000010010000001001000011011110110011001100110011011010110000101101110001011101110001011101110001011101000101110100010111010001011101000101110 0100100100100000011101110110000101101110011101000010000001110100011011110010000001101000011000010111011001100101001000000110000101101110011000010110110000100000011100110110010101111000001000000111011101101001011101000110100000100000010001000110000101101110011000010010000001001000011011110110011001100110011011010110000101101110001011101110001011101110001011101000101110100010111010001011101000101101";
			Intent myIntent = new Intent(this, GameActivity.class);
			myIntent.putExtra("info", temp);
			startActivity(myIntent);
		}
		*/
	}
}
