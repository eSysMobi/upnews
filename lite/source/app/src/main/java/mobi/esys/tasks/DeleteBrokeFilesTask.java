package mobi.esys.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mobi.esys.constants.K2Constants;
import mobi.esys.fileworks.DirectiryWorks;
import mobi.esys.upnews_server.K2Server;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class DeleteBrokeFilesTask extends AsyncTask<Void, Void, Void> {
	private transient Context context;
	private transient K2Server k2Server;
	private transient Set<String> md5set;
	private transient SharedPreferences prefs;

	public DeleteBrokeFilesTask(Context context) {
		this.context = context;
		this.k2Server = new K2Server(context);
		this.prefs = context.getSharedPreferences(K2Constants.APP_PREF,
				Context.MODE_PRIVATE);
	}

	@Override
	protected Void doInBackground(Void... params) {
		if (!prefs.getBoolean("isDownload", false)) {
			Log.d("isDelete", "isDel");
			DirectiryWorks directiryWorks = new DirectiryWorks(
					K2Constants.VIDEO_DIR_NAME);
			List<String> folderMD5s = directiryWorks.getMD5Sums();
			List<Integer> maskList = new ArrayList<Integer>();
			md5set = k2Server.getMD5FromServer();
			List<String> md5sList = new ArrayList<String>();
			md5sList.addAll(md5set);
			Log.d("md5 list", md5sList.toString());
			Log.d("md5 folder list", folderMD5s.toString());
			if (md5sList.size() == 0
					&& directiryWorks.getDirFileList("del").length > 0) {
				maskList.add(0);
			} else {
				for (int i = 1; i < folderMD5s.size(); i++) {
					if (!md5sList.contains(folderMD5s.get(i))) {
						maskList.add(i);
					}
				}
			}
			Log.d("mask list task", maskList.toString());
			SharedPreferences.Editor editor = context.getSharedPreferences(
					K2Constants.APP_PREF, Context.MODE_PRIVATE).edit();
			editor.putBoolean("isDeleting", true);
			editor.commit();
			directiryWorks.deleteFilesFromDir(maskList, context);
		} else {
			cancel(true);
		}
		return null;
	}

}
