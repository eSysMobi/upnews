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

import mobi.esys.consts.ISConsts;

/**
 * Created by Артем on 17.04.2015.
 */
public class TweeterHashTagActivity extends Activity implements View.OnClickListener {
    private transient EditText hashTagEdit;
    private transient Button enterHashBtn;
    private transient SharedPreferences preferences;
    private transient EasyTracker easyTracker;

    private static final int MIN_EDITABLE_LENGTH = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_hashtag);

        easyTracker = EasyTracker.getInstance(TweeterHashTagActivity.this);

        hashTagEdit = (EditText) findViewById(R.id.twHashTagEdit);
        enterHashBtn = (Button) findViewById(R.id.enterHashTagBtn);

        enterHashBtn.setOnClickListener(TweeterHashTagActivity.this);

        preferences = getSharedPreferences(ISConsts.globals.pref_prefix, MODE_PRIVATE);
        String hashTag = preferences.getString(ISConsts.prefstags.twitter_hashtag, "");
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
                    if (!hashTagEdit.getEditableText().toString().isEmpty()
                            && hashTagEdit.getEditableText().toString().length() >= MIN_EDITABLE_LENGTH) {
                        startActivity(new Intent(TweeterHashTagActivity.this, SliderActivity.class));
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(ISConsts.prefstags.twitter_hashtag, hashTagEdit.getEditableText().toString());
                        editor.commit();
                        easyTracker.send(MapBuilder.createEvent("input_hashtag",
                                "twitter_input_hashtag", hashTagEdit.getEditableText().toString(), null).build());
                    } else {
                        Toast.makeText(TweeterHashTagActivity.this, getString(R.string.twitter_hashtag_required_message), Toast.LENGTH_SHORT).show();
                    }
                    handled = true;
                }

                return handled;
            }
        });
    }


    @Override
    public void onClick(View v) {
        if (!hashTagEdit.getEditableText().toString().isEmpty()
                && hashTagEdit.getEditableText().toString().length() >= MIN_EDITABLE_LENGTH) {
            startActivity(new Intent(TweeterHashTagActivity.this, SliderActivity.class));
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(ISConsts.prefstags.twitter_hashtag, hashTagEdit.getEditableText().toString());
            editor.commit();
        } else {
            Toast.makeText(TweeterHashTagActivity.this, getString(R.string.twitter_hashtag_required_message), Toast.LENGTH_SHORT).show();
        }
    }
}

