package com.hipspots.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hipspots.app.HipspotsApplication;
import com.hipspots.database.VideoLocationDAO;

public class Util {
	static final int TIMEOUT_CONNECTION = 10000;
	static final int TIMEOUT_SOCKET = 50000;
	static final int BUFFER_SIZE = 8192;

	public static boolean isJsonDBDownloaded() {
		SQLiteDatabase db = HipspotsApplication.getInstance().getJsonDBInstance().getWritableDatabase();
		VideoLocationDAO videoLocationsDAO = new VideoLocationDAO();
		return videoLocationsDAO.areVideoLocations(db);
	}

	public static void downloadFromURL(String url, File cacheDir, String fileName) throws IOException {
		long startTime = System.currentTimeMillis();
		String TAG = "DL";
		if (url != null) {
			if (fileName.contains("/"))
				fileName = fileName.replace("/", "-");
			url = url.replace(" ", "%20");
			URL mUrl = new URL(url);
			// Log.d("DL", "Download beginning: " + url);
			// Log.d("DL", "Download to: " + fileName);

			// Open a connection to that URL.
			URLConnection ucon = mUrl.openConnection();

			// this timeout affects how long it takes for the app to realize there's a connection problem
			ucon.setReadTimeout(TIMEOUT_CONNECTION);
			ucon.setConnectTimeout(TIMEOUT_SOCKET);
//			int totalSize = ucon.getContentLength();

			// Define InputStreams to read from the URLConnection. Uses 3KB download buffer
			File f = new File(cacheDir, fileName);
			InputStream is = ucon.getInputStream();
			BufferedInputStream inStream = new BufferedInputStream(is, BUFFER_SIZE);
			FileOutputStream outStream = new FileOutputStream(f);
			byte[] buff = new byte[BUFFER_SIZE];

			// Read bytes (and store them) until there is nothing more to read(-1)
//			long amount = 0;
			int len;
			while ((len = inStream.read(buff)) != -1) {
//				amount += len;
//				Log.d(TAG, "(" + (amount * 100) / totalSize + "%) " + amount + "/" + totalSize);
				SettingsProvider.getInstance().setDownloadTimestamp(System.currentTimeMillis());
				outStream.write(buff, 0, len);
			}
			SettingsProvider.getInstance().setDownloadTimestamp(0);

			// clean up
			outStream.flush();
			outStream.close();
			inStream.close();

			Log.d(TAG, "Download completed in " + ((System.currentTimeMillis() - startTime) / 1000) + " sec");
		}
	}

	public static void downloadFromURLByHTTP(String url, File cacheDir, String fileName) throws IOException {
		if (url != null) {
			// Log.d(TAG, url);
			URL mUrl = new URL(url.replace(" ", "%20"));
			HttpURLConnection urlConnection = (HttpURLConnection) mUrl.openConnection();

			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.connect();

			File file = new File(cacheDir, fileName);
			FileOutputStream fileOutput = new FileOutputStream(file);
			InputStream inputStream = urlConnection.getInputStream();

			byte[] buffer = new byte[65536];
			int bufferLength = 0; // used to store a temporary size of the buffer

			while ((bufferLength = inputStream.read(buffer)) > 0) {
				fileOutput.write(buffer, 0, bufferLength);

			}
			fileOutput.close();
		}
	}

	public static File createCacheDir(String name, Context context) {
		File cacheDir;
		// name = Util.normalize(name);
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
			cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "/Android/data/com.wwh/" + name);
		else
			cacheDir = context.getCacheDir();
		if (!cacheDir.exists())
			cacheDir.mkdirs();
		return cacheDir;
	}

	public static int now() {
		Calendar c = Calendar.getInstance();
		Date today = c.getTime();
		return (int) (today.getTime() / 1000);
	}

	/*
	 * public static String normalize(String text) { return (text.replace("/", "-")).replace("|", "-"); }
	 */
}
