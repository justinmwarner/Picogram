
package com.picogram.awesomeness;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.util.List;

public class PreGameFragment extends Fragment implements OnClickListener {

	private static final String ARG_POSITION = "position";
	protected static final String TAG = "PreGameFragment";

	public static PreGameFragment newInstance(final int position) {
		final PreGameFragment f = new PreGameFragment();
		final Bundle b = new Bundle();
		b.putInt(ARG_POSITION, position);
		f.setArguments(b);
		return f;
	}

	Picogram current;
	private int position;
	ListView lvComments, lvHighscores;
	PicogramCommentAdapter comments;
	PicogramHighscoreAdapter highscores;

	public void loadComments() {
		this.comments.clear();
		this.comments.notifyDataSetChanged();
		final ParseQuery<ParseObject> query = ParseQuery.getQuery("PicogramComment");
		query.whereEqualTo("puzzleId", this.current.getID());
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(final List<ParseObject> result, final ParseException e) {
				if (e == null)
				{
					for (final ParseObject po : result)
					{
						final PicogramComment pc = new PicogramComment();
						pc.setAuthor(po.getString("author"));
						pc.setComment(po.getString("comment"));
						pc.setPuzzleId(po.getString("puzzleId"));
						PreGameFragment.this.comments.add(pc);
						PreGameFragment.this.comments.notifyDataSetChanged();
						PreGameFragment.this.lvComments.invalidate();
					}
				}
				else
				{
					Log.d(TAG, "ERROR LOADING COMMENTS: " + e.getMessage());
				}
			}
		});
	}

	public void loadHighScores() {
		this.highscores.clear();
		this.highscores.notifyDataSetChanged();
		final ParseQuery<ParseObject> query = ParseQuery.getQuery("PicogramHighscore");
		query.whereEqualTo("puzzleId", this.current.getID());
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(final List<ParseObject> result, final ParseException e) {
				if (e == null)
				{
					final SQLitePicogramAdapter sql = new SQLitePicogramAdapter(
							PreGameFragment.this.getActivity(), "Picograms", null, 1);

					final PicogramHighscore mine = new PicogramHighscore(Util
							.id(PreGameFragment.this.getActivity()),
							PreGameFragment.this.current.getID(), sql
							.getHighscore(PreGameFragment.this.current.getID()));
					sql.close();
					boolean shouldSave = false;
					long worstScore = 0;
					for (final ParseObject po : result)
					{
						final PicogramHighscore ph = new PicogramHighscore();
						ph.setName(po.getString("name"));
						ph.setScore(po.getLong("score"));
						ph.setPuzzleId(po.getString("puzzleId"));
						PreGameFragment.this.highscores.add(ph);
						PreGameFragment.this.highscores.notifyDataSetChanged();
						PreGameFragment.this.lvHighscores.invalidate();

						if (worstScore < ph.getScore())
						{
							worstScore = ph.getScore();
						}
						if (ph.getName().equals(mine.getName()) && ph.getPuzzleId().equals(mine.getPuzzleId())) {
							shouldSave = true;
						}
					}
					if (PreGameFragment.this.current.getCurrent().equals(PreGameFragment.this.current.getSolution()))
					{
						// Only add users if they've beaten it.
						PreGameFragment.this.highscores.add(mine);
						PreGameFragment.this.highscores.notifyDataSetChanged();

						// Check if user should have his score uploaded.
						if (shouldSave && (mine.getScore() < worstScore)) {
							mine.save();
						}
					}
					PreGameFragment.this.lvHighscores.invalidate();
				}
				else
				{
					Log.d(TAG, "ERROR LOADING COMMENTS: " + e.getMessage());
				}
			}
		});
	}

	public void onClick(final View v) {
		if (v instanceof Button)
		{
			final Button b = (Button) v;
			if (b.getText().toString().startsWith("Play")) {
				if (this.current.getStatus().equals("1")) {
					Crouton.makeText(this.getActivity(),
							"You must clear the game first to play again.", Style.INFO);
				} else {
					this.startGame(this.current);
				}
			}
			else if (b.getText().toString().startsWith("Clear")) {
			}
			else if (b.getText().toString().startsWith("Delete")) {
			}
			else if (b.getText().toString().startsWith("Comment")) {
				// Making a comment to server based on ID.
				Log.d(TAG, "Commenting");
				final PicogramComment pc = new PicogramComment();
				final EditText etComment = ((EditText) this.getActivity().findViewById(
						R.id.etComment));
				pc.setPuzzleId("" + this.current.getID());
				pc.setAuthor(Util.id(this.getActivity()));
				pc.setComment(etComment
						.getText().toString());
				if (pc.getComment().isEmpty())
				{
					return;
				}
				pc.save();
				this.loadComments();
				// TODO Check if a comment has already been made by this user for this puzzle. This will prevent spam somewhat.
				etComment.setText(""); // Reset it.
			}
			else if (b.getText().toString().startsWith("Facebook")) {
			}
			else if (b.getText().toString().startsWith("Pinterest")) {
			}
			else if (b.getText().toString().startsWith("Twitter")) {
			}
			else if (b.getText().toString().startsWith("Email")) {
			}
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.position = this.getArguments().getInt(ARG_POSITION);
		this.comments = new PicogramCommentAdapter(this.getActivity(),
				R.id.tvCommentAuthor);
		this.highscores = new PicogramHighscoreAdapter(this.getActivity(), R.id.tvCommentAuthor);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {

		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		final LinearLayout ll = new LinearLayout(this.getActivity());
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setLayoutParams(params);

		final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
				this.getResources()
				.getDisplayMetrics());

		final TextView v = new TextView(this.getActivity());
		params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		if (this.current == null)
		{
			// TODO Catch error.
			this.getActivity().finish();
		}
		if (this.position == 0)
		{
			// Options
			Button b = new Button(this.getActivity());
			b.setLayoutParams(params);
			b.setGravity(Gravity.CENTER);
			b.setText("Play " + this.current.getName());
			b.setOnClickListener(this);
			ll.addView(b);
			b = new Button(this.getActivity());
			b.setLayoutParams(params);
			b.setGravity(Gravity.CENTER);
			b.setText("Clear " + this.current.getName());
			b.setOnClickListener(this);
			ll.addView(b);
			b = new Button(this.getActivity());
			b.setLayoutParams(params);
			b.setGravity(Gravity.CENTER);
			b.setText("Delete " + this.current.getName());
			b.setOnClickListener(this);
			ll.addView(b);
		}
		else if (this.position == 1)
		{
			// Load comments.
			final View childLayout = inflater.inflate(R.layout.include_comments, null);
			ll.addView(childLayout);
			this.lvComments = (ListView) childLayout.findViewById(R.id.lvComments);
			this.lvComments.setAdapter(this.comments);
			final Button b = (Button) childLayout.findViewById(R.id.bComment);
			b.setOnClickListener(this);
			this.loadComments();
		}
		else if (this.position == 2)
		{
			// Load high scores.
			this.lvHighscores = new ListView(this.getActivity());
			this.lvHighscores.setAdapter(this.highscores);
			ll.addView(this.lvHighscores);
			this.loadHighScores();
		}
		else if (this.position == 3)
		{
			// Sharing.
			Button b = new Button(this.getActivity());
			b.setLayoutParams(params);
			b.setGravity(Gravity.CENTER);
			b.setText("Facebook");
			b.setOnClickListener(this);
			ll.addView(b);
			b = new Button(this.getActivity());
			b.setLayoutParams(params);
			b.setGravity(Gravity.CENTER);
			b.setText("Twitter");
			b.setOnClickListener(this);
			ll.addView(b);
			b = new Button(this.getActivity());
			b.setLayoutParams(params);
			b.setGravity(Gravity.CENTER);
			b.setText("Pinterest");
			b.setOnClickListener(this);
			ll.addView(b);
			b = new Button(this.getActivity());
			b.setLayoutParams(params);
			b.setGravity(Gravity.CENTER);
			b.setText("Email");
			b.setOnClickListener(this);
			ll.addView(b);
		}

		else if (this.position == 4)
		{
		} else
		{
			// Default.
			params.setMargins(margin, margin, margin, margin);
			v.setLayoutParams(params);
			v.setLayoutParams(params);
			v.setGravity(Gravity.CENTER);
			v.setBackgroundResource(R.drawable.background_card);
			v.setText("Error");
			ll.addView(v);
		}
		return ll;
	}

	private void startGame(final Picogram go) {
		FlurryAgent.logEvent("UserPlayGame");
		final Intent gameIntent = new Intent(this.getActivity(),
				AdvancedGameActivity.class);
		gameIntent.putExtra("name", go.getName());
		gameIntent.putExtra("solution", go.getSolution());
		gameIntent.putExtra("current", go.getCurrent());
		gameIntent.putExtra("width", go.getWidth());
		gameIntent.putExtra("height", go.getHeight());
		gameIntent.putExtra("id", go.getID());
		gameIntent.putExtra("status", go.getStatus());
		gameIntent.putExtra("colors", go.getColors());
		this.getActivity().startActivityForResult(gameIntent,
				MenuActivity.GAME_CODE);
	}
}
