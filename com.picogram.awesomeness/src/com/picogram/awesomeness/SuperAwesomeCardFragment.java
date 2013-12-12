package com.picogram.awesomeness;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;

import com.flurry.android.FlurryAgent;
import com.stackmob.sdk.api.StackMobQuery;
import com.stackmob.sdk.callback.StackMobModelCallback;
import com.stackmob.sdk.callback.StackMobQueryCallback;
import com.stackmob.sdk.exception.StackMobException;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.util.ArrayList;
import java.util.List;

public class SuperAwesomeCardFragment extends Fragment implements OnItemClickListener {

	private static final String ARG_POSITION = "position";
	private static final String TAG = "SuperAwesomeCardFragment";
	public static final int CREATE_RESULT = 100;
	public static final int GAME_RESULT = 1337;

	public static SuperAwesomeCardFragment newInstance(final int position) {
		final SuperAwesomeCardFragment f = new SuperAwesomeCardFragment();
		final Bundle b = new Bundle();
		b.putInt(ARG_POSITION, position);
		f.setArguments(b);
		return f;
	}

	private int position;

	GriddlerListAdapter myAdapter;
	Handler h = new Handler();
	SQLiteGriddlerAdapter sql = null;

	public void clearAdapter()
	{
		this.myAdapter.clear();
		this.myAdapter.notifyDataSetChanged();
	}

	public void getMyPuzzles(final FragmentActivity a) {
		if (this.sql == null) {
			this.sql = new SQLiteGriddlerAdapter(a, "Griddlers", null, 1);
		}
		final String[][] griddlersArray = this.sql.getGriddlers();
		Log.d(TAG, "My Puzzles: " + griddlersArray.length);
		final SharedPreferences prefs = Util.getPreferences(a);
		for (int i = 0; i < griddlersArray.length; i++) {
			final String temp[] = griddlersArray[i];
			final String id = temp[0];
			final String name = temp[2];
			final String rate = temp[3];
			final String width = temp[7];
			final String height = temp[8];
			final String current = temp[5];
			final String solution = temp[4];
			final String diff = temp[6];
			final String author = temp[1];

			int numColors = 0;
			String colors = null;
			if ((temp[10] != null) && (temp[11] != null)) {
				numColors = Integer.parseInt(temp[10]);
				colors = temp[11];
			}
			String status;
			if (temp[4].equals(temp[5])) {
				if (name.equals("Create a Griddler")) {
					// Special
					status = 2 + "";
				} else {
					// Completed
					status = 1 + "";
				}
			} else {
				// Not completed.
				status = 0 + "";
			}
			boolean isAdd = true;

			if (prefs != null) {
				if (prefs.getBoolean("wonvisible", false)) {
					if (status.equals("1")) {
						isAdd = false;
					}
				}
			}
			if (isAdd) {
				if (status.equals("2") || !Util.isOnline())
				{
					final GriddlerOne tempGriddler = new GriddlerOne(status, name, diff,
							rate, 0, author, width, height,
							solution, current, numColors, colors);
					a.runOnUiThread(new Runnable() {

						public void run() {
							SuperAwesomeCardFragment.this.myAdapter.add(tempGriddler);
							SuperAwesomeCardFragment.this.myAdapter.notifyDataSetChanged();
						}

					});

				} else
				{
					// Get data from online about the Griddler for its updated rating.
					// These variables should be removed.
					final String cols = colors;
					final int nc = numColors;
					final String oldStatus = status; // Don't get rid of this.
					final String oldCurrent = current;
					final GriddlerOne g = new GriddlerOne();
					g.setID(id);
					g.fetch(new StackMobModelCallback() {
						@Override
						public void failure(final StackMobException arg0) {
							a.runOnUiThread(new Runnable() {

								public void run() {
									final GriddlerOne tempGriddler = new GriddlerOne(oldStatus,
											name, diff, rate, 0, author, width, height,
											solution, current, nc, cols);
									tempGriddler.setID(id);
									SuperAwesomeCardFragment.this.myAdapter.add(tempGriddler);
									SuperAwesomeCardFragment.this.myAdapter.notifyDataSetChanged();
								}
							});
						}

						@Override
						public void success() {
							a.runOnUiThread(new Runnable() {

								public void run() {
									g.setStatus(oldStatus);
									g.setCurrent(oldCurrent);
									SuperAwesomeCardFragment.this.myAdapter.add(g);
									SuperAwesomeCardFragment.this.myAdapter.notifyDataSetChanged();
								}
							});
						}
					});
				}
			}
		}

	}

