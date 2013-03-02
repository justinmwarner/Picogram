package com.pic2griddler.awesomeness;

import java.util.ArrayList;
import java.util.Collections;

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
		// Get a 2D array of "current" griddler.
		char current2D[][] = solutionTo2DArray();
		// Create bitmap based on the current. Make a int array with pixel
		// colors.
		int colors[] = getPixelArrayFromString(gSolution, gSolution.length());

		ArrayList<String> rows = getRows(current2D);
		ArrayList<String> columns = getColumns(current2D);

		ArrayList<String[]> topHints = getTopHints(columns); // Because of how
																// we're making
																// the
																// top hints, it
																// needs its own
																// method...
		ArrayList<String> sideHints = getSideHints(rows);

		int longestTop = topHints.size(); // Since this is layered, we just need
											// number of layers.
		int longestSide = getLongest(sideHints); // Get widest "layer"
		// Create bitmap with padding for the numbers.
		colors = resizeBitMapsForHints(colors, longestTop, longestSide);
		Bitmap bm = Bitmap.createScaledBitmap(Bitmap.createBitmap(colors, gWidth + longestSide, gHeight + longestTop, Bitmap.Config.ARGB_8888), gWidth * 100, gHeight * 100, false);
		Canvas c = new Canvas(bm);
		Paint p = new Paint();
		p.setColor(Color.RED);
		p.setStrokeWidth(4);
		// Change canvas and it'll reflect on the bm.

		drawOnCanvas(c, p, topHints, sideHints);
		setImageBitmap(bm); // bm should contain all info we changed. Don't
							// worry.
	}

	// Just add on fluff area for the hints on the top and on the side.
	private int[] resizeBitMapsForHints(int[] colors, int longestTop, int longestSide) {
		int result[] = new int[(longestTop * (longestSide + gWidth)) + colors.length + (gHeight * longestSide)];
		String temp = "";
		int runner;
		// Fill up the top with blank white.
		for (runner = 0; runner != (longestTop * (longestSide + gWidth)); ++runner) {
			result[runner] = Color.WHITE;
			temp += "W";
		}
		// Fill side hints with white, and the image with what was in it
		// previously.
		int colorRunner = 0; // Used to run through original colors.
		for (int i = 0; i != gHeight; ++i) {
			// Draw side for hints.
			for (int j = 0; j != longestSide; ++j) {
				result[runner++] = Color.WHITE;
			}
			// Add in the array.
			for (int j = 0; j != gWidth; ++j) {
				result[runner++] = colors[colorRunner++];
			}
		}
		return result;
	}

	private void drawOnCanvas(Canvas c, Paint paint, ArrayList<String[]> topHints, ArrayList<String> sideHints) {
		int longestSide = getLongest(sideHints);
		int longestTop = (topHints.size());
		drawGridlines(c, paint, longestTop, longestSide);
		drawHints(c, paint, topHints, sideHints, longestTop, longestSide);
	}

	private void drawHints(Canvas c, Paint paint, ArrayList<String[]> topHints, ArrayList<String> sideHints, int longestTop, int longestSide) {
		paint.setAntiAlias(true);
		paint.setColor(Color.GREEN);
		int sideIncrement = c.getHeight() / (longestTop + gHeight);
		int topIncrement = c.getWidth() / (longestSide + gWidth);
		paint.setTextSize(topIncrement / 2);
		// Draw top hints.
		for (int i = 0; i != longestTop; ++i) {
			for (int j = 0; j != topHints.get(i).length; ++j) {
				c.drawText(topHints.get(i)[j] + "", (longestSide * sideIncrement) + (sideIncrement / 2) + (j * sideIncrement), (longestTop * topIncrement) - (topIncrement * i), paint);
			}
		}

		// Draw side hints.
		paint.setTextAlign(Align.RIGHT);
		paint.setTextSize(sideIncrement / 2);
		for (int i = 0; i != sideHints.size(); ++i) {
			c.drawText(sideHints.get(i), longestSide * sideIncrement, (longestTop * topIncrement) + (i * topIncrement) + (2 * topIncrement / 3), paint);

		}
	}

	private void drawGridlines(Canvas c, Paint paint, int longestTop, int longestSide) {
		// Up down.
		int widthOffset = c.getWidth() / (gWidth + longestSide);
		// i=0, gWidth+1 if you want sides.
		for (int i = longestSide; i != (gWidth + longestSide); ++i) {
			c.drawLine(widthOffset * i, 0, widthOffset * i, c.getHeight(), paint);
		}
		// Side side.
		int heightOffset = c.getHeight() / (gHeight + longestTop);
		// i=0, gHeight+1 if you want sides.
		for (int i = longestTop; i != (gHeight + longestTop); ++i) {
			c.drawLine(0, heightOffset * i, c.getWidth(), heightOffset * i, paint);

		}
	}

	private char[][] solutionTo2DArray() {
		char[][] result = new char[gHeight][gWidth];
		int runner = 0;
		for (int i = 0; i != result.length; ++i) {
			for (int j = 0; j != result[i].length; ++j) {
				result[i][j] = gSolution.charAt(runner++);
			}
		}
		return result;
	}

	private ArrayList<String[]> getTopHints(ArrayList<String> columns) {
		ArrayList<String[]> result = new ArrayList<String[]>();
		ArrayList<String> parsed = getSideHints(columns);
		for (int i = 0; i != parsed.size(); ++i) {
			parsed.set(i, new StringBuilder(parsed.get(i)).reverse().toString());
		}
		int longest = getLongest(parsed);
		for (int i = 0; i != longest; ++i) {
			String temp = "";
			for (int j = 0; j != parsed.size(); ++j) {
				String split[] = parsed.get(j).split(" ");
				if (i >= split.length) {
					temp += " ,";
				} else {
					temp += split[i] + ",";
				}
			}
			// Using a , split for double digit numbers, things can get big ;).
			result.add(temp.split(","));
		}
		// Note: result needs to be flipped when actually printed, or printed
		// upside down.
		return result;
	}

	// regex:
	// http://stackoverflow.com/questions/15101577/split-string-when-character-changes-possible-regex-solution
	private ArrayList<String> getSideHints(ArrayList<String> rows) {
		ArrayList<String> result = new ArrayList<String>();
		for (String row : rows) {
			String temp = "";
			row.replaceFirst("^0+(?=[^0])", ""); // Remove leading 0's.
			String nums[] = row.split("0+|(?<=([1-9]))(?=[1-9])(?!\\1)");
			for (String item : nums) {
				temp += item + " ";
			}
			result.add(temp);
		}
		ArrayList<String> lengths = listToLengths(result);
		result.clear();

		return lengths;
	}

	private int getLongest(ArrayList<?> list) {
		int longest = 0;
		for (Object o : list) {
			String temp[] = o.toString().replaceAll(" +", " ").split(" ");
			if (temp.length > longest) {
				longest = temp.length;
			}
		}
		return longest;
	}

	private ArrayList<String> listToLengths(ArrayList<String> list) {
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i != list.size(); ++i) {
			String temp = "";
			String parse[] = list.get(i).split(" +");
			for (String p : parse) {
				if (p.length() != 0) {
					temp += p.length() + " ";
				}
			}
			if (temp.length() == 0) {
				result.add("0");
			} else {
				result.add(temp.substring(0, temp.length() - 1));
			}
		}
		return result;
	}

	private ArrayList<String> getRows(char[][] current2d) {
		ArrayList<String> result = new ArrayList<String>();

		for (int i = 0; i != current2d.length; ++i) {
			String temp = "";
			for (int j = 0; j != current2d[i].length; ++j) {
				temp += current2d[i][j];
			}
			result.add(temp);
		}
		return result;
	}

	private ArrayList<String> getColumns(char[][] current2d) {
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i != current2d[0].length; ++i) {
			String temp = "";
			for (int j = 0; j != current2d.length; ++j) {
				temp += current2d[j][i];
			}
			result.add(temp);
		}
		return result;
	}

	private int[] getPixelArrayFromString(String from, int length) {
		int[] colors = new int[length];
		for (int i = 0; i != colors.length; ++i) {
			if (from.charAt(i) == '0') {
				colors[i] = Color.WHITE;
			} else {
				colors[i] = Color.BLACK;
			}
		}
		return colors;
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