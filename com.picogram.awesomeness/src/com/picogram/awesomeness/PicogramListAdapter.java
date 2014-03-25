
package com.picogram.awesomeness;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class PicogramListAdapter extends ArrayAdapter<Picogram> {
	static class ViewHolder {
		TextView name, diff, rate;
		ImageView pic;
		RelativeLayout rl;
		FrameLayout fl;
	}

	private static final String TAG = "PicogramListAdapter";
	private Context context;

	ArrayList<Picogram> picograms = new ArrayList<Picogram>();
	ArrayList<Picogram> myPicograms = new ArrayList<Picogram>();
	ArrayList<Picogram> topPicograms = new ArrayList<Picogram>();
	ArrayList<Picogram> recentPicograms = new ArrayList<Picogram>();

	IntHolder ih;
	public PicogramListAdapter(final Context context,
			final int textViewResourceId) {
		super(context, textViewResourceId);
	}
	public PicogramListAdapter(final Context context, final int resource,
			final ArrayList<Picogram> items) {
		super(context, resource, items);
		this.context = context;
		this.picograms = items;
	}

	@Override
	public void add(final Picogram object) {
		super.add(object);
		this.picograms.add(object);
	}

	public void addMy(final Picogram p)
	{
		this.myPicograms.add(p);
	}

	public void addRecent(final Picogram p)
	{
		this.recentPicograms.add(p);
	}

	public void addTop(final Picogram p)
	{
		this.topPicograms.add(p);
	}

	@Override
	public void clear() {
		super.clear();
		this.picograms.clear();
	}

	public boolean existsById(final String id) {
		for (final Picogram g : this.picograms) {
			if ((g.getID() != null)) {
				if (g.getID().equals(id)) {
					return true;
				}
			}
		}
		return false;
	}

	public Picogram get(final int pos) {
		return this.picograms.get(pos);
	}

	private Bitmap getBitmapFromCurrent(final Picogram p, final int position) {
		Bitmap bm;
		int height;
		try {
			height = Integer.parseInt(p.getHeight());
		} catch (final Exception e) {
			height = 0;
		}
		int width;
		try {
			width = Integer.parseInt(p.getWidth());
		} catch (final Exception e) {
			width = 0;
		}
		String curr = p.getCurrent();
		int run = 0;
		if ((height > 0) && (width > 0)) {
			bm = BitmapFactory.decodeResource(this.context.getResources(),
					R.drawable.ic_launcher);
			bm = Bitmap.createScaledBitmap(bm, width, height, true);
			final String cols = p.getColors();
			String[] colors = null;
			if (cols.contains(",")) {
				colors = cols.split(",");
			} else if (cols.contains(" ")) {
				colors = cols.split(" ");
			}
			else {
				colors = new String[] {
						"000000"
				};// Problem.
			}

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					if (curr.length() != (height * width)) {
						// Online or something, just default to white.
						curr += "0";
					}
					if (curr.charAt(run) == 'x') {
						bm.setPixel(j, i, Color.TRANSPARENT);
					} else {
						bm.setPixel(
								j,
								i,
								Integer.parseInt(colors[Integer.parseInt(""
										+ curr.charAt(run))]));
					}
					run++;
				}
			}
		} else {
			if (p.getName().contains("Pack")) {
				// If we're a pack.
				bm = BitmapFactory.decodeResource(this.context.getResources(),
						R.drawable.ic_launcher);
			} else {
				if (position == 0) {
					bm = BitmapFactory.decodeResource(
							this.context.getResources(), R.drawable.create);
				} else if (position == 1) {
					bm = BitmapFactory.decodeResource(
							this.context.getResources(), R.drawable.random);
				} else {
					// Just show the icon if something is wrong =/.
					bm = BitmapFactory
							.decodeResource(this.context.getResources(),
									R.drawable.ic_launcher);
				}
			}
			bm = Bitmap.createScaledBitmap(bm, 100, 100, true);
		}
		if (height == 0) {
			height = 10;
		}
		if (width == 0) {
			width = 10;
		}
		bm = Bitmap.createScaledBitmap(bm, width * 10, height * 10, false);
		return bm;
	}

	@Override
	public int getCount() {
		return this.picograms.size();
	}

	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {
		if (this.context == null) {
			this.context = this.getContext();
		}
		final LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// TODO Implement viewholder: http://gmariotti.blogspot.com/2013/06/tips-for-listview-view-recycling-use.html
		ViewHolder holder;
		final Picogram picogram = this.picograms.get(position);
		if (convertView == null)
		{
			convertView = inflater.inflate(R.layout.picogram_menu_choice_item,
					parent, false);
			holder = new ViewHolder();
			holder.diff = (TextView) convertView.findViewById(R.id.tvDiff);
			holder.name = (TextView) convertView.findViewById(R.id.tvName);
			holder.rate = (TextView) convertView.findViewById(R.id.tvRating);
			holder.pic = (ImageView) convertView.findViewById(R.id.ivCurrent);
			holder.rl = (RelativeLayout) convertView.findViewById(R.id.rlMenuHolder);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		picogram.nullsToValue(this.context);// Just reset all nulls to a value.
		final String curr = picogram.getCurrent();
		final Bitmap bm = this.getBitmapFromCurrent(picogram, position);
		holder.pic.setImageBitmap(bm);
		holder.pic.setVisibility(View.VISIBLE);
		if (picogram.getNumberOfRatings() != 0) {
			final String rateText = "Rating: "
					+ picogram.getRating();
			final int index = rateText.indexOf('.');
			if (index != -1) {
				if (rateText.length() > (index + 3)) {
					holder.rate.setText(rateText.substring(0,
							rateText.indexOf('.') + 3));
				} else {
					holder.rate.setText(rateText.substring(0,
							rateText.indexOf('.') + 2));
				}
			} else {
				holder.rate.setText(rateText);
			}
		}
		// Change color if user has beaten level.
		int status = 0;
		try {
			status = Integer.parseInt(picogram.getStatus());
		} catch (final NumberFormatException e) {
		}
		// convertView.setBackgroundResource(R.drawable.picogram_menu_choice_border_red);
		holder.rl.setBackgroundDrawable(this.context.getResources().getDrawable(R.drawable.picogram_menu_choice_border_red));
		holder.rl.setBackgroundResource(R.drawable.picogram_menu_choice_border_red);
		if (picogram.getCurrent() == null) {
			picogram.setCurrent("");
		}
		final String diff = (picogram.getWidth() + " X " + picogram.getHeight() + " X " + picogram.getNumberOfColors());
		String rate = picogram.getRating() + "";
		if (status == 2) {
			// Other (Custom, special levels, etc.).
			// holder.rl.setBackgroundResource(R.drawable.picogram_menu_choice_border_other);
			holder.rl.setBackgroundDrawable(this.context.getResources().getDrawable(R.drawable.picogram_menu_choice_border_other));
			holder.diff.setText("w X h X c");
			rate = "You decide ;)";
		} else if ((status == 1)
				|| this.picograms.get(position).getSolution().replaceAll("x", "0")
				.equals(picogram.getCurrent().replaceAll("x", "0"))) {
			// Won.
			holder.rl.setBackgroundDrawable(this.context.getResources().getDrawable(R.drawable.picogram_menu_choice_border_green));// .setBackgroundResource(R.drawable.picogram_menu_choice_border_green);
		} else {
			// In progress.
			holder.rl.setBackgroundDrawable(this.context.getResources().getDrawable(R.drawable.picogram_menu_choice_border_red));
			// holder.rl.setBackgroundResource(R.drawable.picogram_menu_choice_border_red);
		}
		holder.diff.setText(diff);
		holder.name.setText(picogram.getName());
		holder.rate.setText(rate);
		holder.rl.invalidate();
		convertView.invalidate();
		return convertView;
	}

	public void removeById(final String id) {
		for (int i = 0; i != this.picograms.size(); ++i) {
			if (this.picograms.get(i).getID().equals(id)) {
				this.picograms.remove(i);
				return;
			}
		}
	}

	public void setPicograms(final ArrayList<Picogram> g) {
		this.picograms = g;
	}

	public void updateCurrentById(final String id, final String newCurrent, final String status) {
		for (int i = 0; i != this.picograms.size(); ++i) {
			if (this.picograms.get(i).getID().equals(id)) {
				final Picogram go = this.picograms.get(i);
				go.setCurrent(newCurrent);
				go.setStatus(status);
				this.picograms.set(i, go);
				return;
			}
		}
	}

	public void updateRateById(final String id, final int newRate)
	{
		final SQLitePicogramAdapter sql = new SQLitePicogramAdapter(this.context, "Picograms",
				null, 1);
		for (int i = 0; i != this.picograms.size(); ++i) {
			if (this.picograms.get(i).getID().equals(id))
			{
				final Picogram p = this.picograms.get(i);
				p.setRating(newRate + "");
				this.picograms.set(i, p);
				sql.updateRate(id, newRate);
				// this.picograms.remove(i);
				// this.picograms.add(p);
				// Log.d(TAG, "UPDATING");
				this.notifyDataSetChanged();
				return;
			}
		}
		sql.close();
	}
}
