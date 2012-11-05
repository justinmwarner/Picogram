package com.pic2griddler.awesomeness;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends Activity implements OnClickListener, OnTouchListener
{

	private GridView grid;
	private int[][] solution;
	private int[][] current;
	private boolean[][] wasChanged;
	private int solutionOnes = 0, currentOnes = 0;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // Full
																														// screen.
		setContentView(R.layout.activity_game);
		processInfo(this.getIntent().getExtras().getString("info"));
		grid = (GridView) findViewById(R.id.gvGame);
		grid.setNumColumns(current.length);
		grid.setVerticalSpacing(1);
		grid.setHorizontalSpacing(1);
		GriddlerGridAdapter adapter = new GriddlerGridAdapter(this, solution, current);
		grid.setAdapter(adapter);
		grid.setOnTouchListener(this);

		Button up = (Button) findViewById(R.id.bUp);
		Button down = (Button) findViewById(R.id.bDown);
		up.setOnClickListener(this);
		down.setOnClickListener(this);
	}

	@Override
	public void onBackPressed()
	{
		super.onPause();
		Intent returnIntent = new Intent();
		returnIntent.putExtra("current", getCurrent());
		returnIntent.putExtra("status", "0");
		returnIntent.putExtra("ID", getSolution());
		setResult(2, returnIntent);
		finish();
	}

	private void processInfo(String info)
	{
		String[] split = info.split(" ");
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
				current[i][j] = Integer.parseInt("" + split[3].charAt(runner));
				solution[i][j] = Integer.parseInt("" + split[2].charAt(runner));
				wasChanged[i][j] = false;
				solutionOnes += solution[i][j];
				currentOnes += current[i][j];
				++runner;
			}
		}
		// For horizontal, a bit complicated.
		// Grab the LinearLayout that holds all the numbers.
		LinearLayout ll = (LinearLayout) findViewById(R.id.llHorizontalHolder);
		TextView[] tv = new TextView[width]; // Create an array of TextViews to
												// hold each column.
		for (int i = 0; i < width; i++) // Like above, may want to change to
										// actual array.
		{
			tv[i] = new TextView(this);
			String temp = "";
			int sum = 0;
			// Sum up each block of 1's.
			for (int j = 0; j < height; j++) // Like above, may want to change
												// to actual array.
			{
				if (solution[i][j] == 1)
				{
					sum++;
				}
				else
				{
					if (sum != 0)
					{
						temp += sum + "\n";
					}
					sum = 0;
				}
			}
			// Save last of that column, show 0 if it is a zero.
			if (temp.length() == 0)
			{
				temp += sum;
			}
			// Give some space between entries so it coordinates well.
			tv[i].setOnTouchListener(this);
			LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			llp.setMargins(5, 0, 5, 0); // llp.setMargins(left, top, right,
										// bottom);
			tv[i].setLayoutParams(llp);
			tv[i].setText(temp);
			ll.addView(tv[i]); // Add like a baowse.
		}
		// Now do the vertical, or the rows, has its own perks of using one
		// string and TextView.
		TextView vert = (TextView) findViewById(R.id.tvVertical);
		String temp = "\n";
		boolean hasMore = false, hasPrinted = false;
		for (int i = 0; i < height; i++) // Like above, may want to change to
											// actual array.
		{
			int sum = 0;
			for (int j = 0; j < width; j++) // Like above, may want to change to
											// actual array.
			{
				if (solution[j][i] == 1) // Going by row order, not column, so
											// i's and j's are backwards.
				{
					sum++;
					hasMore = true;
				}
				else
				{
					if (sum != 0)
					{
						temp += sum + " ";
						hasMore = false;
						hasPrinted = true;
					}
					sum = 0;
				}
			}

			if (hasMore)
			{
				temp += sum + "\n";
				hasPrinted = true;
			}
			if (!hasPrinted)
			{
				temp += "0\n\n";
			}
			if (!hasMore)
			{
				temp += "\n";
			}
			hasPrinted = false;
			hasMore = false;
		}
		vert.setText(temp);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_game, menu);
		return true;
	}

	public void onClick(View v)
	{
		if (v.getId() == R.id.bUp)
		{
			// Up vote.
		}
		else if (v.getId() == R.id.bDown)
		{
			// Down vote.
		}
	}

	public boolean onTouch(View v, MotionEvent event)
	{
		if (event.getActionMasked() == MotionEvent.ACTION_MOVE)
		{
			// || event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			int pos = grid.pointToPosition((int) event.getX(), (int) event.getY());
			if (pos >= 0)
			{
				if (!wasChanged[pos % wasChanged.length][pos / wasChanged.length])
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
		if (event.getActionMasked() == MotionEvent.ACTION_UP)
		{
			resetWasChanged();
		}
		return false;
	}

	private void resetWasChanged()
	{
		for (int i = 0; i < wasChanged.length; i++)
		{
			for (int j = 0; j < wasChanged[i].length; j++)
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
		// Win!
		Toast.makeText(this, "Congrats, you win.", Toast.LENGTH_SHORT).show();
		Intent returnIntent = new Intent();
		returnIntent.putExtra("current", getCurrent());
		returnIntent.putExtra("status", "1");
		returnIntent.putExtra("ID", getSolution());
		setResult(2, returnIntent);
		finish();
	}

	private String getCurrent()
	{
		String temp = "";
		for (int i = 0; i < current[0].length; i++)
		{
			for (int j = 0; j < current.length; j++)
			{
				temp += current[j][i];
			}
		}
		return temp;
	}
	
	private String getSolution()
	{
		String temp = "";

		for (int i = 0; i < solution[0].length; i++)
		{
			for (int j = 0; j < solution.length; j++)
			{
				temp += solution[j][i];
			}
		}
		return temp;
	}
}
