package mobi.esys.upnewshashtag;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import net.londatiga.android.instagram.Instagram;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import de.greenrobot.event.EventBus;
import io.fabric.sdk.android.Fabric;
import mobi.esys.consts.ISConsts;
import mobi.esys.downloaders.InstagramPhotoDownloader;
import mobi.esys.eventbus.SongStopEvent;
import mobi.esys.filesystem.directories.DirectoryHelper;
import mobi.esys.instagram.model.InstagramPhoto;
import mobi.esys.tasks.GetTagPhotoIGTask;
import mobi.esys.twitter.model.TwitterHelper;


public class SliderActivity extends Activity {
    private transient SliderLayout mSlider;
    private static final SliderLayout.Transformer[] ANIMATIONS = SliderLayout.Transformer.values();
    private transient MediaPlayer mediaPlayer;
    private transient TextView textView;
    private transient RelativeLayout relativeLayout;
    private transient UNHApp mApp;


    private transient SharedPreferences preferences;

    private transient List<Integer> rawIds;
    private transient List<Integer> imageIds;
    private transient List<Integer> soundIds;
    private transient int musicIndex = 0;
    private transient int musicPosition = 0;

    private static final String URL = "url";

    private transient JSONObject igObject;

    private transient Instagram instagram;

    private transient String igHashTag;
    private transient String twHashTag;

    private transient boolean isTwAllow;


    private transient final String TAG = this.getClass().getSimpleName();


    private transient Handler twitterFeedHandler;
    private transient Runnable twitterFeedRunnable;

    private transient List<InstagramPhoto> igPhotos;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(ISConsts.globals.pref_prefix, MODE_PRIVATE);

