
package com.picogram.awesomeness;

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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class PicogramListAdapter extends ArrayAdapter<Picogram> {
	static class ViewHolder{
		TextView name, diff, rate;
		ImageView pic;
		RelativeLayout rl;
		FrameLayout fl;
		Picogram p;
	}
	private static final String TAG = "PicogramListAdapter";
	private Context context;

	ArrayList<Picogram> picograms = new ArrayList<Picogram>();

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
		final LayoutInflater inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// TODO Implement viewholder: http://gmariotti.blogspot.com/2013/06/tips-for-listview-view-recycling-use.html
		ViewHolder holder;
		final Picogram picogram = this.picograms.get(position);
		Log.d(TAG, "ADD: " + picogram.getName());
		String first = "";
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

			first = holder.name.getText().toString();
			// picogram = holder.p;
		}

		picogram.nullsToValue(this.context);// Just reset all nulls to a value.
		holder.pic.setVisibility(0);
		int height;
		try {
			height = Integer.parseInt(picogram.getHeight());
		} catch (final Exception e) {
			height = 0;
		}
		int width;
		try {
			width = Integer.parseInt(picogram.getWidth());
		} catch (final Exception e) {
			width = 0;
		}
		String curr = picogram.getCurrent();
		int run = 0;
		if (curr == null) {
			curr = "";
			for (int i = 0; i != picogram.getSolution().length(); ++i) {
				curr += "0";
			}
		}
		Bitmap bm;
		if ((height > 0) && (width > 0)) {
			bm = BitmapFactory.decodeResource(this.context.getResources(),
					R.drawable.ic_launcher);
			bm = Bitmap.createScaledBitmap(bm, width, height, true);
			final String cols = picogram.getColors();
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
						bm.setPixel(j, i, Color.WHITE);
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
			if (picogram.getName().contains("Pack")) {
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
		bm = Bitmap.createScaledBitmap(bm, 100, 100, false);
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
		final Drawable gd = holder.rl.getBackground().mutate();
		convertView.setBackgroundResource(R.drawable.picogram_menu_choice_border_red);
		holder.rl.setBackgroundResource(R.drawable.picogram_menu_choice_border_red);
		if (picogram.getCurrent() == null) {
			picogram.setCurrent("");
		}
		String rate = picogram.getRating() + "";
		if (status == 2) {
			// Other (Custom, special levels, etc.).
			// convertView.setBackgroundResource(R.drawable.picogram_menu_choice_border_other);
			holder.rl.setBackgroundResource(R.drawable.picogram_menu_choice_border_other);
			// Also change difficulty to w X h X c
			holder.diff.setText("w X h X c");
			// Erase rate
			// holder.rl.setBackgroundColor(Color.YELLOW);
			rate = "You decide ;)";
		} else if ((status == 1)
				|| this.picograms.get(position).getSolution().replaceAll("x", "0")
				.equals(picogram.getCurrent().replaceAll("x", "0"))) {
			// Won.
			// convertView.setBackgroundResource(R.drawable.picogram_menu_choice_border_green);
			holder.rl.setBackgroundResource(R.drawable.picogram_menu_choice_border_green);
			// holder.rl.setBackgroundColor(Color.GREEN);
		} else {
			// In progress.
			// convertView.setBackgroundResource(R.drawable.picogram_menu_choice_border_red);
			holder.rl.setBackgroundResource(R.drawable.picogram_menu_choice_border_red);
			// holder.rl.setBackgroundColor(Color.RED);
		}
		holder.diff.setText(width + " X " + height + " X " + picogram.getNumberOfColors());
		holder.name.setText(picogram.getName());
		holder.rate.setText(rate);
		holder.rl.invalidate();
		convertView.invalidate();
		// gd.invalidateSelf();
		// convertView.invalidate();
		// holder.rl.invalidate();
		// ((ViewGroup) convertView)
		// .setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		// Log.d(TAG, "ADD " + first + " : " +
		// holder.name.getText().toString());
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
}
