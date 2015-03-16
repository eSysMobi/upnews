package mobi.esys.upnewslite;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
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

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import mobi.esys.constants.UNLConsts;
import mobi.esys.tasks.CreateDriveFolderTask;


public class DriveAuthActivity extends Activity implements View.OnClickListener {
    private transient Button gdAuthBtn;
    private transient SharedPreferences prefs;
    private transient GoogleAccountCredential credential;
    private transient static final int REQUEST_ACCOUNT_PICKER = 101;
    private transient static final int REQUEST_AUTHORIZATION = 102;
    private transient static final int REQUEST_AUTH_IF_ERROR = 103;
    private transient boolean isFirstAuth;
    private transient UNLApp mApp;
    private transient Drive drive;
    private transient long installDateMillis;
    private transient Date installDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (UNLApp) getApplication();
        isFirstAuth = true;
        prefs = mApp.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, MODE_PRIVATE);

        installDateMillis = prefs.getLong("installedTime", 0);
        Calendar today = Calendar.getInstance();

        if (installDateMillis == 0) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong("installedTime", today.getTimeInMillis());
            editor.commit();
            installDate = today.getTime();
        } else {
            installDate = new Date(installDateMillis);
        }


        DateTime initDT = new DateTime(installDate);
        DateTime now = new DateTime();
        Period sinceInstall = new Period(initDT, now, PeriodType.dayTime()).normalizedStandard(PeriodType.dayTime());

        Log.d("time", sinceInstall.toString());


        int daysBetween = Math.abs(sinceInstall.getDays());

        String accName = prefs.getString("accName", "");
        credential = GoogleAccountCredential.usingOAuth2(
                DriveAuthActivity.this, DriveScopes.DRIVE);

        createFolderIfNotExist();

        if (daysBetween <= 34) {
            if (accName.isEmpty()) {
                setContentView(R.layout.activity_drive_auth_activity);
                gdAuthBtn = (Button) findViewById(R.id.gdAuthBtn);
                gdAuthBtn.setOnClickListener(this);
            } else {
                credential.setSelectedAccountName(accName);
                drive = getDriveService(credential);
                mApp.registerGoogle(drive);
                createFolderInDriveIfDontExists();


            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(DriveAuthActivity.this);
            builder.setTitle("Attention!")
                    .setMessage("Test using of the app is over. \n" +
                            "Download new App from Google.Play or connect with the developer.")
                    .setIcon(R.drawable.ic_launcher)
                    .setCancelable(false)
                    .setNegativeButton("Exit",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    finish();
                                }
                            }).setPositiveButton("Go to Play Market", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(android.content.Intent.ACTION_VIEW);
                    i.setData(Uri.parse("https://play.google.com/store/apps/details?id=mobi.esys.upnewslite"));
                    startActivity(i);
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
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
                        Editor editor = prefs.edit();
                        editor.putString("accName", accountName);
                        editor.commit();
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
                        Editor editor = prefs.edit();
                        editor.putString("accName", accountName);
                        editor.commit();
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

    public void catchUSERException(Intent intent) {
        startActivityForResult(intent, REQUEST_AUTHORIZATION);
    }

    private void createFolderInDriveIfDontExists() {
        CreateDriveFolderTask createDriveFolderTask = new CreateDriveFolderTask(DriveAuthActivity.this, true, mApp);
        createDriveFolderTask.execute();
    }

    private void createFolderIfNotExist() {
        File videoDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + UNLConsts.VIDEO_DIR_NAME);
        if (!videoDir.exists()) {
            videoDir.mkdir();

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
