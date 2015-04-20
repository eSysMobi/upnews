package mobi.esys.upnewshashtag;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import net.londatiga.android.instagram.Instagram;
import net.londatiga.android.instagram.InstagramUser;

import mobi.esys.consts.ISConsts;

/**
 * Created by Артем on 13.04.2015.
 */
public class InstaLoginActivity extends Activity implements View.OnClickListener {
    private transient Instagram instagram;
    private transient Button instAuthBtn;

    //debug gd key 683764382729-2rqbvb03877h0hv48lakl1t56qo49r1n


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        instagram = new Instagram(InstaLoginActivity.this, ISConsts.INSTAGRAM_CLIENT_ID, ISConsts.INSTAGRAM_CLIENT_SECRET, ISConsts.INSTAGRAM_REDIRECT_URI);

        if ("".equals(instagram.getSession().getAccessToken()) || instagram.getSession().getAccessToken() == null) {
            setContentView(R.layout.activity_instalogin);
            instAuthBtn = (Button) findViewById(R.id.instAuthBtn);
            instAuthBtn.setOnClickListener(this);
        } else {
            finish();
            startActivity(new Intent(InstaLoginActivity.this, InstagramHashTagActivity.class));
        }

    }

    private final transient Instagram.InstagramAuthListener igAuthListener = new Instagram.InstagramAuthListener() {
        @Override
        public void onSuccess(final InstagramUser user) {
            finish();
            startActivity(new Intent(InstaLoginActivity.this, InstagramHashTagActivity.class));
        }

        @Override
        public void onError(final String error) {
            Toast.makeText(InstaLoginActivity.this,
                    "Не удалось авторизоватся в Instagram",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(InstaLoginActivity.this,
                    "Попытка входа была отменена пользоваетелем",
                    Toast.LENGTH_LONG).show();
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
