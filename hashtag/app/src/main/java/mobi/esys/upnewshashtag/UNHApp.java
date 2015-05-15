package mobi.esys.upnewshashtag;

import android.app.Application;
import android.content.res.Configuration;
import android.os.Environment;

import java.io.File;

import mobi.esys.consts.ISConsts;


/**
 * Created by Артем on 14.04.2015.
 */
public class UNHApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        createFoldersIfNotExist();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private void createFoldersIfNotExist() {
        File dir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(ISConsts.globals.dir_name));
        File photoDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(ISConsts.globals.dir_name).concat(ISConsts.globals.photo_dir_name));

        if (!dir.exists()) {
            dir.mkdir();
        }
        if (!photoDir.exists()) {
            photoDir.mkdir();
        }

    }


}
