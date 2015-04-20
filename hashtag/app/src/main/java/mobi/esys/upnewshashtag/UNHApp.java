package mobi.esys.upnewshashtag;

import android.app.Application;
import android.content.res.Configuration;

import com.google.api.services.drive.Drive;


/**
 * Created by Артем on 14.04.2015.
 */
public class UNHApp extends Application {
    private static Drive driveService;

    public void registerGoogle(Drive drive) {
        driveService = drive;
    }

    public static Drive getDriveService() {
        return driveService;
    }


    @Override
    public void onCreate() {
        super.onCreate();
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
}
