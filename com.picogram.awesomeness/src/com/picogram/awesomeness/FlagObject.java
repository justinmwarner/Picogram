package com.picogram.awesomeness;

import com.parse.ParseObject;

public class FlagObject {
	private String puzzleId, authorId, type, reason;

	FlagObject() {
	}

	FlagObject(final String pi, final String ai, final String t, final String r)
	{
		this.puzzleId = pi;
		this.authorId = ai;
		this.type = t;
		this.reason = r;
	}

	public String getAuthorId() {
		return this.authorId;
	}

	public String getPuzzleId() {
		return this.puzzleId;
	}

	public String getReason() {
		return this.reason;
	}

	public String getType() {
		return this.type;
	}

	public void save()
	{
		final ParseObject po = new ParseObject("FlagObject");
		po.put("puzzleId", this.puzzleId);
		po.put("authorId", this.authorId);
		po.put("type", this.type);
		po.put("reason", this.reason);
		po.saveEventually();
	}

	public void setAuthorId(final String authorId) {
		this.authorId = authorId;
	}

	public void setPuzzleId(final String puzzleId) {
		this.puzzleId = puzzleId;
	}


	public void setReason(final String reason) {
		this.reason = reason;
	}

	public void setType(final String type) {
		this.type = type;
	}
}
