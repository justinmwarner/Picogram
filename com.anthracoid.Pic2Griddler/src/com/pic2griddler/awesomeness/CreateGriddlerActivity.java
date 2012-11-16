package com.pic2griddler.awesomeness;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CreateGriddlerActivity extends Activity implements OnClickListener
{

	private static final int CAMERA_REQUEST_CODE = 1888, FILE_SELECT_CODE = 1337;
	private ImageView ivBefore, ivAfter;
	private EditText etURL;
	private Bitmap orig;
	private Spinner sX, sY, sColor;
	private String solution = "", current = "";
	private int numColors, yNum, xNum;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create);
		Button photoButton = (Button) findViewById(R.id.buttonCamera);
		Button fileButton = (Button) findViewById(R.id.buttonFile);
		Button urlButton = (Button) findViewById(R.id.buttonURL);
		Button submitButton = (Button) findViewById(R.id.buttonSubmit);
		Button doneButton = (Button) findViewById(R.id.buttonDone);
		ivBefore = (ImageView) findViewById(R.id.preImage);
		ivAfter = (ImageView) findViewById(R.id.ivPreview);
		photoButton.setOnClickListener(this);
		fileButton.setOnClickListener(this);
		urlButton.setOnClickListener(this);
		submitButton.setOnClickListener(this);
		doneButton.setOnClickListener(this);

		// Add items to spinners... Might be a better way to do this, seriously,
		// this is idiotic.
		sX = (Spinner) findViewById(R.id.spinX);
		sY = (Spinner) findViewById(R.id.spinY);
		sColor = (Spinner) findViewById(R.id.spinColor);
		String colorNumbers[] = new String[9];
		String xyNumbers[] = new String[20]; // Support more than 20 for
												// multi-griddlers in future.
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
				ivBefore.setImageBitmap(bi);
				orig = bi;
			}
		}
		// Prevent keyboard from popping up.
		etURL = (EditText) findViewById(R.id.etURL);
		etURL.clearFocus();
		LinearLayout trash = (LinearLayout) findViewById(R.id.llTrash);
		trash.requestFocus();
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
						ivBefore.post(new Runnable()
						{
							public void run()
							{
								ivBefore.setImageBitmap(bm);
								orig = bm;
							}
						});
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						// print(e.toString());
					}
				}

			}).start();
		}
		else if (v.getId() == R.id.buttonSubmit)
		{
			ivBefore.setImageBitmap(orig);
			alterPhoto();
		}
		else if (v.getId() == R.id.buttonDone)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			LayoutInflater inflater = getLayoutInflater();
			builder.setTitle("Before you leave...");
			builder.setMessage("Would would you like to do with your masterpiece?");
			final View view = (inflater.inflate(R.layout.dialog_save_griddler, null));
			builder.setView(view);
			// Only show save if the user actually did something.
			if (this.ivAfter.getHeight() > 5)
			{
				builder.setPositiveButton("Save!", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						// Save the stuff.
						// Implement this in a bit.

						EditText etName = (EditText) view.findViewById(R.id.etName);
						EditText etTags = (EditText) view.findViewById(R.id.etTags);
						SeekBar sbDifficulty = (SeekBar) view.findViewById(R.id.sbDiff);
						
						String name = etName.getText().toString();
						String difficulty = "Easy";
						
						if(sbDifficulty.getProgress() == 1)
						{
							difficulty = "Medium";
						}
						else if(sbDifficulty.getProgress() == 2)
						{
							difficulty = "Hard";
						}			 
						else
						{
							difficulty = "Extreme";
						}
						// 2 Create Custom We'll~see 0 0 0 0,
						Intent returnIntent = new Intent();
						returnIntent.putExtra("solution", solution + "");
						returnIntent.putExtra("author", "justinwarner");	//This must be changed to current user.
						returnIntent.putExtra("name", name + "");	
						returnIntent.putExtra("rank", "1");	//Give them a point ;).
						returnIntent.putExtra("difficulty", difficulty + "");
						returnIntent.putExtra("width", xNum + "");
						returnIntent.putExtra("height", yNum + "");
						returnIntent.putExtra("tags", etTags.getText().toString().replace(" ", ","));
						setResult(RESULT_OK, returnIntent);
						finish();
					}
				});
			}
			// Add the buttons
			builder.setNegativeButton("Menu", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					finish();
				}
			});

			builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					dialog.dismiss();
				}
			});
			// Create the AlertDialog
			AlertDialog dialog = builder.create();
			dialog.show();

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
				ivBefore.setImageBitmap(photo);
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
				ivBefore.setImageBitmap(bi);
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
			// Touch this up. It's a bit messy.
			numColors = Integer.parseInt(sColor.getSelectedItem().toString());
			yNum = Integer.parseInt(sY.getSelectedItem().toString());
			xNum = Integer.parseInt(sX.getSelectedItem().toString());
			Bitmap scaled = Bitmap.createScaledBitmap(orig, xNum * 10, yNum * 10, false);
			ivBefore.setImageBitmap(scaled);
			Bitmap alter = Bitmap.createScaledBitmap(orig, xNum, yNum, false);
			//Set pixels = to each pixel in the scaled image (Easier to find values, and smaller!)
			int pixels[] = new int[xNum * yNum];
			alter.getPixels(pixels, 0, alter.getWidth(), 0, 0, alter.getWidth(), alter.getHeight());
			TextView tv = (TextView) findViewById(R.id.tv);//For debuging.
			String temp = "";
			for (int i = 0; i < pixels.length; i++)
			{
				int r = (pixels[i]) >> 16 & 0xff;
				int g = (pixels[i]) >> 8 & 0xff;
				int b = (pixels[i]) & 0xff;
				pixels[i] = (r + g + b) / 3;	//Convert the values in pixels to be grey values.  Or normalize them.
			}
			int pix[][] = new int[yNum][xNum];	//Height, then width per a height.
			int run = 0;
			for (int i = 0; i < pix.length; i++)
			{
				for (int j = 0; j < pix[i].length; j++)
				{
					pix[i][j] = pixels[run++];	//Griddlers go column by column, not row by row.
					temp += pix[i][j] + " ";
				}
				temp += "\n";
			}
			temp += "\n";
			temp += "\n";
			temp += "\n";
			for (int i = 0; i < alter.getWidth(); i++)
			{
				for (int j = 0; j < alter.getHeight(); j++)
				{
					if (pix[j][i] >= 256 / numColors)
					{
						alter.setPixel(i, j, Color.WHITE); // Change color in an
															// array. Get to it
															// later.
						pix[j][i] = 0;
						temp += "0";
					}
					else
					{
						alter.setPixel(i, j, Color.BLACK);
						pix[j][i] = 1;
						temp += "1";
					}
				}
				temp += "\n";
			}
			temp += "\n";
			temp += "\n";
			temp += "\n";
			// Set up "solution" for when it's submitted, this requires us to go
			for (int i =  0; i < pix[0].length; i++)
			{
				for (int j = 0; j < pix.length; j++)
				{
					solution += pix[j][i];
					current += "0";
					temp+=pix[j][i];
				}
				temp+="\n";
			}
			alter = Bitmap.createScaledBitmap(alter, yNum * 10, xNum * 10, false);
			this.ivAfter.setImageBitmap(alter);
			/*
			 * 3 5
			 * 111000111111100
			*/
			temp+=solution;
			tv.setText(temp);
		}
		else
		{
			print("We need a valid photo first.");
		}
	}

	private void print(String t)
	{
		Toast.makeText(this, t, Toast.LENGTH_LONG).show();
	}

}
