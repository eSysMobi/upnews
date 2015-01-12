package mobi.esys.upnewslite;

import mobi.esys.constants.K2Constants;
import mobi.esys.tasks.CreateDriveFolderTask;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

@EActivity
public class DriveAuthActivity extends Activity {
	@ViewById
	Button gdAuthBtn;
	private transient SharedPreferences prefs;
	private transient GoogleAccountCredential credential;
	private transient static final int REQUEST_ACCOUNT_PICKER = 101;
	private transient static final int REQUEST_AUTHORIZATION = 102;
	private transient static final int REQUEST_AUTH_IF_ERROR = 103;
	private transient static Drive drive;
	private transient boolean isFirstAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isFirstAuth = true;
		prefs = getSharedPreferences(K2Constants.APP_PREF, MODE_PRIVATE);

		String accName = prefs.getString("accName", "");
		credential = GoogleAccountCredential.usingOAuth2(
				DriveAuthActivity.this, DriveScopes.DRIVE);
		if ("".equals(accName)) {
			setContentView(R.layout.activity_drive_auth_activity);
		} else {
			credential.setSelectedAccountName(accName);
			finish();
			startActivity(new Intent(DriveAuthActivity.this,
					FirstVideoActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

		}
	}

	private Drive getDriveService(GoogleAccountCredential credential) {
		return new Drive.Builder(AndroidHttp.newCompatibleTransport(),
				new GsonFactory(), credential).build();
	}

	@AfterViews
	void init() {

	}

	@Click
	void gdAuthBtn() {
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
				startActivity(new Intent(DriveAuthActivity.this,
						FirstVideoActivity.class));
				finish();
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
					startActivity(new Intent(DriveAuthActivity.this,
							FirstVideoActivity.class));
					finish();
				} else {
					picker();
				}
				break;

			}
		}
		finish();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
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
