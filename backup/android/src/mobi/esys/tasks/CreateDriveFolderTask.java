package mobi.esys.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mobi.esys.constants.K2Constants;
import mobi.esys.upnewslite.DriveAuthActivity;
import mobi.esys.upnewslite.FirstVideoActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class CreateDriveFolderTask extends AsyncTask<Drive, Void, Void> {
	private transient SharedPreferences prefs;
	private transient Context context;
	private transient boolean isAuthSuccess;
	private transient boolean isStartVideoOnSuccess;

	public CreateDriveFolderTask(SharedPreferences prefs, Context context,
			boolean isStartVideoOnSuccess) {
		this.prefs = prefs;
		this.context = context;
		this.isAuthSuccess = true;
		this.isStartVideoOnSuccess = isStartVideoOnSuccess;
	}

	@Override
	protected Void doInBackground(Drive... params) {
		if (isOnline() && params[0] != null) {
			Log.d("gd", "create folder");
			try {
				List<String> fileName = new ArrayList<String>();
				Files.List request = params[0]
						.files()
						.list()
						.setQ("'root' in parents and mimeType = 'application/vnd.google-apps.folder' and trashed=false");
				FileList files = request.execute();

				for (File file : files.getItems()) {
					fileName.add(file.getTitle());
					Log.d("drive", file.getTitle() + ":" + file.getId());
				}

				Log.d("folder", fileName.toString());
				Editor editor = prefs.edit();
				if (!fileName.contains(K2Constants.GD_VIDEO_DIR_NAME)) {
					File body = new File();
					body.setTitle(K2Constants.GD_VIDEO_DIR_NAME);
					body.setMimeType("application/vnd.google-apps.folder");
					File unlFolder = params[0].files().insert(body).execute();
					Log.d("folderId", unlFolder.getId());
					editor.putString("folderId", unlFolder.getId());
				} else {
					Log.d("folderId",
							files.getItems()
									.get(fileName
											.indexOf(K2Constants.GD_VIDEO_DIR_NAME))
									.getId());
					editor.putString(
							"folderId",
							files.getItems()
									.get(fileName
											.indexOf(K2Constants.GD_VIDEO_DIR_NAME))
									.getId());
				}

				editor.commit();
				isAuthSuccess = true;

			} catch (UserRecoverableAuthIOException e) {
				Log.d("URAIOE", "Error");
				if (((Activity) context) instanceof DriveAuthActivity) {
					((DriveAuthActivity) context).catchUSERException(e
							.getIntent());
				}

			} catch (IOException e) {
				Log.d("IOE", "Error: " + e.getLocalizedMessage());
			}
		} else {
			isAuthSuccess = false;
		}
		return null;
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		if (isStartVideoOnSuccess) {
			if (isAuthSuccess) {
				context.startActivity(new Intent(context,
						FirstVideoActivity.class));
			} else {
				context.startActivity(new Intent(context,
						DriveAuthActivity.class));
			}
		}

	}
}
