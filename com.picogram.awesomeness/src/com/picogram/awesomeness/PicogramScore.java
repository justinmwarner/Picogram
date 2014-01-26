
package com.picogram.awesomeness;

import com.stackmob.sdk.model.StackMobModel;

public class PicogramScore extends StackMobModel {
	private int score;
	private String author;

	PicogramScore()
	{
		super(PicogramScore.class);
		this.id = "0";
		this.setScore(0);
		this.setAuthor("");
	}

	String getAuthor() {
		return this.author;
	}

	int getScore() {
		return this.score;
	}

	void setAuthor(final String author) {
		this.author = author;
	}

	void setScore(final int score) {
		this.score = score;
	}
}
