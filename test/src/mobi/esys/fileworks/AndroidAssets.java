package mobi.esys.fileworks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import mobi.esys.constants.K2Constants;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

public class AndroidAssets {
	private transient final Context context;
	private transient final String folderPath;
	private static final String ASSETS_TAG = "AndroidAssets";
	private static final String FILE_PREFIX = K2Constants.FILE_PREFIX;

	public AndroidAssets(final Context context, final String folderPath) {
		this.context = context;
		this.folderPath = folderPath;
	}

	public void saveFileFromAssets() {
		final AssetManager assetManager = context.getAssets();
		InputStream inStream = null;
		OutputStream out = null;

		try {
			final String fileName = assetManager.list("")[0];

			SharedPreferences.Editor editor = context.getSharedPreferences(
					K2Constants.APP_PREF, Context.MODE_PRIVATE).edit();

			List<String> stringArray = new ArrayList<String>();
			stringArray.add(FILE_PREFIX + fileName);

			String list = stringArray.toString().replace("[", "")
					.replace("]", "");
			editor.putString("videoFilesNames", list);
			editor.commit();

			inStream = assetManager.open(fileName);
			final File assetFile = new File(Environment
					.getExternalStorageDirectory().getAbsolutePath()
					+ folderPath + FILE_PREFIX + fileName);
			if (!assetFile.exists()) {
				out = new FileOutputStream(Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ folderPath + FILE_PREFIX + fileName);
				Log.d(ASSETS_TAG, Environment.getExternalStorageDirectory()
						.getAbsolutePath()
						+ folderPath
						+ FILE_PREFIX
						+ fileName);

				copyFile(inStream, out);
				inStream.close();
				inStream = null;
				out.flush();
				out.close();
				out = null;
			}
		} catch (IOException e) {

		}

	}

	private void copyFile(final InputStream inputStream,
			final OutputStream outputStream) throws IOException {
		final byte[] buffer = new byte[1024];
		int read;
		while ((read = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, read);
		}
	}
}
