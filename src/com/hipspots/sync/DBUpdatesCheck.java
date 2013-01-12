package com.hipspots.sync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.hipspots.app.HipspotsApplication;
import com.hipspots.database.VideoLocationDAO;
import com.hipspots.model.VideoLocationItem;
import com.hipspots.model.VideoLocationJSON;
import com.hipspots.model.VideoLocationList;
import com.hipspots.util.SettingsProvider;

public class DBUpdatesCheck {

	private Context ctx;
	// Holds new Location temporarily, will be released after storing data to DB
	public static VideoLocationJSON[] videoLocations = null;
	ProgressDialog progressDialog;
	String TAG = "UPD_CHK";

	DBSync dBSyncAlt;
	DBUpdatesCheck dBUpdateCheck;
	DBSync.DownloadTask downloadTask;

	private static final String URI = DBSync.URI;
	private static final String USER = DBSync.USER;
	private static final String PASSWORD = DBSync.PASSWORD;

	public DBUpdatesCheck(Context context) {
		this.ctx = context;
	}

	public class DBUpdatesCheckTask extends AsyncTask<Context, Integer, String> {

		protected String doInBackground(Context... params) {
			try {
				// Log.d(TAG, "Background...");
				doSync();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return "Complete";
		}

		protected void onPreExecute() {
			super.onPreExecute();
		}

		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		protected void onCancelled() {
			super.onCancelled();
		}

		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (HipspotsApplication.getNeedUpdate()) {
				// Log.d(TAG, "Starting Update...");
				dBSyncAlt = new DBSync(ctx);
				downloadTask = dBSyncAlt.new DownloadTask();
				downloadTask.execute(ctx);
			} else {
				VideoLocationDAO videoLocationDAO = new VideoLocationDAO();
				// UPDATE CONTENT
				boolean isContentUpdated = !videoLocationDAO.isContentNotUpdated(HipspotsApplication.getInstance().getJsonDBInstance()
						.getWritableDatabase(), SettingsProvider.getInstance().getAppLanguage());
				boolean downloadStarted = SettingsProvider.getInstance().getDownloadStarted();
				boolean downloading = SettingsProvider.getInstance().isDownloading();
				
				if (!isContentUpdated && !downloadStarted)
					HipspotsApplication.getFragmentInstance().startDownloadAlert();
				else if(!isContentUpdated && downloadStarted && !downloading)
					HipspotsApplication.getFragmentInstance().continueDownloadAlert();
			}
		}
	}

	void doSync() throws IOException {
		VideoLocationList locations = null;
		videoLocations = null;

		DefaultHttpClient httpClient = new DefaultHttpClient();
		String lastDate = HipspotsApplication.getFragmentInstance().getLastOpenDate();

		HttpGet request = new HttpGet(URI + "?smfDataLastUpdated=" + lastDate);

		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(USER, PASSWORD);
		request.addHeader(BasicScheme.authenticate(creds, "UTF-8", false));

		try {
			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				InputStream instream = response.getEntity().getContent();
				BufferedReader r = new BufferedReader(new InputStreamReader(instream, "UTF-8"), 8000);
				StringBuilder total = new StringBuilder();
				String line;
				while ((line = r.readLine()) != null) {
					total.append(line);
				}
				instream.close();

				// Parse to GSON here
				String JSON_Result = total.toString();

				JSON_Result = "{ locations:" + JSON_Result + "}";

				// Log.d(TAG, "JSON_Result:" + JSON_Result);
				Gson gson = new Gson();
				locations = (VideoLocationList) gson.fromJson(JSON_Result, VideoLocationList.class);

				VideoLocationItem[] vidLocItems = locations.getVideoLocations();
				int numLocations = vidLocItems.length;
				// Log.d(TAG, "Number of updates: " + numLocations);
				if (numLocations > 0)
					HipspotsApplication.setNeedUpdate(true);
				else
					HipspotsApplication.setNeedUpdate(false);

			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}