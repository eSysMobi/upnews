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

import mobi.esys.consts.ISConsts;

/**
 * Created by Артем on 17.04.2015.
 */
public class InstagramHashTagActivity extends Activity implements View.OnClickListener {
    private transient EditText hashTagEdit;
    private transient Button enterHashBtn;
    private transient SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instagram_hash_tag);

        hashTagEdit = (EditText) findViewById(R.id.instHashTagEdit);
        enterHashBtn = (Button) findViewById(R.id.enterHashTagBtn);

        enterHashBtn.setOnClickListener(InstagramHashTagActivity.this);

        preferences = getSharedPreferences(ISConsts.PREF_PREFIX, MODE_PRIVATE);
        String hashTag = preferences.getString("igHashTag", "");
        if (!"".equals(hashTag)) {
            hashTagEdit.setText(hashTag);
        }


        if (hashTagEdit.getEditableText().length() > 2) {
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

                // Some phones disregard the IME setting option in the xml, instead
                // they send IME_ACTION_UNSPECIFIED so we need to catch that
                if (EditorInfo.IME_ACTION_DONE == actionId || EditorInfo.IME_ACTION_UNSPECIFIED == actionId) {
                    if (!"".equals(hashTagEdit.getEditableText().toString())
                            && hashTagEdit.getEditableText().toString().length() >= 2) {
                        startActivity(new Intent(InstagramHashTagActivity.this, TwitterLoginActivity.class));
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("igHashTag", hashTagEdit.getEditableText().toString());
                        editor.commit();
                    } else {

                        Toast.makeText(InstagramHashTagActivity.this, "Введите тэг для поиска изображений в Instagram", Toast.LENGTH_SHORT).show();
                    }

                    handled = true;
                }

                return handled;
            }
        });
    }


    @Override
    public void onClick(View v) {
        if (!"".equals(hashTagEdit.getEditableText().toString())
                && hashTagEdit.getEditableText().toString().length() >= 2) {
            startActivity(new Intent(InstagramHashTagActivity.this, TwitterLoginActivity.class));
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("igHashTag", hashTagEdit.getEditableText().toString());
            editor.commit();
        } else {

            Toast.makeText(InstagramHashTagActivity.this, "Введите тэг для поиска изображений в Instagram", Toast.LENGTH_SHORT).show();
        }
    }
}
