
package com.picogram.awesomeness;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.flurry.android.FlurryAgent;
import com.stackmob.sdk.api.StackMobQuery;
import com.stackmob.sdk.callback.StackMobQueryCallback;
import com.stackmob.sdk.exception.StackMobException;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.util.ArrayList;
import java.util.List;

public class WorldGriddlers extends Activity implements OnClickListener, OnItemClickListener {

	private static final String TAG = "WorldGriddlers";
	Spinner spinSort;
	private ListView lv;
	EditText etQ;
	Handler h = new Handler();

	private final ArrayList<GriddlerOne> griddlers = new ArrayList<GriddlerOne>();
	private SQLiteGriddlerAdapter sql;

	private void loadByTag(final String tag) {
		final Activity a = this;
		final StackMobQuery smq = new StackMobQuery().fieldIsEqualTo("tag", tag);
		GriddlerTag.query(
				GriddlerTag.class, smq,
				new StackMobQueryCallback<GriddlerTag>() {

					@Override
					public void failure(final StackMobException arg0) {
						Log.d(TAG, "ERROR: " + arg0.toString());
						Crouton.makeText(a, "Error fetching data: " + arg0.toString(), Style.ALERT);

					}

					@Override
					public void success(final List<GriddlerTag> gts) {
						final ArrayList<String> ids = new ArrayList();
						for (final GriddlerTag gt : gts) {
							ids.add(gt.getID());
						}

						final StackMobQuery smqInner = new StackMobQuery().isInRange(0, 9)
								.fieldIsIn("griddlerone_id", ids);

						if (WorldGriddlers.this.spinSort.getSelectedItem().toString()
								.equals("Rank")) {
							smq.fieldIsOrderedBy("rate", StackMobQuery.Ordering.DESCENDING);
						} else if (WorldGriddlers.this.spinSort.getSelectedItem().toString()
								.equals("Date")) {
							smq.fieldIsOrderedBy("createddate", StackMobQuery.Ordering.DESCENDING);
						}
						GriddlerOne.query(GriddlerOne.class, smqInner,
								new StackMobQueryCallback<GriddlerOne>() {

									@Override
									public void failure(final StackMobException arg0) {
										Crouton.makeText(a,
												"Error fetching data: " + arg0.toString(),
												Style.ALERT);
									}

									@Override
									public void success(final List<GriddlerOne> gs) {

										for (final GriddlerOne g : gs) {
											WorldGriddlers.this.griddlers.add(g);
										}
										WorldGriddlers.this.h.post(new Runnable() {

											public void run() {
												WorldGriddlers.this.updateListView();

											}
										});
									}
								});

					}
				});
	}

	private void loadMostRecent() {
		final Activity a = this;
		GriddlerOne.query(
				GriddlerOne.class,
				new StackMobQuery().isInRange(0, 9).fieldIsOrderedBy("createddate",
						StackMobQuery.Ordering.DESCENDING),
				new StackMobQueryCallback<GriddlerOne>() {

					@Override
					public void failure(final StackMobException arg0) {
						Log.d(TAG, "ERROR: " + arg0.toString());
						Crouton.makeText(a, "Error fetching data: " + arg0.toString(), Style.ALERT);

					}

					@Override
					public void success(final List<GriddlerOne> gs) {
						for (final GriddlerOne g : gs) {
							WorldGriddlers.this.griddlers.add(g);
						}
						WorldGriddlers.this.h.post(new Runnable() {

							public void run() {
								WorldGriddlers.this.updateListView();

							}
						});
					}
				});
	}

	private void loadTopAllTime() {
		final Activity a = this;
		GriddlerOne.query(
				GriddlerOne.class,
				new StackMobQuery().isInRange(0, 9).fieldIsOrderedBy("rate",
						StackMobQuery.Ordering.DESCENDING),
				new StackMobQueryCallback<GriddlerOne>() {

					@Override
					public void failure(final StackMobException arg0) {
						Log.d(TAG, "ERROR: " + arg0.toString());
						Crouton.makeText(a, "Error fetching data: " + arg0.toString(), Style.ALERT);

					}

					@Override
					public void success(final List<GriddlerOne> gs) {
						for (final GriddlerOne g : gs) {
							WorldGriddlers.this.griddlers.add(g);
						}
						WorldGriddlers.this.h.post(new Runnable() {

							public void run() {
								WorldGriddlers.this.updateListView();

							}
						});
					}
				});
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		// These could be compiled in to one, but for now, just keep it as is
		// for simplicity.
		if (resultCode == 2) {
			final String id = data.getStringExtra("ID");
			final String status = data.getStringExtra("status");
			final String current = data.getStringExtra("current");
			this.sql.updateCurrentGriddler(id, status, current);
			// Reset to User frame (Mini-tutorial to show that it adds
			// previously played games).
			this.sql.close();
			// TODO Update My Puzzle tab.
			// ((MenuActivity) this.getParent()).switchTab(0);
		}
	}

	public void onClick(final View v) {
		if (v.getId() == R.id.bSearch) {
			WorldGriddlers.this.griddlers.clear();
			if (this.etQ.getText().toString().length() == 0)
			{
				if (this.spinSort.getSelectedItem().toString().equals("Date")) {
					this.loadMostRecent();
				} else if (this.spinSort.getSelectedItem().toString().equals("Rank")) {
					this.loadTopAllTime();
				}
			}
			else {
				this.loadByTag(this.etQ.getText().toString());
			}
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_world_griddlers);

		// Check server for new Griddler of the Days
		// Search Stuff
		final Button search = (Button) this.findViewById(R.id.bSearch);
		search.setOnClickListener(this);
		this.etQ = (EditText) this.findViewById(R.id.etSearch);
		this.spinSort = (Spinner) this.findViewById(R.id.spinSort);
		final String[] array_spinner = new String[2];
		array_spinner[0] = "Rank";
		array_spinner[1] = "Date";
		final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item,
				array_spinner);
		this.spinSort.setAdapter(adapter);
		// Other setup.
		this.sql = new SQLiteGriddlerAdapter(this.getApplicationContext(), "Griddlers", null, 1);
		this.lv = (ListView) this.findViewById(R.id.lvWorld);
		this.lv.setOnItemClickListener(this);
		FlurryAgent.logEvent("WorldOpened");
		// Click the search button to get the top puzzles at start.
		search.performClick();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.getMenuInflater().inflate(R.menu.activity_world_griddlers, menu);
		return true;
	}

	public void onItemClick(final AdapterView<?> parent, final View v, final int pos, final long id) {
		// If user tries to play, do the same exact thing as a normal game.
		// BUT, add this to the SQLite for their own personal games.
		if (pos >= 0) {
			// Start game with info!
			final Intent gameIntent = new Intent(this, AdvancedGameActivity.class);
			this.sql.addUserGriddler(this.griddlers.get(pos));
			this.sql.close();
			// gameIntent.putExtra("info", griddlers.get(pos).getInfo());
			gameIntent.putExtra("id", this.griddlers.get(pos).getID());
			FlurryAgent.logEvent("UserOpenedWorldPuzzle");
			this.startActivityForResult(gameIntent, 2);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void updateListView()
	{
		final GriddlerListAdapter adapter = new GriddlerListAdapter(this,
				R.id.lvWorld, WorldGriddlers.this.griddlers);
		adapter.setGriddlers(WorldGriddlers.this.griddlers);
		this.lv.setAdapter(adapter);
	}
}
