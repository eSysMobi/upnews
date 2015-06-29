package mobi.esys.unl_new_api.un_api;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.greenrobot.event.EventBus;
import mobi.esys.unl_new_api.R;
import mobi.esys.unl_new_api.eventbus.GetFirstVideoEvent;
import mobi.esys.unl_new_api.eventbus.GetPlaylistFailure;
import mobi.esys.unl_new_api.eventbus.LoginFailureEvent;
import mobi.esys.unl_new_api.eventbus.LoginSuccessEvent;
import mobi.esys.unl_new_api.helpers.TimeHelper;
import mobi.esys.unl_new_api.model.UNPlaylist;
import mobi.esys.unl_new_api.model.UNVideo;
import mobi.esys.unl_new_api.model.UNVideoFile;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;


public class UNApi {
    private static UNAPIInterface unapiInterface;
    private static SharedPreferences unPrefs;
    private static String tokenName;
    private static EventBus mBus;
    private static Context mContext;
    private static UNVideo firstVideo;
    private static UNVideo nextVideo;


    public static void init() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://upnews.tv/dev-api")
                .build();
        unapiInterface = restAdapter.create(UNAPIInterface.class);
    }

    public static void setCurrentContext(Context context, EventBus bus) {
        unPrefs = context.getSharedPreferences("unPref", Context.MODE_PRIVATE);
        tokenName = context.getResources().getString(R.string.tokenRes);
        mBus = bus;
        mContext = context;
    }

    public static void auth(String login, String password) {
        unapiInterface.auth(login, password, new Callback<JsonElement>() {

                    @Override
                    public void success(JsonElement response, Response response2) {
                        Logger.d(response2.getUrl());
                        JsonObject resObj = response.getAsJsonObject();
                        String token = resObj.get("_token").getAsString();
                        if (!Strings.isNullOrEmpty(token)) {
                            SharedPreferences.Editor editor = unPrefs.edit();
                            editor.putString(tokenName, token);
                            editor.apply();
                        }
                        Logger.d(token);
                        mBus.post(new LoginSuccessEvent(token));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Logger.d(error.getUrl());
                        Logger.d(error.toString());
                        mBus.post(new LoginFailureEvent());
                    }
                }

        );
    }

    public static List<UNPlaylist> getPlaylists() {
        final List<UNPlaylist> playlists = new ArrayList<>();
        String token = unPrefs.getString(tokenName, "");
        Logger.d(token);
        unapiInterface.getPlaylist(token, new Callback<JsonElement>() {
            @Override
            public void success(JsonElement response, Response response2) {
                Logger.d(response2.getUrl());
                Gson gson = new Gson();
                JsonObject resObj = response.getAsJsonObject();
                JsonArray resArray = resObj.get("result").getAsJsonArray();
                for (int i = 0; i < resArray.size(); i++) {
                    JsonObject currObj = resArray.get(i).getAsJsonObject();
                    playlists.add(gson.fromJson(currObj, UNPlaylist.class));
                }

                Logger.d(playlists.toString());
                getFirsVideo(playlists.get(0));
            }

            @Override
            public void failure(RetrofitError error) {
                mBus.post(new GetPlaylistFailure());
            }
        });

        return playlists;
    }

    public static List<UNVideo> getPlaylistVideos(final String playlistID) {
        String token = unPrefs.getString(tokenName, "");
        final List<UNVideo> unVideos = new ArrayList<>();
        unapiInterface.getPlaylistVideos(playlistID, token, new Callback<JsonElement>() {
                    @Override
                    public void success(JsonElement jsonElement, Response response) {
                        Logger.d(response.getUrl());
                        JsonObject resObj = jsonElement.getAsJsonObject().get("result").getAsJsonObject();
                        JsonArray videosArray = resObj.get("videos").getAsJsonArray();
                        Logger.json(resObj.toString());
                        Logger.json(videosArray.toString());
                        for (int i = 0; i < videosArray.size(); i++) {
                            JsonObject currObj = videosArray.get(i).getAsJsonObject();
                            JsonObject fileObj = currObj.get("file").getAsJsonObject();
                            Gson gson = new Gson();

                            Logger.json(fileObj.toString());
                            Logger.d(currObj.get("id").getAsString());
                            Logger.d(currObj.get("name").getAsString());
                            Logger.d(currObj.get("file_webpath").getAsString());

                            unVideos.add(new UNVideo(currObj.get("id").getAsString(),
                                    currObj.get("name").getAsString(),
                                    currObj.get("file_webpath").getAsString(),
                                    gson.fromJson(fileObj, UNVideoFile.class),
                                    currObj.get("videosettings").getAsJsonObject().get("video_order").getAsInt()));

                            Logger.d(unVideos.toString());
                        }


                        Collections.sort(unVideos, new Comparator<UNVideo>() {
                            @Override
                            public int compare(UNVideo lhs, UNVideo rhs) {
                                return lhs.getUnOrderNum() - rhs.getUnOrderNum();
                            }
                        });

                        Gson gson = new Gson();
                        List<UNVideo> textList = new ArrayList<>();
                        textList.addAll(unVideos);
                        String jsonText = gson.toJson(textList);

                        SharedPreferences.Editor editor = unPrefs.edit();

                        editor.putString("pl", jsonText);
                        editor.apply();

                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                }

        );

        Collections.sort(unVideos, new Comparator<UNVideo>() {
            @Override
            public int compare(UNVideo lhs, UNVideo rhs) {
                return lhs.getUnOrderNum() - rhs.getUnOrderNum();
            }
        });

        return unVideos;
    }

    public static UNVideo getFirsVideo(final UNPlaylist playlist) {
        String token = unPrefs.getString(tokenName, "");

        unapiInterface.getFirstVideo(token, playlist.getUnPlaylistID(), new Callback<JsonElement>() {
            @Override
            public void success(JsonElement json, Response response) {
                JsonObject resObj = json.getAsJsonObject().get("result").getAsJsonObject();
                JsonObject fileObj = resObj.get("file").getAsJsonObject();
                Gson gson = new Gson();
                firstVideo = new UNVideo(resObj.get("id").getAsString(),
                        resObj.get("name").getAsString(),
                        resObj.get("file_webpath").getAsString(),
                        gson.fromJson(fileObj, UNVideoFile.class), 1);
                Logger.d(firstVideo.toString());
                mBus.post(new GetFirstVideoEvent(firstVideo, playlist));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
        return firstVideo;
    }

    public static UNVideo getNextVideo(String playlistID, String videoID, int playedTimes, final int orderNumber) {
        String token = unPrefs.getString(tokenName, "");
        String timeStamp = TimeHelper.getCurrentTimeStamp();

        unapiInterface.getNextVideo(token, playlistID, videoID, playedTimes, timeStamp, new Callback<JsonElement>() {
            @Override
            public void success(JsonElement jsonElement, Response response) {
                Logger.d("next video ".concat(response.getUrl()));
                Logger.json(jsonElement.toString());
                if (!jsonElement.getAsJsonObject().get("result").isJsonNull()) {
                    JsonObject resObject = jsonElement.getAsJsonObject().get("result").getAsJsonObject();
                    JsonObject fileObj = resObject.get("file").getAsJsonObject();
                    Gson gson = new Gson();

                    nextVideo = new UNVideo(resObject.get("id").getAsString(),
                            resObject.get("name").getAsString(),
                            resObject.get("file_webpath").getAsString(),
                            gson.fromJson(fileObj, UNVideoFile.class), orderNumber);

                    Logger.d("next video: ".concat(nextVideo.toString()));

                    SharedPreferences.Editor editor = unPrefs.edit();
                    editor.putInt("nextVideoIndex", nextVideo.getUnOrderNum());
                    editor.apply();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Logger.d("next video " + error.toString());
            }
        });

        return nextVideo;
    }

    public static void sendPhoto(File photo, int countFaces, String videoName) {
        String token = unPrefs.getString(tokenName, "");
        TypedFile photoTypedFile = new TypedFile("image/jpeg", photo);
        unapiInterface.sendPhotoToServer(token, photoTypedFile, countFaces, videoName, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public static void sendData(String batteryChargeLevel, String signalLevel, String powerSupply, String lat, String lon, String videoName, String deviceID) {
        String token = unPrefs.getString(tokenName, "");
        unapiInterface.sendDataToServer(token, batteryChargeLevel, signalLevel, powerSupply, lat, lon, videoName, deviceID, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                Logger.d(response.getUrl());
                Logger.d(response.getBody().toString());
            }

            @Override
            public void failure(RetrofitError error) {
                Logger.d(error.getUrl());
                Logger.d(error.getBody().toString());
                Logger.d(error.getMessage());
            }
        });
    }


}
