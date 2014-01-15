package com.picogram.awesomeness;

import com.stackmob.sdk.model.StackMobModel;

public class GriddlerTag extends StackMobModel {
	String tag;

	public GriddlerTag(final String tag) {
		super(GriddlerTag.class);
		this.tag = tag.toLowerCase();
	}
}
