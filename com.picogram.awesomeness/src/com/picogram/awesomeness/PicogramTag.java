
package com.picogram.awesomeness;

import android.util.Log;

import com.parse.ParseObject;

public class PicogramTag {
	private static final String TAG = "PicogramTag";
	private String id;
	private String tag;

	PicogramTag() {
		this.id = this.tag = null;
	}

	public PicogramTag(final String tag) {
		this.setTag(tag.toLowerCase());
	}

	String getID() {
		return this.id;
	}

	String getTag() {
		return this.tag;
	}

	public void save() {
		if ((this.id == null) || (this.tag == null))
		{
			try {
				throw new Exception("ID or TAG is null " + (this.id == null) + " " + (this.tag == null));
			} catch (final Exception e)
			{
			}
			return;
		}
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
