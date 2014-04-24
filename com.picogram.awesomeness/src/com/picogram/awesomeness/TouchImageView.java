
package com.picogram.awesomeness;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

public class TouchImageView extends ImageView implements OnGestureListener,
		OnDoubleTapListener {

	public interface HistoryListener {
		public void action(String curr);
	}

	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(final ScaleGestureDetector detector) {
			float mScaleFactor = detector.getScaleFactor();
			final float origScale = TouchImageView.this.saveScale;
			TouchImageView.this.saveScale *= mScaleFactor;
			if (TouchImageView.this.saveScale > TouchImageView.this.maxScale) {
				TouchImageView.this.saveScale = TouchImageView.this.maxScale;
				mScaleFactor = TouchImageView.this.maxScale / origScale;
			} else if (TouchImageView.this.saveScale < TouchImageView.this.minScale) {
				TouchImageView.this.saveScale = TouchImageView.this.minScale;
				mScaleFactor = TouchImageView.this.minScale / origScale;
			}

			if (((TouchImageView.this.origWidth * TouchImageView.this.saveScale) <= TouchImageView.this.viewWidth)
					|| ((TouchImageView.this.origHeight * TouchImageView.this.saveScale) <= TouchImageView.this.viewHeight)) {
				TouchImageView.this.matrix.postScale(mScaleFactor, mScaleFactor,
						TouchImageView.this.viewWidth / 2,
						TouchImageView.this.viewHeight / 2);
			} else {
				TouchImageView.this.matrix.postScale(mScaleFactor, mScaleFactor,
						detector.getFocusX(), detector.getFocusY());
			}
			TouchImageView.this.fixTrans();
			return true;
		}

		@Override
		public boolean onScaleBegin(final ScaleGestureDetector detector) {
			TouchImageView.this.mode = ZOOM;
			return true;
		}
	}

	public interface WinnerListener {
		public void win();
	}

	Matrix matrix;
	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;
	// Remember some things for zooming
	PointF last = new PointF();
	PointF start = new PointF();
	float minScale = 0.9f;

	float maxScale = 3f;
	float[] m;

	int viewWidth, viewHeight;
	static final int CLICK = 3;
	float saveScale = 1f;

	protected float origWidth, origHeight;

	int oldMeasuredWidth, oldMeasuredHeight;

	ScaleGestureDetector mScaleDetector;

	Context context;
	// Control whether we're moving around or in actual gameplay mode.
	boolean isGameplay = false;
	Handler h = new Handler();
	int lastTouchX = 0;
	int lastTouchY = 0;
	char colorCharacter = '0';
	protected static final String TAG = "TouchImageView";

	// These take a long time to calculate and don't change. Only do it once.
	ArrayList<String[]> topHints;
	ArrayList<String> sideHints;
	ArrayList<String> columns;
	ArrayList<String> rows;
	int longestSide, longestTop;
	Bitmap bm;
	Canvas canvasBitmap;
	Paint paintBitmap;
	int gridlinesColor;
	ArrayList<String> history = new ArrayList<String>();
	// Picogram specifics.
	String gCurrent, gSolution, gName, gId;

	int gWidth, gHeight, lTop, lSide, cellWidth, cellHeight;

	int[] gColors;

	/*
	 * Interfaces and such to see if we win and history stuff.
	 */
	private WinnerListener winListener;
	private HistoryListener historyListener;

	OnTouchListener touchListener = new OnTouchListener() {

		public boolean onTouch(final View v, final MotionEvent event) {
			TouchImageView.this.mDetector.onTouchEvent(event);

			// Normal touch instance between gameplay and non.
			if (!TouchImageView.this.isGameplay) {
				TouchImageView.this.mScaleDetector.onTouchEvent(event);
				final PointF curr = new PointF(event.getX(), event.getY());

				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						TouchImageView.this.last.set(curr);
						TouchImageView.this.start.set(TouchImageView.this.last);
						TouchImageView.this.mode = DRAG;
						break;

					case MotionEvent.ACTION_MOVE:
						if (TouchImageView.this.mode == DRAG) {
							final float deltaX = curr.x - TouchImageView.this.last.x;
							final float deltaY = curr.y - TouchImageView.this.last.y;
							final float fixTransX = TouchImageView.this
									.getFixDragTrans(deltaX, TouchImageView.this.viewWidth,
											TouchImageView.this.origWidth
													* TouchImageView.this.saveScale);
							final float fixTransY = TouchImageView.this
									.getFixDragTrans(deltaY, TouchImageView.this.viewHeight,
											TouchImageView.this.origHeight
													* TouchImageView.this.saveScale);
							TouchImageView.this.matrix.postTranslate(fixTransX, fixTransY);
							TouchImageView.this.fixTrans();
							TouchImageView.this.last.set(curr.x, curr.y);
						}
						break;

					case MotionEvent.ACTION_UP:
						TouchImageView.this.mode = NONE;
						final int xDiff = (int) Math.abs(curr.x - TouchImageView.this.start.x);
						final int yDiff = (int) Math.abs(curr.y - TouchImageView.this.start.y);
						if ((xDiff < CLICK) && (yDiff < CLICK)) {
							TouchImageView.this.performClick();
						}
						break;

					case MotionEvent.ACTION_POINTER_UP:
						TouchImageView.this.mode = NONE;
						break;
				}

				TouchImageView.this.setImageMatrix(TouchImageView.this.matrix);
				TouchImageView.this.invalidate();
				return true;
			}
			return false;
		}

	};

	int previousX = -1, previousY = -1;
	boolean didPreviousSwitcher = false;
	boolean didSwitch = false; // If we're switching a color to an X so it
	// doesn't switch back on the MOVE/UP
	ArrayList<Integer> topColors = new ArrayList<Integer>();
	ArrayList<Integer> sideColors = new ArrayList<Integer>();
	String oldCurrent = "";

	public boolean isRefreshing = false;

	boolean isFirstTime = true;

	GestureDetectorCompat mDetector;

	boolean onScrollingDraw = false;

	public TouchImageView(final Context context) {
		super(context);
		this.sharedConstructing(context);
	}

	public TouchImageView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.sharedConstructing(context);
	}

	// Convert current String to a bitmap that's drawable. This will draw
	// everything: grid, numbers, and onclicks.
	public void bitmapFromCurrent() {

		// Get a 2D array of "current" Picogram.
		final char current2D[][] = this.puzzleTo2DArray(this.gSolution);
		// Create bitmap based on the current. Make an int array with pixel
		// colors.
		// Because of how we're making the top hints, it needs its own method.
		if ((this.topHints == null) || this.isRefreshing) {

			this.rows = this.getRows(current2D);
			this.columns = this.getColumns(current2D);
			this.sideHints = this.getSideHints(this.rows);

			this.topHints = this.getTopHints(this.columns);
			this.longestTop = this.topHints.size();
			this.longestSide = this.getLongest(this.sideHints); // Get widest
			// "layer"
			this.topColors = this.getColors(this.columns, true);
			this.sideColors = this.getColors(this.rows, false);
			// Since this is layered, we just need number of layers.
			this.lTop = this.longestTop;
			this.lSide = this.longestSide;
			// this.bm = Bitmap.createBitmap((this.gWidth + this.longestSide) *
			// 50,(this.gHeight + this.longestTop) * 50, Bitmap.Config.RGB_565);
			int changer = 0;
			if ((this.gWidth + this.lSide) > (this.gHeight + this.lTop))
			{
				changer = 1000 / (this.gWidth + this.lSide);
			} else {
				changer = 1000 / (this.gHeight + this.lTop);
			}

			this.bm = Bitmap.createBitmap(
					(this.gWidth + this.longestSide) * changer,
					(this.gHeight + this.longestTop) * changer,
					Bitmap.Config.ARGB_4444);

			this.canvasBitmap = new Canvas(this.bm);
			this.paintBitmap = new Paint();
			// Reverse the side hints, just because. Sorry, this is really
			// stupid, it's for colors somehow.

			for (int i = 0; i != this.sideHints.size(); ++i) {
				this.sideHints.set(i, new StringBuilder(this.sideHints.get(i))
						.reverse().toString());
			}

		}
		// Clear
		// canvasBitmap.drawColor(Color.rgb((int) (Math.random() * 200),
		// (int) (Math.random() * 200), (int) (Math.random() * 200)));

		this.drawOnCanvas();

		// Change canvas and it'll reflect on the bm.
		this.setImageBitmap(this.bm);

	}

	public void checkWin() {
		if (this.gCurrent.replaceAll("x|X", "0").equals(this.gSolution)) {
			if (this.winListener != null) {
				this.winListener.win();
			} else {
				try {
					throw new Exception("No WinListener!");
				} catch (final Exception e) {
					// Should never get here.
					e.printStackTrace();
				}
			}
		}
	}

	public void clearGame() {
		this.gCurrent.replace("1", "0");
		this.bitmapFromCurrent();
	}

	/**
	 * http://stackoverflow.com/questions/12166476/android-canvas-drawtext-set-
	 * font-size-from-width Retrieve the maximum text size to fit in a given
	 * width.
	 * 
	 * @param str
	 *            (String): Text to check for size.
	 * @param maxWidth
	 *            (float): Maximum allowed width.
	 * @return (int): The desired text size.
	 */
	private int determineMaxTextSize(final String str, final float maxWidth) {
		int size = 0;
		final Paint paint = new Paint();
		if (str.isEmpty()) {
			return 1;
		}

		do {

			paint.setTextSize(++size);
		} while ((paint.measureText(str) < maxWidth));

		return size;
	}

	private void drawCornerInfo() {

		// Draw the name and the size in the upper left corner of game board.
		final String size = this.gWidth + " X " + this.gHeight;

		this.paintBitmap.setColor(this.gridlinesColor);
		this.paintBitmap
				.setTextSize(this.determineMaxTextSize(this.gName, this.lSide * this.cellWidth));

		this.canvasBitmap.drawText(this.gName, 0, this.paintBitmap.getTextSize(),
				this.paintBitmap);

		final float oldSize = this.paintBitmap.getTextSize();
		this.paintBitmap
				.setTextSize(this.determineMaxTextSize(size, this.lSide * this.cellWidth));

		this.canvasBitmap.drawText(size, 0,
				this.paintBitmap.getTextSize() + oldSize, this.paintBitmap);

	}

	private void drawGame() {
		final int heightTrim = this.canvasBitmap.getHeight()
				% (this.gHeight + this.longestTop);
		final int widthTrim = this.canvasBitmap.getWidth()
				% (this.gWidth + this.longestSide);
		this.paintBitmap.setColor(Color.RED);
		final int widthOffset = (this.canvasBitmap.getWidth() - widthTrim)
				/ (this.longestSide + this.gWidth);
		final int heightOffset = (this.canvasBitmap.getHeight() - heightTrim)
				/ (this.gHeight + this.longestTop);
		this.cellWidth = widthOffset;
		this.cellHeight = heightOffset;
		int row = -1, column = 0;
		if (this.gCurrent == null) {
			// User hasn't played yet, make it a new game.
			this.gCurrent = "";
			for (int i = 0; i != this.gSolution.length(); ++i) {
				this.gCurrent += "0";
			}
		}
		for (int i = 0; i != this.gCurrent.length(); ++i) {
			if ((i % (this.gWidth)) == 0) {
				column = 0;
				++row;
			}
			final Rect r = new Rect(widthOffset * (this.longestSide + column),
					heightOffset * (this.longestTop + row), widthOffset
							* (this.longestSide + column + 1), heightOffset
							* (this.longestTop + row + 1));

			final Xfermode old = this.paintBitmap.getXfermode();
			if (this.gCurrent.charAt(i) == 'x') {
				// We have an x.
				this.paintBitmap.setColor(Color.TRANSPARENT);
				this.paintBitmap.setTextSize(this.cellHeight);
				this.paintBitmap.setXfermode(new PorterDuffXfermode(
						android.graphics.PorterDuff.Mode.SRC));
				this.canvasBitmap.drawRect(r, this.paintBitmap);// White out
				// this spot
				this.paintBitmap.setXfermode(old);
				this.paintBitmap.setColor(Color.BLACK);
				final Paint.Align first = this.paintBitmap.getTextAlign();
				this.paintBitmap.setTextAlign(Paint.Align.CENTER);
				this.canvasBitmap.drawText("X", r.left + (this.cellWidth / 2), r.top
						+ ((this.cellHeight * 9) / 10), this.paintBitmap);
				this.paintBitmap.setTextAlign(first);
			} else {
				this.paintBitmap.setColor(this.gColors[Integer
						.parseInt(this.gCurrent.charAt(i) + "")]);
				if (this.gColors[Integer.parseInt(this.gCurrent.charAt(i) + "")] == Color.TRANSPARENT) {
					// Draw white ontop for transparency.
					this.paintBitmap.setXfermode(new PorterDuffXfermode(
							android.graphics.PorterDuff.Mode.SRC));
				}
				// Dim the color to see the gridlines.
				this.canvasBitmap.drawRect(r, this.paintBitmap);
				this.paintBitmap.setXfermode(old);
			}
			++column;
		}
	}

	private void drawGridlines() {
		this.paintBitmap.setStrokeWidth(3);
		final int heightTrim = this.canvasBitmap.getHeight()
				% (this.gHeight + this.longestTop);
		final int widthTrim = this.canvasBitmap.getWidth()
				% (this.gWidth + this.longestSide);
		// Up down.
		this.paintBitmap.setColor(this.gridlinesColor);
		final int widthOffset = (this.canvasBitmap.getWidth() - widthTrim)
				/ (this.longestSide + this.gWidth);
		final int heightOffset = (this.canvasBitmap.getHeight() - heightTrim)
				/ (this.gHeight + this.longestTop);
		int runner = 0;
		for (int i = this.longestSide; i != ((this.gWidth + this.longestSide) + 1); ++i) {
			if ((runner % 5) == 0) {
				this.paintBitmap.setStrokeWidth(this.paintBitmap
						.getStrokeWidth() + 4);
			}
			this.canvasBitmap.drawLine(widthOffset * i, 0, widthOffset * i,
					this.canvasBitmap.getHeight(), this.paintBitmap);
			if ((runner % 5) == 0) {
				this.paintBitmap.setStrokeWidth(this.paintBitmap
						.getStrokeWidth() - 4);
			}
			runner++;
		}
		// Side side.
		runner = 0;
		for (int i = this.longestTop; i != ((this.gHeight + this.longestTop) + 1); ++i) {
			if ((runner % 5) == 0) {
				this.paintBitmap.setStrokeWidth(this.paintBitmap
						.getStrokeWidth() + 4);
			}
			this.canvasBitmap.drawLine(0, heightOffset * i,
					this.canvasBitmap.getWidth(), heightOffset * i,
					this.paintBitmap);
			if ((runner % 5) == 0) {
				this.paintBitmap.setStrokeWidth(this.paintBitmap
						.getStrokeWidth() - 4);
			}
			runner++;
		}
	}

	private void drawHints() {
		this.paintBitmap.setAntiAlias(true);
		this.paintBitmap.setColor(this.getResources().getColor(
				R.color.foreground));
		this.paintBitmap.setStrokeWidth(1);
		final int widthOffset = this.canvasBitmap.getWidth()
				/ (this.longestSide + this.gWidth);
		final int heightOffset = this.canvasBitmap.getHeight()
				/ (this.gHeight + this.longestTop);
		this.paintBitmap.setTextSize(heightOffset / 2);
		this.paintBitmap.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		// Draw top hints.
		int colorRun = 0;
		for (int i = 0; i != this.longestTop; ++i) {
			for (int j = 0; j != this.topHints.get(i).length; ++j) {
				if (Character.isDigit(this.topHints.get(i)[j].charAt(0))) {
					this.paintBitmap.setColor(this.gColors[this.topColors
							.get(colorRun)]);
					colorRun++;
				}
				if (this.topHints.get(i)[j].equals("0")) {
					this.paintBitmap.setColor(Color.TRANSPARENT);
				}

				this.canvasBitmap
						.drawRect(
								new Rect(
										((this.longestSide * widthOffset) + (j * widthOffset)),
										((this.longestTop * heightOffset)
												- (heightOffset * i) - heightOffset),
										((this.longestSide * widthOffset) + (j * widthOffset))
												+ (widthOffset),
										((this.longestTop * heightOffset) - (heightOffset * i))),
								this.paintBitmap);
				final int[] rgbOriginal = this.getRGB(this.paintBitmap.getColor());
				if (this.paintBitmap.getColor() == Color.TRANSPARENT) {
					this.paintBitmap.setColor(this.gridlinesColor);
				} else {
					this.paintBitmap.setColor(Color.rgb(255 - rgbOriginal[0],
							255 - rgbOriginal[1], 255 - rgbOriginal[2]));
				}
				this.canvasBitmap
						.drawText(
								this.topHints.get(i)[j],
								((this.longestSide * widthOffset)
										+ (widthOffset / 2) + (j * widthOffset)) - 5,
								(this.longestTop * heightOffset)
										- (heightOffset * i) - 5,
								this.paintBitmap);
				this.paintBitmap.setColor(Color.TRANSPARENT);
			}
		}
		// Draw side hints.
		colorRun = 0;
		final Align oldAlign = this.paintBitmap.getTextAlign();
		this.paintBitmap.setTextAlign(Align.RIGHT);
		this.paintBitmap.setTextSize(widthOffset / 2);
		for (int i = 0; i != this.sideHints.size(); ++i) {
			// The 2 * heightOffset/3 is for balance issues.
			for (int j = 0; j != this.sideHints.get(i).split(" ").length; ++j) {
				this.paintBitmap.setColor(this.gColors[this.sideColors
						.get(colorRun)]);

				colorRun++;

				String side = this.sideHints.get(i).split(" ")[j];
				if (side.length() > 1) {
					side = new StringBuilder(side).reverse().toString();
				}
				if (side.equals("0")) {
					this.paintBitmap.setColor(Color.TRANSPARENT);
				}

				this.canvasBitmap.drawRect(new Rect((this.longestSide * widthOffset)
						- (j * widthOffset), (this.longestTop * heightOffset)
						+ (i * heightOffset), (this.longestSide * widthOffset)
						- (j * widthOffset) - (widthOffset),
						(this.longestTop * heightOffset) + (i * heightOffset)
								+ heightOffset), this.paintBitmap);

				final int[] rgbOriginal = this.getRGB(this.paintBitmap.getColor());
				if (this.paintBitmap.getColor() == Color.TRANSPARENT) {
					this.paintBitmap.setColor(this.gridlinesColor);
				} else {
					this.paintBitmap.setColor(Color.rgb(255 - rgbOriginal[0],
							255 - rgbOriginal[1], 255 - rgbOriginal[2]));
				}
				this.canvasBitmap.drawText(side + "  ",
						(this.longestSide * widthOffset) - 5
								- (j * widthOffset),
						(this.longestTop * heightOffset) + (i * heightOffset)
								+ ((2 * heightOffset) / 3), this.paintBitmap);
			}
		}
		this.paintBitmap.setTextAlign(oldAlign);
	}

	private void drawOnCanvas() {
		// Draw game surface.
		this.drawGame();

		// Draw gridlines and hints
		if (this.isFirstTime || this.isRefreshing) {
			this.drawHints();
			this.isFirstTime = false;
		}

		this.drawGridlines();
		this.drawSolvedPortions();

		this.drawCornerInfo();
		this.paintBitmap.setColor(Color.RED);

		this.canvasBitmap.drawCircle(this.lastTouchX, this.lastTouchY, 5,
				this.paintBitmap);

	}

	private void drawSolvedPortions() {
		final char[][] solution2D = this.puzzleTo2DArray(this.gSolution);
		final ArrayList<String> solutionRows = this.getRows(solution2D);
		final ArrayList<String> solutionColumns = this.getColumns(solution2D);
		int numSolves = 0;
		final char[][] current2D = this.puzzleTo2DArray(this.gCurrent);
		final ArrayList<String> currentRows = this.getRows(current2D);
		final ArrayList<String> currentColumns = this.getColumns(current2D);
		final Rect r = new Rect(0, 0, 0, 0);
		for (int i = 0; i != solutionRows.size(); ++i) {
			final String sr = solutionRows.get(i).replaceAll("X", "0")
					.replaceAll("0+", "0");
			final String cr = currentRows.get(i).replaceAll("[X|x]", "0")
					.replaceAll("0+", "0");
			if (sr.equals(cr)) {
				this.paintBitmap.setColor(Color.GREEN);
				r.set((this.lSide * this.cellWidth) - 1, (this.cellHeight * this.lTop)
						+ (i * this.cellHeight), (this.lSide * this.cellWidth) + 1,
						(this.cellHeight * this.lTop) + (i * this.cellHeight) + this.cellHeight);
				this.canvasBitmap.drawRect(r, this.paintBitmap);
			} else if (sr.replaceAll("^0*|0*$", "").equals(cr.replaceAll("^0*|0*$", "")))
			{
				numSolves++;
			}
		}

		for (int i = 0; i != solutionColumns.size(); ++i) {
			final String sr = solutionColumns.get(i).replaceAll("X", "0")
					.replaceAll("0+", "0");
			final String cr = currentColumns.get(i).replaceAll("[X|x]", "0")
					.replaceAll("0+", "0");
			if (sr.equals(cr)) {
				this.paintBitmap.setColor(Color.GREEN);
				r.set((i * this.cellWidth) + (this.lSide * this.cellWidth),
						(this.lTop * this.cellHeight)
						- 1, (i * this.cellWidth) + (this.lSide * this.cellWidth) + this.cellWidth,
						(this.lTop * this.cellHeight) + 1);
				this.canvasBitmap.drawRect(r, this.paintBitmap);
			} else if (sr.replaceAll("^0*|0*$", "").equals(cr.replaceAll("^0*|0*$", "")))
			{
				numSolves++;
			}
		}
		if (numSolves == gWidth * gHeight)
		{
			//TODO: Handle a false win, update online data and inform the player.
			Log.d(TAG, "FALSE WIN!!!!");
		}
	}

	void fixTrans() {
		this.matrix.getValues(this.m);
		final float transX = this.m[Matrix.MTRANS_X];
		final float transY = this.m[Matrix.MTRANS_Y];

		final float fixTransX = this.getFixTrans(transX, this.viewWidth,
				this.origWidth * this.saveScale);
		final float fixTransY = this.getFixTrans(transY, this.viewHeight,
				this.origHeight * this.saveScale);

		if ((fixTransX != 0) || (fixTransY != 0)) {
			this.matrix.postTranslate(fixTransX, fixTransY);
		}
	}

	private ArrayList<Integer> getColors(final ArrayList<String> segments,
			final boolean isTop) {
		final ArrayList<Integer> result = new ArrayList<Integer>();
		final ArrayList<char[]> chars = new ArrayList<char[]>();
		for (final String segment : segments) {
			if (segment.matches("[0]+")) {
				chars.add(new char[] {
						'0'
				});
				continue;
			}
			final String middle = this.removeDuplicates(segment);
			final String last = middle.replaceAll("0", "");
			final char[] whole = new StringBuilder(last).reverse().toString()
					.toCharArray();
			chars.add(whole);
		}
		int longest;
		longest = (isTop) ? this.longestTop : this.longestSide;
		if (isTop) {
			for (int i = 0; i != longest; ++i) {
				for (final char[] c : chars) {
					if (i < c.length) {
						if (c[i] != '0') {
							result.add(Integer.parseInt(c[i] + ""));
						} else {
							result.add(1);
						}
					}
				}
			}
		} else {
			for (final char[] c : chars) {
				// c = new StringBuilder(new
				// String(c)).reverse().toString().toCharArray();
				for (int i = 0; i != c.length; ++i) {
					if (c[i] != '0') {
						result.add(Integer.parseInt(c[i] + ""));
					} else {
						result.add(1);
					}
				}
			}
		}
		return result;
	}

	private ArrayList<String> getColumns(final char[][] current2d) {
		final ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i != current2d[0].length; ++i) {
			String temp = "";
			for (int j = 0; j != current2d.length; ++j) {
				temp += current2d[j][i];
			}
			result.add(temp);
		}
		return result;
	}

	float getFixDragTrans(final float delta, final float viewSize,
			final float contentSize) {
		if (contentSize <= viewSize) {
			return 0;
		}
		return delta;
	}

	float getFixTrans(final float trans, final float viewSize,
			final float contentSize) {
		float minTrans, maxTrans;

		if (contentSize <= viewSize) {
			minTrans = 0;
			maxTrans = viewSize - contentSize;
		} else {
			minTrans = viewSize - contentSize;
			maxTrans = 0;
		}

		if (trans < minTrans) {
			return -trans + minTrans;
		}
		if (trans > maxTrans) {
			return -trans + maxTrans;
		}
		return 0;
	}

	private int getLongest(final ArrayList<?> list) {
		int longest = 0;
		for (final Object o : list) {
			final String temp[] = o.toString().replaceAll(" +", " ").split(" ");
			if (temp.length > longest) {
				longest = temp.length;
			}
		}
		return longest;
	}

	private int[] getRGB(final int i) {

		final int r = (i >> 16) & 0xff;
		final int g = (i >> 8) & 0xff;
		final int b = (i & 0xff);
		return new int[] {
				r, g, b
		};
	}

	private ArrayList<String> getRows(final char[][] current2d) {
		final ArrayList<String> result = new ArrayList<String>();

		for (int i = 0; i != current2d.length; ++i) {
			String temp = "";
			for (int j = 0; j != current2d[i].length; ++j) {
				temp += current2d[i][j];
			}
			result.add(temp);
		}
		return result;
	}

	// regex:
	// http://stackoverflow.com/questions/15101577/split-string-when-character-changes-possible-regex-solution
	private ArrayList<String> getSideHints(final ArrayList<String> rows) {
		final ArrayList<String> result = new ArrayList<String>();
		for (final String row : rows) {
			String temp = "";
			row.replaceFirst("^0+(?=[^0])", ""); // Remove leading 0's.
			final String nums[] = row.split("0+|(?<=([1-9]))(?=[1-9])(?!\\1)");
			for (final String item : nums) {
				temp += item + " ";
			}
			result.add(temp);
		}
		final ArrayList<String> lengths = this.listToLengths(result);
		result.clear();

		return lengths;
	}

	private ArrayList<String[]> getTopHints(final ArrayList<String> columns) {
		final ArrayList<String[]> result = new ArrayList<String[]>();
		final ArrayList<String> parsed = this.getSideHints(columns);
		for (int i = 0; i != parsed.size(); ++i) {
			String temp = parsed.get(i);
			final String[] split = temp.split(" ");
			for (int j = 0; j != split.length; ++j) {
				split[j] = new StringBuilder(split[j]).reverse().toString();
			}
			String emp = "";
			for (final String s : split) {
				emp += s + " ";
			}
			temp = emp.substring(0, emp.length() - 1); // Minus 1 to get rid of
			// space.
			temp = new StringBuilder(temp).reverse().toString();
			parsed.set(i, temp);
		}
		final int longest = this.getLongest(parsed);
		for (int i = 0; i != longest; ++i) {
			String temp = "";
			for (int j = 0; j != parsed.size(); ++j) {
				final String split[] = parsed.get(j).split(" ");
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

	private ArrayList<String> listToLengths(final ArrayList<String> list) {
		final ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i != list.size(); ++i) {
			String temp = "";
			final String parse[] = list.get(i).split(" +");
			for (final String p : parse) {
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

	public boolean onDoubleTap(final MotionEvent event) {
		// Make sure we're outside of the game.
		this.matrix.getValues(this.m);
		final float transX = this.m[Matrix.MTRANS_X] * -1;
		final float transY = this.m[Matrix.MTRANS_Y] * -1;
		final float scaleX = this.m[Matrix.MSCALE_X];
		final float scaleY = this.m[Matrix.MSCALE_Y];
		this.lastTouchX = (int) ((event.getX() + transX) / scaleX);
		this.lastTouchY = (int) ((event.getY() + transY) / scaleY);
		this.lastTouchX = Math.abs(this.lastTouchX);
		this.lastTouchY = Math.abs(this.lastTouchY);
		if (((this.lastTouchX) < (this.canvasBitmap.getWidth() - (this.gWidth * this.cellWidth))) ||
				((this.lastTouchY) < (this.canvasBitmap.getHeight() - (this.gHeight * this.cellHeight))))
		{
			final Vibrator v = (Vibrator) this.context
					.getSystemService(Context.VIBRATOR_SERVICE);
			if (((View) this.getParent()).findViewById(R.id.ibTools) != null) {
				v.vibrate(100);
				((View) this.getParent()).findViewById(R.id.ibTools).performClick();
			} else if (((View) this.getParent()).findViewById(R.id.bToolbox) != null) {
				v.vibrate(100);
				((View) this.getParent()).findViewById(R.id.bToolbox).performClick();
			}
			return true;
		}
		return false;
	}

	public boolean onDoubleTapEvent(final MotionEvent e) {
		return false;
	}

	public boolean onDown(final MotionEvent event) {
		Log.d(TAG, "1");
		this.matrix.getValues(this.m);
		final float transX = this.m[Matrix.MTRANS_X] * -1;
		final float transY = this.m[Matrix.MTRANS_Y] * -1;
		final float scaleX = this.m[Matrix.MSCALE_X];
		final float scaleY = this.m[Matrix.MSCALE_Y];
		this.lastTouchX = (int) ((event.getX() + transX) / scaleX);
		this.lastTouchY = (int) ((event.getY() + transY) / scaleY);
		this.lastTouchX = Math.abs(this.lastTouchX);
		this.lastTouchY = Math.abs(this.lastTouchY);
		if (((this.lastTouchX) < (this.canvasBitmap.getWidth() - (this.gWidth * this.cellWidth))) ||
				((this.lastTouchY) < (this.canvasBitmap.getHeight() - (this.gHeight * this.cellHeight))))
		{
			Log.d(TAG, "2");
			if (event.getPointerCount() > 2)
			{
				// Go to movement.
				this.isGameplay = false;
				((View) this.getParent()).findViewById(R.id.ibTools).setBackgroundResource(R.drawable.move);
				return true;
			}
			return true;
		}
		try {
			Log.d(TAG, "3 " + isGameplay);
			if (this.isGameplay) {
				Log.d(TAG, "4");

				int indexX = (int) Math.floor((this.lastTouchX - (this.cellWidth * this.lSide))
						/ this.cellWidth);
				int indexY = (int) Math.floor((this.lastTouchY - (this.cellHeight * this.lTop))
						/ this.cellHeight);
				if (this.lastTouchX >= ((this.lSide + this.gWidth) * this.cellWidth)) {
					indexX -= 1;
				}
				if (this.lastTouchY >= ((this.lTop + this.gHeight) * this.cellHeight)) {
					indexY -= 1;
				}
				this.oldCurrent = this.gCurrent;
				final char[] temp = this.gCurrent.toCharArray();
				final String past = this.gCurrent;
				if (temp[(indexY * this.gWidth) + indexX] == '0') {
					temp[(indexY * this.gWidth) + indexX] = this.colorCharacter;
				} else if (temp[(indexY * this.gWidth) + indexX] == 'x') {
					temp[(indexY * this.gWidth) + indexX] = '0';
				} else if (temp[(indexY * this.gWidth) + indexX] == this.colorCharacter) {
					temp[(indexY * this.gWidth) + indexX] = 'x';
				} else {
					temp[(indexY * this.gWidth) + indexX] = this.colorCharacter;
				}
				this.gCurrent = String.valueOf(temp);
				if (!past.equals(this.gCurrent)) {
					Log.d(TAG, "5");

					this.previousX = indexX;
					this.previousY = indexY;
					new Thread(new Runnable() {

						public void run() {
							TouchImageView.this.h.post(new Runnable() {

								public void run() {
									if (TouchImageView.this.historyListener != null) {

										Log.d(TAG, "6");
										TouchImageView.this.historyListener
												.action(TouchImageView.this.oldCurrent);
									}
									TouchImageView.this.bitmapFromCurrent();
								}

							});
						}

					}).start();
				}
				this.checkWin();
				return true;
			}
		} catch (final Exception e)
		{
			// For those odd ball touches ;). Lol.
		}
		return false;
	}

	public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX,
			final float velocityY) {
		return false;
	}

	public void onLongPress(final MotionEvent event) {
		// Make sure we're outside of the game.
		this.matrix.getValues(this.m);
		final float transX = this.m[Matrix.MTRANS_X] * -1;
		final float transY = this.m[Matrix.MTRANS_Y] * -1;
		final float scaleX = this.m[Matrix.MSCALE_X];
		final float scaleY = this.m[Matrix.MSCALE_Y];
		this.lastTouchX = (int) ((event.getX() + transX) / scaleX);
		this.lastTouchY = (int) ((event.getY() + transY) / scaleY);
		this.lastTouchX = Math.abs(this.lastTouchX);
		this.lastTouchY = Math.abs(this.lastTouchY);
		if (((this.lastTouchX) < (this.canvasBitmap.getWidth() - (this.gWidth * this.cellWidth))) ||
				((this.lastTouchY) < (this.canvasBitmap.getHeight() - (this.gHeight * this.cellHeight))))
		{
			final Vibrator v = (Vibrator) this.context
					.getSystemService(Context.VIBRATOR_SERVICE);
			if (((View) this.getParent()).findViewById(R.id.ibTools) != null) {
				v.vibrate(100);
				((View) this.getParent()).findViewById(R.id.ibTools).performClick();
			} else if (((View) this.getParent()).findViewById(R.id.bToolbox) != null) {
				v.vibrate(100);
				((View) this.getParent()).findViewById(R.id.bToolbox).performClick();
			}
		}
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec,
			final int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		this.viewWidth = MeasureSpec.getSize(widthMeasureSpec);
		this.viewHeight = MeasureSpec.getSize(heightMeasureSpec);

		//
		// Rescales image on rotation
		//
		if (((this.oldMeasuredHeight == this.viewWidth) && (this.oldMeasuredHeight == this.viewHeight))
				|| (this.viewWidth == 0) || (this.viewHeight == 0)) {
			return;
		}
		this.oldMeasuredHeight = this.viewHeight;
		this.oldMeasuredWidth = this.viewWidth;

		if (this.saveScale == 1) {
			// Fit to screen.
			float scale;

			final Drawable drawable = this.getDrawable();
			if ((drawable == null) || (drawable.getIntrinsicWidth() == 0)
					|| (drawable.getIntrinsicHeight() == 0)) {
				return;
			}
			final int bmWidth = drawable.getIntrinsicWidth();
			final int bmHeight = drawable.getIntrinsicHeight();

			final float scaleX = (float) this.viewWidth / (float) bmWidth;
			final float scaleY = (float) this.viewHeight / (float) bmHeight;
			scale = Math.min(scaleX, scaleY);
			this.matrix.setScale(scale, scale);

			// Center the image
			float redundantYSpace = this.viewHeight - (scale * bmHeight);
			float redundantXSpace = this.viewWidth - (scale * bmWidth);
			redundantYSpace /= 2;
			redundantXSpace /= 2;

			this.matrix.postTranslate(redundantXSpace, redundantYSpace);

			this.origWidth = this.viewWidth - (2 * redundantXSpace);
			this.origHeight = this.viewHeight - (2 * redundantYSpace);
			this.setImageMatrix(this.matrix);
		}
		this.fixTrans();
	}

	public boolean onScroll(final MotionEvent e1, final MotionEvent event, final float distanceX,
			final float distanceY) {
		if (event.getPointerCount() > 2)
		{
			// Go to movement.
			this.isGameplay = false;
			((View) this.getParent()).findViewById(R.id.ibTools).setBackgroundResource(R.drawable.move);
		}
		this.matrix.getValues(this.m);
		final float transX = this.m[Matrix.MTRANS_X] * -1;
		final float transY = this.m[Matrix.MTRANS_Y] * -1;
		final float scaleX = this.m[Matrix.MSCALE_X];
		final float scaleY = this.m[Matrix.MSCALE_Y];
		this.lastTouchX = (int) ((event.getX() + transX) / scaleX);
		this.lastTouchY = (int) ((event.getY() + transY) / scaleY);
		this.lastTouchX = Math.abs(this.lastTouchX);
		this.lastTouchY = Math.abs(this.lastTouchY);
		if (((this.lastTouchX) < (this.canvasBitmap.getWidth() - (this.gWidth * this.cellWidth))) ||
				((this.lastTouchY) < (this.canvasBitmap.getHeight() - (this.gHeight * this.cellHeight))))
		{
			return false;
		}
		if (this.isGameplay) {
			int indexX = (int) Math.floor((this.lastTouchX - (this.cellWidth * this.lSide))
					/ this.cellWidth);
			int indexY = (int) Math.floor((this.lastTouchY - (this.cellHeight * this.lTop))
					/ this.cellHeight);
			if (this.lastTouchX >= ((this.lSide + this.gWidth) * this.cellWidth)) {
				indexX -= 1;
			}
			if (this.lastTouchY >= ((this.lTop + this.gHeight) * this.cellHeight)) {
				indexY -= 1;
			}
			this.oldCurrent = this.gCurrent;
			final char[] temp = this.gCurrent.toCharArray();
			final String past = this.gCurrent;
			if ((indexX != this.previousX) || (this.previousY != indexY)) {
				this.previousX = indexX;
				this.previousY = indexY;
				if (((indexY * this.gWidth) + indexX) < temp.length)
				{
					if (temp[(indexY * this.gWidth) + indexX] == '0') {
						temp[(indexY * this.gWidth) + indexX] = this.colorCharacter;
					} else if (temp[(indexY * this.gWidth) + indexX] == 'x') {
						temp[(indexY * this.gWidth) + indexX] = '0';
					} else if (temp[(indexY * this.gWidth) + indexX] == this.colorCharacter) {
						temp[(indexY * this.gWidth) + indexX] = 'x';
					} else {
						temp[(indexY * this.gWidth) + indexX] = this.colorCharacter;
					}
				} else {
					return false;
				}
			}
			this.gCurrent = String.valueOf(temp);
			if (!past.equals(this.gCurrent)) {

				this.previousX = indexX;
				this.previousY = indexY;
				new Thread(new Runnable() {

					public void run() {
						TouchImageView.this.h.post(new Runnable() {

							public void run() {
								if (TouchImageView.this.historyListener != null) {
									TouchImageView.this.historyListener
											.action(TouchImageView.this.oldCurrent);
								}
								TouchImageView.this.bitmapFromCurrent();
							}

						});
					}

				}).start();
			}
			this.checkWin();
			return true;
		}
		return false;
	}

	public void onShowPress(final MotionEvent event) {
	}

	public boolean onSingleTapConfirmed(final MotionEvent event) {
		return false;
	}

	public boolean onSingleTapUp(final MotionEvent e) {
		return false;
	}

	private char[][] puzzleTo2DArray(final String in) {
		final char[][] result = new char[this.gHeight][this.gWidth];
		int runner = 0;
		for (int i = 0; i != result.length; ++i) {
			for (int j = 0; j != result[i].length; ++j) {
				result[i][j] = in.charAt(runner++);
			}
		}
		return result;
	}

	String removeDuplicates(final String str) {
		// 10001 -> 101
		// 2233 -> 23

		final StringBuilder noDupes = new StringBuilder();
		char look = ' ';
		for (int i = 0; i != str.length();) {
			boolean isDone = false;
			look = str.charAt(i);
			for (int j = i + 1; j != str.length(); ++j) {
				if (str.charAt(j) != look) {
					noDupes.append(look);
					isDone = true;
					i = j;
					break;
				}
			}
			if (!isDone) {
				i++;
			}
		}
		noDupes.append(look);
		return noDupes.toString();
	}

	public void setHistoryListener(final HistoryListener hl) {
		this.historyListener = hl;
	}

	public void setMaxZoom(final float x) {
		this.maxScale = x;
	}

	// Get bundled info and set it for use.
	public void setPicogramInfo(final Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			return;
		}
		this.isRefreshing = savedInstanceState.getBoolean("refresh", false);
		this.gName = savedInstanceState.getString("name", "");
		this.gCurrent = savedInstanceState.getString("current");
		this.gHeight = Integer.parseInt(savedInstanceState.getString("height", "0"));
		this.gWidth = Integer.parseInt(savedInstanceState.getString("width", "0"));
		Log.d(TAG, "Cur: " + gCurrent);
		while (this.gCurrent.length() != (this.gHeight * this.gWidth))
		{
			Log.d(TAG, "Adding 0");
			this.gCurrent += "0";
		}
		this.gSolution = savedInstanceState.getString("solution");
		Log.d(TAG, "Cur: " + gSolution);
		this.gId = savedInstanceState.getString("id", (this.gSolution + "").hashCode() + "");
		final String[] cols = savedInstanceState.getString("colors").split(",");
		this.gColors = new int[cols.length];

		for (int i = 0; i != cols.length; ++i) {
			this.gColors[i] = Integer.parseInt(cols[i]);
		}

		this.oldCurrent = "";
		if (this.gCurrent == null) {
			this.gCurrent = ""; // Start it out as empty.
			for (int i = 0; i != (this.gHeight * this.gWidth); ++i) {
				this.gCurrent += "0";
			}
		}

		if (this.gSolution == null)
		{
			this.gSolution = this.gCurrent; // Not a real game, so no win listener.
		}
		else if (gSolution.length() > gWidth * gHeight)
			gSolution = gSolution.substring(0, gWidth * gHeight);// For mess ups early on, remove at a later date.
		// Below is for optimization so it only draws all the squares once and
		// only again after it's changed.
		for (int i = 0; i != this.gSolution.length(); i++) {
			this.oldCurrent += "-";
		}

		this.bitmapFromCurrent();
	}

	public void setWinListener(final WinnerListener winListener) {
		this.winListener = winListener;
	}

	private void sharedConstructing(final Context context) {
		super.setClickable(true);
		this.context = context;
		this.mScaleDetector = new ScaleGestureDetector(context,
				new ScaleListener());
		this.matrix = new Matrix();
		this.m = new float[9];
		this.setImageMatrix(this.matrix);
		this.setScaleType(ScaleType.MATRIX);

		this.setOnTouchListener(this.touchListener);
		this.mDetector = new GestureDetectorCompat(context, this);
		this.mDetector.setOnDoubleTapListener(this);

	}
}
