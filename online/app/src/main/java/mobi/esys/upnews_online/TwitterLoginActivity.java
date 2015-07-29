package mobi.esys.upnews_online;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;
import mobi.esys.upnews_online.constants.DevelopersKeys;

public class TwitterLoginActivity extends Activity {
    private transient TwitterLoginButton loginButton;
    private transient SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(DevelopersKeys.TWITTER_KEY, DevelopersKeys.TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        TwitterSession session =
                Twitter.getSessionManager().getActiveSession();

        if (session == null) {
            setContentView(R.layout.fragment_twitterlogin);

            preferences = getSharedPreferences("unoPref", MODE_PRIVATE);
            String aT = preferences.getString("twAt", "");

            loginButton = (TwitterLoginButton) findViewById(R.id.twLgnBtn);
            loginButton.setCallback(new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("twAT", result.data.getAuthToken().token);
                    editor.apply();
                    startActivity(new Intent(TwitterLoginActivity.this, PlayerActivity.class));
                    finish();
                }

                @Override
                public void failure(TwitterException exception) {

                }
            });
        } else {
            startActivity(new Intent(TwitterLoginActivity.this, PlayerActivity.class));
            finish();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }
}
