package mobi.esys.upnews_server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import mobi.esys.constants.K2Constants;
import mobi.esys.data.GDFile;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

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

		if (isOnline()) {
			try {
				printFilesInFolder(drive, folderId);
				for (int i = 0; i < gdFiles.size(); i++) {
					resultMD5.add(gdFiles.get(i).getGdFileMD5());
				}
				Log.d("md5 server size", String.valueOf(resultMD5.size()));
			} catch (IOException e) {
				Log.d("get md5 from server error", e.getLocalizedMessage());
			}

			SharedPreferences.Editor editor = prefs.edit();
			editor.putStringSet("md5sApp", resultMD5);
			editor.commit();
		} else {
			resultMD5 = prefs.getStringSet("md5sApp", resultMD5);
		}
		return resultMD5;
	}

	private void saveURLS() {
		List<String> resultURL = new ArrayList<String>();
		if (isOnline()) {
			try {
				Log.d("save urls", "save urls");
				printFilesInFolder(drive, folderId);
				for (int i = 0; i < gdFiles.size(); i++) {
					resultURL.add(gdFiles.get(i).getGdFileName());
				}
			} catch (IOException e) {
				Log.d("save url error", e.getLocalizedMessage());
			}
			Collections.sort(resultURL, new Comparator<String>() {

				@Override
				public int compare(String lhs, String rhs) {
					return lhs.toLowerCase(Locale.getDefault()).compareTo(
							rhs.toLowerCase(Locale.getDefault()));
				}

			});
			Log.d("saved urls", resultURL.toString());
			Editor editor = prefs.edit();
			editor.putString("urls", resultURL.toString());
			Set<String> urlsSet = new HashSet<String>();
			for (int i = 0; i < resultURL.size(); i++) {
				urlsSet.add(Environment.getExternalStorageDirectory()
						+ K2Constants.VIDEO_DIR_NAME + resultURL.get(i));
			}
			editor.putStringSet("filesServer", urlsSet);
			editor.commit();
		}
	}

	public boolean isOnline() {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobileInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		State mobile = NetworkInfo.State.DISCONNECTED;
		if (mobileInfo != null) {
			mobile = mobileInfo.getState();
		}
		NetworkInfo wifiInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		State wifi = NetworkInfo.State.DISCONNECTED;
		if (wifiInfo != null) {
			wifi = wifiInfo.getState();
		}
		boolean dataOnWifiOnly = (Boolean) PreferenceManager
				.getDefaultSharedPreferences(context).getBoolean(
						"data_wifi_only", true);
		if ((!dataOnWifiOnly && (mobile.equals(NetworkInfo.State.CONNECTED) || wifi
				.equals(NetworkInfo.State.CONNECTED)))
				|| (dataOnWifiOnly && wifi.equals(NetworkInfo.State.CONNECTED))) {
			return true;
		} else {
			return false;
		}
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
