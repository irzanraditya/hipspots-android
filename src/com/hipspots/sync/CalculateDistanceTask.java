package com.hipspots.sync;

import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;

import com.hipspots.app.HipspotsApplication;
import com.hipspots.database.VideoLocationDAO;
import com.hipspots.model.VideoLocationDB;

public class CalculateDistanceTask extends AsyncTask<Void, Void, Void> {

	private static CalculateDistanceTask task;

	private CalculateDistanceTask() {
		// TODO Auto-generated constructor stub
	}

	public static CalculateDistanceTask getInstance() {
		if (task != null && task.getStatus().equals(Status.RUNNING)) {
			task.cancel(true);
		}
		task = new CalculateDistanceTask();
		return task;
	}

	@Override
	protected Void doInBackground(Void... params) {
		SQLiteDatabase db = HipspotsApplication.getInstance().getJsonDBInstance().getWritableDatabase();
		VideoLocationDAO videoLocationsDAO = new VideoLocationDAO();
		if (HipspotsApplication.getVideoLocations() == null)
			HipspotsApplication.setVideoLocations(videoLocationsDAO.getVideoLocations(db));

		if (HipspotsApplication.getVideoLocations() != null) {
			double lat = HipspotsApplication.getLatitude();
			double lng = HipspotsApplication.getLongitude();
			for (VideoLocationDB location : HipspotsApplication.getVideoLocations()) {
				double distance = countDistance(lat, lng, location.latitude, location.longitude);
				videoLocationsDAO.setDistance(db, location._id, distance);
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		HipspotsApplication.getListActivityInstance().loadListView();
	}

	public double countDistance(double lat1, double lng1, double lat2, double lng2) {
		Location locationUser = new Location("point A");
		Location locationPlace = new Location("point B");
		locationUser.setLatitude(lat1);
		locationUser.setLongitude(lng1);
		locationPlace.setLatitude(lat2);
		locationPlace.setLongitude(lng2);

		double distance = locationUser.distanceTo(locationPlace);

		return distance;
	}
}
