
package com.picogram.awesomeness;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.crittercism.app.Crittercism;
import com.github.espiandev.showcaseview.ShowcaseView;
import com.google.analytics.tracking.android.EasyTracker;
import com.picogram.awesomeness.TouchImageView.WinnerListener;
import com.socialize.api.SocializeSession;
import com.socialize.api.action.share.SocialNetworkShareListener;
import com.socialize.entity.Entity;
import com.socialize.error.SocializeException;
import com.socialize.listener.SocializeAuthListener;
import com.socialize.networks.PostData;
import com.socialize.networks.SocialNetwork;
import com.socialize.networks.facebook.FacebookUtils;
import com.socialize.networks.twitter.TwitterUtils;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import org.json.JSONObject;

public class AdvancedGameActivity extends Activity implements OnClickListener, WinnerListener,
        ShowcaseView.OnShowcaseEventListener {
    private static final String TAG = "AdvancedGameActivity";
    TouchImageView tiv;
    ShowcaseView sv;
    Handler handle = new Handler();
    int tutorialStep = 0;
    private static SQLiteGriddlerAdapter sql;
    Handler handler = new Handler();

    private void doFacebookStuff() {
        if (FacebookUtils.isLinked(this)) {
            final String name = this.getIntent().getExtras().getString("name");
            final Entity entity = Entity.newInstance("http://www.google.com", name);

            // The "this" argument refers to the current Activity
            FacebookUtils.postEntity(this, entity, "I just beat " + name + " on Picogram!",
                    new SocialNetworkShareListener() {

                        @Override
                        public void onAfterPost(final Activity parent,
                                final SocialNetwork socialNetwork,
                                final JSONObject responseObject) {
                            // Called after the post returned from Facebook.
                            // responseObject contains the raw JSON response
                            // from
                            // Facebook.
                            Crouton.makeText(parent, "Facebook post successful!", Style.ALERT)
                                    .show();
                            FlurryAgent.logEvent("FacebookSuccess");
                            AdvancedGameActivity.this.returnIntent();
                        }

                        @Override
                        public boolean onBeforePost(final Activity parent,
                                final SocialNetwork socialNetwork,
                                final PostData postData) {
                            // Called just prior to the post.
                            // postData contains the dictionary (map) of data to
                            // be
                            // posted.
                            // You can change this here to customize the post.
                            // Return true to prevent the post from occurring.
                            return false;
                        }

                        @Override
                        public void onCancel() {
                            // The user cancelled the operation.
                            AdvancedGameActivity.this.returnIntent();
                        }

                        @Override
                        public void onNetworkError(final Activity context,
                                final SocialNetwork network,
                                final Exception error) {
                            // Handle error
                            Crouton.makeText(context, "Couldn't post to Facebook.", Style.ALERT)
                                    .show();
                            FlurryAgent.logEvent("FacebookFail");
                            Crittercism.logHandledException(error);
                            AdvancedGameActivity.this.returnIntent();
                        }
                    });
        } else {
            // Request write access
            FacebookUtils.link(this, new SocializeAuthListener() {

                public void onAuthFail(final SocializeException error) {
                    Crittercism.logHandledException(error);
                    AdvancedGameActivity.this.returnIntent();
                }

                public void onAuthSuccess(final SocializeSession session) {
                    // Perform direct Facebook operation.
                    AdvancedGameActivity.this.doFacebookStuff();
                }

                public void onCancel() {
                    // The user cancelled the operation.
                    AdvancedGameActivity.this.returnIntent();
                }

                public void onError(final SocializeException error) {
                    Crittercism.logHandledException(error);
                    AdvancedGameActivity.this.returnIntent();
                }
            }, "publish_stream");
        }
    }

    private void doTwitterStuff() {
        final Activity a = this;
        if (TwitterUtils.isLinked(this)) {
            final String name = this.getIntent().getExtras().getString("name");
            final Entity entity = Entity.newInstance("http://www.google.com", name);

            TwitterUtils.tweetEntity(this, entity, "I just beat " + name + " on Picogram!",
                    new SocialNetworkShareListener() {

                        @Override
                        public void onAfterPost(final Activity parent,
                                final SocialNetwork socialNetwork,
                                final JSONObject responseObject) {
                            // Called after the post returned from Twitter.
                            // responseObject contains the raw JSON response
                            // from Twitter.
                            Crouton.makeText(parent, "Successfully posted to Twitter.", Style.ALERT)
                                    .show();
                        }

                        @Override
                        public boolean onBeforePost(final Activity parent,
                                final SocialNetwork socialNetwork,
                                final PostData postData) {
                            // Called just prior to the post. postData contains
                            // the dictionary (map) of data to be posted.
                            // You can change this here to customize the post.
                            // Return true to prevent the post from occurring.
                            return false;
                        }

                        @Override
                        public void onCancel() {
                            // The user cancelled the operation.
                        }

                        @Override
                        public void onNetworkError(final Activity context,
                                final SocialNetwork network,
                                final Exception error) {
                            // Handle error
                            Crouton.makeText(context, "Couldn't post to Twitter", Style.ALERT)
                                    .show();
                            Crittercism.logHandledException(error);
                        }
                    });
        } else {
            // The "this" argument refers to the current Activity
            TwitterUtils.link(this, new SocializeAuthListener() {

                public void onAuthFail(final SocializeException error) {
                    Crittercism.logHandledException(error);
                }

                public void onAuthSuccess(final SocializeSession session) {
                    // User was authed.
                    AdvancedGameActivity.this.doTwitterStuff();
                }

                public void onCancel() {
                    // The user cancelled the operation.
                }

                public void onError(final SocializeException error) {
                    Crittercism.logHandledException(error);
                }
            });

        }
    }

    @Override
    public void onBackPressed() {
        super.onPause();
        final Intent returnIntent = new Intent();
        returnIntent.putExtra("current", this.tiv.gCurrent);
        returnIntent.putExtra("status", "0");
        returnIntent.putExtra("ID", this.tiv.gSolution.hashCode() + "");
        this.setResult(2, returnIntent);
        this.finish();
    }

    public void onClick(final View v) {
        if (v.getId() == R.id.bToolboxHand) {
            this.tiv.isGameplay = false;
        } else if (v.getId() == R.id.bToolboxBlack) {
            this.tiv.colorCharacter = '1';
            this.tiv.isGameplay = true;
        } else if (v.getId() == R.id.bToolboxWhite) {
            this.tiv.colorCharacter = '0';
            this.tiv.isGameplay = true;
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.activity_advanced_game);
        final Button bHand = (Button) this.findViewById(R.id.bToolboxHand);
        final Button bWhite = (Button) this.findViewById(R.id.bToolboxWhite);
        final Button bBlack = (Button) this.findViewById(R.id.bToolboxBlack);
        bHand.setOnClickListener(this);
        bWhite.setOnClickListener(this);
        bBlack.setOnClickListener(this);
        this.tiv = (TouchImageView) this.findViewById(R.id.tivGame);
        this.tiv.setWinListener(this);
        this.tiv.setGriddlerInfo(this.getIntent().getExtras());
        final String name = this.getIntent().getExtras().getString("name");
        final String c = this.getIntent().getExtras().getString("current");
        final String s = this.getIntent().getExtras().getString("solution");
        FlurryAgent.logEvent("UserPlayingGame");
        if (name != null) {
            EasyTracker.getTracker().trackEvent("Game", "GriddlerName", name, (long) 1);
            if (name.equals("Tutorial")) {
                // We're in a tutorial.
                if (!c.equals(s)) {
                    this.showStepOne();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.getMenuInflater().inflate(R.menu.activity_advanced_game, menu);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        EasyTracker.getInstance().activityStop(this); // Add this method.
        sql.updateCurrentGriddler(this.tiv.gSolution.hashCode() + "", "0", this.tiv.gCurrent);
        sql.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        EasyTracker.getInstance().activityStart(this); // Add this method.
        sql = new SQLiteGriddlerAdapter(this.getApplicationContext(), "Griddlers", null, 1);
    }

    public void onShowcaseViewHide(final ShowcaseView showcaseView) {

        if (this.tutorialStep == 0) {
            this.tutorialStep++;
            this.showStepTwo();
        } else if (this.tutorialStep == 1) {
            this.tutorialStep++;
            this.showStepThree();
        } else if (this.tutorialStep == 2) {
            // This is our final step, and we must wait until the top row is
            // filled to continue. Run a thread and wait until the game is
            // either finished, or they complete the top row to show the
            // next step.
            new Thread(new Runnable() {

                public void run() {
                    while (true) {
                        if (AdvancedGameActivity.this.tiv.gCurrent.startsWith("1111")) {
                            AdvancedGameActivity.this.tutorialStep++;
                            AdvancedGameActivity.this.handle.post(new Runnable() {

                                public void run() {
                                    AdvancedGameActivity.this.showStepFour();
                                }

                            });
                            break;
                        }
                    }
                }

            }).start();
        } else if (this.tutorialStep == 3) {
        }
    }

    public void onShowcaseViewShow(final ShowcaseView showcaseView) {
        // TODO Auto-generated method stub

    }

    private void returnIntent() {

        final Intent returnIntent2 = new Intent();
        returnIntent2.putExtra("current", this.tiv.gCurrent);
        returnIntent2.putExtra("status", "1");
        returnIntent2.putExtra("ID", this.tiv.gSolution.hashCode() + "");
        this.setResult(2, returnIntent2);
        this.finish();
    }

    private void showStepFour() {
        final ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
        co.hideOnClickOutside = true;
        this.sv = ShowcaseView
                .insertShowcaseView(
                        R.id.tivGame,
                        this,
                        "Finish her!",
                        "Good, now you just gotta finish the two. If we check the side hints, we can see 1 1.  This means that we have one black, with some white space between the next.  Use the top hints to figure out where the remainding pieces go.",
                        co);
        this.sv.setOnShowcaseEventListener(this);

    }

    private void showStepOne() {
        final ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
        co.hideOnClickOutside = true;
        this.sv = ShowcaseView
                .insertShowcaseView(
                        R.id.llToolbox,
                        this,
                        "Movement and Brush Color",
                        "Here are your tools to use during your griddler-ing.  The Move is used to move and zoom.  You can zoom in via-pinching, and from there, you may slide around.\n\nAs you may know, Griddlers use colors to draw pictures.  This is also the location of your brushes you may use.",
                        co);
        this.sv.setOnShowcaseEventListener(this);

    }

    private void showStepThree() {

        final ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
        co.hideOnClickOutside = true;
        this.sv = ShowcaseView
                .insertShowcaseView(
                        R.id.tivGame,
                        this,
                        "First Row",
                        "As you can see, the board is a 4X4.  If we see the numbers on the side, we see two rows have 4.  This means, by deduction, the whole row must be filled.  So let's finish filling up the first row. \n\nRemember, Click the black up top to be able to draw.",
                        co);
        this.sv.setOnShowcaseEventListener(this);

    }

    private void showStepTwo() {

        final ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
        co.hideOnClickOutside = true;
        this.sv = ShowcaseView
                .insertShowcaseView(
                        R.id.tivGame,
                        this,
                        "Game Board",
                        "This here is the heart and soul of the Griddler game.  This is your game board.\n\nAs you can see, the side and top numbers are your hints. You can use these to figure out this board.  If you already know how to win, just play.  If not, then follow the steps.",
                        co);
        this.sv.setOnShowcaseEventListener(this);

    }

    public void win() {

        final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Facebook.
                        AdvancedGameActivity.this.handler.post(new Runnable() {

                            public void run() {
                                AdvancedGameActivity.this.doFacebookStuff();
                            }
                        });
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // Nothing.
                        AdvancedGameActivity.this.returnIntent();
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        // Twitter.
                        AdvancedGameActivity.this.handler.post(new Runnable() {

                            public void run() {
                                AdvancedGameActivity.this.doTwitterStuff();
                            }
                        });
                        break;
                }

            }

        };

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You won! Share?").setPositiveButton("Facebook", dialogClickListener)
                .setNeutralButton("Twitter", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
        // Leaked window, it quits too fast.
    }
}
