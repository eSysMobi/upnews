package mobi.esys.upnewslite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StartActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		Thread timer = new Thread() {
			public void run() {
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					Intent i = new Intent(StartActivity.this,
							DriveAuthActivity.class);
					finish();
					startActivity(i);
				}
			}
		};
		timer.start();
	}
}
