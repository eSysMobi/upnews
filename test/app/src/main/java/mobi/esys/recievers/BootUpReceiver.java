package mobi.esys.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mobi.esys.constants.UNLConsts;
import mobi.esys.upnewslite.DriveAuthActivity;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            try {
                Thread.sleep(UNLConsts.APP_START_DELAY);
            } catch (InterruptedException e) {
            }
            context.startActivity(new Intent(context, DriveAuthActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));

        }
    }
}
