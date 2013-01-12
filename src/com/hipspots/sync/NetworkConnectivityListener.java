package com.hipspots.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.hipspots.util.SettingsProvider;

/**
 * A wrapper for a broadcast receiver that provides network connectivity state information, independent of network type (mobile, Wi-Fi, etc.). {@hide}
 */
public class NetworkConnectivityListener extends BroadcastReceiver {
	private static final String TAG = "NetworkConnectivityListener";
//	private boolean mListening;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (!action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION )) {
			return;
		}

		Log.d(TAG, "onReceived() called");
		ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi.isConnected()) {
			SettingsProvider.initSettings(context);
			if(!SettingsProvider.getInstance().isDownloading() && SettingsProvider.getInstance().getDownloadStarted()){	
				Intent service = new Intent(context, DownloadContentService.class);
				context.startService(service);
			}
		}
	}
}
