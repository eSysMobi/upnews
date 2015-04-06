package mobi.esys.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mobi.esys.constants.UNLConsts;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.net.NetWork;
import mobi.esys.server.UNLServer;
import mobi.esys.upnewslite.FirstVideoActivity;
import mobi.esys.upnewslite.FullscreenActivity;
import mobi.esys.upnewslite.UNLApp;

public class DeleteBrokeFilesTask extends AsyncTask<Void, Void, Void> {
    private transient UNLServer server;
    private transient Set<String> md5set;
    private transient SharedPreferences prefs;
    private transient UNLApp mApp;
    private transient String mActName;
    private transient Context mContext;


    public DeleteBrokeFilesTask(UNLApp app, Context context, String actName) {
        prefs = app.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, Context.MODE_PRIVATE);
        mApp = app;
        server = new UNLServer(app);
        mActName = actName;
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (NetWork.isNetworkAvailable(mApp)) {
            if (!prefs.getBoolean("isDownload", false)) {
                Log.d("isDelete", "isDel");
                DirectoryWorks directoryWorks = new DirectoryWorks(
                        UNLConsts.VIDEO_DIR_NAME);
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

        if ("first".equals(mActName)) {
            ((FirstVideoActivity) mContext).recToMP("video_deleting", "Video delete has been ended");
        } else {
            ((FullscreenActivity) mContext).recToMP("video_deleting", "Video delete has been ended");

        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        if ("first".equals(mActName)) {
            ((FirstVideoActivity) mContext).recToMP("video_deleting", "Video delete has been canceled");
        } else {
            ((FullscreenActivity) mContext).recToMP("video_deleting", "Video delete has been canceled");

        }
    }
}
