package com.hipspots.database;

import org.json.JSONException;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hipspots.model.OpeningHoursJSON;
import com.hipspots.model.VideoLocationDB;
import com.hipspots.model.VideoLocationJSON;
import com.hipspots.util.Util;
import com.hipspots.util.SettingsProvider.Language;

public class VideoLocationDAO {

	private String Query = null;

	private final static String TAG_HISTORIC_PLACES = "'HISTORIC_PLACES'";
	private final static String TAG_MUSEUM = "'MUSEUM'";
	private final static String TAG_EAT_AND_DRINK = "'EAT_AND_DRINK_AND_HOTEL'";

	private static final String TAG = null;

	private final String SELECT_ALL_LOCATIONS = "SELECT * FROM " + DBHelper.TABLE_LOCATION;
	private final String SELECT_ALL_UPDATED_LOCATIONS = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE " + DBHelper.COL_UPDATE_TIME + " <= ?";

	private final String SELECT_ALL_LOCATIONS_WITH_VIDEO_GER = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE "
			+ DBHelper.COL_VIDEO_URL_DE + " <> ''";
	
	private final String SELECT_ALL_LOCATIONS_WITH_VIDEO_ENG = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE "
			+ DBHelper.COL_VIDEO_URL_EN + " <> ''";
	
	private final String SELECT_ALL_NOT_CONTENT_UPDATED_LOCATIONS_GER = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE "
			+ DBHelper.COL_CONTENT_UPDATED_GER + " = 0";

	private final String COUNT_ALL_NOT_CONTENT_UPDATED_LOCATIONS_GER = "SELECT COUNT(1) FROM " + DBHelper.TABLE_LOCATION + " WHERE "
			+ DBHelper.COL_CONTENT_UPDATED_GER + " = 0";

	private final String SELECT_ALL_NOT_CONTENT_UPDATED_LOCATIONS_ENG = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE "
			+ DBHelper.COL_CONTENT_UPDATED_ENG + " = 0";

	private final String SELECT_ALL_NOT_CONTENT_UPDATED_LOCATIONS_IMG = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE "
			+ DBHelper.COL_CONTENT_UPDATED_IMG + " = 0";

	private final String COUNT_ALL_NOT_CONTENT_UPDATED_LOCATIONS_ENG = "SELECT COUNT(1) FROM " + DBHelper.TABLE_LOCATION + " WHERE "
			+ DBHelper.COL_CONTENT_UPDATED_ENG + " = 0";

	private final String SELECT_ALL_LOCATIONS_SORTED_BY_DISTANCE = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " ORDER BY " + DBHelper.COL_DISTANCE
			+ " ASC";
	private final String SELECT_ALL_LOCATIONS_SORTED_ALPHABET_GERMAN = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " ORDER BY UPPER("
			+ DBHelper.COL_NAME_DE + ") ASC";
	private final String SELECT_ALL_LOCATIONS_SORTED_ALPHABET_ENGLISH = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " ORDER BY UPPER("
			+ DBHelper.COL_NAME_EN + ") ASC";
	// FAVORITED
	private final String SELECT_FAVORITED_LOCATIONS = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE " + DBHelper.COL_FAVORITED + " = 1";
	private final String SELECT_FAVORITED_LOCATIONS_SORTED_BY_DISTANCE = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE "
			+ DBHelper.COL_FAVORITED + " = 1 ORDER BY " + DBHelper.COL_DISTANCE + " ASC";
	private final String SELECT_FAVORITED_LOCATIONS_SORTED_ALPHABET_GERMAN = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE "
			+ DBHelper.COL_FAVORITED + " = 1 ORDER BY UPPER(" + DBHelper.COL_NAME_DE + ") ASC";
	private final String SELECT_FAVORITED_LOCATIONS_SORTED_ALPHABET_ENGLISH = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE "
			+ DBHelper.COL_FAVORITED + " = 1 ORDER BY UPPER(" + DBHelper.COL_NAME_EN + ") ASC";
	// HISTORIC
	private final String SELECT_HISTORIC_LOCATIONS = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE " + DBHelper.COL_CATEGORY + " = "
			+ TAG_HISTORIC_PLACES;
	private final String SELECT_HISTORIC_LOCATIONS_SORTED_BY_DISTANCE = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE "
			+ DBHelper.COL_CATEGORY + " = " + TAG_HISTORIC_PLACES + " ORDER BY " + DBHelper.COL_DISTANCE + " ASC";
	private final String SELECT_HISTORIC_LOCATIONS_SORTED_ALPHABET_GERMAN = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE "
			+ DBHelper.COL_CATEGORY + " = " + TAG_HISTORIC_PLACES + " ORDER BY UPPER(" + DBHelper.COL_NAME_DE + ") ASC";
	private final String SELECT_HISTORIC_LOCATIONS_SORTED_ALPHABET_ENGLISH = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE "
			+ DBHelper.COL_CATEGORY + " = " + TAG_HISTORIC_PLACES + " ORDER BY UPPER(" + DBHelper.COL_NAME_EN + ") ASC";
	// MUSEUM
	private final String SELECT_MUSEUM = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE " + DBHelper.COL_CATEGORY + " = " + TAG_MUSEUM;
	private final String SELECT_MUSEUM_SORTED_BY_DISTANCE = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE " + DBHelper.COL_CATEGORY + " = "
			+ TAG_MUSEUM + " ORDER BY " + DBHelper.COL_DISTANCE + " ASC";
	private final String SELECT_MUSEUM_SORTED_ALPHABET_GERMAN = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE " + DBHelper.COL_CATEGORY
			+ " = " + TAG_MUSEUM + " ORDER BY UPPER(" + DBHelper.COL_NAME_DE + ") ASC";
	private final String SELECT_MUSEUM_SORTED_ALPHABET_ENGLISH = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE " + DBHelper.COL_CATEGORY
			+ " = " + TAG_MUSEUM + " ORDER BY UPPER(" + DBHelper.COL_NAME_EN + ") ASC";
	// EAT & DRINK
	private final String SELECT_EAT_AND_DRINK = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE " + DBHelper.COL_CATEGORY + " = "
			+ TAG_EAT_AND_DRINK;
	private final String SELECT_EAT_AND_DRINK_SORTED_BY_DISTANCE = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE " + DBHelper.COL_CATEGORY
			+ " = " + TAG_EAT_AND_DRINK + " ORDER BY " + DBHelper.COL_DISTANCE + " ASC";
	private final String SELECT_EAT_AND_DRINK_SORTED_ALPHABET_GERMAN = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE " + DBHelper.COL_CATEGORY
			+ " = " + TAG_EAT_AND_DRINK + " ORDER BY UPPER(" + DBHelper.COL_NAME_DE + ") ASC";
	private final String SELECT_EAT_AND_DRINK_SORTED_ALPHABET_ENGLISH = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE "
			+ DBHelper.COL_CATEGORY + " = " + TAG_EAT_AND_DRINK + " ORDER BY UPPER(" + DBHelper.COL_NAME_EN + ") ASC";

