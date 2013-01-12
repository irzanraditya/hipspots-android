package com.hipspots.activities;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.hipspots.R;
import com.hipspots.app.HipspotsApplication;
import com.hipspots.sync.DownloadContentService;
import com.hipspots.sync.DownloadContentTask;
import com.hipspots.util.MultiDirectionSlidingDrawer;
import com.hipspots.util.SettingsProvider;
import com.hipspots.util.SettingsProvider.Language;
import com.hipspots.util.Util;

public class MainFragmentActivity extends FragmentActivity implements OnClickListener {

	private MultiDirectionSlidingDrawer slidingDrawer;
	DownloadContentTask downloadContentTask;

	private final static String TAG = "MAIN";

	Geocoder geocoder;
	Location location;
	LocationListener locationListener;
	CountDownTimer locationtimer;

	@Override
	protected void onCreate(Bundle bundle) {
		SettingsProvider.initSettings(getApplicationContext());

		HipspotsApplication.setFragmentInstance(this);
		HipspotsApplication.getInstance().getJsonDBInstance();
		Util.isJsonDBDownloaded();

		super.onCreate(bundle);
		setContentView(R.layout.main_fragment_activity_alt);

		slidingDrawer = (MultiDirectionSlidingDrawer) findViewById(R.id.drawer);

		// Always prepare content on start
		HipspotsApplication.setCategory('a');
		HipspotsApplication.getListActivityInstance().loadListView();

		animateStart();

		if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH)
			HipspotsApplication.getListActivityInstance().setUIEnglish();
		else
			HipspotsApplication.getListActivityInstance().setUIGerman();

