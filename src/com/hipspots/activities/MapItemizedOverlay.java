package com.hipspots.activities;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.hipspots.app.HipspotsApplication;

public class MapItemizedOverlay extends ItemizedOverlay {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	public MapItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	public void addOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
	}

	@Override
	public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, false);
	}

	@Override
	protected boolean onTap(int index) {
		OverlayItem item = mOverlays.get(index);
		GeoPoint point = item.getPoint();

		int id = Integer.parseInt(item.getTitle());
		HipspotsApplication.setMapSelectedId(id);
		
		if(HipspotsApplication.isSortedNearby())
			HipspotsApplication.getListActivityInstance().sortNearby();
		else
			HipspotsApplication.getListActivityInstance().sortAZ();
		HipspotsApplication.getMapActivityInstance().zoomMap(point);
		HipspotsApplication.getSlidingDrawer().animateDrawerHeight();
		HipspotsApplication.setIsPinPressed(true);
		HipspotsApplication.getListActivityInstance().loadListView();
		HipspotsApplication.getListActivityInstance().scrollListToTop();

		return true;
	}
}