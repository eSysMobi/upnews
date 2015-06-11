package mobi.esys.unl_new_api.un_api;

import com.google.gson.JsonElement;

import java.util.List;

import mobi.esys.unl_new_api.model.UNVideo;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;


public interface UNAPIInterface {
    @GET("/api/auth.json")
    void auth(@Query("login") String login, @Query("password") String password, Callback<JsonElement> authCB);

    @GET("/api/playlists.json")
    void getPlaylist(@Query("_token") String token, Callback<JsonElement> plCB);

    @GET("/api/nextvideo.json")
    void getNextVideo(@Query("_token") String token, @Query("playlist_id") String playlistID,
                      @Query("video_id") String videoID,
                      @Query("played_times") int playedTimes,
                      @Query("convertible_time") String timeStamp, Callback<JsonElement> nvCB);

    @GET("/api/playlist/{id}.json")
    void getPlaylistVideos(@Path("id") String playlistID, @Query("_token") String token, Callback<JsonElement> gpvCB);


    @GET("/api/videolist2.json")
    void getVideoList(@Query("_token") String token, Callback<List<UNVideo>> vlCB);

    @GET("/api/firstvideo.json")
    void getFirstVideo(@Query("_token") String token, @Query("playlist_id") String playlistID, Callback<JsonElement> fvCB);

    @GET("/api/mediaplayers.json")
    void getMediaPlayersList(@Query("_token") String token);

    @GET("/api/sendplaylist.json")
    void sendPlaylist(@Query("_token") String token);

    @GET("/api/senddata.json")
    void sendDataToServer(@Query("_token") String token,
                          @Query("battery_charge_level") String batteryChargeLevel,
                          @Query("signal_level") String signalLevel,
                          @Query("power_supply") String powerSupply,
                          @Query("latitude") String lat,
                          @Query("longitude") String lon,
                          @Query("video_name") String videoName,
                          @Query("device_id") String deviceID, Callback<Response> sdtsCB);

    @Multipart
    @POST("/api/sendphoto.json")
    void sendPhotoToServer(@Part("_token") String token,
                           @Part("photo") TypedFile photo,
                           @Part("count_faces") int countFaces,
                           @Part("video_name") String videoName,
                           Callback<Response> spCB);
}
