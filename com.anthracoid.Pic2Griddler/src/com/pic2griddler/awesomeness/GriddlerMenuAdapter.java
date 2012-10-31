package com.pic2griddler.awesomeness;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GriddlerMenuAdapter extends BaseAdapter
{
	private Context context;
	private String[] statuses, names, difficulties, ratings, infos;

	public GriddlerMenuAdapter(Context context, String[] s, String[] n, String[] d, String[] r, String[] i)
	{
		this.statuses = s;
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
		Button play = (Button) item.findViewById(R.id.bPlay); // Invisible, not
																// yet in use
																// (If ever!)
		rate.setText("Rating: " + this.ratings[position]);
		diff.setText("Difficulty: " + this.difficulties[position]);
		name.setText(this.names[position]);
		play.setText("Play " + this.names[position]);
		// Change color if user has beaten level.
		int status = 0;
		try
		{
			status = Integer.parseInt(statuses[position]);
		}
		catch (NumberFormatException e)
		{
			//Log.d("Tag", "Pooped on: " + statuses[position]);// + " at position " + position);
		}
		RelativeLayout rl = (RelativeLayout) item.findViewById(R.id.rlMenuHolder);
		Drawable gd = rl.getBackground().mutate();
		if (status == 0)
		{
			// In progress.
			rl.setBackgroundResource(R.drawable.griddler_menu_choice_border_red);

		}
		else if (status == 1)
		{
			// Won.
			rl.setBackgroundResource(R.drawable.griddler_menu_choice_border_green);
		}
		else
		{
			// Other (Custom, special levels, etc.).
			rl.setBackgroundResource(R.drawable.griddler_menu_choice_border_other);
		}
		gd.invalidateSelf();
		return item;
	}

}
