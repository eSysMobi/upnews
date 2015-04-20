package mobi.esys.upnewshashtag;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import mobi.esys.consts.ISConsts;
import mobi.esys.tasks.CreateDriveFolderTask;

/**
 * Created by Артем on 14.04.2015.
 */
public class DriveLoginActivity extends Activity implements View.OnClickListener {
    private transient Button gdAuthBtn;
    private transient SharedPreferences prefs;
    private transient GoogleAccountCredential credential;
    private transient static final int REQUEST_ACCOUNT_PICKER = 101;
    private transient static final int REQUEST_AUTHORIZATION = 102;
    private transient static final int REQUEST_AUTH_IF_ERROR = 103;
    private transient boolean isFirstAuth;
    private transient UNHApp mApp;
    private transient Drive drive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (UNHApp) getApplication();
        isFirstAuth = true;
        prefs = mApp.getApplicationContext().getSharedPreferences(ISConsts.PREF_PREFIX, MODE_PRIVATE);


        String accName = prefs.getString("accName", "");
        credential = GoogleAccountCredential.usingOAuth2(
                DriveLoginActivity.this, DriveScopes.DRIVE);

        createFoldersIfNotExist();


        if (accName.isEmpty()) {
            setContentView(R.layout.activity_drivelogin);
            gdAuthBtn = (Button) findViewById(R.id.gdAuthBtn);
            gdAuthBtn.setOnClickListener(this);
        } else {
            credential.setSelectedAccountName(accName);
            drive = getDriveService(credential);
            mApp.registerGoogle(drive);
            createFolderInDriveIfDontExists();


        }
    }


    private Drive getDriveService(GoogleAccountCredential credential) {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(),
                new GsonFactory(), credential).build();
    }


    private void picker() {
        startActivityForResult(credential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER);
    }

    public void auth() {
        startActivityForResult(credential.newChooseAccountIntent(),
                REQUEST_AUTHORIZATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null
                        && data.getExtras() != null) {
                    String accountName = data
                            .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    Log.d("accName", accountName);
                    if (accountName != null) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("accName", accountName);
                        editor.commit();

                        JSONObject props = new JSONObject();
                        try {
                            props.put("gd_account_add", "Google drive account has been added");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        credential.setSelectedAccountName(accountName);
                        drive = getDriveService(credential);
                        mApp.registerGoogle(getDriveService(credential));
                        if (isFirstAuth) {
                            createFolderInDriveIfDontExists();
                            isFirstAuth = false;
                        }
                    }

                }
                break;

            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    createFolderInDriveIfDontExists();
                } else {
                    startActivityForResult(credential.newChooseAccountIntent(),
                            REQUEST_ACCOUNT_PICKER);
                    createFolderInDriveIfDontExists();

                }
                break;

            case REQUEST_AUTH_IF_ERROR:
                if (resultCode == Activity.RESULT_OK) {

                    String accountName = data
                            .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    Log.d("accName", accountName);
                    if (accountName != null) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("accName", accountName);
                        editor.commit();

                        JSONObject props = new JSONObject();
                        try {
                            props.put("gd_account_add", "Google drive account has been added");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        credential.setSelectedAccountName(accountName);
                        drive = getDriveService(credential);
                        mApp.registerGoogle(drive);
                        createFolderInDriveIfDontExists();
                    } else {
                        picker();
                    }
                    break;

                }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void catchUSERException(Intent intent) {
        startActivityForResult(intent, REQUEST_AUTHORIZATION);
    }

    private void createFolderInDriveIfDontExists() {
        CreateDriveFolderTask createDriveFolderTask = new CreateDriveFolderTask(DriveLoginActivity.this, true, mApp, true);
        createDriveFolderTask.execute();
    }

    private void createFoldersIfNotExist() {
        File dir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(ISConsts.DIR_NAME));
        File photoDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(ISConsts.DIR_NAME).concat(ISConsts.PHOTO_DIR_NAME));
        File musicDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath().concat(ISConsts.DIR_NAME).concat(ISConsts.MUSIC_DIR_NAME));
        if (!dir.exists()) {
            dir.mkdir();
        }
        if (!photoDir.exists()) {
            photoDir.mkdir();
        }
        if (!musicDir.exists()) {
            musicDir.mkdir();
        }
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        picker();
    }
}
