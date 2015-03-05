package mobi.esys.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.net.URL;

import mobi.esys.net.NetWork;
import mobi.esys.rss.RSS;
import mobi.esys.upnewslite.FirstVideoActivity;
import mobi.esys.upnewslite.FullscreenActivity;
import mobi.esys.upnewslite.UNLApp;

/**
 * Created by Артем on 26.02.2015.
 */
public class RSSFeedTask extends AsyncTask<URL, Void, String> {
    private transient String mActName;
    private transient UNLApp mApp;
    private transient Context mContext;

    public RSSFeedTask(Context context, String actName, UNLApp app) {
        mContext = context;
        mActName = actName;
        mApp = app;
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
    protected String doInBackground(URL... params) {
        String feed = "";
        if (NetWork.isNetworkAvailable(mApp)) {
            feed = RSS.parseRSSURL(params[0], " *** ");
        } else {
            cancel(true);
        }
        return feed;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (s != null && !s.isEmpty()) {
            if ("first".equals(mActName)) {
                ((FirstVideoActivity) mContext).startRSS(s);
            } else {
                ((FullscreenActivity) mContext).startRSS(s);

            }
        }
    }


}
