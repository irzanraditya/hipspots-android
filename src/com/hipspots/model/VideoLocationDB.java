package com.hipspots.model;

public class VideoLocationDB {

	public int _id = -1;
	public int id = -1;
	public int updated_ts;
	public int deleted_ts;
	public String name_de = null;
	public String name_en = null;
	public double longitude = 0d;
	public double latitude = 0d;
	public String text_de = null;
	public String text_en = null;
	public String photo_url = null;
	public String photo_detail_url = null;
	public String thumbnail_url = null;
	public String audio_url_de = null;
	public String audio_url_en = null;
	public String video_url_de = null;
	public String video_url_en = null;
	public String photo_path = null;
	public String photo_detail_path = null;
	public String thumbnail_path = null;
	public String video_path_de = null;
	public String video_path_en = null;
	public String audio_path_de = null;
	public String audio_path_en = null;
	public String street = null;
	public String street_number = null;
	public String zip_code = null;
	public String phone = null;
	public String website = null;
	public String category = null;
	public String email = null;
	public int is_favorited = 0;
	public double distance = 0;
	public int update_time = 0;
	public int content_updated_img = 0;
	public int content_updated_ger = 0;
	public int content_updated_eng = 0;
	public TransportationDB[] attr_transportation = null;
	public OpeningHoursJSON attr_opening_hours = null;

	public VideoLocationDB() {

	}

}
