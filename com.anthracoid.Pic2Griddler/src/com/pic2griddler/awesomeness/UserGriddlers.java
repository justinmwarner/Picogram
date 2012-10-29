package com.pic2griddler.awesomeness;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class UserGriddlers extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_griddlers);
        
        //Load up local file, import data.
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_user_griddlers, menu);
        return true;
    }
}
