package mobi.esys.upnews_server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mobi.esys.constants.K2Constants;
import mobi.esys.upnews_requests.K2Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class K2Server {
	private transient final Context context;
	private transient Set<String> set;

	public K2Server(Context context) {
		this.context = context;
	}

	public String[] getMD5FromServer() {
		SharedPreferences preferences = context.getSharedPreferences(
				K2Constants.APP_PREF, Context.MODE_PRIVATE);
		final Set<String> defaultSet = new HashSet<String>();
		defaultSet.add(K2Constants.FIRST_MD5);
		final Set<String> md5Sset = preferences.getStringSet("md5sApp",
				defaultSet);
		String[] md5FromServer = md5Sset.toArray(new String[md5Sset.size()]);

		JSONObject jsonObject = K2Request.getJSONFromURL("videolist", "");

		String[] md5JSON = new String[1];
		JSONArray array;
		try {
			array = jsonObject.getJSONArray("result");

			md5JSON = new String[array.length()];

			for (int i = 0; i < array.length(); i++) {
				final JSONObject currVidList = array.getJSONObject(i);
				md5JSON[i] = currVidList.getJSONObject("file").getString("md5");
			}

			set = new HashSet<String>(Arrays.asList(md5JSON));
			Log.d("set", set.toString());
		} catch (JSONException e) {
			Log.d("je", "je");
		}

		if (!Arrays.deepEquals(md5FromServer, md5JSON) && md5JSON.length > 1) {
			saveURLS(jsonObject);
			final Set<String> md5Set = new HashSet<String>(
					Arrays.asList(md5JSON));
			final SharedPreferences.Editor editor = preferences.edit();
			editor.putStringSet("md5sApp", md5Set);
			editor.commit();
		}

		else {
			final Set<String> md5Set = new HashSet<String>(
					Arrays.asList(md5FromServer));
			final SharedPreferences.Editor editor = preferences.edit();
			editor.putStringSet("md5sApp", md5Set);
			editor.commit();
		}
		return md5FromServer;
	}

	private void saveURLS(JSONObject serverObject) {
		Log.d("je", "sU");
		final SharedPreferences.Editor editor = context.getSharedPreferences(
				K2Constants.APP_PREF, Context.MODE_PRIVATE).edit();
		JSONArray array;
		String[] urls = { "" };

		try {
			array = serverObject.getJSONArray("result");

			urls = new String[array.length()];

			for (int i = 0; i < array.length(); i++) {
				final JSONObject currVidList = array.getJSONObject(i);

				urls[i] = currVidList.getString("file_webpath");
			}
		} catch (JSONException e) {
			Log.d("je", "je");
		}

		List<String> urlList = new ArrayList<String>(Arrays.asList(urls));
		String list = urlList.toString().replace("[", "").replace("]", "");
		Log.d("server urls json", list);
		editor.putString("urls", list);
		editor.commit();
	}

	public void sendDataToServer(final Bundle sendParams) {
		K2Request k2Request = new K2Request();
		k2Request.sendDataToServer(sendParams, K2Constants.VIDEO_URLPREFIX
				+ K2Constants.SEND_DATA_METHOD_NAME + K2Constants.GET_TYPE);
	}

}
