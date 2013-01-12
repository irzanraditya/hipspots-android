package com.hipspots.database;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.hipspots.util.Util;

public class DBHelper extends SQLiteOpenHelper {

	static final String DATABASE_NAME = "wwh.db";
//	static final String DATABASE_NAME = "/mnt/sdcard/Android/data/com.wwh/db";
	// static final String DATABASE_NAME = "/mnt/sdcard/wwh/db";
	static int db_version = 1;
	// -LOCATION-//
	static final String TABLE_LOCATION = "location";
	static final String COL_ID = "_id";
	static final String COL_JSON_ID = "json_id";
	static final String COL_UPDATED = "updated_ts";
	static final String COL_DELETED = "deleted_ts";
	static final String COL_CATEGORY = "category";
	static final String COL_EMAIL = "email";
	static final String COL_NAME_DE = "name_de";
	static final String COL_NAME_EN = "name_en";
	static final String COL_LONGITUDE = "longitude";
	static final String COL_LATITUDE = "latitude";
	static final String COL_STREET = "street";
	static final String COL_STREET_NUMBER = "street_number";
	static final String COL_ZIPCODE = "zip_code";
	static final String COL_PHONE = "phone";
	static final String COL_TEXT_DE = "text_de";
	static final String COL_TEXT_EN = "text_en";
	static final String COL_WEBSITE = "website";
	static final String COL_PHOTO_URL = "photo_url";
	static final String COL_PHOTO_DETAIL_URL = "photo_detail_url";
	static final String COL_THUMB_URL = "thumbnail_url";
	static final String COL_AUDIO_URL_DE = "audio_url_de";
	static final String COL_AUDIO_URL_EN = "audio_url_en";
	static final String COL_VIDEO_URL_DE = "video_url_de";
	static final String COL_VIDEO_URL_EN = "video_url_en";
	static final String COL_PHOTO_DETAIL_PATH = "detail_photo_path";
	static final String COL_THUMB_PATH = "thumbnail_path";
	static final String COL_VIDEO_PATH_DE = "video_path_de";
	static final String COL_VIDEO_PATH_EN = "video_path_en";
	static final String COL_AUDIO_PATH_DE = "audio_path_de";
	static final String COL_AUDIO_PATH_EN = "audio_path_en";
	static final String COL_FAVORITED = "is_favorited";// 0=False 1=True
	static final String COL_MONDAY = "monday";
	static final String COL_TUESDAY = "tuesday";
	static final String COL_WEDNESDAY = "wednesday";
	static final String COL_THURSDAY = "thursday";
	static final String COL_FRIDAY = "friday";
	static final String COL_SATURDAY = "saturday";
	static final String COL_SUNDAY = "sunday";
	static final String COL_DISTANCE = "distance";
	static final String COL_UPDATE_TIME = "update_time";
	static final String COL_CONTENT_UPDATED_IMG = "content_updated_image";// 0=False 1=True
	static final String COL_CONTENT_UPDATED_GER = "content_updated_german";// 0=False 1=True
	static final String COL_CONTENT_UPDATED_ENG = "content_updated_english";// 0=False 1=True
	static final String COL_JSON = "json";
	static final String IND_LOCATION = "ind_location";

	// -TRANSPORTATION-//
	static final String TABLE_TRANSPORTATION = "attr_transportation";
	static final String COL_TRANSPORTATION_ID = "_id";
	static final String COL_TRANSPORTATION_LOC_ID = "location_id";// ForeingKey:COL_JSON_ID
	static final String COL_TRANSPORTATION_TYPE = "type";
	static final String COL_TRANSPORTATION_LINE = "line";
	static final String COL_TRANSPORTATION_STATION = "station";
	static final String IND_TRANSPORTATION = "ind_transportation";

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, db_version++);
		File dbFile = context.getDatabasePath(DATABASE_NAME);
		Log.d("DB",dbFile.getAbsolutePath());
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.execSQL(CreateLocationStatment());
			db.execSQL(CreateLocationIndex());
			db.execSQL(CreateTransportationStatment());
			db.execSQL(CreateTransportationIndex());
		} catch (Exception e) {
			Log.e("Exception", e.toString());
		}
	}

	public String CreateLocationStatment() {
		return "CREATE TABLE " + TABLE_LOCATION + "(" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_JSON_ID + "," + COL_UPDATED + ","
				+ COL_DELETED + "," + COL_CATEGORY + "," + COL_EMAIL + "," + COL_NAME_DE + "," + COL_NAME_EN + "," + COL_LONGITUDE + ","
				+ COL_LATITUDE + "," + COL_STREET + "," + COL_STREET_NUMBER + "," + COL_ZIPCODE + "," + COL_PHONE + "," + COL_TEXT_DE + ","
				+ COL_TEXT_EN + "," + COL_PHOTO_URL + "," + COL_PHOTO_DETAIL_URL + "," + COL_THUMB_URL + "," + COL_AUDIO_URL_DE + ","
				+ COL_AUDIO_URL_EN + "," + COL_VIDEO_URL_DE + "," + COL_VIDEO_URL_EN + "," + COL_WEBSITE + "," + COL_PHOTO_DETAIL_PATH + ","
				+ COL_THUMB_PATH + "," + COL_VIDEO_PATH_DE + "," + COL_VIDEO_PATH_EN + "," + COL_AUDIO_PATH_DE + "," + COL_AUDIO_PATH_EN + ","
				+ COL_FAVORITED + "," + COL_MONDAY + "," + COL_TUESDAY + "," + COL_WEDNESDAY + "," + COL_THURSDAY + "," + COL_FRIDAY + ","
				+ COL_SATURDAY + "," + COL_SUNDAY + "," + COL_DISTANCE + "," + COL_UPDATE_TIME + "," + COL_CONTENT_UPDATED_IMG + ","
				+ COL_CONTENT_UPDATED_GER + "," + COL_CONTENT_UPDATED_ENG + "," + COL_JSON + ")";
	}

	public String CreateLocationIndex() {
		return "CREATE INDEX " + IND_LOCATION + " ON " + TABLE_LOCATION + " ( " + COL_ID + " )";
	}

	public String CreateTransportationStatment() {
		return "CREATE TABLE " + TABLE_TRANSPORTATION + "(" + COL_TRANSPORTATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COL_TRANSPORTATION_LOC_ID + "," + COL_TRANSPORTATION_TYPE + "," + COL_TRANSPORTATION_LINE + "," + COL_TRANSPORTATION_STATION + ")";
	}

	public String CreateTransportationIndex() {
		return "CREATE INDEX " + IND_TRANSPORTATION + " ON " + TABLE_LOCATION + " ( " + COL_TRANSPORTATION_LOC_ID + " )";
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (Util.isJsonDBDownloaded()) { // will be null if update fails
			db.execSQL(DropStatmentLocations());
			onCreate(db);
			db.execSQL(DropStatmentFootage());
			onCreate(db);
		} else {
			// here would be a possibility to inform the user about the
			// update-error
		}
	}

	public String DropStatmentLocations() {
		return "DROP TABLE IF EXISTS " + TABLE_LOCATION;
	}

	public String DropStatmentFootage() {
		return "DROP TABLE IF EXISTS " + TABLE_TRANSPORTATION;
	}

	public void DeleteAll(SQLiteDatabase db) {
		db.delete(DATABASE_NAME, null, null);
	}

	public SQLiteOpenHelper Recreate() {
		onUpgrade(getWritableDatabase(), 1, 2);
		return this;
	}

}