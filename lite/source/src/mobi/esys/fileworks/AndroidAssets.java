package mobi.esys.fileworks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

public class AndroidAssets {
	private transient Context context;
	private transient String folderPath;
	private static final String ASSETS_TAG = "AndroidAssets";

	public AndroidAssets(Context context, String folderPath) {
		this.context = context;
		this.folderPath = folderPath;
	}

	public void saveFileFromAssets() {
		AssetManager assetManager = context.getAssets();
		InputStream in = null;
		OutputStream out = null;

		try {
			String fileName = assetManager.list("")[0];
			in = assetManager.open(fileName);
			File assetFile = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + folderPath + "dd" + fileName);
			if (!assetFile.exists()) {
				out = new FileOutputStream(Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ folderPath + "dd" + fileName);
				Log.d(ASSETS_TAG, Environment.getExternalStorageDirectory()
						.getAbsolutePath() + folderPath + "dd" + fileName);

				copyFile(in, out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			}
		} catch (IOException e) {

		}

	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}
}
