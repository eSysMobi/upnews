package mobi.esys.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mobi.esys.consts.ISConsts;
import mobi.esys.filesystem.directories.DirectoryHelper;
import mobi.esys.network.monitoring.NetMonitor;
import mobi.esys.server.UNHServer;
import mobi.esys.upnewshashtag.UNHApp;

/**
 * Created by Артем on 14.04.2015.
 */
public class DeleteBrokenFilesTask extends AsyncTask<Void, Void, Void> {
    private transient UNHServer server;
    private transient Set<String> md5set;
    private transient SharedPreferences prefs;
    private transient UNHApp mApp;
    //    private transient String mActName;
    private transient Context mContext;


    public DeleteBrokenFilesTask(UNHApp app, Context context) {
        prefs = app.getApplicationContext().getSharedPreferences(ISConsts.PREF_PREFIX, Context.MODE_PRIVATE);
        mApp = app;
        server = new UNHServer(app);
//        mActName = actName;
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (NetMonitor.isNetworkAvailable(mApp)) {
            if (!prefs.getBoolean("isDownload", false)) {
                Log.d("isDelete", "isDel");
                DirectoryHelper directoryWorks = new DirectoryHelper(
                        ISConsts.DIR_NAME.concat(ISConsts.MUSIC_DIR_NAME));
                List<String> folderMD5s = directoryWorks.getMD5Sums();
                List<Integer> maskList = new ArrayList<Integer>();
                md5set = server.getMD5FromServer();
                List<String> md5sList = new ArrayList<String>();
                md5sList.addAll(md5set);
                Log.d("md5 list", md5sList.toString());
                Log.d("md5 folder list", folderMD5s.toString());
                if (md5sList.size() == 0
                        && directoryWorks.getDirFileList("del").length > 0) {
                    maskList.add(0);
                } else {
                    for (int i = 1; i < folderMD5s.size(); i++) {
                        if (!md5sList.contains(folderMD5s.get(i))) {
                            maskList.add(i);
                        }
                    }
                }
                Log.d("mask list task", maskList.toString());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isDeleting", true);
                editor.commit();
                directoryWorks.deleteFilesFromDir(maskList, mApp.getApplicationContext());
            } else {
                cancel(true);
            }
        } else {
            cancel(true);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
//
//        if ("first".equals(mActName)) {
//            ((SliderActivity) mContext).recToMP("video_deleting", "Video delete has been ended");
//        } else {
//            ((MainSliderActivity) mContext).recToMP("video_deleting", "Video delete has been ended");
//
//        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

//        if ("first".equals(mActName)) {
//            ((SliderActivity) mContext).recToMP("video_deleting", "Video delete has been canceled");
//        } else {
//            ((MainSliderActivity) mContext).recToMP("video_deleting", "Video delete has been canceled");
//
//        }
    }
}
