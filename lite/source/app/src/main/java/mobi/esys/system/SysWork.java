package mobi.esys.system;

import mobi.esys.upnewslite.UNLApp;

/**
 * Created by Артем on 02.03.2015.
 */
public class SysWork {
    public static void trimCache(UNLApp app) {
        try {
            java.io.File dir = app.getApplicationContext().getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
        }
    }

    private static boolean deleteDir(java.io.File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new java.io.File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
}
