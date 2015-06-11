package mobi.esys.unl_new_api.filesystem;


import android.os.Environment;

import com.google.common.io.Closer;
import com.orhanobut.logger.Logger;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FilesHelper {
    private transient String fileName;
    private transient String parentDir;
    private transient File fileInstance;


    public FilesHelper(String fileName, String parentDir) {
        this.fileName = fileName;
        this.parentDir = parentDir;
        initFileInstance();
    }

    public FilesHelper(File fileInstance) {
        this.fileInstance = fileInstance;
    }

    private void initFileInstance() {
        fileInstance = new File(Environment.getExternalStorageDirectory().getAbsolutePath().concat(parentDir).concat(fileName));
    }

    public boolean isFileExists() {
        return fileInstance.exists();
    }

    public boolean createFile() throws IOException {
        return isFileExists() || fileInstance.createNewFile();
    }

    public boolean deleteFile() {
        return !isFileExists() || fileInstance.delete();
    }

    public String getMD5Sum() throws IOException {
        String md5 = "";
        Closer closer = Closer.create();
        try {
            InputStream fis = closer.register(new FileInputStream(fileInstance));
            md5 = new String(Hex.encodeHex(DigestUtils.md5(fis)));
            Logger.d(md5);
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
        Logger.d(md5);
        return md5;
    }
}
