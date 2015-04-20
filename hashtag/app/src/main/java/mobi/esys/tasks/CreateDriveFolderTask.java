package mobi.esys.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mobi.esys.consts.ISConsts;
import mobi.esys.filesystem.directories.DirectoryHelper;
import mobi.esys.network.monitoring.NetMonitor;
import mobi.esys.system.StreamsUtils;
import mobi.esys.upnewshashtag.DriveLoginActivity;
import mobi.esys.upnewshashtag.R;
import mobi.esys.upnewshashtag.SliderActivity;
import mobi.esys.upnewshashtag.UNHApp;

/**
 * Created by Артем on 14.04.2015.
 */
public class CreateDriveFolderTask extends AsyncTask<Void, Void, Void> {
    private transient SharedPreferences prefs;
    private transient Context mContext;
    private transient boolean isAuthSuccess;
    private transient boolean mStartVideoOnSuccess;
    private static final String RSS_TITLE = ISConsts.GD_RSS_FILE_NAME;
    private static final String RSS_MIME_TYPE = ISConsts.GD_RSS_FILE_MIME_TYPE;
    private static final String TAG = "CreateDriveFolderTask";
    private transient Drive drive;
    private transient UNHApp mApp;
    private transient ProgressDialog pd;
    private transient boolean mIsShowPD;


    public CreateDriveFolderTask(Context context, boolean isStartVideoOnSuccess, UNHApp app, boolean isShowPD) {
        mApp = app;
        prefs = app.getApplicationContext().getSharedPreferences(ISConsts.PREF_PREFIX, Context.MODE_PRIVATE);
        isAuthSuccess = true;
        mStartVideoOnSuccess = isStartVideoOnSuccess;
        drive = app.getDriveService();
        mIsShowPD = isShowPD;
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mIsShowPD) {
            pd = new ProgressDialog(mContext);
            pd.setMessage("Обработка папки на Google Drive");
            if (!pd.isShowing()) {
                pd.show();
            }
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (NetMonitor.isNetworkAvailable(mApp)) {
            Log.d(TAG, "create folder");
            try {
                List<String> fileName = new ArrayList<>();

                Drive.Files.List request = drive
                        .files()
                        .list()
                        .setQ(ISConsts.GD_FOLDER_QUERY);
                FileList files = request.execute();

                for (File file : files.getItems()) {
                    fileName.add(file.getTitle());
                    Log.d(TAG, file.getTitle() + ":" + file.getId());
                }

                Log.d(TAG, fileName.toString());
                SharedPreferences.Editor editor = prefs.edit();
                if (!fileName.contains(ISConsts.GD_DIR_NAME)) {
                    File body = new File();
                    body.setTitle(ISConsts.GD_DIR_NAME);
                    body.setMimeType("application/vnd.google-apps.folder");
                    File unlFolder = drive.files().insert(body).execute();
                    Log.d(TAG, unlFolder.getId());
                    editor.putString("folderId", unlFolder.getId());


                    File file = new File();
                    file.setTitle(RSS_TITLE);
                    file.setDescription("file to configure rss reader");
                    file.setMimeType(RSS_MIME_TYPE);
                    file.setParents(Arrays.asList(new ParentReference().setId(unlFolder.getId())));

                    java.io.File tmpFile = new java.io.File(Environment.getExternalStorageDirectory() + ISConsts.DIR_NAME, "rss.txt");
                    Log.d(TAG, tmpFile.getAbsolutePath());
                    InputStream tmpInStream = mContext.getResources().openRawResource(R.raw.rss);
                    StreamsUtils.copyInputStreamToFile(tmpInStream, tmpFile);
                    FileContent fileContent = new FileContent(RSS_MIME_TYPE, tmpFile);
                    file = drive.files().insert(file, fileContent).execute();

                    Log.d("CreateDriveFolderTask", file.getId());

                    if (tmpFile.exists()) {
                        tmpFile.delete();
                    }

                } else {
                    String folderID = files.getItems()
                            .get(fileName
                                    .indexOf(ISConsts.GD_DIR_NAME))
                            .getId();
                    List<String> fileNames = new ArrayList<String>();
                    Log.d(TAG,
                            folderID);
                    editor.putString(
                            "folderId",
                            files.getItems()
                                    .get(fileName
                                            .indexOf(ISConsts.GD_DIR_NAME))
                                    .getId());

                    com.google.api.services.drive.Drive.Children.List fileList = drive.children().list(folderID);
                    ChildList children = fileList.execute();
                    for (ChildReference child : children.getItems()) {
                        File file = drive.files().get(child.getId()).execute();
                        fileNames.add(file.getTitle());
                    }

                    Log.d(TAG, fileNames.toString());

                    if (!fileNames.contains(RSS_TITLE)) {
                        File file = new File();
                        file.setTitle(RSS_TITLE);
                        file.setDescription("file to configure rss reader");
                        file.setMimeType(RSS_MIME_TYPE);
                        file.setParents(Arrays.asList(new ParentReference().setId(folderID)));

                        java.io.File tmpFile = new java.io.File(Environment.getExternalStorageDirectory() + ISConsts.DIR_NAME, "rss.txt");
                        Log.d(TAG, tmpFile.getAbsolutePath());
                        InputStream tmpInStream = mContext.getResources().openRawResource(R.raw.rss);
                        StreamsUtils.copyInputStreamToFile(tmpInStream, tmpFile);
                        FileContent fileContent = new FileContent(RSS_MIME_TYPE, tmpFile);
                        file = drive.files().insert(file, fileContent).execute();
                        Log.d("CreateDriveFolderTask", file.getId());

                        if (tmpFile.exists()) {
                            tmpFile.delete();
                        }
                    }


                }

                editor.commit();
                isAuthSuccess = true;

            } catch (UserRecoverableAuthIOException e) {
                Log.d(TAG, "Error");
                if (mContext instanceof DriveLoginActivity) {
                    ((DriveLoginActivity) mContext).catchUSERException(e
                            .getIntent());
                }

            } catch (IOException e) {
                Log.d(TAG, "Error: " + e.getLocalizedMessage());
            }
        } else

        {
            isAuthSuccess = false;
        }

        return null;
    }


    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (mStartVideoOnSuccess) {
            if (isAuthSuccess) {
                DirectoryHelper directoryWorks = new DirectoryHelper(
                        ISConsts.DIR_NAME);
                if (directoryWorks.getDirFileList("if have files").length == 0) {
                    mContext.startActivity(new Intent(mContext,
                            SliderActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    ((DriveLoginActivity) mContext).finish();
                } else {
                    mContext.startActivity(new Intent(mContext,
                            SliderActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    ((DriveLoginActivity) mContext).finish();
                }
            } else {
                mContext.startActivity(new Intent(mContext,
                        DriveLoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }

        if (mIsShowPD) {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
        }

    }
}
