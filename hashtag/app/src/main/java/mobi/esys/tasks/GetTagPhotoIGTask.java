package mobi.esys.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

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

public class GetTagPhotoIGTask extends AsyncTask<String, Void, String> {
    private static final String TAG_ID = "tagID";
    private final transient Context mContext;
    private final transient String mode;
    private final transient String tag;
    private final transient SharedPreferences prefs;
    private final transient ProgressDialog dialog;
    private final transient boolean isShowProgress;
    private transient UNHApp mApp;
    private transient final String TAG = this.getClass().getSimpleName();

    public GetTagPhotoIGTask(final Context context,
                             final String mode, final String tag, final boolean isShowProgress, final UNHApp app) {
        super();
        this.mContext = context;
        this.mode = mode;
        this.tag = tag;
        this.prefs = context.getSharedPreferences("IPPrefs",
                Context.MODE_PRIVATE);
        this.isShowProgress = isShowProgress;
        this.dialog = new ProgressDialog(context);
        this.dialog.setCancelable(false);
        this.dialog.setIndeterminate(true);
        mApp = app;

    }

    @Override
    protected void onPreExecute() {
        if (isShowProgress) {
            this.dialog.setMessage("Çàãðóçêà ôîòîãðàôèé èç Instagram ïî òåãó "
                    + tag);
            if (!this.dialog.isShowing()) {
                this.dialog.show();
            }
        }
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(final String... params) {
        String response = "";
        if (tag.length() >= 2) {
            final InstagramRequest request = new InstagramRequest(params[0]);

            String edTag = tag.substring(1).toLowerCase(Locale.ENGLISH);

            if (NetMonitor.isNetworkAvailable(mApp)) {
                try {
                    if ("more".equals(mode)) {

                        Log.d("get ig photos mode: ", mode);

                        final List<NameValuePair> moreParams = new ArrayList<NameValuePair>(
                                2);
                        moreParams.add(new BasicNameValuePair("count", String
                                .valueOf(ISConsts.instagramconsts.instagram_page_count)));
                        moreParams.add(new BasicNameValuePair("max_tag_id",
                                prefs.getString(TAG_ID, "0")));

                        response = request.requestGet("/tags/" + edTag
                                + "/media/recent", moreParams);
                        if (isJSONValid(response)) {
                            JSONObject resObject = new JSONObject(response);
                            if (resObject.has("meta")
                                    && resObject.getJSONObject("meta").has(
                                    "error_type")) {

                            } else {
                                final JSONObject pageObject = resObject
                                        .getJSONObject("pagination");

                                final SharedPreferences.Editor editor = prefs
                                        .edit();
                                editor.putString(TAG_ID,
                                        pageObject.getString("next_max_tag_id"));
                                editor.commit();
                                Log.d("max tag id",
                                        prefs.getString(TAG_ID, "0"));

                            }
                        }

                    } else {
                        Log.d("get ig photos mode: ", mode);
                        final List<NameValuePair> reqParams = new ArrayList<>(
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

                                Handler handler = new Handler(
                                        mContext.getMainLooper());
                                handler.post(new Runnable() {
                                    public void run() {

                                        if (GetTagPhotoIGTask.this.dialog
                                                .isShowing()) {
                                            GetTagPhotoIGTask.this.dialog
                                                    .dismiss();
                                        }
                                    }
                                });
                            } else {
                                final JSONObject pageObject = resObject
                                        .getJSONObject("pagination");
                                final SharedPreferences.Editor editor = prefs
                                        .edit();
                                editor.putString(TAG_ID,
                                        pageObject.getString("next_max_tag_id"));
                                editor.commit();
                                Log.d("max tag id",
                                        prefs.getString(TAG_ID, "0"));

                            }
                        }

                    }
                } catch (Exception e) {
                }
            } else {
                Handler handler = new Handler(mContext.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {

                        if (GetTagPhotoIGTask.this.dialog.isShowing()) {
                            GetTagPhotoIGTask.this.dialog.dismiss();
                        }
                    }
                });
            }

            Log.d("grid", response);
        }
        return response;
    }

    @Override
    protected void onPostExecute(final String result) {
        super.onPostExecute(result);
        if (mode.equals("default")) {
            Handler handler = new Handler(mContext.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    if (GetTagPhotoIGTask.this.dialog.isShowing()) {
                        GetTagPhotoIGTask.this.dialog.dismiss();
                    }
                }
            });

        } else if (mode.equals("update")) {

            Handler handler = new Handler(mContext.getMainLooper());
            handler.post(new Runnable() {
                public void run() {

                    if (GetTagPhotoIGTask.this.dialog.isShowing()) {
                        GetTagPhotoIGTask.this.dialog.dismiss();
                    }
                }
            });

        }
        if (isShowProgress && this.dialog.isShowing()) {
            this.dialog.dismiss();
        }

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
