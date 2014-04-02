
package com.picogram.awesomeness;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
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
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.plus.PlusShare;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class PreGameFragment extends Fragment implements OnClickListener, OnItemClickListener {
	static final String ARG_POSITION = "position";

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

	private Button buttonLoginLogout;

	UiLifecycleHelper uiHelper;

	private final Session.StatusCallback callback = new Session.StatusCallback() {
		public void call(final Session session, final SessionState state, final Exception exception) {
			if (exception != null) {
				Log.d(TAG, "Facebook Error: " + exception);
			}
			PreGameFragment.this.onSessionStateChange(session, state, exception);
		}
	};
	LoginButton facebookLogin;
	Button facebookPost;

	String dlFacebook = null;

	private void facebookCreateView(final Bundle savedInstanceState, final View view) {

		this.facebookLogin = (LoginButton) view.findViewById(R.id.authButton);
		this.facebookPost = (Button) view.findViewById(R.id.publishButton);
		this.facebookLogin.setFragment(this);
		this.facebookPost.setOnClickListener(new View.OnClickListener() {

			public void onClick(final View v) {
				// Trigger the Facebook feed dialog
				PreGameFragment.this.facebookFeedDialog();
			}
		});

	}

	/*
	 * Show the feed dialog using the deprecated APIs
	 */
	private void facebookFeedDialog() {
		// Set the dialog parameters
		final Bundle params = new Bundle();
		params.putString("name", this.current.getName());
		params.putString("caption", "Play Picogram with me!");
		params.putString("description", "Picogram is a fun puzzle game for Android that allows you to create your own puzzles that your friends and yourself can play.");
		params.putString("link", "www.picogram.com/" + this.current.getID());
		params.putString("picture", ("i.imgur.com/JDSNKkp.png").toString());

		// Invoke the dialog
		final WebDialog feedDialog = (
				new WebDialog.FeedDialogBuilder(this.getActivity(),
						Session.getActiveSession(),
						params))
						.setOnCompleteListener(new OnCompleteListener() {

							public void onComplete(final Bundle values, final FacebookException error) {
								if (error == null) {
									// When the story is posted, echo the success
									// and the post Id.
									final String postId = values.getString("post_id");
									if (postId != null) {
										Toast.makeText(PreGameFragment.this.getActivity(),
												"Story published: " + postId,
												Toast.LENGTH_SHORT).show();
									}
								}
							}

						})
						.build();
		feedDialog.show();

	}

	protected PicogramPart[] getParts() {
		final ArrayList<PicogramPart> result = new ArrayList<PicogramPart>();
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
		for (int i = 0; i != current2D.length; ++i) {
			runY = (int) Math.ceil(i / cellHeight);
			for (int j = 0; j != current2D[i].length; ++j) {
				runX = (int) Math.ceil(j / cellWidth);
				final int location = runX + (runY * cellX);
				if (result.size() == location) {
					result.add(new PicogramPart());
				}
				final PicogramPart pr = result.get(location);
				pr.appendCurrent("" + current2D[i][j]);
				result.set(location, pr);
			}
		}
		runX = runY = run = 0;
		// Get the solutions
		final String solution = this.current.getSolution();
		final char[][] solution2D = new char[Integer.parseInt(this.current.getHeight())][Integer.parseInt(this.current.getWidth())];
		for (int i = 0; i != solution2D.length; ++i)
		{
			for (int j = 0; j != solution2D[i].length; ++j) {
				solution2D[i][j] = solution.charAt(run);
				run++;
			}
		}
		for (int i = 0; i != solution2D.length; ++i) {
			runY = (int) Math.ceil(i / cellHeight);
			for (int j = 0; j != solution2D[i].length; ++j) {
				runX = (int) Math.ceil(j / cellWidth);
				final int location = runX + (runY * cellX);
				final PicogramPart pr = result.get(location);
				pr.appendSolution("" + solution2D[i][j]);
				result.set(location, pr);
			}
		}
		final PicogramPart[] list = new PicogramPart[result.size()];
		for (int i = 0; i != list.length; ++i) {
			final PicogramPart part = result.get(i);
			part.setWidth(pga.cellWidth);
			part.setHeight(pga.cellHeight);
			if (((i + 1) % pga.xCellNum) == 0)
			{

				if (i >= (pga.yCellNum * (pga.xCellNum - 1)))
				{
					// Bottom-Right-Corner, has both remainders.
					part.setWidth((int) (Integer.parseInt(this.current.getWidth()) - (pga.cellWidth * (pga.xCellNum - 1))));
					part.setHeight((int) (Integer.parseInt(this.current.getHeight()) - (pga.cellHeight * (pga.yCellNum - 1))));
				}
				else
				{
					// On the right, has the width of the right remainder.
					part.setWidth((int) (Integer.parseInt(this.current.getWidth()) - (pga.cellWidth * (pga.xCellNum - 1))));
				}
			} else if ((pga.xCellNum * (pga.yCellNum - 1)) <= i)
			{
				// On the bottom, has the height of the bottom remainder.
				part.setHeight((int) (Integer.parseInt(this.current.getHeight()) - (pga.cellHeight * (pga.yCellNum - 1))));
			}
			list[i] = part;
		}
		return list;
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

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Check for an incoming deep link
		final Uri targetUri = this.getActivity().getIntent().getData();
		if (targetUri != null) {
			this.dlFacebook = targetUri.toString();
			Log.i(TAG, "Incoming deep link: " + targetUri);
		}
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		this.uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@SuppressLint("NewApi")
	public void onClick(final View v) {
		final SQLitePicogramAdapter sql = new SQLitePicogramAdapter(
				PreGameFragment.this.getActivity(), "Picograms", null, 1);
		if (v.getId() == R.id.bPlay)
		{
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
					final PicogramPart[] parts = this.getParts();
					Log.d(TAG, "Part: " + parts[part]);
					this.startGame(parts[part], part);
				}
			}
		}
		else if (v.getId() == R.id.bGoogle)
		{
			// TODO Make it use an interactive post.
			final Intent shareIntent = new PlusShare.Builder(this.getActivity())
			.setText("Check out: " + this.current.getName())
			.setType("text/plain")
			.setContentUrl(Uri.parse("http://i.imgur.com/JDSNKkp.png"))
			.setContentDeepLinkId(this.current.getID())
			.getIntent();
			this.startActivityForResult(shareIntent, 0);
		}else if (v.getId() == R.id.bComment){

			if (!Util.getPreferences(this.getActivity()).getBoolean("hasLoggedInSuccessfully", false))
			{
				// Have not logged in. Start Log in Activity.
				final Intent loginIntent = new Intent(this.getActivity(), LoginActivity.class);

				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					final ActivityOptions opts = ActivityOptions.makeCustomAnimation(
							this.getActivity(), R.anim.fadein, R.anim.fadeout);
					this.getActivity().startActivity(loginIntent, opts.toBundle());
				}
				else
				{
					this.getActivity().startActivity(loginIntent);
				}
			} else {
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

		// Facebook
		this.uiHelper = new UiLifecycleHelper(this.getActivity(), this.callback);
		this.uiHelper.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			this.getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		}
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
			final Button bGoogle = (Button) childLayout.findViewById(R.id.bGoogle);
			this.partSpinner = (Spinner) childLayout.findViewById(R.id.spinParts);

			bPlay.setOnClickListener(this);
			bGoogle.setOnClickListener(this);
			if ((Integer.parseInt(this.current.getWidth()) > 25) || (Integer.parseInt(this.current.getHeight()) > 25))
			{
				this.partSpinner.setAdapter(new PartSpinnerAdapter(this.getActivity(), 0, this.getParts()));
			} else {
				this.partSpinner.setVisibility(View.GONE);
			}
			this.facebookCreateView(savedInstanceState, childLayout);
			ll.addView(childLayout);
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

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.uiHelper.onDestroy();
	}

	public void onItemClick(final AdapterView<?> parent, final View view, final int pos, final long id) {
		// If it's not the authors comment, flag. If it is, delete.
		final PicogramComment pc = this.comments.getItem(pos);
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

	@Override
	public void onPause() {
		super.onPause();
		this.uiHelper.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		if ((Integer.parseInt(this.current.getWidth()) > 25) || (Integer.parseInt(this.current.getHeight()) > 25))
		{
			if (this.partSpinner == null) {
				this.partSpinner = (Spinner) this.getActivity().findViewById(R.id.spinParts);
				Log.d(TAG, "Spinner? " + this.partSpinner.toString());
			}
			this.partSpinner.setAdapter(new PartSpinnerAdapter(this.getActivity(), 0, this.getParts()));
		}

		// For scenarios where the main activity is launched and user
		// session is not null, the session state change notification
		// may not be triggered. Trigger it if it's open/closed.
		final Session session = Session.getActiveSession();
		if (this.buttonLoginLogout == null)
		{
			final View view = this.getActivity().findViewById(android.R.id.content);

			this.facebookLogin = (LoginButton) view.findViewById(R.id.authButton);
			this.facebookPost = (Button) view.findViewById(R.id.publishButton);
		}
		if ((session != null) &&
				(session.isOpened() || session.isClosed())) {
			session.addCallback(this.callback);
			this.onSessionStateChange(session, session.getState(), null);
		}

		this.uiHelper.onResume();
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		this.uiHelper.onSaveInstanceState(outState);
	}

	private void onSessionStateChange(final Session session, final SessionState state, final Exception exception) {
		// Check if the user is authenticated and
		// a deep link needs to be handled.
		if (exception != null) {
			Log.d(TAG, "Facebook - Error: " + exception);
		}
		Log.d(TAG, "Facebook - Session state change. " + state.toString() + " " + session.toString() + " ");
		try {
			throw new Exception();
		} catch (final Exception e) {
			for (final StackTraceElement ste : e.getStackTrace()) {
				// Log.d(TAG, "Facebook: " + ste.getLineNumber() + " " + ste.getMethodName());
			}
		}
		try {
			final PackageInfo info = this.getActivity().getPackageManager().getPackageInfo("com.picogram.awesomeness", PackageManager.GET_SIGNATURES);
			for (final Signature signature : info.signatures) {
				final MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				final String sign = Base64.encodeToString(md.digest(), Base64.DEFAULT);
				Log.e("MY KEY HASH:", sign);
				Toast.makeText(this.getActivity().getApplicationContext(), sign, Toast.LENGTH_LONG).show();
			}
		} catch (final NameNotFoundException e) {
		} catch (final NoSuchAlgorithmException e) {
		}
		if (state.isOpened() && (this.dlFacebook != null)) {
			// Launch the menu details activity, passing on
			// the info on the item that was selected.
			Log.d(TAG, "Facebook - Got a Facebook DL");
			this.facebookLogin.setVisibility(View.GONE);
			this.facebookPost.setVisibility(View.VISIBLE);
		} else if (state.isOpened()) {
			// User is logged in.
			Log.d(TAG, "Facebook - Logged in");
			this.facebookLogin.setVisibility(View.GONE);
			this.facebookPost.setVisibility(View.VISIBLE);
		} else if (state.isClosed()) {
			// User is not logged in.
			Log.d(TAG, "Facebook - Not logged in");
			this.facebookLogin.setVisibility(View.VISIBLE);
			this.facebookPost.setVisibility(View.GONE);
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

	void showReportDialog(final String type, final String pid, final String aid) {
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

	@SuppressLint("NewApi")
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
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			final ActivityOptions opts = ActivityOptions.makeCustomAnimation(
					this.getActivity(), R.anim.fadein, R.anim.fadeout);
			this.getActivity().startActivityForResult(gameIntent,
					MenuActivity.GAME_CODE, opts.toBundle());
		} else {
			this.getActivity().startActivityForResult(gameIntent,
					MenuActivity.GAME_CODE);
		}
	}

	@SuppressLint("NewApi")
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
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			final ActivityOptions opts = ActivityOptions.makeCustomAnimation(
					this.getActivity(), R.anim.fadein, R.anim.fadeout);
			this.getActivity().startActivityForResult(gameIntent,
					MenuActivity.GAME_CODE, opts.toBundle());
		} else {
			this.getActivity().startActivityForResult(gameIntent,
					MenuActivity.GAME_CODE);
		}
	}

	@SuppressLint("NewApi")
	protected void startGame(final PicogramPart part, final int partNumber) {
		FlurryAgent.logEvent("UserPlayGame");
		// This is used when we're playing a part, so some things will be different.
		final PreGameActivity pga = (PreGameActivity) this.getActivity();
		final Intent gameIntent = new Intent(this.getActivity(),
				AdvancedGameActivity.class);
		gameIntent.putExtra("name", this.current.getName());
		gameIntent.putExtra("solution", part.getSolution());
		gameIntent.putExtra("current", part.getCurrent());
		gameIntent.putExtra("width", "" + part.getWidth());
		gameIntent.putExtra("height", "" + part.getHeight());
		gameIntent.putExtra("id", this.current.getID());
		gameIntent.putExtra("status", this.current.getStatus());
		gameIntent.putExtra("colors", this.current.getColors());
		gameIntent.putExtra("part", partNumber);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			final ActivityOptions opts = ActivityOptions.makeCustomAnimation(
					this.getActivity(), R.anim.fadein, R.anim.fadeout);
			this.getActivity().startActivityForResult(gameIntent,
					MenuActivity.GAME_CODE, opts.toBundle());
		} else {
			this.getActivity().startActivityForResult(gameIntent,
					MenuActivity.GAME_CODE);
		}
	}

}
