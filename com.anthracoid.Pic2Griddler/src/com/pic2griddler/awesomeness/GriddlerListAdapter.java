package com.pic2griddler.awesomeness;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GriddlerListAdapter extends ArrayAdapter<Griddler>
{
	private static final String TAG = "GriddlerListAdapter";
	private Context context;
	ArrayList<Griddler> griddlers = new ArrayList<Griddler>();

	public GriddlerListAdapter(Context context, int textViewResourceId)
	{
		super(context, textViewResourceId);
	}

	@Override
	public int getCount()
	{
		return griddlers.size();
	}

	public void setGriddlers(ArrayList<Griddler> g)
	{
		griddlers = g;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		// This is expensive!!
		View item;
		if (context == null)
		{
			context = this.getContext();
		}
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
		{
			item = inflater.inflate(R.layout.griddler_menu_choice_item, parent, false);
		}
		else
		{
			item = convertView;
		}
		TextView rate = (TextView) item.findViewById(R.id.tvRating);
		TextView diff = (TextView) item.findViewById(R.id.tvDiff);
		TextView name = (TextView) item.findViewById(R.id.tvName);
		ImageView iv = (ImageView) item.findViewById(R.id.ivCurrent);
		iv.setVisibility(0);
		int height = Integer.parseInt(griddlers.get(position).getInfo().split(" ")[0]);
		int width = Integer.parseInt(griddlers.get(position).getInfo().split(" ")[1]);
		String curr = griddlers.get(position).getInfo().split(" ")[3];
		int run = 0;
		if (height > 0 && width > 0)
		{
			Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
			bm = Bitmap.createScaledBitmap(bm, width, height, true);
			for (int i = 0; i < height; i++)
			{
				for (int j = 0; j < width; j++)
				{

					if (curr.charAt(run) == '0')
					{
						bm.setPixel(j, i, Color.WHITE);
					}
					else
					{
						bm.setPixel(j, i, Color.BLACK);
					}
					run++;
				}
			}
			bm = Bitmap.createScaledBitmap(bm, 100, 100, false);
			iv.setImageBitmap(bm);
			iv.setVisibility(1);
		}
		rate.setText("Rating: " + griddlers.get(position).getRank());
		diff.setText("Difficulty: " + griddlers.get(position).getDiff());
		name.setText(griddlers.get(position).getName());
		// Change color if user has beaten level.
		int status = 0;
		try
		{
			status = Integer.parseInt(griddlers.get(position).getStatus());
		}
		catch (NumberFormatException e)
		{
			// Log.d("Tag", "Pooped on: " + statuses[position]);// +
			// " at position " + position);
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
		((ViewGroup) item).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		return item;
	}
}
