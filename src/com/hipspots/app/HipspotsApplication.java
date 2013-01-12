package com.hipspots.app;

import android.app.Application;
import android.util.Log;
import android.widget.LinearLayout;

import com.hipspots.activities.DetailsActivity;
import com.hipspots.activities.LocationsListActivity;
import com.hipspots.activities.MainFragmentActivity;
import com.hipspots.activities.MyMapActivity;
import com.hipspots.database.DBHelper;
import com.hipspots.model.VideoLocationDB;
import com.hipspots.sync.DBSync;
import com.hipspots.util.MultiDirectionSlidingDrawer;

public class HipspotsApplication extends Application {

	// singleton
	private static HipspotsApplication instance = null;
	private static MyMapActivity mapActivity = null;
	private static LocationsListActivity listActivity = null, favListActivity = null;
	private static MainFragmentActivity fragmentActivity = null;
	private static DetailsActivity detailsActivity = null;
	private static MultiDirectionSlidingDrawer slidingDrawer = null;
	private static DBSync dbSync = null;
	private static VideoLocationDB currentLocation = null;
	private static VideoLocationDB[] videoLocations = null;

	// Layout
	private static LinearLayout listContainer;

	// singleton SQLiteOpenHelper to avoid locking problems
	private static DBHelper jsonDBInstance = null;

	// general variables
	private static Boolean isDBLoaded = false;
	private static Boolean isDownloading = false;
	private static Boolean isSortedNearby = true;
	private static Boolean isPinPressed = false;
	private static Boolean isLanguageChanged = false;
	private static Boolean needUpdate = false;
	private static float scale = 0;
	private static char category2;

	private static double lat2;
	private static double lng2;
	private static int mapSelectedId = 0;
	private static int selectedId = 0;
	private static String distance;

	@Override
	public void onCreate() {
		super.onCreate();
		// set singleton
		instance = this;
		Log.d("WWHApplication", "singleton instance for running WWHApplication application created");

	}

	public static HipspotsApplication getInstance() {
		checkInstance();
		return instance;
	}

	private static void checkInstance() {
		if (instance == null)
			throw new IllegalStateException("Application not created yet!");
	}

	public DBHelper getJsonDBInstance() {
		if (jsonDBInstance == null) {
			jsonDBInstance = new DBHelper(instance.getApplicationContext());
		}
		return jsonDBInstance;
	}

	public static Boolean isDBLoaded() {
		return isDBLoaded;
	}

	public static void setDBLoaded(Boolean dbLoaded) {
		isDBLoaded = dbLoaded;
	}

	public static void setMapActivityInstance(MyMapActivity myMapActivity) {
		mapActivity = myMapActivity;
	}

	public static MyMapActivity getMapActivityInstance() {
		return mapActivity;
	}

	public static void setListActivityInstance(LocationsListActivity myListActivity) {
		listActivity = myListActivity;
	}

	public static LocationsListActivity getListActivityInstance() {
		return listActivity;
	}

	public static void setFavoritesListActivityInstance(LocationsListActivity myFavListActivity) {
		favListActivity = myFavListActivity;
	}

	public static LocationsListActivity getFavoritesListActivityInstance() {
		return favListActivity;
	}

	public static DetailsActivity getDetailsActivityInstance() {
		return detailsActivity;
	}

	public static void setDetailsActivityInstance(DetailsActivity myDetailsActivity) {
		detailsActivity = myDetailsActivity;
	}

	public static float getScale() {
		return scale;
	}

	public static void setScale(float scale) {
		HipspotsApplication.scale = scale;
	}

	public static void setFragmentInstance(MainFragmentActivity myFragmentActivity) {
		fragmentActivity = myFragmentActivity;
	}

	public static DBSync getSyncInstance() {
		return dbSync;

	}

	public static MainFragmentActivity getFragmentInstance() {
		return fragmentActivity;
	}

	public static char getCategory() {
		return category2;
	}

	public static void setCategory(char category) {
		category2 = category;
	}

	public static double getLatitude() {
		return lat2;
	}

	public static void setLatitude(double lat) {
		lat2 = lat;
	}

	public static double getLongitude() {
		return lng2;
	}

	public static void setLongitude(double lng) {
		lng2 = lng;
	}

	public static String getDistance() {
		return distance;
	}

	public static void setDistance(String _distance) {
		distance = _distance;
	}

	public static MultiDirectionSlidingDrawer getSlidingDrawer() {
		return slidingDrawer;
	}

	public static void setSlidingDrawer(MultiDirectionSlidingDrawer mySlidingDrawer) {
		slidingDrawer = mySlidingDrawer;
	}

	public static Boolean getIsDownloading() {
		return isDownloading;
	}

	public static void setIsDownloading(Boolean isDownloading) {
		HipspotsApplication.isDownloading = isDownloading;
	}

	public static Boolean isSortedNearby() {
		return isSortedNearby;
	}

	public static void setIsSortedNearby(Boolean isSortedNearby) {
		HipspotsApplication.isSortedNearby = isSortedNearby;
	}

	public static Boolean isPinPressed() {
		return isPinPressed;
	}

	public static void setIsPinPressed(Boolean isPinPressed) {
		HipspotsApplication.isPinPressed = isPinPressed;
	}

	public static Boolean isLanguageChanged() {
		return isLanguageChanged;
	}

	public static void setIsLanguageChanged(Boolean isLanguageChanged) {
		HipspotsApplication.isLanguageChanged = isLanguageChanged;
	}

	public static Boolean getNeedUpdate() {
		return needUpdate;
	}

	public static void setNeedUpdate(Boolean needUpdate) {
		HipspotsApplication.needUpdate = needUpdate;
	}

	public static int getSelectedId() {
		return selectedId;
	}

	public static void setSelectedId(int selectedId) {
		HipspotsApplication.selectedId = selectedId;
	}

	public static int getMapSelectedId() {
		return mapSelectedId;
	}

	public static void setMapSelectedId(int selectedId) {
		HipspotsApplication.mapSelectedId = selectedId;
	}

	public static LinearLayout getListContainer() {
		return listContainer;
	}

	public static void setListContainer(LinearLayout listContainer) {
		HipspotsApplication.listContainer = listContainer;
	}

	public static VideoLocationDB getCurrentLocation() {
		return currentLocation;
	}

	public static void setCurrentLocation(VideoLocationDB currentLocation) {
		HipspotsApplication.currentLocation = currentLocation;
	}

	public static VideoLocationDB[] getVideoLocations() {
		return videoLocations;
	}

	public static void setVideoLocations(VideoLocationDB[] videoLocations) {
		HipspotsApplication.videoLocations = videoLocations;
	}

}
