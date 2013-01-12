package com.hipspots.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.hipspots.R;

public class SplashActivity extends Activity {

	@Override
	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);

		setContentView(R.layout.splash);

		/*
		 * New Handler to start the Menu-Activity
		 *
		 * and close this Splash-Screen after some seconds.
		 */

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {

				/* Create an Intent that will start the Menu-Activity. */

				Intent mainIntent = new Intent(SplashActivity.this, MainFragmentActivity.class);

				SplashActivity.this.startActivity(mainIntent);
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				SplashActivity.this.finish();

			}

		}, 800);

	}
}
