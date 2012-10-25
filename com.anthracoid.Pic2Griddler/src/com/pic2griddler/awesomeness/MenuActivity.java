package com.pic2griddler.awesomeness;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableRow.LayoutParams;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

public class MenuActivity extends Activity implements OnClickListener
{
	
	ArrayList<Button> buttons = new ArrayList<Button>();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		TableLayout tl = (TableLayout) findViewById(R.id.TLMain);
		Button create = (Button) findViewById(R.id.bCreate);
		buttons.add(create);
		TableRow tr = new TableRow(this);
		tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		Button b = new Button(this);
		b.setText("Dynamic Button");
		b.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		tr.addView(b);
		tl.addView(tr, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		buttons.add(b);
		for(int i = 0; i < buttons.size(); i++)
		{
			buttons.get(i).setOnClickListener(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}

	public void onClick(View v)
	{
		if(v.getId() == R.id.bCreate)
		{
			//Creating a new Griddler.
			Intent createIntent = new Intent(this, CreateGriddlerActivity.class);
			startActivity(createIntent);
		}
		else
		{
			String temp = "5 3 111111011011111 111111011011000";
			Intent myIntent = new Intent(this, GameActivity.class);
			myIntent.putExtra("info", temp);
			startActivity(myIntent);
		}
	}
}