	private final String COUNT_ALL_LOCATIONS = "SELECT COUNT(1) FROM " + DBHelper.TABLE_LOCATION;
	private final String CHECK_FAVORITED = "SELECT " + DBHelper.COL_FAVORITED + " FROM " + DBHelper.TABLE_LOCATION + " WHERE _id= ";
	private final String GET_ID_FROM_NAME = "SELECT _ID FROM " + DBHelper.TABLE_LOCATION + " WHERE " + DBHelper.COL_NAME_DE + " = ";
	private final String GET_DISTANCE_FROM_ID = "SELECT " + DBHelper.COL_DISTANCE + " FROM " + DBHelper.TABLE_LOCATION + " WHERE " + DBHelper.COL_ID
			+ " = ";

	private final String GET_LOCATION_FROM_NAME = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE " + DBHelper.COL_NAME_DE + " = ";

	private final String GET_LOCATION_FROM_ID = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE " + DBHelper.COL_ID + " = ";
	private final String GET_LOCATION_FROM_JSON_ID = "SELECT * FROM " + DBHelper.TABLE_LOCATION + " WHERE " + DBHelper.COL_JSON_ID + " = ";

	private final String COUNT_VIDEOS_DOWNLOADED = "SELECT COUNT(1) FROM " + DBHelper.TABLE_LOCATION + " WHERE " + DBHelper.COL_VIDEO_PATH_DE
			+ " is not NULL and not(" + DBHelper.COL_VIDEO_PATH_DE + "='')";

	// ALL
	public VideoLocationDB[] getVideoLocations(SQLiteDatabase db) {
		return getVideoLocations(db, 'a');
	}

	// NOT IMAGE UPDATED
	public VideoLocationDB[] getNotUpdatedImageVideoLocations(SQLiteDatabase db) {

		Cursor cursor = db.rawQuery(SELECT_ALL_NOT_CONTENT_UPDATED_LOCATIONS_IMG, null);
		VideoLocationDB[] videoLocationsDB = parseDBEntries(cursor);
		if (videoLocationsDB == null)
			videoLocationsDB = new VideoLocationDB[0];
		cursor.close();
		return videoLocationsDB;
	}

	// NOT CONTENT UPDATED
	public VideoLocationDB[] getAllLocationsWithVideo(SQLiteDatabase db, Language language) {
		String query;
		if (language == Language.ENGLISH)
			query = SELECT_ALL_LOCATIONS_WITH_VIDEO_ENG;
		else
			query = SELECT_ALL_LOCATIONS_WITH_VIDEO_GER;

		Cursor cursor = db.rawQuery(query, null);
		VideoLocationDB[] videoLocationsDB = parseDBEntries(cursor);
		if (videoLocationsDB == null)
			videoLocationsDB = new VideoLocationDB[0];
		cursor.close();
		return videoLocationsDB;
	}

	// NOT CONTENT UPDATED
	public VideoLocationDB[] getNotUpdatedContentVideoLocations(SQLiteDatabase db, Language language) {
		String query;
		if (language == Language.ENGLISH)
			query = SELECT_ALL_NOT_CONTENT_UPDATED_LOCATIONS_ENG;
		else
			query = SELECT_ALL_NOT_CONTENT_UPDATED_LOCATIONS_GER;

		Cursor cursor = db.rawQuery(query, null);
		VideoLocationDB[] videoLocationsDB = parseDBEntries(cursor);
		if (videoLocationsDB == null)
			videoLocationsDB = new VideoLocationDB[0];
		cursor.close();
		return videoLocationsDB;
	}

