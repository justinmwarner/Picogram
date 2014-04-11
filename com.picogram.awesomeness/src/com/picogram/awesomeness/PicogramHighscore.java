
package com.picogram.awesomeness;

import com.parse.ParseObject;

public class PicogramHighscore implements Comparable<PicogramHighscore> {
	private String name, puzzleId;
	private long score;

	PicogramHighscore() {
	}

	PicogramHighscore(final String name, final String puzzleId, final long l)
	{
		this.name = name;
		this.puzzleId = puzzleId;
		this.score = l;
	}

	public int compareTo(final PicogramHighscore another) {
		final Long other = Long.parseLong("" + another.getScore());
		return other.compareTo(this.score);
	}

	String getName() {
		return this.name;
	}

	String getPuzzleId() {
		return this.puzzleId;
	}

	long getScore() {
		return this.score;
	}

	public void save() {
		final ParseObject po = new ParseObject("PicogramHighscore");
		po.put("puzzleId", this.puzzleId);
		po.put("score", this.score);
		po.put("name", this.name);
		po.saveEventually();
	}

	void setName(final String name) {
		this.name = name;
	}

	void setPuzzleId(final String puzzleId) {
		this.puzzleId = puzzleId;
	}

	void setScore(final long score) {
		this.score = score;
	}
}
