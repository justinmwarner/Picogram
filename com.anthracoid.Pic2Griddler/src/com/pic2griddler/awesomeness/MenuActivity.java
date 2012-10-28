package com.pic2griddler.awesomeness;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Button;

public class MenuActivity extends Activity implements OnClickListener {

	ArrayList<Button> buttons = new ArrayList<Button>();
	ArrayList<Griddler> gotd = new ArrayList<Griddler>(); // To be implemented.

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		ImageButton create = (ImageButton) findViewById(R.id.bCreate);
		Button about = (Button) findViewById(R.id.bAbout);
		Button git = (Button) findViewById(R.id.bGit);

		create.setOnClickListener(this);
		buttons.add(about);
		buttons.add(git);
		for (int i = 0; i < buttons.size(); i++) {
			buttons.get(i).setOnClickListener(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}

	public void onClick(View v) {
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
			String temp = "5 3 111111011011111 111111011011000";
			Intent myIntent = new Intent(this, GameActivity.class);
			myIntent.putExtra("info", temp);
			startActivity(myIntent);
		}
	}
}
