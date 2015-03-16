package mobi.esys.tasks;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.common.io.ByteStreams;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import mobi.esys.constants.UNLConsts;
import mobi.esys.data.GDFile;
import mobi.esys.fileworks.DirectoryWorks;
import mobi.esys.fileworks.FileWorks;
import mobi.esys.net.NetWork;
import mobi.esys.upnews_server.UNLServer;
import mobi.esys.upnewslite.R;
import mobi.esys.upnewslite.UNLApp;

public class DownloadVideoTask extends AsyncTask<Void, Void, Void> {
    private transient Context context;
    private transient SharedPreferences prefs;
    private transient boolean isDelete;
    private transient List<GDFile> gdFiles;
    private transient static FileOutputStream output;
    private transient Set<String> serverMD5;
    private transient int downCount;
    private transient List<GDFile> listWithoutDuplicates;
    private transient List<String> folderMD5;
    private transient Drive drive;
    private transient UNLApp mApp;
    private transient UNLServer server;

    public DownloadVideoTask(UNLApp app) {
        downCount = 0;
        mApp = app;
        context = app.getApplicationContext();
        prefs = app.getApplicationContext().getSharedPreferences(UNLConsts.APP_PREF, Context.MODE_PRIVATE);
        drive = app.getDriveService();
        server = new UNLServer(app);

    }

    @Override
    protected Void doInBackground(Void... params) {

        if (NetWork.isNetworkAvailable(mApp)) {
            if (!isDelete) {
                Log.d("down", "isDownload");

                serverMD5 = server.getMD5FromServer();
                gdFiles = server.getGdFiles();
                DirectoryWorks directoryWorks = new DirectoryWorks(
                        UNLConsts.VIDEO_DIR_NAME);
                folderMD5 = directoryWorks.getMD5Sums();
                isDelete = prefs.getBoolean("isDeleting", false);

                Set<String> urlSet = new HashSet<>();
                urlSet.add("");
                Set<String> urlSetRec = new HashSet<>(Arrays.asList(context
                        .getSharedPreferences(UNLConsts.APP_PREF,
                                Context.MODE_PRIVATE).getString("urls", "")
                        .replace("[", "").replace("]", "").split(",")));
                SharedPreferences.Editor editor = context.getSharedPreferences(
                        UNLConsts.APP_PREF, Context.MODE_PRIVATE).edit();
                editor.putBoolean("isDownload", true);
                editor.commit();

                LinkedHashSet<GDFile> listToSet = new LinkedHashSet<GDFile>(gdFiles);

                listWithoutDuplicates = new ArrayList<>(listToSet);

                Log.d("drive files", String.valueOf(listWithoutDuplicates.size()));
                Log.d("md5", String.valueOf(serverMD5.size()));
                String[] urls = urlSetRec.toArray(new String[urlSetRec.size()]);
                for (int i = 0; i < urls.length; i++) {
                    if (urls[i].startsWith(" ")) {
                        urls[i] = urls[i].substring(0, urls[i].length() - 1);
                    }
                }


                Collections.sort(listWithoutDuplicates, new Comparator<GDFile>() {
                    @Override
                    public int compare(GDFile lhs, GDFile rhs) {
                        return lhs.getGdFileName().compareTo(rhs.getGdFileName());
                    }
                });

                Log.d("files", listWithoutDuplicates.toString());


                while (downCount < listWithoutDuplicates.size()) {
                    try {
                        Log.d("count", String.valueOf(downCount));
                        downloadFile(drive, listWithoutDuplicates.get(downCount)
                                .getGdFileInst());
                    } catch (Exception e) {
                        Log.d("exc", e.getLocalizedMessage());
                        downCount++;
                    }


                }

            } else {
                Log.d("md5", "all MD5");
                downCount++;
                if (downCount == listWithoutDuplicates.size() - 1) {
                    cancel(true);
                }
            }
        } else {
            cancel(true);
        }
        return null;
    }

