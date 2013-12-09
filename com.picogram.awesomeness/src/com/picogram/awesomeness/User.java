
package com.picogram.awesomeness;

import com.stackmob.sdk.model.StackMobUser;

public class User extends StackMobUser {
	protected User() {
		super(User.class);
	}

	protected User(final String username, final String password) {
		super(User.class, username, password);
	}

}
