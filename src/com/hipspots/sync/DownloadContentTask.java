package com.hipspots.sync;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;

import com.hipspots.app.HipspotsApplication;
import com.hipspots.database.DBHelper;
import com.hipspots.database.VideoLocationDAO;
import com.hipspots.model.VideoLocationDB;
import com.hipspots.util.SettingsProvider;
import com.hipspots.util.Util;
import com.hipspots.util.SettingsProvider.Language;

public class DownloadContentTask extends AsyncTask<Void, Integer, String> {
	private String TAG = "DL";
	private SQLiteDatabase db;
	private File dirVideo, dirPhoto, dirAudio;
	private Context context;		
	private String externalStoragePath;
	private PowerManager.WakeLock wakeLock;
	private WifiLock wifiLock;
	private DownloadContentService service;
	private int videosAmount, imagesAmount;

	public DownloadContentTask(Context context, DownloadContentService service) {
		this.context = context;
		this.service = service;
		externalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		
		SettingsProvider.initSettings(context);
		DBHelper dbhelper = HipspotsApplication.getInstance().getJsonDBInstance();
		db = dbhelper.getWritableDatabase();

		imagesAmount = getImagesAmount();
		videosAmount = getVideosAmount(SettingsProvider.getInstance().getAppLanguage());	
		
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WHATWASHERE Downloader");

		// Acquire WiFi lock
		wifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "WHATWASHERE Downloader");
	}

	private int getImagesAmount(){
		VideoLocationDAO dao = new VideoLocationDAO();
		VideoLocationDB[] videoLocations = dao.getVideoLocations(db);
		return videoLocations.length;
	}
	
	private int getVideosAmount(Language language){
		VideoLocationDAO dao = new VideoLocationDAO();
		VideoLocationDB[] videoLocations = dao.getAllLocationsWithVideo(db, language);
		return videoLocations.length;
	}

	protected String doInBackground(Void... params) {
		try {
			download();
		} catch (IOException e) {
			e.printStackTrace();
			service.stopSelf();
		}
		
		if(wakeLock != null)
			wakeLock.release();
		if(wifiLock != null)
			wifiLock.release();
		
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
		Log.d(TAG, "Download cancelled");
	}

	protected void onPostExecute(String result) {
		super.onPostExecute(result);

		HipspotsApplication.getMapActivityInstance().initLocations();
		HipspotsApplication.getListActivityInstance().loadListView();
		
		service.stopSelf();
	}

	private void download() throws IOException {
		wakeLock.acquire();
		wifiLock.acquire();
		
		dirVideo = Util.createCacheDir("video", context);
		dirPhoto = Util.createCacheDir("photo", context);
		dirAudio = Util.createCacheDir("audio", context);

		VideoLocationDAO dao = new VideoLocationDAO();

		HipspotsApplication.setIsDownloading(true);

		// IMAGES
		VideoLocationDB[] imageVideoLocations;
		imageVideoLocations = dao.getNotUpdatedImageVideoLocations(db);
		// AUDIO & VIDEO
		VideoLocationDB[] videoLocations;
		videoLocations = dao.getNotUpdatedContentVideoLocations(db, SettingsProvider.getInstance().getAppLanguage());

		for (int i = 0; i < imageVideoLocations.length; i++) {
			downloadLocationImage(imageVideoLocations[i]);
		}
		for (int i = 0; i < videoLocations.length; i++) {
			if (isWifiConnected()) {
				if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH) {
					downloadLocationEnglish(videoLocations[i]);
				} else {
					downloadLocationGerman(videoLocations[i]);
				}
				System.gc();
			} else
				return;
		}

		HipspotsApplication.setIsDownloading(false);

		if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH) {
			SettingsProvider.getInstance().setAppLanguageGerman(false);
		} else {
			SettingsProvider.getInstance().setAppLanguageGerman(true);
		}
	}

	private void downloadLocationImage(VideoLocationDB videoLocation) throws IOException {
		int id = videoLocation.id;
		String photo_url = videoLocation.photo_detail_url;
		Log.d(TAG, "Downloading Image: " + videoLocation.name_de + "id: " + id);

		if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH) 
			service.updateNotificationStatus("Downloading image " + videoLocation._id + "/" + imagesAmount);
		else
			service.updateNotificationStatus("Foto " + videoLocation._id + "/" + imagesAmount + " wird heruntergeladen");
		Util.downloadFromURL(photo_url, dirPhoto, id + ".jpg");

		VideoLocationDAO videoLocationDAO = new VideoLocationDAO();
		videoLocation.photo_detail_path = externalStoragePath + "/Android/data/com.wwh/photo/" + id + ".jpg";
		Log.d(TAG, "Downloaded to: " + videoLocation.photo_detail_path);
		videoLocationDAO.updateImagePath(db, videoLocation);
	}

	private void downloadLocationGerman(VideoLocationDB videoLocation) throws IOException {
		int id = videoLocation.id;
		String video_url_de = videoLocation.video_url_de;
		String audio_url_de = videoLocation.audio_url_de;
		Log.d(TAG, "Downloading: " + id);

		service.updateNotificationStatus("Video " + videoLocation._id + "/"+videosAmount + " wird heruntergeladen");
		Util.downloadFromURL(video_url_de, dirVideo, id + "_de.mp4");
		service.updateNotificationStatus("Audio " + videoLocation._id + "/" + videosAmount + " wird heruntergeladen");
		Util.downloadFromURL(audio_url_de, dirAudio, id + "_de.mp3");

		VideoLocationDAO videoLocationDAO = new VideoLocationDAO();

		videoLocation.video_path_de = externalStoragePath + "/Android/data/com.wwh/video/" + id + "_de.mp4";
		Log.d(TAG, "Downloaded to: " + videoLocation.video_path_de);
		videoLocation.audio_path_de = externalStoragePath + "/Android/data/com.wwh/audio/" + id + "_de.mp3";
		Log.d(TAG, "Downloaded to: " + videoLocation.audio_path_de);
		videoLocationDAO.updatePaths(db, videoLocation, Language.GERMAN);
	}

	private void downloadLocationEnglish(VideoLocationDB videoLocation) throws IOException {
		int id = videoLocation.id;
		String video_url_en = videoLocation.video_url_en;
		String audio_url_en = videoLocation.audio_url_en;
		Log.d(TAG, "Downloading: " + id);

		service.updateNotificationStatus("Downloading video " + videoLocation._id + "/"+videosAmount);
		Util.downloadFromURL(video_url_en, dirVideo, id + "_en.mp4");
		service.updateNotificationStatus("Downloading audio " + videoLocation._id + "/" + videosAmount);
		Util.downloadFromURL(audio_url_en, dirAudio, id + "_en.mp3");

		VideoLocationDAO videoLocationDAO = new VideoLocationDAO();

		videoLocation.video_path_en = externalStoragePath + "/Android/data/com.wwh/video/" + id + "_en.mp4";
		Log.d(TAG, "Downloaded to: " + videoLocation.video_path_en);
		videoLocation.audio_path_en = externalStoragePath + "/Android/data/com.wwh/audio/" + id + "_en.mp3";
		Log.d(TAG, "Downloaded to: " + videoLocation.audio_path_en);
		videoLocationDAO.updatePaths(db, videoLocation, Language.ENGLISH);
	}

	public boolean isWifiConnected() {
		ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi.isConnected()) {
			return true;
		} else
			return false;
	}
}