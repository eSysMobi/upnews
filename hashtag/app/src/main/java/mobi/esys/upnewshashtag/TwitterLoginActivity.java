package mobi.esys.upnewshashtag;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

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
    private transient TwitterAuthClient client;

    public TwitterLoginActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(ISConsts.TWITTER_KEY, ISConsts.TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));


        TwitterSession session =
                Twitter.getSessionManager().getActiveSession();

        if (session == null) {
            setContentView(R.layout.activity_twitterlogin);
            twitterAuthBtn = (Button) findViewById(R.id.twitterAuthBtn);
            twitterAuthBtn.setOnClickListener(TwitterLoginActivity.this);
        } else {
            startActivity(new Intent(TwitterLoginActivity.this, TweeterHashTagActivity.class));
        }

    }


    @Override
    public void onClick(View v) {
        client = new TwitterAuthClient();
        client.authorize(TwitterLoginActivity.this, new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
                startActivity(new Intent(TwitterLoginActivity.this, TweeterHashTagActivity.class));
            }

            @Override
            public void failure(TwitterException e) {
                Toast.makeText(TwitterLoginActivity.this, "Не удалось в войти в вашу учетную запись в Twitter", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        client.onActivityResult(requestCode, resultCode, data);
    }
}
