package mobi.esys.upnews_online.instagram;

import android.os.AsyncTask;

import net.londatiga.android.instagram.InstagramRequest;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mobi.esys.upnews_online.UpnewsOnlineApp;
import mobi.esys.upnews_online.net.NetMonitor;


public class CheckInstaTagTask extends AsyncTask<String, Void, Boolean> {
    private transient String mHashTag;
    private transient UpnewsOnlineApp mApp;

    public CheckInstaTagTask(String hashTag, UpnewsOnlineApp app) {
        mHashTag = hashTag;
        mApp = app;
    }


    @Override
    protected Boolean doInBackground(String... params) {
        boolean status = false;
        String response = "";
        if (mHashTag.length() >= 2) {
            final InstagramRequest request = new InstagramRequest(params[0]);

            String edTag = mHashTag.substring(1).toLowerCase(Locale.ENGLISH);

            if (NetMonitor.isNetworkAvailable(mApp)) {
                try {
                    final List<NameValuePair> reqParams = new ArrayList<NameValuePair>(
                            1);
                    reqParams.add(new BasicNameValuePair("count", String
                            .valueOf(100)));
                    response = request.requestGet("/tags/" + edTag
                            + "/media/recent", reqParams);
                    if (isJSONValid(response)) {
                        JSONObject resObject = new JSONObject(response);
                        if (resObject.has("meta")
                                && resObject.getJSONObject("meta").has(
                                "error_type")) {
                            status = false;
                        } else {
                            status = true;
                        }
                    }
                } catch (Exception e) {
                    status = false;
                }
            }
        }

        return status;
    }

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
}
