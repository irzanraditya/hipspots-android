package com.hipspots.database;

import org.json.JSONException;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hipspots.model.TransportationDB;
import com.hipspots.model.TransportationJSON;
import com.hipspots.model.VideoLocationDB;
import com.hipspots.model.VideoLocationJSON;

public class TransportationDAO {

	private final String GET_TRANSPORTATION_FROM_LOC_ID = "SELECT * FROM " + DBHelper.TABLE_TRANSPORTATION + " WHERE "
			+ DBHelper.COL_TRANSPORTATION_LOC_ID + " = ";

	/*
	 * private final String GET_FOOTAGE_FROM_ID = "SELECT * FROM " + DBHelper.TABLE_FOOTAGE + " WHERE " + DBHelper.COL_FOOTAGE_JSON_ID + " = ";
	 */

	/*
	 * private final String GET_FOOTAGE_URLS_FROM_NAME = "SELECT " + DBHelper.COL_TRANSPORTATION_VIDEO_URL + " FROM " + DBHelper.TABLE_FOOTAGE +
	 * " WHERE " + DBHelper.COL_TRANSPORTATION_LOC_ID + " = ";
	 */

	public Boolean insertTransportationArray(SQLiteDatabase db, VideoLocationJSON vidLoc) {
		try {
			for (int i = 0; i < vidLoc.attr_transportation.length; i++) {
				TransportationJSON transportation = vidLoc.attr_transportation[i];

				long ind = insertTransportationJSON(db, vidLoc.id, transportation);
				Log.d("TransportationDAO", "Inserting Transportation: " + i + ", from : " + vidLoc.name_de + " in position: " + ind);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.d("TransportationDAO", "InsertTransportationArray() : error writing transportations infos to db, returning false...");
			return false;
		}
		return true;
	}

	private long insertTransportationJSON(SQLiteDatabase db, int videoLocId, TransportationJSON transportation) throws JSONException {

		ContentValues vals = new ContentValues();
		vals.put(DBHelper.COL_TRANSPORTATION_LOC_ID, videoLocId);
		vals.put(DBHelper.COL_TRANSPORTATION_TYPE, transportation.type);
		vals.put(DBHelper.COL_TRANSPORTATION_LINE, transportation.line);
		vals.put(DBHelper.COL_TRANSPORTATION_STATION, transportation.station);

		return db.insert(DBHelper.TABLE_TRANSPORTATION, null, vals);
	}

	public Boolean updateTransportationArray(SQLiteDatabase db, VideoLocationJSON vidLoc) {
		try {
			db.delete(DBHelper.TABLE_TRANSPORTATION, DBHelper.COL_TRANSPORTATION_LOC_ID + '=' + vidLoc.id, null);
			for (int i = 0; i < vidLoc.attr_transportation.length; i++) {
				TransportationJSON transportation = vidLoc.attr_transportation[i];

				long ind = insertTransportationJSON(db, vidLoc.id, transportation);
				Log.d("TransportationDAO", "Updating Transportation: " + i + ", from : " + vidLoc.name_de + " in position: " + ind);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.d("TransportationDAO", "Updating TransportationArray() : error writing transportations infos to db, returning false...");
			return false;
		}
		return true;
	}

	/*
	 * public TransportationDB getTransportation(SQLiteDatabase db, int id) { Cursor cursor = db.rawQuery(GET_TRANSPORTATION_FROM_ID + "\"" + id +"\""
	 * , null); TransportationDB transportation= new TransportationDB(); cursor.moveToFirst(); if (cursor.getCount()==0){ cursor.close(); return
	 * transportation; } transportation._id = cursor.getInt(cursor .getColumnIndexOrThrow(DBHelper.COL_TRANSPORTATION_ID)); transportation.location_id
	 * = cursor.getInt(cursor .getColumnIndexOrThrow(DBHelper.COL_TRANSPORTATION_LOC_ID)); transportation.type = cursor.getString(cursor
	 * .getColumnIndexOrThrow(DBHelper.COL_TRANSPORTATION_TYPE)); transportation.line = cursor.getString(cursor
	 * .getColumnIndexOrThrow(DBHelper.COL_TRANSPORTATION_LINE)); transportation.station = cursor.getString(cursor
	 * .getColumnIndexOrThrow(DBHelper.COL_TRANSPORTATION_STATION)); cursor.close(); return transportation; }
	 */
	public TransportationDB[] getTransportationArray(SQLiteDatabase db, VideoLocationDB videoLocation) {

		Cursor cursor = db.rawQuery(GET_TRANSPORTATION_FROM_LOC_ID + String.valueOf(videoLocation.id), null);
		int numberOfTransportations = cursor.getCount();
		TransportationDB[] transportations = new TransportationDB[numberOfTransportations];
		cursor.moveToFirst();
		for (int i = 0; i < transportations.length; i++) {
			TransportationDB transportation = new TransportationDB();
			transportation._id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_TRANSPORTATION_ID));
			transportation.location_id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_TRANSPORTATION_LOC_ID));
			transportation.type = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TRANSPORTATION_TYPE));
			transportation.line = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TRANSPORTATION_LINE));
			transportation.station = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_TRANSPORTATION_STATION));
			transportations[i] = transportation;
			cursor.moveToNext();
		}
		cursor.close();
		return transportations;
	}

	/*
	 * public void updatePaths(SQLiteDatabase db, TransportationDB footage) { ContentValues contentValues = new ContentValues();
	 * contentValues.put(DBHelper.COL_TRANSPORTATION_VIDEO_PATH, footage.video_path); contentValues .put(DBHelper.COL_TRANSPORTATION_THUMB_PATH,
	 * footage.thumbnail_path); db.update(DBHelper.TABLE_FOOTAGE, contentValues, DBHelper.COL_ID + "=?", new String[] { String.valueOf(footage._id)
	 * }); }
	 */

}
