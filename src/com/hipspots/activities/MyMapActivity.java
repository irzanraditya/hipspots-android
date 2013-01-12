package com.hipspots.activities;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.hipspots.R;
import com.hipspots.app.HipspotsApplication;
import com.hipspots.database.DBHelper;
import com.hipspots.database.VideoLocationDAO;
import com.hipspots.model.VideoLocationDB;
import com.hipspots.model.VideoLocationJSON;
import com.hipspots.sync.CalculateDistanceTask;
import com.hipspots.sync.DBSync;
import com.hipspots.sync.DBUpdatesCheck;
import com.hipspots.sync.DownloadContentService;
import com.hipspots.sync.DownloadContentTask;
import com.hipspots.util.LocationHelper;
import com.hipspots.util.LocationHelper.LocationResult;
import com.hipspots.util.SettingsProvider;
import com.hipspots.util.SettingsProvider.Language;
import com.hipspots.util.Util;

public class MyMapActivity extends MapActivity implements OnClickListener {
	public static final String TAG = "GoogleMapsActivity";
	private MapView mapView;
	private LocationManager locationManager;
	Geocoder geocoder;
	Location location;
	LocationListener locationListener;
	CountDownTimer locationtimer;
	MapController mapController;
	MyLocationOverlay myLocationOverlay;
	protected HipspotsApplication application;
	private SQLiteDatabase db;
	private VideoLocationDB[] videoLocationsDB = null;
	private VideoLocationDB[] videoLocationsUpdated = null;
	private VideoLocationDAO videoLocationDAO = null;

	public static VideoLocationJSON[] videoLocations = null;
	ProgressDialog dialog;
	DBSync dBSyncAlt;
	DBSync.DownloadTask downloadTask;
	DBUpdatesCheck dBUpdateCheck;
	DBUpdatesCheck.DBUpdatesCheckTask dBUpdateCheckTask;
	char category = 'a';

	DownloadContentTask downloadContentTask;

