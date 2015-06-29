package mobi.esys.unl_new_api.filesystem;


import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    public String getMD5Sum() {
        byte[] b;

        b = createChecksum(fileInstance.getAbsolutePath());

        String result = "";

        for (byte aB : b) {
            result += Integer.toString((aB & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    private static byte[] createChecksum(String filename) {
        InputStream fis;
        byte[] sum = new byte[1];
        try {
            fis = new FileInputStream(filename);

            byte[] buffer = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;

            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            fis.close();
            sum = complete.digest();

        } catch (NoSuchAlgorithmException | IOException ignored) {
        }

        return sum;
    }

}
