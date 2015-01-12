package mobi.esys.tasks;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import mobi.esys.constants.K2Constants;
import mobi.esys.data.GDFile;
import mobi.esys.fileworks.DirectiryWorks;
import mobi.esys.upnews_server.K2Server;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

public class DownloadVideoTask extends AsyncTask<Void, Void, Void> {
	private transient K2Server k2Server;
	private transient Context context;
	private transient SharedPreferences prefs;
	private transient String accName;
	private transient boolean isDelete;
	private transient List<GDFile> gdFiles;
	private transient static Drive drive;
	private transient GoogleAccountCredential credential;
	private transient static FileOutputStream output;
	private transient Set<String> serverMD5;
	private transient int downCount;
	private transient List<GDFile> listWithoutDuplicates;
	private List<String> folderMD5;

	public DownloadVideoTask(Context context) {
		k2Server = new K2Server(context);
		this.downCount = 0;
		this.prefs = context.getSharedPreferences(K2Constants.APP_PREF,
				Context.MODE_PRIVATE);
		this.accName = prefs.getString("accName", "");
		this.credential = GoogleAccountCredential.usingOAuth2(context,
				DriveScopes.DRIVE);
		this.credential.setSelectedAccountName(accName);
		drive = getDriveService(credential);
		this.context = context;

	}

	@Override
	protected Void doInBackground(Void... params) {
		Log.d("down", "isDwonl");
		serverMD5 = k2Server.getMD5FromServer();
		gdFiles = k2Server.getGdFiles();
		DirectiryWorks directiryWorks = new DirectiryWorks(
				K2Constants.VIDEO_DIR_NAME);
		folderMD5 = directiryWorks.getMD5Sums();
		isDelete = context.getSharedPreferences(K2Constants.APP_PREF,
				Context.MODE_PRIVATE).getBoolean("isDeleting", false);

		if (!isDelete) {
			Set<String> urlSet = new HashSet<String>();
			urlSet.add("");
			Set<String> urlSetRec = new HashSet<String>(Arrays.asList(context
					.getSharedPreferences(K2Constants.APP_PREF,
							Context.MODE_PRIVATE).getString("urls", "")
					.replace("[", "").replace("]", "").split(",")));
			SharedPreferences.Editor editor = context.getSharedPreferences(
					K2Constants.APP_PREF, Context.MODE_PRIVATE).edit();
			editor.putBoolean("isDownload", true);
			editor.commit();

			LinkedHashSet<GDFile> listToSet = new LinkedHashSet<GDFile>(gdFiles);

			listWithoutDuplicates = new ArrayList<GDFile>(listToSet);

			Log.d("drive files", String.valueOf(listWithoutDuplicates.size()));
			Log.d("md5", String.valueOf(serverMD5.size()));
			String[] urls = urlSetRec.toArray(new String[urlSetRec.size()]);
			for (int i = 0; i < urls.length; i++) {
				if (urls[i].startsWith(" ")) {
					urls[i] = urls[i].substring(0, urls[i].length() - 1);
				}
			}

			Collections.sort(listWithoutDuplicates, new Comparator<GDFile>() {
				@Override
				public int compare(GDFile lhs, GDFile rhs) {
					return lhs.getGdFileName().compareTo(rhs.getGdFileName());
				}
			});
			Log.d("files", listWithoutDuplicates.toString());

			while (downCount < listWithoutDuplicates.size()) {
				try {
					Log.d("count", String.valueOf(downCount));
					downloadFile(drive, listWithoutDuplicates.get(downCount)
							.getGdFileInst());
				} catch (Exception e) {
					Log.d("exc", e.getLocalizedMessage());
					downCount++;
				}

			}

		} else {
			Log.d("md5", "all MD5");
			downCount++;
			if (downCount == listWithoutDuplicates.size() - 1) {
				cancel(true);
			}
		}
		return null;
	}

	private void downloadFile(Drive service, File file) {
		DirectiryWorks directiryWorks = new DirectiryWorks(
				K2Constants.VIDEO_DIR_NAME);
		folderMD5 = directiryWorks.getMD5Sums();
		Log.d("down", "start down file");
		if (folderMD5.containsAll(serverMD5)
				&& folderMD5.size() == serverMD5.size()) {
			cancel(true);
			downCount++;
		} else {
			if (!folderMD5.contains(file.getMd5Checksum())) {
				if (file.getDownloadUrl() != null
						&& file.getDownloadUrl().length() > 0) {
					try {
						HttpResponse resp = service
								.getRequestFactory()
								.buildGetRequest(
										new GenericUrl(file.getDownloadUrl()))
								.execute();
						String root_sd = Environment
								.getExternalStorageDirectory()
								.getAbsolutePath()
								+ K2Constants.VIDEO_DIR_NAME;
						String path = file.getTitle();

						java.io.File downFile = new java.io.File(root_sd, path);
						Log.d("down", downFile.getAbsolutePath());
						if (!downFile.exists()) {
							output = new FileOutputStream(downFile);
							int bufferSize = 1024;
							byte[] buffer = new byte[bufferSize];
							int len = 0;
							while ((len = resp.getContent().read(buffer)) != -1) {
								output.write(buffer, 0, len);
							}

							output.flush();
							output.close();
							downCount++;

							Log.d("count down complete",
									String.valueOf(downCount));
							return;

						}

					} catch (IOException e) {
						downCount++;
						Log.d("count exc", String.valueOf(downCount));
						Log.d("exc", e.getLocalizedMessage());
						return;
					}
				}
			} else {
				Log.d("already down", String.valueOf(downCount));
				downCount++;
				return;
			}
		}
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		stopDownload();
		if (!isDelete) {
			DeleteBrokeFilesTask brokeFilesTask = new DeleteBrokeFilesTask(
					context);
			brokeFilesTask.execute();
		}
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		stopDownload();
	}

	private void stopDownload() {
		SharedPreferences.Editor editor = context.getSharedPreferences(
				K2Constants.APP_PREF, Context.MODE_PRIVATE).edit();
		editor.putBoolean("isDownload", false);
		editor.commit();
	}

	private Drive getDriveService(GoogleAccountCredential credential) {
		return new Drive.Builder(AndroidHttp.newCompatibleTransport(),
				new GsonFactory(), credential).build();
	}

}
