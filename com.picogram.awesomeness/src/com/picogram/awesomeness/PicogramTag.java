
package com.picogram.awesomeness;

import com.parse.ParseObject;


public class PicogramTag  {
	private String id;
	private String tag;

	public PicogramTag(final String tag) {
		this.setTag(tag.toLowerCase());
	}

	String getID() {
		return this.id;
	}

	String getTag() {
		return this.tag;
	}

	public void save(){
		final ParseObject po = new ParseObject("PicogramTag");
		po.put("puzzleId", this.id);
		po.put("tag", this.tag);
		po.saveEventually();
	}

	void setID(final String id) {
		this.id = id;
	}

	void setTag(final String tag) {
		this.tag = tag;
	}
}
