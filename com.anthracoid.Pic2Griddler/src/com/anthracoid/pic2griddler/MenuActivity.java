package com.anthracoid.pic2griddler;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MenuActivity extends Activity implements OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button playButton = (Button) findViewById(R.id.bPlay);
        Button createButton = (Button) findViewById(R.id.bCreate);

        playButton.setOnClickListener(this);
        createButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }

	public void onClick(View v)
	{
		if(v.getId() == R.id.bPlay)
		{
	        Intent startGame = new Intent(MenuActivity.this,GameActivity.class);
	        startActivity(startGame);
		}
		else if(v.getId() == R.id.bCreate)
		{
	        Intent create = new Intent(MenuActivity.this,CreateGriddlerActivity.class);
	        startActivity(create);
		}
		
	}
}
