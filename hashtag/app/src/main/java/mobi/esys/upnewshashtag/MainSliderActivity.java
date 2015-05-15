package mobi.esys.upnewshashtag;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import net.londatiga.android.instagram.Instagram;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import io.fabric.sdk.android.Fabric;
import mobi.esys.consts.ISConsts;
import mobi.esys.downloaders.InstagramPhotoDownloader;
import mobi.esys.filesystem.directories.DirectoryHelper;
import mobi.esys.instagram.model.InstagramPhoto;
import mobi.esys.tasks.GetTagPhotoIGTask;
import mobi.esys.twitter.model.TwitterHelper;

/**
 * Created by Артем on 17.04.2015.
 */
public class MainSliderActivity extends Activity {
    private static final String URL = "url";


    private static transient SliderLayout mSlider;
    private static final SliderLayout.Transformer[] ANIMATIONS = SliderLayout.Transformer.values();
    private transient RelativeLayout relativeLayout;
    private transient UNHApp mApp;
    private transient SharedPreferences preferences;


    private transient String[] photoFiles;


    private transient JSONObject igObject;

    private transient Instagram instagram;

    private transient String igHashTag;
    private transient String twHashTag;

    private transient boolean isTwAllow;


    private transient final String TAG = this.getClass().getSimpleName();


    private transient List<InstagramPhoto> igPhotos;

    private transient int musicIndex;
    private transient MediaPlayer mediaPlayer;


    private transient List<Integer> soundIds;
    private transient List<Integer> rawIds;
    private transient List<Integer> imageIds;
    private transient List<String> twFeed;


    private transient Timer mTimer;
    private transient ChangeAnimationTimerTask changeAnimationTimerTask;


