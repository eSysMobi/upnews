package mobi.esys.upnews_online;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;


public class LoginActivity extends Activity {
    private transient CallbackManager callbackManager;
    private transient SharedPreferences prefs;
    private EasyTracker easyTracker;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        prefs = getSharedPreferences("unoPref", MODE_PRIVATE);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        easyTracker = EasyTracker.getInstance(LoginActivity.this);

        easyTracker.send(MapBuilder.createEvent("auth",
                "fb_auth", "go_to_fb_group_input", null).build());

        String fbAT = prefs.getString("fbAT", "");
        Log.d("fbAt", fbAT);

        if (fbAT.equals("")) {
            setContentView(R.layout.fragment_facebooklogin);

            LoginButton loginBtn = (LoginButton) findViewById(R.id.fbLgBtn);

            loginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("fbAT", loginResult.getAccessToken().getToken());
                    editor.apply();
                    startActivity(new Intent(LoginActivity.this, FacebookGroupActivity.class));
                    finish();
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onError(FacebookException exception) {

                }
            });
        } else {
            startActivity(new Intent(LoginActivity.this, FacebookGroupActivity.class));
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
