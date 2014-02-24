package com.picogram.awesomeness;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PartSpinnerAdapter extends ArrayAdapter<String> implements SpinnerAdapter {

	private static final String TAG = "PartSpinnerAdapter";
	ArrayList<String> currents = new ArrayList<String>(), solutions = new ArrayList<String>();
	Context context;


	public PartSpinnerAdapter(final Context ctx, final int txtViewResourceId, final String[] objects) {
		super(ctx, txtViewResourceId, objects);
		this.context = ctx;
		boolean isSolution = false;
		for (final String obj : objects) {
			Log.d(TAG, "OBJ: " + obj);
			if(obj.isEmpty())
			{
				isSolution = true;
				continue;
			}
			if (isSolution) {
				this.solutions.add(obj);
			}
			else
			{
				this.currents.add(obj);
			}
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.currents.size();
	}
	@Override
	public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {
		return this.getView(position, convertView, parent);
	}

	@Override
	public String getItem(final int position) {
		// TODO Auto-generated method stub
		return super.getItem(position);
	}

	@Override
	public long getItemId(final int position) {
		// TODO Auto-generated method stub
		return super.getItemId(position);
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final TextView tv = new TextView(this.context);
		if (this.currents.get(position).replaceAll("x|X", "0").equals(this.solutions.get(position)))
		{
			tv.setText("\u2714 Part " + position);
			tv.setTextColor(this.context.getResources().getColor(R.color.good));
		} else if (this.currents.get(position).replaceAll("[^0]", "0").equals(this.currents.get(position))) {
			//All 0's
			tv.setText("\u2716 Part " + position);
			tv.setTextColor(this.context.getResources().getColor(R.color.bad));
		}
		else
		{
			// User has done some messing around with this part.
			tv.setText("\u2715 Part " + position);
			tv.setTextColor(this.context.getResources().getColor(R.color.special));
		}
		tv.setGravity(Gravity.CENTER);
		return tv;
	}

}
