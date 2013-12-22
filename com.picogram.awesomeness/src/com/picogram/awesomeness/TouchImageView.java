package com.picogram.awesomeness;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

public class TouchImageView extends ImageView {

	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(final ScaleGestureDetector detector) {
			float mScaleFactor = detector.getScaleFactor();
			final float origScale = saveScale;
			saveScale *= mScaleFactor;
			if (saveScale > maxScale) {
				saveScale = maxScale;
				mScaleFactor = maxScale / origScale;
			} else if (saveScale < minScale) {
				saveScale = minScale;
				mScaleFactor = minScale / origScale;
			}

			if (((origWidth * saveScale) <= viewWidth)
					|| ((origHeight * saveScale) <= viewHeight)) {
				matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2,
						viewHeight / 2);
			} else {
				matrix.postScale(mScaleFactor, mScaleFactor,
						detector.getFocusX(), detector.getFocusY());
			}
			fixTrans();
			return true;
		}

		@Override
		public boolean onScaleBegin(final ScaleGestureDetector detector) {
			mode = ZOOM;
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
	protected static final String TAG = "TouchImageView";
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

	// These take a long time to calculate and don't change. Only do it once.
	ArrayList<String[]> topHints;
	ArrayList<String> sideHints;
	ArrayList<String> columns;
	ArrayList<String> rows;
	int longestSide, longestTop;
	Bitmap bm;
	Canvas canvasBitmap;
	Paint paintBitmap;

	// Griddler specifics.
	String gCurrent, gSolution;

	int gWidth, gHeight, gId, lTop, lSide, cellWidth, cellHeight;

	int[] gColors;

	/*
	 * Interface and such to see if we win.
	 */
	private WinnerListener winListener;

	OnTouchListener touchListener = new OnTouchListener() {

		public boolean onTouch(final View v, final MotionEvent event) {
			if (!isGameplay) {
				mScaleDetector.onTouchEvent(event);
				final PointF curr = new PointF(event.getX(), event.getY());

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					last.set(curr);
					start.set(last);
					mode = DRAG;
					break;

				case MotionEvent.ACTION_MOVE:
					if (mode == DRAG) {
						final float deltaX = curr.x - last.x;
						final float deltaY = curr.y - last.y;
						final float fixTransX = TouchImageView.this
								.getFixDragTrans(deltaX, viewWidth, origWidth
										* saveScale);
						final float fixTransY = TouchImageView.this
								.getFixDragTrans(deltaY, viewHeight, origHeight
										* saveScale);
						matrix.postTranslate(fixTransX, fixTransY);
						fixTrans();
						last.set(curr.x, curr.y);
					}
					break;

				case MotionEvent.ACTION_UP:
					mode = NONE;
					final int xDiff = (int) Math.abs(curr.x - start.x);
					final int yDiff = (int) Math.abs(curr.y - start.y);
					if ((xDiff < CLICK) && (yDiff < CLICK)) {
						performClick();
					}
					break;

				case MotionEvent.ACTION_POINTER_UP:
					mode = NONE;
					break;
				}

				setImageMatrix(matrix);
				invalidate();
			} else {
				if ((event.getAction() == MotionEvent.ACTION_MOVE)
						|| (event.getAction() == MotionEvent.ACTION_DOWN)) {
					matrix.getValues(m);
					final float transX = m[Matrix.MTRANS_X] * -1;
					final float transY = m[Matrix.MTRANS_Y] * -1;
					final float scaleX = m[Matrix.MSCALE_X];
					final float scaleY = m[Matrix.MSCALE_Y];
					lastTouchX = (int) ((event.getX() + transX) / scaleX);
					lastTouchY = (int) ((event.getY() + transY) / scaleY);
					lastTouchX = Math.abs(lastTouchX);
					lastTouchY = Math.abs(lastTouchY);
					final int indexX = (int) Math
							.floor((lastTouchX - (cellWidth * lSide))
									/ cellWidth);
					final int indexY = (int) Math
							.floor((lastTouchY - (cellHeight * lTop))
									/ cellHeight);
					Log.d(TAG, "Leaving 3");
					if ((lastTouchX < (cellWidth * lSide))
							|| (lastTouchY < (cellHeight * lTop))
							|| (lastTouchX > bm.getWidth())
							|| (lastTouchY > ((lTop + gHeight) * cellHeight))) {
						// If we're on the hints, just get out of there.
						// Don't do anything.
						if (lastTouchY > ((lTop + gHeight) * cellHeight)) {
						}
						return true;
					}
					final char[] temp = gCurrent.toCharArray();
					final String past = gCurrent;
					if (((indexY * gWidth) + indexX) < temp.length) {
						temp[(indexY * gWidth) + indexX] = colorCharacter;
						gCurrent = String.valueOf(temp);
						if (!past.equals(gCurrent)) {
							new Thread(new Runnable() {

								public void run() {
									h.post(new Runnable() {

										public void run() {
											TouchImageView.this
													.bitmapFromCurrent();
										}

									});
								}

							}).start();
						}
					}
					if (gCurrent.equals(gSolution)) {
						if (winListener != null) {
							winListener.win();
						} else {
							try {
								throw new Exception("No WinListener!");
							} catch (final Exception e) {
								// Should never get here.
								e.printStackTrace();
								return false;
							}
						}
					}
				}

			}
			return true; // indicate event was handled
		}

	};

	ArrayList<Integer> topColors = new ArrayList();
	ArrayList<Integer> sideColors = new ArrayList();

	public TouchImageView(final Context context) {
		super(context);
		this.sharedConstructing(context);
	}

	public TouchImageView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.sharedConstructing(context);
	}

	public boolean isRefreshing = false;

	// Convert current String to a bitmap that's drawable. This will draw
	// everything: grid, numbers, and onclicks.
	private void bitmapFromCurrent() {
		// Get a 2D array of "current" griddler.
		final char current2D[][] = this.solutionTo2DArray();
		// Create bitmap based on the current. Make a int array with pixel
		// colors.
		// Because of how we're making the top hints, it needs its own method.
		if (this.topHints == null || isRefreshing) {
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
			this.bm = Bitmap.createBitmap(
					(this.gWidth + this.longestSide) * 50,
					(this.gHeight + this.longestTop) * 50,
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

	public void clearGame() {
		this.gCurrent.replace("1", "0");
		this.bitmapFromCurrent();
	}

	// Site
	// http://stackoverflow.com/questions/8629202/fast-conversion-from-one-dimensional-array-to-two-dimensional-in-java
	private char[][] convertOneDimensionalToTwoDimensional(
			final int numberOfRows, final int rowSize, final char[] srcMatrix) {

		final int srcMatrixLength = srcMatrix.length;
		int srcPosition = 0;

		final char[][] returnMatrix = new char[numberOfRows][];
		for (int i = 0; i < numberOfRows; i++) {
			final char[] row = new char[rowSize];
			final int nextSrcPosition = srcPosition + rowSize;
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
			// INFO: This is where we draw the board.
			this.paintBitmap.setColor(this.gColors[Integer
					.parseInt(this.gCurrent.charAt(i) + "")]);
			if (this.gColors[Integer.parseInt(this.gCurrent.charAt(i) + "")] == Color.TRANSPARENT) {
				this.paintBitmap.setColor(Color.WHITE);
			}
			this.canvasBitmap.drawRect(r, this.paintBitmap);
			/*
			 * if (i != 0) { if (this.gCurrent.charAt(i) !=
			 * this.gCurrent.charAt(i - 1)) { if (this.gCurrent.charAt(i) ==
			 * '0') { this.paintBitmap.setColor(Color.WHITE); } else {
			 * this.paintBitmap.setColor(Color.BLACK); } } } else { if
			 * (this.gCurrent.charAt(i) == '0') {
			 * this.paintBitmap.setColor(Color.WHITE); } else {
			 * this.paintBitmap.setColor(Color.BLACK); } }
			 */
			// this.paintBitmap.setColor(this.gColors[Integer.parseInt(this.colorCharacter
			// + "")]);
			// this.canvasBitmap.drawRect(r, this.paintBitmap);
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
		this.paintBitmap.setColor(this.getResources().getColor(
				R.color.foreground));
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
				this.canvasBitmap
						.drawText(
								this.topHints.get(i)[j],
								((this.longestSide * widthOffset)
										+ (widthOffset / 2) + (j * widthOffset)) - 5,
								(this.longestTop * heightOffset)
										- (heightOffset * i) - 5,
								this.paintBitmap);
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
				this.canvasBitmap.drawText(side + " ",
						(this.longestSide * widthOffset) - 5
								- (j * this.paintBitmap.getFontSpacing()),
						(this.longestTop * heightOffset) + (i * heightOffset)
								+ ((2 * heightOffset) / 3), this.paintBitmap);
			}
		}
		this.paintBitmap.setTextAlign(oldAlign);
	}

	private void drawOnCanvas() {
		// White out whole canvas.
		this.drawWhiteCanvas();
		// Draw game surface.
		this.drawGame();
		// Draw gridlines and hints
		this.drawGridlines();
		this.drawHints();
		this.paintBitmap.setColor(Color.RED);
		this.canvasBitmap.drawCircle(this.lastTouchX, this.lastTouchY, 5,
				this.paintBitmap);
	}

	private void drawWhiteCanvas() {
		final Bitmap draw = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.light_grid);
		final Shader old = this.paintBitmap.getShader();
		this.paintBitmap.setShader(new BitmapShader(draw, TileMode.REPEAT,
				TileMode.REPEAT));
		this.paintBitmap.setColor(this.getResources().getColor(
				R.color.background));
		this.paintBitmap.setColor(Color.TRANSPARENT);
		this.canvasBitmap.drawRect(0, 0, this.canvasBitmap.getWidth(),
				this.canvasBitmap.getHeight(), this.paintBitmap);
		this.paintBitmap.setShader(old);
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
		final ArrayList<Integer> result = new ArrayList();
		final ArrayList<char[]> chars = new ArrayList();
		for (final String segment : segments) {
			if (segment.matches("[0]+")) {
				chars.add(new char[] { '0' });
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

	private int[] getPixelArrayFromString(final String from, final int length) {
		final int[] colors = new int[length];
		for (int i = 0; i != colors.length; ++i) {
			if (from.charAt(i) == '0') {
				colors[i] = Color.WHITE;
			} else {
				colors[i] = Color.BLACK;
			}
		}
		return colors;
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

	// Just add on fluff area for the hints on the top and on the side.
	private int[] resizeBitMapsForHints(final int[] colors,
			final int longestTop, final int longestSide) {
		final int result[] = new int[(longestTop * (longestSide + this.gWidth))
				+ colors.length + (this.gHeight * longestSide)];
		int runner;
		// Fill up the top with blank white.
		for (runner = 0; runner != (longestTop * (longestSide + this.gWidth)); ++runner) {
			result[runner] = Color.WHITE;
		}
		// Fill side hints with white, and the image with what was in it
		// previously.
		int colorRunner = 0; // Used to run through original colors.
		for (int i = 0; i != this.gHeight; ++i) {
			// Draw side for hints.
			for (int j = 0; j != longestSide; ++j) {
				result[runner++] = Color.WHITE;
			}
			// Add in the array/picture.
			for (int j = 0; j != this.gWidth; ++j) {
				result[runner++] = colors[colorRunner++];
			}
		}
		return result;
	}

	// Get bundled info and set it for use.
	public void setGriddlerInfo(final Bundle savedInstanceState) {
		this.gCurrent = savedInstanceState.getString("current");
		this.gHeight = Integer.parseInt(savedInstanceState.getString("height"));
		this.gWidth = Integer.parseInt(savedInstanceState.getString("width"));
		if (savedInstanceState.getString("id") != null) {
			this.gId = Integer.parseInt(savedInstanceState.getString("id"));
		}
		this.gSolution = savedInstanceState.getString("solution");
		final String[] cols = savedInstanceState.getString("colors").split(",");
		this.gColors = new int[cols.length];
		for (int i = 0; i != cols.length; ++i) {
			this.gColors[i] = Integer.parseInt(cols[i]);
		}
		this.bitmapFromCurrent();
	}

	public void setMaxZoom(final float x) {
		this.maxScale = x;
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
	}

	private char[][] solutionTo2DArray() {
		final char[][] result = new char[this.gHeight][this.gWidth];
		int runner = 0;
		for (int i = 0; i != result.length; ++i) {
			for (int j = 0; j != result[i].length; ++j) {
				result[i][j] = this.gSolution.charAt(runner++);
			}
		}
		return result;
	}
}
