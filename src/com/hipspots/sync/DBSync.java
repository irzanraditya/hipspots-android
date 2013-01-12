package com.hipspots.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;

import com.google.gson.Gson;
import com.hipspots.app.HipspotsApplication;
import com.hipspots.database.DBHelper;
import com.hipspots.database.VideoLocationDAO;
import com.hipspots.model.VideoLocationDB;
import com.hipspots.model.VideoLocationItem;
import com.hipspots.model.VideoLocationJSON;
import com.hipspots.model.VideoLocationList;
import com.hipspots.util.SettingsProvider;
import com.hipspots.util.Util;

public class DBSync {

	private Context context;
	// Holds new Location temporarily, well be released after storing data to DB
	public static VideoLocationJSON[] videoLocations = null;
	ProgressDialog progressDialog;

	public static final String URI = "http://whatwashere.solutions.smfhq.com/api/locations.json";
	// public static final String URI = "http://development.whatwashere.solutions.smfhq.com/api/locations.json";
	public static final String USER = "wwhapi";
	public static final String PASSWORD = "LetsTellTheWorldWhatHasBeenHere";

	private static final String TAG = "DBSync";
	private String title = "";
	private String subTitle = "";

	public DBSync(Context context) {
		this.context = context;
		progressDialog = new ProgressDialog(context);
		String deviceLanguage = Locale.getDefault().getDisplayLanguage();
		// GERMAN
		if (deviceLanguage.equals("Deutsch")) {
			title = "Bitte warten...";
			if (!HipspotsApplication.getNeedUpdate())
				subTitle = "Daten werden geladen ...";
			else
				subTitle = "Daten werden aktualisiert ...";
			// ENGLISH
		} else {
			title = "Please wait...";
			if (!HipspotsApplication.getNeedUpdate())
				subTitle = "Retrieving data ...";
			else
				subTitle = "Updating data ...";
		}

		progressDialog = ProgressDialog.show(context, title, subTitle, true);
	}

	public class DownloadTask extends AsyncTask<Context, Integer, String> {

		protected String doInBackground(Context... params) {

			try {
				// Log.d("DW", "Background...");
				doSync();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return "Complete";
		}

		protected void onPreExecute() {
			// Log.d("DW", "Prepare Download");
			super.onPreExecute();
		}

		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			// Log.d("DW", "Downloading" + String.valueOf(values[0]));
		}

		protected void onCancelled() {
			super.onCancelled();
			// Log.d("DW", "Cancelled");
		}

		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			// Log.d("DW", "Json Download: " + result);
			progressDialog.dismiss();
			HipspotsApplication.getMapActivityInstance().initLocations();
			HipspotsApplication.getListActivityInstance().setAdapter();
			HipspotsApplication.getListActivityInstance().loadListView();
			VideoLocationDAO videoLocationDAO = new VideoLocationDAO();
			HipspotsApplication.getFragmentInstance().storeCurrentDate();

			// UPDATE CONTENT
			Boolean isContentNotUpdated = videoLocationDAO.isContentNotUpdated(
					HipspotsApplication.getInstance().getJsonDBInstance().getWritableDatabase(), SettingsProvider.getInstance().getAppLanguage());
			if (isContentNotUpdated)
				HipspotsApplication.getFragmentInstance().startDownloadAlert();

			HipspotsApplication.getMapActivityInstance().initializeViews();

		}
	}

	void doSync() throws IOException {
		VideoLocationList locations = null;
		videoLocations = null;

		DefaultHttpClient httpClient = new DefaultHttpClient();

		String lastDate = HipspotsApplication.getFragmentInstance().getLastOpenDate();
		HttpGet request = null;
		if (lastDate == null)
			request = new HttpGet(URI);
		else {
			request = new HttpGet(URI + "?smfDataLastUpdated=" + lastDate);
		}
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

				// Log.d("DBSync", "JSON_Result:" + JSON_Result);
				Gson gson = new Gson();
				locations = (VideoLocationList) gson.fromJson(JSON_Result, VideoLocationList.class);

				VideoLocationItem[] vidLocItems = locations.getVideoLocations();
				int numLocations = vidLocItems.length;

				videoLocations = new VideoLocationJSON[numLocations];
				for (int i = 0; i < vidLocItems.length; i++) {
					videoLocations[i] = vidLocItems[i].location;
				}

			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			// Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			// Log.e(TAG, e.getMessage());
		}

		// After sucessfully retrieving reomte json we open and update DB
		if (videoLocations != null && !HipspotsApplication.isDBLoaded() && !HipspotsApplication.getNeedUpdate()) {

			DBHelper dbhelper = HipspotsApplication.getInstance().getJsonDBInstance();
			SQLiteDatabase db = dbhelper.getWritableDatabase();

			db.beginTransaction();
			VideoLocationDAO videoLocationsDAO = new VideoLocationDAO();
			videoLocationsDAO.InsertLocationArray(db, videoLocations);
			db.setTransactionSuccessful();
			db.endTransaction();

			VideoLocationDB[] videoLocationsDB = videoLocationsDAO.getVideoLocations(db);

			for (int i = 0; i < videoLocationsDB.length; i++) {
				int id = videoLocationsDB[i].id;
				String name = videoLocationsDB[i].name_de;
				String thumbnail_url = videoLocationsDB[i].thumbnail_url;
				if (thumbnail_url != null)
					downloadThumbnail(thumbnail_url, id, name, db);
			}

		} else if (videoLocations != null && HipspotsApplication.getNeedUpdate()) {
			// Update DataBase with Updates
			DBHelper dbhelper = HipspotsApplication.getInstance().getJsonDBInstance();
			SQLiteDatabase db = dbhelper.getWritableDatabase();

			VideoLocationDAO videoLocationsDAO = new VideoLocationDAO();
			videoLocationsDAO.UpdateLocationArray(db, videoLocations);
			HipspotsApplication.setNeedUpdate(false);
			for (int i = 0; i < videoLocations.length; i++) {
				VideoLocationDB videoLocationDB = videoLocationsDAO.getVideoLocation(db, videoLocations[i].name_de);
				downloadThumbnail(videoLocationDB.thumbnail_url, videoLocations[i].id, videoLocations[i].name_de, db);
			}
		}
		HipspotsApplication.getFragmentInstance().storeCurrentDate();

	}

	private void downloadThumbnail(String url, int id, String name, SQLiteDatabase db) throws IOException {
		File cacheDir = Util.createCacheDir("thumbnail", context);
		Util.downloadFromURL(url, cacheDir, id + ".jpg");
		VideoLocationDAO videoLocationDAO = new VideoLocationDAO();
		if (id == 0)
			id = 1;
		VideoLocationDB videoLocationDB = videoLocationDAO.getVideoLocation(db, name);
		videoLocationDB.thumbnail_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.wwh/thumbnail/" + id + ".jpg";
		videoLocationDAO.updateThumbnailPath(db, videoLocationDB);
	}

}