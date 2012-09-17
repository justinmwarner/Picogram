package com.example.com.anthracoid.pic2griddler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private static final int CAMERA_REQUEST_CODE = 1888, FILE_SELECT_CODE = 1337;	//Look this up a bit later.
	private ImageView preImageView, finalImageView;
	private EditText etURL;
	private TextView tvInfo;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button photoButton = (Button) findViewById(R.id.buttonCamera);
        Button fileButton = (Button) findViewById(R.id.buttonFile);
        Button urlButton = (Button) findViewById(R.id.buttonURL);
        preImageView = (ImageView) findViewById(R.id.preImage);
        finalImageView = (ImageView) findViewById(R.id.finalImage);
        photoButton.setOnClickListener(this);
        fileButton.setOnClickListener(this);
        urlButton.setOnClickListener(this);
		tvInfo = (TextView) findViewById(R.id.tvInfo);

        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	public void onClick(View v) {
		// TODO Auto-generated method stub
		tvInfo.setText("Changing");
		
		if(v.getId() == R.id.buttonCamera)
		{
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			this.startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
		}
		else if (v.getId() == R.id.buttonFile)
		{
			//File stuff.
			Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
			fileIntent.setType("*/*");
			fileIntent.addCategory(Intent.CATEGORY_OPENABLE);	//Shows openable files! =)
			this.startActivityForResult(fileIntent, FILE_SELECT_CODE);
		}
		else if (v.getId() == R.id.buttonURL)
		{
			new Thread(new Runnable() {
				public void run()
				{
					try {
						etURL = (EditText) findViewById(R.id.etURL);
						URL url = new URL(etURL.getText().toString());
						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
						conn.setDoInput(true);
						conn.connect();
						InputStream in;
						in = conn.getInputStream();
						final Bitmap bm = BitmapFactory.decodeStream(in);
						preImageView.post(new Runnable() 
						{ 
							public void run() 
							{ 
								preImageView.setImageBitmap(bm); 
							} 
						});
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						print(e.toString());
					}
				}
				
			}).start();
		}
	}
	
	protected void onActivityResult(int request, int result, Intent data)
	{
		print("On Result");
		if(request == CAMERA_REQUEST_CODE)
		{
			if(result == Activity.RESULT_OK)
			{
				Bitmap photo = (Bitmap) data.getExtras().get("data");
				preImageView.setImageBitmap(photo);
			}
			else
			{
				print("Aww, we wanted your picture =(");
			}
		}
		else if (request == FILE_SELECT_CODE)
		{
			if(result == Activity.RESULT_OK)
			{
				try {
					Uri uri = data.getData();
					Bitmap bi = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
					preImageView.setImageBitmap(bi);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					print(e.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					print(e.toString());
				}
			}
		}
	}

	private void alterPhoto() {
		Bitmap result = null;
		while(result == null)
		{
			preImageView.buildDrawingCache();
			result = preImageView.getDrawingCache();
		}
		String temp = "";
		tvInfo.setText(temp);
		//Do stuff.
		int[][] theColorsToDraw = new int[result.getWidth()][result.getHeight()];
		for(int i = 0; i < result.getWidth(); i++)
		{
			for(int j = 0; j < result.getHeight(); j++)
			{
				int colorNum = result.getPixel(i, j);
				temp += colorNum + " ";
				theColorsToDraw[i][j] = colorNum;
			}
			temp += "\n";
		}
		tvInfo.setText(temp);
		finalImageView.setImageBitmap(result);
	}
	
	private void print(String t )
	{
		Toast.makeText(this, t , Toast.LENGTH_LONG).show(); 
	}

}
