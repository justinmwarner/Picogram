
package com.picogram.awesomeness;

import android.util.Log;

public class PicogramPart {
	private static final String TAG = "PicogramPart";
	String current, solution;
	int width, height;

	PicogramPart() {
		this.current = "";
		this.solution = "";
		this.width = this.height = 0;
	}

	public void appendCurrent(final String app)
	{
		final StringBuilder sb = new StringBuilder(this.current);
		sb.append(app);
		this.current = sb.toString();
	}

	public void appendSolution(final String app)
	{
		final StringBuilder sb = new StringBuilder(this.solution);
		sb.append(app);
		this.solution = sb.toString();
	}

	public char[][] get2D() {
		final char[][] result = new char[this.height][this.width];
		int run = 0;
		for (int i = 0; i != result.length; ++i) {
			for (int j = 0; j != result[i].length; ++j)
			{
				result[i][j] = this.current.charAt(run);
				run++;
			}
		}
		return result;
	}

	public String getCurrent() {
		return this.current;
	}

	public int getHeight() {
		return this.height;
	}

	public String getSolution() {
		return this.solution;
	}

	public int getWidth() {
		return this.width;
	}

	public void setCurrent(final String current) {
		this.current = current;
	}

	public void setHeight(final int height) {
		this.height = height;
	}

	public void setSolution(final String solution) {
		this.solution = solution;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	@Override
	public String toString() {
		return this.width + " " + this.height + " " + this.current.length();
	}
}
