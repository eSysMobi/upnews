package mobi.esys.downloaders;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import mobi.esys.consts.ISConsts;
import mobi.esys.instagram.model.InstagramPhoto;
import mobi.esys.upnewshashtag.MainSliderActivity;
import okio.BufferedSink;
import okio.Okio;


public class InstagramPhotoDownloader {
    private static final String photoDownDir = Environment.getExternalStorageDirectory()
            .getAbsolutePath().concat(ISConsts.globals.dir_name).concat(ISConsts.globals.photo_dir_name);
    private transient Context mContext;
    private transient boolean mIsMain;
    private transient String lastFileName;


    public InstagramPhotoDownloader(final Context context, final boolean isMain) {
        mContext = context;
        mIsMain = isMain;
    }


    public void download(List<InstagramPhoto> instagramPhotos) {
        Log.d("new download", instagramPhotos.toString());
        lastFileName = "photo".concat(String.valueOf(instagramPhotos.size()).concat(".").concat(FilenameUtils.getExtension(instagramPhotos.get(0).getIgOriginURL())));
        for (int i = 0; i < instagramPhotos.size(); i++) {

            String currFileName = "photo".concat(String.valueOf(i + 1).concat(".").concat(FilenameUtils.getExtension(instagramPhotos.get(0).getIgOriginURL())));
            Log.d("file name", currFileName);

            String url = instagramPhotos.get(i).getIgOriginURL();
            downloadFileAsync(url, currFileName);


        }


    }

    public void downloadFileAsync(String url, final String fileName) {

        Glide.with(mContext).load(url).asBitmap().toBytes().into(new SimpleTarget<byte[]>() {
                                                                     @Override
                                                                     public void onResourceReady(byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                                                                         File picFile = new File(photoDownDir, fileName);
                                                                         Log.d("pic file", picFile.getAbsolutePath());
                                                                         try {
                                                                             if (!picFile.exists()) {

                                                                                 picFile.createNewFile();

                                                                             }


                                                                             BufferedSink sink = Okio.buffer(Okio.sink(picFile));
                                                                             sink.write(resource);
                                                                             sink.close();
                                                                         } catch (
                                                                                 FileNotFoundException e
                                                                                 )

                                                                         {
                                                                             e.printStackTrace();
                                                                         } catch (
                                                                                 IOException e
                                                                                 )

                                                                         {
                                                                             e.printStackTrace();
                                                                         }
                                                                         if (fileName.equals(lastFileName)) {
                                                                             if (mIsMain) {
                                                                                 ((MainSliderActivity) mContext).loadSlide(true);
                                                                             }
                                                                         }
                                                                     }
                                                                 }

        );

    }


}
