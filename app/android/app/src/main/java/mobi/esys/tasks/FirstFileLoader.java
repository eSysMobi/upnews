package mobi.esys.tasks;

import android.content.Context;
import android.content.Loader;
import android.os.Bundle;

public class FirstFileLoader extends Loader<String> {
	private transient String folderName;

	public FirstFileLoader(Context context, Bundle args) {
		super(context);
		if (args != null) {
			folderName = args.getString("dir");
		}
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		GetFirstFileTask getFirstFileTask = new GetFirstFileTask(getContext());
		getFirstFileTask.execute(folderName);
		deliverResult(folderName);
	}

	@Override
	public void deliverResult(String data) {
		super.deliverResult(data);
	}

}
