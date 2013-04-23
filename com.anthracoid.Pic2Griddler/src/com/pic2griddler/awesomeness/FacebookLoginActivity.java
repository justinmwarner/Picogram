package com.pic2griddler.awesomeness;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.*;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.facebook.widget.*;
import com.socialize.ActionBarUtils;
import com.socialize.Socialize;
import com.socialize.entity.Entity;

import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;

public class FacebookLoginActivity extends FragmentActivity {
	protected static final String TAG = "FacebookLoginActivity";
	private Button shareButton;
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
	private boolean pendingPublishReauthorization = false;

	public void onCreateView(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Entity entity =
		// Entity.newInstance("http://www.example.com/object/1234",
		// "Example Entity");
		// View actionBarWrapped = ActionBarUtils.showActionBar(this,
		// R.layout.activity_facebook_login, entity);
		// setContentView(actionBarWrapped);
		// Call Socialize in onCreate
		Socialize.onCreate(this, savedInstanceState);

		// Your entity key. May be passed as a Bundle parameter to your activity
		String entityKey = "http://www.getsocialize.com";

		// Create an entity object including a name
		// The Entity object is Serializable, so you could also store the whole
		// object in the Intent
		Entity entity = Entity.newInstance(entityKey, "Socialize");

		// Wrap your existing view with the action bar.
		// your_layout refers to the resource ID of your current layout.
		View actionBarWrapped = ActionBarUtils.showActionBar(this, R.layout.activity_facebook_login, entity);

		// Now set the view for your activity to be the wrapped view.
		setContentView(actionBarWrapped);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Call Socialize in onPause
		Socialize.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Call Socialize in onResume
		Socialize.onResume(this);
	}

	@Override
	protected void onDestroy() {
		// Call Socialize in onDestroy before the activity is destroyed
		Socialize.onDestroy(this);

		super.onDestroy();
	}
}
