package com.pic2griddler.awesomeness;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

public class TouchImageView extends ImageView {

	Matrix matrix = new Matrix();

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// Remember some things for zooming
	PointF last = new PointF();
	PointF start = new PointF();
	float minScale = 1f;
	float maxScale = 3f;
	float[] m;

	float redundantXSpace, redundantYSpace;

	float width, height;
	static final int CLICK = 3;

	private static final String TAG = "TOUCHIMAGEVIEW";
	float saveScale = 1f;
	float right, bottom, origWidth, origHeight, bmWidth, bmHeight;

	ScaleGestureDetector mScaleDetector;

	Context context;

	// Control whether we're moving around or in actual gameplay mode.
	boolean isGameplay = false;

	// Griddler specifics.
	String gCurrent, gSolution;
	int gWidth, gHeight, gId;

	public TouchImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup(context);
	}

	public TouchImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(context);
	}

	public TouchImageView(Context context) {
		super(context);
		setup(context);

	}

	private void setup(Context context) {
		super.setClickable(true);
		this.context = context;
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		matrix.setTranslate(1f, 1f);
		m = new float[9];
		setImageMatrix(matrix);
		setScaleType(ScaleType.MATRIX);

		setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				if (!isGameplay) {
					mScaleDetector.onTouchEvent(event);

					matrix.getValues(m);
					float x = m[Matrix.MTRANS_X];
					float y = m[Matrix.MTRANS_Y];
					PointF curr = new PointF(event.getX(), event.getY());

					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						last.set(event.getX(), event.getY());
						start.set(last);
						mode = DRAG;
						break;
					case MotionEvent.ACTION_MOVE:
						if (mode == DRAG) {
							float deltaX = curr.x - last.x;
							float deltaY = curr.y - last.y;
							float scaleWidth = Math.round(origWidth * saveScale);
							float scaleHeight = Math.round(origHeight * saveScale);
							if (scaleWidth < width) {
								deltaX = 0;
								if (y + deltaY > 0)
									deltaY = -y;
								else if (y + deltaY < -bottom)
									deltaY = -(y + bottom);
							} else if (scaleHeight < height) {
								deltaY = 0;
								if (x + deltaX > 0)
									deltaX = -x;
								else if (x + deltaX < -right)
									deltaX = -(x + right);
							} else {
								if (x + deltaX > 0)
									deltaX = -x;
								else if (x + deltaX < -right)
									deltaX = -(x + right);

								if (y + deltaY > 0)
									deltaY = -y;
								else if (y + deltaY < -bottom)
									deltaY = -(y + bottom);
							}
							matrix.postTranslate(deltaX, deltaY);
							last.set(curr.x, curr.y);
						}
						break;

					case MotionEvent.ACTION_UP:
						mode = NONE;
						int xDiff = (int) Math.abs(curr.x - start.x);
						int yDiff = (int) Math.abs(curr.y - start.y);
						if (xDiff < CLICK && yDiff < CLICK)
							performClick();
						break;

					case MotionEvent.ACTION_POINTER_UP:
						mode = NONE;
						break;
					}
					setImageMatrix(matrix);
					invalidate();
					return true; // indicate event was handled
				} else {
					// If we're playing, we can't move the screen around and
					// whatnot.

					// Update the current image to follow changes in the current
					// array.
					bitmapFromCurrent();

					return true;
				}
			}

		});
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		bmWidth = bm.getWidth();
		bmHeight = bm.getHeight();
	}

	public void setMaxZoom(float x) {
		maxScale = x;
	}

	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			mode = ZOOM;
			return true;
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float mScaleFactor = (float) Math.min(Math.max(.95f, detector.getScaleFactor()), 1.05);
			float origScale = saveScale;
			saveScale *= mScaleFactor;
			if (saveScale > maxScale) {
				saveScale = maxScale;
				mScaleFactor = maxScale / origScale;
			} else if (saveScale < minScale) {
				saveScale = minScale;
				mScaleFactor = minScale / origScale;
			}
			right = width * saveScale - width - (2 * redundantXSpace * saveScale);
			bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
			if (origWidth * saveScale <= width || origHeight * saveScale <= height) {
				matrix.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2);
				if (mScaleFactor < 1) {
					matrix.getValues(m);
					float x = m[Matrix.MTRANS_X];
					float y = m[Matrix.MTRANS_Y];
					if (mScaleFactor < 1) {
						if (Math.round(origWidth * saveScale) < width) {
							if (y < -bottom)
								matrix.postTranslate(0, -(y + bottom));
							else if (y > 0)
								matrix.postTranslate(0, -y);
						} else {
							if (x < -right)
								matrix.postTranslate(-(x + right), 0);
							else if (x > 0)
								matrix.postTranslate(-x, 0);
						}
					}
				}
			} else {
				matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
				matrix.getValues(m);
				float x = m[Matrix.MTRANS_X];
				float y = m[Matrix.MTRANS_Y];
				if (mScaleFactor < 1) {
					if (x < -right)
						matrix.postTranslate(-(x + right), 0);
					else if (x > 0)
						matrix.postTranslate(-x, 0);
					if (y < -bottom)
						matrix.postTranslate(0, -(y + bottom));
					else if (y > 0)
						matrix.postTranslate(0, -y);
				}
			}
			return true;

		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		width = MeasureSpec.getSize(widthMeasureSpec);
		height = MeasureSpec.getSize(heightMeasureSpec);
		// Fit to screen.
		float scale;
		float scaleX = (float) width / (float) bmWidth;
		float scaleY = (float) height / (float) bmHeight;
		scale = Math.min(scaleX, scaleY);
		matrix.setScale(scale, scale);
		setImageMatrix(matrix);
		saveScale = 1f;

		// Center the image
		redundantYSpace = (float) height - (scale * (float) bmHeight);
		redundantXSpace = (float) width - (scale * (float) bmWidth);
		redundantYSpace /= (float) 2;
		redundantXSpace /= (float) 2;

		matrix.postTranslate(redundantXSpace, redundantYSpace);

		origWidth = width - 2 * redundantXSpace;
		origHeight = height - 2 * redundantYSpace;
		right = width * saveScale - width - (2 * redundantXSpace * saveScale);
		bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
		setImageMatrix(matrix);
	}

	// Get bundled info and set it for use.
	public void setGriddlerInfo(Bundle savedInstanceState) {
		gCurrent = savedInstanceState.getString("current");
		gHeight = Integer.parseInt(savedInstanceState.getString("height"));
		gWidth = Integer.parseInt(savedInstanceState.getString("width"));
		gId = Integer.parseInt(savedInstanceState.getString("id"));
		gSolution = savedInstanceState.getString("solution");
		bitmapFromCurrent();
	}

	// Convert current String to a bitmap that's drawable.
	// This will draw everything: grid, numbers, and onclicks.
	private void bitmapFromCurrent() {
		int colors[] = new int[gCurrent.length()];
		for (int i = 0; i < colors.length; i++) {
			if (gCurrent.charAt(i) == '1') {
				colors[i] = Color.BLACK;
			} else {
				colors[i] = Color.WHITE;
			}
		}
		// Add numbers
		// Build up outside, numbers for puzzle.
		// ArrayList's for top and side numbers.
		ArrayList<String> top = new ArrayList<String>();
		ArrayList<String> side = new ArrayList<String>();

		char current2D[][] = convertOneDimensionalToTwoDimensional(gHeight, gWidth, gCurrent.toCharArray());
		// Top
		for (int i = 0; i < gWidth; i++) {
			String temp = "";
			char prev = current2D[i][0];
			int iter = 0;
			for (int j = 0; j < gHeight; j++) {
				// Make sure we aren't a background piece.
				if (current2D[i][j] != '0') {
					if (prev == current2D[i][j]) {
						// Still in same series.
						iter++;
					}
				} else {
					if (iter > 0) {
						// We found one series. Add to temp.
						temp += iter + " ";
						iter = 0;// reset iter.
					}
				}
			}
			if (iter > 0) {
				temp += iter + " ";
			}
			top.add(temp);
		}
		// Side
		for (int i = 0; i < gHeight; i++) {
			String temp = "";
			char prev = current2D[i][0];
			int iter = 0;
			for (int j = 0; j < gWidth; j++) {
				// Make sure we aren't a background piece.
				if (current2D[i][j] != '0') {
					if (prev == current2D[i][j]) {
						// Still in same series.
						iter++;
					}
				} else {
					if (iter > 0) {
						// We found one series. Add to temp.
						temp += iter + " ";
						iter = 0;// reset iter.
					}
				}
			}
			if (iter > 0) {
				temp += iter + " ";
			}
			side.add(temp);
		}
		// Resize bitmap according to longest length of number clues.
		int longestTop = 0, longestSide = 0;
		for (int i = 0; i < top.size(); i++) {
			if (top.get(i).length() > longestTop) {
				longestTop = top.get(i).length();
			}
		}
		for (int i = 0; i < side.size(); i++) {
			if (side.get(i).length() > longestSide) {
				longestSide = side.get(i).length();
			}
		}
		longestSide = longestSide / 2;
		longestTop = longestTop / 2;
		ArrayList<Integer> colorFinal = new ArrayList<Integer>();
		for (int i = 0; i < longestTop; i++) {
			// Add in fluff pixels for location of numbers on the top.
			// Add in the extra width from the sides too.
			for (int j = 0; j < gWidth + longestSide; j++) {
				colorFinal.add(Color.WHITE);
			}
		}
		// Top section covered, now do the sides and actual image.
		int runner = 0; // runner marks progress in actual image.
		for (int i = 0; i < gWidth; i++) {
			// First add fluff for side.
			for (int j = 0; j < longestSide; j++) {
				colorFinal.add(Color.WHITE);
			}
			// Now add the picture part for this row of pixels.
			for (int j = 0; j < gWidth; j++, runner++) {
				colorFinal.add(colors[runner]);
			}
		}
		int[] color = new int[colorFinal.size()];
		for (int i = 0; i < colorFinal.size(); i++) {
			color[i] = colorFinal.get(i);
		}
		// Create bitmap with padding for the numbers.
		Bitmap bm = Bitmap.createScaledBitmap(Bitmap.createBitmap(color, gWidth + longestSide, gHeight + longestTop, Bitmap.Config.ARGB_8888), gWidth * 100, gHeight * 100, false);
		colors = new int[bm.getWidth() * bm.getHeight()];
		bm.getPixels(colors, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());

		Canvas c = new Canvas(bm);
		Paint p = new Paint();
		p.setColor(Color.RED);

		int lw = 100; // size of the initial square to fill in.
		p.setStrokeWidth(5);
		for (int i = longestTop; i < c.getHeight(); i++) {	//Side to side.
			c.drawLine(0, i * (c.getHeight() / (gHeight + longestTop)), c.getWidth(), i * (c.getHeight() / (gHeight + longestTop)), p);
		}
		c.drawLine(0, c.getHeight() * (c.getHeight() / gHeight), c.getWidth(), c.getHeight() * (c.getHeight() / gHeight), p);
		for (int i = longestSide; i < c.getWidth(); i++) {	//Up and down.
			c.drawLine(i * (c.getWidth() / (gWidth + longestSide)), 0, i * (c.getHeight() / (gHeight + longestSide)), c.getHeight(), p);
		}
		c.drawLine(c.getWidth() * (bm.getWidth() / gWidth), 0, c.getWidth() * (c.getHeight() / gHeight), c.getHeight(), p);

		// --------------------------------------------------

		// Draw the number clues.
		p.setTextSize(50);
		p.setAntiAlias(true);
		p.setTypeface(Typeface.MONOSPACE); // Typeface.createFromAsset(getAssets(),
											// "myfont.ttf");
		p.setTextAlign(Align.RIGHT);
		for (int i = 0; i < top.size(); i++) {
		}
		for (int i = 0; i < side.size(); i++) {
			c.drawText(top.get(i), (longestSide - 1) * lw, (longestTop * lw) + (i * lw), p);
		}
		setImageBitmap(bm); // bm should contain all info we changed. Don't
							// worry.

	}

	// Site
	// http://stackoverflow.com/questions/8629202/fast-conversion-from-one-dimensional-array-to-two-dimensional-in-java
	private char[][] convertOneDimensionalToTwoDimensional(int numberOfRows, int rowSize, char[] srcMatrix) {
		int srcMatrixLength = srcMatrix.length;
		int srcPosition = 0;

		char[][] returnMatrix = new char[numberOfRows][];
		for (int i = 0; i < numberOfRows; i++) {
			char[] row = new char[rowSize];
			int nextSrcPosition = srcPosition + rowSize;
			if (srcMatrixLength >= nextSrcPosition) {
				// Copy the data from the file if it has been written before.
				// Otherwise we just keep row empty.
				System.arraycopy(srcMatrix, srcPosition, row, 0, rowSize);
			}
			returnMatrix[i] = row;
			srcPosition = nextSrcPosition;
		}
		return returnMatrix;
	}
}