	// BY CATEGORY
	public VideoLocationDB[] getVideoLocations(SQLiteDatabase db, char category) {
		switch (category) {
		case 'f':
			Query = SELECT_FAVORITED_LOCATIONS;
			break;
		case 'h':
			Query = SELECT_HISTORIC_LOCATIONS;
			break;
		case 'e':
			Query = SELECT_EAT_AND_DRINK;
			break;
		case 'm':
			Query = SELECT_MUSEUM;
			break;
		default:
			Query = SELECT_ALL_LOCATIONS;
			break;
		}
		Cursor cursor = db.rawQuery(Query, null);
		VideoLocationDB[] videoLocationsDB = parseDBEntries(cursor);
		cursor.close();
		return videoLocationsDB;
	}

	// DISTANCE SORT
	public VideoLocationDB[] getVideoLocationsDistanceSorted(SQLiteDatabase db, char category) {
		switch (category) {
		case 'f':
			Query = SELECT_FAVORITED_LOCATIONS_SORTED_BY_DISTANCE;
			break;
		case 'h':
			Query = SELECT_HISTORIC_LOCATIONS_SORTED_BY_DISTANCE;
			break;
		case 'e':
			Query = SELECT_EAT_AND_DRINK_SORTED_BY_DISTANCE;
			break;
		case 'm':
			Query = SELECT_MUSEUM_SORTED_BY_DISTANCE;
			break;
		default:
			Query = SELECT_ALL_LOCATIONS_SORTED_BY_DISTANCE;
			break;
		}

		Cursor cursor = db.rawQuery(Query, null);
		VideoLocationDB[] videoLocationsDB = parseDBEntries(cursor);

		if (videoLocationsDB == null)
			videoLocationsDB = new VideoLocationDB[0];

		cursor.close();
		return videoLocationsDB;
	}

	// BY UPDATE TIME
	public VideoLocationDB[] getVideoLocations(SQLiteDatabase db, long time) {

		String[] _time = { String.valueOf(time) };
		Cursor cursor = db.rawQuery(SELECT_ALL_UPDATED_LOCATIONS, _time);
		VideoLocationDB[] videoLocationsDB = parseDBEntries(cursor);
		cursor.close();
		return videoLocationsDB;
	}

	// ALPHABETICALLY GERMAN
	public VideoLocationDB[] getVideoLocationsAlphabeticallySortedGerman(SQLiteDatabase db, char category) {
		switch (category) {
		case 'f':
			Query = SELECT_FAVORITED_LOCATIONS_SORTED_ALPHABET_GERMAN;
			break;
		case 'h':
			Query = SELECT_HISTORIC_LOCATIONS_SORTED_ALPHABET_GERMAN;
			break;
		case 'e':
			Query = SELECT_EAT_AND_DRINK_SORTED_ALPHABET_GERMAN;
			break;
		case 'm':
			Query = SELECT_MUSEUM_SORTED_ALPHABET_GERMAN;
			break;
		default:
			Query = SELECT_ALL_LOCATIONS_SORTED_ALPHABET_GERMAN;
			break;
		}

		Cursor cursor = db.rawQuery(Query, null);
		VideoLocationDB[] videoLocationsDB = parseDBEntries(cursor);

		if (videoLocationsDB == null)
			videoLocationsDB = new VideoLocationDB[0];

		cursor.close();
		return videoLocationsDB;
	}

	// ALPHABETICALLY ENGLISH
	public VideoLocationDB[] getVideoLocationsAlphabeticallySortedEnglish(SQLiteDatabase db, char category) {
		switch (category) {
		case 'f':
			Query = SELECT_FAVORITED_LOCATIONS_SORTED_ALPHABET_ENGLISH;
			break;
		case 'h':
			Query = SELECT_HISTORIC_LOCATIONS_SORTED_ALPHABET_ENGLISH;
			break;
		case 'e':
			Query = SELECT_EAT_AND_DRINK_SORTED_ALPHABET_ENGLISH;
			break;
		case 'm':
			Query = SELECT_MUSEUM_SORTED_ALPHABET_ENGLISH;
			break;
		default:
			Query = SELECT_ALL_LOCATIONS_SORTED_ALPHABET_ENGLISH;
			break;
		}

		Cursor cursor = db.rawQuery(Query, null);
		VideoLocationDB[] videoLocationsDB = parseDBEntries(cursor);

		if (videoLocationsDB == null)
			videoLocationsDB = new VideoLocationDB[0];

		cursor.close();
		return videoLocationsDB;
	}

