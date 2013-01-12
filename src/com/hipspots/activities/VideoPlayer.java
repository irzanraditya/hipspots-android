package com.hipspots.activities;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import com.hipspots.R;

public class VideoPlayer extends Activity implements OnCompletionListener, OnErrorListener {
	private String filename;

	private VideoView videoPlayer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.video_player);

		System.gc();
		Intent i = getIntent();
		Bundle extras = i.getExtras();
		filename = extras.getString("video_url");
		filename = filename.replace(" ", "%20");

		videoPlayer = (VideoView) findViewById(R.id.videoPlayer);
		if(videoPlayer != null){
			videoPlayer.setOnCompletionListener(this);
			videoPlayer.setKeepScreenOn(true);
			videoPlayer.setVideoPath(filename);
			videoPlayer.setMediaController(new MediaController(this));
			videoPlayer.setOnErrorListener(this);
			videoPlayer.requestFocus();
			videoPlayer.start();
		}
	}

	/** This callback will be invoked when the file is finished playing */
	@Override
	public void onCompletion(MediaPlayer mp) {
		// Statements to be executed when the video finishes.
		if (videoPlayer != null) {
			videoPlayer.stopPlayback();
			videoPlayer = null;
		}
		this.finish();
	}

	/** Use screen touches to toggle the video between playing and paused. */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			if (videoPlayer.isPlaying()) {
				videoPlayer.pause();
			} else {
				videoPlayer.start();
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		if (videoPlayer != null) {
			if (videoPlayer.getCurrentPosition() == 0) {
				videoPlayer.stopPlayback();
				videoPlayer = null;
				this.finish();
				return true;
			}
		}
		return false;
	}

}
