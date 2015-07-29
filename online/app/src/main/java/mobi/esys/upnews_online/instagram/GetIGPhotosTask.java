package mobi.esys.upnews_online.instagram;


import android.os.AsyncTask;

import net.londatiga.android.instagram.InstagramRequest;

public class GetIGPhotosTask extends AsyncTask<String, Void, String> {
    private transient String userID;

    public GetIGPhotosTask(String userID) {
        this.userID = userID;
    }

    @Override
    protected String doInBackground(final String... params) {
        String response = "";

        final InstagramRequest request = new InstagramRequest(params[0]);

        try {
            response = request.requestGet("users/" + userID + "/media/recent/", null);
        } catch (Exception ignored) {
        }

        return response;
    }

}
