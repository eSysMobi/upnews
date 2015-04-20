package mobi.esys.server;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import mobi.esys.consts.ISConsts;
import mobi.esys.googledrive.model.model.GDFile;
import mobi.esys.upnewshashtag.UNHApp;
import mobi.esys.network.monitoring.NetMonitor;

/**
 * Created by Артем on 14.04.2015.
 */
public class UNHServer {
    private transient String folderId;
    private transient SharedPreferences prefs;
    private transient List<GDFile> gdFiles;
    private transient static Drive drive;
    private transient GDFile gdRSS;
    private transient Context context;
    private transient UNHApp mApp;


    public UNHServer(UNHApp app) {
        mApp = app;
        context = app.getApplicationContext();
        prefs = app.getApplicationContext().getSharedPreferences(ISConsts.PREF_PREFIX, Context.MODE_PRIVATE);
        drive = app.getDriveService();
        folderId = prefs.getString("folderId", "");
        gdFiles = new ArrayList<>();
    }

    @SuppressLint("LongLogTag")
    public Set<String> getMD5FromServer() {
        saveURLS();

        Set<String> resultMD5 = new HashSet<String>();
        Set<String> defaultSet = new HashSet<String>();

        if (NetMonitor.isNetworkAvailable(mApp)) {
            try {
                printFilesInFolder(folderId);
                for (int i = 0; i < gdFiles.size(); i++) {
                    if (Arrays.asList(ISConsts.UNH_ACCEPTED_FILE_EXTS)
                            .contains(gdFiles.get(i).getGdFileInst().getFileExtension())) {
                        resultMD5.add(gdFiles.get(i).getGdFileMD5());
                    }
                }
                Log.d("md5 server size", String.valueOf(resultMD5.size()));
            } catch (IOException e) {
                Log.d("get md5 from server error", e.getLocalizedMessage());
            }

            if (!prefs.getStringSet("md5sApp", defaultSet).equals(resultMD5)) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putStringSet("md5sApp", resultMD5);
                editor.commit();
            }
        } else {
            resultMD5 = prefs.getStringSet("md5sApp", resultMD5);
        }
        return resultMD5;
    }

    private void saveURLS() {
        List<String> resultURL = new ArrayList<>();
        List<String> defultURL = new ArrayList<>();
        if (NetMonitor.isNetworkAvailable(mApp)) {
            try {
                Log.d("save urls", "save urls");
                printFilesInFolder(folderId);
                for (int i = 0; i < gdFiles.size(); i++) {
                    resultURL.add(gdFiles.get(i).getGdFileName());
                }
            } catch (IOException e) {
                Log.d("save url error", e.getLocalizedMessage());
            }
            Collections.sort(resultURL, new Comparator<String>() {

                @Override
                public int compare(String lhs, String rhs) {
                    return lhs.toLowerCase(Locale.getDefault()).compareTo(
                            rhs.toLowerCase(Locale.getDefault()));
                }

            });
            Log.d("saved urls", resultURL.toString());
            SharedPreferences.Editor editor = prefs.edit();
            String prefURL = prefs.getString("urls", defultURL.toString());
            if (!prefURL.equals(resultURL)) {
                editor.putString("urls", resultURL.toString());
            }
            Set<String> urlsSet = new HashSet<String>();
            Set<String> defaultSet = new HashSet<String>();
            for (int i = 0; i < resultURL.size(); i++) {
                urlsSet.add(Environment.getExternalStorageDirectory()
                        + ISConsts.DIR_NAME + resultURL.get(i));
            }
            if (!prefs.getStringSet("filesServer", defaultSet).equals(urlsSet)) {
                editor.putStringSet("filesServer", urlsSet);
            }
            editor.commit();
        }
    }


    private void printFilesInFolder(String folderId)
            throws IOException {
        Log.d("UNLServer", "print google drive folder");
        Drive.Children.List request = drive.children().list(folderId);

        do {
            try {
                ChildList children = request.execute();

                for (ChildReference child : children.getItems()) {

                    File file = drive.files().get(child.getId()).execute();
                    if (Arrays.asList(ISConsts.UNH_ACCEPTED_FILE_EXTS)
                            .contains(file.getFileExtension())) {
                        gdFiles.add(new GDFile(file.getId(), file.getTitle(),
                                file.getDownloadUrl(), String.valueOf(file
                                .getFileSize()), file
                                .getFileExtension(), file
                                .getMd5Checksum(), file));
                    }

                    if ("rss.txt".equals(file.getTitle())) {
                        gdRSS = new GDFile(file.getId(), file.getTitle(),
                                file.getWebContentLink(), String.valueOf(file
                                .getFileSize()), file
                                .getFileExtension(), file
                                .getMd5Checksum(), file);
                    }


                }
                request.setPageToken(children.getNextPageToken());
            } catch (IOException e) {
                System.out.println("An error occurred: " + e);
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null
                && request.getPageToken().length() > 0);

    }


    public GDFile getGdRSS() {
        try {
            printFilesInFolder(folderId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        GDFile rss = new GDFile("1", "empty",
                "empty", String.valueOf(0), "empty", "empty", new File());
        if (gdRSS != null) {
            rss = gdRSS;
        }
        return rss;
    }


    public List<GDFile> getGdFiles() {
        return gdFiles;
    }
}
