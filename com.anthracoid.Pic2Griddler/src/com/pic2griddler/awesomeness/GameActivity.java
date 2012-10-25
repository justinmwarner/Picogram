package com.pic2griddler.awesomeness;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

public class GameActivity extends Activity implements OnClickListener, OnTouchListener
{

	private GridView	grid;
	int[][]				solution;
	int[][]				current;
	boolean[][]			wasChanged;
	int					solutionOnes	= 0, currentOnes = 0;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		processInfo(this.getIntent().getExtras().getString("info"));
		Button b1 = (Button) findViewById(R.id.bColorOne);
		Button b2 = (Button) findViewById(R.id.bColorTwo);
		grid = (GridView) findViewById(R.id.gvGame);
		grid.setNumColumns(current.length);
		grid.setVerticalSpacing(1);
		grid.setHorizontalSpacing(1);
		GridAdapter adapter = new GridAdapter(this, solution, current);
		grid.setAdapter(adapter);
		grid.setOnTouchListener(this);

	}

	private void processInfo(String temp)
	{
		String[] split = temp.split(" ");
		int height = Integer.parseInt(split[0]);
		int width = Integer.parseInt(split[1]);
		current = new int[width][height];
		solution = new int[width][height];
		wasChanged = new boolean[width][height];
		int runner = 0;
		for (int i = 0; i < current.length; i++)
		{
			for (int j = 0; j < current[i].length; j++)
			{
				Log.d("Tag", "Filling in " + i + " " + j);
				current[i][j] = Integer.parseInt("" + split[3].charAt(runner));
				solution[i][j] = Integer.parseInt("" + split[2].charAt(runner));
				wasChanged[i][j] = false;
				solutionOnes += solution[i][j];
				currentOnes += current[i][j];
				++runner;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_game, menu);
		return true;
	}

	public void onClick(View v)
	{}

	public boolean onTouch(View v, MotionEvent event)
	{
		if (event.getActionMasked() == MotionEvent.ACTION_MOVE || event.getActionMasked() == MotionEvent.ACTION_DOWN)
		{
			int pos = grid.pointToPosition((int) event.getX(), (int) event.getY());
			if (pos >= 0)
			{
				if(!wasChanged[pos % wasChanged.length][pos / wasChanged.length])
				{
					if (current[pos % current.length][pos / current.length] == 0)
					{
						grid.getChildAt(pos).setBackgroundColor(Color.RED);
						current[pos % current.length][pos / current.length] = 1;
						++currentOnes;
					}
					else
					{
						grid.getChildAt(pos).setBackgroundColor(Color.WHITE);
						current[pos % current.length][pos / current.length] = 0;
						--currentOnes;
					}
					if (currentOnes == solutionOnes)
					{
						checkWin();
					}
				}
				wasChanged[pos % wasChanged.length][pos / wasChanged.length] = true;
			}
		}
		if(event.getActionMasked() == MotionEvent.ACTION_UP)
		{
			resetWasChanged();
		}
		return false;
	}

	private void resetWasChanged()
	{
		for(int i = 0; i < wasChanged.length; i++)
		{
			for(int j = 0; j < wasChanged[i].length; j++)
			{
				wasChanged[i][j] = false;
			}
		}
	}

	private void checkWin()
	{
		for (int i = 0; i < current.length; i++)
		{
			for (int j = 0; j < current[i].length; j++)
			{
				if (current[i][j] != solution[i][j])
				{
					return;
				}
			}
		}
		//Win!
		Toast.makeText(this, "Congrats, you win.", Toast.LENGTH_SHORT).show();
	}
}
