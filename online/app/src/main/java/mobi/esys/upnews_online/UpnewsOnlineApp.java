package mobi.esys.upnews_online;

import android.app.Activity;
import android.app.Application;


public class UpnewsOnlineApp extends Application {
    private transient Activity currentActivityInstance;


    public Activity getCurrentActivityInstance() {
        return currentActivityInstance;
    }

    public void setCurrentActivityInstance(Activity currentActivityInstance) {
        this.currentActivityInstance = currentActivityInstance;
    }
}
