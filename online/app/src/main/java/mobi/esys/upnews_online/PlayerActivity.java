package mobi.esys.upnews_online;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DigitalClock;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.danikula.videocache.HttpProxyCache;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import net.londatiga.android.instagram.Instagram;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import io.fabric.sdk.android.Fabric;
import mobi.esys.upnews_online.cbr.CurrenciesList;
import mobi.esys.upnews_online.cbr.GetCurrencies;
import mobi.esys.upnews_online.constants.DevelopersKeys;
import mobi.esys.upnews_online.facebook.FacebookVideoItem;
import mobi.esys.upnews_online.instagram.GetIGPhotosTask;
import mobi.esys.upnews_online.instagram.InstagramItem;
import mobi.esys.upnews_online.twitter.TwitterHelper;
import zh.wang.android.apis.yweathergetter4a.WeatherInfo;
import zh.wang.android.apis.yweathergetter4a.YahooWeather;
import zh.wang.android.apis.yweathergetter4a.YahooWeatherExceptionListener;
import zh.wang.android.apis.yweathergetter4a.YahooWeatherInfoListener;

public class PlayerActivity extends Activity implements LocationListener, YahooWeatherInfoListener,
        YahooWeatherExceptionListener {
    private transient RelativeLayout relativeLayout;
    private transient Instagram instagram;
    private transient SliderLayout mSlider;
    private transient List<InstagramItem> igPhotos;
    private static final String URL = "url";
    private transient List<FacebookVideoItem> videoItems;
    private transient int videoIndex = 0;

    private transient VideoView playerView;

    private transient Location location;

    private transient ProgressDialog dialog;


    private YahooWeather mYahooWeather = YahooWeather.getInstance(5000, 5000, true);

    boolean isEuroUp = true;
    boolean isDollarUp = true;
    boolean isPoundUp = true;
    boolean isYenaUp = true;


    private transient LinearLayout dashLayout;
    private transient LinearLayout weatherLayout;

    private transient boolean isFirst = true;

    private transient SharedPreferences preferences;
    private boolean isStartVideo = true;
    private transient HttpProxyCache proxyCache;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        getWindow().setFormat(PixelFormat.TRANSPARENT);

        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("unoPref", MODE_PRIVATE);

        UpnewsOnlineApp app = (UpnewsOnlineApp) getApplicationContext();
        app.setCurrentActivityInstance(this);

        ImageView logo=(ImageView)findViewById(R.id.logo);
        logo.setLayerType(View.LAYER_TYPE_SOFTWARE, null);


        mYahooWeather.setExceptionListener(this);

        relativeLayout = (RelativeLayout) findViewById(R.id.playerID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            TextClock digitalClock = new TextClock(this);
            digitalClock.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 60);
            digitalClock.setTextColor(Color.WHITE);
            digitalClock.setBackgroundColor(Color.TRANSPARENT);
            RelativeLayout.LayoutParams dgLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                    , ViewGroup.LayoutParams.WRAP_CONTENT);
            dgLP.setMargins(20,0,0,0);
            dgLP.addRule(RelativeLayout.ALIGN_BASELINE, R.id.logo);
            digitalClock.setLayoutParams(dgLP);
            digitalClock.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            relativeLayout.addView(digitalClock);
        }
        else{
            DigitalClock digitalClock = new DigitalClock(this);
            digitalClock.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 60);
            digitalClock.setTextColor(Color.WHITE);
            digitalClock.setBackgroundColor(Color.TRANSPARENT);
            RelativeLayout.LayoutParams dgLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                    , ViewGroup.LayoutParams.WRAP_CONTENT);
            dgLP.setMargins(20,0,0,0);
            dgLP.addRule(RelativeLayout.ALIGN_BASELINE, R.id.logo);
            digitalClock.setLayoutParams(dgLP);
            digitalClock.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            relativeLayout.addView(digitalClock);
        }


        igPhotos = new ArrayList<>();
        videoItems = new ArrayList<>();


        dashLayout = new LinearLayout(this);
        weatherLayout = new LinearLayout(this);

        relativeLayout.addView(dashLayout);
        relativeLayout.addView(weatherLayout);


        dialog = new ProgressDialog(this);
        dialog.setTitle("");
        dialog.setMessage("Buffering...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);


        playerView = (VideoView) findViewById(R.id.video);

        MediaController mediaController = new MediaController(this);
        mediaController.setVisibility(View.GONE);
        mediaController.setAnchorView(playerView);

// Init Video
        playerView.setMediaController(mediaController);

        instagram = new Instagram(PlayerActivity.this, DevelopersKeys.INSTAGRAM_CLIENT_ID, DevelopersKeys.INSTAGRAM_CLIENT_SECRET, DevelopersKeys.INSTAGRAM_REDIRECT_URI);


        final Handler locationHandler = new Handler();
        Runnable locationRunnable = new Runnable() {
            @Override
            public void run() {
                getLocation();
                locationHandler.postDelayed(this, 3600000);
            }
        };
        locationHandler.postDelayed(locationRunnable, 1500);


        loadfbGroupVideos();

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR1) {
            playerView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    switch (what) {
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            if (!dialog.isShowing()) {
                                dialog.show();
                            }
                            break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            break;
                    }
                    return false;
                }
            });
        }

        playerView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                loadfbGroupVideos();
            }
        });


        playerView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (videoIndex == videoItems.size() - 1) {
                    videoIndex = 0;
                } else {
                    videoIndex++;
                }


                playerView.setVideoURI(Uri.parse(videoItems.get(videoIndex).getSource()));
                playerView.start();

