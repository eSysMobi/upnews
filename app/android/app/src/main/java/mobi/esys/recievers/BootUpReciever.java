package mobi.esys.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mobi.esys.upnews.SplashActivity;

/**
 * Created by Артем on 19.01.2015.
 */
public class BootUpReciever extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
            context.startActivity(new Intent(context, SplashActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        }
    }
}
