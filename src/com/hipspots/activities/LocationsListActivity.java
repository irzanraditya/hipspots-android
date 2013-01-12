package com.hipspots.activities;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.hipspots.R;
import com.hipspots.app.HipspotsApplication;
import com.hipspots.database.DBHelper;
import com.hipspots.database.VideoLocationDAO;
import com.hipspots.model.VideoLocationDB;
import com.hipspots.util.LoadThumbnail;
import com.hipspots.util.SettingsProvider;
import com.hipspots.util.SettingsProvider.Language;

public class LocationsListActivity extends ListActivity implements android.view.View.OnClickListener {

	private VideoLocationDB[] videoLocationsDB = null;
	private VideoLocationAdapter videoLocationAdapter = null;

	CheckBox cb_sort;
	CheckBox cb_sort_en;

	RadioButton cbAll;
	RadioButton cbFavorites;
	RadioButton cbHistorical;
	RadioButton cbFood;
	RadioButton cbMuseum;

	RadioButton cbAll_en;
	RadioButton cbFavorites_en;
	RadioButton cbHistorical_en;
	RadioButton cbFood_en;
	RadioButton cbMuseum_en;

	int position = 0;

	Geocoder geocoder;
	Location location;
	LocationListener locationListener;
	CountDownTimer locationtimer;
	Typeface fontRegular;
	Typeface fontLight;
	FeedViewHolder feedViewHolder = null;
	LayoutInflater layoutInflater;

	// private double lat;
	// private double lng;
	long id;

