package com.pic2griddler.awesomeness;

import android.os.Bundle;
import android.app.TabActivity;
import android.content.Intent;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MenuActivity extends TabActivity
{
	protected static final String TAG = "MenuActivity";
	private TabHost th;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // Full
																														// screen.
		setContentView(R.layout.activity_menu);
		th = getTabHost();

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

		th.addTab(userSpec);
		th.addTab(worldSpec);
		th.addTab(settingsSpec);
		/*
		 * final EditText user = new EditText(this); user.setWidth(100); final
		 * EditText pass = new EditText(this); pass.setWidth(100); LinearLayout
		 * ll = new LinearLayout(this); ll.addView(user); ll.addView(pass);
		 * ll.setOrientation(LinearLayout.VERTICAL); AlertDialog.Builder adb =
		 * new AlertDialog.Builder(this);
		 * adb.setTitle("User name and password please..."); adb.setMessage(
		 * "We don't require an email, only a user/pass.  Previous usernames will be checked with password."
		 * ); adb.setView(ll); adb.setCancelable(false);
		 * adb.setPositiveButton("Submit", new DialogInterface.OnClickListener()
		 * {
		 * 
		 * public void onClick(DialogInterface dialog, int which) { } });
		 * adb.show();
		 */
		// Puzzles for future test.
		// "5 3 111101000000000 111111011011000"
		// "20 20 0100100100100000011101110110000101101110011101000010000001110100011011110010000001101000011000010111011001100101001000000110000101101110011000010110110000100000011100110110010101111000001000000111011101101001011101000110100000100000010001000110000101101110011000010010000001001000011011110110011001100110011011010110000101101110001011101110001011101110001011101000101110100010111010001011101000101110 0100100100100000011101110110000101101110011101000010000001110100011011110010000001101000011000010111011001100101001000000110000101101110011000010110110000100000011100110110010101111000001000000111011101101001011101000110100000100000010001000110000101101110011000010010000001001000011011110110011001100110011011010110000101101110001011101110001011101110001011101000101110100010111010001011101000101101"
	}

	public void switchTab(int tab)
	{
		getTabHost().setCurrentTab(tab);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}

}
