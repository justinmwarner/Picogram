package com.picogram.awesomeness;

import com.stackmob.sdk.model.StackMobModel;

public class PicogramHighscore extends StackMobModel implements Comparable<PicogramHighscore> {
	private String name, puzzleId;
	private long score;

	PicogramHighscore() {
		super(PicogramHighscore.class);
	}

	PicogramHighscore(final String name, final String puzzleId, final long l)
	{
		super(PicogramHighscore.class);
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
