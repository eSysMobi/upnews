package mobi.esys.unl_new_api.receivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.androidannotations.annotations.EReceiver;

import mobi.esys.unl_new_api.RegActivity_;

@EReceiver
public class BootUpReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Log.d("boot error", "Interrupt");
            }
        }
        context.startActivity(new Intent(context, RegActivity_.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
