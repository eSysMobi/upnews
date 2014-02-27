package mobi.esys.upnews;

import mobi.esys.constants.K2Constants;
import mobi.esys.playback.K2Playback;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.VideoView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class FullscreenActivity extends Activity implements LocationListener {
	private transient VideoView k2VideoView;
	private static final String K2Tag = "K2";
	private transient K2Playback k2Playback;
	private transient Bundle sendBundle;
	private transient TelephonyManager tel;
	PhoneStateListener phoneStateListener;

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
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = FullscreenActivity.this.registerReceiver(null,
				ifilter);

		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
				|| status == BatteryManager.BATTERY_STATUS_FULL;

		sendBundle.putString("battery_charge_level", String.valueOf(level));
		sendBundle.putString("power_supply", String.valueOf(isCharging));
	}

	private void initLocationGetting() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 600000, 0,
				this);
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
		EasyTracker.getInstance(this).activityStop(this);
		tel.listen(phoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		k2Playback.pausePlayback();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		tel.listen(phoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		k2Playback.stopPlayback();
	}

	@Override
	protected void onPause() {
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
		tel.listen(phoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		k2Playback.resumePlayback();
	}

	public Bundle sendParams() {
		return sendBundle;

	}

	@Override
	public void onLocationChanged(Location arg0) {
		sendBundle.putString("latitude", String.valueOf(arg0.getLatitude()));
		sendBundle.putString("longitude", String.valueOf(arg0.getLongitude()));
	}

	@Override
	public void onProviderDisabled(String arg0) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	private class SignalStrengthListener extends PhoneStateListener {
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			sendBundle.putString("signal_level",
					String.valueOf(signalStrength.getGsmSignalStrength()));
			super.onSignalStrengthsChanged(signalStrength);
		}
	}
}