	// PARSE
	private VideoLocationDB[] parseDBEntries(Cursor cursor) {
		VideoLocationDB[] videoLocationsDB = null;
		if (!cursor.moveToFirst()) {
			cursor.close();
			return null;
		} else {

			videoLocationsDB = new VideoLocationDB[cursor.getCount()];
			int i = 0;
			while (!cursor.isAfterLast()) {
				try {
					VideoLocationDB videoLocation = new VideoLocationDB();

					videoLocation._id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_ID));
					videoLocation.category = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_CATEGORY));
					videoLocation.email = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_EMAIL));
					videoLocation.name_de = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_NAME_DE));
					videoLocation.name_en = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_NAME_EN));
					videoLocation.is_favorited = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_FAVORITED));
					videoLocation.longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_LONGITUDE));
					videoLocation.latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_LATITUDE));
					videoLocation.street = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STREET));
					videoLocation.street_number = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STREET_NUMBER));
					videoLocation.zip_code = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ZIPCODE));
					videoLocation.id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_JSON_ID));
					videoLocation.text_de = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEXT_DE));
					videoLocation.text_en = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEXT_EN));
					videoLocation.website = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_WEBSITE));
					videoLocation.phone = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PHONE));
					videoLocation.photo_url = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PHOTO_URL));
					videoLocation.photo_detail_url = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PHOTO_DETAIL_URL));
					videoLocation.thumbnail_url = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_THUMB_URL));
					videoLocation.audio_url_de = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_AUDIO_URL_DE));
					videoLocation.audio_url_en = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_AUDIO_URL_EN));
					videoLocation.video_url_de = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_VIDEO_URL_DE));
					videoLocation.video_url_en = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_VIDEO_URL_EN));
					videoLocation.photo_detail_path = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PHOTO_DETAIL_PATH));
					videoLocation.thumbnail_path = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_THUMB_PATH));
					videoLocation.video_path_de = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_VIDEO_PATH_DE));
					videoLocation.video_path_en = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_VIDEO_PATH_EN));
					videoLocation.audio_path_de = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_AUDIO_PATH_DE));
					videoLocation.audio_path_en = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_AUDIO_PATH_EN));
					videoLocation.attr_opening_hours = new OpeningHoursJSON();
					videoLocation.attr_opening_hours.setMonday(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_MONDAY)));
					videoLocation.attr_opening_hours.setTuesday(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TUESDAY)));
					videoLocation.attr_opening_hours.setWednesday(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_WEDNESDAY)));
					videoLocation.attr_opening_hours.setThursday(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_THURSDAY)));
					videoLocation.attr_opening_hours.setFriday(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_FRIDAY)));
					videoLocation.attr_opening_hours.setSaturday(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_SATURDAY)));
					videoLocation.attr_opening_hours.setSunday(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_SUNDAY)));
					videoLocation.distance = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_DISTANCE));
					videoLocation.update_time = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_UPDATE_TIME));
					videoLocation.content_updated_img = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_CONTENT_UPDATED_IMG));
					videoLocation.content_updated_ger = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_CONTENT_UPDATED_GER));
					videoLocation.content_updated_eng = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_CONTENT_UPDATED_ENG));

					videoLocationsDB[i] = videoLocation;
					i++;

				} catch (JsonSyntaxException e) {
					// TODO: handle exception
				}
				cursor.moveToNext();
			}
		}
		return videoLocationsDB;
	}

	// INSERT JSONs
	public boolean InsertLocationArray(SQLiteDatabase db, VideoLocationJSON[] vidLocs) {
		try {
			for (int i = 0; i < vidLocs.length; i++) {
				VideoLocationJSON vidLoc = vidLocs[i];

				long ind = InsertLocationJSON(db, vidLoc);
				Log.d("JsonDB", "created row with id: " + ind);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.d("JsonDB", "InsertLocationArray() : error writing locations infos to db, returning false...");
			return false;
		}
		return true;
	}

	// INSERT 1 JSON
	private long InsertLocationJSON(SQLiteDatabase db, VideoLocationJSON vidLoc) throws JSONException {
		Gson gson = new Gson();
		String locationJSON = gson.toJson(vidLoc, VideoLocationJSON.class);

		ContentValues vals = new ContentValues();
		vals.put(DBHelper.COL_UPDATED, (int) vidLoc.updated_ts);
		vals.put(DBHelper.COL_DELETED, (int) vidLoc.deleted_ts);
		vals.put(DBHelper.COL_CATEGORY, vidLoc.category);
		vals.put(DBHelper.COL_NAME_DE, vidLoc.name_de);
		vals.put(DBHelper.COL_NAME_EN, vidLoc.name_en);
		vals.put(DBHelper.COL_FAVORITED, "0");
		vals.put(DBHelper.COL_LONGITUDE, String.valueOf(vidLoc.longitude));
		vals.put(DBHelper.COL_LATITUDE, String.valueOf(vidLoc.latitude));
		vals.put(DBHelper.COL_JSON_ID, String.valueOf(vidLoc.id));
		vals.put(DBHelper.COL_TEXT_DE, vidLoc.text_de);
		vals.put(DBHelper.COL_TEXT_EN, vidLoc.text_en);
		vals.put(DBHelper.COL_STREET, vidLoc.street);
		vals.put(DBHelper.COL_STREET_NUMBER, vidLoc.street_number);
		vals.put(DBHelper.COL_ZIPCODE, vidLoc.zip_code);
		vals.put(DBHelper.COL_PHONE, vidLoc.phone);
		vals.put(DBHelper.COL_EMAIL, vidLoc.email);
		vals.put(DBHelper.COL_WEBSITE, vidLoc.website);
		vals.put(DBHelper.COL_PHOTO_URL, vidLoc.thumbnail_url);
		vals.put(DBHelper.COL_PHOTO_DETAIL_URL, vidLoc.thumbnail_url_detail);
		vals.put(DBHelper.COL_THUMB_URL, vidLoc.thumbnail_url_thumb);
		vals.put(DBHelper.COL_AUDIO_URL_DE, vidLoc.audio_url_de);
		vals.put(DBHelper.COL_AUDIO_URL_EN, vidLoc.audio_url_en);
		vals.put(DBHelper.COL_VIDEO_URL_DE, vidLoc.video_url_de);
		vals.put(DBHelper.COL_VIDEO_URL_EN, vidLoc.video_url_en);
		vals.put(DBHelper.COL_PHOTO_DETAIL_PATH, "");
		vals.put(DBHelper.COL_THUMB_PATH, "");
		vals.put(DBHelper.COL_VIDEO_PATH_DE, "");
		vals.put(DBHelper.COL_VIDEO_PATH_EN, "");
		vals.put(DBHelper.COL_AUDIO_PATH_DE, "");
		vals.put(DBHelper.COL_AUDIO_PATH_EN, "");
		vals.put(DBHelper.COL_MONDAY, vidLoc.attr_opening_hours.getMonday());
		vals.put(DBHelper.COL_TUESDAY, vidLoc.attr_opening_hours.getTuesday());
		vals.put(DBHelper.COL_WEDNESDAY, vidLoc.attr_opening_hours.getWednesday());
		vals.put(DBHelper.COL_THURSDAY, vidLoc.attr_opening_hours.getThursday());
		vals.put(DBHelper.COL_FRIDAY, vidLoc.attr_opening_hours.getFriday());
		vals.put(DBHelper.COL_SATURDAY, vidLoc.attr_opening_hours.getSaturday());
		vals.put(DBHelper.COL_SUNDAY, vidLoc.attr_opening_hours.getSunday());
		vals.put(DBHelper.COL_UPDATE_TIME, Util.now());
		vals.put(DBHelper.COL_CONTENT_UPDATED_IMG, 0);
		if (vidLoc.audio_url_de != null && vidLoc.video_url_de != null)
			vals.put(DBHelper.COL_CONTENT_UPDATED_GER, 0);
		else
			vals.put(DBHelper.COL_CONTENT_UPDATED_GER, 1);
		if (vidLoc.audio_url_en != null && vidLoc.video_url_en != null)
			vals.put(DBHelper.COL_CONTENT_UPDATED_ENG, 0);
		else
			vals.put(DBHelper.COL_CONTENT_UPDATED_ENG, 1);
		vals.put(DBHelper.COL_JSON, locationJSON);

		TransportationDAO transportationDAO = new TransportationDAO();
		transportationDAO.insertTransportationArray(db, vidLoc);
		long i = db.insert(DBHelper.TABLE_LOCATION, null, vals);
		return i;
	}

	// UPDATE LOCATIONs
	public boolean UpdateLocationArray(SQLiteDatabase db, VideoLocationJSON[] vidLocs) {
		try {
			for (int i = 0; i < vidLocs.length; i++) {
				VideoLocationJSON vidLoc = vidLocs[i];

				long ind = UpdateLocationJSON(db, vidLoc);
				Log.d("JsonDB", "created row with id: " + ind);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.d("JsonDB", "InsertLocationArray() : error writing locations infos to db, returning false...");
			return false;
		}
		return true;
	}

	// UPDATE 1 LOCATION
	private long UpdateLocationJSON(SQLiteDatabase db, VideoLocationJSON vidLoc) throws JSONException {
		Gson gson = new Gson();
		String locationJSON = gson.toJson(vidLoc, VideoLocationJSON.class);

		if (vidLoc.deleted_ts != 0) {
			// TODO: Test deleting
			String[] id = { String.valueOf(vidLoc.id) };
			return db.delete(DBHelper.TABLE_LOCATION, DBHelper.COL_JSON_ID + "=?", id);
		} else {
			ContentValues vals = new ContentValues();
			vals.put(DBHelper.COL_UPDATED, (int) vidLoc.updated_ts);
			vals.put(DBHelper.COL_DELETED, (int) vidLoc.deleted_ts);
			vals.put(DBHelper.COL_CATEGORY, vidLoc.category);
			vals.put(DBHelper.COL_NAME_DE, vidLoc.name_de);
			vals.put(DBHelper.COL_NAME_EN, vidLoc.name_en);
			vals.put(DBHelper.COL_FAVORITED, "0");
			vals.put(DBHelper.COL_LONGITUDE, String.valueOf(vidLoc.longitude));
			vals.put(DBHelper.COL_LATITUDE, String.valueOf(vidLoc.latitude));
			vals.put(DBHelper.COL_JSON_ID, String.valueOf(vidLoc.id));
			vals.put(DBHelper.COL_TEXT_DE, vidLoc.text_de);
			vals.put(DBHelper.COL_TEXT_EN, vidLoc.text_en);
			vals.put(DBHelper.COL_STREET, vidLoc.street);
			vals.put(DBHelper.COL_STREET_NUMBER, vidLoc.street_number);
			vals.put(DBHelper.COL_ZIPCODE, vidLoc.zip_code);
			vals.put(DBHelper.COL_PHONE, vidLoc.phone);
			vals.put(DBHelper.COL_EMAIL, vidLoc.email);
			vals.put(DBHelper.COL_WEBSITE, vidLoc.website);
			vals.put(DBHelper.COL_PHOTO_URL, vidLoc.thumbnail_url);
			vals.put(DBHelper.COL_PHOTO_DETAIL_URL, vidLoc.thumbnail_url_detail);
			vals.put(DBHelper.COL_THUMB_URL, vidLoc.thumbnail_url_thumb);
			vals.put(DBHelper.COL_AUDIO_URL_DE, vidLoc.audio_url_de);
			vals.put(DBHelper.COL_AUDIO_URL_EN, vidLoc.audio_url_en);
			vals.put(DBHelper.COL_VIDEO_URL_DE, vidLoc.video_url_de);
			vals.put(DBHelper.COL_VIDEO_URL_EN, vidLoc.video_url_en);
			vals.put(DBHelper.COL_PHOTO_DETAIL_PATH, "");
			vals.put(DBHelper.COL_THUMB_PATH, "");
			vals.put(DBHelper.COL_VIDEO_PATH_DE, "");
			vals.put(DBHelper.COL_VIDEO_PATH_EN, "");
			vals.put(DBHelper.COL_AUDIO_PATH_DE, "");
			vals.put(DBHelper.COL_AUDIO_PATH_EN, "");
			vals.put(DBHelper.COL_MONDAY, vidLoc.attr_opening_hours.getMonday());
			vals.put(DBHelper.COL_TUESDAY, vidLoc.attr_opening_hours.getTuesday());
			vals.put(DBHelper.COL_WEDNESDAY, vidLoc.attr_opening_hours.getWednesday());
			vals.put(DBHelper.COL_THURSDAY, vidLoc.attr_opening_hours.getThursday());
			vals.put(DBHelper.COL_FRIDAY, vidLoc.attr_opening_hours.getFriday());
			vals.put(DBHelper.COL_SATURDAY, vidLoc.attr_opening_hours.getSaturday());
			vals.put(DBHelper.COL_SUNDAY, vidLoc.attr_opening_hours.getSunday());
			vals.put(DBHelper.COL_UPDATE_TIME, Util.now());
			vals.put(DBHelper.COL_CONTENT_UPDATED_IMG, 0);
			// CONTENT VIDEO AUDIO
			if (vidLoc.audio_url_de != null && vidLoc.video_url_de != null)
				vals.put(DBHelper.COL_CONTENT_UPDATED_GER, 0);
			else
				vals.put(DBHelper.COL_CONTENT_UPDATED_GER, 1);
			if (vidLoc.audio_url_en != null && vidLoc.video_url_en != null)
				vals.put(DBHelper.COL_CONTENT_UPDATED_ENG, 0);
			else
				vals.put(DBHelper.COL_CONTENT_UPDATED_ENG, 1);

			vals.put(DBHelper.COL_JSON, locationJSON);

			db.beginTransaction();
			TransportationDAO transportationDAO = new TransportationDAO();
			transportationDAO.updateTransportationArray(db, vidLoc);

			String[] id = { String.valueOf(vidLoc.id) };
			int result = db.update(DBHelper.TABLE_LOCATION, vals, DBHelper.COL_JSON_ID + "=?", id);
			if (result > 0)
				Log.d(TAG, String.valueOf(result));
			else
				db.insert(DBHelper.TABLE_LOCATION, null, vals);// Insert if not exist: New location

			db.setTransactionSuccessful();
			db.endTransaction();
			return result;
		}
	}

	// PARSE CURSOR
	public VideoLocationDB getVideoLocationFromCursor(Cursor cursor) {
		VideoLocationDB videoLocation = new VideoLocationDB();
		videoLocation._id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_ID));
		videoLocation.updated_ts = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_UPDATED));
		videoLocation.deleted_ts = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_DELETED));
		videoLocation.category = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_CATEGORY));
		videoLocation.email = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_EMAIL));
		videoLocation.name_de = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_NAME_DE));
		videoLocation.name_en = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_NAME_EN));
		videoLocation.is_favorited = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_FAVORITED));
		videoLocation.longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_LONGITUDE));
		videoLocation.latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_LATITUDE));
		videoLocation.street = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STREET));
		videoLocation.street_number = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STREET_NUMBER));
		videoLocation.zip_code = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ZIPCODE));
		videoLocation.id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_JSON_ID));
		videoLocation.text_de = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEXT_DE));
		videoLocation.text_en = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TEXT_EN));
		videoLocation.website = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_WEBSITE));
		videoLocation.phone = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PHONE));
		videoLocation.photo_url = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PHOTO_URL));
		videoLocation.photo_detail_url = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PHOTO_DETAIL_URL));
		videoLocation.thumbnail_url = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_THUMB_URL));
		videoLocation.audio_url_de = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_AUDIO_URL_DE));
		videoLocation.audio_url_en = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_AUDIO_URL_EN));
		videoLocation.video_url_de = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_VIDEO_URL_DE));
		videoLocation.video_url_en = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_VIDEO_URL_EN));
		videoLocation.photo_detail_path = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PHOTO_DETAIL_PATH));
		videoLocation.thumbnail_path = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_THUMB_PATH));
		videoLocation.video_path_de = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_VIDEO_PATH_DE));
		videoLocation.video_path_en = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_VIDEO_PATH_EN));
		videoLocation.audio_path_de = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_AUDIO_PATH_DE));
		videoLocation.audio_path_en = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_AUDIO_PATH_EN));
		videoLocation.attr_opening_hours = new OpeningHoursJSON();
		videoLocation.attr_opening_hours.setMonday(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_MONDAY)));
		videoLocation.attr_opening_hours.setTuesday(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TUESDAY)));
		videoLocation.attr_opening_hours.setWednesday(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_WEDNESDAY)));
		videoLocation.attr_opening_hours.setThursday(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_THURSDAY)));
		videoLocation.attr_opening_hours.setFriday(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_FRIDAY)));
		videoLocation.attr_opening_hours.setSaturday(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_SATURDAY)));
		videoLocation.attr_opening_hours.setSunday(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_SUNDAY)));
		videoLocation.distance = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_DISTANCE));
		videoLocation.content_updated_img = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_CONTENT_UPDATED_IMG));
		videoLocation.content_updated_ger = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_CONTENT_UPDATED_GER));
		videoLocation.content_updated_eng = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_CONTENT_UPDATED_ENG));

		return videoLocation;
	}

	// SELECT 1 BY NAME
	public VideoLocationDB getVideoLocation(SQLiteDatabase db, String name) {
		VideoLocationDB videoLocation = new VideoLocationDB();
		Cursor cursor = db.rawQuery(GET_LOCATION_FROM_NAME + "\"" + name + "\"", null);
		cursor.moveToFirst();

		videoLocation = getVideoLocationFromCursor(cursor);
		TransportationDAO transportationDAO = new TransportationDAO();
		transportationDAO.getTransportationArray(db, videoLocation);
		cursor.close();
		return videoLocation;
	}

	// SELECT 1 BY ID
	public VideoLocationDB getVideoLocation(SQLiteDatabase db, int id) {
		Log.d(TAG, "Query");
		VideoLocationDB videoLocation = new VideoLocationDB();
		Cursor cursor = db.rawQuery(GET_LOCATION_FROM_ID + id, null);
		cursor.moveToFirst();
		Log.d(TAG, "Parse Location");
		videoLocation = getVideoLocationFromCursor(cursor);
		// TransportationDAO transportationDAO = new TransportationDAO();
		// Log.d(TAG, "Parse Transportation");
		// videoLocation.attr_transportation = transportationDAO.getTransportationArray(db, videoLocation);
		cursor.close();
		return videoLocation;
	}

	// SELECT 1 BY JSON_ID
	public VideoLocationDB getVideoLocationByJsonId(SQLiteDatabase db, int id) {
//		Log.d(TAG, "Query");
		VideoLocationDB videoLocation = new VideoLocationDB();
		Cursor cursor = db.rawQuery(GET_LOCATION_FROM_JSON_ID + "\"" + id + "\"", null);
		cursor.moveToFirst();
//		Log.d(TAG, "Parse Location");
		if (cursor.getCount() > 0) {
			videoLocation = getVideoLocationFromCursor(cursor);
		}
		cursor.close();
		return videoLocation;
	}

	// SELECT > 0
	public Boolean areVideoLocations(SQLiteDatabase db) {
		Cursor cursor = db.rawQuery(COUNT_ALL_LOCATIONS, null);
		cursor.moveToFirst();
		Integer count = Integer.valueOf(cursor.getString(0));
		cursor.close();
		return count > 0;
	}

	// COUNT
	public Integer countVideoLocations(SQLiteDatabase db) {
		Cursor cursor = db.rawQuery(COUNT_ALL_LOCATIONS, null);
		cursor.moveToFirst();
		Integer count = Integer.valueOf(cursor.getString(0));
		cursor.close();
		return count;
	}

	// ID->NAME
	public Integer getIdFromName(SQLiteDatabase db, String name) {
		Cursor cursor = db.rawQuery(GET_ID_FROM_NAME + "\"" + name + "\"", null);
		cursor.moveToFirst();
		Integer id = Integer.valueOf(cursor.getString(0));
		cursor.close();
		return id;
	}

	// IS FAVORITED?
	public Boolean isFavorited(SQLiteDatabase db, Integer id) {
		Cursor cursor = db.rawQuery(CHECK_FAVORITED + id, null);
		cursor.moveToFirst();
		Integer favorited = Integer.valueOf(cursor.getString(0));
		cursor.close();
		return favorited == 1;
	}

	// !FAVORITED
	public void toogleFavorite(SQLiteDatabase db, Integer id) {
		String strFilter = "_id=" + id;
		Boolean favorited = !isFavorited(db, id);
		ContentValues args = new ContentValues();
		args.put(DBHelper.COL_FAVORITED, favorited);
		db.update(DBHelper.TABLE_LOCATION, args, strFilter, null);
	}

	// SET DISTANCE
	public void setDistance(SQLiteDatabase db, Integer id, double distance) {
		String strFilter = "_id=" + id;
		ContentValues args = new ContentValues();
		args.put(DBHelper.COL_DISTANCE, distance);
		db.update(DBHelper.TABLE_LOCATION, args, strFilter, null);
	}

	// GET DISTANCE
	public double getDistance(SQLiteDatabase db, int id) {
		Cursor cursor = db.rawQuery(GET_DISTANCE_FROM_ID + id, null);
		cursor.moveToFirst();
		double distance = cursor.getDouble(0);
		cursor.close();
		return distance;
	}

	// CONTENT>0?
	public Boolean isContentNotUpdated(SQLiteDatabase db, Language language) {
		String query;
		if (language == Language.ENGLISH)
			query = COUNT_ALL_NOT_CONTENT_UPDATED_LOCATIONS_ENG;
		else
			query = COUNT_ALL_NOT_CONTENT_UPDATED_LOCATIONS_GER;

		Cursor cursor = db.rawQuery(query, null);
		cursor.moveToFirst();
		Integer count = Integer.valueOf(cursor.getString(0));
		cursor.close();
		return count > 0;
	}

	// UPDATE CONTENT
	public int updatePaths(SQLiteDatabase db, VideoLocationDB videoLocation, Language language) {
		int result = 0;
		String column;
		if (language == Language.ENGLISH)
			column = DBHelper.COL_CONTENT_UPDATED_ENG;
		else
			column = DBHelper.COL_CONTENT_UPDATED_GER;

		ContentValues contentValues = new ContentValues();
		contentValues.put(DBHelper.COL_VIDEO_PATH_DE, videoLocation.video_path_de);
		contentValues.put(DBHelper.COL_VIDEO_PATH_EN, videoLocation.video_path_en);
		contentValues.put(DBHelper.COL_PHOTO_DETAIL_PATH, videoLocation.photo_detail_path);
		// contentValues.put(DBHelper.COL_THUMB_PATH, videoLocation.thumbnail_path); Not needed here
		contentValues.put(DBHelper.COL_AUDIO_PATH_DE, videoLocation.audio_path_de);
		contentValues.put(DBHelper.COL_AUDIO_PATH_EN, videoLocation.audio_path_en);
		contentValues.put(DBHelper.COL_UPDATE_TIME, Util.now());
		contentValues.put(column, 1);// Content Downloaded
		result = db.update(DBHelper.TABLE_LOCATION, contentValues, DBHelper.COL_ID + "=?", new String[] { String.valueOf(videoLocation._id) });

		return result;
	}
	
	// UPDATE CONTENT
	public void updateContentPath(SQLiteDatabase db, VideoLocationDB videoLocation, Language language) {
		String column;
		ContentValues contentValues = new ContentValues();
		if (language == Language.ENGLISH){
			column = DBHelper.COL_CONTENT_UPDATED_ENG;
			
			if(videoLocation.video_path_en != null && videoLocation.video_path_en.trim().length() > 0 && 
					videoLocation.audio_path_en != null && videoLocation.audio_path_en.trim().length() > 0)
				contentValues.put(column, 1);// Content Downloaded			
		} else {
			column = DBHelper.COL_CONTENT_UPDATED_GER;
			
			if(videoLocation.video_path_de != null && videoLocation.video_path_de.trim().length() > 0 && 
					videoLocation.audio_path_de != null && videoLocation.audio_path_de.trim().length() > 0)
				contentValues.put(column, 1);// Content Downloaded
		}
			
		contentValues.put(DBHelper.COL_VIDEO_PATH_DE, videoLocation.video_path_de);
		contentValues.put(DBHelper.COL_VIDEO_PATH_EN, videoLocation.video_path_en);
		// contentValues.put(DBHelper.COL_PHOTO_DETAIL_PATH, videoLocation.photo_detail_path); Not needed here
		// contentValues.put(DBHelper.COL_THUMB_PATH, videoLocation.thumbnail_path); Not needed here
		contentValues.put(DBHelper.COL_AUDIO_PATH_DE, videoLocation.audio_path_de);
		contentValues.put(DBHelper.COL_AUDIO_PATH_EN, videoLocation.audio_path_en);
		
		contentValues.put(DBHelper.COL_UPDATE_TIME, Util.now());
		
		db.update(DBHelper.TABLE_LOCATION, contentValues, DBHelper.COL_ID + "=?", new String[] { String.valueOf(videoLocation._id) });
	}

	// UPDATE IMAGE
	public void updateImagePath(SQLiteDatabase db, VideoLocationDB videoLocation) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(DBHelper.COL_PHOTO_DETAIL_PATH, videoLocation.photo_detail_path);
		if(videoLocation.photo_detail_path == null || videoLocation.photo_detail_path.trim().equals(""))
			contentValues.put(DBHelper.COL_CONTENT_UPDATED_IMG, 0);// Image Downloaded
		else 
			contentValues.put(DBHelper.COL_CONTENT_UPDATED_IMG, 1);// Image Downloaded
		db.update(DBHelper.TABLE_LOCATION, contentValues, DBHelper.COL_ID + "=?", new String[] { String.valueOf(videoLocation._id) });
	}

	// UPDATE THUMBNAIL
	public void updateThumbnailPath(SQLiteDatabase db, VideoLocationDB videoLocation) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(DBHelper.COL_THUMB_PATH, videoLocation.thumbnail_path);
		db.update(DBHelper.TABLE_LOCATION, contentValues, DBHelper.COL_ID + "=?", new String[] { String.valueOf(videoLocation._id) });
	}

}