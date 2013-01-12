package com.hipspots.sync;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;

import com.hipspots.R;
import com.hipspots.activities.MainFragmentActivity;
import com.hipspots.util.SettingsProvider;
import com.hipspots.util.SettingsProvider.Language;

public class DownloadContentService extends Service {
	private static final int ONGOING_NOTIFICATION = 1;
	Notification notification;
	PendingIntent pendingIntent;
	NetworkConnectivityListener listener;

	@Override
	public void onCreate() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		notification = new Notification(R.drawable.ic_launcher, "WHATWASHERE", System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, MainFragmentActivity.class);
		pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH)
			notification.setLatestEventInfo(this, "WHATWASHERE downloading content", "Initializing...", pendingIntent);
		else
			notification.setLatestEventInfo(this, "WHATWASHERE Inhalte herunterladen", "Initialisiere...", pendingIntent);
		startForeground(ONGOING_NOTIFICATION, notification);

		DownloadContentTask contentDownloader = new DownloadContentTask(this.getApplicationContext(), this);
		contentDownloader.execute();

		// start NetworkconnectivityListener
		listener = new NetworkConnectivityListener();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		this.getApplicationContext().registerReceiver(listener, filter);

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	public void updateNotificationStatus(String text){
		if(notification != null){
			NotificationManager notificationManager = (NotificationManager) this.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
			if (SettingsProvider.getInstance().getAppLanguage() == Language.ENGLISH)
				notification.setLatestEventInfo(this, "WHATWASHERE downloading content...", text, pendingIntent);
			else
				notification.setLatestEventInfo(this, "WHATWASHERE Inhalte herunterladen...", text, pendingIntent);
			notificationManager.notify(ONGOING_NOTIFICATION, notification);
		}
	}

	@Override
	public void onDestroy() {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