	public void getRecentPuzzles(final Activity a) {
		GriddlerOne.query(
				GriddlerOne.class,
				new StackMobQuery().isInRange(0, 9).fieldIsOrderedBy("createddate",
						StackMobQuery.Ordering.DESCENDING),
						new StackMobQueryCallback<GriddlerOne>() {

					@Override
					public void failure(final StackMobException arg0) {
						Crouton.makeText(a, "Error fetching data: " + arg0.toString(), Style.ALERT);

					}

					@Override
					public void success(final List<GriddlerOne> gs) {
						for (final GriddlerOne g : gs) {
							SuperAwesomeCardFragment.this.myAdapter.add(g);
							SuperAwesomeCardFragment.this.myAdapter.notifyDataSetChanged();
						}
					}
				});
	}

	public void getSortedPuzzles(final Activity a, final String sort) {
		this.myAdapter.clear();
		GriddlerOne.query(
				GriddlerOne.class,
				new StackMobQuery().isInRange(0, 9).fieldIsOrderedBy(sort,
						StackMobQuery.Ordering.DESCENDING),
						new StackMobQueryCallback<GriddlerOne>() {

					@Override
					public void failure(final StackMobException arg0) {
						Crouton.makeText(a, "Error fetching data: " + arg0.toString(), Style.ALERT);
						SuperAwesomeCardFragment.this.myAdapter.notifyDataSetChanged();
					}

					@Override
					public void success(final List<GriddlerOne> gs) {
						for (final GriddlerOne g : gs) {
							a.runOnUiThread(new Runnable() {

								public void run() {
									SuperAwesomeCardFragment.this.myAdapter.add(g);
									SuperAwesomeCardFragment.this.myAdapter.notifyDataSetChanged();
								}

							});
						}
					}
				});
	}

