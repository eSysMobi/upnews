package mobi.esys.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.google.api.client.http.GenericUrl;
import com.google.api.services.drive.Drive;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import mobi.esys.consts.ISConsts;
import mobi.esys.googledrive.model.model.GDFile;
import mobi.esys.upnewshashtag.UNHApp;
import mobi.esys.network.monitoring.NetMonitor;
import mobi.esys.server.UNHServer;
import mobi.esys.system.StreamsUtils;

/**
 * Created by Артем on 14.04.2015.
 */
public class RSSTask extends AsyncTask<Void, Void, String> {
    private transient final SharedPreferences prefs;
    private transient Context mContext;
    private transient String mActName;
    private transient UNHServer server;
    private transient Drive drive;
    private transient UNHApp mApp;

    public RSSTask(Context context, String actName, UNHApp app) {
        mApp = app;
        prefs = app.getApplicationContext().getSharedPreferences(ISConsts.PREF_PREFIX, Context.MODE_PRIVATE);
        mContext = context;
        mActName = actName;
        server = new UNHServer(app);
        drive = app.getDriveService();

    }


    @Override
    protected String doInBackground(Void... params) {
        String rssURL = "";
        if (NetMonitor.isNetworkAvailable(mApp)) {
            server.getMD5FromServer();
            GDFile gdFile = server.getGdRSS();
            try {
                if (gdFile != null && gdFile.getGdFileInst().getDownloadUrl() != null) {
                    com.google.api.client.http.HttpResponse resp = drive
                            .getRequestFactory()
                            .buildGetRequest(
                                    new GenericUrl(gdFile.getGdFileInst().getDownloadUrl()))
                            .execute();
                    String cont = StreamsUtils.convertStreamToString(resp.getContent());

                    String[] lines = cont.split("\\n");
                    if (lines.length >= 2) {
                        rssURL = lines[1];
                    }
                } else {
                    cancel(true);
                }
            } catch (IOException e) {
                cancel(true);
                e.printStackTrace();
            }
        } else {
            cancel(true);
        }
        return rssURL;
    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (s != null && !s.isEmpty()) {
            try {
                URL url = new URL(s);
                RSSFeedTask rssFeedTask = new RSSFeedTask(mContext, mActName, mApp);
                rssFeedTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }


    }
}
