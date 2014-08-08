package mobi.esys.fileworks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mobi.esys.constants.K2Constants;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

public class DirectiryWorks {
	private transient String directoryPath;
	private static final String DIR_WORKS_TAG = "DirectoryWorks";

	public DirectiryWorks(String directoryPath) {
		this.directoryPath = directoryPath;
	}

	public void createDir() {
		File videoDir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + this.directoryPath);
		if (!videoDir.exists()) {
			videoDir.mkdirs();
		}
	}

	public String[] getDirFileList() {
		File videoDir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + this.directoryPath);
		Log.d(DIR_WORKS_TAG, videoDir.getAbsolutePath());
		List<String> filePaths = new ArrayList<String>();
		if (videoDir.exists()) {
			File[] files = videoDir.listFiles();
			for (File file : files) {
				filePaths.add(file.getPath());
			}
		} else {
			Log.d(DIR_WORKS_TAG, "folder don't exist");
		}

		return filePaths.toArray(new String[filePaths.size()]);
	}

	public void deleteFilesFromDir(List<Integer> maskList, Context context) {
		File videoDir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + this.directoryPath);
		Log.d(DIR_WORKS_TAG, "deleteFilesFromDir");
		Log.d(DIR_WORKS_TAG, Environment.getExternalStorageDirectory()
				.getAbsolutePath() + this.directoryPath);

		Log.d("mask list task", maskList.toString());
		if (videoDir.exists()) {
			int ci = context.getSharedPreferences(K2Constants.APP_PREF,
					Context.MODE_PRIVATE).getInt("currPlIndex", 0);

			File[] files = videoDir.listFiles();

			if (maskList.size() == 1 && maskList.get(0) == 0) {
				for (int i = 0; i < files.length; i++) {
					if (!files[i].getName().startsWith("dd"))
						files[i].delete();
				}
			} else {

				for (int i = 0; i < files.length; i++) {

					if (maskList.contains(i)) {

						if (files[i].exists()
								&& (!files[i].getName().startsWith("dd"))
								&& ci != i) {
							files[i].delete();
						}
					}
				}
			}
			SharedPreferences.Editor editor = context.getSharedPreferences(
					K2Constants.APP_PREF, Context.MODE_PRIVATE).edit();
			editor.putBoolean("isDeleting", false);
			editor.commit();
		} else {
			Log.d(DIR_WORKS_TAG, "Folder don't exists");
		}
	}

	public List<String> getMD5Sums() {
		String[] files = getDirFileList();
		List<String> dirMD5s = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {
			FileWorks fileWorks = new FileWorks(files[i]);
			dirMD5s.add(fileWorks.getFileMD5());
		}
		return dirMD5s;
	}

	public boolean contains(final int[] mask, final int i) {
		boolean isCont = false;
		for (final int e : mask) {

			if (e == i || i == e) {

				isCont = true;
			} else {
				isCont = false;
			}

		}
		Log.d(DIR_WORKS_TAG, String.valueOf(isCont));
		return isCont;

	}
}
