package mobi.esys.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mobi.esys.constants.K2Constants;
import mobi.esys.fileworks.DirectiryWorks;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class DeleteBrokeFilesTask extends AsyncTask<Void, Void, Void> {
	private transient Context context;
	private transient Set<String> md5set;

	public DeleteBrokeFilesTask(Context context) {
		this.context = context;
	}

	@Override
	protected Void doInBackground(Void... params) {
		DirectiryWorks directiryWorks = new DirectiryWorks(context,
				K2Constants.VIDEO_DIR_NAME);
		String[] folderMD5s = directiryWorks.getMD5Sums();
		Set<String> defSet = new HashSet<String>();
		defSet.add(K2Constants.FIRST_MD5);
		List<Integer> maskList = new ArrayList<Integer>();
		md5set = context.getSharedPreferences(K2Constants.APP_PREF,
				Context.MODE_PRIVATE).getStringSet("md5sApp", defSet);
		List<String> md5sList = new ArrayList<String>(md5set);
		Log.d("md5", md5sList.toString());
		if (md5sList.isEmpty()) {
			maskList.add(0);
		} else {
			for (int i = 0; i < folderMD5s.length; i++) {
				if (md5sList.indexOf(folderMD5s[i]) == -1) {
					maskList.add(i);
				}
			}
		}
		SharedPreferences.Editor editor = context.getSharedPreferences(
				K2Constants.APP_PREF, Context.MODE_PRIVATE).edit();
		editor.putBoolean("isDeleting", true);
		editor.commit();
		directiryWorks.deleteFilesFromDir(maskList, context);
		return null;
	}
}
