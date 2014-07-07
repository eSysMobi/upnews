package mobi.esys.upnews;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

//App key
//i1td6fhy5vt6sck
//App secret
//ci8n84oycoszzml
//eDbzX5vSotEAAAAAAAAy-kUkNhXqF13NKwvNVjenncnG7e_uPpXlp2AxCMuhheFj
public class LoginActivity extends Activity {
	final static private String APP_KEY = "i1td6fhy5vt6sck";
	final static private String APP_SECRET = "ci8n84oycoszzml";
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	private transient DropboxAPI<AndroidAuthSession> mDBApi;
	private transient ImageView dropBoxBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		@SuppressWarnings("deprecation")
		AndroidAuthSession session = new AndroidAuthSession(appKeys,
				ACCESS_TYPE);
		mDBApi = new DropboxAPI<AndroidAuthSession>(session);

		dropBoxBtn = (ImageView) findViewById(R.id.dropBoxBtn);
		dropBoxBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDBApi.getSession().startOAuth2Authentication(
						LoginActivity.this);
			}
		});
	}

	protected void onResume() {
		super.onResume();

		if (mDBApi.getSession().authenticationSuccessful()) {
			try {
				mDBApi.getSession().finishAuthentication();
				String accessToken = mDBApi.getSession().getOAuth2AccessToken();
				Log.d("dropboxAT", accessToken);
				if (!accessToken.equals("")) {
					startActivity(new Intent(LoginActivity.this,
							SplashActivity.class));
				}
			} catch (IllegalStateException e) {
				Log.i("DbAuthLog", "Error authenticating", e);
			}
		}
	}
}
