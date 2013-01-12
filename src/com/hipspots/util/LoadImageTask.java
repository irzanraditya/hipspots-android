package com.hipspots.util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import com.hipspots.app.HipspotsApplication;
import com.hipspots.model.VideoLocationDB;

/**
 * Used to load thumbnails from the sdcard.
 * 
 * @author Sebastian
 * 
 */
public class LoadImageTask {
	private ImageView imageView;
	private boolean isThumbnail;
	private Handler handler;

	public LoadImageTask(ImageView imageView, boolean isThumbnail) {
		this.imageView = imageView;
		this.isThumbnail = isThumbnail;
		handler = new Handler();
	}

	public void execute(VideoLocationDB videoLocationDB) {
		final VideoLocationDB videoLocation = videoLocationDB;
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				Bitmap result = null;
				if (videoLocation != null) {
					int size = 400;

					// If isThumb -> Load(ThumbSD)
					if (isThumbnail) {
						size = 100;
						result = loadImageSD(videoLocation.thumbnail_path, size);
					} else {
						// If !isThumb && !Photo && !Network-> Load(Clicked(ThumbSD))
						if (!HipspotsApplication.getFragmentInstance().isNetworkConnected()
								&& (videoLocation.photo_detail_path == null || videoLocation.photo_detail_path.trim().equals(""))) {
							result = loadImageSD(videoLocation.thumbnail_path, size);
						}
						// If !isThumb && !Photo && Network-> Load(Clicked(PhotoURL))
						else if (videoLocation.photo_detail_path == null || videoLocation.photo_detail_path.trim().equals("")) {
							result = loadImageURL(videoLocation.photo_detail_url, size);
						}
						// If !isThumb && Photo-> Load(Clicked(PhotoSD))
						else {
							result = loadImageSD(videoLocation.photo_detail_path, size);
						}
					}
				}
				if (result != null) {
					final Bitmap bitmap = result;
					handler.post(new Runnable() {
						@Override
						public void run() {
							imageView.setImageBitmap(bitmap);
						}
					});
				}

			}
		};
		new Thread(runnable).start();
	}

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

	private static Bitmap loadImageURL(String _url, int size) {
		HttpURLConnection con = null;
		Bitmap bmp;
		try {
			URL url = new URL(_url);
			con = (HttpURLConnection) url.openConnection();
			InputStream is = con.getInputStream();

			// With workaround
			bmp = BitmapFactory.decodeStream(new FlushedInputStream(is));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (con != null)
				con.disconnect();
		}
		return bmp;
	}

	public static Bitmap loadImageSD(String path, int size) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, size, size);
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(path, options);
	}
}