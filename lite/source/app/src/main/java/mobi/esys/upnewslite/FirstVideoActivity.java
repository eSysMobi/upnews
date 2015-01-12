package mobi.esys.upnewslite;

import java.util.HashSet;
import java.util.Set;

import mobi.esys.constants.K2Constants;
import mobi.esys.fileworks.DirectiryWorks;
import mobi.esys.fileworks.FileWorks;
import mobi.esys.tasks.CreateDriveFolderTask;
import mobi.esys.tasks.DownloadVideoTask;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

public class FirstVideoActivity extends Activity {
	private transient DownloadVideoTask downloadVideoTask;
	private transient GoogleAccountCredential credential;
	private transient VideoView video;
	private transient String uriPath;
	private transient MediaController controller;
	private transient SharedPreferences prefs;
	private transient boolean isDown;
	private transient Drive drive;
	private transient Set<String> md5sApp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		String accName = getSharedPreferences(K2Constants.APP_PREF,
				MODE_PRIVATE).getString("accName", "");
		downloadVideoTask = new DownloadVideoTask(FirstVideoActivity.this);
		downloadVideoTask.execute();
		super.onCreate(savedInstanceState);
		DirectiryWorks directiryWorks = new DirectiryWorks(
				K2Constants.VIDEO_DIR_NAME);
		this.credential = GoogleAccountCredential.usingOAuth2(
				FirstVideoActivity.this, DriveScopes.DRIVE);
		this.credential.setSelectedAccountName(accName);
		this.drive = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
				new GsonFactory(), this.credential).build();
		prefs = getSharedPreferences(K2Constants.APP_PREF, MODE_PRIVATE);
		isDown = prefs.getBoolean("isDownload", true);
		this.uriPath = "";
		Set<String> defSet = new HashSet<String>();
		md5sApp = prefs.getStringSet("md5sApp", defSet);
		if (directiryWorks.getDirFileList("first").length == 0
				&& md5sApp.size() == 0) {
			setContentView(R.layout.activity_firstvideo);
			controller = new MediaController(FirstVideoActivity.this);
			video = (VideoView) findViewById(R.id.k2FirstVideoView);
			video.setMediaController(controller);
			uriPath = "android.resource://" + getPackageName() + "/assets/"
					+ R.raw.emb;
			Log.d("video", uriPath);
			Uri uri = Uri.parse(uriPath);
			video.setVideoURI(uri);
			video.start();
			video.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					createFolderInDriveIfDontExists(drive);
					DirectiryWorks directiryWorks = new DirectiryWorks(
							K2Constants.VIDEO_DIR_NAME);
					Set<String> defSet = new HashSet<String>();
					md5sApp = prefs.getStringSet("md5sApp", defSet);
					if (directiryWorks.getDirFileList("first").length == 0
							&& md5sApp.size() == 0) {
						if (!isDown) {
							restartDownload();
						}
						Uri uri = Uri.parse(uriPath);
						video.setVideoURI(uri);
						video.start();
					} else {
						FileWorks fileWorks = new FileWorks(directiryWorks
								.getDirFileList("first")[0]);
						stopDownload();
						if (md5sApp.contains(fileWorks.getFileMD5())) {
							startActivity(new Intent(FirstVideoActivity.this,
									FullscreenActivity.class));
							finish();

						} else {
							if (!isDown) {
								restartDownload();
							}
							Uri uri = Uri.parse(uriPath);
							video.setVideoURI(uri);
							video.start();
						}
					}

				}
			});

		} else {
			startActivity(new Intent(FirstVideoActivity.this,
					FullscreenActivity.class));
			finish();
			stopDownload();

		}
	}

	public void restartDownload() {

		downloadVideoTask.cancel(true);
		downloadVideoTask = new DownloadVideoTask(FirstVideoActivity.this);
		downloadVideoTask.execute();
	}

	private void createFolderInDriveIfDontExists(Drive drive) {
		CreateDriveFolderTask createDriveFolderTask = new CreateDriveFolderTask(
				getSharedPreferences(K2Constants.APP_PREF, MODE_PRIVATE),
				FirstVideoActivity.this, false);
		createDriveFolderTask.execute(drive);
	}

	@Override
	protected void onStop() {
		super.onStop();
		video.pause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		video.pause();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (!video.isPlaying()) {
			video.resume();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!video.isPlaying()) {
			video.resume();
		}
	}

	@Override
	protected void onStart() {
		EasyTracker.getInstance(this).activityStart(this);
		super.onStart();
	}

	public void stopDownload() {
		downloadVideoTask.cancel(true);
	}

}
