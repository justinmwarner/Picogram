
package com.picogram.awesomeness;

import com.stackmob.sdk.model.StackMobModel;

public class Griddler extends StackMobModel {
	private String status, name, diff, rate, author, width, height, solution, current,
			numberOfColors;
	private int[] colors;
	private int numberOfRatings;

	public Griddler() {
		super(Griddler.class);

	}

	/*
	 * // Constructor for test. public Griddler(final int i) { super(Griddler.class); //this.id = "" + i; this.status = "" + 0; this.name = "Name: " + i; }
	 */
	public Griddler(final String status, final String name,
			final String difficulty, final String rank, final int numberOfRatings,
			final String author, final String width,
			final String height, final String solution, final String current, final int numColors,
			final int[] colors) {
		super(Griddler.class);
		// this.id = id;
		this.status = status;
		this.name = name;
		this.diff = difficulty;
		this.rate = rank;
		this.author = author;
		this.width = width;
		this.height = height;
		this.current = current;
		this.solution = solution;
		this.colors = colors;
		this.numberOfColors = numColors + "";
		this.numberOfRatings = numberOfRatings;
	}

	public String getAuthor() {
		return this.author;
	}

	public int[] getColors() {
		return this.colors;
	}

	public String getCurrent() {
		return this.current;
	}

	public String getDiff() {
		return this.diff;
	}

	public String getHeight() {
		return this.height;
	}

	/*
	 * public String getId() { return this.id; }
	 */
	public String getName() {
		return this.name;
	}

	public String getNumberOfColors() {
		return this.numberOfColors;
	}

	public int getNumberOfRatings() {
		return this.numberOfRatings;
	}

	public String getRate() {
		return this.rate;
	}

	public String getSolution() {
		return this.solution;
	}

	public String getStatus() {
		return this.status;
	}

	public String getWidth() {
		return this.width;
	}

	public void setAuthor(final String author) {
		this.author = author;
	}

	public void setColors(final int[] colors) {
		this.colors = colors;
	}

	public void setCurrent(final String current) {
		this.current = current;
	}

	public void setDiff(final String diff) {
		this.diff = diff;
	}

	public void setHeight(final String height) {
		this.height = height;
	}

	/*
	 * public void setId(final String id) { this.id = id; }
	 */
	public void setName(final String name) {
		this.name = name;
	}

	public void setNumberOfColors(final String numberOfColors) {
		this.numberOfColors = numberOfColors;
	}

	public void setNumberOfRatings(final int numberOfRatings) {
		this.numberOfRatings = numberOfRatings;
	}

	public void setRate(final String rank) {
		this.rate = rank;
	}

	public void setSolution(final String solution) {
		this.solution = solution;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public void setWidth(final String width) {
		this.width = width;
	}

}
