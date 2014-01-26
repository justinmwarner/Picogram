
package com.picogram.awesomeness;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CommentAdapter extends ArrayAdapter<PicogramComment> {
	private static final String TAG = "CommentAdapter";
	private Context context;
	ArrayList<PicogramComment> comments = new ArrayList<PicogramComment>();

	public CommentAdapter(final Context context, final int resource) {
		super(context, resource);
		this.context = context;
	}

	public CommentAdapter(final Context context, final int resource,
			final ArrayList<PicogramComment> objects) {
		super(context, resource, objects);
		this.context = context;
		this.comments = objects;
	}

	@Override
	public void add(final PicogramComment object) {
		super.add(object);
		this.comments.add(object);
		this.notifyDataSetChanged();
	}

	@Override
	public void clear() {
		super.clear();
		this.comments.clear();
	}
	@Override
	public int getCount() {
		return this.comments.size();
	}

	@Override
	public PicogramComment getItem(final int position) {
		return this.comments.get(position);
	}

	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {
		if (this.context == null) {
			this.context = this.getContext();
		}
		final PicogramComment comment = this.comments.get(position);

		Log.d(TAG, "Creating list item: " + position + " " + comment.getComment());
		final LayoutInflater inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View item = inflater.inflate(R.layout.comment_menu_choice_item,
				parent, false);
		final TextView tvAuthor = (TextView) item.findViewById(R.id.tvCommentAuthor);
		final TextView tvPicogramComment = (TextView) item.findViewById(R.id.tvComment);
		tvAuthor.setText(comment.getAuthor());
		tvPicogramComment.setText(comment.getComment());
		item.invalidate();
		return item;
	}

}
