package mobi.esys.unl_new_api.download;


import android.content.Context;
import android.content.SharedPreferences;

import com.orhanobut.logger.Logger;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mobi.esys.unl_new_api.filesystem.FilesHelper;
import mobi.esys.unl_new_api.model.UNVideo;
import okio.BufferedSink;
import okio.Okio;

public class DownloadVideoHelper {
    private transient List<UNVideo> mUnVideoList;
    private transient String mDownFolderName;
    private transient SharedPreferences prefs;


    public DownloadVideoHelper(String downFolderName, List<UNVideo> unVideoList, Context context) {
        mDownFolderName = downFolderName;
        mUnVideoList = new ArrayList<>();
        prefs = context.getSharedPreferences("unPref", Context.MODE_PRIVATE);
        mUnVideoList.addAll(unVideoList);
    }

    public void download() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isDown", true);
        editor.apply();

        Collections.sort(mUnVideoList, new Comparator<UNVideo>() {
            @Override
            public int compare(UNVideo lhs, UNVideo rhs) {
                return lhs.getUnOrderNum() - rhs.getUnOrderNum();
            }
        });

        for (int i = 0; i < mUnVideoList.size(); i++) {
            if (i == mUnVideoList.size() - 1) {
                downloadFromURL(mUnVideoList.get(i).getUnVideoURL(), true);
            } else {
                downloadFromURL(mUnVideoList.get(i).getUnVideoURL(), false);
            }
        }
    }


    private void downloadVideo(String url, boolean isOver) {
        OkHttpClient client = new OkHttpClient();
        String fileName = FilenameUtils.getName(url);
        String name = fileName.replace(".mp4", ".tmp").replace(".avi", ".tmp");
        File picFile = new File(mDownFolderName, name);
        File file = new File(mDownFolderName, fileName);


        try {
            if (!file.exists()) {
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


                picFile.renameTo(file);
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
        if (isOver) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isDown", false);
            editor.apply();
        }
    }

    private void downloadFromURL(final String videoUrl, final boolean isOver) {
        InputStream is = null;
        final URL url;
        try {
            url = new URL(videoUrl);
            final HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            is = urlConnection.getInputStream();

            String fileName = FilenameUtils.getName(videoUrl);
            String name = fileName.replace(".mp4", ".tmp").replace(".avi", ".tmp");
            File picFile = new File(mDownFolderName, name);
            File file = new File(mDownFolderName, fileName);
            FilesHelper picFileHelper = new FilesHelper(picFile);

            if (!file.exists()) {
                writeFile(is, picFile, file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isOver) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isDown", false);
                editor.apply();
            }
        }
        if (isOver) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isDown", false);
            editor.apply();
        }

    }


    private void writeFile(final InputStream is, File tempFile, final File endFile) throws Exception {
//        if (tempFile.exists()) {
//            tempFile.delete();
//        }
        tempFile.createNewFile();

        FileOutputStream fos = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int len1 = 0;

        if (is != null) {
            while ((len1 = is.read(buffer)) > 0) {
                fos.write(buffer, 0, len1);

            }
        }
        fos.close();
        tempFile.renameTo(endFile);
    }
}



