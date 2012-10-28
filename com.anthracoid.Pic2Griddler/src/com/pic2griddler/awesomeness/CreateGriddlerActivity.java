package com.pic2griddler.awesomeness;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class CreateGriddlerActivity extends Activity implements OnClickListener
{

	private static final int	CAMERA_REQUEST_CODE	= 1888, FILE_SELECT_CODE = 1337;
	private ImageView			preImageView, ivPreview;
	private EditText			etURL;
	private Bitmap				orig;
	private Spinner				sX, sY, sColor;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button photoButton = (Button) findViewById(R.id.buttonCamera);
		Button fileButton = (Button) findViewById(R.id.buttonFile);
		Button urlButton = (Button) findViewById(R.id.buttonURL);
		Button submitButton = (Button) findViewById(R.id.buttonSubmit);
		preImageView = (ImageView) findViewById(R.id.preImage);
		ivPreview = (ImageView) findViewById(R.id.ivPreview);
		photoButton.setOnClickListener(this);
		fileButton.setOnClickListener(this);
		urlButton.setOnClickListener(this);
		submitButton.setOnClickListener(this);

		//Add items to spinners... Might be a better way to do this, seriously, this is idiotic.
		sX = (Spinner) findViewById(R.id.spinX);
		sY = (Spinner) findViewById(R.id.spinY);
		sColor = (Spinner) findViewById(R.id.spinColor);
		String colorNumbers[] = new String[9];
		String xyNumbers[] = new String[20];	//Support more than 20 for multi-griddlers in future.
		for (int i = 1; i < 21; i++)
			xyNumbers[i - 1] = "" + i;
		for (int i = 2; i < 11; i++)
			colorNumbers[i - 2] = "" + i;
		ArrayAdapter xy = new ArrayAdapter(this, android.R.layout.simple_spinner_item, xyNumbers);
		ArrayAdapter cols = new ArrayAdapter(this, android.R.layout.simple_spinner_item, colorNumbers);
		sX.setAdapter(xy);
		sY.setAdapter(xy);
		sColor.setAdapter(cols);

		// Check if we're getting data from a share.
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		if (Intent.ACTION_SEND.equals(action) && type != null)
		{
			if (type.startsWith("image/"))
			{
				Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
				Bitmap bi = readBitmap(uri);
				preImageView.setImageBitmap(bi);
				orig = bi;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onClick(View v)
	{
		if (v.getId() == R.id.buttonCamera)
		{
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			this.startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
		}
		else if (v.getId() == R.id.buttonFile)
		{
			// File stuff.
			Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
			fileIntent.setType("*/*");
			fileIntent.addCategory(Intent.CATEGORY_OPENABLE); // Shows openable
																// files! =)
			this.startActivityForResult(fileIntent, FILE_SELECT_CODE);
		}
		else if (v.getId() == R.id.buttonURL)
		{
			new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
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
								orig = bm;
							}
						});
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						//print(e.toString());
					}
				}

			}).start();
		}
		else if (v.getId() == R.id.buttonSubmit)
		{
			preImageView.setImageBitmap(orig);
			alterPhoto();
		}

	}

	@Override
	protected void onActivityResult(int request, int result, Intent data)
	{
		if (request == CAMERA_REQUEST_CODE)
		{
			if (result == Activity.RESULT_OK)
			{
				Bitmap photo = (Bitmap) data.getExtras().get("data");
				preImageView.setImageBitmap(photo);
				orig = photo;
			}
			else
			{
				print("Aww, we wanted your picture =(");
			}
		}
		else if (request == FILE_SELECT_CODE)
		{
			if (result == Activity.RESULT_OK)
			{
				Uri uri = data.getData();
				Bitmap bi = readBitmap(uri);
				preImageView.setImageBitmap(bi);
				orig = bi;
			}
		}
	}

	// Read bitmap - From
	// http://tutorials-android.blogspot.co.il/2011/11/outofmemory-exception-when-decoding.html
	public Bitmap readBitmap(Uri selectedImage)
	{
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 5;
		AssetFileDescriptor fileDescriptor = null;
		try
		{
			fileDescriptor = this.getContentResolver().openAssetFileDescriptor(selectedImage, "r");
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
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return bm;
	}

	private void alterPhoto()
	{
		if (orig != null)
		{

			int numColors = Integer.parseInt(sColor.getSelectedItem().toString()), yNum = Integer.parseInt(sY.getSelectedItem().toString()), xNum = Integer.parseInt(sX.getSelectedItem().toString());
			int newHeight = orig.getHeight() + (yNum - (orig.getHeight() % yNum));
			int newWidth = orig.getWidth() + (xNum - (orig.getWidth() % xNum));
			Bitmap alter = Bitmap.createScaledBitmap(orig, newWidth, newHeight, true);
			Bitmap tttt = Bitmap.createScaledBitmap(orig, yNum, xNum, true);
			tttt = Bitmap.createScaledBitmap(tttt, yNum * 10, xNum * 10, true);
			this.preImageView.setImageBitmap(tttt);
			tttt = Bitmap.createScaledBitmap(orig, yNum, xNum, false);
			tttt = Bitmap.createScaledBitmap(tttt, yNum * 10, xNum * 10, false);
			this.ivPreview.setImageBitmap(tttt);

			//For future ideas.  May not be implemented.
			/*
			 * int finalMatrix[][] = new int[alter.getHeight()][alter.getWidth()]; String temp = "Height: " + alter.getHeight() + " Width: " + alter.getWidth() + "\n\n"; for(int i = 0; i < alter.getHeight(); i++) { for(int j = 0; j < alter.getWidth(); j++) { int color = alter.getPixel(i, j); int r = color % 256; int g = (color / 256) % 256; int b = (color / 256 / 256) % 256; int norm = (r+b+g)/3; finalMatrix[i][j] = colorNumber(norm, numColors); temp += finalMatrix[i][j]; } temp+="\n"; } //Final
			 * matrix is the color pattern. Now draw that. TextView tv = (TextView) findViewById(R.id.tvFinalCode); tv.setText(temp);
			 */

		}
		else
		{
			print("We need a valid photo first.");
		}
	}

	/*
	 * For past ideas, and for future ideas.
	 */
	private int colorNumber(int norm, int numColors)
	{
		//255 split by numColors.
		int split = 255 / numColors;
		for (int i = numColors - 1; i > 0; i--)
		{
			if (norm >= split * i)
			{
				return i * split;
			}
		}
		return 0;	//Return 0.
	}

	private int colorCode(int norm, int numColors)
	{
		//255 split by numColors.
		int split = 255 / numColors;
		for (int i = numColors - 1; i > 0; i--)
		{
			if (norm >= split * i)
			{
				return i;
			}
		}
		return 0;	//Return 0.
	}

	private void print(String t)
	{
		Toast.makeText(this, t, Toast.LENGTH_LONG).show();
	}

}
