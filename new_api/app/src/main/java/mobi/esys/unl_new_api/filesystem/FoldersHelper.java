package mobi.esys.unl_new_api.filesystem;


import android.os.Environment;

import com.orhanobut.logger.Logger;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FoldersHelper {
    private transient String folderName;
    private transient String parentDir;
    private transient File folderInstance;

    public FoldersHelper(String folderName, String parentDir) {
        this.folderName = folderName;
        this.parentDir = parentDir;
        initFolderInstance();
    }

    private void initFolderInstance() {
        if (parentDir.equals("")) {
            folderInstance = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath().concat(File.separator).concat(folderName));
        } else {
            folderInstance = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath().concat(File.separator).concat(parentDir).concat(File.separator).concat(folderName));
        }
        Logger.d(folderInstance.getAbsolutePath());
    }

    public boolean isFolderExist() {
        return folderInstance.exists();
    }

    public void createFolder() {
        if (!folderInstance.exists()) {
            folderInstance.mkdirs();
        }
        Logger.d(folderInstance.getAbsolutePath());
    }

    public void deleteFolder() {
        if (folderInstance.exists()) {
            folderInstance.delete();
        }
    }

    public boolean isEmpty() {
        return getFileList().length > 0;
    }

    public File[] getFileList() {
        return folderInstance.listFiles();
    }

    public List<String> getFolderFileNames() {
        List<String> fileNames = new ArrayList<>();
        File[] fileList = getFileList();
        for (File aFileList : fileList) {
            fileNames.add(FilenameUtils.getName(aFileList.getName()));
        }
        return fileNames;
    }

    public void clearFolder() {
        File[] fileList = getFileList();
        for (File aFileList : fileList) {
            aFileList.delete();
        }
    }

    public void deleteFilesByMask(List<Integer> deleteMask) {
        File[] filesList = getFileList();
        for (int i = 0; i < deleteMask.size(); i++) {
            filesList[deleteMask.get(i)].delete();
        }
    }

    public void deleteTMPFiles() {
        File[] filesList = getFileList();
        for (File aFilesList : filesList) {
            if (FilenameUtils.getExtension(aFilesList.getName()).equals(".tmp")) {
                aFilesList.delete();
            }
        }
    }

    public List<String> getFolderMD5Sums() {
        List<String> md5Sums = new ArrayList<>();
        File[] fileList = getFileList();
        for (File aFileList : fileList) {
            FilesHelper filesHelper = new FilesHelper(aFileList);
            md5Sums.add(filesHelper.getMD5Sum());
            Logger.d(folderName + ": ", md5Sums.toString());
        }
        return md5Sums;
    }


    public File getFolderInstance() {
        return folderInstance;
    }
}
