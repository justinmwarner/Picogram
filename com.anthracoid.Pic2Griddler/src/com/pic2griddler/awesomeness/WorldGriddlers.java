
package com.pic2griddler.awesomeness;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import com.google.analytics.tracking.android.EasyTracker;

import java.util.ArrayList;

public class WorldGriddlers extends Activity implements OnClickListener, OnItemClickListener {

    private static final String TAG = "WorldGriddlers";
    Spinner spinSort;
    private ListView lv;
    EditText etQ;
    private final ArrayList<Griddler> griddlers = new ArrayList<Griddler>();

    private SQLiteGriddlerAdapter sql;

    private void loadGriddlers(final String parse) {
        final String[] me = parse.split("\n");

        Griddler temp = null;
        for (int i = 0; i < me.length; i++) {
            final String[] now = me[i].replace("{", "").replace("}", "").split(" ");
            if (now.length == 8) {
                final String author = now[1];
                final String name = now[2];
                final String rank = now[3];
                final String diff = now[4];
                final String width = now[5];
                final String height = now[6];
                final String solution = now[7];
                final String current = now[7].replaceAll("[1-9]", "0"); // Make
                                                                        // it an
                // empty
                // start.
                final String id = now[7].hashCode() + "";
                temp = new Griddler(id, "0", name, diff, rank, author, width, height, solution,
                        current);
                this.griddlers.add(temp);
            }
        }
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
            ((MenuActivity) this.getParent()).switchTab(0);
        }
    }

    public void onClick(final View v) {
        if (v.getId() == R.id.bSearch) {
            final GriddlerListAdapter adapter = new GriddlerListAdapter(this, R.id.lvWorld);
            adapter.setGriddlers(this.griddlers);
            this.lv.setOnItemClickListener(this);
            this.lv.setAdapter(adapter);
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
        array_spinner[0] = "Date";
        array_spinner[1] = "Rank";
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item,
                array_spinner);
        this.spinSort.setAdapter(adapter);
        // Other setup.
        this.sql = new SQLiteGriddlerAdapter(this.getApplicationContext(), "Griddlers", null, 1);
        this.lv = (ListView) this.findViewById(R.id.lvWorld);
        this.lv.setOnItemClickListener(this);

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
            gameIntent.putExtra("id", this.griddlers.get(pos).getId());
            this.startActivityForResult(gameIntent, 2);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EasyTracker.getInstance().activityStop(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EasyTracker.getInstance().activityStart(this);
    }
}
