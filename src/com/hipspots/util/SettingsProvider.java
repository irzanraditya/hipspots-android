package com.hipspots.util;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsProvider {
	private SharedPreferences preferences;
	private static SettingsProvider settingsProvider;

	public enum Language {
		GERMAN, ENGLISH;
	}

	private SettingsProvider(Context context) {
		preferences = context.getSharedPreferences("WWH", Context.MODE_PRIVATE);
	}

	public static SettingsProvider initSettings(Context context) {
		if(settingsProvider == null)
			settingsProvider = new SettingsProvider(context);
		return settingsProvider;
	}

	public static SettingsProvider getInstance() {
		return settingsProvider;
	}

	public Language getAppLanguage() {
		String languageString = preferences.getString("language", null);
		Language language;
		if (languageString == null){
			String deviceLanguage = Locale.getDefault().getDisplayLanguage();
			if(deviceLanguage.equals("Deutsch"))
				language = Language.GERMAN;
			else 
				language = Language.ENGLISH;
		} else if(languageString.equals("GERMAN"))
			language = Language.GERMAN;
		else
			language = Language.ENGLISH;
		
		return language;
	}

	public void setAppLanguageGerman(boolean german) {
		SharedPreferences.Editor editor = preferences.edit();
		if (german)
			editor.putString("language", "GERMAN");
		else
			editor.putString("language", "ENGLISH");
		editor.commit();
	}

	public void setDownloadStarted(boolean started) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("downloadStarted", started);
		editor.commit();
	}

	public boolean getDownloadStarted() {
		return preferences.getBoolean("downloadStarted", false);
	}

	public void setDownloadTimestamp(long timestamp) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong("downloadTimestamp", timestamp);
		editor.commit();
	}

	public long getDownloadTimestamp() {
		return preferences.getLong("downloadTimestamp", 0);
	}

	public boolean isDownloading() {
		return getDownloadTimestamp() + (1000) > System.currentTimeMillis() ? true : false;
	}

	public void setCurrentDownloadJsonId(int id) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("currentDownloadId", id);
		editor.commit();
	}

	public int getCurrentDownloadJsonId() {
		return preferences.getInt("currentDownloadId", 0);
	}

	public void setDownloadPhotoJsonId(long downloadId, int jsonId) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(downloadId + "", jsonId + "-p");
		editor.commit();
	}

	public void setDownloadAudioJsonId(long downloadId, int jsonId) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(downloadId + "", jsonId + "-a");
		editor.commit();
	}

	public void setDownloadVideoJsonId(long downloadId, int jsonId) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(downloadId + "", jsonId + "-v");
		editor.commit();
	}

	public String getDownloadJsonId(long downloadId) {
		return preferences.getString(downloadId + "", "");
	}

	public void setDownloadIdFinished(long downloadId) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(downloadId + "finished", true);
		editor.commit();
	}

	public boolean getDownloadIdFinished(long downloadId) {
		return preferences.getBoolean(downloadId + "finished", false);
	}

}
