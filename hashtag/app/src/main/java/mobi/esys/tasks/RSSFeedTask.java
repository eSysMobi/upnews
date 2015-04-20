package mobi.esys.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.net.URL;

//import mobi.esys.upnewshashtag.MainSliderActivity;
import mobi.esys.upnewshashtag.SliderActivity;
import mobi.esys.upnewshashtag.UNHApp;
import mobi.esys.network.monitoring.NetMonitor;
import mobi.esys.rss.RSS;

/**
 * Created by Артем on 14.04.2015.
 */
public class RSSFeedTask extends AsyncTask<URL, Void, String> {
    private transient String mActName;
    private transient UNHApp mApp;
    private transient Context mContext;

    public RSSFeedTask(Context context, String actName, UNHApp app) {
        mContext = context;
        mActName = actName;
        mApp = app;
    }


    @Override
    protected String doInBackground(URL... params) {
        String feed = "";
        if (NetMonitor.isNetworkAvailable(mApp)) {
            feed = RSS.parseRSSURL(params[0], " <font color='red'> | </font> ");
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
                ((SliderActivity) mContext).startRSS(s);
                ((SliderActivity) mContext).recToMP("rss_start", "Start rss feed");
            } else {
//                ((MainSliderActivity) mContext).startRSS(s);
//                ((MainSliderActivity) mContext).recToMP("rss_start", "Start rss feed");

            }
        }
    }

}
