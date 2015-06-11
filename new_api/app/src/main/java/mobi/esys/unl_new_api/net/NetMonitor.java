package mobi.esys.unl_new_api.net;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import mobi.esys.unl_new_api.UNApp;


public class NetMonitor {
    public static boolean isNetworkAvailable(UNApp app) {
        Context context = app.getApplicationContext();
        boolean status = false;

        final ConnectivityManager connManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != connManager) {
            NetworkInfo[] allNetworks = connManager.getAllNetworkInfo();
            if (null != allNetworks) {
                for (NetworkInfo info : allNetworks) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        status = true;
                        break;
                    }
                }
            }
        }
        return status;
    }
}