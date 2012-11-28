package com.pic2griddler.awesomeness;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.GridView;
import android.widget.Toast;

public class UserGriddlers extends Activity implements OnTouchListener
{
	private String[] ids = null, statuses = null, names = null, diffs = null, rates = null, infos = null;
	private GridView gv;
	private final String FILENAME = "USER_GRIDDLERS", SETTINGS = "USER_SETTINGS";
	private SharedPreferences settings;
	private SharedPreferences.Editor edit;
	private GriddlerMenuAdapter gma;
	private SQLiteGriddlerAdapter sql;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_griddlers);
		gv = (GridView) findViewById(R.id.gvUser);
		// Grab all the Griddlers on local drive.
		// IE: The ones the user started on.
		// Also show the create a Griddler and Tutorial Griddler.
		sql = new SQLiteGriddlerAdapter(this.getApplicationContext(), "Griddlers", null, 1);

		loadGriddlers();
		gv.setNumColumns(2);
		gv.setOnTouchListener(this);

	}

	private void loadGriddlers()
	{
		String[][] griddlers = sql.getGriddlers(1);
		ids = new String[griddlers.length];
		statuses = new String[griddlers.length];
		names = new String[griddlers.length];
		diffs = new String[griddlers.length];
		rates = new String[griddlers.length];
		infos = new String[griddlers.length];
		for (int i = 0; i < griddlers.length; i++)
		{
			String temp[] = griddlers[i];
			ids[i] = temp[0];
			names[i] = temp[2];
			rates[i] = temp[3];
			infos[i] = temp[8] + " " + temp[7] + " " + temp[4] + " " + temp[5];
			diffs[i] = temp[6];
			if (temp[4].equals(temp[5]))
			{
				if (names[i].equals("Create a Griddler"))
				{
					statuses[i] = 2 + "";
				}
				else
				{
					statuses[i] = 1 + "";
				}
			}
			else
			{
				statuses[i] = 0 + "";
			}// statuses[i] = temp[9];
		}
		gma = new GriddlerMenuAdapter(this, statuses, names, diffs, rates, infos);
		gv.setAdapter(gma);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_user_griddlers, menu);
		return true;
	}

	public boolean onTouch(View v, MotionEvent me)
	{
		if (me.getAction() == MotionEvent.ACTION_UP)
		{
			int pos = gv.pointToPosition((int) me.getX(), (int) me.getY());
			if (pos >= 0)
			{
				if (pos == 0)
				{
					// Start Create.
					Intent createIntent = new Intent(this, CreateGriddlerActivity.class);
					sql.close();
					this.startActivityForResult(createIntent, 1);
					return false;
				}
				else
				{
					// Start game with info!
					Intent gameIntent = new Intent(this, GameActivity.class);
					sql.close();
					gameIntent.putExtra("info", infos[pos]);
					gameIntent.putExtra("id", ids[pos]);
					this.startActivityForResult(gameIntent, 2);
					return false;
				}
			}
			return false;
		}
		else
		{
			return false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// These could be compiled in to one, but for now, just keep it as is
		// for simplicity.
		if (resultCode == RESULT_OK)
		{
			// New Girddler, add to database.
			String id = data.getStringExtra("solution").hashCode() + "";
			String status = "0";
			String solution = data.getStringExtra("solution");
			String author = data.getStringExtra("author");
			String name = data.getStringExtra("name");
			String rank = data.getStringExtra("rank");
			String difficulty = data.getStringExtra("difficulty");
			String width = data.getStringExtra("width");
			String height = data.getStringExtra("height");
			sql.addUserGriddler(id, author, name, rank, solution, difficulty, width, height, status);
			loadGriddlers();
		}
		else if (resultCode == 2)
		{
			// Back button pushed.
			String id = data.getStringExtra("ID");
			String status = data.getStringExtra("status");
			String current = data.getStringExtra("current");
			sql.updateCurrentGriddler(id, status, current);
			loadGriddlers();
		}
		else
		{
			// Nothing added.
		}
	}
}
