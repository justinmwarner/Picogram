
package com.picogram.awesomeness;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class PicogramListAdapter extends ArrayAdapter<GriddlerOne> {
	private static final String TAG = "PicogramListAdapter";
	private Context context;
	ArrayList<GriddlerOne> picograms = new ArrayList<GriddlerOne>();

	public PicogramListAdapter(final Context context,
			final int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public PicogramListAdapter(final Context context, final int resource,
			final ArrayList<GriddlerOne> items) {
		super(context, resource, items);
		this.context = context;
		this.picograms = items;
	}

	@Override
	public void add(final GriddlerOne object) {
		super.add(object);
		this.picograms.add(object);
	}

	@Override
	public void clear() {
		super.clear();
		this.picograms.clear();
	}

	public boolean existsById(final String id) {
		for (final GriddlerOne g : this.picograms) {
			if (g.getID().equals(id)) {
				return true;
			}
		}
		return false;
	}

	public GriddlerOne get(final int pos) {
		return this.picograms.get(pos);
	}

	@Override
	public int getCount() {
		return this.picograms.size();
	}

	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {
		if (this.context == null) {
			this.context = this.getContext();
		}
		final GriddlerOne picogram = this.picograms.get(position);
		picogram.nullsToValue(this.context);// Just reset all nulls to a value.
		final LayoutInflater inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View item = inflater.inflate(R.layout.picogram_menu_choice_item,
				parent, false);
		final TextView rate = (TextView) item.findViewById(R.id.tvRating);
		final TextView diff = (TextView) item.findViewById(R.id.tvDiff);
		final TextView name = (TextView) item.findViewById(R.id.tvName);
		final ImageView iv = (ImageView) item.findViewById(R.id.ivCurrent);
		iv.setVisibility(0);
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
		iv.setImageBitmap(bm);
		iv.setVisibility(View.VISIBLE);
		if (picogram.getNumberOfRatings() != 0) {
			final String rateText = "Rating: "
					+ (Double.parseDouble(picogram.getRating()));
			final int index = rateText.indexOf('.');
			if (index != -1) {
				if (rateText.length() > (index + 3)) {
					rate.setText(rateText.substring(0,
							rateText.indexOf('.') + 3));
				} else {
					rate.setText(rateText.substring(0,
							rateText.indexOf('.') + 2));
				}
			} else {
				rate.setText(rateText);
			}
		}
		diff.setText(width + " X " + height + " X "
				+ picogram.getNumberOfColors());
		name.setText(picogram.getName());
		// Change color if user has beaten level.
		int status = 0;
		try {
			status = Integer.parseInt(picogram.getStatus());
		} catch (final NumberFormatException e) {
		}
		final RelativeLayout rl = (RelativeLayout) item
				.findViewById(R.id.rlMenuHolder);
		final Drawable gd = rl.getBackground().mutate();
		item.setBackgroundResource(R.drawable.picogram_menu_choice_border_red);
		if (picogram.getCurrent() == null) {
			picogram.setCurrent("");
		}
		if (status == 2) {
			// Other (Custom, special levels, etc.).
			item.setBackgroundResource(R.drawable.picogram_menu_choice_border_other);
			// Also change difficulty to w X h X c
			diff.setText("w X h X c");
			// Erase rate
			rate.setText("You decide ;)");
		} else if ((status == 1)
				|| this.picograms.get(position).getSolution().replaceAll("x", "0")
						.equals(picogram.getCurrent().replaceAll("x", "0"))) {
			// Won.
			item.setBackgroundResource(R.drawable.picogram_menu_choice_border_green);
		} else if (status == 0) {
			// In progress.
			item.setBackgroundResource(R.drawable.picogram_menu_choice_border_red);

		}
		gd.invalidateSelf();
		item.invalidate();
		rl.invalidate();
		((ViewGroup) item)
				.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

		return item;
	}

	public void removeById(final String id) {
		for (int i = 0; i != this.picograms.size(); ++i) {
			if (this.picograms.get(i).getID().equals(id)) {
				this.picograms.remove(i);
				return;
			}
		}
	}

	public void setPicograms(final ArrayList<GriddlerOne> g) {
		this.picograms = g;
	}

	public void updateCurrentById(final String id, final String newCurrent, final String status) {
		for (int i = 0; i != this.picograms.size(); ++i) {
			if (this.picograms.get(i).getID().equals(id)) {
				final GriddlerOne go = this.picograms.get(i);
				go.setCurrent(newCurrent);
				go.setStatus(status);
				this.picograms.set(i, go);
				return;
			}
		}
	}
}
