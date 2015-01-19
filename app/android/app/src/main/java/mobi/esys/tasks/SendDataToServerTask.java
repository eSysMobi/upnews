package mobi.esys.tasks;

import mobi.esys.upnews_server.K2Server;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

public class SendDataToServerTask extends AsyncTask<Bundle, Void, Void> {
	private transient K2Server k2Server;

	public SendDataToServerTask(Context context) {
		k2Server = new K2Server(context);
	}

	@Override
	protected Void doInBackground(Bundle... params) {
		k2Server.sendDataToServer(params[0]);
		return null;
	}

}
