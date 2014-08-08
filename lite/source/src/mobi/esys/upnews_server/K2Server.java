package mobi.esys.upnews_server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mobi.esys.constants.K2Constants;
import mobi.esys.data.GDFile;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Children;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;

public class K2Server {
	private transient Context context;
	private transient String accName;
	private transient String folderId;
	private transient SharedPreferences prefs;
	private transient List<GDFile> gdFiles;
	private transient static Drive drive;
	private transient GoogleAccountCredential credential;

	public K2Server(Context context) {
		this.context = context;
		this.prefs = context.getSharedPreferences(K2Constants.APP_PREF,
				Context.MODE_PRIVATE);
		this.accName = this.prefs.getString("accName", "");
		this.folderId = this.prefs.getString("folderId", "");
		this.gdFiles = new ArrayList<GDFile>();
		credential = GoogleAccountCredential.usingOAuth2(context,
				DriveScopes.DRIVE);
		credential.setSelectedAccountName(accName);
		drive = getDriveService(credential);
	}

	public Set<String> getMD5FromServer() {
		saveURLS();
		Set<String> resultMD5 = new HashSet<String>();
		try {
			printFilesInFolder(drive, folderId);
			for (int i = 0; i < gdFiles.size(); i++) {
				resultMD5.add(gdFiles.get(i).getGdFileMD5());
			}
		} catch (IOException e) {
		}

		SharedPreferences.Editor editor = prefs.edit();
		editor.putStringSet("md5sApp", resultMD5);
		editor.commit();

		return resultMD5;
	}

	private void saveURLS() {
		List<String> resultURL = new ArrayList<String>();
		try {
			printFilesInFolder(drive, folderId);
			for (int i = 0; i < gdFiles.size(); i++) {
				resultURL.add(gdFiles.get(i).getGdFileName());
			}
		} catch (IOException e) {
		}
		Editor editor = prefs.edit();
		editor.putString("urls", resultURL.toString());
		editor.commit();
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

	private void printFilesInFolder(Drive service, String folderId)
			throws IOException {
		Children.List request = service.children().list(folderId);

		do {
			try {
				ChildList children = request.execute();

				for (ChildReference child : children.getItems()) {

					File file = drive.files().get(child.getId()).execute();
					if (Arrays.asList(K2Constants.UNL_ACCEPTED_FILE_EXTS)
							.contains(file.getFileExtension())
							&& file.getFileSize() < K2Constants.UNL_MAX_FILE_SIZE) {
						gdFiles.add(new GDFile(file.getId(), file.getTitle(),
								file.getDownloadUrl(), String.valueOf(file
										.getFileSize()), file
										.getFileExtension(), file
										.getMd5Checksum(), file));
					}
				}
				request.setPageToken(children.getNextPageToken());
			} catch (IOException e) {
				System.out.println("An error occurred: " + e);
				request.setPageToken(null);
			}
		} while (request.getPageToken() != null
				&& request.getPageToken().length() > 0);

	}

	private Drive getDriveService(GoogleAccountCredential credential) {
		return new Drive.Builder(AndroidHttp.newCompatibleTransport(),
				new GsonFactory(), credential).build();
	}

	public List<GDFile> getGdFiles() {
		return gdFiles;
	}

	public void setGdFiles(List<GDFile> gdFiles) {
		this.gdFiles = gdFiles;
	}

}
