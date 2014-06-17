package mobi.esys.playback;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import mobi.esys.constants.K2Constants;
import mobi.esys.fileworks.DirectiryWorks;
import mobi.esys.fileworks.FileWorks;
import mobi.esys.tasks.DownloadVideoTask;
import mobi.esys.tasks.SendDataToServerTask;
import mobi.esys.upnews.FullscreenActivity;
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

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class K2Playback {
	private final static String K2P_FILE_PREFIX = K2Constants.FILE_PREFIX;
	private static final String PLAY_TAG = "K2Playback";
	// private static final String VIDEO_FORMAT = ".mp4";
	private static final String VIDEO_EVENT = "video";
	// private static final String VIDEO_NAME_PARAM = "video_name";

	private transient Context context;
	private transient String folderPath;
	private transient VideoView k2VideoView;
	private transient int videoIndex = 0;
	private transient int currentServerIndex = 0;

	private transient Set<String> md5sApp;
	private transient String[] files;
	private transient SharedPreferences preferences;
	private transient DownloadVideoTask downloadVideoTask;
	private transient Bundle sendBundle;

	private transient boolean isFirstNext = true;

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
	}

	public void playFile(String filePath) {

		FullscreenActivity activity = (FullscreenActivity) context;
		Bundle actBundle = activity.sendParams();
		sendBundle.putString("battery_charge_level",
				actBundle.getString("battery_charge_level"));
		sendBundle.putString("signal_level",
				actBundle.getString("signal_level"));
		sendBundle.putString("power_supply",
				actBundle.getString("power_supply"));
		sendBundle.putString("latitude", actBundle.getString("latitude"));
		sendBundle.putString("longitude", actBundle.getString("longitude"));
		preferences = context.getSharedPreferences(K2Constants.APP_PREF,
				Context.MODE_PRIVATE);
		Set<String> defaultSet = new HashSet<String>();
		defaultSet.add(K2Constants.FIRST_MD5);
		md5sApp = preferences.getStringSet("md5sApp", defaultSet);

		FileWorks fileWorks = new FileWorks(filePath);
		File file = new File(filePath);
		if ((file.exists() && md5sApp.contains(fileWorks.getFileMD5()))
				|| (file.exists() && file.getName().startsWith(K2P_FILE_PREFIX))) {
			k2VideoView.setVideoURI(Uri.parse(filePath));
			k2VideoView.start();

		} else {
			nextTrack(files);
			EasyTracker.getInstance(context).send(
					MapBuilder.createEvent(VIDEO_EVENT, // Event
							// category
							// (required)
							"next_video", // Event action (required)
							VIDEO_EVENT, // Event label
							null) // Event value
							.build());
		}

	}

	public void playFolder() {
		downloadVideoTask = new DownloadVideoTask(context);
		downloadVideoTask.execute();

		DirectiryWorks directiryWorks = new DirectiryWorks(context, folderPath);
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

		if (files.length > 0) {
			if (videoIndex == files.length - 1) {
				videoIndex = 0;
				File file = new File(files[files.length - 1]);
				String videoName = file.getName().replace(".mp4", "");
				if (videoName.startsWith("dd")) {
					videoName.substring(2, videoName.length() - 1);
				}
				sendBundle.putString("video_name", videoName);
			} else {
				videoIndex++;
				File file = new File(files[videoIndex - 1]);
				String videoName = file.getName().replace(".mp4", "");
				if (videoName.startsWith("dd")) {
					videoName.substring(2, videoName.length() - 1);
				}
				sendBundle.putString("video_name", videoName);
			}

			String nextFile = files[videoIndex];
			Log.d("cPF", nextFile);
			String[] vFiles = getServerVideoFileList();
			if (isFirstNext) {
				if (currentServerIndex == vFiles.length - 1) {
					currentServerIndex = 0;
				} else {
					currentServerIndex++;
				}
				isFirstNext = false;
			}
			Log.d("cSF", vFiles[currentServerIndex]);
			do {
				if (videoIndex == files.length - 1) {
					videoIndex = 0;
					File file = new File(files[files.length - 1]);
					String videoName = file.getName().replace(".mp4", "");
					if (videoName.startsWith("dd")) {
						videoName.substring(2, videoName.length() - 1);
					}
					sendBundle.putString("video_name", videoName);
				} else {
					videoIndex++;
					File file = new File(files[videoIndex - 1]);
					String videoName = file.getName().replace(".mp4", "");
					if (videoName.startsWith("dd")) {
						videoName.substring(2, videoName.length() - 1);
					}
					sendBundle.putString("video_name", videoName);
				}

				nextFile = files[videoIndex];
				// Log.d("next", nextFile);
			} while (!nextFile.equals(vFiles[currentServerIndex]));

			if (currentServerIndex == vFiles.length - 1) {
				currentServerIndex = 0;
			} else {
				currentServerIndex++;
			}
			Log.d("cPF", nextFile);
			playFile(files[videoIndex]);

			SharedPreferences.Editor editor = preferences.edit();
			editor.putInt("currPlIndex", videoIndex);
			editor.commit();
		}

		sendDataToServer(sendBundle);
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
		downloadVideoTask.cancel(true);
		downloadVideoTask = new DownloadVideoTask(context);
		downloadVideoTask.execute();
	}

	public void stopDownload() {
		downloadVideoTask.cancel(true);
	}

	private void sendDataToServer(Bundle bundle) {
		SendDataToServerTask dataToServerTask = new SendDataToServerTask(
				context);
		dataToServerTask.execute(bundle);
	}

	private String[] getServerVideoFileList() {

		String vfn = context.getSharedPreferences(K2Constants.APP_PREF,
				Context.MODE_PRIVATE).getString("videoFilesNames", "");

		String[] vf = vfn.split(",");
		String[] videoFilesNext = new String[vf.length];
		for (int i = 0; i < vf.length; i++) {
			String cvf = vf[i].replace(" ", "");
			videoFilesNext[i] = (Environment.getExternalStorageDirectory()
					+ K2Constants.VIDEO_DIR_NAME + cvf);
			Log.d("files", videoFilesNext[i]);
		}

		return videoFilesNext;
	}
}
