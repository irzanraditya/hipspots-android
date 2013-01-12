package com.hipspots.model;

public class VideoLocationList {

	private VideoLocationItem[] locations = null;
	
	public VideoLocationList(){
		
	}
	
	public void setVideoLocations(VideoLocationItem[] locs){
		this.locations = locs;
	}
	
	public VideoLocationItem[] getVideoLocations(){
		return this.locations;
	}
}