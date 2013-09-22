
package com.pic2griddler.awesomeness;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.agimind.widget.SlideHolder;
import com.agimind.widget.SlideHolder.OnSlideListener;
import com.github.espiandev.showcaseview.ShowcaseView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CreateGriddlerActivity extends Activity implements OnClickListener,
        OnItemSelectedListener {
    private static final int CAMERA_REQUEST_CODE = 1888, FILE_SELECT_CODE = 1337;
    private static final String TAG = "CreateGriddlerActivity";
    private EditText etURL;
    private boolean isOriginal = true;
    private int numColors, yNum, xNum;
    private Bitmap oldPicture, newPicture;
    private String solution = "", current = "", griddlerName, griddlerTags;
    private Spinner sX, sY, sColor, sDiff;
    ShowcaseView sv;
    TouchImageView tiv;
    Handler handler = new Handler();
    Button bURLSubmit;

    private void alterPhoto() {
        if (this.oldPicture != null) {
            // Touch this up. It's a bit messy.
            this.solution = ""; // Change back to nothing.
            this.numColors = Integer.parseInt(this.sColor.getSelectedItem().toString());
            this.yNum = Integer.parseInt(this.sY.getSelectedItem().toString());
            this.xNum = Integer.parseInt(this.sX.getSelectedItem().toString());
            final Bitmap scaled = Bitmap.createScaledBitmap(this.oldPicture, this.xNum * 10,
                    this.yNum * 10, false);
            // this.ivPicture.setImageBitmap(scaled);
            this.tiv.setImageBitmap(scaled);
            Bitmap alter = Bitmap.createScaledBitmap(this.oldPicture, this.xNum, this.yNum, false);
            // Set pixels = to each pixel in the scaled image (Easier to find
            // values, and smaller!)
            final int pixels[] = new int[this.xNum * this.yNum];
            alter.getPixels(pixels, 0, alter.getWidth(), 0, 0, alter.getWidth(), alter.getHeight());
            for (int i = 0; i < pixels.length; i++) {
                final int r = ((pixels[i]) >> 16) & 0xff;
                final int g = ((pixels[i]) >> 8) & 0xff;
                final int b = (pixels[i]) & 0xff;
                pixels[i] = (r + g + b) / 3; // Convert the values in pixels to
                                             // be grey values. Or normalize
                                             // them.
            }
            final int pix[][] = new int[this.yNum][this.xNum]; // Height, then
                                                               // width per a
            // height.
            int run = 0;
            for (int i = 0; i < pix.length; i++) {
                for (int j = 0; j < pix[i].length; j++) {
                    pix[i][j] = pixels[run++];
                }
            }
            for (int i = 0; i < alter.getWidth(); i++) {
                for (int j = 0; j < alter.getHeight(); j++) {
                    if (pix[j][i] >= (256 / this.numColors)) {
                        alter.setPixel(i, j, Color.WHITE); // Change color in an
                                                           // array. Get to it
                                                           // later.
                        pix[j][i] = 0;
                    } else {
                        alter.setPixel(i, j, Color.BLACK);
                        pix[j][i] = 1;
                    }
                }
            }
            // Set up "solution" for when it's submitted, this requires us to go
            for (int i = 0; i < pix.length; i++) {
                for (int j = 0; j < pix[i].length; j++) {
                    this.solution += pix[i][j];
                    this.current += "0";
                }
            }
            alter = Bitmap.createScaledBitmap(alter, this.xNum * 10, this.yNum * 10, false);
            this.newPicture = alter;
            this.changePictures(); // TODO: Add button to change pictures.
        } else {
            this.print("We need a valid photo first.");
        }
    }

    private void changePictures() {
        if (this.isOriginal) // Go to the new picture.
        {
            if (this.newPicture != null) {
                this.tiv.setImageBitmap(this.newPicture);
                this.isOriginal = false;
            }
        } else
        // Go to the old picture.
        {
            this.tiv.setImageBitmap(this.oldPicture);
            this.isOriginal = true;
        }
    }

    // http://stackoverflow.com/questions/5832368/tablet-or-phone-android
    private boolean isTabletDevice(final Resources resources) {
        final int screenLayout = resources.getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK;
        final boolean isScreenLarge = (screenLayout == Configuration.SCREENLAYOUT_SIZE_LARGE);
        final boolean isScreenXlarge = (screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE);
        return (isScreenLarge || isScreenXlarge);
    }

    @Override
    protected void onActivityResult(final int request, final int result, final Intent data) {
        if (result == Activity.RESULT_OK)
        {
            this.etURL.setVisibility(View.INVISIBLE);
            this.bURLSubmit.setVisibility(View.INVISIBLE);
        }
        else
        {
            this.print("Aww, we wanted your picture =(");
        }
        if (request == CAMERA_REQUEST_CODE) {
            if (result == Activity.RESULT_OK) {
                final Bitmap photo = (Bitmap) data.getExtras().get("data");
                this.tiv.setImageBitmap(photo);
                this.oldPicture = photo;
            }
        } else if (request == FILE_SELECT_CODE) {
            if (result == Activity.RESULT_OK) {
                final Uri uri = data.getData();
                final Bitmap bi = this.readBitmap(uri);
                this.tiv.setImageBitmap(bi);
                this.oldPicture = bi;
            }
        }
    }

    public void onClick(final View v) {
        // "Hide" the keyboard when you move steps.
        final InputMethodManager imm = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(((EditText) this.findViewById(R.id.etNameA)).getWindowToken(),
                0);
        if (v.getId() == R.id.bURLA)
        {
            // Make the EditText visible.
            this.etURL.setVisibility(View.VISIBLE);
            this.bURLSubmit.setVisibility(View.VISIBLE);
        }
        else if (v.getId() == R.id.bCameraA)
        {
            final Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            this.startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        }
        else if (v.getId() == R.id.bFileA)
        {
            // File stuff.
            final Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
            fileIntent.setType("image/*");
            fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
            // Shows openable files! =)
            this.startActivityForResult(fileIntent, FILE_SELECT_CODE);
        }
        else if (v.getId() == R.id.bURLSubmit)
        {
            this.processURL();
            this.alterPhoto();
        }
        else if (v.getId() == R.id.bCancel)
        {
            this.finish();
        }
        else if (v.getId() == R.id.bSubmit)
        {// Now save. final Intent returnIntent = new Intent();
            final Intent returnIntent = new Intent();
            returnIntent.putExtra("solution", this.solution);
            final String username = "justinwarner";
            returnIntent.putExtra("author", username);
            returnIntent.putExtra("name", this.griddlerName);
            returnIntent.putExtra("rank", 1 + "");
            returnIntent.putExtra("difficulty",
                    this.sDiff.getItemAtPosition(this.sDiff.getSelectedItemPosition()).toString());
            returnIntent.putExtra("width", this.xNum + "");
            returnIntent.putExtra("height", this.yNum + "");
            returnIntent.putExtra("tags", this.griddlerTags);
            this.setResult(RESULT_OK, returnIntent);
            this.finish();
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_create_advanced);
        final Button photoButton = (Button) this.findViewById(R.id.bCameraA);
        final Button fileButton = (Button) this.findViewById(R.id.bFileA);
        final Button urlButton = (Button) this.findViewById(R.id.bURLA);
        final Button submitButton = (Button) this.findViewById(R.id.bSubmit);
        final Button cancelButton = (Button) this.findViewById(R.id.bCancel);
        this.bURLSubmit = (Button) this.findViewById(R.id.bURLSubmit);
        photoButton.setOnClickListener(this);
        fileButton.setOnClickListener(this);
        urlButton.setOnClickListener(this);
        submitButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        this.bURLSubmit.setOnClickListener(this);
        // Initially invisible for URL.
        this.etURL = (EditText) this.findViewById(R.id.etURLA);
        this.etURL.setVisibility(View.INVISIBLE);
        this.bURLSubmit.setVisibility(View.INVISIBLE);

        this.tiv = (TouchImageView) this.findViewById(R.id.tivPreview);
        this.tiv.setOnClickListener(this);

        // Add items to spinners... Might be a better way to do this, seriously,
        // this is idiotic.
        this.sX = (Spinner) this.findViewById(R.id.spinWidth);
        this.sY = (Spinner) this.findViewById(R.id.spinHeight);
        this.sColor = (Spinner) this.findViewById(R.id.spinColor);
        this.sDiff = (Spinner) this.findViewById(R.id.spinDiffA);
        this.sX.setOnItemSelectedListener(this);
        this.sY.setOnItemSelectedListener(this);
        this.sColor.setOnItemSelectedListener(this);
        final String colorNumbers[] = new String[9];
        final String xyNumbers[] = new String[20]; // Support more than 20 for
        // multi-griddlers in future.
        final String difficulties[] = {
                "Easy", "Medium", "Hard", "Extreme"
        };
        for (int i = 1; i < 21; i++) {
            xyNumbers[i - 1] = "" + i;
        }
        for (int i = 2; i < 11; i++) {
            colorNumbers[i - 2] = "" + i;
        }
        final ArrayAdapter xy = new ArrayAdapter(this, android.R.layout.simple_spinner_item,
                xyNumbers);
        final ArrayAdapter cols = new ArrayAdapter(this, android.R.layout.simple_spinner_item,
                colorNumbers);
        final ArrayAdapter diffs = new ArrayAdapter(this, android.R.layout.simple_spinner_item,
                difficulties);
        this.sX.setAdapter(xy);
        this.sY.setAdapter(xy);
        this.sColor.setAdapter(cols);
        this.sDiff.setAdapter(diffs);

        // Check if we're getting data from a share.
        final Intent intent = this.getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && (type != null)) {
            if (type.startsWith("image/")) {
                final Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                final Bitmap bi = this.readBitmap(uri);
                this.tiv.setImageBitmap(bi);
                this.oldPicture = bi;
            }
        }

        // SlideHolder.
        final SlideHolder sh = (SlideHolder) this.findViewById(R.id.slideHolder);

        if (this.isTabletDevice(this.getResources())) {
            sh.setAlwaysOpened(true);
        }
        else
        {
            final View gif = this.findViewById(R.id.wvGif);
            final ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
            co.hideOnClickOutside = true;
            this.sv = ShowcaseView
                    .insertShowcaseView(
                            R.id.tivPreview,
                            this,
                            "Swipe left to right.",
                            "The toolbox contains all you'll need to create beautiful griddlers.  Click one of the three buttons at the top to start!",
                            co);
            sh.setDirection(SlideHolder.DIRECTION_LEFT);
            sh.setOnSlideListener(new OnSlideListener() {

                public void onSlideCompleted(final boolean isOpen) {
                    if (isOpen)
                    {
                        gif.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        this.getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void onItemSelected(final AdapterView<?> p, final View v, final int pos, final long id) {
        // When item is changed, update.'
        Log.d(TAG, pos + "");
        if (pos >= 1) {
            this.alterPhoto();
        }
    }

    public void onNothingSelected(final AdapterView<?> arg0) {
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        final WebView wv = (WebView) this.findViewById(R.id.wvGif);
        if (!this.isTabletDevice(this.getResources()))
        {
            wv.loadUrl("file:///android_asset/LeftToRight.gif");
        } else {
            wv.setVisibility(View.GONE);
        }
    }

    private void print(final String t) {
        Toast.makeText(this, t, Toast.LENGTH_SHORT).show();
    }

    private void processURL()
    {
        new Thread(new Runnable() {
            public void run() {
                try {
                    CreateGriddlerActivity.this.etURL = (EditText) CreateGriddlerActivity.this
                            .findViewById(R.id.etURLA);
                    final URL url = new URL(CreateGriddlerActivity.this.etURL.getText()
                            .toString());
                    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream in;
                    in = conn.getInputStream();
                    final Bitmap bm = BitmapFactory.decodeStream(in);
                    CreateGriddlerActivity.this.oldPicture = bm;
                    CreateGriddlerActivity.this.tiv.post(new Runnable() {
                        public void run() {
                            CreateGriddlerActivity.this.tiv.setImageBitmap(bm);
                            CreateGriddlerActivity.this.oldPicture = bm;
                            CreateGriddlerActivity.this.alterPhoto();
                        }

                    });
                } catch (final IOException e) {
                    e.printStackTrace();
                    // print(e.toString());
                }
            }

        }).start();
        this.print("Saved picture from url.");
    }

    // Read bitmap - From
    // http://tutorials-android.blogspot.co.il/2011/11/outofmemory-exception-when-decoding.html
    public Bitmap readBitmap(final Uri selectedImage) {
        Bitmap bm = null;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 5;
        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = this.getContentResolver().openAssetFileDescriptor(selectedImage, "r");
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                bm = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null,
                        options);
                fileDescriptor.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return bm;
    }

}
