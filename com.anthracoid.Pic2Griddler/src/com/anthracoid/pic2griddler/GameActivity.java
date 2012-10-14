package com.anthracoid.pic2griddler;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GameActivity extends Activity implements OnClickListener {

	private DrawGriddler svGame;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Button b1 = (Button) findViewById(R.id.bColorOne);
        Button b2 = (Button) findViewById(R.id.bColorTwo);
        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        svGame = (DrawGriddler) findViewById(R.id.svGame);
        svGame.setValues("2442", "2442");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_game, menu);
        return true;
    }

	public void onClick(View v)
	{
		if(v.getId() == R.id.bColorOne)
		{
			//Black
			svGame.setColor(Color.RED);
		}
		else if(v.getId() == R.id.bColorTwo)
		{
			//White
			svGame.setColor(Color.WHITE);
		}
	}
}
