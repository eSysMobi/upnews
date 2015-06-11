package mobi.esys.unl_new_api.download;


import android.content.Context;
import android.content.SharedPreferences;

import com.orhanobut.logger.Logger;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mobi.esys.unl_new_api.filesystem.FilesHelper;
import mobi.esys.unl_new_api.model.UNVideo;
import okio.BufferedSink;
import okio.Okio;

public class DownloadVideoHelper {
    private transient List<UNVideo> mUnVideoList;
    private transient String mDownFolderName;
    private transient SharedPreferences prefs;
    private transient List<String> plMD5;


    public DownloadVideoHelper(String downFolderName, List<UNVideo> unVideoList, Context context) {
        mDownFolderName = downFolderName;
        mUnVideoList = unVideoList;
        prefs = context.getSharedPreferences("unPref", Context.MODE_PRIVATE);
        plMD5 = new ArrayList<>();

        for (int i = 0; i < unVideoList.size(); i++) {
            plMD5.add(unVideoList.get(i).getUnVideoFileInstance().getUnVideoFileMD5());
        }
    }

    public void download() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isDown", true);
        editor.apply();


        for (int i = 0; i < mUnVideoList.size(); i++) {
            if (i == mUnVideoList.size() - 1) {
                downloadVideo(mUnVideoList.get(i).getUnVideoURL(), true);
            } else {
                downloadVideo(mUnVideoList.get(i).getUnVideoURL(), false);
            }
        }
    }


    private void downloadVideo(String url, boolean isOver) {
        OkHttpClient client = new OkHttpClient();
        String fileName = FilenameUtils.getName(url);
        File picFile = new File(mDownFolderName, fileName);

        FilesHelper picFileHelper = new FilesHelper(picFile);

        try {
            if (!picFile.exists()) {
                Logger.d(url);
                Logger.d(picFile.getAbsolutePath());

                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = null;

                response = client.newCall(request).execute();
                BufferedSink sink = Okio.buffer(Okio.sink(picFile));
                sink.write(response.body().bytes());
                sink.close();
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (isOver) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isDown", false);
                editor.apply();
            }
        }
    }


}



