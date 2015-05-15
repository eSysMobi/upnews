package mobi.esys.upnewshashtag;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import net.londatiga.android.instagram.Instagram;

import java.util.concurrent.ExecutionException;

import mobi.esys.consts.ISConsts;
import mobi.esys.tasks.CheckInstaTagTask;

/**
 * Created by Артем on 17.04.2015.
 */
public class InstagramHashTagActivity extends Activity implements View.OnClickListener {
    private transient EditText hashTagEdit;
    private transient Button enterHashBtn;
    private transient SharedPreferences preferences;
    private transient UNHApp mApp;
    private transient Instagram instagram;
    private transient EasyTracker easyTracker;

    private static final int MIN_EDITABLE_LENGTH = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instagram_hash_tag);

        easyTracker = EasyTracker.getInstance(InstagramHashTagActivity.this);

        hashTagEdit = (EditText) findViewById(R.id.instHashTagEdit);
        enterHashBtn = (Button) findViewById(R.id.enterHashTagBtn);

        enterHashBtn.setOnClickListener(InstagramHashTagActivity.this);

        mApp = (UNHApp) getApplicationContext();

        instagram = new Instagram(InstagramHashTagActivity.this,
                ISConsts.instagramconsts.instagram_client_id, ISConsts.instagramconsts.instagram_client_secret,
                ISConsts.instagramconsts.instagram_redirect_uri);

        preferences = getSharedPreferences(ISConsts.globals.pref_prefix, MODE_PRIVATE);
        String hashTag = preferences.getString(ISConsts.prefstags.instagram_hashtag, "");
        if (!hashTag.isEmpty()) {
            hashTagEdit.setText(hashTag);
        }


        if (hashTagEdit.getEditableText().length() > MIN_EDITABLE_LENGTH) {
            hashTagEdit.setSelection(hashTagEdit.getEditableText().length() - 1);
        } else {
            hashTagEdit.setSelection(1);
        }

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
                    StringBuilder sb = new StringBuilder();
                    sb.append("#").append(s.toString());
                    String unSpaceStr = sb.toString().replaceAll(" ",
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

                if (EditorInfo.IME_ACTION_DONE == actionId || EditorInfo.IME_ACTION_UNSPECIFIED == actionId) {
                    checkTagAndGo();
                    handled = true;
                }

                return handled;
            }
        });
    }


    @Override
    public void onClick(View v) {
        checkTagAndGo();
    }

    public void checkTagAndGo() {
        if (!hashTagEdit.getEditableText().toString().isEmpty()
                && hashTagEdit.getEditableText().toString().length() >= MIN_EDITABLE_LENGTH) {
            CheckInstaTagTask checkInstaTagTask = new CheckInstaTagTask(hashTagEdit.getEditableText().toString(), mApp);
            checkInstaTagTask.execute(instagram.getSession().getAccessToken());
            try {
                if (checkInstaTagTask.get()) {
                    startActivity(new Intent(InstagramHashTagActivity.this, TwitterLoginActivity.class));
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(ISConsts.prefstags.instagram_hashtag, hashTagEdit.getEditableText().toString());
                    editor.commit();
                    easyTracker.send(MapBuilder.createEvent("input_hashtag",
                            "instagram_input_hashtag", hashTagEdit.getEditableText().toString(), null).build());
                } else {
                    Toast.makeText(InstagramHashTagActivity.this, "Sorry but this hashtag don't allowed", Toast.LENGTH_SHORT).show();
                    easyTracker.send(MapBuilder.createEvent("input_hashtag",
                            "instagram_input_hashtag", "hashtag_dont_allowed", null).build());

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(InstagramHashTagActivity.this, getString(R.string.instagram_hashtag_required_message), Toast.LENGTH_SHORT).show();
        }
    }
}
