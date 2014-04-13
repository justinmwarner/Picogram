
package com.picogram.awesomeness;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PicogramHighscoreAdapter extends ArrayAdapter<PicogramHighscore> {
	private static final String TAG = "PicogramHighscoreAdapter";
	private Context context;
	ArrayList<PicogramHighscore> scores = new ArrayList<PicogramHighscore>();

	public PicogramHighscoreAdapter(final Context context,
			final int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public PicogramHighscoreAdapter(final Context context, final int resource,
			final ArrayList<PicogramHighscore> items) {
		super(context, resource, items);
		this.context = context;
		this.scores = items;
	}

	@Override
	public void add(final PicogramHighscore object) {
		super.add(object);
		this.scores.add(object);
	}

	@Override
	public void clear() {
		super.clear();
		this.scores.clear();
	}

	public boolean doesHighscoreExist(final String name, final long score) {
		for (final PicogramHighscore s : this.scores)
		{
			if (s.getName().equals(name) && (s.getScore() == score)) {
				return true;
			}
		}
		return false;
	}

	public PicogramHighscore get(final int pos) {
		return this.scores.get(pos);
	}

	@Override
	public int getCount() {
		return this.scores.size();
	}

	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {
		// This might be expensive
		if (this.context == null) {
			this.context = this.getContext();
		}
		final PicogramHighscore ph = this.scores.get(position);
		final LayoutInflater inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// We're reusing the comment layout. Same thing, just score instead of comment. =).
		final View item = inflater.inflate(R.layout.comment_menu_choice_item,
				parent, false);
		final TextView tvName = (TextView) item.findViewById(R.id.tvCommentAuthor);
		final TextView tvScore = (TextView) item.findViewById(R.id.tvComment);
		tvName.setTextColor(Color.LTGRAY);
		tvName.setText(ph.getName());
		tvScore.setText("" + ph.getScore());
		if (Util.id(this.context) == ph.getName())
		{
			// This is the person, so change background.
			item.setBackgroundColor(context.getResources().getColor(R.color.light_yellow));
		}
		return item;
	}
}
