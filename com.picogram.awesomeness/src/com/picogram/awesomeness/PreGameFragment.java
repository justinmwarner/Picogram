
package com.picogram.awesomeness;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.plus.PlusShare;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.util.ArrayList;
import java.util.List;

public class PreGameFragment extends Fragment implements OnClickListener, OnItemClickListener {

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

	Spinner partSpinner;

	protected String[] getCells() {
		final ArrayList<String> result = new ArrayList<String>();
		// Get the currents
		final String current = this.current.getCurrent();
		final char[][] current2D = new char[Integer.parseInt(this.current.getHeight())][Integer.parseInt(this.current.getWidth())];
		int run = 0;
		for (int i = 0; i != current2D.length; ++i)
		{
			for (int j = 0; j != current2D[i].length; ++j) {
				current2D[i][j] = current.charAt(run);
				run++;
			}
		}
		final PreGameActivity pga = (PreGameActivity) this.getActivity();
		final int cellX = (int) pga.xCellNum, cellY = (int) pga.yCellNum;
		final int cellWidth = pga.cellWidth, cellHeight = pga.cellHeight;
		int runX = 0, runY = 0;
		for (int i = 0; i != (cellX * cellY); ++i) {
			result.add("");
		}
		for (int i = 0; i != current2D.length; ++i) {
			runY = (int) Math.ceil(i / cellHeight);
			for (int j = 0; j != current2D[i].length; ++j) {
				runX = (int) Math.ceil(j / cellWidth);
				final int location = runX + (runY * cellX);
				result.set(location, result.get(location) + current2D[i][j]);
			}
		}
		result.add("");
		runX = runY = run = 0;
		// Get the solutions
		final String solution = this.current.getSolution();
		Log.d(TAG, "OUT: " + current + " " + solution);
		final char[][] solution2D = new char[Integer.parseInt(this.current.getHeight())][Integer.parseInt(this.current.getWidth())];
		for (int i = 0; i != solution2D.length; ++i)
		{
			for (int j = 0; j != solution2D[i].length; ++j) {
				solution2D[i][j] = solution.charAt(run);
				run++;
			}
		}
		final ArrayList<String> one = new ArrayList<String>();
		for (int i = 0; i != (cellX * cellY); ++i) {
			one.add("");
		}
		for (int i = 0; i != solution2D.length; ++i) {
			runY = (int) Math.ceil(i / cellHeight);
			for (int j = 0; j != solution2D[i].length; ++j) {
				runX = (int) Math.ceil(j / cellWidth);
				final int location = runX + (runY * cellX);
				one.set(location, one.get(location) + solution2D[i][j]);
			}
		}
		for (final String o : one) {
			result.add(o);
		}
		String[] list = new String[result.size()];
		list = result.toArray(list);
		for (final String l : list) {
			Log.d(TAG, "OOOO: " + l);
		}
		return result.toArray(list);
	}

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
						final int part = this.partSpinner.getSelectedItemPosition();
						Log.d(TAG, "PART: " + part);
						final String[] cells = this.getCells();
						final String cur = cells[part];
						String sol = "";
						for (int i = 0; i != cells.length; ++i)
						{
							if (cells[i].isEmpty())
							{
								sol = cells[i + part + 1];
								break;
							}
						}

