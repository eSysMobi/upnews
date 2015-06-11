package mobi.esys.unl_new_api.playback;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.VideoView;

import com.orhanobut.logger.Logger;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;

import mobi.esys.unl_new_api.R;
import mobi.esys.unl_new_api.VideoActivity;
import mobi.esys.unl_new_api.filesystem.FoldersHelper;
import mobi.esys.unl_new_api.model.UNPlaylist;
import mobi.esys.unl_new_api.model.UNVideo;
import mobi.esys.unl_new_api.un_api.UNApi;

public class UNPlayback {
    private transient VideoView mVideoView;
    private transient Context mContext;
    private transient FoldersHelper videoDirHelper;
    private transient UNVideo nextUnVideo;
    private transient UNVideo lastUnVideo;
    private transient SharedPreferences prefs;
    private transient int folderIndex;
    private transient UNPlaylist playlist;

    public UNPlayback(VideoView videoView, final Context context, final UNPlaylist servPlaylist, UNVideo servUnVideo) {
        Logger.d("serv video" + servUnVideo.toString());
        lastUnVideo = servUnVideo;
        folderIndex = 0;
        mVideoView = videoView;
        mContext = context;
        playlist = servPlaylist;


        String videoDir = mContext.getResources().getString(R.string.video_dir);
        String baseDir = mContext.getResources().getString(R.string.base_dir);

        videoDirHelper = new FoldersHelper(videoDir, baseDir);

        prefs = mContext.getSharedPreferences("unPref", Context.MODE_PRIVATE);


        Logger.d("folder name ".concat(videoDirHelper.getFolderInstance().getAbsolutePath()));
        Logger.d(videoDirHelper.getFolderFileNames().toString());
        Logger.d(playlist.getUnPlaylistID());

        clearLastVideo();

        UNApi.getPlaylistVideos(playlist.getUnPlaylistID());
        nextUnVideo = lastUnVideo;

        nextVideo();
    }


    public void nextVideo() {
        Logger.d("files".concat(Arrays.toString(videoDirHelper.getFileList())));
        if (videoDirHelper.getFileList().length == 0) {
            Logger.d("folder empty");
            mVideoView.setVideoURI(Uri.parse("android.resource://".concat(mContext.getPackageName()).concat("/assets/").concat(String.valueOf(R.raw.emb))));
            mVideoView.start();
            saveLastVideo("emb.mp4");

        } else {
            getNextVideo();
        }

    }


    private void getNextVideo() {
        if (lastUnVideo != null) {
            int pt = lastUnVideo.getUnVideoPT();

            nextUnVideo = ((VideoActivity) mContext).getNextVideo(playlist.getUnPlaylistID(), lastUnVideo.getUnVideoID(), pt);
            Logger.d("pt: ".concat(String.valueOf(pt)));
            play();

        } else

        {
            play();
        }

    }


    private void play() {
        File[] files = videoDirHelper.getFileList();
        if (files.length > 0) {
            if (nextUnVideo != null) {
                int pt = nextUnVideo.getUnVideoPT();
                Logger.d("plb next ".concat(nextUnVideo.toString()));
                Logger.d("plb next ".concat(videoDirHelper.getFolderMD5Sums().toString()));
                Logger.d("plb next ".concat(FilenameUtils.getName(nextUnVideo.getUnVideoURL())));
                Logger.d("plb next ".concat(videoDirHelper.getFolderFileNames().toString()));

                if (videoDirHelper.getFolderMD5Sums().contains(nextUnVideo.getUnVideoFileInstance()
                        .getUnVideoFileMD5()) && videoDirHelper.getFolderFileNames()
                        .contains(FilenameUtils.getName(nextUnVideo.getUnVideoURL()))) {
                    mVideoView.setVideoURI(Uri.parse(videoDirHelper
                            .getFolderInstance().getAbsolutePath()
                            .concat(File.separator)
                            .concat(FilenameUtils.getName(nextUnVideo.getUnVideoURL())
                            )));
                    mVideoView.start();
                    saveLastVideo(FilenameUtils.getName(nextUnVideo.getUnVideoURL()));
                    lastUnVideo.setUnVideoPT(pt + 1);

                    lastUnVideo = nextUnVideo;
                }

            } else {
                Logger.d("plb next".concat(files[folderIndex].getAbsolutePath()));
                files = videoDirHelper.getFileList();
                mVideoView.setVideoURI(Uri.parse(files[folderIndex].getAbsolutePath()));
                mVideoView.start();
                saveLastVideo(FilenameUtils.getName(files[folderIndex].getAbsolutePath()));
                if (folderIndex == files.length - 1) {
                    folderIndex = 0;
                } else {
                    folderIndex++;
                }
            }
        } else {
            Logger.d("plb next".concat("android.resource://"
                    .concat(mContext.getPackageName())
                    .concat("/assets/")
                    .concat(String.valueOf(R.raw.emb))));
            Logger.d("folder empty");
            mVideoView.setVideoURI(Uri.parse("android.resource://"
                    .concat(mContext.getPackageName())
                    .concat("/assets/")
                    .concat(String.valueOf(R.raw.emb))));
            mVideoView.start();
            saveLastVideo("emb.mp4");
        }
    }

    private void saveLastVideo(String name) {
        Logger.d("name: ".concat(name));
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("video", name);
        editor.apply();
    }

    private void clearLastVideo() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("video", "");
        editor.apply();
    }

    public UNPlaylist getPlaylist() {
        return playlist;
    }
}

