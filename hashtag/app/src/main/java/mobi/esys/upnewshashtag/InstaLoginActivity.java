package mobi.esys.upnewshashtag;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import net.londatiga.android.instagram.Instagram;
import net.londatiga.android.instagram.InstagramUser;

import mobi.esys.consts.ISConsts;


/**
 * Created by Артем on 13.04.2015.
 */
public class InstaLoginActivity extends Activity implements View.OnClickListener {
    private transient Instagram instagram;
    private transient Button instAuthBtn;
    private transient EasyTracker easyTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        instagram = new Instagram(InstaLoginActivity.this, ISConsts.instagramconsts.instagram_client_id, ISConsts.instagramconsts.instagram_client_secret, ISConsts.instagramconsts.instagram_redirect_uri);
        easyTracker = EasyTracker.getInstance(InstaLoginActivity.this);


        if (instagram.getSession().getAccessToken().isEmpty() || instagram.getSession().getAccessToken() == null) {
            setContentView(R.layout.activity_instalogin);
            instAuthBtn = (Button) findViewById(R.id.instAuthBtn);
            instAuthBtn.setOnClickListener(this);
        } else {
            finish();
            startActivity(new Intent(InstaLoginActivity.this, InstagramHashTagActivity.class));
            easyTracker.send(MapBuilder.createEvent("auth",
                    "instagram_auth", "go_to_hashtag_input", null).build());
        }

    }

    private final transient Instagram.InstagramAuthListener igAuthListener = new Instagram.InstagramAuthListener() {
        @Override
        public void onSuccess(final InstagramUser user) {
            finish();
            startActivity(new Intent(InstaLoginActivity.this, InstagramHashTagActivity.class));
            easyTracker.send(MapBuilder.createEvent("auth",
                    "instagram_auth", "success", null).build());
        }

        @Override
        public void onError(final String error) {
            Toast.makeText(InstaLoginActivity.this,
                    getString(R.string.instagram_auth_failure_message),
                    Toast.LENGTH_LONG).show();
            easyTracker.send(MapBuilder.createEvent("auth",
                    "instagram_auth", "error", null).build());
        }

        @Override
        public void onCancel() {
            Toast.makeText(InstaLoginActivity.this,
                    getString(R.string.instagram_auth_cancel_message),
                    Toast.LENGTH_LONG).show();
            easyTracker.send(MapBuilder.createEvent("auth",
                    "instagram_auth", "cancel", null).build());
        }
    };


    @Override
    public void onClick(View v) {
        instagram.authorize(igAuthListener);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