	public void getTagPuzzles(final Activity a, final String tag, final boolean isSortByRate) {
		Log.d(TAG, "0. Tag: " + tag);
		Log.d(TAG, "1. Size: " + this.myAdapter.getCount());
		this.myAdapter.clear();
		Log.d(TAG, "2. Size: " + this.myAdapter.getCount());
		final StackMobQuery smq = new StackMobQuery().fieldIsEqualTo("tag", tag);
		GriddlerTag.query(
				GriddlerTag.class, smq,
				new StackMobQueryCallback<GriddlerTag>() {

					@Override
					public void failure(final StackMobException arg0) {
						Log.d(TAG, "3. Fail: " + arg0.toString());
						Crouton.makeText(a, "Error fetching data: " + arg0.toString(), Style.ALERT);

					}

					@Override
					public void success(final List<GriddlerTag> gts) {
						Log.d(TAG, "3. Succ: " + gts.size());
						final ArrayList<String> ids = new ArrayList();
						for (final GriddlerTag gt : gts) {
							ids.add(gt.getID());
						}

						final StackMobQuery smqInner = new StackMobQuery().isInRange(0, 9)
								.fieldIsIn("griddlerone_id", ids);

						if (isSortByRate) {
							smq.fieldIsOrderedBy("rate", StackMobQuery.Ordering.DESCENDING);
						} else {
							smq.fieldIsOrderedBy("createddate", StackMobQuery.Ordering.DESCENDING);
						}
						GriddlerOne.query(GriddlerOne.class, smqInner,
								new StackMobQueryCallback<GriddlerOne>() {

							@Override
							public void failure(final StackMobException arg0) {

								Log.d(TAG, "4. Err: " + arg0.toString());
								Crouton.makeText(a,
										"Error fetching data: " + arg0.toString(),
										Style.ALERT);
							}

							@Override
							public void success(final List<GriddlerOne> gs) {

								Log.d(TAG, "4. Size: " + gs.size());
								for (final GriddlerOne g : gs) {
									a.runOnUiThread(new Runnable() {

										public void run() {
											SuperAwesomeCardFragment.this.myAdapter.add(g);
											SuperAwesomeCardFragment.this.myAdapter
											.notifyDataSetChanged();
										}
									});
								}
							}
						});

					}
				});
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.position = this.getArguments().getInt(ARG_POSITION);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {
		final LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		final FrameLayout fl = new FrameLayout(this.getActivity());
		fl.setLayoutParams(params);

		final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, this
				.getResources()
				.getDisplayMetrics());

		final ListView v = new ListView(this.getActivity());
		params.setMargins(margin, margin, margin, margin);
		v.setLayoutParams(params);
		v.setLayoutParams(params);
		v.setBackgroundResource(R.drawable.background_card);
		// final List<String> items = new ArrayList();
		this.myAdapter = new GriddlerListAdapter(this.getActivity(), R.id.tvName);
		if (this.position == MenuActivity.TITLES.indexOf("My"))
		{
			this.getMyPuzzles(this.getActivity());
		}
		else if (this.position == MenuActivity.TITLES.indexOf("Top")) {
			this.getSortedPuzzles(this.getActivity(), "rate");
		} else if (this.position == MenuActivity.TITLES.indexOf("Recent"))
		{
			this.getSortedPuzzles(this.getActivity(), "createddate");
		}
		else if (this.position == MenuActivity.TITLES.indexOf("Search"))
		{
			// Don't load anything on start.
			// this.getTagPuzzles(this.getActivity(), "", true);
		}
		else if (this.position == MenuActivity.TITLES.indexOf("Prefs")) {
			return new View(this.getActivity());
		}
		else
		{
			for (int i = 0; i != 20; ++i) {
				final GriddlerOne obj = new GriddlerOne("0",
						"We had an error. You shouldn't see this " + i,
						"0", "0", 1,
						"Justin", "1",
						"1", "1", "0", 2,
						Color.BLACK + " " + Color.RED);
				obj.setID(i + "" + this.position);
				this.myAdapter.add(obj);
				// items.add(MenuActivity.TITLES.get(this.position) + " " + this.position + " " + i);
			}
		}
		v.setAdapter(this.myAdapter);
		v.setOnItemClickListener(this);

		fl.addView(v);
		return fl;
	}

	@Override
	public void onDestroy() {
		if (this.sql != null) {
			this.sql.close();
		}
		super.onDestroy();
	}

	public void onItemClick(final AdapterView<?> parent, final View v, final int pos, final long id) {
		if (pos >= 0) // If valid position to select.
		{
			if ((this.position == MenuActivity.TITLES.indexOf("My")) && (pos == 0)) // Can this be the Creating?
			{
				final Intent createIntent = new Intent(this.getActivity(),
						CreateGriddlerActivity.class);
				this.sql.close();
				Log.d(TAG, "Create code: " + MenuActivity.CREATE_CODE);
				this.getActivity().startActivityForResult(createIntent, MenuActivity.CREATE_CODE);
			}
			else
			{
				this.startGame(this.myAdapter.get(pos).getSolution(), this.myAdapter.get(pos)
						.getCurrent(), this.myAdapter.get(pos).getWidth(), this.myAdapter.get(pos)
						.getHeight(), this.myAdapter.get(pos).getID(), this.myAdapter.get(pos)
						.getName(), this.myAdapter.get(pos).getColors());
			}
		}
	}

	private void startGame(final String solution, final String current, final String width,
			final String height, final String id, final String name, final String colors) {
		FlurryAgent.logEvent("UserPlayGame");
		// Intent gameIntent = new Intent(this, AdvancedGameActivity.class);
		final Intent gameIntent = new Intent(this.getActivity(), AdvancedGameActivity.class);
		gameIntent.putExtra("solution", solution);
		gameIntent.putExtra("current", current);
		gameIntent.putExtra("width", width);
		gameIntent.putExtra("height", height);
		gameIntent.putExtra("id", id);
		gameIntent.putExtra("name", name);
		gameIntent.putExtra("colors", colors);
		this.startActivityForResult(gameIntent, GAME_RESULT);
	}
}