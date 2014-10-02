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
import mobi.esys.upnewslite.FirstVideoActivity;
import mobi.esys.upnewslite.FullscreenActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
	private transient VideoView k2VideoView;
	private static final String PLAY_TAG = "K2Playback";
	private transient Set<String> md5sApp;
	private transient String[] files;
	private transient SharedPreferences preferences;
	private transient DownloadVideoTask downloadVideoTask;
	private transient Bundle sendBundle;
	private transient String[] ulrs = { "" };
	private transient int serverIndex = 0;
	private transient SharedPreferences prefs;
	private transient GoogleAccountCredential credential;
	private transient Drive drive;
	private transient boolean isDownload;

	public K2Playback(Context context) {
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
		md5sApp = preferences.getStringSet("md5sApp", defaultSet);
		File file = new File(filePath);
		FileWorks fileWorks = new FileWorks(filePath);
		if ((file.exists() && md5sApp.contains(fileWorks.getFileMD5()))) {
			k2VideoView.setVideoURI(Uri.parse(filePath));
			k2VideoView.start();
		} else {
			nextTrack(files);
		}

	}

	public void playFolder() {
		downloadVideoTask = new DownloadVideoTask(context);
		downloadVideoTask.execute();
		DirectiryWorks directiryWorks = new DirectiryWorks(
				K2Constants.VIDEO_DIR_NAME);
		files = directiryWorks.getDirFileList("play folder");
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
					DirectiryWorks directiryWorks = new DirectiryWorks(
							K2Constants.VIDEO_DIR_NAME);
					if (directiryWorks.getDirFileList("").length == 0) {
						context.startActivity(new Intent(context,
								FirstVideoActivity.class));
						((Activity) context).finish();
					} else {
						isDownload = context.getSharedPreferences(
								K2Constants.APP_PREF, Context.MODE_PRIVATE)
								.getBoolean("isDownload", false);
						nextTrack(files);
						if (!isDownload) {
							restartDownload();
						}
					}
				}

			});
		} else {
			Log.d(PLAY_TAG, "file list is empty");
		}
	}

	private void nextTrack(final String[] files) {
		createFolderInDriveIfDontExists(drive);

		String[] listFiles = { files[0] };

		if (!context
				.getSharedPreferences(K2Constants.APP_PREF,
						Context.MODE_PRIVATE).getString("urls", "").equals("")) {
			Log.d("urls string",
					context.getSharedPreferences(K2Constants.APP_PREF,
							Context.MODE_PRIVATE).getString("urls", "")
							.replace("[", "").replace("]", ""));

			ulrs = context
					.getSharedPreferences(K2Constants.APP_PREF,
							Context.MODE_PRIVATE).getString("urls", "")
					.replace("[", "").replace("]", "").split(",");

			if (files.length > 0) {

			}

			listFiles = new String[ulrs.length];
			for (int i = 0; i < listFiles.length; i++) {
				if (ulrs[i].startsWith(" ")) {
					ulrs[i] = ulrs[i].substring(1, ulrs[i].length());
				}
				listFiles[i] = Environment.getExternalStorageDirectory()
						.getAbsolutePath()
						+ K2Constants.VIDEO_DIR_NAME
						+ ulrs[i]
								.substring(ulrs[i].lastIndexOf('/') + 1,
										ulrs[i].length()).replace("[", "")
								.replace("]", "");
			}

			Log.d("urls next", Arrays.asList(listFiles).toString());
			File fs = new File(listFiles[serverIndex]);

			if (fs.exists()) {
				FileWorks fileWorks = new FileWorks(listFiles[serverIndex]);

				DirectiryWorks directiryWorks = new DirectiryWorks(
						K2Constants.VIDEO_DIR_NAME);
				String[] refreshFiles = directiryWorks.getDirFileList("folder");
				Log.d("files", Arrays.asList(refreshFiles).toString());

				if (md5sApp.contains(fileWorks.getFileMD5())
						&& Arrays.asList(refreshFiles).contains(
								fs.getAbsolutePath())) {

					if (serverIndex == listFiles.length - 1) {
						Log.d("index", String.valueOf(serverIndex));
						Log.d("len", String.valueOf(listFiles.length));
						playFile(listFiles[serverIndex]);
						serverIndex = 0;

					} else {
						playFile(listFiles[serverIndex]);
						Log.d("index", String.valueOf(serverIndex));
						serverIndex++;
					}
				} else {
					playFile(files[0]);
					serverIndex = 0;
				}

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