	private final static String TAG_HISTORIC_PLACES = "HISTORIC_PLACES";
	private final static String TAG_MUSEUM = "MUSEUM";
	private final static String TAG_EAT_AND_DRINK = "EAT_AND_DRINK_AND_HOTEL";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH) {
			setContentView(R.layout.locations_list_en);
		} else {
			setContentView(R.layout.locations_list);
		}

		HipspotsApplication.setListActivityInstance(this);

		LinearLayout listContainter = (LinearLayout) findViewById(R.id.list_container);
		HipspotsApplication.setListContainer(listContainter);

		fontRegular = Typeface.createFromAsset(getAssets(), "miso.otf");
		fontLight = Typeface.createFromAsset(getAssets(), "miso-light.otf");

		setListViewInitialPadding();

		loadListView();

		setAdapter();

		getListView().setFastScrollEnabled(true);
		getListView().setScrollingCacheEnabled(false);

		if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH)
			setUIEnglish();
		else
			setUIGerman();
	}

	public void setAdapter() {
		videoLocationAdapter = new VideoLocationAdapter(getApplicationContext(), R.layout.listitems);
		setListAdapter(videoLocationAdapter);

	}

	public void loadListView() {
		if (HipspotsApplication.getCategory() == 'f') {
			HipspotsApplication.setFavoritesListActivityInstance(this);
		}

		if (HipspotsApplication.isSortedNearby())
			sortNearby();
		else
			sortAZ();

		HipspotsApplication.getMapActivityInstance().setVideoLocationsDB(videoLocationsDB);

	}

	public void scrollListToTop() {
		if (videoLocationsDB != null && videoLocationsDB.length > 0) {
			getListView().setSelectionAfterHeaderView();
			getListView().setSelection(0);
		}
	}

	public void setUIEnglish() {
		if (HipspotsApplication.isLanguageChanged() == true)
			setContentView(R.layout.locations_list_en);

		// English
		cbAll_en = (RadioButton) findViewById(R.id.cb_tab_all_en);
		cbAll_en.setChecked(true);
		cbAll_en.setSelected(true);
		cbAll_en.setOnClickListener(this);

		cbFavorites_en = (RadioButton) findViewById(R.id.cb_tab_favorites_en);
		cbFavorites_en.setOnClickListener(this);

		cbHistorical_en = (RadioButton) findViewById(R.id.cb_tab_historical_en);
		cbHistorical_en.setOnClickListener(this);

		cbFood_en = (RadioButton) findViewById(R.id.cb_tab_food_en);
		cbFood_en.setOnClickListener(this);

		cbMuseum_en = (RadioButton) findViewById(R.id.cb_tab_museum_en);
		cbMuseum_en.setOnClickListener(this);

		cb_sort = (CheckBox) findViewById(R.id.cb_tab_sort_en);
		cb_sort.setOnClickListener(this);
	}

	public void setUIGerman() {
		if (HipspotsApplication.isLanguageChanged() == true)
			setContentView(R.layout.locations_list);

		// German
		cbAll = (RadioButton) findViewById(R.id.cb_tab_all);
		cbAll.setChecked(true);
		cbAll.setSelected(true);
		cbAll.setOnClickListener(this);

		cbFavorites = (RadioButton) findViewById(R.id.cb_tab_favorites);
		cbFavorites.setOnClickListener(this);

		cbHistorical = (RadioButton) findViewById(R.id.cb_tab_historical);
		cbHistorical.setOnClickListener(this);

		cbFood = (RadioButton) findViewById(R.id.cb_tab_food);
		cbFood.setOnClickListener(this);

		cbMuseum = (RadioButton) findViewById(R.id.cb_tab_museum);
		cbMuseum.setOnClickListener(this);

		cb_sort = (CheckBox) findViewById(R.id.cb_tab_sort);
		cb_sort.setOnClickListener(this);
	}

	public void resizeListView(int i) {
	}

	public void animateScrolltoTop() {
	}

	public double roundDown(double d) {
		return (long) (d * 1e8) / 1e8;
	}

	public String formatDistance(double distance) {
		String distanceString = null;
		if (distance >= 500) {
			double kilometer = distance / 1000;
			int decimalPlaces = 1;
			BigDecimal decimal = new BigDecimal(kilometer);
			decimal = decimal.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP);
			double new_km = decimal.doubleValue();
			distanceString = String.valueOf(new_km + " km");
		} else {
			int decimalPlaces = 0;
			BigDecimal decimal = new BigDecimal(distance);
			decimal = decimal.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP);
			double meter = decimal.doubleValue();
			DecimalFormat format = new DecimalFormat();
			format.setDecimalSeparatorAlwaysShown(false);
			distanceString = String.valueOf(format.format(meter) + " m");
		}
		return distanceString;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent listIntent = new Intent(this, DetailsActivity.class);
		System.gc();
		HipspotsApplication.setCurrentLocation(videoLocationsDB[position]);
		HipspotsApplication.setSelectedId(videoLocationsDB[position]._id);

		HipspotsApplication.setDistance(formatDistance(videoLocationsDB[position].distance));
		listIntent.putExtra("category", HipspotsApplication.getCategory());
		startActivity(listIntent);
	}

	// ADAPTER
	public class VideoLocationAdapter extends ArrayAdapter<VideoLocationDB> {
		public LoadThumbnail loadThumbnail;

		public VideoLocationAdapter(Context context, int resource) {
			super(context, resource);
			loadThumbnail = new LoadThumbnail(HipspotsApplication.getListActivityInstance(), 1000);
			layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (videoLocationsDB != null) {
				final VideoLocationDB vidLocation = videoLocationsDB[position];
				// String url = vidLocation.thumbnail_url;
				String title = null;
				String desc = null;

				if (SettingsProvider.getInstance().getAppLanguage() == Language.GERMAN) {
					title = vidLocation.name_de;
					desc = vidLocation.text_de;
				} else {
					title = vidLocation.name_en;
					desc = vidLocation.text_en;
				}

				String distance = formatDistance(vidLocation.distance);

				if (convertView == null) {
					convertView = layoutInflater.inflate(R.layout.listitems, parent, false);
					feedViewHolder = new FeedViewHolder();
					feedViewHolder.layout = (LinearLayout) convertView.findViewById(R.id.list_bg);
					feedViewHolder.titleView = (TextView) convertView.findViewById(R.id.txt_title);
					feedViewHolder.descView = (TextView) convertView.findViewById(R.id.txt_list_desc);
					feedViewHolder.more = (TextView) convertView.findViewById(R.id.txt_more);
					feedViewHolder.distanceView = (TextView) convertView.findViewById(R.id.txt_distance);
					feedViewHolder.listIcon = (ImageView) convertView.findViewById(R.id.list_icon);
					feedViewHolder.dividerTop = (ImageView) convertView.findViewById(R.id.divider_top);
					feedViewHolder.v = (ImageView) convertView.findViewById(R.id.image);

					feedViewHolder.titleView.setTypeface(fontRegular);
					feedViewHolder.descView.setTypeface(fontLight);
					feedViewHolder.more.setTypeface(fontLight);
					feedViewHolder.distanceView.setTypeface(fontRegular);

					convertView.setTag(feedViewHolder);

					// load image into view if this is a new cell
					feedViewHolder.v.setScaleType(ImageView.ScaleType.CENTER_CROP);
					if (vidLocation.thumbnail_url == null || vidLocation.thumbnail_url.trim().equals(""))
						feedViewHolder.v.setImageResource(R.drawable.frame_content);
					else {
						try {

							loadThumbnail.displayImage(vidLocation.id, feedViewHolder.v, R.drawable.frame_content);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					feedViewHolder = (FeedViewHolder) convertView.getTag();
					// if this is an old cell and the content has changed, load image
					if (feedViewHolder.id != vidLocation._id) {
						feedViewHolder.v.setImageBitmap(null);

						feedViewHolder.v.setScaleType(ImageView.ScaleType.CENTER_CROP);
						if (vidLocation.thumbnail_url == null || vidLocation.thumbnail_url.trim().equals(""))
							feedViewHolder.v.setImageResource(R.drawable.frame_content);
						else {
							try {
								loadThumbnail.displayImage(vidLocation.id, feedViewHolder.v, R.drawable.frame_content);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				feedViewHolder.id = vidLocation._id;

				if (vidLocation.category.equals(TAG_HISTORIC_PLACES)) {
					feedViewHolder.titleView.setTextColor(getResources().getColor(R.color.dark_red));
					feedViewHolder.listIcon.setBackgroundResource(R.drawable.list_icon_historical);
				} else if (vidLocation.category.equals(TAG_EAT_AND_DRINK)) {
					feedViewHolder.titleView.setTextColor(getResources().getColor(R.color.dark_grey));
					feedViewHolder.listIcon.setBackgroundResource(R.drawable.list_icon_food);
				} else if (vidLocation.category.equals(TAG_MUSEUM)) {
					feedViewHolder.titleView.setTextColor(getResources().getColor(R.color.dark_grey));
					feedViewHolder.listIcon.setBackgroundResource(R.drawable.list_icon_museum);
				}

				if (vidLocation._id == HipspotsApplication.getMapSelectedId()) {
					feedViewHolder.layout.setBackgroundResource(R.drawable.list_segment_selected);
					feedViewHolder.dividerTop.setBackgroundResource(R.drawable.list_divider_selected);
					if (vidLocation.category.equals(TAG_HISTORIC_PLACES)) {
						feedViewHolder.titleView.setTextColor(getResources().getColor(R.color.dark_red));
						feedViewHolder.listIcon.setBackgroundResource(R.drawable.list_icon_historical_selected);
					} else if (vidLocation.category.equals(TAG_EAT_AND_DRINK)) {
						feedViewHolder.titleView.setTextColor(getResources().getColor(R.color.dark_grey));
						feedViewHolder.listIcon.setBackgroundResource(R.drawable.list_icon_food_selected);
					} else if (vidLocation.category.equals(TAG_MUSEUM)) {
						feedViewHolder.titleView.setTextColor(getResources().getColor(R.color.dark_grey));
						feedViewHolder.listIcon.setBackgroundResource(R.drawable.list_icon_museum_selected);
					}
				} else {
					feedViewHolder.layout.setBackgroundResource(android.R.drawable.list_selector_background);
					feedViewHolder.dividerTop.setBackgroundResource(R.drawable.check_label_background);
					if (vidLocation.category.equals(TAG_HISTORIC_PLACES)) {
						feedViewHolder.titleView.setTextColor(getResources().getColor(R.color.dark_red));
						feedViewHolder.listIcon.setBackgroundResource(R.drawable.list_icon_historical);
					} else if (vidLocation.category.equals(TAG_EAT_AND_DRINK)) {
						feedViewHolder.titleView.setTextColor(getResources().getColor(R.color.dark_grey));
						feedViewHolder.listIcon.setBackgroundResource(R.drawable.list_icon_food);
					} else if (vidLocation.category.equals(TAG_MUSEUM)) {
						feedViewHolder.titleView.setTextColor(getResources().getColor(R.color.dark_grey));
						feedViewHolder.listIcon.setBackgroundResource(R.drawable.list_icon_museum);
					}
				}

				feedViewHolder.titleView.setText(title.toUpperCase());
				feedViewHolder.descView.setText(desc);
				feedViewHolder.distanceView.setText(distance);

				if (SettingsProvider.getInstance().getAppLanguage() == Language.GERMAN) {
					feedViewHolder.more.setText(getString(R.string.de_list_more));
				} else {
					feedViewHolder.more.setText(getString(R.string.en_list_more));
				}
			}
			return convertView;
		}

		@Override
		public int getCount() {
			return videoLocationsDB == null ? 0 : videoLocationsDB.length;
		}

		@Override
		public VideoLocationDB getItem(int position) {
			return videoLocationsDB[position];
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isEnabled(int position) {
			return true;
		}

		@Override
		public long getItemId(int position) {
			return videoLocationsDB[position].id;
		}
	}

	static class FeedViewHolder {
		LinearLayout layout;
		TextView titleView;
		TextView descView;
		TextView more;
		TextView distanceView;
		ImageView v;
		ImageView listIcon;
		ImageView dividerTop;
		int id;
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {

		case R.id.cb_tab_sort:
			if (view.isSelected()) {
				view.setSelected(false);
				sortNearby();
				videoLocationAdapter.notifyDataSetChanged();
				loadListView();
			} else {
				view.setSelected(true);
				videoLocationAdapter.notifyDataSetChanged();
				sortAZ();
			}
			break;
		case R.id.cb_tab_sort_en:
			if (view.isSelected()) {
				view.setSelected(false);
				sortNearby();
				videoLocationAdapter.notifyDataSetChanged();
				loadListView();
			} else {
				view.setSelected(true);
				sortAZ();
				videoLocationAdapter.notifyDataSetChanged();
			}
			break;
		case R.id.cb_tab_all:
			HipspotsApplication.setIsPinPressed(false);
			HipspotsApplication.setCategory('a');
			HipspotsApplication.getListActivityInstance().loadListView();
			HipspotsApplication.getMapActivityInstance().initLocations();
			break;
		case R.id.cb_tab_favorites:
			HipspotsApplication.setIsPinPressed(false);
			HipspotsApplication.setCategory('f');
			HipspotsApplication.getListActivityInstance().loadListView();
			HipspotsApplication.getMapActivityInstance().initLocations();
			break;
		case R.id.cb_tab_historical:
			HipspotsApplication.setIsPinPressed(false);
			HipspotsApplication.setCategory('h');
			HipspotsApplication.getListActivityInstance().loadListView();
			HipspotsApplication.getMapActivityInstance().initLocations();
			break;
		case R.id.cb_tab_food:
			HipspotsApplication.setIsPinPressed(false);
			HipspotsApplication.setCategory('e');
			HipspotsApplication.getListActivityInstance().loadListView();
			HipspotsApplication.getMapActivityInstance().initLocations();
			break;
		case R.id.cb_tab_museum:
			HipspotsApplication.setIsPinPressed(false);
			HipspotsApplication.setCategory('m');
			HipspotsApplication.getListActivityInstance().loadListView();
			HipspotsApplication.getMapActivityInstance().initLocations();
			break;
		// ENGLISH
		case R.id.cb_tab_all_en:
			HipspotsApplication.setIsPinPressed(false);
			HipspotsApplication.setCategory('a');
			HipspotsApplication.getListActivityInstance().loadListView();
			HipspotsApplication.getMapActivityInstance().initLocations();
			break;
		case R.id.cb_tab_favorites_en:
			HipspotsApplication.setIsPinPressed(false);
			HipspotsApplication.setCategory('f');
			HipspotsApplication.getListActivityInstance().loadListView();
			HipspotsApplication.getMapActivityInstance().initLocations();
			break;
		case R.id.cb_tab_historical_en:
			HipspotsApplication.setIsPinPressed(false);
			HipspotsApplication.setCategory('h');
			HipspotsApplication.getListActivityInstance().loadListView();
			HipspotsApplication.getMapActivityInstance().initLocations();
			break;
		case R.id.cb_tab_food_en:
			HipspotsApplication.setIsPinPressed(false);
			HipspotsApplication.setCategory('e');
			HipspotsApplication.getListActivityInstance().loadListView();
			HipspotsApplication.getMapActivityInstance().initLocations();
			break;
		case R.id.cb_tab_museum_en:
			HipspotsApplication.setIsPinPressed(false);
			HipspotsApplication.setCategory('m');
			HipspotsApplication.getListActivityInstance().loadListView();
			HipspotsApplication.getMapActivityInstance().initLocations();
			break;
		}
	}

	public void sortAZ() {
		char category = HipspotsApplication.getCategory();

		DBHelper dbhelper = HipspotsApplication.getInstance().getJsonDBInstance();
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		VideoLocationDAO videoLocationDAO = new VideoLocationDAO();

		if (SettingsProvider.getInstance().getAppLanguage() == Language.GERMAN) {
			videoLocationsDB = videoLocationDAO.getVideoLocationsAlphabeticallySortedGerman(db, category);
		} else {
			videoLocationsDB = videoLocationDAO.getVideoLocationsAlphabeticallySortedEnglish(db, category);
		}

		moveLocationToTop();
		HipspotsApplication.setIsSortedNearby(false);

		if (videoLocationAdapter != null)
			videoLocationAdapter.notifyDataSetChanged();
	}

	public void sortNearby() {
		char category = HipspotsApplication.getCategory();

		DBHelper dbhelper = HipspotsApplication.getInstance().getJsonDBInstance();
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		VideoLocationDAO videoLocationDAO = new VideoLocationDAO();
		videoLocationsDB = videoLocationDAO.getVideoLocationsDistanceSorted(db, category);

		moveLocationToTop();
		HipspotsApplication.setIsSortedNearby(true);

		if (videoLocationAdapter != null)
			videoLocationAdapter.notifyDataSetChanged();
	}

	public void moveLocationToTop() {
		if (HipspotsApplication.getMapSelectedId() > 0 && videoLocationsDB != null && videoLocationsDB.length > 0) {
			DBHelper dbhelper = HipspotsApplication.getInstance().getJsonDBInstance();
			SQLiteDatabase db = dbhelper.getWritableDatabase();
			VideoLocationDAO videoLocationDAO = new VideoLocationDAO();
			VideoLocationDB videoLocationDBTapped = videoLocationDAO.getVideoLocation(db, HipspotsApplication.getMapSelectedId());
			int position = 0;
			// TODO: Make this beautiful Please
			for (int i = 0; i < videoLocationsDB.length; i++) {
				if (videoLocationsDB[i]._id == videoLocationDBTapped._id) {
					position = i;
					i = videoLocationsDB.length;
				} else
					position = 0;
			}
			if (position != 0) {
				for (int i = position; i > 0; --i) {
					videoLocationsDB[i] = videoLocationsDB[i - 1];
				}
				videoLocationsDB[0] = videoLocationDBTapped;
			}
		}
	}

	private void setListViewInitialPadding() {
		int dpiDensity = getResources().getDisplayMetrics().densityDpi;
		int currentY = 0;
		switch (dpiDensity) {
		case DisplayMetrics.DENSITY_LOW:
			// Do sth for LDPI-screen devices
			currentY = 180;
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			currentY = 260;
			break;
		case DisplayMetrics.DENSITY_HIGH:
			currentY = 460;
			break;
		case DisplayMetrics.DENSITY_XHIGH:
			currentY = 720;
			break;
		}
		HipspotsApplication.getListContainer().setPadding(0, 0, 0, currentY);
	}
}