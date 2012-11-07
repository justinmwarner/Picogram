package com.pic2griddler.awesomeness;

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
		try
		{
			loadGriddlers();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		gv.setNumColumns(2);
		gv.setOnTouchListener(this);
	}

	private void loadGriddlers() throws IOException
	{
		/*
		 * settings = getSharedPreferences(SETTINGS, 0); edit = settings.edit();
		 * // Get preferences to see if user ran app before. boolean isVirgin =
		 * settings.getBoolean("virgin", true); // Put in some defaults if
		 * they've never ran the app before. if (isVirgin) { String add =
		 * "2 Create Custom We'll~see 0 0 0 0," +
		 * System.getProperty("line.separator") +
		 * "1 Tutorial Easy 0 4 4 1111100110011111 1110011001100111," +
		 * System.getProperty("line.separator"); add +=
		 * "1 Test Easy 0 5 3 111101000000000 111111111111111," +
		 * System.getProperty("line.separator"); FileOutputStream fos =
		 * openFileOutput(FILENAME, Context.MODE_WORLD_WRITEABLE);
		 * fos.write(add.getBytes()); fos.close(); edit.putBoolean("virgin",
		 * false); edit.commit(); } // Now read the file to get all the
		 * Griddlers from it. FileInputStream fis = openFileInput(FILENAME);
		 * StringBuffer sb = new StringBuffer(""); byte[] buffer = new
		 * byte[1024]; int length = 0; while ((length = fis.read(buffer)) != -1)
		 * { sb.append(new String(buffer)); } fis.close(); // Now process the
		 * file. String[] griddlers = sb.toString().split(","); // Because of
		 * buffer size, we want to ignore anything "Extra", so // length-1 will
		 * do. ids = new String[griddlers.length - 1]; statuses = new
		 * String[griddlers.length - 1]; names = new String[griddlers.length -
		 * 1]; diffs = new String[griddlers.length - 1]; rates = new
		 * String[griddlers.length - 1]; infos = new String[griddlers.length -
		 * 1]; for (int i = 0; i < griddlers.length - 1; i++) { String temp[] =
		 * griddlers[i].split(" "); statuses[i] = temp[0]; names[i] =
		 * temp[1].replace("~", " "); diffs[i] = temp[2]; rates[i] =
		 * temp[3].replace("~", " "); infos[i] = temp[4] + " " + temp[5] + " " +
		 * temp[6] + " " + temp[7]; ids[i] = temp[6].hashCode() + ""; // ID is
		 * based on solutions hash // code. }
		 */
		String[] griddlers = sql.getGriddlers(1);
		Log.d("TAG", griddlers.length +"");
		ids = new String[griddlers.length];
		statuses = new String[griddlers.length];
		names = new String[griddlers.length];
		diffs = new String[griddlers.length];
		rates = new String[griddlers.length];
		infos = new String[griddlers.length];
		for (int i = 0; i < griddlers.length; i++)
		{
			String temp[] = griddlers[i].split(" ");
			ids[i] = temp[0];
			names[i] = temp[2];
			rates[i] = temp[3];
			infos[i] = temp[8] + " " + temp[7] + " " + temp[4] + " " + temp[5];
			diffs[i] = temp[6];
			statuses[i] = temp[9];
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
					this.startActivityForResult(createIntent, 1);
					return false;
				}
				else
				{
					// Start game with info!
					Intent gameIntent = new Intent(this, GameActivity.class);
					gameIntent.putExtra("info", infos[pos]);
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
		if (resultCode == RESULT_OK)
		{
			// Add to the file.
			String info = data.getStringExtra("info");
			try
			{

				FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_APPEND);
				fos.write(info.getBytes());
				fos.close();
				loadGriddlers();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else if (resultCode == 2)
		{
			// Game activitiy.
			String current = data.getStringExtra("current");
			String status = data.getStringExtra("status");
			String solve = data.getStringExtra("ID");
			// Alter file and change it.

			// Update views for new colors.
		}
		else
		{
			// Nothing added.
		}
	}
}
