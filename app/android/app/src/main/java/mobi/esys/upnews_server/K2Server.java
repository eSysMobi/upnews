package mobi.esys.upnews_server;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import mobi.esys.constants.K2Constants;
import mobi.esys.upnews_requests.K2Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

public class K2Server {
	private transient Context context;
	Set<String> set;

	public K2Server(Context context) {
		this.context = context;
	}

	public String[] getMD5FromServer() {
		Log.d("server", "get md5s from server");
		SharedPreferences preferences = context.getSharedPreferences(
				K2Constants.APP_PREF, Context.MODE_PRIVATE);
		Set<String> defaultSet = new LinkedHashSet<String>();
		defaultSet.add(K2Constants.FIRST_MD5);
		Set<String> md5Sset = preferences.getStringSet("md5sApp", defaultSet);
		String[] md5FromServer = md5Sset.toArray(new String[md5Sset.size()]);
		String[] resultMd5 = { "" };

		JSONObject jsonObject = K2Request.getJSONFromURL("videolist", "");

		String[] md5JSON = new String[1];
		JSONArray array;
		try {
			array = jsonObject.getJSONArray("result");

			md5JSON = new String[array.length()];

			for (int i = 0; i < array.length(); i++) {
				JSONObject currVidList = array.getJSONObject(i);
				md5JSON[i] = currVidList.getJSONObject("file").getString("md5");
			}

			set = new LinkedHashSet<String>(Arrays.asList(md5JSON));
			Log.d("set", set.toString());
			if (isOnline()) {
				Log.d("online", "online");
				if (!Arrays.deepEquals(md5FromServer, md5JSON)) {
					Log.d("pllt", "playlist change");
					saveURLS(jsonObject);
					Set<String> md5Set = new LinkedHashSet<String>(
							Arrays.asList(md5JSON));
					resultMd5 = new String[md5JSON.length];
					for (int i = 0; i < resultMd5.length; i++) {
						resultMd5[i] = md5JSON[i];
					}
					SharedPreferences.Editor editor = preferences.edit();
					editor.putStringSet("md5sApp", md5Set);
					editor.commit();
				}

				else {
					Log.d("online", "don't online");
					Log.d("pllt", "playlist don't change");
					Set<String> md5Set = new LinkedHashSet<String>(
							Arrays.asList(md5FromServer));
					resultMd5 = new String[md5FromServer.length];
					for (int i = 0; i < resultMd5.length; i++) {
						resultMd5[i] = md5FromServer[i];
					}
					SharedPreferences.Editor editor = preferences.edit();
					editor.putStringSet("md5sApp", md5Set);
					editor.commit();
				}
			} else {
				Log.d("pllt", "playlist don't change");
				Set<String> md5Set = new LinkedHashSet<String>(
						Arrays.asList(md5FromServer));
				resultMd5 = new String[md5FromServer.length];
				for (int i = 0; i < resultMd5.length; i++) {
					resultMd5[i] = md5FromServer[i];
				}
				SharedPreferences.Editor editor = preferences.edit();
				editor.putStringSet("md5sApp", md5Set);
				editor.commit();
			}
		} catch (JSONException e) {
			Log.d("je", "je");
		}

		return resultMd5;
	}

	private void saveURLS(JSONObject serverObject) {
		Log.d("je", "sU");

		SharedPreferences.Editor editor = context.getSharedPreferences(
				K2Constants.APP_PREF, Context.MODE_PRIVATE).edit();
		JSONArray array;
		String[] urlsJSON = { "" };
		SharedPreferences preferences = context.getSharedPreferences(
				K2Constants.APP_PREF, Context.MODE_PRIVATE);
		String[] urlsFromServer = preferences.getString("urls", "")
				.replace("[", "").replace("]", "").split(",");
		Log.d("urls from server", Arrays.asList(urlsFromServer).toString());
		try {

			if (isOnline()) {

				Log.d("online", "online");
				array = serverObject.getJSONArray("result");
				urlsJSON = new String[array.length()];

				for (int i = 0; i < array.length(); i++) {
					JSONObject currVidList = array.getJSONObject(i);

					urlsJSON[i] = currVidList.getString("file_webpath");
				}

				if (!Arrays.deepEquals(urlsFromServer, urlsJSON)) {
					Log.d("urls request", Arrays.asList(urlsJSON).toString());
					editor.putString("urls", Arrays.asList(urlsJSON).toString());
					editor.commit();
				} else {
					editor.putString("urls", Arrays.asList(urlsFromServer)
							.toString());
					editor.commit();
				}

			} else {
				editor.putString("urls", Arrays.asList(urlsFromServer)
						.toString());
				editor.commit();
			}

		} catch (JSONException e) {
			Log.d("je", "je");
		}

	}

	public void sendDataToServer(Bundle sandParams) {
		K2Request k2Request = new K2Request();
		k2Request.sendDataToServer(sandParams, K2Constants.VIDEO_URLPREFIX
				+ K2Constants.SEND_DATA_METHOD_NAME + K2Constants.GET_TYPE);
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

}
