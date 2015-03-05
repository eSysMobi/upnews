package mobi.esys.playback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.VideoView;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.fileworks.FileWorks;
import mobi.esys.tasks.DownloadVideoTask;
import mobi.esys.upnewslite.FirstVideoActivity;
import mobi.esys.upnewslite.FullscreenActivity;
import mobi.esys.upnewslite.R;
import mobi.esys.upnewslite.UNLApp;

public class Playback {
    private transient MediaController mController;
    private transient Context mContext;
    private transient VideoView mVideo;
    private static final String TAG = "Playback";
    private transient String[] files;
    private transient SharedPreferences preferences;
    private transient String[] ulrs = {""};
    private transient int serverIndex = 0;
    private transient SharedPreferences prefs;
    private transient boolean isDownload;
    private transient Set<String> md5sApp;
    private transient UNLApp mApp;
    private transient DownloadVideoTask downloadVideoTask;


    //127578844442-9qab0sqd5p13fhhs671lg1joqetcvj7k debug
    //127578844442-h41s9f3md1ni2soa7e3t3rpuqrukkd1u release

    public Playback(Context context, UNLApp app) {
        super();
        mController = new MediaController(context);
        mVideo = ((FullscreenActivity) context).getVideoView();
        mVideo.setMediaController(mController);
        mVideo.requestFocus();
        mContext = context;
        mApp = app;
        prefs = app.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, Context.MODE_PRIVATE);
        isDownload = false;
    }


    public void playFile(String filePath) {
        preferences = mContext.getSharedPreferences(UNLConsts.APP_PREF,
                Context.MODE_PRIVATE);
        Set<String> defaultSet = new HashSet<String>();
        md5sApp = preferences.getStringSet("md5sApp", defaultSet);
        File file = new File(filePath);

        FileWorks fileWorks = new FileWorks(filePath);
        if (file.exists() && md5sApp.contains(fileWorks.getFileMD5())) {
            mVideo.setVideoURI(Uri.parse(filePath));
            mVideo.start();
        } else {
            nextTrack(files);
        }

    }

    public void playFolder() {
        downloadVideoTask = new DownloadVideoTask(mApp);
        downloadVideoTask.execute();
        DirectoryWorks directoryWorks = new DirectoryWorks(
                UNLConsts.VIDEO_DIR_NAME);
        files = directoryWorks.getDirFileList("play folder");
        this.mVideo.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                nextTrack(files);
                return true;
            }
        });
        if (files.length > 0) {
            playFile(files[0]);
            mVideo.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    DirectoryWorks directoryWorks = new DirectoryWorks(
                            UNLConsts.VIDEO_DIR_NAME);
                    if (directoryWorks.getDirFileList("").length == 0) {
                        mContext.startActivity(new Intent(mContext,
                                FirstVideoActivity.class));
                        ((Activity) mContext).finish();

                    } else {
                        isDownload = mContext.getSharedPreferences(
                                UNLConsts.APP_PREF, Context.MODE_PRIVATE)
                                .getBoolean("isDownload", false);
                        nextTrack(files);
                        restartDownload();
                    }
                    ((FullscreenActivity) mContext).restartCreepingLine();
                }

            });

            mVideo.setOnErrorListener(new OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.d("mp error", String.valueOf(what) + ":" + String.valueOf(extra));
                    nextTrack(files);
                    return false;
                }
            });

            mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mController.show();
                    LinearLayout ll = (LinearLayout) mController.getChildAt(0);

                    for (int i = 0; i < ll.getChildCount(); i++) {

                        if (ll.getChildAt(i) instanceof LinearLayout) {
                            LinearLayout llC = (LinearLayout) ll.getChildAt(i);
                            for (int j = 0; j < llC.getChildCount(); j++) {
                                if (llC.getChildAt(j) instanceof SeekBar) {
                                    SeekBar seekBar = (SeekBar) llC.getChildAt(j);
                                    seekBar.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.seekbartheme_scrubber_progress_horizontal_holo_dark));
                                    seekBar.setThumb(mContext.getResources().getDrawable(R.drawable.seekbartheme_scrubber_control_selector_holo_dark));
                                }
                            }
                        }

                    }

                    ((FullscreenActivity) mContext).restartCreepingLine();
                }
            });
        } else {
            Log.d(TAG, "file list is empty");
        }
    }

    private void nextTrack(final String[] files) {

        String[] listFiles = {files[0]};

        if (!mContext
                .getSharedPreferences(UNLConsts.APP_PREF,
                        Context.MODE_PRIVATE).getString("urls", "").equals("")) {
            Log.d("urls string",
                    mContext.getSharedPreferences(UNLConsts.APP_PREF,
                            Context.MODE_PRIVATE).getString("urls", "")
                            .replace("[", "").replace("]", ""));

            ulrs = mContext
                    .getSharedPreferences(UNLConsts.APP_PREF,
                            Context.MODE_PRIVATE).getString("urls", "")
                    .replace("[", "").replace("]", "").split(",");

            if (files.length > 0) {

            }

            listFiles = new String[ulrs.length];
            for (int i = 0; i < listFiles.length; i++) {
                if (ulrs[i].startsWith(" ")) {
                    ulrs[i] = ulrs[i].substring(1, ulrs[i].length());
                }
                listFiles[i] = Environment.getExternalStorageDirectory()
                        .getAbsolutePath()
                        + UNLConsts.VIDEO_DIR_NAME
                        + ulrs[i]
                        .substring(ulrs[i].lastIndexOf('/') + 1,
                                ulrs[i].length()).replace("[", "")
                        .replace("]", "");
            }

            Log.d("urls next", Arrays.asList(listFiles).toString());
            File fs = new File(listFiles[serverIndex]);
            Log.d("next file", fs.getAbsolutePath());
            String currDownFile = prefs.getString("currDownFile", "");
            Log.d("current download", currDownFile);
            if (fs.exists()) {
                if (!currDownFile.equals(fs.getAbsolutePath())) {
                    FileWorks fileWorks = new FileWorks(fs.getAbsolutePath());

                    DirectoryWorks directoryWorks = new DirectoryWorks(
                            UNLConsts.VIDEO_DIR_NAME);
                    String[] refreshFiles = directoryWorks.getDirFileList("folder");
                    Log.d("files", Arrays.asList(refreshFiles).toString());
                    Log.d("ext", fileWorks.getFileExtension());
                    if (!UNLConsts.TEMP_FILE_EXT.equals(fileWorks.getFileExtension())) {
                        if (md5sApp.contains(fileWorks.getFileMD5()) && Arrays.asList(refreshFiles).contains(
                                fs.getAbsolutePath())) {

                            if (serverIndex == listFiles.length - 1) {
                                Log.d("index", String.valueOf(serverIndex));
                                Log.d("len", String.valueOf(listFiles.length));
                                playFile(listFiles[serverIndex]);
                                serverIndex = 0;

                            } else {
                                playFile(listFiles[serverIndex]);
                                Log.d("index", String.valueOf(serverIndex));
                                serverIndex++;
                            }
                        } else {
                            serverIndex++;
                            nextTrack(refreshFiles);
                        }
                    } else {
                        serverIndex++;
                        nextTrack(refreshFiles);
                    }
                } else {
                    serverIndex++;
                    nextTrack(files);
                }

            } else {
                serverIndex++;
                nextTrack(files);
            }
        }

    }


    public void restartDownload() {
        if (!isDownload) {
            downloadVideoTask.cancel(true);
            downloadVideoTask = new DownloadVideoTask(mApp);
            downloadVideoTask.execute();
        }
    }

    public void stopDownload() {
        downloadVideoTask.cancel(true);
    }


    public void restartPlayback() {
        playFile(files[0]);
        serverIndex = 0;
    }

}
