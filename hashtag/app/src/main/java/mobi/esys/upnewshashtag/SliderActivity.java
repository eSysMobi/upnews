package mobi.esys.upnewshashtag;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.SearchService;

import net.londatiga.android.instagram.Instagram;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import mobi.esys.consts.ISConsts;
import mobi.esys.instagram.model.InstagramPhoto;
import mobi.esys.tasks.GetTagPhotoIGTask;


public class SliderActivity extends Activity {
    private transient SliderLayout mSlider;
    private static final SliderLayout.Transformer[] ANIMATIONS = SliderLayout.Transformer.values();
    private static final int ANIM_DURATION = 3000;
    private transient MediaPlayer mediaPlayer;
    private transient TextView textView;
    private transient RelativeLayout relativeLayout;
    private transient UNHApp mApp;
    private transient JSONObject igObject;

    private static final String THUMBNAIL = "standard_resolution";
    private static final String URL = "url";

    private transient List<InstagramPhoto> igPhotos;
    private transient List<InstagramPhoto> morePhotos;

    private transient Instagram instagram;

    private transient SharedPreferences preferences;

    private static final int[] RAW_IDS = {R.raw.bgmusic, R.raw.lew};
    private transient int musicIndex = 0;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slider);

        mApp = (UNHApp) getApplicationContext();

        preferences = getSharedPreferences(ISConsts.PREF_PREFIX, MODE_PRIVATE);


        instagram = new Instagram(SliderActivity.this,
                ISConsts.INSTAGRAM_CLIENT_ID, ISConsts.INSTAGRAM_CLIENT_SECRET,
                ISConsts.INSTAGRAM_REDIRECT_URI);


        igPhotos = new ArrayList<>();
        morePhotos = new ArrayList<>();

        textView = new TextView(SliderActivity.this);
        relativeLayout = (RelativeLayout) findViewById(R.id.embeded_slider_layout);

        mSlider = (SliderLayout) findViewById(R.id.slider);

        String igHashTag = preferences.getString("igHashTag", "#news");
        Toast.makeText(SliderActivity.this, igHashTag, Toast.LENGTH_SHORT).show();
        updateIGPhotos(igHashTag, false);

        playMP3();
        mSlider.startAutoCycle();

        Timer myTimer = new Timer(); // Создаем таймер
        myTimer.schedule(new TimerTask() { // Определяем задачу
            @Override
            public void run() {
                changeAnimation();
            }


        }

                , 0L, 2995);

        loadTweets();


    }

    private void changeAnimation() {
        Random r = new Random();
        mSlider.setPresetTransformer(SliderLayout.Transformer.valueOf(ANIMATIONS[r.nextInt(ANIMATIONS.length)].name()));
    }


    public void restartCreepingLine() {
    }

    public void recToMP(String playlist_video_play, String s) {

    }

    public void startRSS(String feed) {


        Log.d("feed", feed);

        textView.setBackgroundColor(getResources().getColor(R.color.rss_line));
        textView.setTextColor(Color.WHITE);
        RelativeLayout.LayoutParams tslp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 80);
        tslp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        tslp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        tslp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setLayoutParams(tslp);
        textView.setTextSize(30);
        textView.setPadding(20, 0, 20, 0);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        textView.setMarqueeRepeatLimit(-1);
        textView.setHorizontallyScrolling(true);
        textView.setFocusable(true);
        textView.setFocusableInTouchMode(true);
        textView.setFreezesText(true);
        textView.requestFocus();


        textView.setText(Html.fromHtml(feed), TextView.BufferType.SPANNABLE);
        relativeLayout.addView(textView);


    }

    private void playMP3() {
        mediaPlayer = MediaPlayer.create(SliderActivity.this, RAW_IDS[musicIndex]);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                musicIndex++;
                if (musicIndex == RAW_IDS.length) {
                    AssetFileDescriptor afd = getResources().openRawResourceFd(RAW_IDS[musicIndex]);
                    if (afd == null) {
                        return;
                    }
                    try {
                        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        afd.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    AssetFileDescriptor afd = getResources().openRawResourceFd(RAW_IDS[musicIndex]);
                    if (afd == null) {
                        return;
                    }
                    try {
                        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        afd.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void loadTweets() {
        final SearchService service = Twitter.getInstance().getApiClient().getSearchService();
        String twHashTag = preferences.getString("twHashTag", "#news");

        service.tweets(twHashTag, null, null, null, null, null, null, null, null, null, new Callback<Search>() {
            @Override
            public void success(Result<Search> searchResult) {
                StringBuilder builder = new StringBuilder();
                List<Tweet> tweets = searchResult.data.tweets;
                for (int i = 0; i < tweets.size(); i++) {
                    builder.append("<font color='blue'>@</font>").append("<font color='blue'>").append(tweets.get(i).user.name).append("</font>").append(":").append(tweets.get(i).text);


                }

                startRSS(builder.toString());
            }

            @Override
            public void failure(TwitterException e) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    private void updateIGPhotos(final String tag, final boolean isShowProgress) {
        final GetTagPhotoIGTask getTagPhotoIGTask = new GetTagPhotoIGTask(
                SliderActivity.this,
                "default", tag, isShowProgress, mApp);
        getTagPhotoIGTask.execute(instagram.getSession().getAccessToken());

        Log.d("aT", instagram.getSession().getAccessToken());
        try {
            igObject = new JSONObject(getTagPhotoIGTask.get());
            getIGPhotos("get");

        } catch (JSONException e) {
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }
    }

    private void getIGPhotos(final String mode) {
        try {
            final JSONArray igData = igObject.getJSONArray("data");

            if ("get".equals(mode)) {
                for (int i = 0; i < igData.length(); i++) {

                    final JSONObject currObj = igData.getJSONObject(i)
                            .getJSONObject("images");
                    final String origURL = currObj.getJSONObject(THUMBNAIL)
                            .getString(URL).replace("s.jpg", "n.jpg");
                    igPhotos.add(new InstagramPhoto(igData.getJSONObject(i)
                            .getString("id"), currObj.getJSONObject(THUMBNAIL)
                            .getString(URL), origURL));
                }
            } else if ("more".equals(mode)) {
                Log.d("mode", mode);
                Log.d("data", igData.toString());
                morePhotos.clear();
                for (int i = 0; i < igData.length(); i++) {

                    final JSONObject currObj = igData.getJSONObject(i)
                            .getJSONObject("images");
                    String origURL = currObj.getJSONObject(THUMBNAIL)
                            .getString(URL).replace("s.jpg", "n.jpg");
                    morePhotos.add(new InstagramPhoto(igData.getJSONObject(i)
                            .getString("id"), currObj.getJSONObject(THUMBNAIL)
                            .getString(URL), origURL));
                }
            }
        } catch (JSONException e) {
        }

//        DownloadInstagramPhotoTask photoTask = new DownloadInstagramPhotoTask(SliderActivity.this);
//        photoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, igPhotos);


        for (int i = 0; i < igPhotos.size(); i++) {
            DefaultSliderView textSliderView = new DefaultSliderView(this);
            textSliderView
                    .image(igPhotos.get(i).getIgThumbURL()).setScaleType(BaseSliderView.ScaleType.FitCenterCrop);

            mSlider.addSlider(textSliderView);
        }

        Random r = new Random();
        mSlider.setPresetTransformer(SliderLayout.Transformer.valueOf(ANIMATIONS[r.nextInt(ANIMATIONS.length)].name()));
        mSlider.setDuration(ANIM_DURATION);
    }
}
