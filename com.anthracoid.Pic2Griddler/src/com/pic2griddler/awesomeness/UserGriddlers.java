package com.pic2griddler.awesomeness;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.os.Bundle;
import android.app.Activity;
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
	String[] statuses = null, names = null, diffs = null, rates = null, infos = null;
	GridView gv;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_griddlers);
		// Grab all the Griddlers on local drive.
		// IE: The ones the user started on.
		// Also show the create a Griddler and Tutorial Griddler.
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
		gv = (GridView) findViewById(R.id.gvUser);
		gv.setNumColumns(2);
		GriddlerMenuAdapter gma = new GriddlerMenuAdapter(this, statuses, names, diffs, rates, infos);
		gv.setAdapter(gma);
		gv.setOnTouchListener(this);
	}

	private void loadGriddlers() throws IOException
	{
		final String FILENAME = "USER_GRIDDLERS", SETTINGS = "USER_SETTINGS";
		// Get preferences to see if user ran app before.
		SharedPreferences settings = getSharedPreferences(SETTINGS, 0);
		boolean isVirgin = settings.getBoolean("virgin", true);
		// Put in some defaults if they've never ran the app before.
		if (isVirgin)
		{
			String add = "2 Create Custom We'll~see 0 0 0 0," + System.getProperty("line.separator") + "0 Tutorial Easy 0 4 4 1111100110011111 0000000000000000," + System.getProperty("line.separator");
			FileOutputStream fos = openFileOutput(FILENAME, this.MODE_PRIVATE);
			fos.write(add.getBytes());
			fos.close();
		}
		// Now read the file to get all the Griddlers from it.
		FileInputStream fis = openFileInput(FILENAME);
		StringBuffer sb = new StringBuffer("");
		byte[] buffer = new byte[1024];
		int length = 0;
		while ((length = fis.read(buffer)) != -1)
		{
			sb.append(new String(buffer));
		}
		fis.close();
		// Now process the file.
		String[] griddlers = sb.toString().split(",");
		// Because of buffer size, we want to ignore anything "Extra", so
		// length-1 will do.
		statuses = new String[griddlers.length - 1];
		names = new String[griddlers.length - 1];
		diffs = new String[griddlers.length - 1];
		rates = new String[griddlers.length - 1];
		infos = new String[griddlers.length - 1];
		for (int i = 0; i < griddlers.length - 1; i++)
		{
			String temp[] = griddlers[i].split(" ");
			statuses[i] = temp[0];
			names[i] = temp[1].replace("~", " ");
			diffs[i] = temp[2];
			rates[i] = temp[3].replace("~", " ");
			infos[i] = temp[4] + " " + temp[5] + " " + temp[6] + " " + temp[7];
		}
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
					this.startActivity(createIntent);
					return false;
				}
				else
				{
					// Start game with info!
					Intent gameIntent = new Intent(this, GameActivity.class);
					gameIntent.putExtra("info", infos[pos]);
					this.startActivity(gameIntent);
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
}
