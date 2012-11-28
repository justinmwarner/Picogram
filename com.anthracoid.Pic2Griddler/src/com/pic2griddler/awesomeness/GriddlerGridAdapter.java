package com.pic2griddler.awesomeness;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GriddlerGridAdapter extends BaseAdapter
{

	private int[][] solution, current;
	private Context context;

	public GriddlerGridAdapter(Context context, int[][] colors, int[][] current)
	{
		this.context = context;
		this.solution = colors;
		this.current = current;
	}

	public int getCount()
	{
		return solution.length * solution[0].length;
	}

	public Object getItem(int position)
	{
		int r = position / solution.length;
		int c = position % solution[0].length;
		return solution[c][r];
	}

	public long getItemId(int position)
	{
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		// This is expensive!!
		TextView text;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
		{
			text = (TextView) inflater.inflate(R.layout.griddler_grid_item, parent, false);
		}
		else
		{
			text = (TextView) convertView;
		}
		if (current[position / solution[0].length][position % solution[0].length] == 0)
		{
			text.setBackgroundColor(Color.WHITE);
		}
		else
		{
			text.setBackgroundColor(Color.RED);
		}
		text.setText("" + solution[position / solution[0].length][position % solution[0].length]);
		return text;
	}

}
