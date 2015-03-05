package mobi.esys.upnewslite;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.playback.Playback;
import mobi.esys.tasks.DownloadVideoTask;
import mobi.esys.tasks.RSSTask;

@SuppressLint({"NewApi", "SimpleDateFormat"})
public class FullscreenActivity extends Activity {
    private transient VideoView videoView;
    private transient Playback playback;
    private transient TextView textView;
    private transient boolean isFirstRSS;
    private transient RelativeLayout relativeLayout;
    private transient Handler handler;
    private transient Runnable runnable;
    private transient UNLApp mApp;


    @Override
    protected void onCreate(Bundle stateBundle) {
        super.onCreate(stateBundle);
        setContentView(R.layout.activity_videofullscreen);

        relativeLayout = (RelativeLayout) findViewById(R.id.fullscreenLayout);
        textView = new TextView(FullscreenActivity.this);

        isFirstRSS = true;
        mApp = (UNLApp) getApplication();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                RSSTask rssTask = new RSSTask(FullscreenActivity.this, "full", mApp);
                rssTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                handler.postDelayed(this, UNLConsts.RSS_REFRESH_INTERVAL);
            }
        };

        handler.postDelayed(runnable, UNLConsts.RSS_TASK_START_DELAY);


        EasyTracker.getInstance(this).send(
                MapBuilder.createEvent("ui_action", "activity_launch",
                        "fullscreen_acivity", null).build());


        videoView = (VideoView) findViewById(R.id.video);

        startPlayback();
        DownloadVideoTask downloadVideoTask = new DownloadVideoTask(
                mApp);
        downloadVideoTask.execute();


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
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
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        DirectoryWorks directoryWorks = new DirectoryWorks(
                UNLConsts.VIDEO_DIR_NAME);
        if (directoryWorks.getDirFileList("fullscreen").length == 0) {
            startActivity(new Intent(FullscreenActivity.this,
                    FirstVideoActivity.class));
            finish();
        } else {
            startPlayback();
        }
        if (handler != null && runnable != null) {
            handler.postDelayed(runnable, UNLConsts.RSS_TASK_START_DELAY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DirectoryWorks directoryWorks = new DirectoryWorks(
                UNLConsts.VIDEO_DIR_NAME);
        if (directoryWorks.getDirFileList("fullscreen").length == 0) {
            startActivity(new Intent(FullscreenActivity.this,
                    FirstVideoActivity.class));
            finish();
        } else {
            startPlayback();
        }
    }

    public void startPlayback() {
        playback = new Playback(FullscreenActivity.this, mApp);
        playback.playFolder();
    }

    public VideoView getVideoView() {
        return this.videoView;
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


            textView.setText(feed);
            relativeLayout.addView(textView);
            isFirstRSS = false;

        } else {
            textView.setText("");
            textView.setText(feed);
            textView.requestFocus();
        }
    }


    public void restartCreepingLine() {
        textView.requestFocus();
    }

}
