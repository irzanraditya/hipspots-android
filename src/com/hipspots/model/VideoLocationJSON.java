package com.hipspots.model;

public class VideoLocationJSON {

	public int id = -1;
	public int updated_ts;
	public int deleted_ts;
	public String category = null;
	public String email = null;
	public double latitude = 0d;
	public double longitude = 0d;
	public String name_de = null;
	public String name_en = null;
	public String street = null;
	public String street_number = null;
	public String zip_code = null;
	public String phone = null;
	public String website = null;
	public String text_de = null;
	public String text_en = null;
	public String video_url_de = null;
	public String video_url_en = null;
	public String thumbnail_url = null;// NORMAL
	public String thumbnail_url_detail = null;// REDUCE - DETAIL
	public String thumbnail_url_thumb = null;// REDUCE - THUMBNAIL
	public String audio_url_de = null;
	public String audio_url_en = null;
	public TransportationJSON[] attr_transportation = null;
	public OpeningHoursJSON attr_opening_hours = null;

}
