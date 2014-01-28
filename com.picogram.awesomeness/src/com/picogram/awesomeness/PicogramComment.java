
package com.picogram.awesomeness;

import com.parse.ParseObject;

public class PicogramComment {
	private String comment, author, puzzleId;

	PicogramComment()
	{
		this.setPuzzleId("0");
		this.setComment("");
		this.setAuthor("");
	}

	public PicogramComment(final String id, final String author, final String comment) {
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

	public void save() {
		final ParseObject po = new ParseObject("PicogramComment");
		po.put("puzzleId", this.puzzleId);
		po.put("comment", this.comment);
		po.put("author", this.author);
		po.saveEventually();
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
