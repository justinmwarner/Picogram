
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
import com.stackmob.sdk.api.StackMobQuery;
import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.callback.StackMobQueryCallback;
import com.stackmob.sdk.exception.StackMobException;

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

	GriddlerOne current;
	private int position;
	ListView lvComments, lvHighscores;
	CommentAdapter comments;
	PicogramHighscoreAdapter highscores;

	public void loadComments() {
		this.getActivity().runOnUiThread(new Runnable() {

			public void run() {
				PreGameFragment.this.comments.clear();
				for (int i = 0; i != 0; ++i)
				{
					PreGameFragment.this.comments.add(new PicogramComment(
							PreGameFragment.this.current.getID(), Util.id(PreGameFragment.this
									.getActivity()
									), "Comment is here " + i));
				}
				PreGameFragment.this.comments.notifyDataSetChanged();
				Log.d(TAG, "loading");
				PicogramComment.query(PicogramComment.class, new StackMobQuery().isInRange(0, 9)
						.fieldIsOrderedBy("lastmoddate",
								StackMobQuery.Ordering.DESCENDING)
								.fieldIsEqualTo("puzzleid", PreGameFragment.this.current.getID()),
								new StackMobQueryCallback<PicogramComment>() {
					@Override
					public void failure(final StackMobException e) {
						Log.d(TAG, "Fail");
					}

					@Override
					public void success(final List<PicogramComment> result) {
						for (final PicogramComment pc : result)
						{
							PreGameFragment.this.comments.add(pc);
							PreGameFragment.this.comments.notifyDataSetChanged();
							Log.d(TAG, result.size() + " " + pc.getComment());
							PreGameFragment.this.lvComments.invalidate();
						}
						Log.d(TAG, "Success " + result.size());
					}
				});
			}
		});
	}

	public void loadHighScores() {
		/*
		 * this.getActivity().runOnUiThread(new Runnable() {
		 * 
		 * public void run() {
		 * PreGameFragment.this.highscores.clear();
		 * PreGameFragment.this.highscores.notifyDataSetChanged();
		 * Log.d(TAG, "loading");
		 * PicogramHighscore.query(PicogramHighscore.class, new StackMobQuery()
		 * .isInRange(0, 9)
		 * .fieldIsOrderedBy("score",
		 * StackMobQuery.Ordering.DESCENDING)
		 * .fieldIsEqualTo("puzzleid", PreGameFragment.this.current.getID()),
		 * new StackMobQueryCallback<PicogramHighscore>() {
		 * 
		 * @Override
		 * public void failure(final StackMobException e) {
		 * Log.d(TAG, "Fail");
		 * }
		 * 
		 * @Override
		 * public void success(final List<PicogramHighscore> result) {
		 * // Add this users score, sort it, then add it all to the list.
		 * final SQLitePicogramAdapter sql = new SQLitePicogramAdapter(
		 * PreGameFragment.this.getActivity(), "Picograms", null, 1);
		 * final PicogramHighscore ph = new PicogramHighscore(Util
		 * .id(PreGameFragment.this.getActivity()),
		 * PreGameFragment.this.current.getID(), sql
		 * .getHighscore(PreGameFragment.this.current.getID()));
		 * sql.close();
		 * result.add(ph);
		 * Collections.sort(result);
		 * final int loc = result.indexOf(ph);
		 * Log.d(TAG, "Location of your highscore:  " + loc);
		 * if (loc != result.size())
		 * {
		 * // Save highscore if they're not last.
		 * // Check if our highscore already exists.
		 * PicogramHighscore.query(
		 * PicogramHighscore.class,
		 * new StackMobQuery().fieldIsEqualTo("puzzleid",
		 * ph.getPuzzleId()).fieldIsEqualTo("name",
		 * ph.getName()),
		 * new StackMobQueryCallback<PicogramHighscore>() {
		 * 
		 * @Override
		 * public void failure(final StackMobException arg0) {
		 * // TODO Auto-generated method stub
		 * ph.save();
		 * for (final PicogramHighscore pc : result)
		 * {
		 * PreGameFragment.this.highscores.add(pc);
		 * PreGameFragment.this.highscores
		 * .notifyDataSetChanged();
		 * }
		 * Log.d(TAG, "Adding to the online thing size "
		 * + result.size());
		 * PreGameFragment.this.getActivity()
		 * .runOnUiThread(new Runnable() {
		 * 
		 * public void run() {
		 * // PreGameFragment.this.lvComments
		 * // .setAdapter(PreGameFragment.this.highscores);
		 * PreGameFragment.this.highscores
		 * .notifyDataSetChanged();
		 * PreGameFragment.this.lvHighscores
		 * .invalidate();
		 * }
		 * });
		 * }
		 * 
		 * @Override
		 * public void success(final List arg0) {
		 * // Already exists, don't save it, but rather remove this fromt he list.
		 * result.remove(ph);
		 * for (final PicogramHighscore pc : result)
		 * {
		 * PreGameFragment.this.highscores.add(pc);
		 * PreGameFragment.this.highscores
		 * .notifyDataSetChanged();
		 * }
		 * Log.d(TAG, "Already added. " + result.size());
		 * PreGameFragment.this.getActivity()
		 * .runOnUiThread(new Runnable() {
		 * 
		 * public void run() {
		 * // PreGameFragment.this.lvComments
		 * // .setAdapter(PreGameFragment.this.highscores);
		 * PreGameFragment.this.highscores
		 * .notifyDataSetChanged();
		 * PreGameFragment.this.lvHighscores
		 * .invalidate();
		 * }
		 * });
		 * }
		 * 
		 * });
		 * ph.save();
		 * }
		 * 
		 * }
		 * });
		 * }
		 * });
		 */
	}

	public void onClick(final View v) {
		final SQLitePicogramAdapter sql = new SQLitePicogramAdapter(
				PreGameFragment.this.getActivity(), "Picograms", null, 1);
		final PicogramHighscore ph = new PicogramHighscore(Util
				.id(PreGameFragment.this.getActivity()),
				PreGameFragment.this.current.getID(), sql
				.getHighscore(PreGameFragment.this.current.getID()));
		ph.save();
		sql.close();
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
				pc.setID(this.current.getID() + "" + Util.id(this.getActivity()));
				pc.setPuzzleId("" + this.current.getID());
				pc.setAuthor(Util.id(this.getActivity()));
				pc.setComment(etComment
						.getText().toString());
				final String commentToSave = etComment.getText().toString();
				if (pc.getComment().isEmpty())
				{
					Log.d(TAG, "Empty thing.");
					return;
				}
				// Check if a comment has already been made by this user for this puzzle. This will prevent spam somewhat.
				PicogramComment.query(PicogramComment.class,
						new StackMobQuery().fieldIsEqualTo("author", pc.getAuthor()),
						new StackMobQueryCallback<PicogramComment>() {

					@Override
					public void failure(final StackMobException arg0) {
						// TODO Offline comment?
						pc.save();
						Log.d(TAG, "Saving fail");
					}

					@Override
					public void success(final List<PicogramComment> arg0) {
						if (arg0.size() == 0)
						{
							// Create.
							pc.save();
							Log.d(TAG, "Saving success");
						}
						else
						{
							// Update.
							pc.fetch(new StackMobCallback() {

								@Override
								public void failure(final StackMobException arg0) {
									// TODO Auto-generated method stub

								}

								@Override
								public void success(final String arg0) {
									pc.setComment(commentToSave);
									pc.save();
									Log.d(TAG, "Updating");
								}
							});

						}
						PreGameFragment.this.loadComments();
					}
				});
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
		this.comments = new CommentAdapter(this.getActivity(),
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

	private void startGame(final GriddlerOne go) {
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
		this.startActivityForResult(gameIntent,
				MenuActivity.GAME_CODE);
	}
}
