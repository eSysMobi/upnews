package mobi.esys.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mobi.esys.consts.ISConsts;
import mobi.esys.upnewshashtag.InstaLoginActivity;

/**
 * Created by Артем on 14.04.2015.
 */
public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            try {
                Thread.sleep(ISConsts.APP_START_DELAY);
            } catch (InterruptedException e) {
            }
            context.startActivity(new Intent(context, InstaLoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));

        }
    }
}
