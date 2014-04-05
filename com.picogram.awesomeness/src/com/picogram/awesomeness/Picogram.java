
package com.picogram.awesomeness;

import android.content.Context;
import android.graphics.Color;

import com.parse.ParseObject;

public class Picogram implements Comparable {
	private String status, name, diff, author, width, height, solution,
	current, numberOfColors, colors, id;
	private int numberOfRatings, rate;
	private long highscore = 0;

	public Picogram() {
	}

	public Picogram(final ParseObject object) {
		this.id = object.getString("id");
		this.name = object.getString("name");
		this.diff = object.getString("diff");
		this.rate = object.getInt("rate");
		this.numberOfRatings = object.getInt("numberOfRatings");
		this.author = object.getString("author");
		this.width = object.getString("width");
		this.height = object.getString("height");
		this.solution = object.getString("solution");
		this.current = object.getString("current");
		this.numberOfColors = object.getString("numberOfColors");
		this.colors = object.getString("colors");
	}

	public Picogram(final String id, final String status, final String name,
			final String difficulty, final String rank,
			final int numberOfRatings, final String author, final String width,
			final String height, final String solution, final String current,
			final int numColors, final String colors) {
		this.setID(id);
		this.status = status;
		this.name = name;
		this.diff = difficulty;
		this.rate = Integer.parseInt(rank);
		this.author = author;
		this.width = width;
		this.height = height;
		this.current = current;
		this.solution = solution;
		this.colors = colors;
		this.numberOfColors = numColors + "";
		this.numberOfRatings = numberOfRatings;
	}

	public Picogram(final String[] arr) {
		final String id = arr[0];
		final String author = arr[1];
		final String name = arr[2];
		final String rate = arr[3];
		final String solution = arr[4];
		String current = arr[5];
		final String diff = arr[6];
		final String width = arr[7];
		final String height = arr[8];
		final String status = arr[9];
		final String personalRank = arr[12];
		final String isUploaded = arr[13];
		int numColors = 0;
		final int nor = 0;
		String colors = null;
		if ((arr[10] != null) && (arr[11] != null)) {
			numColors = Integer.parseInt(arr[10]);
			colors = arr[11];
		}
		if (current == null) {
			current = "";
			for (int i = 0; i != solution.length(); ++i) {
				current += "0";
			}
		}
		this.setID(id);
		this.status = status;
		this.name = name;
		this.diff = diff;
		this.rate = Integer.parseInt(rate);
		this.numberOfRatings = nor;
		this.author = author;
		this.width = width;
		this.height = height;
		this.solution = solution;
		this.current = current;
		this.numberOfColors = numColors + "";
		this.colors = colors;
	}

	public int compareTo(final Object g) {
		// equal is equivlant to making it less than, so no 0 needed.
		return (this.rate >= ((Picogram) g).getRating()) ? 1 : -1;
	}

	public String getAuthor() {
		return this.author;
	}

	public String getColors() {
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

	public long getHighscore() {
		return this.highscore;
	}

	public String getID() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getNumberOfColors() {
		return this.numberOfColors;
	}

	public int getNumberOfRatings() {
		return this.numberOfRatings;
	}

	public int getRating() {
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

	public Picogram nullsToValue(final Context a) {
		if (this.getID() == null) {
			if (this.solution != null) {
				this.setID(this.solution.hashCode() + "");
			} else {
				this.setID("0");
			}
		}
		if (this.status == null) {
			this.status = "0";
		}
		if (this.name == null) {
			this.name = "N/A";
		}
		if (this.diff == null) {
			this.diff = "1";
		}
		if (this.author == null) {
			this.author = "N/A";
		}
		if (this.width == null) {
			this.width = "0";
		}
		if (this.height == null) {
			this.height = "0";
		}
		if (this.solution == null) {
			this.solution = "";
		}
		if (this.current == null) {
			this.current = "";
			for (int i = 0; i != (Integer.parseInt(this.width)
					* Integer.parseInt(this.height)); ++i) {
				this.current += "0";
			}
		}
		if (this.numberOfColors == null) {
			this.numberOfColors = "2";
		}
		if (this.colors == null) {
			this.colors = Color.TRANSPARENT + "," + Color.BLACK;
		}
		// Now update the rating database, if it already exists, this will
		// return nothing.
		final SQLiteRatingAdapter sra = new SQLiteRatingAdapter(a, "Rating", null, 2);
		sra.insertOnOpenOnlineGame(this.getID());
		sra.close();
		return this;
	}

	public void save() {
		final ParseObject gameScore = new ParseObject("Picogram");
		gameScore.put("name", this.name);
		gameScore.put("diff", this.diff);
		gameScore.put("puzzleId", this.id);
		gameScore.put("rate", this.rate);
		gameScore.put("numRating", this.numberOfRatings);
		gameScore.put("author", this.author);
		gameScore.put("width", this.width);
		gameScore.put("height", this.height);
		gameScore.put("solution", this.solution);
		gameScore.put("numberOfColors", this.numberOfColors);
		gameScore.put("colors", this.colors);
		gameScore.saveEventually();
	}

	public void setAuthor(final String author) {
		this.author = author;
	}


	public void setColors(final String colors) {
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

	public void setHighscore(final long highscore) {
		this.highscore = highscore;
	}

	public void setID(final String id) {
		this.id = id;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setNumberOfColors(final String numberOfColors) {
		this.numberOfColors = numberOfColors;
	}

	public void setNumberOfRatings(final int numberOfRatings) {
		this.numberOfRatings = numberOfRatings;
	}

	public void setRating(final String rank) {
		this.rate = Integer.parseInt(rank);
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

	@Override
	public String toString() {
		return this.getID() + " " + this.status + " " + this.name + " " + this.diff
				+ " " + this.rate + " " + this.author + " " + this.width + " "
				+ this.height + " " + this.solution + " " + this.current + " "
				+ this.numberOfColors + " " + this.colors + " "
				+ this.numberOfRatings;
	}
}
