package com.pic2griddler.awesomeness;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.GridView;

public class UserGriddlers extends Activity implements OnTouchListener
{
	String[] names = null, diffs = null, rates = null, infos = null;
	GridView gv;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_griddlers);
		gv = (GridView) findViewById(R.id.gvUser);
		// Grab all the Griddlers on local drive.
		// IE: The ones the user started on.
		// Also show the create a Griddler and Tutorial Griddler.
		int numGriddlers = 2; //+ SQLite stuff;
		names = new String[numGriddlers];
		diffs = new String[numGriddlers];
		rates = new String[numGriddlers];
		infos = new String[numGriddlers];
		names[0] = "Create";
		names[1] = "Tutorial";
		diffs[0] = "Custom";
		diffs[1] = "Easy";
		rates[0] = "We'll see";
		rates[1] = "0";
		infos[0] = "";
		infos[1] = "4 4 1111100110011111 0000000000000000";
		gv.setNumColumns(2);
		GriddlerMenuAdapter gma = new GriddlerMenuAdapter(this, names, diffs, rates, infos);
		gv.setAdapter(gma);
		gv.setOnTouchListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_user_griddlers, menu);
		return true;
	}

	public boolean onTouch(View v, MotionEvent me)
	{
		int pos = gv.pointToPosition((int) me.getX(), (int) me.getY());
		if(pos == 0)
		{
			//Start  Create.
			Intent createIntent = new Intent(this, CreateGriddlerActivity.class);
			this.startActivity(createIntent);
		}
		else
		{
			//Start game with info!
			Intent gameIntent = new Intent(this, GameActivity.class);
			gameIntent.putExtra("info", infos[pos]);
			this.startActivity(gameIntent);
		}
		return false;
	}
}
