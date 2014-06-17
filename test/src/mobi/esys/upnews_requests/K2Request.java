package mobi.esys.upnews_requests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import mobi.esys.constants.K2Constants;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

public class K2Request {

	private static final String SEND_TAG = "send";
	private static final String RESP_TAG = "resp";
	private static InputStream inputStream;

	public static JSONObject getJSONFromURL(final String method,
			final String postfix) {

		inputStream = new InputStream() {

			@Override
			public int read() {
				return 0;
			}
		};
		String result = "";
		JSONObject jsonObject = new JSONObject();

		try {

			inputStream = new DefaultHttpClient()
					.execute(
							new HttpGet(K2Constants.VIDEO_URLPREFIX + method
									+ K2Constants.GET_TYPE + postfix))
					.getEntity().getContent();

			final BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputStream));
			final StringBuilder stringBuilder = new StringBuilder();
			String line = "";

			while (line != null) {
				line = bufferedReader.readLine();
				stringBuilder.append(line).append('\n');
			}

			inputStream.close();

			result = stringBuilder.toString();

			jsonObject = new JSONObject(result);
			Log.d("result", jsonObject.toString());
		} catch (Exception Exception) {

		}

		return jsonObject;
	}

	// "http://icu.im/rhymevideo/api/sendplaylist.json"
	public void sendDataToServer(final Bundle sendParams, final String url) {

		final HttpClient httpclient = new DefaultHttpClient();
		final HttpPost httppost = new HttpPost(url);
		Log.d(SEND_TAG, SEND_TAG);
		Log.d(SEND_TAG, url);

		try {
			Log.d(SEND_TAG, sendParams.toString());
			Log.d("params", bundle2nameValuePairs(sendParams).toString());
			httppost.setEntity(new UrlEncodedFormEntity(
					bundle2nameValuePairs(sendParams)));
			final HttpResponse response = httpclient.execute(httppost);
			Log.d(RESP_TAG, response.toString());

		} catch (UnsupportedEncodingException e) {
			Log.d(RESP_TAG, "uee");

		} catch (ClientProtocolException e) {
			Log.d(RESP_TAG, "cpe");

		} catch (IOException e) {
			Log.d(RESP_TAG, "ioe");
		}
	}

	public List<NameValuePair> bundle2nameValuePairs(final Bundle procBundle) {
		final List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (final String key : procBundle.keySet()) {
			nameValuePairs.add(new BasicNameValuePair(key, String
					.valueOf(procBundle.get(key))));
		}
		return nameValuePairs;
	}

}
