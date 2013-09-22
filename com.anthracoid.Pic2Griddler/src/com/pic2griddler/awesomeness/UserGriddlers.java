
package com.pic2griddler.awesomeness;

import com.google.analytics.tracking.android.EasyTracker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class UserGriddlers extends Activity implements OnTouchListener, OnItemClickListener {
    protected static final String TAG = "UserGriddlers";
    private ArrayList<Griddler> griddlers = new ArrayList<Griddler>();
    private ListView lv;
    private static SQLiteGriddlerAdapter sql;
    int yPrev;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_griddlers);
        EasyTracker.getInstance().setContext(this);
        EasyTracker.getInstance().activityStart(this);
        lv = (ListView) findViewById(R.id.lvUser);
        // Grab all the Griddlers on local drive.
        // IE: The ones the user started on.
        // Also show the create a Griddler and Tutorial Griddler.
        sql = new SQLiteGriddlerAdapter(this.getApplicationContext(), "Griddlers", null, 1);
        lv.setOnItemClickListener(this);
    }

    public void loadGriddlers() {
        GriddlerListAdapter adapter = new GriddlerListAdapter(this, R.id.lvUser);
        griddlers.clear(); // Clear all old info.
        adapter.setGriddlers(griddlers);

        lv.setAdapter(null);
        String[][] griddlers = sql.getGriddlers();
        Griddler tempGriddler = new Griddler();
        SharedPreferences prefs = getSharedPreferences(MenuActivity.PREFS_FILE, MODE_PRIVATE);
        for (int i = 0; i < griddlers.length; i++) {
            String temp[] = griddlers[i];
            String id = temp[0];
            String name = temp[2];
            String rate = temp[3];
            String width = temp[7];
            String height = temp[8];
            String current = temp[5];
            String solution = temp[4];
            String diff = temp[6];
            String author = temp[1];
            Log.d(TAG, "Author: " + author);
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
                tempGriddler = new Griddler(id, status, name, diff, rate, author, width, height,
                        solution, current);
                this.griddlers.add(tempGriddler);
            }
        }
        lv.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // These could be compiled in to one, but for now, just keep it as is
        // for simplicity.
        if (resultCode == RESULT_OK) {
            // New Girddler, add to database.
            final String id = data.getStringExtra("solution").hashCode() + "";
            final String status = "0";
            final String solution = data.getStringExtra("solution");
            final String author = data.getStringExtra("author");
            final String name = data.getStringExtra("name");
            final String rank = data.getStringExtra("rank");
            final String difficulty = data.getStringExtra("difficulty");
            final String width = data.getStringExtra("width");
            final String height = data.getStringExtra("height");
            final String tags = data.getStringExtra("tags");
            sql.addUserGriddler(id, author, name, rank, solution, difficulty, width, height, status);
            loadGriddlers();
            // Now submit it to the online network.

            Thread t = new Thread(new Runnable() {

                public void run() {
                    try {
                        HttpClient hc = new DefaultHttpClient();
                        String url = "http:// www.pic2griddler.appspot.com/create?id=" + id
                                + "&author=" + author + "&name=" + name + "&rank=" + rank
                                + "&diff=" + difficulty + "&width=" + width + "&height=" + height
                                + "&solution=" + solution
                                + "&tags=" + tags.toLowerCase(Locale.getDefault());
                        url = url.replace(" ", "");
                        HttpGet hg = new HttpGet(url);
                        HttpResponse r = hc.execute(hg);

                        StatusLine sl = r.getStatusLine();
                        if (sl.getStatusCode() == HttpStatus.SC_OK) {
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            r.getEntity().writeTo(out);
                            out.close();
                            Log.d(TAG, out.toString());
                        } else {
                            r.getEntity().getContent().close();
                            Log.d(TAG, sl.getReasonPhrase().toString());
                        }
                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        } else if (resultCode == 2) {
            // Back button pushed or won.
            String id = data.getStringExtra("ID");
            String status = data.getStringExtra("status");
            String current = data.getStringExtra("current");
            sql.updateCurrentGriddler(id, status, current);
            loadGriddlers();
        } else {
            // Nothing added.
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_user_griddlers, menu);
        return true;
    }

    public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
        if (pos >= 0) {
            if (pos == 0) {
                // Start Create.
                Intent createIntent = new Intent(this, CreateGriddlerActivity.class);
                sql.close();
                this.startActivityForResult(createIntent, 1);
            } else {
                // Start game with info!
                startGame(griddlers.get(pos).getSolution(), griddlers.get(pos).getCurrent(),
                        griddlers.get(pos).getWidth(), griddlers.get(pos).getHeight(), griddlers
                                .get(pos).getId(), griddlers.get(pos).getName());
            }
        }
    }

    private void startGame(String solution, String current, String width, String height, String id,
            String name) {
        // Intent gameIntent = new Intent(this, AdvancedGameActivity.class);
        Intent gameIntent = new Intent(this, AdvancedGameActivity.class);
        gameIntent.putExtra("solution", solution);
        gameIntent.putExtra("current", current);
        gameIntent.putExtra("width", width);
        gameIntent.putExtra("height", height);
        gameIntent.putExtra("id", id);
        gameIntent.putExtra("name", name);
        this.startActivityForResult(gameIntent, 2); // 2 because we need to know
                                                    // what the outcome of the
                                                    // game was.
    }

    public boolean onTouch(View v, MotionEvent me) {
        Log.d(TAG, "Touched: " + lv.pointToPosition((int) me.getX(), (int) me.getY()));
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            yPrev = new Date().getSeconds();
        }
        if (me.getAction() == MotionEvent.ACTION_UP) {
            if (yPrev + 20 < me.getY() || yPrev - 20 > me.getY()) {
                int pos = lv.pointToPosition((int) me.getX(), (int) me.getY());
                if (pos >= 0) {
                    if (pos == 0) {
                        // Start Create.
                        Intent createIntent = new Intent(this, CreateGriddlerActivity.class);
                        sql.close();
                        this.startActivityForResult(createIntent, 1);
                        return false;
                    } else {
                        // Start game with info!
                        startGame(griddlers.get(pos).getSolution(),
                                griddlers.get(pos).getCurrent(), griddlers.get(pos).getWidth(),
                                griddlers.get(pos).getHeight(), griddlers.get(pos).getId(),
                                griddlers.get(pos).getName());
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

    @Override
    public void onResume() {
        super.onResume();
        loadGriddlers();
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EasyTracker.getInstance().activityStop(this);
    }

}