    private void downloadFile(Drive service, File file) {

        if (file.getFileSize() < Environment.getExternalStorageDirectory().getUsableSpace()) {
            DirectoryWorks directoryWorks = new DirectoryWorks(
                    UNLConsts.VIDEO_DIR_NAME);
            folderMD5 = directoryWorks.getMD5Sums();
            Log.d("down", "start down file");
            if (folderMD5.containsAll(serverMD5)
                    && folderMD5.size() == serverMD5.size()) {
                cancel(true);
                downCount++;
            } else {
                if (!folderMD5.contains(file.getMd5Checksum())) {
                    if (file.getDownloadUrl() != null
                            && file.getDownloadUrl().length() > 0) {
                        try {
                            HttpResponse resp = service
                                    .getRequestFactory()
                                    .buildGetRequest(
                                            new GenericUrl(file.getDownloadUrl()))
                                    .execute();
                            String root_sd = Environment
                                    .getExternalStorageDirectory()
                                    .getAbsolutePath()
                                    + UNLConsts.VIDEO_DIR_NAME;
                            String path = file.getTitle().substring(0, file.getTitle().indexOf(".")).concat(".").concat(UNLConsts.TEMP_FILE_EXT);
                            Log.d("down", path);
                            java.io.File downFile = new java.io.File(root_sd, path);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("currDownFile", downFile.getAbsolutePath());
                            editor.commit();
                            FileWorks fileWorks = new FileWorks(downFile.getAbsolutePath());
                            Log.d("down", downFile.getAbsolutePath());
                            if (!downFile.exists()) {
                                output = new FileOutputStream(downFile);
                                int bufferSize = 1024;
                                byte[] buffer = new byte[bufferSize];
                                int len = 0;
                                while ((len = resp.getContent().read(buffer)) != -1) {
                                    output.write(buffer, 0, len);
                                }

                                output.flush();
                                output.close();


                                if (serverMD5.contains(fileWorks.getFileMD5())) {
                                    fileWorks.renameFileExtension(file.getFileExtension());
                                    downCount++;

                                    Log.d("count down complete",
                                            String.valueOf(downCount));
                                    return;


                                } else {
                                    downCount++;
                                    return;
                                }


                            } else if (downFile.exists() && UNLConsts.TEMP_FILE_EXT.equals(fileWorks.getFileExtension()) && !serverMD5.contains(fileWorks.getFileMD5()) && downFile.length() < file.getFileSize()) {
                                Log.d("down_tag", fileWorks.getFileExtension());

                                output = new FileOutputStream(downFile, true);
                                int bufferSize = 1024;
                                byte[] buffer = new byte[bufferSize];
                                int len = 0;
                                InputStream inputStream = resp.getContent();

                                long skipped = inputStream.skip(file.getFileSize() - downFile.length());
                                Log.d("down_tag", String.valueOf(file.getFileSize() - downFile.length()) + ":" + String.valueOf(skipped));
                                if (skipped < file.getFileSize() - downFile.length()) {
                                    append(downFile, ByteStreams.toByteArray(inputStream));
                                } else {
                                    downFile.delete();
                                }

                                if (serverMD5.contains(fileWorks.getFileMD5())) {
                                    fileWorks.renameFileExtension(file.getFileExtension());
                                    downCount++;

                                    Log.d("count down complete",
                                            String.valueOf(downCount));
                                    return;


                                } else {
                                    downCount++;
                                    return;
                                }


                            }

                        } catch (IOException e) {
                            downCount++;
                            Log.d("count exc", String.valueOf(downCount));
                            Log.d("exc", e.getLocalizedMessage());
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.d("already down", String.valueOf(downCount));
                    downCount++;
                    return;
                }
            }
        } else {
            cancel(true);
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, context.getResources().getString(R.string.empty_space), Toast.LENGTH_LONG);
                }
            });
        }
    }


    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        stopDownload();
        if (!isDelete) {
            DeleteBrokeFilesTask brokeFilesTask = new DeleteBrokeFilesTask(mApp);
            brokeFilesTask.execute();
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        stopDownload();
    }

    private void stopDownload() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isDownload", false);
        editor.apply();
    }

    public static void append(java.io.File file, byte[] bytes) throws Exception {
        long fileLength = file.length();
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(fileLength);
        raf.write(bytes);
        raf.close();
    }


}
