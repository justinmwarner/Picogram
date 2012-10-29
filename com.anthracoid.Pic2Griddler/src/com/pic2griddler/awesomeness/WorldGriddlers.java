package com.pic2griddler.awesomeness;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class WorldGriddlers extends Activity implements OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world_griddlers);
        
        //Check server for new Griddler of the Days
        
        //Search Stuff
        Button search = (Button) findViewById(R.id.bSearch);
        search.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_world_griddlers, menu);
        return true;
    }

	public void onClick(View v) {
		if(v.getId() == R.id.bSearch)
		{
			//Searching.
		}
	}
}
