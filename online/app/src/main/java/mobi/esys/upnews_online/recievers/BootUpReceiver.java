package mobi.esys.upnews_online.recievers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mobi.esys.upnews_online.LoginActivity;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ignored) {
            }
            context.startActivity(new Intent(context, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));

        }
    }
}
