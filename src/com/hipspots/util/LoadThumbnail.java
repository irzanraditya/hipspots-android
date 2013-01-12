package com.hipspots.util;

import java.io.File;
import java.text.DateFormat;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

/**
 * 
 * Uses: http://codehenge.net/blog/2011/06/android-development-tutorial-asynchronous-lazy-loading-and-caching-of-listview-images/
 */

public class LoadThumbnail {

	long cacheDuration;
	DateFormat mDateFormatter;

	private HashMap<Integer, Bitmap> imageMap = new HashMap<Integer, Bitmap>();

	File cacheDir;
	private static String cacheDirPath = android.os.Environment.getExternalStorageDirectory().toString() + "/Android/data/com.wwh/thumbnail/";

	public LoadThumbnail(Context context, long _cacheDuration) {

		cacheDuration = _cacheDuration;
		// Make background thread low priority, to avoid affecting UI performance
	}

	public void displayImage(int id, ImageView imageView, int defaultDrawableId) {
		if (imageMap.containsKey(id)) {
			imageView.setImageBitmap(imageMap.get(id));
		} else {
			loadImage(id, imageView);
			// Load defaultDrawable
			imageView.setImageBitmap(imageMap.get(id));
		}
		imageView.invalidate();
	}

	private void loadImage(int id, ImageView imageView) {
		// TODO: do this in a thread
		Bitmap bmp = getBitmap(id);
		imageMap.put(id, bmp);
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

	public static Bitmap getBitmap(int id) {
		int size = 100;

		final BitmapFactory.Options options = new BitmapFactory.Options();
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, size, size);
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(cacheDirPath + id + ".jpg", options);
	}

}