package com.pic2griddler.awesomeness;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class GriddlerMenuAdapter extends BaseAdapter
{
	private Context context;
	private String[] names, difficulties, ratings, infos;

	public GriddlerMenuAdapter(Context context, String[] n, String[] d, String[] r, String[] i)
	{
		this.context = context;
		this.names = n;
		this.difficulties = d;
		this.ratings = r;
		this.infos = i;
	}

	public int getCount()
	{
		return names.length;
	}

	public Object getItem(int pos)
	{
		return pos;
	}

	public long getItemId(int arg0)
	{
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		View item;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
		{
			item = inflater.inflate(R.layout.griddler_menu_choice_item, parent, false);
		}
		else
		{
			item = convertView;
		}
		TextView rate = (TextView) item.findViewById(R.id.tvRating), diff = (TextView) item.findViewById(R.id.tvDiff), name = (TextView) item.findViewById(R.id.tvName);
		Button play = (Button) item.findViewById(R.id.bPlay);
		rate.setText(this.ratings[position]);
		diff.setText(this.difficulties[position]);
		name.setText(this.names[position]);
		play.setText("Play " + this.names[position]);

		return item;
	}

}
