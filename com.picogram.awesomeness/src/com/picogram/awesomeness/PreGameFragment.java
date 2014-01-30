
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ScrollView;
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
					if (PreGameFragment.this.comments.getCount() == 0)
					{
						final PicogramComment temp = new PicogramComment();
						temp.setComment("No one has commented yet.");
						PreGameFragment.this.comments.add(temp);
						PreGameFragment.this.comments.notifyDataSetChanged();
						PreGameFragment.this.lvComments.invalidate();
					}
				}
				else
				{
					final PicogramComment temp = new PicogramComment();
					temp.setComment("Sorry, we had an error loading fromt he interwebs.");
					PreGameFragment.this.comments.add(temp);
					PreGameFragment.this.comments.notifyDataSetChanged();
					PreGameFragment.this.lvComments.invalidate();
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
					boolean shouldSave = true;
					long worstScore = Long.MAX_VALUE;
					for (final ParseObject po : result)
					{
						final PicogramHighscore ph = new PicogramHighscore();
						ph.setName(po.getString("name"));
						ph.setScore(po.getLong("score"));
						ph.setPuzzleId(po.getString("puzzleId"));
						if (!PreGameFragment.this.highscores.doesHighscoreExist(ph.getName(), ph.getScore()))
						{
							PreGameFragment.this.highscores.add(ph);
							PreGameFragment.this.highscores.notifyDataSetChanged();
							PreGameFragment.this.lvHighscores.invalidate();
							if (worstScore < ph.getScore())
							{
								worstScore = ph.getScore();
							}
							if (ph.getName().equals(mine.getName()) && ph.getPuzzleId().equals(mine.getPuzzleId())) {
								shouldSave = false;
							}
						}
					}
					Log.d(TAG, "ph ShouldSave :  " + shouldSave + " Score: " + mine.getScore() + " Worst: " + worstScore + " Extra: " + (mine.getScore() < worstScore));

					if (PreGameFragment.this.current.getCurrent().replaceAll("x", "0").equals(PreGameFragment.this.current.getSolution()))
					{
						// Only add users if they've beaten it.
						if (!PreGameFragment.this.highscores.doesHighscoreExist(mine.getName(), mine.getScore())) {

							// Check if user should have his score uploaded.
							if (shouldSave && (mine.getScore() < worstScore)) {
								Log.d(TAG, "ph saving.");
								mine.save();
							}
							PreGameFragment.this.highscores.add(mine);
							PreGameFragment.this.highscores.notifyDataSetChanged();

						}
					}
					if (PreGameFragment.this.highscores.getCount() == 0)
					{
						final PicogramHighscore ph = new PicogramHighscore();
						ph.setName("No one has publically beaten the game.");
						PreGameFragment.this.highscores.add(ph);
						PreGameFragment.this.highscores.notifyDataSetChanged();
						PreGameFragment.this.lvHighscores.invalidate();
					}
					PreGameFragment.this.lvHighscores.invalidate();
				}
				else
				{
					final PicogramHighscore ph = new PicogramHighscore();
					ph.setName("Error loading highscores from the interwebs.");
					PreGameFragment.this.highscores.add(ph);
					PreGameFragment.this.highscores.notifyDataSetChanged();
					PreGameFragment.this.lvHighscores.invalidate();
					Log.d(TAG, "ERROR LOADING COMMENTS: " + e.getMessage());
				}
			}
		});
	}

	public void onClick(final View v) {
		final SQLitePicogramAdapter sql = new SQLitePicogramAdapter(
				PreGameFragment.this.getActivity(), "Picograms", null, 1);

		if (v instanceof Button)
		{
			final Button b = (Button) v;
			if (b.getText().toString().startsWith("Play")) {
				if (this.current.getStatus().equals("1")) {
					Crouton.makeText(this.getActivity(),
							"You must clear the game first to play again.", Style.INFO);
				} else {
					if ((Integer.parseInt(this.current.getWidth()) < 26) && (Integer.parseInt(this.current.getHeight()) < 26))
					{
						this.startGame(this.current);
					}
					else
					{
						// Multipart, so show the part selector.
						((PicogramPreGame) this.getActivity()).showPartSelector();
					}
				}
			}
			else if (b.getText().toString().startsWith("Clear")) {
				final String newCurrent = this.current.getCurrent().replaceAll("[^0]", "0");
				sql.updateCurrentPicogram(this.current.getID(), "0", newCurrent);
				this.current.setCurrent(newCurrent);
				((PicogramPreGame) this.getActivity()).current = this.current.getCurrent();
				((PicogramPreGame) this.getActivity()).updateAndGetImageView();
			}
			else if (b.getText().toString().startsWith("Delete")) {
				Log.d(TAG, "bb DELETE");
				sql.deletePicogram(this.current.getID());
				sql.close();
				this.getActivity().finish();
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
		sql.close();
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
		if (!Util.isOnline() && (this.position != 0))
		{
			final LayoutParams params = new LayoutParams(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.MATCH_PARENT);

			final FrameLayout fl = new FrameLayout(this.getActivity());
			fl.setLayoutParams(params);

			final int margin = (int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP, 8, this.getResources()
					.getDisplayMetrics());
			final TextView v = new TextView(this.getActivity());
			params.setMargins(margin, margin, margin, margin);
			v.setLayoutParams(params);
			v.setLayoutParams(params);
			v.setGravity(Gravity.CENTER);
			v.setBackgroundResource(R.drawable.background_card);
			v.setText("You're currently offline, and this functionality is online only.  We apologize.\nIf you think this is a mistake, please email us.");

			fl.addView(v);
			return fl;
		}
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		final LinearLayout ll = new LinearLayout(this.getActivity());
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setLayoutParams(params);

		final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
				this.getResources()
				.getDisplayMetrics());

		params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		if (this.current == null)
		{
			// TODO Catch error.
			this.getActivity().finish();
		}
		if (this.position == 0)
		{
			final ScrollView sv = new ScrollView(this.getActivity());
			final LinearLayout llSub = new LinearLayout(this.getActivity());
			llSub.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
			llSub.setOrientation(LinearLayout.VERTICAL);
			// Options
			TextView tv = new TextView(this.getActivity());
			tv.setLayoutParams(params);
			tv.setGravity(Gravity.CENTER);
			tv.setText("Puzzle");
			llSub.addView(tv);
			Button b = new Button(this.getActivity());
			b.setLayoutParams(params);
			b.setGravity(Gravity.CENTER);
			b.setText("Play " + this.current.getName());
			b.setOnClickListener(this);
			llSub.addView(b);
			b = new Button(this.getActivity());
			b.setLayoutParams(params);
			b.setGravity(Gravity.CENTER);
			b.setText("Clear " + this.current.getName());
			b.setOnClickListener(this);
			llSub.addView(b);
			b = new Button(this.getActivity());
			b.setLayoutParams(params);
			b.setGravity(Gravity.CENTER);
			b.setText("Delete " + this.current.getName());
			b.setOnClickListener(this);
			llSub.addView(b);
			// Sharing.
			tv = new TextView(this.getActivity());
			tv.setLayoutParams(params);
			tv.setGravity(Gravity.CENTER);
			tv.setText("Sharing");
			llSub.addView(tv);
			b = new Button(this.getActivity());
			b.setLayoutParams(params);
			b.setGravity(Gravity.CENTER);
			b.setText("Facebook");
			b.setOnClickListener(this);
			llSub.addView(b);
			b = new Button(this.getActivity());
			b.setLayoutParams(params);
			b.setGravity(Gravity.CENTER);
			b.setText("Twitter");
			b.setOnClickListener(this);
			llSub.addView(b);
			b = new Button(this.getActivity());
			b.setLayoutParams(params);
			b.setGravity(Gravity.CENTER);
			b.setText("Pinterest");
			b.setOnClickListener(this);
			llSub.addView(b);
			b = new Button(this.getActivity());
			b.setLayoutParams(params);
			b.setGravity(Gravity.CENTER);
			b.setText("Email");
			b.setOnClickListener(this);
			llSub.addView(b);
			sv.addView(llSub);
			ll.addView(sv);
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
		}

		else if (this.position == 4)
		{
		} else
		{
			// Default.
			final TextView v = new TextView(this.getActivity());
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

	protected void startGame() {
		FlurryAgent.logEvent("UserPlayGame");
		final Intent gameIntent = new Intent(this.getActivity(),
				AdvancedGameActivity.class);
		gameIntent.putExtra("name", this.current.getName());
		gameIntent.putExtra("solution", this.current.getSolution());
		gameIntent.putExtra("current", this.current.getCurrent());
		gameIntent.putExtra("width", this.current.getWidth());
		gameIntent.putExtra("height", this.current.getHeight());
		gameIntent.putExtra("id", this.current.getID());
		gameIntent.putExtra("status", this.current.getStatus());
		gameIntent.putExtra("colors", this.current.getColors());
		this.getActivity().startActivityForResult(gameIntent,
				MenuActivity.GAME_CODE);
	}

	protected void startGame(final Picogram go) {
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
