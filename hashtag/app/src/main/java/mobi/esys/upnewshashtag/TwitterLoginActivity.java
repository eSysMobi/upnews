package mobi.esys.upnewshashtag;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import io.fabric.sdk.android.Fabric;
import mobi.esys.consts.ISConsts;

/**
 * Created by Артем on 14.04.2015.
 */
public class TwitterLoginActivity extends Activity implements View.OnClickListener {
    private transient Button twitterAuthBtn;
    private transient Button skipBtn;
    private transient TwitterAuthClient client;
    private transient SharedPreferences prefs;
    private transient EasyTracker easyTracker;

    public TwitterLoginActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(ISConsts.twitterconsts.twitter_key, ISConsts.twitterconsts.twitter_secret);
        Fabric.with(this, new Twitter(authConfig));
        prefs = getSharedPreferences(ISConsts.globals.pref_prefix, MODE_PRIVATE);

        easyTracker = EasyTracker.getInstance(TwitterLoginActivity.this);


        TwitterSession session =
                Twitter.getSessionManager().getActiveSession();

        if (session == null) {
            setContentView(R.layout.activity_twitterlogin);


            twitterAuthBtn = (Button) findViewById(R.id.twitterAuthBtn);
            twitterAuthBtn.setOnClickListener(TwitterLoginActivity.this);
            skipBtn = (Button) findViewById(R.id.skipBtn);
            skipBtn.setOnClickListener(TwitterLoginActivity.this);
        } else {
            startActivity(new Intent(TwitterLoginActivity.this, TweeterHashTagActivity.class));
            easyTracker.send(MapBuilder.createEvent("auth",
                    "twitter_auth", "go_to_hashtag_input", null).build());
        }

    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.twitterAuthBtn) {
            client = new TwitterAuthClient();
            client.authorize(TwitterLoginActivity.this, new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> twitterSessionResult) {
                    finish();
                    startActivity(new Intent(TwitterLoginActivity.this, TweeterHashTagActivity.class));
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(ISConsts.prefstags.twitter_allow, true);
                    editor.commit();
                    easyTracker.send(MapBuilder.createEvent("auth",
                            "twitter_auth", "success", null).build());
                }

                @Override
                public void failure(TwitterException e) {
                    Toast.makeText(TwitterLoginActivity.this, getString(R.string.twitter_auth_failure_message).concat(e.getLocalizedMessage().toString()), Toast.LENGTH_SHORT).show();
                    easyTracker.send(MapBuilder.createEvent("auth",
                            "twitter_auth", "failure", null).build());
                }
            });
        } else {
            finish();
            startActivity(new Intent(TwitterLoginActivity.this, SliderActivity.class));
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(ISConsts.prefstags.twitter_allow, false);
            editor.commit();
            easyTracker.send(MapBuilder.createEvent("auth",
                    "twitter_auth", "skip", null).build());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        client.onActivityResult(requestCode, resultCode, data);
    }
}
