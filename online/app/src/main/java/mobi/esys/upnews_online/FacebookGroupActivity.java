package mobi.esys.upnews_online;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class FacebookGroupActivity extends Activity {
    private transient SharedPreferences preferences;
    private transient EditText fbGroupIDEdit;
    private transient EasyTracker easyTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FacebookSdk.sdkInitialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_facebookgroup);

        easyTracker = EasyTracker.getInstance(FacebookGroupActivity.this);

        easyTracker.send(MapBuilder.createEvent("auth",
                "fb_group_input", "fb_group_input", null).build());

        preferences = getSharedPreferences("unoPref", MODE_PRIVATE);

        String groupID = preferences.getString("fbGroupID", "");

        fbGroupIDEdit = (EditText) findViewById(R.id.fbGroupEdit);

        if (!groupID.isEmpty()) {
            fbGroupIDEdit.setText(groupID);
        }

        Button fbGroupIDButtonEdit = (Button) findViewById(R.id.enterfbGroupBtn);


        fbGroupIDEdit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                boolean handled = false;

                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    goNext();
                    handled = true;
                }
                return handled;
            }
        });

        fbGroupIDButtonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });
    }

    private void goNext() {
        if (!fbGroupIDEdit.getText().toString().isEmpty() && !fbGroupIDEdit.getText().toString().equals("")) {
            checkAndGo(fbGroupIDEdit.getText().toString());
        } else {
            Toast.makeText(this, "Input facebook group id", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveGroupID(String groupID) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("fbGroupID", groupID);
        editor.apply();
    }

    private void checkAndGo(String groupID) {
        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + groupID + "/videos",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        Log.d("resp", response.toString());
                        if (!response.toString().contains("responseCode: 404")) {
                            saveGroupID(fbGroupIDEdit.getText().toString());
                            startActivity(new Intent(FacebookGroupActivity.this, InstagramLoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(FacebookGroupActivity.this, "This group id don't existed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,description,source");

        request.setParameters(parameters);
        request.executeAsync();

    }
}
