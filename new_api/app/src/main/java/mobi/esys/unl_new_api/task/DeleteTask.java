package mobi.esys.unl_new_api.task;


import android.content.Context;

import com.nanotasks.BackgroundWork;
import com.nanotasks.Completion;
import com.nanotasks.Tasks;

import java.util.ArrayList;
import java.util.List;

import mobi.esys.unl_new_api.R;
import mobi.esys.unl_new_api.filesystem.FoldersHelper;
import mobi.esys.unl_new_api.model.UNVideo;

final public class DeleteTask {


    public static void delete(final Context context, final List<UNVideo> unVideoList, final String plID, final String downFolderName) {
        Tasks.executeInBackground(context, new BackgroundWork<Void>() {
            @Override
            public Void doInBackground() throws Exception {
                deleting(context, unVideoList);
                return null;
            }
        }, new Completion<Void>() {
            @Override
            public void onSuccess(Context context, Void aVoid) {
                DownloadVideoTask.download(context, downFolderName, unVideoList);
            }

            @Override
            public void onError(Context context, Exception e) {
                DownloadVideoTask.download(context, downFolderName, unVideoList);
            }
        });
    }

    private static void deleting(Context context, List<UNVideo> unVideoList) {
        List<String> plMD5 = new ArrayList<>();
        List<String> folderMD5;
        List<Integer> deleteMask = new ArrayList<>();


        String videoDir = context.getResources().getString(R.string.video_dir);
        String baseDir = context.getResources().getString(R.string.base_dir);

        FoldersHelper videoDirHelper = new FoldersHelper(videoDir, baseDir);
        folderMD5 = videoDirHelper.getFolderMD5Sums();
        for (int i = 0; i < unVideoList.size(); i++) {
            plMD5.add(unVideoList.get(i).getUnVideoFileInstance().getUnVideoFileMD5());
        }

        for (int j = 0; j < folderMD5.size(); j++) {
            if (!plMD5.contains(folderMD5.get(j))) {
                deleteMask.add(j);
            }
            if (j == folderMD5.size() - 1) {
                videoDirHelper.deleteFilesByMask(deleteMask);
            }
        }

    }
}

