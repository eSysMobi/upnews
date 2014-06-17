package mobi.esys.tasks;

import mobi.esys.constants.K2Constants;
import mobi.esys.fileworks.AndroidAssets;
import mobi.esys.fileworks.DirectiryWorks;
import android.content.Context;
import android.os.AsyncTask;

public class GetFirstFileTask extends AsyncTask<String, Void, Void> {
	private transient Context context;

	public GetFirstFileTask(Context context) {
		this.context = context;
	}

	@Override
	protected Void doInBackground(String... params) {
		DirectiryWorks directiryWorks = new DirectiryWorks(context, params[0]);
		directiryWorks.createDir();

		DirectiryWorks directiryWorks2 = new DirectiryWorks(context,
				K2Constants.PHOTO_DIR_NAME);
		directiryWorks2.createDir();
		AndroidAssets androidAssets = new AndroidAssets(context, params[0]);
		androidAssets.saveFileFromAssets();
		return null;
	}
}
