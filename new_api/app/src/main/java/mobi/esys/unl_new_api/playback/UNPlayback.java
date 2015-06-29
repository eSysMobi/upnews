package mobi.esys.unl_new_api.playback;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mobi.esys.unl_new_api.R;
import mobi.esys.unl_new_api.VideoActivity;
import mobi.esys.unl_new_api.filesystem.FilesHelper;
import mobi.esys.unl_new_api.filesystem.FoldersHelper;
import mobi.esys.unl_new_api.model.UNPlaylist;
import mobi.esys.unl_new_api.model.UNVideo;
import mobi.esys.unl_new_api.task.DownloadVideoTask;
import mobi.esys.unl_new_api.un_api.UNApi;

public class UNPlayback {
    private transient VideoView mVideoView;
    private transient Context mContext;
    private transient FoldersHelper videoDirHelper;
    private transient UNVideo nextUnVideo;
    private transient UNVideo lastUnVideo;
    private transient SharedPreferences prefs;
    private transient UNPlaylist playlist;
    private transient Map<String, Integer> pTimes;
    private transient UNVideo[] unVideoList;
    private transient List<String> plMD5;
    private transient int videoIndex = 0;
    private transient int folderIndex = 0;

    public UNPlayback(VideoView videoView, final Context context, final UNPlaylist servPlaylist, UNVideo servUnVideo) {
        Logger.d("serv video" + servUnVideo.toString());

        mVideoView = videoView;
        mContext = context;
        playlist = servPlaylist;

        pTimes = new HashMap<>();

        plMD5 = new ArrayList<>();


        String videoDir = mContext.getResources().getString(R.string.video_dir);
        String baseDir = mContext.getResources().getString(R.string.base_dir);

        videoDirHelper = new FoldersHelper(videoDir, baseDir);

        prefs = mContext.getSharedPreferences("unPref", Context.MODE_PRIVATE);


        Logger.d("folder name ".concat(videoDirHelper.getFolderInstance().getAbsolutePath()));
        Logger.d(videoDirHelper.getFolderFileNames().toString());
        Logger.d(playlist.getUnPlaylistID());

        clearLastVideo();

        UNApi.getPlaylistVideos("1");


        Gson gson = new Gson();
        String jsonText = prefs.getString("pl", null);
        unVideoList = gson.fromJson(jsonText, UNVideo[].class);

        lastUnVideo = unVideoList[0];


        Logger.d("playlist videos: ".concat(Arrays.toString(unVideoList)));

        for (UNVideo anUnVideoList : unVideoList) {
            plMD5.add(anUnVideoList.getUnVideoFileInstance().getUnVideoFileMD5());
        }


        play();


        String videoDownDir = Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(File.separator).concat(mContext.getString(R.string.base_dir)).concat(File.separator).concat(mContext.getString(R.string.video_dir));
        List<UNVideo> unVideos = new ArrayList<>();

        unVideos.clear();


        Collections.addAll(unVideos, unVideoList);

        Logger.d(unVideos.toString());
        Logger.d(videoDownDir);

        Collections.sort(unVideos, new Comparator<UNVideo>() {
            @Override
            public int compare(UNVideo lhs, UNVideo rhs) {
                return lhs.getUnOrderNum() - rhs.getUnOrderNum();
            }
        });


        DownloadVideoTask.download(mContext, videoDownDir, unVideos);

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
            Integer pTimesC = 0;
            if (pTimes.containsKey(lastUnVideo.getUnVideoID())) {
                pTimesC = pTimes.get(lastUnVideo.getUnVideoID());
            }

            ((VideoActivity) mContext).getNextVideo("1", lastUnVideo.getUnVideoID(), pTimesC, lastUnVideo.getUnOrderNum());

            int videoIndex = prefs.getInt("nextVideoIndex", 0);
            nextUnVideo = unVideoList[videoIndex];

            Logger.d("pt: ".concat(String.valueOf(pTimesC)));
            play();

        } else {
            play();
        }

    }


    private void play() {
        File[] files = videoDirHelper.getFileList();
        if (files.length > 0) {
            if (nextUnVideo != null) {

                lastUnVideo = nextUnVideo;

                Logger.d("plb next ".concat(nextUnVideo.toString()));
                Logger.d("plb next ".concat(videoDirHelper.getFolderMD5Sums().toString()));
                Logger.d("plb next ".concat(FilenameUtils.getName(nextUnVideo.getUnVideoURL())));
                Logger.d("plb next ".concat(videoDirHelper.getFolderFileNames().toString()));

                File plFile = new File(videoDirHelper
                        .getFolderInstance().getAbsolutePath(), FilenameUtils.getName(nextUnVideo.getUnVideoURL()));

                FilesHelper filesHelper = new FilesHelper(plFile);

                Uri plURI = Uri.parse(plFile.getAbsolutePath());

                Logger.d("current file: ".concat(plFile.getAbsolutePath()));
                Logger.d("current file md5: ".concat(filesHelper.getMD5Sum()));


                Logger.d(nextUnVideo.getUnVideoFileInstance()
                        .getUnVideoFileMD5());


                if (filesHelper.getMD5Sum().equals(nextUnVideo.getUnVideoFileInstance()
                        .getUnVideoFileMD5())
                        && videoDirHelper.getFolderFileNames()
                        .contains(FilenameUtils.getName(nextUnVideo.getUnVideoURL())) && plFile.length() > 0
                        && plFile.exists()) {


                    mVideoView.setVideoURI(plURI);
                    mVideoView.start();
                    saveLastVideo(FilenameUtils.getName(nextUnVideo.getUnVideoURL()));
                    if (pTimes.containsKey(nextUnVideo.getUnVideoID())) {
                        Integer pTimesCurr = pTimes.get(nextUnVideo.getUnVideoID());
                        pTimes.put(nextUnVideo.getUnVideoID(), pTimesCurr + 1);
                    } else {
                        pTimes.put(nextUnVideo.getUnVideoID(), 1);
                    }


                }


            } else {
                if (unVideoList.length > 0) {


                    File pltFile = new File(videoDirHelper
                            .getFolderInstance().getAbsolutePath(), FilenameUtils.getName(unVideoList[videoIndex].getUnVideoURL()));

                    FilesHelper filesHelper = new FilesHelper(pltFile);


                    Logger.d("md5:".concat(filesHelper.getMD5Sum()));
                    Logger.d("pl md5:".concat(plMD5.toString()));

                    if (pltFile.exists() && !FilenameUtils.getExtension(pltFile.getName()).equals(".tmp") && plMD5.contains(filesHelper.getMD5Sum())) {
                        mVideoView.setVideoURI(Uri.parse(pltFile.getAbsolutePath()));
                        mVideoView.start();


                        saveLastVideo(FilenameUtils.getName(pltFile.getAbsolutePath()));


                        if (videoIndex == unVideoList.length - 1) {
                            videoIndex = 0;
                        } else {
                            videoIndex++;
                        }
                    } else {
                        nextVideo();
                    }
                } else {
                    File[] plFiles = videoDirHelper.getFileList();

                    FilesHelper filesHelper = new FilesHelper(plFiles[folderIndex]);


                    Logger.d("md5:".concat(filesHelper.getMD5Sum()));
                    Logger.d("pl md5:".concat(plMD5.toString()));

                    if (plFiles[folderIndex].exists() && !FilenameUtils.getExtension(plFiles[folderIndex].getName()).equals(".tmp") && plMD5.contains(filesHelper.getMD5Sum())) {
                        mVideoView.setVideoURI(Uri.parse(plFiles[folderIndex].getAbsolutePath()));
                        mVideoView.start();


                        saveLastVideo(FilenameUtils.getName(plFiles[folderIndex].getAbsolutePath()));


                        if (folderIndex == files.length - 1) {
                            folderIndex = 0;
                        } else {
                            folderIndex++;
                        }
                    }
                    nextVideo();
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

    public void restartDownload() {
        String videoDownDir = Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(File.separator).concat(mContext.getString(R.string.base_dir)).concat(File.separator).concat(mContext.getString(R.string.video_dir));
        List<UNVideo> unVideos = new ArrayList<>();

        Collections.addAll(unVideos, unVideoList);

        Logger.d(unVideos.toString());
        Logger.d(videoDownDir);


        if (!prefs.getBoolean("isDown", false)) {
            DownloadVideoTask.download(mContext, videoDownDir, unVideos);
        }
    }
}

