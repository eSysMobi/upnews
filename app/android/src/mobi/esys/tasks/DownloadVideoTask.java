package mobi.esys.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import mobi.esys.constants.K2Constants;
import mobi.esys.fileworks.DirectiryWorks;
import mobi.esys.fileworks.FileWorks;
import mobi.esys.upnews_server.K2Server;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;

public class DownloadVideoTask extends AsyncTask<Void, Void, Void> {
	private transient String[] md5s;
	private transient K2Server k2Server;
	private transient Context context;
	boolean isDelete;

	public DownloadVideoTask(Context context) {
		k2Server = new K2Server(context);
		this.context = context;
	}

	@Override
	protected Void doInBackground(Void... params) {
		String[] serverMD5 = k2Server.getMD5FromServer();
		DirectiryWorks directiryWorks = new DirectiryWorks(
				K2Constants.VIDEO_DIR_NAME);
		String[] folderMD5 = directiryWorks.getMD5Sums();
		isDelete = context.getSharedPreferences(K2Constants.APP_PREF,
				Context.MODE_PRIVATE).getBoolean("isDeleting", false);

		if (!Arrays.deepEquals(serverMD5, folderMD5)) {
			if (!isDelete) {
				Set<String> urlSet = new HashSet<String>();
				urlSet.add("");
				Set<String> urlSetRec = context.getSharedPreferences(
						K2Constants.APP_PREF, Context.MODE_PRIVATE)
						.getStringSet("urls", urlSet);
				SharedPreferences.Editor editor = context.getSharedPreferences(
						K2Constants.APP_PREF, Context.MODE_PRIVATE).edit();
				editor.putBoolean("isDownload", true);
				editor.commit();
				String[] urls = urlSetRec.toArray(new String[urlSetRec.size()]);
				for (int i = 0; i < urls.length; i++) {
					try {
						videoFromUrlToDisk(urls[i]);
					} catch (Exception e) {
					}
				}
			} else {
				cancel(true);
			}
		} else {
			cancel(true);
		}
		return null;
	}

	public void videoFromUrlToDisk(final String vidURL) throws Exception {
		Set<String> defSet = new HashSet<String>();
		defSet.add(K2Constants.FIRST_MD5);
		Set<String> md5Set = context.getSharedPreferences(K2Constants.APP_PREF,
				Context.MODE_PRIVATE).getStringSet("md5sApp", defSet);
		md5s = md5Set.toArray(new String[md5Set.size()]);
		InputStream is = null;
		final URL url = new URL(vidURL);
		final HttpURLConnection urlConnection = (HttpURLConnection) url
				.openConnection();
		urlConnection.setRequestMethod("GET");
		urlConnection.setDoOutput(true);
		urlConnection.connect();
		is = urlConnection.getInputStream();

		File file = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + K2Constants.VIDEO_DIR_NAME,
				vidURL.substring(vidURL.lastIndexOf('/'), vidURL.length()));
		if (!file.exists()) {
			writeFile(is, file);

		} else {
			FileWorks fileWorks = new FileWorks(file.getPath());
			if (!Arrays.asList(md5s).contains(fileWorks.getFileMD5())) {
				writeFile(is, file);
			}
		}

		if (is != null) {
			is.close();
		}

	}

	private void writeFile(InputStream is, File f) throws Exception {
		f.createNewFile();

		FileOutputStream fos = new FileOutputStream(f);
		byte[] buffer = new byte[1024];
		int len1 = 0;

		if (is != null) {
			while ((len1 = is.read(buffer)) > 0) {
				fos.write(buffer, 0, len1);

			}
		}
		if (fos != null) {
			fos.close();
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
		if (!isDelete) {
			DeleteBrokeFilesTask brokeFilesTask = new DeleteBrokeFilesTask(
					context);
			brokeFilesTask.execute();
		}
	}

	private void stopDownload() {
		SharedPreferences.Editor editor = context.getSharedPreferences(
				K2Constants.APP_PREF, Context.MODE_PRIVATE).edit();
		editor.putBoolean("isDownload", false);
		editor.commit();
	}

}
