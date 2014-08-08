package mobi.esys.upnewslite;

import mobi.esys.constants.K2Constants;
import mobi.esys.tasks.CreateDriveFolderTask;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

public class DriveAuthActivity extends Activity implements OnClickListener {
	private transient Button gdAuthBtn;
	private transient SharedPreferences prefs;
	private transient GoogleAccountCredential credential;
	private static final int REQUEST_ACCOUNT_PICKER = 101;
	private static final int REQUEST_AUTHORIZATION = 102;
	private static final int REQUEST_AUTH_IF_ERROR = 103;
	private static Drive drive;
	private transient boolean isFirstAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isFirstAuth = true;
		prefs = getSharedPreferences(K2Constants.APP_PREF, MODE_PRIVATE);
		String accName = prefs.getString("accName", "");
		credential = GoogleAccountCredential.usingOAuth2(
				DriveAuthActivity.this, DriveScopes.DRIVE);
		if (accName.equals("")) {
			setContentView(R.layout.activity_drive_auth_activity);
			init();
		} else {
			credential.setSelectedAccountName(accName);
			startActivity(new Intent(DriveAuthActivity.this,
					SplashActivity.class));
			finish();
		}
	}

	private Drive getDriveService(GoogleAccountCredential credential) {
		return new Drive.Builder(AndroidHttp.newCompatibleTransport(),
				new GsonFactory(), credential).build();
	}

	private void initAuthBtn() {
		gdAuthBtn = (Button) findViewById(R.id.gdAuthBtn);
		gdAuthBtn.setOnClickListener(DriveAuthActivity.this);
	}

	private void init() {
		initAuthBtn();

	}

	@Override
	public void onClick(View v) {
		picker();
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
					if (isFirstAuth) {
						createFolderInDriveIfDontExists(drive);
						isFirstAuth = false;
					}
				}
			}
			break;

		case REQUEST_AUTHORIZATION:
			if (resultCode == Activity.RESULT_OK) {
				createFolderInDriveIfDontExists(drive);
			} else {
				startActivityForResult(credential.newChooseAccountIntent(),
						REQUEST_ACCOUNT_PICKER);
				createFolderInDriveIfDontExists(drive);
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
				} else {
					picker();
				}
				break;

			}
		}
	}

	public void catchUSERException(Intent intent) {
		startActivityForResult(intent, REQUEST_AUTHORIZATION);
	}

	private void createFolderInDriveIfDontExists(Drive drive) {
		CreateDriveFolderTask createDriveFolderTask = new CreateDriveFolderTask(
				prefs, DriveAuthActivity.this, true);
		createDriveFolderTask.execute(drive);
	}
}
