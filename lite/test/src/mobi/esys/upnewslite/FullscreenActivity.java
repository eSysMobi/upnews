package mobi.esys.upnewslite;

import mobi.esys.constants.K2Constants;
import mobi.esys.fileworks.DirectiryWorks;
import mobi.esys.playback.K2Playback;
import mobi.esys.tasks.CreateDriveFolderTask;
import mobi.esys.tasks.DownloadVideoTask;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.VideoView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

@SuppressLint({ "NewApi", "SimpleDateFormat" })
public class FullscreenActivity extends Activity {
	private transient VideoView k2VideoView;
	private static final String K2Tag = "K2";
	private transient K2Playback k2Playback;
	private transient GoogleAccountCredential credential;

	public class CameraPreview extends SurfaceView {

		public CameraPreview(Context context) {
			super(context);
		}

	}

	@Override
	protected void onCreate(Bundle stateBundle) {
		String accName = getSharedPreferences(K2Constants.APP_PREF,
				MODE_PRIVATE).getString("accName", "");
		credential = GoogleAccountCredential.usingOAuth2(
				FullscreenActivity.this, DriveScopes.DRIVE);
		credential.setSelectedAccountName(accName);
		EasyTracker.getInstance(this).send(
				MapBuilder.createEvent("ui_action", "activity_launch",
						"fullscreen_acivity", null).build());
		super.onCreate(stateBundle);
		setContentView(R.layout.activity_videofullscreen);

		k2VideoView = (VideoView) findViewById(R.id.k2VideoView);

		DownloadVideoTask downloadVideoTask = new DownloadVideoTask(
				FullscreenActivity.this);
		downloadVideoTask.execute();

		startPlayback();

	}

	@Override
	protected void onStop() {
		super.onStop();
		k2Playback.pausePlayback();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		k2Playback.stopPlayback();
	}

	@Override
	protected void onPause() {
		super.onPause();
		k2Playback.pausePlayback();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		k2Playback.pausePlayback();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		DirectiryWorks directiryWorks = new DirectiryWorks(
				K2Constants.VIDEO_DIR_NAME);
		if (directiryWorks.getDirFileList("fullscreen").length == 0) {
			startActivity(new Intent(FullscreenActivity.this,
					FirstVideoActivity.class));
			finish();
		} else {
			startPlayback();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		DirectiryWorks directiryWorks = new DirectiryWorks(
				K2Constants.VIDEO_DIR_NAME);
		if (directiryWorks.getDirFileList("fullscreen").length == 0) {
			startActivity(new Intent(FullscreenActivity.this,
					FirstVideoActivity.class));
			finish();
		} else {
			startPlayback();
		}
	}

	public void startPlayback() {

		Log.d(K2Tag, K2Constants.VIDEO_DIR_NAME);
		k2Playback = new K2Playback(FullscreenActivity.this);

		k2Playback.playFolder();
	}

	public VideoView getVideoView() {
		return this.k2VideoView;
	}

	@Override
	protected void onStart() {
		EasyTracker.getInstance(this).activityStart(this);
		super.onStart();
	}

}
