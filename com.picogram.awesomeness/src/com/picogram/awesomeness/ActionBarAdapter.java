
package com.picogram.awesomeness;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ActionBarAdapter extends BaseAdapter {

	// Declare Variables
	Context context;
	String[] mTitle;
	String[] mSubTitle;
	int[] mIcon;
	LayoutInflater inflater;

	public ActionBarAdapter(final Context context, final String[] title, final String[] subtitle,
			final int[] icon) {
		this.context = context;
		this.mTitle = title;
		this.mSubTitle = subtitle;
		this.mIcon = icon;
	}

	public int getCount() {
		return this.mTitle.length;
	}

	@Override
	public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {
		// Declare Variables
		TextView txtTitle;
		TextView txtSubTitle;
		ImageView imgIcon;

		this.inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View dropdownView = this.inflater.inflate(R.layout.ab_dropdown_item, parent,
				false);

		// Locate the TextViews in nav_dropdown_item.xml
		txtTitle = (TextView) dropdownView.findViewById(R.id.title);
		txtSubTitle = (TextView) dropdownView.findViewById(R.id.subtitle);

		// Locate the ImageView in nav_dropdown_item.xml
		imgIcon = (ImageView) dropdownView.findViewById(R.id.icon);

		// Set the results into TextViews
		txtTitle.setText(this.mTitle[position]);
		txtSubTitle.setText(this.mSubTitle[position]);

		if (this.mIcon == null) {
			imgIcon.setVisibility(View.GONE);
		} else {
			// Set the results into ImageView
			imgIcon.setImageResource(this.mIcon[position]);
		}

		return dropdownView;
	}

	public Object getItem(final int position) {
		return this.mTitle[position];
	}

	public long getItemId(final int position) {
		return position;
	}

	public View getView(final int position, final View convertView, final ViewGroup parent) {
		// Declare Variables
		TextView txtTitle;
		TextView txtSubTitle;

		this.inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View itemView = this.inflater.inflate(R.layout.ab_list_item, parent, false);

		// Locate the TextViews in nav_list_item.xml
		txtTitle = (TextView) itemView.findViewById(R.id.title);
		txtTitle.setTypeface(Typeface.createFromAsset(this.context.getAssets(), "fonts/Xolonium-Regular.otf"));
		txtSubTitle = (TextView) itemView.findViewById(R.id.subtitle);

		// Set the results into TextViews
		txtTitle.setText(this.mTitle[position]);
		txtSubTitle.setText(this.mSubTitle[position]);

		return itemView;
	}
}
