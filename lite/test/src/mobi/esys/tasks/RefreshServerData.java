package mobi.esys.tasks;

import java.util.Set;

import mobi.esys.upnews_server.K2Server;
import android.content.Context;
import android.os.AsyncTask;

public class RefreshServerData extends AsyncTask<Void, Void, Set<String>> {
	private transient K2Server k2server;

	public RefreshServerData(Context context) {
		this.k2server = new K2Server(context);
	}

	@Override
	protected Set<String> doInBackground(Void... params) {
		return this.k2server.getMD5FromServer();
	}

}
