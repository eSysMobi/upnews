package mobi.esys.tasks;

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

import mobi.esys.consts.ISConsts;
import mobi.esys.network.monitoring.NetMonitor;
import mobi.esys.upnewshashtag.UNHApp;

/**
 * Created by Артем on 29.04.2015.
 */
public class CheckInstaTagTask extends AsyncTask<String, Void, Boolean> {
    private transient String mHashTag;
    private transient UNHApp mApp;

    public CheckInstaTagTask(String hashTag, UNHApp app) {
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
                            .valueOf(ISConsts.instagramconsts.instagram_page_count)));
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
