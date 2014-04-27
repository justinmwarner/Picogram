
package com.picogram.awesomeness;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

final class CustomActionMode implements Callback {
	protected static final String TAG = "CustomActionMode";
	private final Picogram selectedPuzzle;
	private final SQLitePicogramAdapter sql;
	PicogramListAdapter adapter;
	MenuFragment frag;
	View me;

	public CustomActionMode(final Picogram p, final SQLitePicogramAdapter sql, final PicogramListAdapter myAdapter, final MenuFragment menuFragment, final View view) {
		this.selectedPuzzle = p;
		this.sql = sql;
		this.adapter = myAdapter;
		this.frag = menuFragment;
		this.me = view;
	}

	public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
		if (item.getTitle().equals("Delete"))
		{
			final Configuration croutonConfiguration = new Configuration.Builder().setDuration(5000).build();
			Crouton c = Crouton.makeText(frag.getActivity(), "\nClick me to undo deletion.\n", Style.INFO);
			c.setConfiguration(croutonConfiguration);
			c.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					sql.addUserPicogram(selectedPuzzle);
					frag.myAdapter.addMy(selectedPuzzle);
					frag.myAdapter.notifyDataSetChanged();
					Crouton.cancelAllCroutons();
					Log.d(TAG, "Size: " + frag.myAdapter.getCount());
					frag.v.post(new Runnable() {

						public void run() {
							frag.v.setSelection(frag.v.getCount() - 1);
						}
					});

				}
			});
			c.show();
			this.sql.deletePicogram(this.selectedPuzzle.getID());
		}
		else if (item.getTitle().equals("Clear"))
		{
			String newCur = "";
			for (final char x : this.selectedPuzzle.getSolution().toCharArray()) {
				newCur += "0";
			}
			this.sql.updateCurrentPicogram(this.selectedPuzzle.getID(), "0", newCur);
		}
		this.adapter.notifyDataSetChanged();
		this.sql.close();
		this.frag.getMyPuzzles(this.frag.getActivity());
		mode.finish();
		return false;
	}

	public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
		// Used to put dark icons on light action bar
		menu.add(0, 1, 2, "Delete");
		menu.add(0, 1, 1, "Clear");

		return true;
	}

	public void onDestroyActionMode(final ActionMode mode) {
		this.me.setSelected(false);
		this.me.setEnabled(false);
		this.sql.close();
	}

	public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
		return false;
	}
}
