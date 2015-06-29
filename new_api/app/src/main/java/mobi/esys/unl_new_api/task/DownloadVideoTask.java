package mobi.esys.unl_new_api.task;


import android.content.Context;
import android.content.SharedPreferences;

import com.nanotasks.BackgroundWork;
import com.nanotasks.Completion;
import com.nanotasks.Tasks;
import com.orhanobut.logger.Logger;

import java.util.List;

import mobi.esys.unl_new_api.R;
import mobi.esys.unl_new_api.download.DownloadVideoHelper;
import mobi.esys.unl_new_api.filesystem.FoldersHelper;
import mobi.esys.unl_new_api.model.UNVideo;

public class DownloadVideoTask {

    public static void download(final Context context, final String downFolderName, final List<UNVideo> unVideoList) {
        Tasks.executeInBackground(context, new BackgroundWork<Void>() {
            @Override
            public Void doInBackground() throws Exception {
                Logger.d(downFolderName);
                Logger.d(unVideoList.toString());

                DownloadVideoHelper downloadVideoHelper = new DownloadVideoHelper(downFolderName
                        , unVideoList, context);
                downloadVideoHelper.download();
                return null;
            }
        }, new Completion<Void>() {
            @Override
            public void onSuccess(Context context, Void aVoid) {
                SharedPreferences prefs = context.getSharedPreferences("unPref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isDown", false);
                editor.apply();


                String videoDir = context.getResources().getString(R.string.video_dir);
                String baseDir = context.getResources().getString(R.string.base_dir);

                FoldersHelper videoDirHelper = new FoldersHelper(videoDir, baseDir);
                videoDirHelper.deleteTMPFiles();
            }

            @Override
            public void onError(Context context, Exception e) {
                SharedPreferences prefs = context.getSharedPreferences("unPref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isDown", false);
                editor.apply();
            }

        });
    }
}
