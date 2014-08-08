package mobi.esys.playback;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import mobi.esys.constants.K2Constants;
import mobi.esys.fileworks.DirectiryWorks;
import mobi.esys.fileworks.FileWorks;
import mobi.esys.tasks.CreateDriveFolderTask;
import mobi.esys.tasks.DownloadVideoTask;
import mobi.esys.upnewslite.FullscreenActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

public class K2Playback {
	private transient Context context;
	private transient String folderPath;
	private transient VideoView k2VideoView;
	// private transient int videoIndex = 0;
	private static final String PLAY_TAG = "K2Playback";
	private transient Set<String> md5sApp;
	private transient String[] files;
	private transient SharedPreferences preferences;
	private transient DownloadVideoTask downloadVideoTask;
	private transient Bundle sendBundle;
	private transient String[] ulrs = { "" };
	private transient int serverIndex = 1;
	private SharedPreferences prefs;
	private GoogleAccountCredential credential;
	private Drive drive;

	public K2Playback(Context context, String folderPath) {
		super();
		this.k2VideoView = ((FullscreenActivity) context).getVideoView();
		this.k2VideoView.setMediaController(new MediaController(context));
		this.k2VideoView.requestFocus();
		this.context = context;
		this.sendBundle = new Bundle();
		sendBundle.putString(
				"device_id",
				context.getSharedPreferences(K2Constants.APP_PREF,
						Context.MODE_PRIVATE).getString("device_id", "0000"));
		this.folderPath = folderPath;
		prefs = context.getSharedPreferences(K2Constants.APP_PREF,
				Context.MODE_PRIVATE);

		String accName = prefs.getString("accName", "");
		credential = GoogleAccountCredential.usingOAuth2(context,
				DriveScopes.DRIVE);
		credential.setSelectedAccountName(accName);

		drive = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
				new GsonFactory(), credential).build();
	}

	public void playFile(String filePath) {
		preferences = context.getSharedPreferences(K2Constants.APP_PREF,
				Context.MODE_PRIVATE);
		Set<String> defaultSet = new HashSet<String>();
		defaultSet.add(K2Constants.FIRST_MD5);
		md5sApp = preferences.getStringSet("md5sApp", defaultSet);
		File file = new File(filePath);
		FileWorks fileWorks = new FileWorks(filePath);
		if ((file.exists() && md5sApp.contains(fileWorks.getFileMD5()))
				|| (file.exists() && file.getName().startsWith("dd"))) {
			k2VideoView.setVideoURI(Uri.parse(filePath));
			k2VideoView.start();
		} else {
			nextTrack(files);
		}

	}

	public void playFolder() {
		downloadVideoTask = new DownloadVideoTask(context);
		downloadVideoTask.execute();
		DirectiryWorks directiryWorks = new DirectiryWorks(folderPath);
		files = directiryWorks.getDirFileList();
		this.k2VideoView.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				nextTrack(files);
				return true;
			}
		});
		if (files.length > 0) {
			playFile(files[0]);
			k2VideoView.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					nextTrack(files);
					if (!context.getSharedPreferences(K2Constants.APP_PREF,
							Context.MODE_PRIVATE).getBoolean("isDownload",
							false)) {
						restartDownload();
					}

				}

			});
		} else {
			Log.d(PLAY_TAG, "file list is empty");
		}
	}

	private void nextTrack(final String[] files) {
		createFolderInDriveIfDontExists(drive);

		if (files.length > 0) {

			String[] listFiles = { files[0] };

			if (!context
					.getSharedPreferences(K2Constants.APP_PREF,
							Context.MODE_PRIVATE).getString("urls", "")
					.equals("")) {
				Log.d("urls string",
						context.getSharedPreferences(K2Constants.APP_PREF,
								Context.MODE_PRIVATE).getString("urls", "")
								.replace("[", "").replace("]", ""));

				ulrs = context
						.getSharedPreferences(K2Constants.APP_PREF,
								Context.MODE_PRIVATE).getString("urls", "")
						.replace("[", "").replace("]", "").split(",");

			}

			listFiles = new String[ulrs.length + 1];
			for (int i = 0; i < listFiles.length; i++) {
				if (i == 0) {
					listFiles[i] = files[0];
				} else {
					if (ulrs[i - 1].startsWith(" ")) {
						ulrs[i - 1] = ulrs[i - 1].substring(1,
								ulrs[i - 1].length());
					}
					listFiles[i] = Environment.getExternalStorageDirectory()
							.getAbsolutePath()
							+ K2Constants.VIDEO_DIR_NAME
							+ ulrs[i - 1]
									.substring(
											ulrs[i - 1].lastIndexOf('/') + 1,
											ulrs[i - 1].length())
									.replace("[", "").replace("]", "");
				}
			}

			Log.d("urls next", Arrays.asList(listFiles).toString());

			Log.d("fs", listFiles[serverIndex]);
			FileWorks fileWorks = new FileWorks(listFiles[serverIndex]);
			File fs = new File(listFiles[serverIndex]);

			DirectiryWorks directiryWorks = new DirectiryWorks(folderPath);
			String[] refreshFiles = directiryWorks.getDirFileList();
			Log.d("files", Arrays.asList(refreshFiles).toString());

			if (fs.exists()
					&& md5sApp.contains(fileWorks.getFileMD5())
					&& Arrays.asList(refreshFiles).contains(
							fs.getAbsolutePath()) || fs.exists()
					&& fs.getName().startsWith("dd")) {

				if (serverIndex == listFiles.length - 1) {
					Log.d("index", String.valueOf(serverIndex));
					Log.d("len", String.valueOf(listFiles.length));
					playFile(listFiles[serverIndex]);
					String videoName = listFiles[serverIndex].substring(
							listFiles[serverIndex].lastIndexOf("/") + 1,
							listFiles[serverIndex].length())
							.replace(".mp4", "");
					sendBundle.putString("video_name", videoName);
					serverIndex = 0;

				} else {
					playFile(listFiles[serverIndex]);
					Log.d("index", String.valueOf(serverIndex));
					String videoName = listFiles[serverIndex].substring(
							listFiles[serverIndex].lastIndexOf("/") + 1,
							listFiles[serverIndex].length())
							.replace(".mp4", "");
					sendBundle.putString("video_name", videoName);
					serverIndex++;
				}
			} else {
				playFile(files[0]);
				serverIndex = 0;
				String videoName = files[0].substring(
						files[0].lastIndexOf("/") + 1, files[0].length())
						.replace(".mp4", "");
				sendBundle.putString("video_name", videoName);
			}

		}

	}

	public void stopPlayback() {
		k2VideoView.stopPlayback();
	}

	public void pausePlayback() {
		k2VideoView.pause();
	}

	public void resumePlayback() {
		k2VideoView.resume();
	}

	public void restartDownload() {
		createFolderInDriveIfDontExists(drive);
		downloadVideoTask.cancel(true);
		downloadVideoTask = new DownloadVideoTask(context);
		downloadVideoTask.execute();
	}

	public void stopDownload() {
		downloadVideoTask.cancel(true);
	}

	private void createFolderInDriveIfDontExists(Drive drive) {
		CreateDriveFolderTask createDriveFolderTask = new CreateDriveFolderTask(
				prefs, context, false);
		createDriveFolderTask.execute(drive);
	}

}
