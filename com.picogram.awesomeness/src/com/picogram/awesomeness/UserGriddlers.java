
package com.picogram.awesomeness;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.flurry.android.FlurryAgent;
import com.stackmob.sdk.callback.StackMobModelCallback;
import com.stackmob.sdk.exception.StackMobException;

import java.util.ArrayList;
import java.util.Date;

public class UserGriddlers extends Activity implements OnTouchListener, OnItemClickListener {
	protected static final String TAG = "UserGriddlers";
	ArrayList<GriddlerOne> griddlers = new ArrayList<GriddlerOne>();
	private ListView lv;
	private static SQLiteGriddlerAdapter sql;
	int yPrev;
	Handler h = new Handler();

	public void loadGriddlers() {
		UserGriddlers.this.griddlers.clear();
		final GriddlerListAdapter adapter = new GriddlerListAdapter(this, R.id.lvUser);
		this.griddlers.clear(); // Clear all old info.
		adapter.setGriddlers(this.griddlers);

		this.lv.setAdapter(null);
		final String[][] griddlersArray = sql.getGriddlers();
		final SharedPreferences prefs = this.getSharedPreferences(MenuActivity.PREFS_FILE,
				MODE_PRIVATE);
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
					this.griddlers.add(tempGriddler);
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
							UserGriddlers.this.h.post(new Runnable() {

								public void run() {
									final GriddlerOne tempGriddler = new GriddlerOne(oldStatus,
											name,
											diff,
											rate, 0, author, width, height,
											solution, current, nc, cols);
									tempGriddler.setID(id);
									UserGriddlers.this.griddlers.add(tempGriddler);
								}
							});
						}

						@Override
						public void success() {
							UserGriddlers.this.h.post(new Runnable() {

								public void run() {
									g.setStatus(oldStatus);
									g.setCurrent(oldCurrent);
									UserGriddlers.this.griddlers.add(g);
									// adapter.setGriddlers(UserGriddlers.this.griddlers);
									// UserGriddlers.this.lv.setAdapter(adapter);
								}
							});
						}
					});
				}
			}
		}
		adapter.setGriddlers(this.griddlers);
		this.lv.setAdapter(adapter);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		// These could be compiled in to one, but for now, just keep it as is
		// for simplicity.
		if (resultCode == RESULT_OK) {
			// New Girddler, add to database.
			final String colors = data.getStringExtra("colors");
			final String id = data.getStringExtra("solution").hashCode() + "";
			final String status = "0";
			final String author = data.getStringExtra("author");
			final String difficulty = data.getStringExtra("difficulty");
			final String height = data.getStringExtra("height");
			final String name = data.getStringExtra("name");
			final int numberOfColors = colors.split(",").length;
			final String rank = data.getStringExtra("rank");
			final String solution = data.getStringExtra("solution");
			final String width = data.getStringExtra("width");
			final GriddlerOne g = new GriddlerOne(status, name, difficulty, rank, 1, author, width,
					height, solution, null, numberOfColors, colors);
			g.setID(id);
			// TODO Check if Picogram already exists. If it does, just add that to the users sql database.
			sql.addUserGriddler(g);
			// TODO If save failed, save offline to upload later on.
			g.save(new StackMobModelCallback() {

				@Override
				public void failure(final StackMobException arg0) {

				}

				@Override
				public void success() {
					// TODO Auto-generated method stub

				}
			});
			final String[] tags = data.getStringExtra("tags").split(" ");
			for (final String tag : tags)
			{
				final GriddlerTag gt = new GriddlerTag(tag);
				gt.setID(id);
				gt.save();
			}

		} else if (resultCode == 2) {
			// Back button pushed or won.
			final String id = data.getStringExtra("ID");
			final String status = data.getStringExtra("status");
			final String current = data.getStringExtra("current");
			sql.updateCurrentGriddler(id, status, current);
		} else {
			// Nothing added.
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_user_griddlers);
		this.lv = (ListView) this.findViewById(R.id.lvUser);
		// Grab all the Griddlers on local drive.
		// IE: The ones the user started on.
		// Also show the create a Griddler and Tutorial Griddler.
		sql = new SQLiteGriddlerAdapter(this.getApplicationContext(), "Griddlers", null, 1);
		this.lv.setOnItemClickListener(this);
		FlurryAgent.logEvent("UserOpened");
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.getMenuInflater().inflate(R.menu.activity_user_griddlers, menu);
		return true;
	}

	public void onItemClick(final AdapterView<?> parent, final View v, final int pos, final long id) {
		if (pos >= 0) {
			if (pos == 0) {
				// Start Create.
				final Intent createIntent = new Intent(this, CreateGriddlerActivity.class);
				sql.close();
				this.startActivityForResult(createIntent, 1);
			} else {
				// Start game with info!
				this.startGame(this.griddlers.get(pos).getSolution(), this.griddlers.get(pos)
						.getCurrent(),
						this.griddlers.get(pos).getWidth(), this.griddlers.get(pos).getHeight(),
						this.griddlers.get(pos).getID(), this.griddlers.get(pos).getName(),
						this.griddlers.get(pos).getColors());
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		this.loadGriddlers();
	}

	public boolean onTouch(final View v, final MotionEvent me) {
		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			this.yPrev = new Date().getSeconds();
		}
		if (me.getAction() == MotionEvent.ACTION_UP) {
			if (((this.yPrev + 20) < me.getY()) || ((this.yPrev - 20) > me.getY())) {
				final int pos = this.lv.pointToPosition((int) me.getX(), (int) me.getY());
				if (pos >= 0) {
					if (pos == 0) {
						// Start Create.
						final Intent createIntent = new Intent(this, CreateGriddlerActivity.class);
						sql.close();
						this.startActivityForResult(createIntent, 1);
						return false;
					} else {
						// Start game with info!
						this.startGame(this.griddlers.get(pos).getSolution(),
								this.griddlers.get(pos).getCurrent(), this.griddlers.get(pos)
										.getWidth(),
								this.griddlers.get(pos).getHeight(), this.griddlers.get(pos)
										.getID(),
								this.griddlers.get(pos).getName(), this.griddlers.get(pos)
										.getColors());
					}
				}
				return false;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private void startGame(final String solution, final String current, final String width,
			final String height, final String id, final String name, final String colors) {
		FlurryAgent.logEvent("UserPlayGame");
		// Intent gameIntent = new Intent(this, AdvancedGameActivity.class);
		final Intent gameIntent = new Intent(this, AdvancedGameActivity.class);
		gameIntent.putExtra("solution", solution);
		gameIntent.putExtra("current", current);
		gameIntent.putExtra("width", width);
		gameIntent.putExtra("height", height);
		gameIntent.putExtra("id", id);
		gameIntent.putExtra("name", name);
		gameIntent.putExtra("colors", colors);
		this.startActivityForResult(gameIntent, 2); // 2 because we need to know
													// what the outcome of the
													// game was.
	}

}