		HipspotsApplication.getListActivityInstance().loadListView();
	}

	public void animateStart() {
		slidingDrawer = (MultiDirectionSlidingDrawer) findViewById(R.id.drawer);
		slidingDrawer.animateOpen();
	}

	public void storeCurrentDate() {
		SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String formattedDate = df.format(c.getTime());
		editor.putString("date", formattedDate);
		editor.commit();
	}

	public String getLastOpenDate() {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		String date = prefs.getString("date", null);
		return date;
	}

	public void startDownloadAlert() {
		if (SettingsProvider.getInstance().getAppLanguage() == Language.GERMAN)
			downloadAlertDialog(getString(R.string.de_dialog_updated), getString(R.string.de_dialog_downloadTitle),
					getString(R.string.de_dialog_downloadContent), getString(R.string.de_dialog_yes), getString(R.string.de_dialog_no));
		else
			downloadAlertDialog(getString(R.string.en_dialog_updated), getString(R.string.en_dialog_downloadTitle),
					getString(R.string.en_dialog_downloadContent), getString(R.string.en_dialog_yes), getString(R.string.en_dialog_no));
	}

	public void continueDownloadAlert() {
		if (SettingsProvider.getInstance().getAppLanguage() == Language.GERMAN)
			downloadContinueDialog(getString(R.string.de_dialog_continue_download_title), getString(R.string.de_dialog_continue_download_message),
					getString(R.string.de_dialog_yes), getString(R.string.de_dialog_no));
		else
			downloadContinueDialog(getString(R.string.en_dialog_continue_download_title), getString(R.string.en_dialog_continue_download_message),
					getString(R.string.en_dialog_yes), getString(R.string.en_dialog_no));
	}

	public void downloadAlertDialog(String updated, String downloadTitle, String downloadContent, String yes, String no) {
		Log.d("ODA", downloadTitle);
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage(downloadContent).setCancelable(true).setPositiveButton(yes, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int id) {
				boolean network = HipspotsApplication.getMapActivityInstance().isNetworkConnected();
				if (network == true) {
					// WIFI ON
					if (isWifiConnected() == true) {
						if (SettingsProvider.getInstance().getAppLanguage() == Language.GERMAN)
							selectLanguageDialog(getString(R.string.de_dialog_wifi_title), getString(R.string.de_dialog_wifi),
									getString(R.string.de_dialog_yes), getString(R.string.de_dialog_no));
						else
							selectLanguageDialog(getString(R.string.en_dialog_wifi_title), getString(R.string.en_dialog_wifi),
									getString(R.string.en_dialog_yes), getString(R.string.en_dialog_no));
					} else {
						if (SettingsProvider.getInstance().getAppLanguage() == Language.GERMAN)
							// WIFI OFF
							selectLanguageDialog(getString(R.string.de_dialog_wifi_title), getString(R.string.de_dialog_no_wifi),
									getString(R.string.de_dialog_yes), getString(R.string.de_dialog_no));
						else
							selectLanguageDialog(getString(R.string.en_dialog_wifi_title), getString(R.string.en_dialog_no_wifi),
									getString(R.string.en_dialog_yes), getString(R.string.en_dialog_no));
					}
				} else {
					if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH)
						alertDialog(getString(R.string.en_dialog_network), getString(R.string.en_dialog_network_content),
						// NO NETWORK
								getString(R.string.dialog_ok));
					else
						alertDialog(getString(R.string.de_dialog_network), getString(R.string.de_dialog_network_content),
								getString(R.string.dialog_ok));
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

	public void downloadContinueDialog(String continueDownloadTitle, String continueDownloadMessage, String yes, String no) {
		Log.d("Continue Download", continueDownloadTitle);
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage(continueDownloadMessage).setCancelable(true).setPositiveButton(yes, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int id) {
				if (isNetworkConnected() == true) {
					if (isWifiConnected() == true) {
						downloadContent();
					} else {
						if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH)
							wifiFinalDialog(getString(R.string.en_dialog_wifi_title), getString(R.string.en_dialog_wifi),
									getString(R.string.en_dialog_yes), getString(R.string.en_dialog_no));
						else
							wifiFinalDialog(getString(R.string.de_dialog_wifi_title), getString(R.string.de_dialog_wifi),
									getString(R.string.de_dialog_yes), getString(R.string.de_dialog_no));
					}
				} else {
					if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH)
						alertDialog(getString(R.string.en_dialog_network), getString(R.string.en_dialog_network_content),
								getString(R.string.dialog_ok));
					else
						alertDialog(getString(R.string.de_dialog_network), getString(R.string.de_dialog_network_content),
								getString(R.string.dialog_ok));
				}
			}
		}).setNegativeButton(no, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = alt_bld.create();
		alert.setTitle(continueDownloadTitle);
		alert.show();
	}

	public void downloadContentDialog(String downloadTitle, String selectLanguage, String german, String english, String contentLanguage) {
		Log.d("ODA", downloadTitle);
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage(selectLanguage).setCancelable(true).setPositiveButton(german, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int id) {
				SettingsProvider.getInstance().setAppLanguageGerman(true);
				HipspotsApplication.setIsLanguageChanged(true);
				HipspotsApplication.getListActivityInstance().setUIGerman();
				HipspotsApplication.getFragmentInstance().animateStart();
				HipspotsApplication.getListActivityInstance().loadListView();
				HipspotsApplication.setIsLanguageChanged(false);
				if (isNetworkConnected() == true) {
					if (isWifiConnected() == true) {
						downloadContent();
					} else {
						wifiFinalDialog(getString(R.string.de_dialog_wifi_title), getString(R.string.de_dialog_wifi),
								getString(R.string.de_dialog_yes), getString(R.string.de_dialog_no));
					}
				} else {
					alertDialog(getString(R.string.de_dialog_network), getString(R.string.de_dialog_network_content), getString(R.string.dialog_ok));
				}
			}
		}).setNeutralButton(english, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int id) {
				SettingsProvider.getInstance().setAppLanguageGerman(false);
				HipspotsApplication.setIsLanguageChanged(true);
				HipspotsApplication.getListActivityInstance().setUIEnglish();
				HipspotsApplication.getFragmentInstance().animateStart();
				HipspotsApplication.getListActivityInstance().loadListView();
				HipspotsApplication.setIsLanguageChanged(false);
				if (isNetworkConnected() == true) {
					if (isWifiConnected() == true) {
						downloadContent();
					} else {
						wifiFinalDialog(getString(R.string.en_dialog_wifi_title), getString(R.string.en_dialog_wifi),
								getString(R.string.en_dialog_yes), getString(R.string.en_dialog_no));
					}
				} else {
					alertDialog(getString(R.string.en_dialog_network), getString(R.string.en_dialog_network_content), getString(R.string.dialog_ok));
				}
			}
		});
		AlertDialog alert = alt_bld.create();
		alert.setTitle(contentLanguage);
		alert.show();
	}

	protected void downloadContent() {
		Log.d(TAG, "Start Download, API Level " + Build.VERSION.SDK_INT);
		SettingsProvider.getInstance().setDownloadStarted(true);

		Intent intent = new Intent(this, DownloadContentService.class);
		startService(intent);
	}

	public void selectLanguageDialog(String downloadTitle, String message, String yes, String no) {
		Log.d("ODA", downloadTitle);
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage(message).setCancelable(true).setPositiveButton(yes, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int id) {
				if (SettingsProvider.getInstance().getAppLanguage() == Language.GERMAN)
					HipspotsApplication.getFragmentInstance().downloadContentDialog(getString(R.string.de_dialog_downloadTitle),
							getString(R.string.de_dialog_selectLanguage), getString(R.string.de_dialog_german),
							getString(R.string.de_dialog_english), getString(R.string.de_dialog_contentLanguage));
				else
					HipspotsApplication.getFragmentInstance().downloadContentDialog(getString(R.string.en_dialog_downloadTitle),
							getString(R.string.en_dialog_selectLanguage), getString(R.string.en_dialog_german),
							getString(R.string.en_dialog_english), getString(R.string.en_dialog_contentLanguage));

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

	public void wifiFinalDialog(String downloadTitle, String message, String yes, String no) {
		Log.d("ODA", downloadTitle);
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage(message).setCancelable(true).setPositiveButton(yes, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int id) {
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

	public boolean isWifiConnected() {
		ConnectivityManager conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mifi = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (mifi.isConnected()) {
			// There are no active networks.
			return true;
		} else
			return false;

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

	@Override
	public void onClick(View v) {
	}

	public void alertDialog(String downloadTitle, String downloadContent, String no) {
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage(downloadContent).setCancelable(true).setNegativeButton(no, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = alt_bld.create();
		alert.setTitle(downloadTitle);
		alert.show();
	}
}