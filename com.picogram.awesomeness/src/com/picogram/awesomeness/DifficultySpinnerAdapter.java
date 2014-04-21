
package com.picogram.awesomeness;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DifficultySpinnerAdapter extends ArrayAdapter<String> {

	Context context;

	public DifficultySpinnerAdapter(Context context, int textViewResourceId,
			String[] objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent);
	}

	public View getCustomView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View row = inflater.inflate(R.layout.difficulty_spinner, parent, false);
		TextView label = (TextView) row.findViewById(R.id.tvSpinDifficulty);
		label.setText(getItem(position));

		ImageView icon = (ImageView) row.findViewById(R.id.ivDiffIcon);
		if (position == 0)
		{
			icon.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),
					R.drawable.easy));
		} else if (position == 1)
		{
			icon.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),
					R.drawable.medium));
		} else if (position == 2)
		{
			icon.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),
					R.drawable.hard));
		}
		return row;
	}
}
