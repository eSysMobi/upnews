package mobi.esys.filesystem.directories;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import mobi.esys.consts.ISConsts;
import mobi.esys.filesystem.files.FilesHelper;

/**
 * Created by Артем on 14.04.2015.
 */
public class DirectoryHelper {
    private transient String directoryPath;
    private final String TAG = this.getClass().getSimpleName().concat(ISConsts.globals.default_logtag_devider);

    public DirectoryHelper(String directoryPath) {
        this.directoryPath = directoryPath;
    }


    public String[] getDirFileList(String mess) {
        File videoDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(this.directoryPath));
        Log.d(TAG, videoDir.getAbsolutePath());
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
            Log.d(TAG, mess.concat(" @ ").concat(filePaths.toString()));
        } else {
            Log.d(TAG, "folder don't exist");
        }

        return filePaths.toArray(new String[filePaths.size()]);
    }

    public void deleteFilesFromDir(List<Integer> maskList, Context context) {
        File videoDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(this.directoryPath));
        Log.d(TAG, "deleteFilesFromDir");
        Log.d(TAG, Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(this.directoryPath));

        Log.d(TAG, maskList.toString());
        if (videoDir.exists()) {
            int ci = context.getSharedPreferences(ISConsts.globals.pref_prefix,
                    Context.MODE_PRIVATE).getInt("currPlIndex", 0);

            File[] files = videoDir.listFiles();

            if (maskList.size() == 1 && maskList.get(0) == 0) {

                for (int i = 0; i < files.length; i++) {
                    Date modDate = new Date(files[i].lastModified());
                    Calendar today = Calendar.getInstance();


                    long diff = today.getTimeInMillis() - modDate.getTime();
                    long days = diff / (24 * 60 * 60 * 1000);
                    if (getFileExtension(files[i].getName()).equals(ISConsts.globals.temp_file_ext) && days > 14) {
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
                                (getFileExtension(files[i].getName()).equals(ISConsts.globals.temp_file_ext) && days > 14)
                        )) {
                            files[i].delete();
                        }
                    }
                }
            }
            SharedPreferences.Editor editor = context.getSharedPreferences(
                    ISConsts.globals.pref_prefix, Context.MODE_PRIVATE).edit();
            editor.putBoolean("isDeleting", false);
            editor.commit();
        } else {
            Log.d(TAG, "Folder don't exists");
        }
    }

    public List<String> getMD5Sums() {
        String[] files = getDirFileList("getMD5SUM");
        List<String> dirMD5s = new ArrayList<String>();
        for (int i = 0; i < files.length; i++) {
            FilesHelper fileWorks = new FilesHelper(files[i]);
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
        Log.d(TAG, ext);
        return ext;
    }
}
