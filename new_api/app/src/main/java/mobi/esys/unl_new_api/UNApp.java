package mobi.esys.unl_new_api;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.orhanobut.logger.Logger;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import net.danlew.android.joda.JodaTimeAndroid;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.res.StringRes;

import mobi.esys.unl_new_api.filesystem.FoldersHelper;
import mobi.esys.unl_new_api.un_api.UNApi;

@EApplication
public class UNApp extends Application {
    @StringRes
    String baseDir;
    @StringRes
    String photoDir;
    @StringRes
    String videoDir;

    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    public static RefWatcher getRefWatcher(Context context) {
        UNApp application = (UNApp) context.getApplicationContext();
        return application.refWatcher;
    }

    private void createFolders() {
        FoldersHelper baseHelper = new FoldersHelper(baseDir, "");
        FoldersHelper photoHelper = new FoldersHelper(photoDir, baseDir);
        FoldersHelper videoHelper = new FoldersHelper(videoDir, baseDir);

        baseHelper.createFolder();
        photoHelper.createFolder();
        videoHelper.createFolder();
    }

    public String getDeviceID() {
        return Build.SERIAL;
    }


    @Background
    void init() {
        refWatcher = LeakCanary.install(this);
        UNApi.init();
        createFolders();
        JodaTimeAndroid.init(this);
        Logger.init("upnews");
    }
}