//                try {
//                    Cache cache = new FileCache(new File(getExternalCacheDir(),"uno_cache"));
//                    HttpUrlSource source = new HttpUrlSource(videoItems.get(videoIndex).getSource());
//                    proxyCache = new HttpProxyCache(source, cache);
//                    playerView.setVideoPath(proxyCache.getUrl());
//                    playerView.start();
//                } catch (ProxyCacheException e) {
//                    Log.e("Video cache error", "Error playing video", e);
//                }
            }
        });


        mSlider = (SliderLayout) findViewById(R.id.slider);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(DevelopersKeys.TWITTER_KEY, DevelopersKeys.TWITTER_SECRET);
        Fabric.with(PlayerActivity.this, new Twitter(authConfig));
        final Handler twitterFeedHandler = new Handler();
        Runnable twitterFeedRunnable = new Runnable() {
            @Override
            public void run() {
                Twitter.getInstance();
                TwitterHelper.startLoadTweets(Twitter.getApiClient(), relativeLayout, PlayerActivity.this, isFirst);
                isFirst = false;
                twitterFeedHandler.postDelayed(this, 900000);
            }
        };

        twitterFeedHandler.postDelayed(twitterFeedRunnable, 1700);

        final Handler instagramHandler = new Handler();
        Runnable instagramRunnable = new Runnable() {
            @Override
            public void run() {
                updateIGPhotos();
                instagramHandler.postDelayed(this, 900000);
            }
        };
        instagramHandler.postDelayed(instagramRunnable, 2000);


        final Handler currHandler = new Handler();
        Runnable currRunnable = new Runnable() {
            @Override
            public void run() {
                getCurrencies();
                currHandler.postDelayed(this, 3600000);
            }
        };
        currHandler.postDelayed(currRunnable, 2200);


    }

    private void loadSlide() {
        if (igPhotos.size() > 0) {
            mSlider.stopAutoCycle();
            mSlider.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);

            mSlider.setPresetTransformer(SliderLayout.Transformer.Fade);


            for (int i = 0; i < igPhotos.size(); i++) {

                final TextSliderView textSliderView = new TextSliderView(PlayerActivity.this);


                textSliderView.description(igPhotos.get(i).getIgUserName() + " Instagram photos").
                        image(igPhotos.get(i).getIgOriginURL()).setScaleType(DefaultSliderView.ScaleType.Fit);


                mSlider.addSlider(textSliderView);

                if (i == igPhotos.size() - 1) {
                    mSlider.setDuration(150);
                    mSlider.startAutoCycle();
                }


            }


            mSlider.startAutoCycle();
        } else {
            Toast.makeText(this, "Instagram photos load fail", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateIGPhotos() {
        String tag = preferences.getString("instHashTag", "");
        final GetIGPhotosTask getTagPhotoIGTask = new GetIGPhotosTask(instagram.getSession().getUser().id);
        getTagPhotoIGTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, instagram.getSession().getAccessToken());

        try {
            JSONObject igObject = new JSONObject(getTagPhotoIGTask.get());
            Log.d("object", igObject.toString());
            getIGPhotos(igObject, tag, location);
        } catch (JSONException e) {
            Log.d("error", "json error");
        } catch (InterruptedException e) {
            Log.d("error", "interrupted error");
        } catch (ExecutionException e) {
            Log.d("error", "execution error");
        }
        loadSlide();
    }

    private void getIGPhotos(JSONObject igObject, String tag, Location location) {
        igPhotos = new ArrayList<>();
        try {
            Log.d("object main", igObject.toString());
            final JSONArray igData = igObject.getJSONArray("data");

            for (int i = 0; i < igData.length(); i++) {

                final JSONObject currObj = igData.getJSONObject(i)
                        .getJSONObject("images");
                Log.d("images main", currObj.toString());
                final String origURL = currObj.getJSONObject("standard_resolution")
                        .getString(URL);
                Log.d("images url main", origURL);
                Log.d("data", igData.toString());
                String fsComm;
                if (igData.getJSONObject(i).getJSONObject("comments").getInt("count") > 0) {
                    fsComm = igData.getJSONObject(i).getJSONObject("comments")
                            .getJSONArray("data").getJSONObject(0).getString("text");
                } else {
                    fsComm = "";
                }


                if (!igData.getJSONObject(i).isNull("tags")) {
                    if (igData.getJSONObject(i).getJSONArray("tags").length() > 0) {
                        JSONArray tagsArray = igData.getJSONObject(i).getJSONArray("tags");
                        List<String> tagsList = new ArrayList<>();
                        for (int j = 0; j < tagsArray.length(); j++) {
                            tagsList.add(tagsArray.getString(j));
                        }

                        Log.d("tags", tagsList.toString());

                        if (!igData.getJSONObject(i).isNull("location")) {
                            double dist;
                            Log.d("loc null", "loc doesn't null");
                            double igPhotoLatitude = Double.valueOf(igData.getJSONObject(i).
                                    getJSONObject("location").getString("latitude"));
                            double igPhotoLongitude = Double.valueOf(igData.getJSONObject(i).
                                    getJSONObject("location").getString("longitude"));

                            Location igPhotoLocation = new Location("");
                            igPhotoLocation.setLatitude(igPhotoLatitude);
                            igPhotoLocation.setLongitude(igPhotoLongitude);


                            if (location != null) {
                                Log.d("loc", String.valueOf(location.getLatitude()).
                                        concat(":").
                                        concat(String.valueOf(location.getLongitude())));
                                dist = location.distanceTo(igPhotoLocation);

                                if (String.valueOf(dist) != null) {
                                    Log.d("dist", String.valueOf(location.distanceTo(igPhotoLocation)));
                                } else {
                                    dist = 0.0;
                                }


                                if (dist > 0 && dist < 50) {
                                    if (tagsList.contains(tag)) {
                                        igPhotos.add(new InstagramItem(igData.getJSONObject(i)
                                                .getString("id"), currObj.getJSONObject("standard_resolution")
                                                .getString(URL), origURL, fsComm, igData.getJSONObject(i).getJSONObject("user").getString("username")));
                                    }
                                }
                            }
                        }
                    }
                }
            }


        } catch (JSONException e) {
            Log.d("json", "json_error: ".concat(e.getMessage()));
        }


    }


    private void loadfbGroupVideos() {
        String fbGroupID = preferences.getString("fbGroupID", "");
        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + fbGroupID + "/videos",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
            /* handle the result */
                        Log.d("resp", response.toString());
                        JsonObject o = new JsonParser().parse(response.getJSONObject().toString()).getAsJsonObject();
                        JsonArray array = o.get("data").getAsJsonArray();
                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<FacebookVideoItem>>() {
                        }.getType();
                        videoItems = gson.fromJson(array.toString(), listType);
                        Log.d("video", videoItems.toString());

                        if (isStartVideo) {
                            playerView.setVideoURI(Uri.parse(videoItems.get(videoIndex).getSource()));
                            playerView.start();
                            isStartVideo = false;
                        }

                    }

                }
        );
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,description,source");

        request.setParameters(parameters);
        request.executeAsync();
    }


    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        Log.d("location", String.valueOf(location.getLatitude()) + ":" + String.valueOf(location.getLongitude()));
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    @Override
    public void onProviderEnabled(String provider) {

    }


    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onFailConnection(Exception e) {

    }

    @Override
    public void onFailParsing(Exception e) {

    }

    @Override
    public void onFailFindLocation(Exception e) {

    }

    @Override
    public void gotWeatherInfo(WeatherInfo weatherInfo) {
        if(weatherInfo!=null) {
            weatherLayout.removeAllViews();
            weatherLayout.setOrientation(LinearLayout.HORIZONTAL);
            weatherLayout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            weatherLayout.setBackgroundColor(getResources().getColor(R.color.rss_line));

            RelativeLayout.LayoutParams wlLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            wlLP.addRule(RelativeLayout.ABOVE, R.id.slider);
            wlLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            wlLP.addRule(RelativeLayout.ALIGN_LEFT, R.id.slider);

            weatherLayout.setLayoutParams(wlLP);

            ImageView conditionImage = new ImageView(this);
            conditionImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            conditionImage.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            LinearLayout.LayoutParams ciLP = new LinearLayout.LayoutParams(40, 40);
            conditionImage.setLayoutParams(ciLP);

            TextView tempText = new TextView(this);
            tempText.setTextColor(Color.WHITE);
            tempText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            tempText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            tempText.setText(String.valueOf(weatherInfo.getCurrentTemp()) + " " + "\u2103");

            weatherLayout.addView(conditionImage);
            weatherLayout.addView(tempText);


            Glide.with(this).load(Uri.parse(weatherInfo.getCurrentConditionIconURL())).into(conditionImage);
        }
        else{
            Toast.makeText(PlayerActivity.this,"Weather information is unavailable",Toast.LENGTH_SHORT).show();
        }

    }

    public void loadCurrencyDashboard(CurrenciesList list, CurrenciesList yesterdayList) {
        String euro = "\u20ac" + " 0,0";
        String dollar = "\u0024" + " 0,0";
        String pound = "\u00a3" + " 0,0";
        String yena = "\u00a5" + " 0,0";

        DecimalFormat df = new DecimalFormat("#.0000");


        isEuroUp = true;
        isDollarUp = true;
        isPoundUp = true;
        isYenaUp = true;

        if (list.currencies.size() > 0 && yesterdayList.currencies.size()>0 && list.currencies.size() == yesterdayList.currencies.size())

            for (int i = 0; i < list.currencies.size(); i++) {
                if (i < list.currencies.size() - 1) {
                    if (list.currencies.get(i).getCurrCharCode().equals("GBP")) {
                        String poundValue = list.currencies.get(i).getCurrValue().replace(",", ".");
                        String nominal = list.currencies.get(i).getNominal();
                        double poundValueDouble = Double.valueOf(poundValue);
                        int nominalInt = Integer.parseInt(nominal);

                        String ypoundValue = yesterdayList.currencies.get(i).getCurrValue().replace(",", ".");
                        String ynominal = yesterdayList.currencies.get(i).getNominal();
                        double ypoundValueDouble = Double.valueOf(ypoundValue);
                        int ynominalInt = Integer.parseInt(ynominal);

                        double tVal = poundValueDouble / nominalInt;
                        double yVal = ypoundValueDouble / ynominalInt;

                        isPoundUp = tVal > yVal;


                        pound = "\u00a3" + " " + df.format(tVal);
                    } else if (list.currencies.get(i).getCurrCharCode().equals("USD")) {
                        String poundValue = list.currencies.get(i).getCurrValue().replace(",", ".");
                        String nominal = list.currencies.get(i).getNominal();
                        double poundValueDouble = Double.valueOf(poundValue);
                        int nominalInt = Integer.parseInt(nominal);

                        String ypoundValue = yesterdayList.currencies.get(i).getCurrValue().replace(",", ".");
                        String ynominal = yesterdayList.currencies.get(i).getNominal();
                        double ypoundValueDouble = Double.valueOf(ypoundValue);
                        int ynominalInt = Integer.parseInt(ynominal);

                        double tVal = poundValueDouble / nominalInt;
                        double yVal = ypoundValueDouble / ynominalInt;

                        isDollarUp = tVal >= yVal;

                        dollar = "\u0024" + " " + df.format(tVal);
                    } else if (list.currencies.get(i).getCurrCharCode().equals("CNY")) {
                        String poundValue = list.currencies.get(i).getCurrValue().replace(",", ".");
                        String nominal = list.currencies.get(i).getNominal();
                        double poundValueDouble = Double.valueOf(poundValue);
                        int nominalInt = Integer.parseInt(nominal);

                        String ypoundValue = yesterdayList.currencies.get(i).getCurrValue().replace(",", ".");
                        String ynominal = yesterdayList.currencies.get(i).getNominal();
                        double ypoundValueDouble = Double.valueOf(ypoundValue);
                        int ynominalInt = Integer.parseInt(ynominal);

                        double tVal = poundValueDouble / nominalInt;
                        double yVal = ypoundValueDouble / ynominalInt;

                        isYenaUp = tVal >= yVal;

                        yena = "\u04b0" + " " + df.format(tVal);
                    } else if (list.currencies.get(i).getCurrCharCode().equals("EUR")) {

                        String poundValue = list.currencies.get(i).getCurrValue().replace(",", ".");
                        String nominal = list.currencies.get(i).getNominal();
                        double poundValueDouble = Double.valueOf(poundValue);
                        int nominalInt = Integer.parseInt(nominal);

                        String ypoundValue = yesterdayList.currencies.get(i).getCurrValue().replace(",", ".");
                        String ynominal = yesterdayList.currencies.get(i).getNominal();
                        double ypoundValueDouble = Double.valueOf(ypoundValue);
                        int ynominalInt = Integer.parseInt(ynominal);

                        double tVal = poundValueDouble / nominalInt;
                        double yVal = ypoundValueDouble / ynominalInt;

                        isEuroUp = tVal >= yVal;

                        euro = "\u20ac" + " " + df.format(tVal);
                    }
                } else {
                    dashLayout.removeAllViews();
                    dashLayout.setOrientation(LinearLayout.VERTICAL);
                    dashLayout.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    dashLayout.setBackgroundColor(getResources().getColor(R.color.rss_line));
                    RelativeLayout.LayoutParams dlLP = new RelativeLayout.
                            LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);

                    dlLP.addRule(RelativeLayout.ALIGN_LEFT, R.id.slider);
                    dlLP.addRule(RelativeLayout.BELOW, R.id.slider);
                    dlLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);


                    dashLayout.setLayoutParams(dlLP);

                    TextView usdText = new TextView(this);
                    usdText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                    TextView eurText = new TextView(this);
                    eurText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                    TextView gbpText = new TextView(this);
                    gbpText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                    TextView cnyText = new TextView(this);
                    cnyText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);

                    usdText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    eurText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    gbpText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    cnyText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                    dashLayout.addView(usdText);
                    dashLayout.addView(eurText);
                    dashLayout.addView(gbpText);
                    dashLayout.addView(cnyText);

                    if (isDollarUp) {
                        usdText.setTextColor(Color.GREEN);
                    } else {
                        usdText.setTextColor(Color.RED);
                    }
                    usdText.setText(dollar);

                    if (isEuroUp) {
                        eurText.setTextColor(Color.GREEN);
                    } else {
                        eurText.setTextColor(Color.RED);
                    }
                    eurText.setText(euro);

                    if (isPoundUp) {
                        gbpText.setTextColor(Color.GREEN);
                    } else {
                        gbpText.setTextColor(Color.RED);
                    }
                    gbpText.setText(pound);

                    if (isYenaUp) {
                        cnyText.setTextColor(Color.GREEN);
                    } else {
                        cnyText.setTextColor(Color.RED);
                    }
                    cnyText.setText(yena);


                }
            }

    }

    public Location getLocation() {
        try {
            LocationManager loc = (LocationManager) getSystemService(LOCATION_SERVICE);

            boolean isGPSEnabled = loc
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            boolean isNetworkEnabled = loc
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
            } else {
                if (isNetworkEnabled) {
                    loc.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            0,
                            0, this);
                    Log.d("Network", "Network Enabled");
                    if (loc != null) {
                        location = loc
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
                if (isGPSEnabled) {
                    if (location == null) {
                        loc.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                0,
                                0, this);
                        Log.d("GPS", "GPS Enabled");
                        if (loc != null) {
                            location = loc
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        getLocationName(location);

        return location;
    }


    public String getLocationName(Location location) {

        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.ENGLISH);
        try {

            List<Address> addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(),
                    10);
            cityName = addresses.get(0).getLocality();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mYahooWeather.setNeedDownloadIcons(true);
        mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.PLACE_NAME);
        mYahooWeather.queryYahooWeatherByPlaceName(this, cityName, this);

        return cityName;

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume(){
        if (playerView != null) {
            playerView.resume();  // <-- this will cause re-buffer.
        }
        super.onResume();
    }


    @Override
    protected void onPause() {
        if (playerView != null) {
            playerView.suspend();
        }
        super.onPause();
    }

    public void getCurrencies() {
        GetCurrencies getCurrencies = new GetCurrencies(this);
        getCurrencies.execute(new Date());
    }


}
