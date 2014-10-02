package mobi.esys.recievers;

import mobi.esys.upnewslite.StartActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
			context.startActivity(new Intent(context, StartActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

		}
	}
}