        DirectoryHelper directoryHelper = new DirectoryHelper(ISConsts.globals.dir_name.concat(ISConsts.globals.photo_dir_name));
        if (directoryHelper.getDirFileList(TAG).length > 0) {
            startActivity(new Intent(SliderActivity.this, MainSliderActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        } else {
            init();
        }

    }

    public void init() {
        setContentView(R.layout.activity_slider);
        EventBus.getDefault().register(this);

        mApp = (UNHApp) getApplicationContext();


        instagram = new Instagram(SliderActivity.this,
                ISConsts.instagramconsts.instagram_client_id, ISConsts.instagramconsts.instagram_client_secret,
                ISConsts.instagramconsts.instagram_redirect_uri);


        rawIds = new ArrayList<>();
        rawIds.addAll(getAllResourceIDs(R.raw.class));

        imageIds = new ArrayList<>();
        soundIds = new ArrayList<>();

        for (int i = 0; i < rawIds.size(); i++) {
            String resName = getResources().getResourceName(rawIds.get(i));
            Log.d("raw id's", resName);
            if (resName.contains("img")) {
                imageIds.add(rawIds.get(i));
            } else if (resName.contains("snd")) {
                soundIds.add(rawIds.get(i));
            }
        }


        textView = new TextView(SliderActivity.this);
        relativeLayout = (RelativeLayout) findViewById(R.id.embeded_slider_layout);

        mSlider = (SliderLayout) findViewById(R.id.slider);
        mSlider.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);

        igHashTag = preferences.getString(ISConsts.prefstags.instagram_hashtag, ISConsts.globals.default_hashtag);
        twHashTag = preferences.getString(ISConsts.prefstags.twitter_hashtag, ISConsts.globals.default_hashtag);
        isTwAllow = preferences.getBoolean(ISConsts.prefstags.twitter_allow, false);


        loadSlide();


        igPhotos = new ArrayList<>();

        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                changeAnimation();
            }
        }, 0L, ISConsts.times.anim_duration - 5);


        playMP3();

        if (isTwAllow) {
            TwitterAuthConfig authConfig = new TwitterAuthConfig(ISConsts.twitterconsts.twitter_key, ISConsts.twitterconsts.twitter_secret);
            Fabric.with(SliderActivity.this, new Twitter(authConfig));
            twitterFeedHandler = new Handler();
            twitterFeedRunnable = new Runnable() {
                @Override
                public void run() {

                    TwitterHelper.startLoadTweets(Twitter.getInstance().getApiClient(), twHashTag, relativeLayout, SliderActivity.this);
                }
            };

            twitterFeedHandler.postDelayed(twitterFeedRunnable, ISConsts.times.twitter_get_feed_delay);
        }

        updateIGPhotos(igHashTag);

    }


    private void changeAnimation() {

        Random r = new Random();
        mSlider.setPresetTransformer(SliderLayout.Transformer.valueOf(ANIMATIONS[r.nextInt(ANIMATIONS.length)].name()));

    }


    private void playMP3() {
        mediaPlayer = MediaPlayer.create(SliderActivity.this, soundIds.get(musicIndex));
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                @Override
                                                public void onCompletion(MediaPlayer mp) {


                                                    DirectoryHelper photoDirHelper = new DirectoryHelper(ISConsts.globals.dir_name.concat(ISConsts.globals.photo_dir_name));
                                                    String[] photoFileList = photoDirHelper.getDirFileList(TAG);

                                                    if (photoFileList.length == 0) {
                                                        playEmbedded();
                                                        EventBus.getDefault().post(new SongStopEvent());
                                                        // restartPhotoDownload();
                                                        //restartMusicDownload();
                                                    } else {
                                                        if (photoFileList.length > 0 && !preferences.getBoolean("isDel", false)) {
                                                            //stopMusicDownload();
                                                            startActivity(new Intent(SliderActivity.this, MainSliderActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                                            finish();

                                                        } else {
                                                            playEmbedded();
                                                            EventBus.getDefault().post(new SongStopEvent());
                                                            //restartPhotoDownload();
                                                            //restartMusicDownload();
                                                        }
                                                    }
                                                }


                                            }

        );

        textView.setSelected(true);
        textView.invalidate();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopTwitterFeedRefresh();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSlider.stopAutoCycle();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            musicPosition = mediaPlayer.getCurrentPosition();
        }
        stopTwitterFeedRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSlider.startAutoCycle();
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(musicPosition);
            mediaPlayer.start();
        }
        restartTwiiterFeed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSlider.removeAllSliders();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            musicPosition = mediaPlayer.getCurrentPosition();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        EventBus.getDefault().unregister(this);
        stopTwitterFeedRefresh();
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            musicPosition = mediaPlayer.getCurrentPosition();
        }
    }


    private List<Integer> getAllResourceIDs(Class<?> aClass) throws IllegalArgumentException {
        Field[] IDFields = aClass.getFields();

        List<Integer> ids = new ArrayList<>();

        try {
            for (int i = 0; i < IDFields.length; i++) {
                ids.add(IDFields[i].getInt(null));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
        return ids;
    }

    private void loadSlide() {
        mSlider.stopAutoCycle();
        for (int i = 0; i < imageIds.size(); i++) {
            DefaultSliderView textSliderView = new DefaultSliderView(this);
            textSliderView
                    .image(imageIds.get(i)).setScaleType(DefaultSliderView.ScaleType.Fit);

            mSlider.addSlider(textSliderView);
        }

        Random r = new Random();
        mSlider.setPresetTransformer(SliderLayout.Transformer.valueOf(ANIMATIONS[r.nextInt(ANIMATIONS.length)].name()));
        mSlider.setDuration(ISConsts.times.anim_duration);

        mSlider.startAutoCycle();
    }

    private void updateIGPhotos(final String tag) {
        final GetTagPhotoIGTask getTagPhotoIGTask = new GetTagPhotoIGTask(
                SliderActivity.this,
                "default", tag, false, mApp);
        getTagPhotoIGTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, instagram.getSession().getAccessToken());

        Log.d("aT", instagram.getSession().getAccessToken());
        try {
            igObject = new JSONObject(getTagPhotoIGTask.get());
            getIGPhotos();
        } catch (JSONException e) {
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }
    }

    private void getIGPhotos() {
        igPhotos = new ArrayList<>();
        try {
            final JSONArray igData = igObject.getJSONArray("data");
            for (int i = 0; i < igData.length(); i++) {

                final JSONObject currObj = igData.getJSONObject(i)
                        .getJSONObject("images");
                Log.d("images main", currObj.toString());
                final String origURL = currObj.getJSONObject(ISConsts.instagramconsts.instagram_image_type)
                        .getString(URL);
                Log.d("images url main", origURL);
                igPhotos.add(new InstagramPhoto(igData.getJSONObject(i)
                        .getString("id"), currObj.getJSONObject(ISConsts.instagramconsts.instagram_image_type)
                        .getString(URL), origURL));
                Log.d("ig photos main", igPhotos.toString());

            }


        } catch (JSONException e) {
        }

        InstagramPhotoDownloader instagramPhotoDownloader = new InstagramPhotoDownloader(SliderActivity.this, false);
        instagramPhotoDownloader.download(igPhotos);

    }

    public void playEmbedded() {
        musicIndex++;
        if (musicIndex == soundIds.size()) {
            musicIndex = 0;

            AssetFileDescriptor afd = getResources().openRawResourceFd(soundIds.get(musicIndex));
            if (afd == null) {
                return;
            }
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mediaPlayer.prepare();
                mediaPlayer.start();
                afd.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            AssetFileDescriptor afd = getResources().openRawResourceFd(soundIds.get(musicIndex));
            if (afd == null) {
                return;
            }
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mediaPlayer.prepare();
                mediaPlayer.start();
                afd.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void stopTwitterFeedRefresh() {
        if (twitterFeedHandler != null && twitterFeedRunnable != null) {
            twitterFeedHandler.removeCallbacks(twitterFeedRunnable);
        }
    }

    public void restartTwiiterFeed() {
        if (twitterFeedHandler != null && twitterFeedRunnable != null) {
            twitterFeedHandler.postDelayed(twitterFeedRunnable, ISConsts.times.twitter_get_feed_delay);
        }
    }

    public void onEvent(SongStopEvent songStop) {
        updateIGPhotos(igHashTag);
    }
}
