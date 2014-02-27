package mobi.esys.upnews;

import java.lang.reflect.Method;

import mobi.esys.constants.K2Constants;
import mobi.esys.tasks.FirstFileLoader;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;

public class SplashActivity extends Activity implements LoaderCallbacks<String> {

	@Override
	protected void onCreate(Bundle stateBundle) {
		super.onCreate(stateBundle);
		setContentView(R.layout.activity_splash);
		Bundle loaderBundle = new Bundle();
		loaderBundle.putString("dir", K2Constants.VIDEO_DIR_NAME);
		getLoaderManager().initLoader(1, loaderBundle, this);
		String deviceID = "";

		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class);
			deviceID = (String) get.invoke(c, "ro.serialno");
		} catch (Exception ignored) {
		}

		SharedPreferences.Editor editor = getSharedPreferences(
				K2Constants.APP_PREF, MODE_PRIVATE).edit();
		editor.putBoolean("isDownload", false);
		editor.putString("device_id", deviceID);
		editor.commit();
	}

	@Override
	public Loader<String> onCreateLoader(int id, Bundle args) {
		Loader<String> fileLoader = new FirstFileLoader(SplashActivity.this,
				args);
		return fileLoader;
	}

	@Override
	public void onLoadFinished(Loader<String> arg0, String arg1) {
		startActivity(new Intent(SplashActivity.this, FullscreenActivity.class));
		finish();
	}

	@Override
	public void onLoaderReset(Loader<String> arg0) {

	}
}