	private final static String TAG_HISTORIC_PLACES = "HISTORIC_PLACES";
	private final static String TAG_MUSEUM = "MUSEUM";
	private final static String TAG_EAT_AND_DRINK = "EAT_AND_DRINK_AND_HOTEL";

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.my_map_activity);
		HipspotsApplication.setMapActivityInstance(this);

		if (!Util.isJsonDBDownloaded()) {
			if (isNetworkConnected()) {
				dBSyncAlt = new DBSync(this);
				downloadTask = dBSyncAlt.new DownloadTask();
				downloadTask.execute(this);
			} else
				startWIFIAlertDialog();
		} else {
			if (isNetworkConnected()) {
				HipspotsApplication.setDBLoaded(true);
				dBUpdateCheck = new DBUpdatesCheck(this);
				dBUpdateCheckTask = dBUpdateCheck.new DBUpdatesCheckTask();
				dBUpdateCheckTask.execute(this);
			}
		}

		initializeViews();
	}

	// ON_RESUME
	@Override protected void onResume() {
		super.onResume();
		// GPS->ON
		Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
		intent.putExtra("enabled", true);
		sendBroadcast(intent);
	}

	// ON_PAUSE
	@Override protected void onPause() {
		// GPS->OFF
		super.onPause();
		Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
		intent.putExtra("enabled", false);
		sendBroadcast(intent);
	}

	// INITIALIZE VIEWS
	public void initializeViews() {
		setViewText();
		// Getting getVideoLocations from DB
		getVideoLocationDB();

		// Init Processes
		initMapComponents();
		processLocation();
		initLocations();

		// Image Buttons
		final ImageButton refresh = (ImageButton) findViewById(R.id.btn_nav_info);
		refresh.setOnClickListener(this);

		final ImageButton search = (ImageButton) findViewById(R.id.btn_nav_locator);
		search.setOnClickListener(this);
	}

	// SET TEXT VIEW
	public void setViewText() {
		TextView nav_title = (TextView) findViewById(R.id.nav_title);
		Typeface fontRegular = Typeface.createFromAsset(getAssets(), "miso.otf");
		nav_title.setTypeface(fontRegular);

		int dpiDensity = getResources().getDisplayMetrics().densityDpi;
		switch (dpiDensity) {
		case DisplayMetrics.DENSITY_MEDIUM:
			nav_title.setTextSize(26);
			break;
		case DisplayMetrics.DENSITY_HIGH:
			nav_title.setTextSize(28);
			break;
		}
	}

	// WIFI ALERT
	public void startWIFIAlertDialog() {
		if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH)
			alertDialog(getString(R.string.en_dialog_network), getString(R.string.en_dialog_network_content), getString(R.string.dialog_ok));
		else
			alertDialog(getString(R.string.de_dialog_network), getString(R.string.de_dialog_network_content), getString(R.string.dialog_ok));
	}

	// LOCATION PROCESS
	public void processLocationAlt() {
		LocationResult locationResult = new LocationResult() {
			@Override
			public void gotLocation(Location location) {
				//
				double mLat = location.getLatitude();
				double mLng = location.getLongitude();

				HipspotsApplication.setLatitude(mLat);
				HipspotsApplication.setLongitude(mLng);

				CalculateDistanceTask.getInstance().execute();
				//
			}
		};
		LocationHelper myLocation = new LocationHelper();
		myLocation.getLocation(this, locationResult);
	}

	public void processLocation() {
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		if (locationManager == null) {
			String language = Locale.getDefault().getDisplayLanguage();
			if (language.equals("Deutsch")) {
				Toast.makeText(MyMapActivity.this, R.string.de_dialog_no_gps, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(MyMapActivity.this, R.string.en_dialog_no_gps, Toast.LENGTH_SHORT).show();
			}
			return;
		}
		location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location == null)
			location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		if (location != null) {
			double mLat = location.getLatitude();
			double mLng = location.getLongitude();

			HipspotsApplication.setLatitude(mLat);
			HipspotsApplication.setLongitude(mLng);

			GeoPoint point = new GeoPoint((int) (mLat * 1E6), (int) (mLng * 1E6));
			mapController.animateTo(point, new Message());
			// mapController.setCenter(point);
		}

		locationListener = new LocationListener() {
			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			}

			@Override
			public void onProviderEnabled(String arg0) {
			}

			@Override
			public void onProviderDisabled(String arg0) {
			}

			@Override
			public void onLocationChanged(Location l) {
				location = l;
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, locationListener);
				if (l.getLatitude() == 0 || l.getLongitude() == 0) {
				} else {
					double lat = l.getLatitude();
					double lng = l.getLongitude();
					HipspotsApplication.setLatitude(lat);
					HipspotsApplication.setLongitude(lng);

					CalculateDistanceTask.getInstance().execute();
				}
			}
		};
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

		locationtimer = new CountDownTimer(120000, 20000) {
			@Override
			public void onTick(long millisUntilFinished) {
				if (location != null)
					locationtimer.cancel();
			}

			@Override
			public void onFinish() {
				if (location == null) {
				}
			}
		};
		locationtimer.start();
	}

	public void getCurrentLocation(Location location) {
		double lat = location.getLatitude();
		double lng = location.getLongitude();

		GeoPoint point = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));

		mapController = mapView.getController();
		mapController.setZoom(17);
		mapController.animateTo(point, new Message());
		// mapController.setCenter(point);
	}

	public void getCurrentLocation() {
		double lat = location.getLatitude();
		double lng = location.getLongitude();

		GeoPoint point = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));

		mapController = mapView.getController();
		mapController.setZoom(17);
		mapController.animateTo(point, new Message());
		// mapController.setCenter(point);
	}

	public void zoomMap(GeoPoint point) {
		mapController = mapView.getController();
		mapController.setZoom(17);
		mapController.animateTo(point, new Message());
		// mapController.setCenter(point);
	}

	public void initLocations() {
		List<Overlay> mapOverlays = mapView.getOverlays();
		mapOverlays.clear();
		Drawable drawable = null;
		MapItemizedOverlay itemizedoverlay = null;

		myLocationOverlay = new MyLocationOverlay(this, mapView);
		myLocationOverlay.enableMyLocation();
		mapOverlays.add(myLocationOverlay);

		if (videoLocationsDB == null || videoLocationsDB.length == 0) {
			DBHelper dbhelper = HipspotsApplication.getInstance().getJsonDBInstance();
			SQLiteDatabase db2 = dbhelper.getWritableDatabase();
			VideoLocationDAO videoLocationsDAO = new VideoLocationDAO();
			videoLocationsDB = videoLocationsDAO.getVideoLocations(db2, 'a');
		}

		if (videoLocationsDB != null) {
			// Add overlays
			for (int i = 0; i < videoLocationsDB.length; i++) {

				VideoLocationDB vidLocationDB = videoLocationsDB[i];
				double lat2 = vidLocationDB.latitude;
				double lng2 = vidLocationDB.longitude;
				String name = vidLocationDB.name_de;

				String category = vidLocationDB.category;

				if (category.equals(TAG_HISTORIC_PLACES)) {
					if (vidLocationDB.is_favorited == 1) {
						drawable = this.getResources().getDrawable(R.drawable.pin_historical_fav);
					} else {
						drawable = this.getResources().getDrawable(R.drawable.pin_historical);
					}
				}

				else if (category.equals(TAG_MUSEUM)) {
					if (vidLocationDB.is_favorited == 1) {
						drawable = this.getResources().getDrawable(R.drawable.pin_museum_fav);
					} else {
						drawable = this.getResources().getDrawable(R.drawable.pin_museum);
					}
				}

				else if (category.equals(TAG_EAT_AND_DRINK)) {
					if (vidLocationDB.is_favorited == 1) {
						drawable = this.getResources().getDrawable(R.drawable.pin_food_fav);
					} else {
						drawable = this.getResources().getDrawable(R.drawable.pin_food);
					}
				}

				itemizedoverlay = new MapItemizedOverlay(drawable);

				GeoPoint point = new GeoPoint((int) (lat2 * 1E6), (int) (lng2 * 1E6));
				OverlayItem overlayitem = new OverlayItem(point, String.valueOf(vidLocationDB._id), "I'm at " + name + " !");
				itemizedoverlay.addOverlay(overlayitem);

				mapOverlays.add(itemizedoverlay);
			}

			mapView.postInvalidate();
		}
	}

	public void getVideoLocationDB() {
		DBHelper dbhelper = HipspotsApplication.getInstance().getJsonDBInstance();
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		VideoLocationDAO videoLocationDAO = new VideoLocationDAO();
		videoLocationsDB = videoLocationDAO.getVideoLocations(db, HipspotsApplication.getCategory());
	}

	public void setVideoLocationsDB(VideoLocationDB[] videoLocationsDB) {
		this.videoLocationsDB = videoLocationsDB;
	}

	public void showAddress(double lat, double lng) {

		Geocoder myLocation = new Geocoder(MyMapActivity.this, Locale.ENGLISH);
		List<Address> myList;
		try {
			myList = myLocation.getFromLocation(lat, lng, 1);
			Address add = myList.get(0);
			String addressString = add.getAddressLine(0);
			String country = add.getCountryName();
			String zipCode = add.getPostalCode();
			String city = add.getLocality();
			Toast.makeText(MyMapActivity.this, "Ihr Standort:\n" + addressString + "\n" + zipCode + "," + city + "\n" + country, Toast.LENGTH_SHORT)
					.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public MapView getMapView() {
		return this.mapView;
	}

	public void initMapComponents() {
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(false);
		mapController = mapView.getController();
		mapController.setZoom(15);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_nav_info:
			showImpressum();
			break;
		case R.id.btn_nav_locator:
			if (location != null) {
				getCurrentLocation(location);
				HipspotsApplication.getSlidingDrawer().animateDrawerHeight();
				//
				Location l = location;
				double lat = l.getLatitude();
				double lng = l.getLongitude();
				HipspotsApplication.setLatitude(lat);
				HipspotsApplication.setLongitude(lng);
				GeoPoint point = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
				mapController.animateTo(point, new Message());
				// mapController.setCenter(point);
			} else {
				Language language = SettingsProvider.getInstance().getAppLanguage();
				if (language == Language.GERMAN) {
					Toast.makeText(MyMapActivity.this, R.string.de_dialog_no_gps, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(MyMapActivity.this, R.string.en_dialog_no_gps, Toast.LENGTH_SHORT).show();
				}

			}
			HipspotsApplication.getListActivityInstance().loadListView();
			break;
		}
	}

	public void showImpressum() {

		final Dialog myDialog;
		myDialog = new Dialog(MyMapActivity.this);
		myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		myDialog.setContentView(R.layout.impressum);
		Typeface fontRegular = Typeface.createFromAsset(getAssets(), "miso.otf");
		Typeface fontLight = Typeface.createFromAsset(getAssets(), "miso-light.otf");

		TextView text_about_header = (TextView) myDialog.findViewById(R.id.txt_about_header);
		text_about_header.setTypeface(fontRegular);

		TextView text_about = (TextView) myDialog.findViewById(R.id.txt_about);
		text_about.setTypeface(fontLight);

		TextView text_company = (TextView) myDialog.findViewById(R.id.txt_company_info);
		text_company.setTypeface(fontLight);

		TextView text_info_language = (TextView) myDialog.findViewById(R.id.txt_language_header);
		text_info_language.setTypeface(fontRegular);

		TextView text_info_contact = (TextView) myDialog.findViewById(R.id.txt_contact_header);
		text_info_contact.setTypeface(fontRegular);
		TextView text_contact = (TextView) myDialog.findViewById(R.id.txt_contact);
		text_contact.setTypeface(fontLight);

		Button btn_call = (Button) myDialog.findViewById(R.id.btn_info_call);
		Button btn_email = (Button) myDialog.findViewById(R.id.btn_info_email);

		btn_call.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				callHotline();
			}
		});

		btn_email.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendEmail();
			}
		});

		if (SettingsProvider.getInstance().getAppLanguage() == Language.GERMAN) {
			text_about_header.setText(getString(R.string.info_title).toUpperCase());
			text_info_language.setText(getString(R.string.info_language).toUpperCase());
			text_info_contact.setText(getString(R.string.info_contact).toUpperCase());
			text_about.setText(getString(R.string.de_about));
			text_company.setText(getString(R.string.de_company_info));
			text_contact.setText((getString(R.string.info_contact_details)));
			btn_call.setBackgroundResource(R.drawable.btn_info_call);
		} else {
			text_about_header.setText(getString(R.string.en_info_title).toUpperCase());
			text_info_language.setText(getString(R.string.en_info_language).toUpperCase());
			text_info_contact.setText(getString(R.string.en_info_contact).toUpperCase());
			text_about.setText(getString(R.string.en_about));
			text_company.setText(getString(R.string.en_company_info));
			text_contact.setText((getString(R.string.en_info_contact_details)));
			btn_call.setBackgroundResource(R.drawable.btn_info_call_en);
			MyMapActivity.this.putBooleanInPreferences(true, "isChecked");
		}

		CheckBox cb_language_selector = (CheckBox) myDialog.findViewById(R.id.cb_language_selector);
		final boolean isChecked = getBooleanFromPreferences("isChecked");
		Log.d("start", "" + isChecked);
		//
		db = HipspotsApplication.getInstance().getJsonDBInstance().getWritableDatabase();
		videoLocationDAO = new VideoLocationDAO();
		videoLocationsUpdated = videoLocationDAO.getNotUpdatedContentVideoLocations(db, SettingsProvider.getInstance().getAppLanguage());
		//
		cb_language_selector.setChecked(isChecked);
		cb_language_selector.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton view, boolean isChecked) {
				if (view.isChecked()) {
					SettingsProvider.getInstance().setAppLanguageGerman(false);
					HipspotsApplication.setIsLanguageChanged(true);
					HipspotsApplication.getListActivityInstance().setUIEnglish();
					HipspotsApplication.getFragmentInstance().animateStart();
					HipspotsApplication.getListActivityInstance().loadListView();
					MyMapActivity.this.putBooleanInPreferences(isChecked, "isChecked");
					myDialog.dismiss();
					showImpressum();
					if (videoLocationsUpdated != null && videoLocationsUpdated.length != 0)
						retrieveDownloadState("english");
					HipspotsApplication.setIsLanguageChanged(false);
				} else {
					SettingsProvider.getInstance().setAppLanguageGerman(true);
					HipspotsApplication.setIsLanguageChanged(true);
					HipspotsApplication.getListActivityInstance().setUIGerman();
					HipspotsApplication.getFragmentInstance().animateStart();
					HipspotsApplication.getListActivityInstance().loadListView();
					MyMapActivity.this.putBooleanInPreferences(isChecked, "isChecked");
					myDialog.dismiss();
					showImpressum();
					if (videoLocationsUpdated != null && videoLocationsUpdated.length != 0)
						retrieveDownloadState("german");
					HipspotsApplication.setIsLanguageChanged(false);
				}
			}
		});
		myDialog.show();
	}

	public void retrieveDownloadState(String language) {
		boolean download = HipspotsApplication.getMapActivityInstance().getBooleanFromPreferences(language);
		if (download == false) {
			if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH)
				downloadAlertDialog(getString(R.string.en_dialog_downloadTitle), getString(R.string.en_dialog_downloadContent),
						getString(R.string.en_dialog_yes), getString(R.string.en_dialog_no));
			else
				downloadAlertDialog(getString(R.string.de_dialog_downloadTitle), getString(R.string.de_dialog_downloadContent),
						getString(R.string.de_dialog_yes), getString(R.string.de_dialog_no));
		}
	}

	public void alertDialog(String downloadTitle, String downloadContent, String no) {
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage(downloadContent).setCancelable(true).setNegativeButton(no, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				// finish();
			}
		});
		AlertDialog alert = alt_bld.create();
		alert.setTitle(downloadTitle);
		alert.show();
	}

	public void downloadAlertDialog(String downloadTitle, String askDownload, String yes, String no) {
		Log.d("ODA", "Downloading Alert");
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage(askDownload).setCancelable(true).setPositiveButton(yes, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int id) {
				if (HipspotsApplication.getIsDownloading() == false) {
					if (isNetworkConnected() == true) {
						if (isWifiConnected() == true) {
							if (SettingsProvider.getInstance().getAppLanguage() == Language.GERMAN)
								wifiFinalDialog(getString(R.string.de_dialog_wifi_title), getString(R.string.de_dialog_wifi),
										getString(R.string.de_dialog_yes), getString(R.string.de_dialog_no));
							else
								wifiFinalDialog(getString(R.string.en_dialog_wifi_title), getString(R.string.en_dialog_wifi),
										getString(R.string.en_dialog_yes), getString(R.string.en_dialog_no));
						} else {
							if (SettingsProvider.getInstance().getAppLanguage() == Language.GERMAN)
								wifiFinalDialog(getString(R.string.de_dialog_wifi_title), getString(R.string.de_dialog_no_wifi),
										getString(R.string.de_dialog_yes), getString(R.string.de_dialog_no));
							else
								wifiFinalDialog(getString(R.string.en_dialog_wifi_title), getString(R.string.en_dialog_no_wifi),
										getString(R.string.en_dialog_yes), getString(R.string.en_dialog_no));
						}
					} else {
						if (SettingsProvider.getInstance().getAppLanguage() == Language.GERMAN)
							HipspotsApplication.getFragmentInstance().alertDialog(getString(R.string.de_dialog_network),
									getString(R.string.de_dialog_network_content), getString(R.string.dialog_ok));
						else
							HipspotsApplication.getFragmentInstance().alertDialog(getString(R.string.en_dialog_network),
									getString(R.string.en_dialog_network_content), getString(R.string.dialog_ok));
					}
				} else {
					if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH)
						noDownloadDialog(getString(R.string.en_dialog_download_progress_title),
								getString(R.string.en_dialog_download_progress_content), getString(R.string.dialog_ok));
					else
						noDownloadDialog(getString(R.string.de_dialog_download_progress_title),
								getString(R.string.de_dialog_download_progress_content), getString(R.string.dialog_ok));
				}
			}
		}).setNegativeButton(no, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = alt_bld.create();
		alert.setTitle(downloadTitle);
		alert.show();
	}

	public boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			// There are no active networks.
			return false;
		} else
			return true;
	}

	public boolean isWifiConnected() {
		ConnectivityManager conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mifi = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (mifi.isConnected()) {
			// There are no active networks.
			return true;
		} else
			return false;

	}

	public void noDownloadDialog(String downloadTitle, String askDownload, String no) {
		Log.d("ODA", "Downloading Alert");
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage(askDownload).setCancelable(true).setNegativeButton(no, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = alt_bld.create();
		alert.setTitle(downloadTitle);
		alert.show();
	}

	public void wifiFinalDialog(String downloadTitle, String message, String yes, String no) {
		Log.d("ODA", downloadTitle);
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage(message).setCancelable(true).setPositiveButton(yes, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int id) {
				Log.d(TAG, "Start Download");
				downloadContent();
			}
		}).setNegativeButton(no, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = alt_bld.create();
		alert.setTitle(downloadTitle);
		alert.show();
	}

	protected void downloadContent() {
		Log.d(TAG, "Start Download, API Level " + Build.VERSION.SDK_INT);

		SettingsProvider.getInstance().setDownloadStarted(true);

		Intent intent = new Intent(this, DownloadContentService.class);
		startService(intent);
	}

	protected void callHotline() {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		String phonenumber = getString(R.string.info_contact_phone);
		callIntent.setData(Uri.parse("tel:" + phonenumber));
		startActivity(callIntent);
	}

	protected void sendEmail() {
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		String email = getString(R.string.info_contact_email);
		sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { email });
		sendIntent.setType("message/rfc822");
		startActivity(Intent.createChooser(sendIntent, " "));
	}

	public void putBooleanInPreferences(boolean isChecked, String key) {
		SharedPreferences sharedPreferences = this.getPreferences(MyMapActivity.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(key, isChecked);
		editor.commit();
	}

	public boolean getBooleanFromPreferences(String key) {
		SharedPreferences sharedPreferences = this.getPreferences(MyMapActivity.MODE_PRIVATE);
		Boolean isChecked = sharedPreferences.getBoolean(key, false);
		return isChecked;
	}
}