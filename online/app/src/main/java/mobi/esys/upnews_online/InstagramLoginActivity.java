package mobi.esys.upnews_online;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import net.londatiga.android.instagram.Instagram;
import net.londatiga.android.instagram.InstagramUser;

import mobi.esys.upnews_online.constants.DevelopersKeys;

public class InstagramLoginActivity extends Activity implements View.OnClickListener {
    private transient Instagram instagram;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        instagram = new Instagram(this, DevelopersKeys.INSTAGRAM_CLIENT_ID,
                DevelopersKeys.INSTAGRAM_CLIENT_SECRET,
                DevelopersKeys.INSTAGRAM_REDIRECT_URI);

        if (instagram.getSession().getAccessToken().isEmpty() || instagram.getSession().getAccessToken() == null) {
            setContentView(R.layout.fragment_instagramlogin);
            Button igLoginBtn = (Button) findViewById(R.id.igLgBtn);
            igLoginBtn.setOnClickListener(this);
        } else {
            startActivity(new Intent(InstagramLoginActivity.this, InstagramHashtagActivity.class));
            finish();
        }

    }


    @Override
    public void onClick(View v) {
        instagram.authorize(igAuthListener);
    }

    private final transient Instagram.InstagramAuthListener igAuthListener = new Instagram.InstagramAuthListener() {
        @Override
        public void onSuccess(final InstagramUser user) {
            startActivity(new Intent(InstagramLoginActivity.this, InstagramHashtagActivity.class));
            finish();
        }

        @Override
        public void onError(final String error) {

        }

        @Override
        public void onCancel() {
        }
    };
}
