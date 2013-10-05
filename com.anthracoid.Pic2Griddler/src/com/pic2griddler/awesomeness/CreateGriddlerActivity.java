
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
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.agimind.widget.SlideHolder;
import com.agimind.widget.SlideHolder.OnSlideListener;
import com.gesturetutorial.awesomeness.TutorialView;
import com.github.espiandev.showcaseview.ShowcaseView;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;

public class CreateGriddlerActivity extends Activity implements OnClickListener,
        OnWheelChangedListener, OnSeekBarChangeListener, OnTouchListener {
    private static final int CAMERA_REQUEST_CODE = 1888, FILE_SELECT_CODE = 1337;
    private static final String TAG = "CreateGriddlerActivity";
    private EditText etURL;
    private int numColors, yNum, xNum;
    private Bitmap oldPicture, newPicture;
    private String solution = "", griddlerName, griddlerTags;
    private WheelView sX, sY, sColor, sDiff;
    private SeekBar sbTransparency;
    ShowcaseView sv;
    ImageView ivOld, ivNew;
    Handler handler = new Handler();
    Button bURLSubmit;
    int colors[] = {
            Color.WHITE, Color.BLACK, Color.GRAY, Color.BLUE, Color.YELLOW, Color.RED, Color.GREEN,
            Color.CYAN, Color.MAGENTA, Color.DKGRAY, Color.LTGRAY
    };

    int whitePlotterColor = 0;

    boolean[][] visits;

    static TutorialView tv;

    private void alterPhoto() {
        this.visits = null;
        if (this.oldPicture != null) {
            // Touch this up. It's a bit messy.
            this.solution = ""; // Change back to nothing.
            this.numColors = Integer.parseInt(((NumericWheelAdapter) this.sColor.getViewAdapter())
                    .getItemText(this.sColor.getCurrentItem()).toString());
            this.yNum = Integer.parseInt(((NumericWheelAdapter) this.sY.getViewAdapter())
                    .getItemText(this.sY.getCurrentItem()).toString());
            this.xNum = Integer.parseInt(((NumericWheelAdapter) this.sX.getViewAdapter())
                    .getItemText(this.sX.getCurrentItem()).toString());
            Bitmap alter = Bitmap.createScaledBitmap(this.oldPicture, this.xNum, this.yNum, false);
            // Set pixels = to each pixel in the scaled image (Easier to find
            // values, and smaller!)
            final int pixels[] = new int[this.xNum * this.yNum];
            alter.getPixels(pixels, 0, alter.getWidth(), 0, 0, alter.getWidth(), alter.getHeight());
            for (int i = 0; i < pixels.length; i++) {
                final int r = ((pixels[i]) >> 16) & 0xff;
                final int g = ((pixels[i]) >> 8) & 0xff;
                final int b = (pixels[i]) & 0xff;
                pixels[i] = (r + g + b) / 3; // Greyscale
            }
            alter.setPixels(pixels, 0, alter.getWidth(), 0, 0, alter.getWidth(), alter.getHeight());

            final int pix[][] = new int[this.yNum][this.xNum];
            int run = 0;
            for (int i = 0; i < pix.length; i++) {
                for (int j = 0; j < pix[i].length; j++) {
                    pix[i][j] = pixels[run++];
                }
            }

            run = 0;
            for (int i = 0; i != alter.getHeight(); ++i) {
                for (int j = 0; j != alter.getWidth(); ++j) {
                    for (int k = 0; k <= this.numColors; ++k)
                    {
                        if (pix[i][j] < ((256 * (k + 1)) / this.numColors))
                        {
                            Log.d(TAG, k + " " + this.numColors);
                            pix[i][j] = this.colors[k];
                            alter.setPixel(j, i, this.colors[k]);
                            break;
                        }
                    }
                }
            }
            // Set up "solution" for when it's submitted, this requires us to go
            final char[] sol = new char[this.xNum * this.yNum];
            for (int i = 0; i < pix.length; i++) {
                for (int j = 0; j < pix[i].length; j++) {
                    sol[(i * j) + j] += pix[i][j];
                }
            }
            this.solution = sol.toString();
            alter = Bitmap.createScaledBitmap(alter, this.xNum * 10, this.yNum * 10, false);
            this.newPicture = alter;
            this.ivOld.setImageBitmap(Bitmap.createScaledBitmap(this.oldPicture, this.xNum * 10,
                    this.yNum * 10, true));
            this.ivNew.setImageBitmap(this.newPicture);
        } else {
            Crouton.makeText(this, "=( We need a picture first.", Style.INFO).show();
        }
    }

    public void colorChanged(final String key, final int color) {
        // TODO Auto-generated method stub
        Log.d(TAG, key + " " + color);
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
        if (request == CAMERA_REQUEST_CODE) {
            if (result == Activity.RESULT_OK) {
                final Bitmap photo = (Bitmap) data.getExtras().get("data");
                this.oldPicture = photo;
            }
        } else if (request == FILE_SELECT_CODE) {
            if (result == Activity.RESULT_OK) {
                final Uri uri = data.getData();
                final Bitmap bi = this.readBitmap(uri);
                this.oldPicture = bi;
            }
        }
        if (result == Activity.RESULT_OK)
        {
            this.etURL.setVisibility(View.INVISIBLE);
            this.bURLSubmit.setVisibility(View.INVISIBLE);
            this.ivOld.setImageBitmap(this.oldPicture);
            this.alterPhoto();
        }
        else
        {
            this.print("Aww, we wanted your picture =(");
            Crouton.makeText(this, "=( We wanted your picture!", Style.CONFIRM).show();
        }
    }

    public void onChanged(final WheelView wv, final int oldVal, final int newVal) {
        if (this.oldPicture != null) {
            this.alterPhoto();
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
                    this.sDiff.getCurrentItem() + "");
            returnIntent.putExtra("width", this.xNum + "");
            returnIntent.putExtra("height", this.yNum + "");
            returnIntent.putExtra("tags", this.griddlerTags);
            this.setResult(RESULT_OK, returnIntent);
            this.finish();
        }
        else if (v.getId() == R.id.bChangeColor)
        {
            Crouton.makeText(this, "To change the color, please click on a color in the image.",
                    Style.INFO).show();
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
        final Button ccButton = (Button) this.findViewById(R.id.bChangeColor);
        this.bURLSubmit = (Button) this.findViewById(R.id.bURLSubmit);
        photoButton.setOnClickListener(this);
        fileButton.setOnClickListener(this);
        urlButton.setOnClickListener(this);
        submitButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        ccButton.setOnClickListener(this);
        this.bURLSubmit.setOnClickListener(this);
        // Initially invisible for URL.
        this.etURL = (EditText) this.findViewById(R.id.etURLA);
        this.etURL.setVisibility(View.INVISIBLE);
        this.bURLSubmit.setVisibility(View.INVISIBLE);

        this.ivOld = (ImageView) this.findViewById(R.id.ivOld);
        this.ivNew = (ImageView) this.findViewById(R.id.ivNew);
        this.ivNew.setOnTouchListener(this);

        this.sbTransparency = (SeekBar) this.findViewById(R.id.sbTransparency);
        this.sbTransparency.setOnSeekBarChangeListener(this);

        // Add items to spinners... Might be a better way to do this, seriously,
        // this is idiotic.
        this.sX = (WheelView) this.findViewById(R.id.spinWidth);
        this.sY = (WheelView) this.findViewById(R.id.spinHeight);
        this.sColor = (WheelView) this.findViewById(R.id.spinColor);
        this.sDiff = (WheelView) this.findViewById(R.id.spinDiffA);
        this.sX.setCyclic(true);
        this.sY.setCyclic(true);
        this.sColor.setCyclic(true);
        this.sDiff.setCyclic(true);
        this.sX.setVisibleItems(3);
        this.sY.setVisibleItems(3);
        this.sColor.setVisibleItems(3);
        this.sDiff.setVisibleItems(2);
        this.sX.addChangingListener(this);
        this.sY.addChangingListener(this);
        this.sColor.addChangingListener(this);
        final String difficulties[] = {
                "Easy", "Medium", "Hard", "Extreme"
        };
        this.sX.setViewAdapter(new NumericWheelAdapter(this, 1, 100));
        this.sY.setViewAdapter(new NumericWheelAdapter(this, 1, 100));
        this.sColor.setViewAdapter(new NumericWheelAdapter(this, 2, 9));

        this.sX.setCurrentItem(3);
        this.sY.setCurrentItem(3);
        this.sColor.setCurrentItem(2);
        final ArrayWheelAdapter<String> diffs = new ArrayWheelAdapter<String>(this, difficulties);
        this.sDiff.setViewAdapter(diffs);

        // Check if we're getting data from a share.
        final Intent intent = this.getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && (type != null)) {
            if (type.startsWith("image/")) {
                final Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                final Bitmap bi = this.readBitmap(uri);
                this.oldPicture = bi;
                this.ivOld.setImageBitmap(this.oldPicture);
            }
        }

        // SlideHolder.
        final SlideHolder sh = (SlideHolder) this.findViewById(R.id.slideHolder);

        if (this.isTabletDevice(this.getResources())) {
            sh.setAlwaysOpened(true);
        }
        else
        {
            // final View gif = this.findViewById(R.id.wvGif);
            final ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
            co.hideOnClickOutside = true;
            this.sv = ShowcaseView
                    .insertShowcaseView(
                            R.id.ivOld,
                            this,
                            "Swipe left to right.",
                            "The toolbox contains all you'll need to create beautiful griddlers.  Click one of the three buttons at the top to start!",
                            co);
            sh.setDirection(SlideHolder.DIRECTION_LEFT);
            sh.setOnSlideListener(new OnSlideListener() {

                public void onSlideCompleted(final boolean isOpen) {
                    if (isOpen)
                    {
                        tv.hide();
                        // gif.setVisibility(View.INVISIBLE);
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

    @Override
    protected void onDestroy() {
        // Workaround until there's a way to detach the Activity from Crouton
        // while
        // there are still some in the Queue.
        Crouton.clearCroutonsForActivity(this);
        super.onDestroy();
    }

    public void onItemSelected(final AdapterView<?> p, final View v, final int pos, final long id) {
        // When item is changed, update.'
        Log.d(TAG, pos + "");
        if (pos >= 1) {
            if (this.oldPicture != null) {
                this.alterPhoto();
            }
        }
    }

    public void onNothingSelected(final AdapterView<?> arg0) {
    }

    public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
        if (seekBar.getId() == R.id.sbTransparency)
        {
            this.ivOld.setAlpha(progress);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        final TutorialView wv = (TutorialView) this.findViewById(R.id.wvGif);
        if (!this.isTabletDevice(this.getResources()))
        {
            // wv.loadUrl("file:///android_asset/LeftToRight.gif");
        } else {
            // wv.setVisibility(View.GONE);
        }
        // wv.startTutorial(this, "LeftToRight", null).show();
        final View v = this.findViewById(android.R.id.content);
        tv = TutorialView.create(this, TutorialView.LeftToRight, 0, v).show();
    }

    public void onStartTrackingTouch(final SeekBar seekBar) {
    }

    public void onStopTrackingTouch(final SeekBar seekBar) {
    }

    public boolean onTouch(final View v, final MotionEvent event) {
        // We're changing a color.
        if (this.newPicture != null)
        {
            final float eventX = event.getX();
            final float eventY = event.getY();
            final float[] eventXY = new float[] {
                    eventX, eventY
            };

            final Matrix invertMatrix = new Matrix();
            this.ivNew.getImageMatrix().invert(invertMatrix);

            invertMatrix.mapPoints(eventXY);
            int x = Integer.valueOf((int) eventXY[0]);
            int y = Integer.valueOf((int) eventXY[1]);

            final Drawable imgDrawable = this.ivNew.getDrawable();
            final Bitmap bitmap = ((BitmapDrawable) imgDrawable).getBitmap();

            // Limit x, y range within bitmap
            if (x < 0) {
                x = 0;
            } else if (x > (bitmap.getWidth() - 1)) {
                x = bitmap.getWidth() - 1;
            }

            if (y < 0) {
                y = 0;
            } else if (y > (bitmap.getHeight() - 1)) {
                y = bitmap.getHeight() - 1;
            }

            final int touchedRGB = bitmap.getPixel(x, y);

            // initialColor is the initially-selected color to be shown in the
            // rectangle on the left of the arrow.
            // for example, 0xff000000 is black, 0xff0000ff is blue. Please be
            // aware
            // of the initial 0xff which is the alpha.
            final AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, touchedRGB,
                    new OnAmbilWarnaListener() {

                        public void onCancel(final AmbilWarnaDialog dialog) {
                            // TODO Auto-generated method stub

                        }

                        public void onOk(final AmbilWarnaDialog dialog, final int color) {
                            // TODO Auto-generated method stub

                        }
                    });

            dialog.show();
            return true;
        } else {
            return false;
        }
    }

    private void print(final String t) {
        Toast.makeText(this, t, Toast.LENGTH_SHORT).show();
    }

    // Read bitmap - From
    // http://tutorials-android.blogspot.co.il/2011/11/outofmemory-exception-when-decoding.html
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
                    CreateGriddlerActivity.this.ivOld.post(new Runnable() {
                        public void run() {
                            CreateGriddlerActivity.this.oldPicture = bm;
                            CreateGriddlerActivity.this.ivOld
                                    .setImageBitmap(CreateGriddlerActivity.this.oldPicture);
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
