package com.pic2griddler.awesomeness;

import com.google.analytics.tracking.android.EasyTracker;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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

	private int[][] current;
	private GridView grid;
	private int[][] solution;
	private int solutionOnes = 0, currentOnes = 0;
	private boolean[][] wasChanged;

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
		returnIntent.putExtra("ID", getSolution().hashCode() + "");
		setResult(2, returnIntent);
		finish();
	}

	private String getCurrent()
	{
		String temp = "";
		for (int i = 0; i < current.length; i++)
		{
			for (int j = 0; j < current[i].length; j++)
			{
				temp += current[i][j];
			}
		}
		return temp;
	}

	private String getSolution()
	{
		String temp = "";

		for (int i = 0; i < solution.length; i++)
		{
			for (int j = 0; j < solution[i].length; j++)
			{
				temp += solution[i][j];
			}
		}
		return temp;
	}

	@Override
	public void onBackPressed()
	{
		super.onPause();
		Intent returnIntent = new Intent();
		returnIntent.putExtra("current", getCurrent());
		returnIntent.putExtra("status", "0");
		returnIntent.putExtra("ID", getSolution().hashCode() + "");
		setResult(2, returnIntent);
		finish();
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

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // Full
																														// screen.
		EasyTracker.getInstance().setContext(this);
		setContentView(R.layout.activity_game);
		processInfo(this.getIntent().getExtras().getString("info"));
		grid = (GridView) findViewById(R.id.gvGame);
		grid.setNumColumns(current[0].length);
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
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_game, menu);
		return true;
	}

	@Override
	public void onRestoreInstanceState(Bundle in)
	{
		super.onRestoreInstanceState(in);
		if (in != null)
		{
			setGameFromString(in.getString("current"));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle out)
	{
		super.onSaveInstanceState(out);
		out.putString("current", this.getCurrent());
	}

	public boolean onTouch(View v, MotionEvent event)
	{
		if (event.getActionMasked() == MotionEvent.ACTION_MOVE)
		{
			// || event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			int pos = grid.pointToPosition((int) event.getX(), (int) event.getY());
			if (pos >= 0)
			{
				if (!wasChanged[pos / wasChanged[0].length][pos % wasChanged[0].length])
				{
					if (current[pos / current[0].length][pos % current[0].length] == 0)
					{
						grid.getChildAt(pos).setBackgroundColor(Color.RED);
						current[pos / current[0].length][pos % current[0].length] = 1;
						++currentOnes;
					}
					else
					{
						grid.getChildAt(pos).setBackgroundColor(Color.WHITE);
						current[pos / current[0].length][pos % current[0].length] = 0;
						--currentOnes;
					}
					if (currentOnes == solutionOnes)
					{
						checkWin();
					}
				}
				wasChanged[pos / wasChanged[0].length][pos % wasChanged[0].length] = true;
			}
		}
		if (event.getActionMasked() == MotionEvent.ACTION_UP)
		{
			resetWasChanged();
		}
		return false;
	}

	private void processInfo(String info)
	{
		String[] split = info.split(" ");
		int height = Integer.parseInt(split[0]);
		int width = Integer.parseInt(split[1]);
		current = new int[height][width];
		solution = new int[height][width];
		wasChanged = new boolean[height][width];
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
		for (int i = 0; i < solution.length; i++) // Like above, may want to
													// change to
		// actual array.
		{
			tv[i] = new TextView(this);
			String temp = "";
			int sum = 0;
			// Sum up each block of 1's.
			for (int j = 0; j < solution[i].length; j++) // Like above, may want
															// to change
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
		for (int i = 0; i < solution.length; i++) // Like above, may want to
													// change to
		// actual array.
		{
			int sum = 0;
			for (int j = 0; j < solution[i].length; j++) // Like above, may want
															// to change to
			// actual array.
			{
				if (solution[i][j] == 1)
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

	private void setGameFromString(String temp)
	{
		int run = 0;
		for (int i = 0; i < current.length; i++)
		{
			for (int j = 0; j < current[i].length; j++)
			{
				current[i][j] = temp.charAt(run++);
			}
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();
		EasyTracker.getInstance().activityStart(this); // Add this method.
	}

	@Override
	public void onStop()
	{
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.
	}
}
