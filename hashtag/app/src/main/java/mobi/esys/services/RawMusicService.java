package mobi.esys.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import mobi.esys.eventbus.SongStartEvent;
import mobi.esys.eventbus.SongStopEvent;
import mobi.esys.mediahelpers.RawMusicGetter;


public class RawMusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private transient MediaPlayer mPlayer;
    private transient int musicPosition;
    private transient int musicIndex;
    private transient List<Integer> soundIds;
    private transient RawMusicGetter rawMusicGetter;

    enum RawMusicServiceStates {
        Playing,
        Stopped,
        Prepairing,
        Paused
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        switch (action) {
            default:
                playRaw();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().post(new SongStartEvent());
        }
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().post(new SongStopEvent());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            musicIndex = 0;
            musicPosition = 0;
            soundIds = new ArrayList<>();
            initMediaPlayer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mPlayer != null) {
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
        }
        stopForeground(true);
    }


    private void initMediaPlayer() {
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
    }

    public class RawMusicBinder extends Binder {
        RawMusicService getService() {
            return RawMusicService.this;
        }

    }

    private void playRaw() {

    }

    private void releaseRes() {
        EventBus.getDefault().unregister(this);
        if (mPlayer != null) {
            mPlayer.release();
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
        }
        stopForeground(true);
    }

}
