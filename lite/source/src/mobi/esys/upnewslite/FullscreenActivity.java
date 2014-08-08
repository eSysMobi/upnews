package mobi.esys.upnewslite;

import mobi.esys.constants.K2Constants;
import mobi.esys.playback.K2Playback;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.VideoView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

@SuppressLint({ "NewApi", "SimpleDateFormat" })
public class FullscreenActivity extends Activity {
	private transient VideoView k2VideoView;
	private static final String K2Tag = "K2";
	private transient K2Playback k2Playback;

	public class CameraPreview extends SurfaceView {

		public CameraPreview(Context context) {
			super(context);
		}

	}

	@Override
	protected void onCreate(Bundle stateBundle) {

		EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", // Event
																				// category
																				// (required)
				"activity_launch", // Event action (required)
				"fullscreen_acivity", // Event label
				null) // Event value
				.build());
		super.onCreate(stateBundle);
		setContentView(R.layout.activity_videofullscreen);

		k2VideoView = (VideoView) findViewById(R.id.k2VideoView);

		startPlayback();

	}

	private void startPlayback() {

		Log.d(K2Tag, K2Constants.VIDEO_DIR_NAME);
		k2Playback = new K2Playback(FullscreenActivity.this,
				K2Constants.VIDEO_DIR_NAME);

		k2Playback.playFolder();
	}

	public VideoView getVideoView() {
		return this.k2VideoView;
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
		finish();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		startPlayback();
	}

	@Override
	protected void onStart() {
		EasyTracker.getInstance(this).activityStart(this);
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

}
