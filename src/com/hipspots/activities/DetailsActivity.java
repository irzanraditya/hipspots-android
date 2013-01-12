package com.hipspots.activities;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.hipspots.R;
import com.hipspots.app.HipspotsApplication;
import com.hipspots.database.DBHelper;
import com.hipspots.database.TransportationDAO;
import com.hipspots.database.VideoLocationDAO;
import com.hipspots.model.TransportationDB;
import com.hipspots.model.VideoLocationDB;
import com.hipspots.util.AudioLoader;
import com.hipspots.util.LoadImageTask;
import com.hipspots.util.SettingsProvider;
import com.hipspots.util.SettingsProvider.Language;

public class DetailsActivity extends Activity implements OnClickListener, OnCompletionListener, SeekBar.OnSeekBarChangeListener {

	public SQLiteDatabase sqd;
	VideoView myVideoView;
	String TAG = "DETAIL";

	Bundle extras = null;
	private char category;

	private String monday, tuesday, wednesday, thursday, friday, saturday, sunday;

	private boolean isChecked;

	LocationManager locationManager;
	Geocoder geocoder;
	Location location;
	LocationListener locationListener;
	CountDownTimer locationtimer;

	ImageButton nav_back;
	ImageButton btn_audio;
	ImageButton btn_route;
	ImageButton btn_website;

	CheckBox cb_fav;

	TextView txt_location_name, txt_distance, txt_info_header, txt_info_title, txt_description, txt_opening_hours_header;

	TextView txt_opening_mo, txt_opening_tue, txt_opening_wed, txt_opening_thu, txt_opening_fri, txt_opening_sa, txt_opening_so;

	TextView txt_opening_mo_time, txt_opening_tue_time, txt_opening_wed_time, txt_opening_thu_time, txt_opening_fri_time, txt_opening_sa_time,
			txt_opening_so_time;

	TextView txt_info_transportation, txt_info_contact, txt_sbahn, txt_ubahn, txt_bus, txt_tram, txt_sbahn_info, txt_ubahn_info, txt_bus_info,
			txt_tram_info, txt_address, txt_zipcode, txt_phone, txt_email, txt_website, txt_current_time, txt_total_time;

	TableLayout tl;
	TableLayout tl_opening_hours;

	LinearLayout layout_phone, layout_website, layout_mail, timer_display;
	TableRow tableRow;
	ImageView img_trans, img_info_opening_line, main_image, playButton;
	TextView txt_line, txt_station;