						this.startGame(cur, sol, part);
					}
				}
			}
			else if (b.getText().toString().startsWith("Clear")) {
				final String newCurrent = this.current.getCurrent().replaceAll("[^0]", "0");
				sql.updateCurrentPicogram(this.current.getID(), "0", newCurrent);
				this.current.setCurrent(newCurrent);
				((PreGameActivity) this.getActivity()).current = this.current.getCurrent();
				((PreGameActivity) this.getActivity()).updateAndGetImageView();
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
			else if (b.getText().toString().startsWith("Report")) {
				// No author, so null.
				this.showReportDialog("puzzle", this.current.getID(), null);
			}
			else if (b.getText().toString().startsWith("Facebook")) {
			}
			else if (b.getText().toString().startsWith("Google")) {
				// TODO Make it use an interactive post.
				final Intent shareIntent = new PlusShare.Builder(this.getActivity())
				.setText("Check out: " + this.current.getName())
				.setType("text/plain")
				.setContentUrl(Uri.parse("http://i.imgur.com/JDSNKkp.png"))
				.setContentDeepLinkId(this.current.getID())
				.getIntent();
				this.startActivityForResult(shareIntent, 0);
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
		if (this.current == null)
		{
			this.current = new Picogram();
			this.current.nullsToValue(this.getActivity());
		}
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
		LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1f);

		final LinearLayout ll = new LinearLayout(this.getActivity());
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setLayoutParams(params);

		final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
				this.getResources()
				.getDisplayMetrics());

		params = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		if (this.current == null)
		{
			// TODO Catch error.
			this.getActivity().finish();
		}
		if (this.position == 0)
		{
			final View childLayout = inflater.inflate(R.layout.include_pregame_action,
					(ViewGroup) this.getActivity().findViewById(R.layout.include_pregame_action));
			final Button bPlay = (Button) childLayout.findViewById(R.id.bPlay);
			final Button bClear = (Button) childLayout.findViewById(R.id.bClear);
			final Button bDelete = (Button) childLayout.findViewById(R.id.bDelete);
			final Button bReport = (Button) childLayout.findViewById(R.id.bReport);
			final Button bFacebook = (Button) childLayout.findViewById(R.id.bFacebook);
			final Button bGoogle = (Button) childLayout.findViewById(R.id.bGoogle);
			this.partSpinner = (Spinner) childLayout.findViewById(R.id.spinParts);

			bPlay.setOnClickListener(this);
			bClear.setOnClickListener(this);
			bDelete.setOnClickListener(this);
			bReport.setOnClickListener(this);
			bFacebook.setOnClickListener(this);
			bGoogle.setOnClickListener(this);
			this.partSpinner.setAdapter(new PartSpinnerAdapter(this.getActivity(), 0, this.getCells()));

			ll.addView(childLayout);
			/*
			 * 
			 * final ScrollView sv = new ScrollView(this.getActivity());
			 * final LinearLayout llSub = new LinearLayout(this.getActivity());
			 * llSub.setLayoutParams(new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT));
			 * llSub.setOrientation(LinearLayout.VERTICAL);
			 * // Options
			 * TextView tv = new TextView(this.getActivity());
			 * tv.setLayoutParams(params);
			 * tv.setGravity(Gravity.CENTER);
			 * tv.setText("Puzzle");
			 * llSub.addView(tv);
			 * Button b = new Button(this.getActivity());
			 * b.setLayoutParams(params);
			 * b.setGravity(Gravity.CENTER);
			 * b.setText("Play " + this.current.getName());
			 * b.setOnClickListener(this);
			 * LinearLayout tempLL = new LinearLayout(this.getActivity());
			 * tempLL.setOrientation(LinearLayout.HORIZONTAL);
			 * if ((Integer.parseInt(this.current.getWidth()) > 25) || (Integer.parseInt(this.current.getHeight()) > 25))
			 * {
			 * // We have multiple parts, add a spinner to the side.
			 * b.setLayoutParams(params);
			 * tempLL.addView(b);
			 * this.partSpinner = new Spinner(this.getActivity());
			 * this.partSpinner.setLayoutParams(params);
			 * this.partSpinner.setAdapter(new PartSpinnerAdapter(this.getActivity(), 0, this.getCells()));
			 * tempLL.addView(this.partSpinner);
			 * llSub.addView(tempLL);
			 * tempLL = new LinearLayout(this.getActivity());
			 * tempLL.setOrientation(LinearLayout.HORIZONTAL);
			 * }
			 * else {
			 * llSub.addView(b);
			 * }
			 * b = new Button(this.getActivity());
			 * b.setLayoutParams(params);
			 * b.setGravity(Gravity.CENTER);
			 * b.setText("Clear " + this.current.getName());
			 * b.setOnClickListener(this);
			 * tempLL.addView(b);
			 * b = new Button(this.getActivity());
			 * b.setLayoutParams(params);
			 * b.setGravity(Gravity.CENTER);
			 * b.setText("Delete " + this.current.getName());
			 * b.setOnClickListener(this);
			 * tempLL.addView(b);
			 * b = new Button(this.getActivity());
			 * b.setLayoutParams(params);
			 * b.setGravity(Gravity.CENTER);
			 * b.setText("Report " + this.current.getName());
			 * b.setOnClickListener(this);
			 * tempLL.addView(b);
			 * llSub.addView(tempLL);
			 * tempLL = new LinearLayout(this.getActivity());
			 * tempLL.setOrientation(LinearLayout.HORIZONTAL);
			 * // Sharing.
			 * tv = new TextView(this.getActivity());
			 * tv.setLayoutParams(params);
			 * tv.setGravity(Gravity.CENTER);
			 * tv.setText("Sharing");
			 * llSub.addView(tv);
			 * b = new Button(this.getActivity());
			 * b.setLayoutParams(params);
			 * b.setGravity(Gravity.CENTER);
			 * b.setText("Facebook");
			 * b.setBackgroundColor(Color.parseColor("#3B5998"));
			 * b.setOnClickListener(this);
			 * tempLL.addView(b);
			 * b = new Button(this.getActivity());
			 * b.setLayoutParams(params);
			 * b.setGravity(Gravity.CENTER);
			 * b.setText("Google");
			 * b.setBackgroundColor(Color.parseColor("#d34836"));
			 * b.setOnClickListener(this);
			 * tempLL.addView(b);
			 * llSub.addView(tempLL);
			 * sv.addView(llSub);
			 * ll.addView(sv);
			 */
		}
		else if (this.position == 1)
		{
			// Load comments.
			final View childLayout = inflater.inflate(R.layout.include_comments, null);
			ll.addView(childLayout);
			this.lvComments = (ListView) childLayout.findViewById(R.id.lvComments);
			this.lvComments.setAdapter(this.comments);
			this.lvComments.setOnItemClickListener(this);
			final Button b = (Button) childLayout.findViewById(R.id.bComment);
			b.setOnClickListener(this);
			// this.loadComments(); Don't need to call.
		}
		else if (this.position == 2)
		{
			// Load high scores.
			this.lvHighscores = new ListView(this.getActivity());
			this.lvHighscores.setAdapter(this.highscores);
			ll.addView(this.lvHighscores);
			// this.loadHighScores(); Don't need to call.
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

	public void onItemClick(final AdapterView<?> parent, final View view, final int pos, final long id) {
		// If it's not the authors comment, flag. If it is, delete.
		final PicogramComment pc = this.comments.getItem(pos);
		Log.d(TAG, "Comment : " + pc.getComment());
		if (pc.getAuthor().equals(Util.id(this.getActivity())))
		{
			// Theirs, delete prompt.
			this.showDeleteComment(this.current.getID(), pc.getAuthor(), pc);
		}
		else
		{
			this.showReportDialog("comment", this.current.getID(), pc.getAuthor());
		}
	}

	private void showDeleteComment(final String pid, final String author, final PicogramComment pc) {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this.getActivity());

		alert.setMessage("Delete your comment " + pc.getComment());

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int whichButton) {
				final ParseQuery<ParseObject> query = ParseQuery.getQuery("PicogramComment");
				Log.d(TAG, "DELETING " + pc.getComment());
				query.whereEqualTo("author", pc.getAuthor());
				query.whereEqualTo("comment", pc.getComment());
				query.whereEqualTo("puzzleId", pc.getPuzzleId());
				ParseObject po;
				try {
					po = query.getFirst();
					po.deleteEventually(new DeleteCallback() {

						@Override
						public void done(final ParseException e) {
							if (e == null)
							{
								Log.d(TAG, "DELETE SUCCESS!");
								// PreGameFragment.this.loadComments();
								PreGameFragment.this.comments.delete(pc.getAuthor(), pc.getComment());
							} else {
								Log.d(TAG, "COULDN'T DELETE: " + e.getMessage());
							}
						}

					});
				} catch (final ParseException e) {
					Log.d(TAG, "COULDN'T DELETE: " + e.getMessage());
				}
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int whichButton) {
			}
		});

		alert.show();
	}

	private void showReportDialog(final String type, final String pid, final String aid) {
		// Puzzle ID will always be filled out. Author ID will if it's a comment.
		final FlagObject fo = new FlagObject();
		fo.setPuzzleId(pid);
		fo.setAuthorId(aid);
		fo.setType(type);
		final AlertDialog.Builder alert = new AlertDialog.Builder(this.getActivity());

		alert.setTitle("Report " + type);
		alert.setMessage("We thank you for helping us keep our game clean =).");

		final EditText input = new EditText(this.getActivity());
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int whichButton) {
				final String value = input.getText().toString();
				fo.setReason(value);
				fo.save();
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int whichButton) {
				// Ignore.
			}
		});

		alert.show();
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
		gameIntent.putExtra("part", -1);
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
		gameIntent.putExtra("part", -1);
		this.getActivity().startActivityForResult(gameIntent,
				MenuActivity.GAME_CODE);
	}

	protected void startGame(final String current, final String solution, final int part) {
		FlurryAgent.logEvent("UserPlayGame");
		// This is used when we're playing a part, so some things will be different.
		final PreGameActivity pga = (PreGameActivity) this.getActivity();
		final Intent gameIntent = new Intent(this.getActivity(),
				AdvancedGameActivity.class);
		gameIntent.putExtra("name", this.current.getName());
		gameIntent.putExtra("solution", solution);
		gameIntent.putExtra("current", current);
		gameIntent.putExtra("width", pga.cellHeight + "");
		gameIntent.putExtra("height", pga.cellWidth + "");
		gameIntent.putExtra("id", this.current.getID());
		gameIntent.putExtra("status", this.current.getStatus());
		gameIntent.putExtra("colors", this.current.getColors());
		gameIntent.putExtra("part", part);
		this.getActivity().startActivityForResult(gameIntent,
				MenuActivity.GAME_CODE);
	}
}
