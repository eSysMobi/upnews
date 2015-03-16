package mobi.esys.upnewslite;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.analytics.tracking.android.EasyTracker;

import java.util.HashSet;
import java.util.Set;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.fileworks.FileWorks;
import mobi.esys.tasks.DownloadVideoTask;
import mobi.esys.tasks.RSSTask;

public class FirstVideoActivity extends Activity {
    private transient VideoView video;
    private transient String uriPath;
    private transient MediaController controller;
    private transient SharedPreferences prefs;
    private transient boolean isDown;
    private transient Set<String> md5sApp;
    private transient RelativeLayout relativeLayout;
    private transient DownloadVideoTask downloadVideoTask;
    private transient Handler handler;
    private transient Runnable runnable;
    private transient boolean isFirstRSS;
    private transient TextView textView;
    private transient UNLApp mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        isFirstRSS = true;
        mApp = (UNLApp) getApplication();

        textView = new TextView(FirstVideoActivity.this);


        DirectoryWorks directoryWorks = new DirectoryWorks(
                UNLConsts.VIDEO_DIR_NAME);


        prefs = mApp.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, MODE_PRIVATE);
        isDown = prefs.getBoolean("isDownload", true);
        uriPath = "";
        Set<String> defSet = new HashSet<>();
        md5sApp = prefs.getStringSet("md5sApp", defSet);
        if (directoryWorks.getDirFileList("first").length == 0
                && md5sApp.size() == 0) {
            setContentView(R.layout.activity_firstvideo);


            relativeLayout = (RelativeLayout) findViewById(R.id.fullscreenLayout);
            controller = new MediaController(FirstVideoActivity.this);

            video = (VideoView) findViewById(R.id.video);
            video.setMediaController(controller);

            controller.setAnchorView(video);


            uriPath = "android.resource://" + getPackageName() + "/assets/"
                    + R.raw.emb;
            Log.d("video", uriPath);
            play();

            video.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    DirectoryWorks directoryWorks = new DirectoryWorks(
                            UNLConsts.VIDEO_DIR_NAME);
                    Set<String> defSet = new HashSet<>();
                    md5sApp = prefs.getStringSet("md5sApp", defSet);
                    if (directoryWorks.getDirFileList("first").length == 0
                            && md5sApp.size() == 0) {
                        play();
                        restartDownload();
                    } else {
                        if (directoryWorks.getDirFileList("first").length > 0) {
                            FileWorks fileWorks = new FileWorks(directoryWorks
                                    .getDirFileList("first")[0]);
                            stopDownload();
                            if (md5sApp.contains(fileWorks.getFileMD5())) {
                                startActivity(new Intent(FirstVideoActivity.this,
                                        FullscreenActivity.class));
                                finish();

                            } else {
                                play();
                                restartDownload();
                            }
                        } else {
                            play();
                            restartDownload();

                        }
                        textView.requestFocus();
                    }

                }
            });
            video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    controller.show();
                    LinearLayout ll = (LinearLayout) controller.getChildAt(0);


                    for (int i = 0; i < ll.getChildCount(); i++) {

                        if (ll.getChildAt(i) instanceof LinearLayout) {
                            LinearLayout llC = (LinearLayout) ll.getChildAt(i);
                            for (int j = 0; j < llC.getChildCount(); j++) {
                                if (llC.getChildAt(j) instanceof SeekBar) {
                                    SeekBar seekBar = (SeekBar) llC.getChildAt(j);
                                    seekBar.setProgressDrawable(getResources().getDrawable(R.drawable.seekbartheme_scrubber_progress_horizontal_holo_dark));
                                    seekBar.setThumb(getResources().getDrawable(R.drawable.seekbartheme_scrubber_control_selector_holo_dark));
                                }
                            }
                        }

                    }
                    textView.requestFocus();
                }
            });

            video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.d("mp error", String.valueOf(what) + ":" + String.valueOf(extra));
                    return false;
                }
            });

            downloadVideoTask = new DownloadVideoTask(mApp);
            downloadVideoTask.execute();

            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    RSSTask rssTask = new RSSTask(FirstVideoActivity.this, "first", mApp);
                    rssTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    handler.postDelayed(this, UNLConsts.RSS_REFRESH_INTERVAL);
                }
            };

            handler.postDelayed(runnable, UNLConsts.RSS_TASK_START_DELAY);


        } else {
            startActivity(new Intent(FirstVideoActivity.this,
                    FullscreenActivity.class));
            finish();

        }


    }


    @Override
    protected void onStop() {
        super.onStop();
        video.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        video.pause();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!video.isPlaying()) {
            video.resume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!video.isPlaying()) {
            video.resume();
        }
        if (handler != null && runnable != null) {
            handler.postDelayed(runnable, UNLConsts.RSS_TASK_START_DELAY);
        }
    }

    @Override
    protected void onStart() {
        EasyTracker.getInstance(this).activityStart(this);
        super.onStart();
    }


    public void startRSS(String feed) {
        if (isFirstRSS) {

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
            isFirstRSS = false;

        } else {
            textView.setText("");
            textView.setText(Html.fromHtml(feed), TextView.BufferType.SPANNABLE);
            textView.requestFocus();
        }
    }

    public void play() {
        Uri uri = Uri.parse(uriPath);
        video.setVideoURI(uri);
        video.start();
    }

    public void restartDownload() {
        if (!isDown) {
            downloadVideoTask.cancel(true);
            downloadVideoTask = new DownloadVideoTask(mApp);
            downloadVideoTask.execute();
        }
    }

    public void stopDownload() {
        downloadVideoTask.cancel(true);
    }


}
