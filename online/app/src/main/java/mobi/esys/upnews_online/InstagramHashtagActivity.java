package mobi.esys.upnews_online;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.londatiga.android.instagram.Instagram;

import java.util.concurrent.ExecutionException;

import mobi.esys.upnews_online.constants.DevelopersKeys;
import mobi.esys.upnews_online.instagram.CheckInstaTagTask;

public class InstagramHashtagActivity extends Activity {
    private transient UpnewsOnlineApp mApp;
    private transient Instagram instagram;
    private transient EditText hashTagEdit;
    private transient SharedPreferences preferences;

    private static final int MIN_EDITABLE_LENGTH = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.fragment_instagramhashtag);

        mApp = (UpnewsOnlineApp) getApplicationContext();
        instagram = new Instagram(this, DevelopersKeys.INSTAGRAM_CLIENT_ID,
                DevelopersKeys.INSTAGRAM_CLIENT_SECRET,
                DevelopersKeys.INSTAGRAM_REDIRECT_URI);

        hashTagEdit = (EditText) findViewById(R.id.instHashTagEdit);
        Button enterHashBtn = (Button) findViewById(R.id.enterHashTagBtn);

        preferences = getSharedPreferences("unoPref", MODE_PRIVATE);
        String hashTag = preferences.getString("instHashTag", "");
        if (!hashTag.isEmpty()) {
            hashTagEdit.setText("#" + hashTag);
        }

        if (hashTagEdit.getEditableText().length() > MIN_EDITABLE_LENGTH) {
            hashTagEdit.setSelection(hashTagEdit.getEditableText().length() - 1);
        } else {
            hashTagEdit.setSelection(1);
        }

        enterHashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkTagAndGo();
            }
        });

        hashTagEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().startsWith("#")) {
                    String unSpaceStr = ("#" + s.toString()).replaceAll(" ",
                            "");
                    hashTagEdit.setText(unSpaceStr);
                }

                if (s.toString().length() == 1) {
                    hashTagEdit.setSelection(1);
                }

            }
        });

        hashTagEdit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                boolean handled = false;

                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    checkTagAndGo();
                    handled = true;
                }

                return handled;
            }
        });

    }

    public void checkTagAndGo() {
        if (!hashTagEdit.getEditableText().toString().isEmpty()
                && hashTagEdit.getEditableText().toString().length() >= MIN_EDITABLE_LENGTH) {
            CheckInstaTagTask checkInstaTagTask = new CheckInstaTagTask(hashTagEdit.getEditableText().toString(), mApp);
            checkInstaTagTask.execute(instagram.getSession().getAccessToken());
            try {
                if (checkInstaTagTask.get()) {

                    String hashtag = hashTagEdit.getEditableText().toString().replace("#", "");
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("instHashTag", hashtag);
                    editor.apply();
                    startActivity(new Intent(InstagramHashtagActivity.this,
                            TwitterLoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Sorry but this hashtag don't allowed", Toast.LENGTH_SHORT).show();

                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Input Instagram hashtag", Toast.LENGTH_SHORT).show();
        }
    }
}
