package com.picogram.awesomeness;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PartSpinnerAdapter extends ArrayAdapter<String> implements SpinnerAdapter {

	private static final String TAG = "PartSpinnerAdapter";
	ArrayList<PicogramPart> parts = new ArrayList<PicogramPart>();
	Context context;


	public PartSpinnerAdapter(final Context ctx, final int txtViewResourceId, final PicogramPart[] picogramParts) {
		super(ctx, txtViewResourceId);
		this.context = ctx;
		for (final PicogramPart part : picogramParts) {
			this.parts.add(part);
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return this.parts.size();
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
		if (this.parts.get(position).getCurrent().replaceAll("x|X", "0").equals(this.parts.get(position).getSolution()))
		{
			tv.setText("\u2714 Part " + position);
			tv.setTextColor(this.context.getResources().getColor(R.color.good));
		} else if (this.parts.get(position).getCurrent().replaceAll("[^0]", "0").equals(this.parts.get(position).getCurrent())) {
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
