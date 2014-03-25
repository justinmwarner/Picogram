
package com.picogram.awesomeness;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

final class CustomActionMode implements Callback {
	private final Picogram selectedPuzzle;
	private final SQLitePicogramAdapter sql;
	PicogramListAdapter adapter;
	MenuFragment frag;

	public CustomActionMode(final Picogram p, final SQLitePicogramAdapter sql, final PicogramListAdapter myAdapter, final MenuFragment menuFragment) {
		this.selectedPuzzle = p;
		this.sql = sql;
		this.adapter = myAdapter;
		this.frag = menuFragment;
	}


	public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
		if (item.getTitle().equals("Delete"))
		{
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
		return false;
	}

	public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
		// Used to put dark icons on light action bar

		menu.add("Delete")
		.setIcon(R.drawable.ic_xs)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add("Clear")
		.setIcon(R.drawable.ic_transparent)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return true;
	}

	public void onDestroyActionMode(final ActionMode mode) {
		this.sql.close();
	}

	public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
		return false;
	}
}
