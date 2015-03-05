package mobi.esys.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.google.api.client.http.GenericUrl;
import com.google.api.services.drive.Drive;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import mobi.esys.constants.UNLConsts;
import mobi.esys.data.GDFile;
import mobi.esys.net.NetWork;
import mobi.esys.system.StremsUtils;
import mobi.esys.upnews_server.UNLServer;
import mobi.esys.upnewslite.UNLApp;

/**
 * Created by Артем on 26.02.2015.
 */
public class RSSTask extends AsyncTask<Void, Void, String> {
    private transient final SharedPreferences prefs;
    private transient Context mContext;
    private transient String mActName;
    private transient UNLServer server;
    private transient Drive drive;
    private transient UNLApp mApp;

    public RSSTask(Context context, String actName, UNLApp app) {
        mApp = app;
        prefs = app.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, Context.MODE_PRIVATE);
        mContext = context;
        mActName = actName;
        server = new UNLServer(app);
        drive = app.getDriveService();

    }

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p/>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected String doInBackground(Void... params) {
        String rssURL = "";
        if (NetWork.isNetworkAvailable(mApp)) {
            server.getMD5FromServer();
            GDFile gdFile = server.getGdRSS();
            try {
                if (gdFile != null && gdFile.getGdFileInst().getDownloadUrl() != null) {
                    com.google.api.client.http.HttpResponse resp = drive
                            .getRequestFactory()
                            .buildGetRequest(
                                    new GenericUrl(gdFile.getGdFileInst().getDownloadUrl()))
                            .execute();
                    String cont = StremsUtils.convertStreamToString(resp.getContent());

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
