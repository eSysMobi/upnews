package mobi.esys.upnews;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import mobi.esys.constants.K2Constants;
import mobi.esys.playback.K2Playback;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

@SuppressLint({ "NewApi", "SimpleDateFormat" })
public class FullscreenActivity extends Activity implements LocationListener,
		SurfaceHolder.Callback, PictureCallback {
	private static final String K2TAG = "K2Fullcsreen ";
	private static final String VIEWS_TAG = K2TAG + "views";
	private static final String PICTURE_TAG = K2TAG + "picture";
	private static final String ASYNC_TAG = K2TAG + "async";
	private static final int NO_FRONT_CAMERA = -1;

	private transient boolean previewIsRunning = false;
	private transient boolean isTakingPicture = false;

	private transient Camera camera;

	private transient VideoView k2VideoView;

	private transient K2Playback k2Playback;

	private transient Bundle sendBundle;

	private transient TelephonyManager tel;

	private transient PhoneStateListener phoneStateListener;

	private transient SurfaceHolder holder;

	private transient Handler handler;

	private transient SurfaceView surfaceView;

	private transient RelativeLayout fullscreenLayout;

	private transient FrameLayout surfaceHodler;

	private transient int cameraId = 0;

	public class CameraPreview extends SurfaceView {

		public CameraPreview(final Context context) {
			super(context);
		}

	}

	@Override
	protected void onCreate(final Bundle stateBundle) {
		surfaceView = new CameraPreview(FullscreenActivity.this);
		EasyTracker.getInstance(this).send(MapBuilder.createEvent("ui_action", // Event
																				// category
																				// (required)
				"activity_launch", // Event action (required)
				"fullscreen_acivity", // Event label
				null) // Event value
				.build());
		super.onCreate(stateBundle);
		setContentView(R.layout.activity_videofullscreen);

		fullscreenLayout = (RelativeLayout) findViewById(R.id.fullscreenLayout);
		surfaceHodler = (FrameLayout) findViewById(R.id.surfaceHolder);
		surfaceHodler.addView(surfaceView);
		holder = surfaceView.getHolder();

		holder.addCallback(FullscreenActivity.this);

		sendBundle = new Bundle();

		k2VideoView = (VideoView) findViewById(R.id.k2VideoView);

		getData();

		startPlayback();

	}

	private void getData() {
		initLocationGetting();
		getBatteryData();
		getSignal();
	}

	private void getSignal() {
		phoneStateListener = new SignalStrengthListener();
		tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		tel.listen(phoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}

	private void getBatteryData() {
		final IntentFilter ifilter = new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED);
		final Intent batteryStatus = FullscreenActivity.this.registerReceiver(
				null, ifilter);

		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

		final boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
				|| status == BatteryManager.BATTERY_STATUS_FULL;

		sendBundle.putString("battery_charge_level", String.valueOf(level));
		sendBundle.putString("power_supply", String.valueOf(isCharging));
	}

	private void initLocationGetting() {
		final LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
				600000, 0, this);
	}

	private void startPlayback() {

		Log.d(K2TAG, K2Constants.VIDEO_DIR_NAME);
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
		EasyTracker.getInstance(this).activityStop(this);
		tel.listen(phoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		k2Playback.pausePlayback();
	}

	@Override
	protected void onDestroy() {
		tel.listen(phoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		k2Playback.stopPlayback();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		stopPreview();
		super.onPause();
		tel.listen(phoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		k2Playback.pausePlayback();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		tel.listen(phoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		k2Playback.pausePlayback();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		tel.listen(phoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
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
		startPreview();
		tel.listen(phoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		k2Playback.resumePlayback();
	}

	public Bundle sendParams() {
		return sendBundle;

	}

	@Override
	public void onLocationChanged(final Location arg0) {
		sendBundle.putString("latitude", String.valueOf(arg0.getLatitude()));
		sendBundle.putString("longitude", String.valueOf(arg0.getLongitude()));
	}

	@Override
	public void onProviderDisabled(final String arg0) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(final String provider, final int status,
			final Bundle extras) {

	}

	private class SignalStrengthListener extends PhoneStateListener {
		@Override
		public void onSignalStrengthsChanged(final SignalStrength signalStrength) {
			sendBundle.putString("signal_level",
					String.valueOf(signalStrength.getGsmSignalStrength()));
			super.onSignalStrengthsChanged(signalStrength);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		initCamera(holder);

	}

	private void initCamera(final SurfaceHolder holder) {
		cameraId = getFrontCameraId();
		if (cameraId != NO_FRONT_CAMERA) {
			try {
				camera = Camera.open(cameraId);

				final Camera.Parameters parameters = camera.getParameters();
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
 {

				}

				final List<String> flashModes = parameters
						.getSupportedFlashModes();
				if (flashModes != null
						&& flashModes
								.contains(Camera.Parameters.FLASH_MODE_OFF))
					parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

				List<String> whiteBalance = parameters
						.getSupportedWhiteBalance();
				if (whiteBalance != null
						&& whiteBalance
								.contains(Camera.Parameters.WHITE_BALANCE_AUTO)) {
					parameters
							.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
				}

				final List<String> focusModes = parameters
						.getSupportedFocusModes();
				if (focusModes != null
						&& focusModes
								.contains(Camera.Parameters.FOCUS_MODE_AUTO))
					parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

				final List<Camera.Size> sizes = parameters
						.getSupportedPictureSizes();
				if (sizes != null && sizes.size() > 0) {
					final Camera.Size size = sizes.get(0);
					parameters.setPictureSize(size.width, size.height);
				}

				final List<Camera.Size> previewSizes = parameters
						.getSupportedPreviewSizes();
				if (previewSizes != null) {
					final Camera.Size previewSize = previewSizes
							.get(previewSizes.size() - 1);
					parameters.setPreviewSize(previewSize.width,
							previewSize.height);
				}

				camera.setParameters(parameters);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
					camera.enableShutterSound(false);
			} catch (RuntimeException e) {
				return;
			}
		} else {
			return;
		}

		try {
			camera.setPreviewDisplay(holder);

		} catch (IOException ioe) {
			Log.d(K2TAG + "camera preview unset", ioe.getMessage());
		}
	}

	@Override
	public void surfaceChanged(final SurfaceHolder holder, final int format,
			final int width, final int height) {
		startPreview();
	}

	@Override
	public void surfaceDestroyed(final SurfaceHolder holder) {
		releaseCamera();
	}

	@Override
	public void onPictureTaken(final byte[] data, final Camera camera) {
		isTakingPicture = false;
		Log.d(VIEWS_TAG, String.valueOf(fullscreenLayout.getChildCount()));

		Log.d(PICTURE_TAG, "take pictrue");
		final File photo = new File(Environment.getExternalStorageDirectory()
				+ K2Constants.PHOTO_DIR_NAME, "upnews.jpg");

		if (photo.exists()) {
			photo.delete();
		}

		try {
			FileOutputStream fos = new FileOutputStream(photo.getPath());
			Log.d(PICTURE_TAG, "take pictrue " + photo.getPath());
			fos.write(data);
			fos.close();

		} catch (IOException e) {
			Log.e("PictureDemo", "Exception in photoCallback", e);
		}

	}

	private int getFrontCameraId() {
		final int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			final Camera.CameraInfo info = new Camera.CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
				return i;
		}
		return NO_FRONT_CAMERA;
	}

	private void startPreview() {
		Log.d("is preview", String.valueOf(previewIsRunning));
		if (!previewIsRunning && camera != null) {
			takePhoto();
			Log.d(PICTURE_TAG, "photo");

		}
	}

	private void takePhoto() {
		try {
			Log.d(ASYNC_TAG, "start");
			camera.startPreview();
			camera.autoFocus(new Camera.AutoFocusCallback() {
				@Override
				public void onAutoFocus(final boolean b, final Camera camera) {
					if (!isTakingPicture) {
						try {
							isTakingPicture = true;
							camera.setPreviewCallback(null);
							camera.takePicture(null, null,
									FullscreenActivity.this);
						} catch (RuntimeException e) {
						}
					}
				}
			});
			previewIsRunning = true;

			handler = new Handler();
			final Runnable runnable = new Runnable() {
				@Override
				public void run() {
					Log.d(VIEWS_TAG,
							String.valueOf(fullscreenLayout.getChildCount()));

					previewIsRunning = false;
					startPreview();

					Log.d(ASYNC_TAG, "surf");
					handler.postDelayed(this, 1800000);
				}
			};

			handler.postDelayed(runnable, 1800000);

		} catch (Exception e) {
			Log.d(ASYNC_TAG, "except");
		}
	}

	private void stopPreview() {
		if (!isTakingPicture && previewIsRunning && camera != null) {
			camera.stopPreview();
			previewIsRunning = false;
			Log.d(ASYNC_TAG, "stop");
		}
	}

	private void releaseCamera() {
		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
			Log.d(ASYNC_TAG, "release");
		}
	}
}
