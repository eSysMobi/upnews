package mobi.esys.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

import mobi.esys.consts.ISConsts;
import mobi.esys.instagram.model.InstagramPhoto;

/**
 * Created by Артем on 17.04.2015.
 */
public class DownloadInstagramPhotoTask extends AsyncTask<List<InstagramPhoto>, Void, Void> {
    private static final File photoDownDir = new File(Environment.getExternalStorageDirectory()
            .getAbsolutePath().concat(ISConsts.DIR_NAME).concat(ISConsts.PHOTO_DIR_NAME));
    private transient Context mContext;

    public DownloadInstagramPhotoTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(List<InstagramPhoto>... params) {
        for (int i = 0; i < params[0].size(); i++) {
            downloadPhoto(params[0].get(i).getIgOriginURL(), "photo".concat(params[0].get(i).getIgPhotoID()).concat(".jpg"));
        }
        return null;
    }

    private void downloadPhoto(String url, String name) {
        try {
            java.net.URL link = new java.net.URL(url);
            HttpURLConnection connection = (HttpURLConnection) link
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            File f = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath().concat(ISConsts.DIR_NAME).concat(ISConsts.PHOTO_DIR_NAME), name);
            f.createNewFile();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            Log.d("Downloader", e.getMessage());
            return;
        }
        return;
    }
}
