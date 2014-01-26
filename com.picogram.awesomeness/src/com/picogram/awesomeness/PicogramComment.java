
package com.picogram.awesomeness;

import com.stackmob.sdk.model.StackMobModel;

public class PicogramComment extends StackMobModel {
	private String comment, author, puzzleId;

	PicogramComment()
	{
		super(PicogramComment.class);
		this.id = "0";
		this.setComment("");
		this.setAuthor("");
	}

	public PicogramComment(final String id, final String author, final String comment) {
		super(PicogramComment.class);
		this.puzzleId = id;
		this.comment = comment;
		this.author = author;

	}

	String getAuthor() {
		return this.author;
	}

	String getComment() {
		return this.comment;
	}

	String getPuzzleId() {
		return this.puzzleId;
	}

	void setAuthor(final String author) {
		this.author = author;
	}

	void setComment(final String comment) {
		this.comment = comment;
	}

	void setPuzzleId(final String puzzleId) {
		this.puzzleId = puzzleId;
	}
}