	VideoLocationDB videoLocation;
	private MediaPlayer audioplayer;
	private SeekBar songProgressBar;
	private AudioLoader utils;
	private final Handler mHandler = new Handler();
	private final Handler mHandler2 = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail);
		HipspotsApplication.setDetailsActivityInstance(this);

		// BUTTON
		nav_back = (ImageButton) findViewById(R.id.btn_nav_back);
		nav_back.setOnClickListener(this);

		// SETUP FONT
		Typeface fontRegular = Typeface.createFromAsset(getAssets(), "miso.otf");
		Typeface fontLight = Typeface.createFromAsset(getAssets(), "miso-light.otf");

		// Locations Name
		txt_location_name = (TextView) findViewById(R.id.txt_location_name);
		txt_location_name.setTypeface(fontRegular);

		// Locations Distance
		txt_distance = (TextView) findViewById(R.id.txt_location_distance);
		txt_distance.setTypeface(fontRegular);

		// INFO HEADER
		txt_info_header = (TextView) findViewById(R.id.txt_info_header);
		txt_info_header.setTypeface(fontRegular);

		// INFO TITLE
		txt_info_title = (TextView) findViewById(R.id.txt_info_title);
		txt_info_title.setTypeface(fontRegular);

		// DESCRIPTION
		txt_description = (TextView) findViewById(R.id.txt_project_description);
		txt_description.setTypeface(fontLight);

		// OPENING HOURS
		txt_opening_hours_header = (TextView) findViewById(R.id.txt_info_opening_hours);
		txt_opening_hours_header.setTypeface(fontRegular);

		img_info_opening_line = (ImageView) findViewById(R.id.img_info_opening_line);
		tl_opening_hours = (TableLayout) findViewById(R.id.tl_opening_hours);

		txt_opening_mo = (TextView) findViewById(R.id.txt_mo);
		txt_opening_tue = (TextView) findViewById(R.id.txt_tue);
		txt_opening_wed = (TextView) findViewById(R.id.txt_wed);
		txt_opening_thu = (TextView) findViewById(R.id.txt_thu);
		txt_opening_fri = (TextView) findViewById(R.id.txt_fri);
		txt_opening_sa = (TextView) findViewById(R.id.txt_sa);
		txt_opening_so = (TextView) findViewById(R.id.txt_so);

		txt_opening_mo.setTypeface(fontRegular);
		txt_opening_tue.setTypeface(fontRegular);
		txt_opening_wed.setTypeface(fontRegular);
		txt_opening_thu.setTypeface(fontRegular);
		txt_opening_fri.setTypeface(fontRegular);
		txt_opening_sa.setTypeface(fontRegular);
		txt_opening_so.setTypeface(fontRegular);

		txt_opening_mo_time = (TextView) findViewById(R.id.txt_mo_time);
		txt_opening_tue_time = (TextView) findViewById(R.id.txt_tue_time);
		txt_opening_wed_time = (TextView) findViewById(R.id.txt_wed_time);
		txt_opening_thu_time = (TextView) findViewById(R.id.txt_thu_time);
		txt_opening_fri_time = (TextView) findViewById(R.id.txt_fri_time);
		txt_opening_sa_time = (TextView) findViewById(R.id.txt_sa_time);
		txt_opening_so_time = (TextView) findViewById(R.id.txt_so_time);

		txt_opening_mo_time.setTypeface(fontLight);
		txt_opening_tue_time.setTypeface(fontLight);
		txt_opening_wed_time.setTypeface(fontLight);
		txt_opening_thu_time.setTypeface(fontLight);
		txt_opening_fri_time.setTypeface(fontLight);
		txt_opening_sa_time.setTypeface(fontLight);
		txt_opening_so_time.setTypeface(fontLight);

		// TRANSPORTATION
		txt_info_transportation = (TextView) findViewById(R.id.txt_info_transportation);
		txt_info_transportation.setTypeface(fontRegular);

		// CONTACT
		txt_info_contact = (TextView) findViewById(R.id.txt_info_contact);
		txt_info_contact.setTypeface(fontRegular);

		txt_address = (TextView) findViewById(R.id.txt_address);
		txt_zipcode = (TextView) findViewById(R.id.txt_zipcode);

		layout_phone = (LinearLayout) findViewById(R.id.layout_phone);
		layout_phone.setOnClickListener(this);
		txt_phone = (TextView) findViewById(R.id.txt_phone);

		layout_mail = (LinearLayout) findViewById(R.id.layout_mail);
		layout_mail.setOnClickListener(this);
		txt_email = (TextView) findViewById(R.id.txt_email);

		layout_website = (LinearLayout) findViewById(R.id.layout_website);
		layout_website.setOnClickListener(this);
		txt_website = (TextView) findViewById(R.id.txt_website);

		txt_address.setTypeface(fontLight);
		txt_zipcode.setTypeface(fontLight);
		txt_phone.setTypeface(fontLight);
		txt_email.setTypeface(fontLight);
		txt_website.setTypeface(fontLight);

		// AUDIO PLAYER
		btn_audio = (ImageButton) findViewById(R.id.btn_tool_audio);
		btn_audio.setOnClickListener(this);
		txt_current_time = (TextView) findViewById(R.id.txt_current_time);
		txt_total_time = (TextView) findViewById(R.id.txt_total_time);
		timer_display = (LinearLayout) findViewById(R.id.timerDisplay);

		txt_current_time.setTypeface(fontLight);
		txt_total_time.setTypeface(fontLight);

		songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
		utils = new AudioLoader();
		songProgressBar.setOnSeekBarChangeListener(this);

		cb_fav = (CheckBox) findViewById(R.id.cb_tool_fav);
		cb_fav.setOnClickListener(this);

		btn_route = (ImageButton) findViewById(R.id.btn_tool_route);
		btn_route.setOnClickListener(this);

		btn_website = (ImageButton) findViewById(R.id.btn_tool_website);
		btn_website.setOnClickListener(this);

		// IMAGE
		main_image = (ImageView) findViewById(R.id.vidlocViewPager);

		// VIDEO
		playButton = (ImageView) findViewById(R.id.vidlocViewPagerPlay);

	}

	@Override
	public void onResume() {
		Bundle extras = getIntent().getExtras();

		videoLocation = HipspotsApplication.getCurrentLocation();

		// POSITION
		txt_distance.setText(HipspotsApplication.getDistance());

		// CATEGORY
		category = extras.getChar("category");

		// NAME
		if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH)
			txt_location_name.setText("  " + videoLocation.name_en.toUpperCase(Locale.ENGLISH));
		else
			txt_location_name.setText("  " + videoLocation.name_de.toUpperCase(Locale.GERMAN));

		// TITLE
		if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH)
			txt_info_title.setText(videoLocation.name_en);
		else
			txt_info_title.setText(videoLocation.name_de);

		// TEXT
		if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH)
			txt_description.setText(videoLocation.text_en);
		else
			txt_description.setText(videoLocation.text_de);

		// CONTACT
		if (videoLocation.phone.equals(null) || videoLocation.phone.trim().equals(""))
			layout_phone.setVisibility(View.GONE);
		if (videoLocation.email.equals(null) || videoLocation.email.trim().equals(""))
			layout_mail.setVisibility(View.GONE);
		if (videoLocation.website.equals(null) || videoLocation.website.trim().equals(""))
			layout_website.setVisibility(View.GONE);

		txt_address.setText(videoLocation.street + " " + videoLocation.street_number);
		txt_zipcode.setText(videoLocation.zip_code + " " + "Berlin");
		txt_phone.setText(videoLocation.phone);
		txt_email.setText(videoLocation.email);
		txt_website.setText(videoLocation.website);

		// OPEN HOURS
		monday = videoLocation.attr_opening_hours.getMonday();
		tuesday = videoLocation.attr_opening_hours.getTuesday();
		wednesday = videoLocation.attr_opening_hours.getWednesday();
		thursday = videoLocation.attr_opening_hours.getThursday();
		friday = videoLocation.attr_opening_hours.getFriday();
		saturday = videoLocation.attr_opening_hours.getSaturday();
		sunday = videoLocation.attr_opening_hours.getSunday();

		// LANGUAGE
		setLanguage();

		// AUDIO
		// Audio file doesn't exist
		if (videoLocation.audio_url_en == null || videoLocation.audio_url_de == null) {
			btn_audio.setVisibility(View.GONE);
		}
		// FAVORITES
		// cb_fav.setChecked(getBooleanFromPreferences(videoLocation.name_de));
		// cb_fav.setSelected(getBooleanFromPreferences(videoLocation.name_de));

		// WEBITE
		if (videoLocation.website == null || videoLocation.website.trim().equals(""))
			btn_website.setVisibility(View.INVISIBLE);
		else
			btn_website.setVisibility(View.VISIBLE);

		// IMAGE
		if (videoLocation.thumbnail_path == null || videoLocation.thumbnail_path.trim().equals("")){
			// Use Background Image
		} else {
			Bitmap result = LoadImageTask.loadImageSD(videoLocation.thumbnail_path, 500);
			main_image.setImageBitmap(result);
		}
		new LoadImageTask(main_image, false).execute(videoLocation);

		// VIDEO
		if ((videoLocation.video_url_de == null && (SettingsProvider.getInstance().getAppLanguage() == Language.GERMAN))
				|| (videoLocation.video_url_en == null && (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH))) {
			playButton.setVisibility(ImageView.GONE);
		} else {
			playButton.setVisibility(ImageView.VISIBLE);

			playButton.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					stopAudio();
					Intent listIntent = new Intent(getApplicationContext(), VideoPlayer.class);

					listIntent.putExtra("video_url", getVideo(videoLocation));
					startActivity(listIntent);
				}
			});
		}

		// LOCATION LISTENER
		if (locationListener != null) {
			locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f, locationListener);
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		if (audioplayer != null) {
			if (audioplayer.isPlaying())
				audioplayer.stop();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindDrawables(findViewById(R.id.RootView));
		System.gc();
	}

	public void stopAudio() {
		timer_display.setVisibility(View.INVISIBLE);
		if (audioplayer != null && audioplayer.isPlaying())
			audioplayer.stop();
		audioplayer = null;
	}

	public void playAudio() {
		if (audioplayer != null) {
			if (audioplayer.isPlaying()) {
				audioplayer.pause();
				timer_display.setVisibility(View.INVISIBLE);
			} else {
				audioplayer.start();
				songProgressBar.setProgress(0);
				updateProgressBar();
				timer_display.setVisibility(View.VISIBLE);
			}
		} else if (videoLocation.audio_url_en != null || videoLocation.audio_url_de != null) { // TODO: check for language specific audio in case one
																								// exists and the other doesn't
			audioplayer = MediaPlayer.create(this, Uri.parse(getAudio(videoLocation)));

			if (audioplayer != null) {
				audioplayer.start();
				// set Progress bar values
				songProgressBar.setProgress(0);
				songProgressBar.setMax(100);
				timer_display.setVisibility(View.VISIBLE);
				updateProgressBar();
			}
			// If Audio doesn't exist
		} else {
			String no_audio = "";
			if (SettingsProvider.getInstance().getAppLanguage() == Language.GERMAN)
				no_audio = getString(R.string.de_no_audio);
			else
				no_audio = getString(R.string.en_no_audio);
			Toast.makeText(DetailsActivity.this, no_audio, Toast.LENGTH_SHORT).show();
		}
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

	/**
	 * Update timer on seekbar
	 * */
	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}

	/**
	 * Background Runnable thread
	 * */

	private final Runnable mUpdateTimeTask = new Runnable() {
		@Override public void run() {
			if (audioplayer != null && audioplayer.isPlaying()) {
				long totalDuration = audioplayer.getDuration();
				long currentDuration = audioplayer.getCurrentPosition();
				if (totalDuration >= 13000000)
					totalDuration = 0;

				// Displaying Total Duration time
				txt_total_time.setText(" / " + utils.milliSecondsToTimer(totalDuration));
				// Displaying time completed playing
				txt_current_time.setText("" + utils.milliSecondsToTimer(currentDuration));

				// Updating progress bar
				int progress = (utils.getProgressPercentage(currentDuration, totalDuration));
				songProgressBar.setProgress(progress);

				// Running this thread after 100 milliseconds
				mHandler.postDelayed(this, 100);
			}
		}
	};

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

	}

	/**
	 * When user starts moving the progress handler
	 * */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// remove message Handler from updating progress bar
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	/**
	 * When user stops moving the progress handler
	 * */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mHandler.removeCallbacks(mUpdateTimeTask);
		int totalDuration = audioplayer.getDuration();
		int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

		// forward or backward to certain seconds
		audioplayer.seekTo(currentPosition);

		// update timer progress again
		updateProgressBar();
	}

	public void setLanguage() {
		// (wednesday.equals("0-24") && monday.equals("0-24") && tuesday.equals("0-24") && thursday.equals("0-24") && friday.equals("0-24")
		// && saturday.equals("0-24") && sunday.equals("0-24"))
		// ||
		if ((wednesday == null && monday == null && tuesday == null && thursday == null && friday == null && saturday == null && sunday == null)
				|| (wednesday.equals("") && monday.equals("") && tuesday.equals("") && thursday.equals("") && friday.equals("")
						&& saturday.equals("") && sunday.equals(""))) {
			txt_opening_hours_header.setVisibility(View.GONE);
			img_info_opening_line.setVisibility(View.GONE);
			tl_opening_hours.setVisibility(View.GONE);
		} else {
			txt_opening_hours_header.setVisibility(View.VISIBLE);
			img_info_opening_line.setVisibility(View.VISIBLE);
			tl_opening_hours.setVisibility(View.VISIBLE);
		}

		if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH) {
			txt_info_header.setText(getString(R.string.en_detail_header).toUpperCase(Locale.ENGLISH));
			txt_opening_hours_header.setText(getString(R.string.en_detail_opening_hours).toUpperCase(Locale.ENGLISH));
			txt_opening_mo.setText(getString(R.string.en_detail_mon));
			txt_opening_tue.setText(getString(R.string.en_detail_tue));
			txt_opening_wed.setText(getString(R.string.en_detail_wed));
			txt_opening_thu.setText(getString(R.string.en_detail_thu));
			txt_opening_fri.setText(getString(R.string.en_detail_fri));
			txt_opening_sa.setText(getString(R.string.en_detail_sa));
			txt_opening_so.setText(getString(R.string.en_detail_so));
			txt_info_transportation.setText(getString(R.string.en_detail_transportation_access).toUpperCase(Locale.ENGLISH));
			txt_info_contact.setText(getString(R.string.en_detail_contact).toUpperCase(Locale.ENGLISH));
			// txt_description.setText(Html.fromHtml(text_en));
			txt_description.setText((videoLocation.text_en));
		} else {
			txt_info_header.setText(getString(R.string.de_detail_header).toUpperCase(Locale.GERMAN));
			txt_opening_hours_header.setText(getString(R.string.de_detail_opening_hours).toUpperCase(Locale.GERMAN));
			txt_opening_mo.setText(getString(R.string.de_detail_mon));
			txt_opening_tue.setText(getString(R.string.de_detail_tue));
			txt_opening_wed.setText(getString(R.string.de_detail_wed));
			txt_opening_thu.setText(getString(R.string.de_detail_thu));
			txt_opening_fri.setText(getString(R.string.de_detail_fri));
			txt_opening_sa.setText(getString(R.string.de_detail_sa));
			txt_opening_so.setText(getString(R.string.de_detail_so));
			txt_info_transportation.setText(getString(R.string.de_detail_transportation_access).toUpperCase(Locale.GERMAN));
			txt_info_contact.setText(getString(R.string.de_detail_contact).toUpperCase(Locale.GERMAN));
			// txt_description.setText(Html.fromHtml(text_de));
			txt_description.setText((videoLocation.text_de));
		}

		// DETAILS
		String closed;
		if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH) {
			closed = getString(R.string.en_detail_no);
		} else {
			closed = getString(R.string.de_detail_no);
		}

		// OPENING HOURS TIME
		if (monday == null || monday.trim().equals(""))
			monday = closed;
		if (tuesday == null || tuesday.trim().equals(""))
			tuesday = closed;
		if (wednesday == null || wednesday.trim().equals(""))
			wednesday = closed;
		if (thursday == null || thursday.trim().equals(""))
			thursday = closed;
		if (friday == null || friday.trim().equals(""))
			friday = closed;
		if (saturday == null || saturday.trim().equals(""))
			saturday = closed;
		if (sunday == null || sunday.trim().equals(""))
			sunday = closed;

		txt_opening_mo_time.setText(monday);
		txt_opening_tue_time.setText(tuesday);
		txt_opening_wed_time.setText(wednesday);
		txt_opening_thu_time.setText(thursday);
		txt_opening_fri_time.setText(friday);
		txt_opening_sa_time.setText(saturday);
		txt_opening_so_time.setText(sunday);

		// TRANSPORT
		setTransportationsTask();
	}

	private void unbindDrawables(View view) {
		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}
		if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			((ViewGroup) view).removeAllViews();
		}
	}

	Drawable drawable_from_url(String url, String src_name) throws java.net.MalformedURLException, java.io.IOException {
		return Drawable.createFromStream(((java.io.InputStream) new java.net.URL(url).getContent()), src_name);
	}

	public double roundDown(double d) {
		return (long) (d * 1e8) / 1e8;
	}

	// TRANSPORT
	public void setTransportationsTask() {
		mHandler2.post(new Runnable() {
			@Override
			public void run() {
				setTransportations();
			}
		});
	}

	public void setTransportations() {

		SQLiteDatabase db = HipspotsApplication.getInstance().getJsonDBInstance().getWritableDatabase();
		TransportationDAO transportationDAO = new TransportationDAO();
		TransportationDB[] transportations = transportationDAO.getTransportationArray(db, videoLocation);

		String[] types = new String[transportations.length];
		String[] lines = new String[transportations.length];
		String[] stations = new String[transportations.length];

		for (int i = 0; i < transportations.length; i++) {
			types[i] = transportations[i].type;
			lines[i] = transportations[i].line;
			stations[i] = transportations[i].station;
		}

		tl = (TableLayout) findViewById(R.id.test_transport);
		tl.removeAllViews();

		for (int i = 0; i < transportations.length; i++) {

			tableRow = new TableRow(this);
			img_trans = new ImageView(this);
			txt_line = new TextView(this);
			txt_station = new TextView(this);

			if (types[i] == null)
				;
			else if (types[i].equals("BUS"))
				img_trans.setBackgroundResource(R.drawable.icon_bus);
			else if (types[i].equals("TRAM"))
				img_trans.setBackgroundResource(R.drawable.icon_tram);
			else if (types[i].equals("SBAHN"))
				img_trans.setBackgroundResource(R.drawable.icon_sbahn);
			else if (types[i].equals("UBAHN"))
				img_trans.setBackgroundResource(R.drawable.icon_ubahn);

			Typeface fontRegular = Typeface.createFromAsset(getAssets(), "miso.otf");
			Typeface fontLight = Typeface.createFromAsset(getAssets(), "miso-light.otf");

			if (lines[i] != null)
				txt_line.setText("      " + lines[i] + " ");
			txt_line.setTypeface(fontRegular);
			txt_line.setTextColor(getResources().getColor(R.color.black));
			txt_line.setTextSize(22);

			if (stations[i] != null)
				txt_station.setText(" " + stations[i] + " ");
			txt_station.setTypeface(fontLight);
			txt_station.setTextColor(getResources().getColor(R.color.black));
			txt_station.setTextSize(22);

			tableRow.addView(img_trans);
			tableRow.addView(txt_line);
			tableRow.addView(txt_station);
			tl.addView(tableRow);
		}
		// View vg = findViewById(R.id.RootView);
		tl.invalidate();
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

	public void putBooleanInPreferences(boolean isChecked, String name) {
		DBHelper dbhelper = HipspotsApplication.getInstance().getJsonDBInstance();
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		VideoLocationDAO videoLocationsDAO = new VideoLocationDAO();
		Integer id = videoLocationsDAO.getIdFromName(db, name);
		videoLocationsDAO.toogleFavorite(db, id);
	}

	public boolean getBooleanFromPreferences(String name) {
		DBHelper dbhelper = HipspotsApplication.getInstance().getJsonDBInstance();
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		VideoLocationDAO videoLocationsDAO = new VideoLocationDAO();
		Integer id = videoLocationsDAO.getIdFromName(db, name);
		Boolean isFavorited = videoLocationsDAO.isFavorited(db, id);
		return isFavorited;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			HipspotsApplication.getMapActivityInstance().getVideoLocationDB();
			HipspotsApplication.getMapActivityInstance().initLocations();
			if (category == 'f') {
				if (HipspotsApplication.getFavoritesListActivityInstance() != null)
					HipspotsApplication.getFavoritesListActivityInstance().loadListView();
			}
			finish();
			// moveTaskToBack(true);
			if (audioplayer != null) {
				if (audioplayer.isPlaying())
					audioplayer.stop();
			}
			return true;
		}
		// Intent intentList = new Intent(WWHApplication.getDetailsActivityInstance(), MainFragmentActivity.class);
		// startActivity(intentList);
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btn_tool_audio:
			playAudio();
			break;
		case R.id.btn_nav_back:
			HipspotsApplication.getMapActivityInstance().getVideoLocationDB();
			HipspotsApplication.getMapActivityInstance().initLocations();
			if (category == 'f') {
				if (HipspotsApplication.getFavoritesListActivityInstance() != null)
					HipspotsApplication.getFavoritesListActivityInstance().loadListView();
			}
			finish();
			// moveTaskToBack(true);
			if (audioplayer != null) {
				if (audioplayer.isPlaying())
					audioplayer.stop();
			}
			// Intent intentList = new Intent(WWHApplication.getDetailsActivityInstance(), MainFragmentActivity.class);
			// startActivity(intentList);
			break;
		case R.id.btn_tool_route:
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr="
					+ HipspotsApplication.getLatitude() + "," + HipspotsApplication.getLongitude() + "&daddr=" + videoLocation.latitude + ","
					+ videoLocation.longitude));
			startActivity(intent);
			break;
		case R.id.btn_tool_website:
			openWebsite();
			break;
		case R.id.cb_tool_fav:
			category = HipspotsApplication.getCategory();
			DetailsActivity.this.putBooleanInPreferences(isChecked, videoLocation.name_de);
			HipspotsApplication.getMapActivityInstance().getVideoLocationDB();
			HipspotsApplication.getMapActivityInstance().initLocations();
			if (category == 'f') {
				if (HipspotsApplication.getFavoritesListActivityInstance() != null)
					HipspotsApplication.getFavoritesListActivityInstance().loadListView();
			}
			break;
		case R.id.layout_phone:
			callLocation();
			break;
		case R.id.layout_mail:
			sendEmail();
			break;
		case R.id.layout_website:
			openWebsite();
			break;
		}
	}

	protected void openWebsite() {
		String url = videoLocation.website;
		if (url.equals("")) {
			if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH) {
				Toast.makeText(DetailsActivity.this, getString(R.string.en_no_website), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(DetailsActivity.this, getString(R.string.de_no_website), Toast.LENGTH_SHORT).show();
			}
		} else {
			if (!url.contains("http"))
				url = "http://" + videoLocation.website;
			Uri uriUrl = Uri.parse(url);
			Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
			startActivity(launchBrowser);
		}
	}

	protected void callLocation() {
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		String phonenumber = videoLocation.phone;
		if (phonenumber.equals("")) {
			if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH) {
				Toast.makeText(DetailsActivity.this, getString(R.string.en_no_phone), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(DetailsActivity.this, getString(R.string.de_no_phone), Toast.LENGTH_SHORT).show();
			}
		} else {
			callIntent.setData(Uri.parse("tel:" + phonenumber));
			startActivity(callIntent);
		}
	}

	protected void sendEmail() {
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		if (videoLocation.email.equals("")) {
			if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH) {
				Toast.makeText(DetailsActivity.this, getString(R.string.en_no_email), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(DetailsActivity.this, getString(R.string.de_no_email), Toast.LENGTH_SHORT).show();
			}
		} else {
			sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { videoLocation.email });
			sendIntent.setType("message/rfc822");
			startActivity(Intent.createChooser(sendIntent, " "));
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// Displaying Total Duration time
		txt_total_time.setText("");
		txt_current_time.setText("");
	}

	// GET VIDEO PATH
	private String getVideo(VideoLocationDB videoLocation) {
		String videoPath = "";

		if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH)
			if (videoLocation.video_path_en != null && !videoLocation.video_path_en.trim().equals(""))
				videoPath = videoLocation.video_path_en;
			else
				videoPath = videoLocation.video_url_en.replace("https:", "http:");
		else {
			if (videoLocation.video_path_de != null && !videoLocation.video_path_de.trim().equals(""))
				videoPath = videoLocation.video_path_de;
			else
				videoPath = videoLocation.video_url_de.replace("https:", "http:");
		}
		return videoPath;
	}

	// GET AUDIO PATH
	private String getAudio(VideoLocationDB videoLocation) {
		String audioPath = "";

		if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH)
			if (videoLocation.audio_path_en != null && !videoLocation.audio_path_en.trim().equals(""))
				audioPath = videoLocation.audio_path_en;
			else
				audioPath = videoLocation.audio_url_en.replace("https:", "http:");
		else {
			if (videoLocation.audio_path_de != null && !videoLocation.audio_path_de.trim().equals(""))
				audioPath = videoLocation.audio_path_de;
			else
				audioPath = videoLocation.audio_url_de.replace("https:", "http:");
		}
		return audioPath;
	}
}
