package com.pic2griddler.awesomeness;

import com.google.analytics.tracking.android.EasyTracker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
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

public class WorldGriddlers extends Activity implements OnClickListener, OnItemClickListener {

	private static final String TAG = "WorldGriddlers";
	Spinner spinSort;
	private ListView lv;
	EditText etQ;
	private ArrayList<Griddler> griddlers = new ArrayList<Griddler>();
	private String parseMe;
	private SQLiteGriddlerAdapter sql;

	public void onClick(View v) {
		if (v.getId() == R.id.bSearch) {
			Thread t = new Thread(new Runnable() {

				public void run() {

					EasyTracker.getTracker().trackEvent("Search", "Searching", etQ.getText().toString().toLowerCase(), (long) 0);
					// Ignore offset for now.
					String url = "http://www.pic2griddler.appspot.com/search?q=" + etQ.getText().toString().toLowerCase();
					try {
						// Searching
						HttpClient hc = new DefaultHttpClient();
						url = url.replace(" ", "");
						HttpGet hg = new HttpGet(url);
						HttpResponse r = hc.execute(hg);
						StatusLine sl = r.getStatusLine();
						if (sl.getStatusCode() == HttpStatus.SC_OK) {
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							r.getEntity().writeTo(out);
							out.close();
							parseMe = out.toString();

						} else {
							r.getEntity().getContent().close();
							parseMe = "ERROR";
						}
					} catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				private void loadGriddlers(String string) {
					// TODO Auto-generated method stub

				}
			});
			t.start();
			// Add them from out
			while (parseMe == null) {

			}
			if (parseMe.equals("ERROR")) {

			} else {
				loadGriddlers(parseMe);
				GriddlerListAdapter adapter = new GriddlerListAdapter(this, R.id.lvWorld);
				adapter.setGriddlers(griddlers);
				lv.setOnItemClickListener(this);
				lv.setAdapter(adapter);
			}
			parseMe = null;
		}
	}

	private void loadGriddlers(String parse) {
		String[] me = parse.split("\n");

		Griddler temp = null;
		for (int i = 0; i < me.length; i++) {
			String[] now = me[i].replace("{", "").replace("}", "").split(" ");
			if (now.length == 8) {
				String author = now[1];
				String name = now[2];
				String rank = now[3];
				String diff = now[4];
				String width = now[5];
				String height = now[6];
				String solution = now[7];
				String current = now[7].replaceAll("[1-9]", "0"); // Make it an
																	// empty
																	// start.
				String id = now[7].hashCode() + "";
				String info = (width + " " + height + " " + solution + " " + current);
				// temp = new Griddler(id, "0", name, diff, rank, info, author);
				griddlers.add(temp);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_world_griddlers);

		EasyTracker.getInstance().setContext(this);
		// Check server for new Griddler of the Days

		// Search Stuff
		Button search = (Button) findViewById(R.id.bSearch);
		search.setOnClickListener(this);
		etQ = (EditText) findViewById(R.id.etSearch);
		spinSort = (Spinner) findViewById(R.id.spinSort);
		String[] array_spinner = new String[2];
		array_spinner[0] = "Date";
		array_spinner[1] = "Rank";
		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, array_spinner);
		spinSort.setAdapter(adapter);
		// Other setup.
		sql = new SQLiteGriddlerAdapter(this.getApplicationContext(), "Griddlers", null, 1);
		lv = (ListView) findViewById(R.id.lvWorld);
		lv.setOnItemClickListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_world_griddlers, menu);
		return true;
	}

	public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
		// If user tries to play, do the same exact thing as a normal game.
		// BUT, add this to the SQLite for their own personal games.
		if (pos >= 0) {
			// Start game with info!
			Intent gameIntent = new Intent(this, GameActivity.class);
			// sql.addUserGriddler(griddlers.get(pos).getId(),
			// griddlers.get(pos).getAuthor(), griddlers.get(pos).getName(),
			// griddlers.get(pos).getRank(),
			// griddlers.get(pos).getInfo().split(" ")[2],
			// griddlers.get(pos).getDiff(),
			// griddlers.get(pos).getInfo().split(" ")[0],
			// griddlers.get(pos).getInfo().split(" ")[1], "0");
			sql.close();
			// gameIntent.putExtra("info", griddlers.get(pos).getInfo());
			gameIntent.putExtra("id", griddlers.get(pos).getId());
			this.startActivityForResult(gameIntent, 2);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// These could be compiled in to one, but for now, just keep it as is
		// for simplicity.
		if (resultCode == 2) {
			String id = data.getStringExtra("ID");
			String status = data.getStringExtra("status");
			String current = data.getStringExtra("current");
			sql.updateCurrentGriddler(id, status, current);
			// Reset to User frame (Mini-tutorial to show that it adds
			// previously played games).
			sql.close();
			((MenuActivity) this.getParent()).switchTab(0);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.
	}
}