    private transient int musicPosition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main_slider);

        preferences = getSharedPreferences(ISConsts.globals.pref_prefix, MODE_PRIVATE);

        igHashTag = preferences.getString(ISConsts.prefstags.instagram_hashtag, ISConsts.globals.default_hashtag);
        twHashTag = preferences.getString(ISConsts.prefstags.twitter_hashtag, ISConsts.globals.default_hashtag);
        isTwAllow = preferences.getBoolean(ISConsts.prefstags.twitter_allow, false);


        igPhotos = new ArrayList<>();


        relativeLayout = (RelativeLayout) findViewById(R.id.embeded_slider_layout);

        instagram = new Instagram(MainSliderActivity.this,
                ISConsts.instagramconsts.instagram_client_id, ISConsts.instagramconsts.instagram_client_secret,
                ISConsts.instagramconsts.instagram_redirect_uri);

        mApp = (UNHApp) getApplicationContext();


        initSlider();


        DirectoryHelper photoDirHelper = new DirectoryHelper(ISConsts.globals.dir_name.concat(ISConsts.globals.photo_dir_name));
        photoFiles = photoDirHelper.getDirFileList(TAG);


        mTimer = new Timer();
        changeAnimationTimerTask = new ChangeAnimationTimerTask();
        mTimer.schedule(changeAnimationTimerTask, 0L, ISConsts.times.anim_duration - 5);

        loadRes();
        loadSlide(false);
        playMP3();


    }

    private void initSlider() {
        mSlider = new SliderLayout(this);
        mSlider.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
        RelativeLayout.LayoutParams msLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        mSlider.setLayoutParams(msLayoutParams);
        relativeLayout.addView(mSlider);

        initTwitter();

    }

    private void updateIGPhotos(final String tag) {
        final GetTagPhotoIGTask getTagPhotoIGTask = new GetTagPhotoIGTask(
                MainSliderActivity.this,
                "default", tag, false, mApp);
        getTagPhotoIGTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, instagram.getSession().getAccessToken());

        Log.d("aT main", instagram.getSession().getAccessToken());
        try {
            igObject = new JSONObject(getTagPhotoIGTask.get());
            Log.d("object", igObject.toString());
            getIGPhotos(igObject);
        } catch (JSONException e) {
            Log.d("error", "json error");
        } catch (InterruptedException e) {
            Log.d("error", "interrupted error");
        } catch (ExecutionException e) {
            Log.d("error", "execution error");
        }

    }

    private void getIGPhotos(JSONObject igObject) {
        igPhotos = new ArrayList<>();
        try {
            Log.d("object main", igObject.toString());
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
                Log.d("ig photos main", igPhotos.get(i).toString());

            }

            InstagramPhotoDownloader instagramPhotoDownloader = new InstagramPhotoDownloader(MainSliderActivity.this, true);
            instagramPhotoDownloader.download(igPhotos);


        } catch (JSONException e) {
            Log.d("json", "json_error");
        }


    }

    private static void changeAnimation() {
        Random r = new Random();
        mSlider.setPresetTransformer(SliderLayout.Transformer.valueOf(ANIMATIONS[r.nextInt(ANIMATIONS.length)].name()));
    }


    public void loadSlide(boolean isRefresh) {

        mSlider.stopAutoCycle();
        mSlider.removeAllSliders();

        if (isRefresh) {
            relativeLayout.removeView(mSlider);
            initSlider();
        }


        Log.d(TAG.concat(" photo"), igPhotos.toString());
        Log.d(TAG, "change slides");
        DirectoryHelper photoDirHelper = new DirectoryHelper(ISConsts.globals.dir_name.concat(ISConsts.globals.photo_dir_name));
        photoFiles = photoDirHelper.getDirFileList(TAG);
        for (int i = 0; i < photoFiles.length; i++) {

            DefaultSliderView textSliderView = new DefaultSliderView(this);
            File imageFile = new File(photoFiles[i]);
            Log.d("img files", imageFile.getAbsolutePath());
            Log.d("slide load", String.valueOf(imageFile.exists()));
            if (imageFile.exists()) {
                textSliderView
                        .image(imageFile).setScaleType(DefaultSliderView.ScaleType.Fit).error(R.raw.error).empty(R.raw.empty);
                mSlider.addSlider(textSliderView);
            }
            if (i == photoFiles.length - 1) {
                mSlider.setDuration(ISConsts.times.anim_duration);
                mSlider.startAutoCycle();
            }
        }


    }



    private void playMP3() {
        mediaPlayer = MediaPlayer.create(MainSliderActivity.this, soundIds.get(0));
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playEmbedded();
                updateIGPhotos(igHashTag);
            }
        });


        mediaPlayer.start();
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
                Log.d(TAG, e.getLocalizedMessage());
            }

        } else {
            AssetFileDescriptor afd = getResources().openRawResourceFd(soundIds.get(musicIndex));
            if (afd == null) {
                return;
            }
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
                afd.close();
            } catch (IOException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }

        }
    }


    public List<Integer> getAllResourceIDs(Class<?> aClass) throws IllegalArgumentException {
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            musicPosition = mediaPlayer.getCurrentPosition();
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            musicPosition = mediaPlayer.getCurrentPosition();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(musicPosition);
            mediaPlayer.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            musicPosition = mediaPlayer.getCurrentPosition();
        }
        if (mSlider != null) {
            mSlider.stopAutoCycle();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (changeAnimationTimerTask != null) {
            changeAnimationTimerTask.cancel();
        }

    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }


    public void loadRes() {
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
    }




    static class ChangeAnimationTimerTask extends TimerTask {
        @Override
        public void run() {
            changeAnimation();
        }
    }

    public void initTwitter() {
        if (isTwAllow) {
            TwitterAuthConfig authConfig = new TwitterAuthConfig(ISConsts.twitterconsts.twitter_key, ISConsts.twitterconsts.twitter_secret);
            Fabric.with(this, new Twitter(authConfig));
            TwitterHelper.startLoadTweets(Twitter.getInstance().getApiClient(), twHashTag, relativeLayout, MainSliderActivity.this);
        }

    }


}


