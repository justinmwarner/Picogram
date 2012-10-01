package com.anthracoid.pic2griddler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.example.com.anthracoid.pic2griddler.R;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private static final int CAMERA_REQUEST_CODE = 1888,
			FILE_SELECT_CODE = 1337; // Look this up a bit later.
	private ImageView preImageView;
	private EditText etURL;
	private Bitmap orig;
	private GridView gv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button photoButton = (Button) findViewById(R.id.buttonCamera);
		Button fileButton = (Button) findViewById(R.id.buttonFile);
		Button urlButton = (Button) findViewById(R.id.buttonURL);
		Button submitButton = (Button) findViewById(R.id.buttonURL);
		preImageView = (ImageView) findViewById(R.id.preImage);
		gv = (GridView) findViewById(R.id.gvPreview);
		photoButton.setOnClickListener(this);
		fileButton.setOnClickListener(this);
		urlButton.setOnClickListener(this);
		submitButton.setOnClickListener(this);

		// Check if we're getting data from a share.
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if (type.startsWith("image/")) {
				Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
				Bitmap bi = readBitmap(uri);
				preImageView.setImageBitmap(bi);
				orig = bi;
				print("Loaded  " + uri);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub

		if (v.getId() == R.id.buttonCamera) {
			Intent cameraIntent = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			this.startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
		} else if (v.getId() == R.id.buttonFile) {
			// File stuff.
			Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
			fileIntent.setType("*/*");
			fileIntent.addCategory(Intent.CATEGORY_OPENABLE); // Shows openable
																// files! =)
			this.startActivityForResult(fileIntent, FILE_SELECT_CODE);
		} else if (v.getId() == R.id.buttonURL) {
			new Thread(new Runnable() {
				public void run() {
					try {
						etURL = (EditText) findViewById(R.id.etURL);
						URL url = new URL(etURL.getText().toString());
						HttpURLConnection conn = (HttpURLConnection) url
								.openConnection();
						conn.setDoInput(true);
						conn.connect();
						InputStream in;
						in = conn.getInputStream();
						final Bitmap bm = BitmapFactory.decodeStream(in);
						preImageView.post(new Runnable() {
							public void run() {
								preImageView.setImageBitmap(bm);
								orig = bm;
							}
						});
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						print(e.toString());
					}
				}

			}).start();
		} else if (v.getId() == R.id.buttonSubmit) {
			preImageView.setImageBitmap(orig);
			alterPhoto();
		}

	}

	@Override
	protected void onActivityResult(int request, int result, Intent data) {
		if (request == CAMERA_REQUEST_CODE) {
			if (result == Activity.RESULT_OK) {
				Bitmap photo = (Bitmap) data.getExtras().get("data");
				preImageView.setImageBitmap(photo);
				orig = photo;
			} else {
				print("Aww, we wanted your picture =(");
			}
		} else if (request == FILE_SELECT_CODE) {
			if (result == Activity.RESULT_OK) {
				Uri uri = data.getData();
				Bitmap bi = readBitmap(uri);
				preImageView.setImageBitmap(bi);
				orig = bi;
			}
		}
	}

	// Read bitmap - From
	// http://tutorials-android.blogspot.co.il/2011/11/outofmemory-exception-when-decoding.html
	public Bitmap readBitmap(Uri selectedImage) { 
		Bitmap bm = null; 
		BitmapFactory.Options options = new BitmapFactory.Options(); 
		options.inSampleSize = 5; 
		AssetFileDescriptor fileDescriptor =null; 
		try 
		{ 
			fileDescriptor = this.getContentResolver().openAssetFileDescriptor(selectedImage,"r"); 
		} 
		catch (FileNotFoundException e) 
		{ 
			e.printStackTrace(); 
		} 
		finally
		{ 
			try 
			{ 
				bm = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options); 
				fileDescriptor.close(); 
			} catch (IOException e) 
			{ 
				e.printStackTrace(); 
			} 
		} 
		return bm; 
	}

	private void alterPhoto() {
		if (orig != null) {

			int cellWidth = 10;
			int cellHeight = 10;
			gv.setNumColumns(cellWidth);
			int h = orig.getHeight() + (orig.getHeight() % cellHeight);
			int w = orig.getWidth() + (orig.getWidth() % cellWidth);
			orig = Bitmap.createScaledBitmap(orig, w, h, true);
			int cellColors[][] = new int[cellWidth][cellHeight];
			for (int i = 0; i < cellColors.length; i++) {
				for (int j = 0; j < cellColors[i].length; j++) {
					int norm = 0;
					for (int x = i * (orig.getWidth() / cellWidth); x < (i + 1)
							* (orig.getWidth() / cellWidth); x++) {
						for (int y = j * (orig.getHeight() / cellHeight); y < (j + 1)
								* (orig.getHeight() / cellHeight); y++) {
							int color = orig.getPixel(x, y);
							int r = color % 256;
							int g = (color / 256) % 256;
							int b = (color / 256 / 256) % 256;
							norm += ((r + g + b) / 3);
						}
					}
					cellColors[i][j] = (norm / ((orig.getWidth() / cellWidth) * (orig
							.getHeight() / cellHeight))); // Gonna be 50 shades
															// of grey XD. Joke.
				}
			}
			// Now find the color to make each cell and color it!
			int num = 0;
			for (int i = 0; i < gv.getCount(); i++) {
				// gv.getChildAt(i).setBackgroundColor(Color.rgb(red, green,
				// blue))
			}
		} else {
			print("We need a valid photo first.");
		}
	}

	private void print(String t) {
		Toast.makeText(this, t, Toast.LENGTH_LONG).show();
	}

}
