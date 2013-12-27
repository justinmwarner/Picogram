package com.picogram.awesomeness;

import com.stackmob.sdk.model.StackMobModel;

public class PicogramTag extends StackMobModel {
	String tag;

	public PicogramTag(final String tag) {
		super(PicogramTag.class);
		this.tag = tag.toLowerCase();
	}
}
