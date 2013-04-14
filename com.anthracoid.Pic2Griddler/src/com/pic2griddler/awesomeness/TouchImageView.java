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
import android.graphics.Paint.Cap;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

public class TouchImageView extends ImageView {

	Matrix matrix;

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

	int viewWidth, viewHeight;
	static final int CLICK = 3;

	protected static final String TAG = "TouchImageView";
	float saveScale = 1f;
	protected float origWidth, origHeight;
	int oldMeasuredWidth, oldMeasuredHeight;

	ScaleGestureDetector mScaleDetector;

	Context context;

	// Control whether we're moving around or in actual gameplay mode.
	boolean isGameplay = false;

	int lastTouchX = 0;
	int lastTouchY = 0;

	// Griddler specifics.
	String gCurrent, gSolution;
	int gWidth, gHeight, gId, lTop, lSide, cellWidth, cellHeight;

	public TouchImageView(Context context) {
		super(context);
		sharedConstructing(context);
	}

	public TouchImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		sharedConstructing(context);
	}

	private void sharedConstructing(Context context) {
		super.setClickable(true);
		this.context = context;
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		matrix = new Matrix();
		m = new float[9];
		setImageMatrix(matrix);
		setScaleType(ScaleType.MATRIX);

		setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				if (!isGameplay) {
					mScaleDetector.onTouchEvent(event);
					PointF curr = new PointF(event.getX(), event.getY());

					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						last.set(curr);
						start.set(last);
						mode = DRAG;
						break;

					case MotionEvent.ACTION_MOVE:
						if (mode == DRAG) {
							float deltaX = curr.x - last.x;
							float deltaY = curr.y - last.y;
							float fixTransX = getFixDragTrans(deltaX, viewWidth, origWidth * saveScale);
							float fixTransY = getFixDragTrans(deltaY, viewHeight, origHeight * saveScale);
							matrix.postTranslate(fixTransX, fixTransY);
							fixTrans();
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
				} else {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						// matrix.postScale(1, 1);
						matrix.getValues(m);
						float transX = m[Matrix.MTRANS_X] * -1;
						float transY = m[Matrix.MTRANS_Y] * -1;
						float scaleX = m[Matrix.MSCALE_X];
						float scaleY = m[Matrix.MSCALE_Y];
						lastTouchX = (int) ((event.getX() + transX) / scaleX);
						lastTouchY = (int) ((event.getY() + transY) / scaleY);
						lastTouchX = Math.abs(lastTouchX);
						lastTouchY = Math.abs(lastTouchY);

						int indexX = (int) Math.floor((lastTouchX - (cellWidth * lSide)) / cellWidth);
						int indexY = (int) Math.floor((lastTouchY - (cellHeight * lTop)) / cellHeight);
						char[] temp = gCurrent.toCharArray();
						if (temp[indexY * gWidth + indexX] == '0') {
							temp[indexY * gWidth + indexX] = '1';
						} else {
							temp[indexY * gWidth + indexX] = '0';
						}
						gCurrent = String.valueOf(temp);
						bitmapFromCurrent();
						if (gCurrent.equals(gSolution)) {
							Log.d(TAG, "WIN!");
							if(winListener != null)
							{
							winListener.win();
							}
							else
							{
								try {
									throw new Exception("No WinListener!");
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}

				}
				return true; // indicate event was handled
			}

		});
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
			float mScaleFactor = detector.getScaleFactor();
			float origScale = saveScale;
			saveScale *= mScaleFactor;
			if (saveScale > maxScale) {
				saveScale = maxScale;
				mScaleFactor = maxScale / origScale;
			} else if (saveScale < minScale) {
				saveScale = minScale;
				mScaleFactor = minScale / origScale;
			}

			if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight) {
				matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2, viewHeight / 2);
			} else {
				matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
			}
			fixTrans();
			return true;
		}
	}

	void fixTrans() {
		matrix.getValues(m);
		float transX = m[Matrix.MTRANS_X];
		float transY = m[Matrix.MTRANS_Y];

		float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);
		float fixTransY = getFixTrans(transY, viewHeight, origHeight * saveScale);

		if (fixTransX != 0 || fixTransY != 0)
			matrix.postTranslate(fixTransX, fixTransY);
	}

	float getFixTrans(float trans, float viewSize, float contentSize) {
		float minTrans, maxTrans;

		if (contentSize <= viewSize) {
			minTrans = 0;
			maxTrans = viewSize - contentSize;
		} else {
			minTrans = viewSize - contentSize;
			maxTrans = 0;
		}

		if (trans < minTrans)
			return -trans + minTrans;
		if (trans > maxTrans)
			return -trans + maxTrans;
		return 0;
	}

	float getFixDragTrans(float delta, float viewSize, float contentSize) {
		if (contentSize <= viewSize) {
			return 0;
		}
		return delta;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		viewWidth = MeasureSpec.getSize(widthMeasureSpec);
		viewHeight = MeasureSpec.getSize(heightMeasureSpec);

		//
		// Rescales image on rotation
		//
		if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight || viewWidth == 0 || viewHeight == 0)
			return;
		oldMeasuredHeight = viewHeight;
		oldMeasuredWidth = viewWidth;

		if (saveScale == 1) {
			// Fit to screen.
			float scale;

			Drawable drawable = getDrawable();
			if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)
				return;
			int bmWidth = drawable.getIntrinsicWidth();
			int bmHeight = drawable.getIntrinsicHeight();

			Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);

			float scaleX = (float) viewWidth / (float) bmWidth;
			float scaleY = (float) viewHeight / (float) bmHeight;
			scale = Math.min(scaleX, scaleY);
			matrix.setScale(scale, scale);

			// Center the image
			float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);
			float redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);
			redundantYSpace /= (float) 2;
			redundantXSpace /= (float) 2;

			matrix.postTranslate(redundantXSpace, redundantYSpace);

			origWidth = viewWidth - 2 * redundantXSpace;
			origHeight = viewHeight - 2 * redundantYSpace;
			setImageMatrix(matrix);
		}
		fixTrans();
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
		int colors[] = getPixelArrayFromString(gCurrent, gCurrent.length());

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
		lTop = longestTop;
		lSide = longestSide;
		// Create bitmap with padding for the numbers.
		colors = resizeBitMapsForHints(colors, longestTop, longestSide);
		// Bitmap bm = Bitmap.createScaledBitmap(Bitmap.createBitmap(colors,
		// gWidth + longestSide, gHeight + longestTop, Bitmap.Config.ARGB_4444),
		// gWidth * 100, gHeight * 100, false);
		Bitmap bm = Bitmap.createBitmap((gWidth + longestSide) * 50, (gHeight + longestTop) * 50, Bitmap.Config.ARGB_4444);
		Canvas c = new Canvas(bm);
		Paint p = new Paint();
		// Change canvas and it'll reflect on the bm.
		drawOnCanvas(c, p, colors, topHints, sideHints);
		setImageBitmap(bm); // bm should contain all info we changed. Don't
							// worry.
	}

	// Just add on fluff area for the hints on the top and on the side.
	private int[] resizeBitMapsForHints(int[] colors, int longestTop, int longestSide) {
		int result[] = new int[(longestTop * (longestSide + gWidth)) + colors.length + (gHeight * longestSide)];
		int runner;
		// Fill up the top with blank white.
		for (runner = 0; runner != (longestTop * (longestSide + gWidth)); ++runner) {
			result[runner] = Color.WHITE;
		}
		// Fill side hints with white, and the image with what was in it
		// previously.
		int colorRunner = 0; // Used to run through original colors.
		for (int i = 0; i != gHeight; ++i) {
			// Draw side for hints.
			for (int j = 0; j != longestSide; ++j) {
				result[runner++] = Color.WHITE;
			}
			// Add in the array/picture.
			for (int j = 0; j != gWidth; ++j) {
				result[runner++] = colors[colorRunner++];
			}
		}
		return result;
	}

	private void drawOnCanvas(Canvas c, Paint paint, int[] colors, ArrayList<String[]> topHints, ArrayList<String> sideHints) {
		int longestSide = getLongest(sideHints);
		int longestTop = (topHints.size());
		// White out whole canvas.
		drawWhiteCanvas(c, paint);
		// Draw game surface.
		drawGame(c, paint, colors, longestTop, longestSide);
		// Draw gridlines and hints
		drawGridlines(c, paint, longestTop, longestSide);
		drawHints(c, paint, topHints, sideHints, longestTop, longestSide);
		paint.setColor(Color.RED);
		c.drawCircle(lastTouchX, lastTouchY, 5, paint);
	}

	private void drawGame(Canvas c, Paint paint, int[] colors, int longestTop, int longestSide) {
		int heightTrim = c.getHeight() % (gHeight + longestTop);
		int widthTrim = c.getWidth() % (gWidth + longestSide);
		// Up down.
		paint.setColor(Color.RED);
		int widthOffset = (c.getWidth() - widthTrim) / (longestSide + gWidth);
		int heightOffset = (c.getHeight() - heightTrim) / (gHeight + longestTop);
		cellWidth = widthOffset;
		cellHeight = heightOffset;
		int row = -1, column = 0;
		for (int i = 0; i != gCurrent.length(); ++i) {
			paint.setColor(Color.rgb(i * 10, i * 10, i * 10));
			if (i % (gWidth) == 0) {
				column = 0;
				++row;
			}
			Rect r = new Rect(widthOffset * (longestSide + column), heightOffset * (longestTop + row), widthOffset * (longestSide + column + 1), heightOffset * (longestTop + row + 1));

			if (gCurrent.charAt(i) == '0') {
				paint.setColor(Color.WHITE);
			} else {
				paint.setColor(Color.BLACK);
			}
			c.drawRect(r, paint);
			++column;
		}
	}

	private void drawWhiteCanvas(Canvas c, Paint paint) {
		paint.setColor(Color.rgb(255, 251, 237));
		c.drawRect(0, 0, c.getWidth(), c.getHeight(), paint);
	}

	private void drawHints(Canvas c, Paint paint, ArrayList<String[]> topHints, ArrayList<String> sideHints, int longestTop, int longestSide) {
		paint.setAntiAlias(true);
		paint.setColor(Color.rgb(61, 54, 26));
		paint.setStrokeWidth(1);
		int widthOffset = c.getWidth() / (longestSide + gWidth);
		int heightOffset = c.getHeight() / (gHeight + longestTop);
		paint.setTextSize(heightOffset / 2);
		// Draw top hints.
		for (int i = 0; i != longestTop; ++i) {
			for (int j = 0; j != topHints.get(i).length; ++j) {
				c.drawText(topHints.get(i)[j] + "", (longestSide * widthOffset) + (widthOffset / 2) + (j * widthOffset), (longestTop * heightOffset) - (heightOffset * i), paint);
			}
		}

		// Draw side hints.
		paint.setTextAlign(Align.RIGHT);
		paint.setTextSize(widthOffset / 2);
		for (int i = 0; i != sideHints.size(); ++i) {
			// The 2 * heightOffset/3 is for balance issues.
			c.drawText(sideHints.get(i), longestSide * widthOffset, (longestTop * heightOffset) + (i * heightOffset) + (2 * heightOffset / 3), paint);

		}
	}

	private void drawGridlines(Canvas c, Paint paint, int longestTop, int longestSide) {
		paint.setStrokeWidth(3);
		int heightTrim = c.getHeight() % (gHeight + longestTop);
		int widthTrim = c.getWidth() % (gWidth + longestSide);
		// Up down.
		paint.setColor(Color.rgb(117, 111, 88));
		int widthOffset = (c.getWidth() - widthTrim) / (longestSide + gWidth);
		int heightOffset = (c.getHeight() - heightTrim) / (gHeight + longestTop);

		for (int i = longestSide; i != (gWidth + longestSide) + 1; ++i) {
			c.drawLine(widthOffset * i, 0, widthOffset * i, c.getHeight(), paint);
		}
		// Side side.
		for (int i = longestTop; i != (gHeight + longestTop) + 1; ++i) {
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

	/*
	 * Interface to see if we win.
	 */private WinnerListener winListener;

	public interface WinnerListener {
		public void win();
	}

	public void setWinListener(WinnerListener winListener) {
		this.winListener = winListener;
	}
}