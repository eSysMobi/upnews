package mobi.esys.unl_new_api.helpers;

import android.app.Activity;

import com.squareup.leakcanary.RefWatcher;

import mobi.esys.unl_new_api.UNApp;


public class UNActivityHelper {
    public static void initUNActivity(Activity activity) {
        initCanary(activity);
    }


    private static void initCanary(Activity activity) {
        RefWatcher refWatcher = UNApp.getRefWatcher(activity);
        refWatcher.watch(activity);
    }
}
