package com.hipspots.sync;

import android.database.sqlite.SQLiteDatabase;

import com.hipspots.app.HipspotsApplication;
import com.hipspots.database.DBHelper;
import com.hipspots.database.VideoLocationDAO;
import com.hipspots.model.VideoLocationDB;
import com.hipspots.util.SettingsProvider.Language;

public class LocationDownloadObject {
	private Integer jsonId;
	private Integer downloadElements;
	private Language language;

	public LocationDownloadObject(int _jsonId, Language _language) {
		setJsonId(_jsonId);
		setDownloadElements(3);
		language = _language;

	}

	public int updateDB() {

		int result = 0;
		DBHelper dbhelper = HipspotsApplication.getInstance().getJsonDBInstance();
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		VideoLocationDAO videoLocationDAO = new VideoLocationDAO();

		VideoLocationDB videoLocation = videoLocationDAO.getVideoLocationByJsonId(db, Integer.valueOf(jsonId));
		if (language == Language.GERMAN) {
			videoLocation.video_path_de = "/Android/data/com.wwh/photo/" + jsonId + "_de.jpg";
			videoLocation.video_path_de = "/Android/data/com.wwh/video/" + jsonId + "_de.mp4";
			videoLocation.audio_path_en = "/Android/data/com.wwh/audio/" + jsonId + "_de.mp3";
			result = videoLocationDAO.updatePaths(db, videoLocation, language);
		} else {
			videoLocation.video_path_de = "/Android/data/com.wwh/photo/" + jsonId + "_en.jpg";
			videoLocation.video_path_de = "/Android/data/com.wwh/video/" + jsonId + "_en.mp4";
			videoLocation.audio_path_en = "/Android/data/com.wwh/audio/" + jsonId + "_en.mp3";
			result = videoLocationDAO.updatePaths(db, videoLocation, language);
		}
		return result;
	}

	public int updateDownloadedCount() {
		int result = 0;
		downloadElements--;
		if (downloadElements == 0) {
			result = updateDB();
		}
		return result;
	}

	public int getJsonId() {
		return jsonId;
	}

	public void setJsonId(int jsonId) {
		this.jsonId = jsonId;
	}

	public int getDownloadElements() {
		return downloadElements;
	}

	public void setDownloadElements(int downloadElements) {
		this.downloadElements = downloadElements;
	}

}
