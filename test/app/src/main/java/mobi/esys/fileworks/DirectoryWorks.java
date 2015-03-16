package mobi.esys.fileworks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import mobi.esys.constants.UNLConsts;

public class DirectoryWorks {
    private transient String directoryPath;
    private static final String DIR_WORKS_TAG = "DirectoryWorks";

    public DirectoryWorks(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public void createDir() {
        File videoDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(this.directoryPath));
        if (!videoDir.exists()) {
            videoDir.mkdirs();
        }
    }

    public String[] getDirFileList(String mess) {
        File videoDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(this.directoryPath));
        Log.d(DIR_WORKS_TAG, videoDir.getAbsolutePath());
        List<String> filePaths = new ArrayList<>();
        if (videoDir.exists()) {
            File[] files = videoDir.listFiles();
            for (File file : files) {
                if (file.exists()) {
                    filePaths.add(file.getPath());
                } else {
                    continue;
                }
            }
            Log.d("files dir works" + mess, filePaths.toString());
        } else {
            Log.d(DIR_WORKS_TAG, "folder don't exist");
        }

        return filePaths.toArray(new String[filePaths.size()]);
    }

    public void deleteFilesFromDir(List<Integer> maskList, Context context) {
        File videoDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(this.directoryPath));
        Log.d(DIR_WORKS_TAG, "deleteFilesFromDir");
        Log.d(DIR_WORKS_TAG, Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(this.directoryPath));

        Log.d("mask list task", maskList.toString());
        if (videoDir.exists()) {
            int ci = context.getSharedPreferences(UNLConsts.APP_PREF,
                    Context.MODE_PRIVATE).getInt("currPlIndex", 0);

            File[] files = videoDir.listFiles();

            if (maskList.size() == 1 && maskList.get(0) == 0) {

                for (int i = 0; i < files.length; i++) {
                    Date modDate = new Date(files[i].lastModified());
                    Calendar today = Calendar.getInstance();


                    long diff = today.getTimeInMillis() - modDate.getTime();
                    long days = diff / (24 * 60 * 60 * 1000);
                    if (getFileExtension(files[i].getName()).equals(UNLConsts.TEMP_FILE_EXT) && days > 14) {
                        files[i].delete();
                    } else {
                        files[i].delete();
                    }
                }
            } else {

                for (int i = 0; i < files.length; i++) {

                    if (maskList.contains(i)) {

                        Date modDate = new Date(files[i].lastModified());
                        Calendar today = Calendar.getInstance();


                        long diff = today.getTimeInMillis() - modDate.getTime();
                        long days = diff / (24 * 60 * 60 * 1000);
                        if ((files[i].exists() && ci != i) || (
                                (getFileExtension(files[i].getName()).equals(UNLConsts.TEMP_FILE_EXT) && days > 14)
                        )) {
                            files[i].delete();
                        }
                    }
                }
            }
            SharedPreferences.Editor editor = context.getSharedPreferences(
                    UNLConsts.APP_PREF, Context.MODE_PRIVATE).edit();
            editor.putBoolean("isDeleting", false);
            editor.commit();
        } else {
            Log.d(DIR_WORKS_TAG, "Folder don't exists");
        }
    }

    public List<String> getMD5Sums() {
        String[] files = getDirFileList("getMD5SUM");
        List<String> dirMD5s = new ArrayList<String>();
        for (int i = 0; i < files.length; i++) {
            FileWorks fileWorks = new FileWorks(files[i]);
            File file = new File(files[i]);
            if (file.exists()) {
                dirMD5s.add(fileWorks.getFileMD5());
            }
        }
        return dirMD5s;
    }


    public String getFileExtension(String fileName) {
        String ext = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0 && i < fileName.length() - 1) {
            ext = fileName.substring(i + 1);
        }
        Log.d("fw_tag", ext);
        return ext;
    }
}
