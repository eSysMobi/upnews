package mobi.esys.network.monitoring;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import mobi.esys.upnewshashtag.UNHApp;

/**
 * Created by Артем on 14.04.2015.
 */
public class NetMonitor {
    public static final boolean isNetworkAvailable(UNHApp app) {
        Context context = app.getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo.State mobile = NetworkInfo.State.DISCONNECTED;
        if (mobileInfo != null) {
            mobile = mobileInfo.getState();
        }
        NetworkInfo wifiInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo.State wifi = NetworkInfo.State.DISCONNECTED;
        if (wifiInfo != null) {
            wifi = wifiInfo.getState();
        }
        boolean dataOnWifiOnly = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(
                        "data_wifi_only", true);
        if ((!dataOnWifiOnly && (mobile.equals(NetworkInfo.State.CONNECTED) || wifi
                .equals(NetworkInfo.State.CONNECTED)))
                || (dataOnWifiOnly && wifi.equals(NetworkInfo.State.CONNECTED))) {
            return true;
        } else {
            return false;
        }
    }
}
