package mobi.esys.unl_new_api;


import android.app.Activity;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;

import com.orhanobut.hawk.Hawk;
import com.orhanobut.logger.Logger;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import mobi.esys.unl_new_api.eventbus.LoginFailureEvent;
import mobi.esys.unl_new_api.eventbus.LoginSuccessEvent;
import mobi.esys.unl_new_api.un_api.UNApi;

@EActivity(R.layout.activity_reg)
public class RegActivity extends Activity {
    @ViewById
    EditText loginEdit;
    @ViewById
    EditText pswEdit;
    @ViewById
    Button unLoginBtn;
    @StringRes
    String loginEmpty;

    private transient EventBus bus;


    @AfterViews
    void init() {
        bus = new EventBus();
        initSec();

        String token = Hawk.get("token", "");
        Logger.d(token);

        if (!"".equals(token)) {
            startActivity(new Intent(RegActivity.this, VideoActivity_.class));
        } else {
            if (!bus.isRegistered(this)) {
                bus.register(this);
            }
        }
    }

    public void onEvent(LoginSuccessEvent event) {
        Logger.d("auth success");
        Logger.d(event.toString());
        Hawk.put("token", event.getToken());
        startActivity(new Intent(RegActivity.this, VideoActivity_.class));
    }

    public void onEvent(LoginFailureEvent event) {
        Logger.d("auth failure");
        Logger.d(event.toString());
        Crouton.makeText(RegActivity.this, "upnews login failure", Style.ALERT).show();
    }


    @Click
    void unLoginBtn() {
        if (loginEdit.getEditableText().length() > 0 && pswEdit.getEditableText().length() > 0) {
            auth(loginEdit.getEditableText().toString(), pswEdit.getEditableText().toString());
        } else {
            Crouton.makeText(RegActivity.this, loginEmpty, Style.INFO).show();
        }
    }

    @Background
    void auth(String login, String password) {
        UNApi.setCurrentContext(RegActivity.this, bus);
        UNApi.auth(login, password);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
        Crouton.cancelAllCroutons();
    }

    private void initSec() {
        Hawk.init(RegActivity.this, "upnews232507");
    }


}
