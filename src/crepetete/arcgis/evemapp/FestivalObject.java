package crepetete.arcgis.evemapp;

import android.graphics.drawable.Drawable;

public class FestivalObject {
	
	private String obj_id;
	private String obj_lat;
	private String obj_lng;
	private String obj_type;
	private Drawable obj_image;
	private String obj_width;
	private String obj_height;
	private String obj_angle;
	
	public String getObj_id() {
		return obj_id;
	}
	public void setObj_id(String obj_id) {
		this.obj_id = obj_id;
	}
	public String getObj_lat() {
		return obj_lat;
	}
	public void setObj_lat(String obj_lat) {
		this.obj_lat = obj_lat;
	}
	public String getObj_lng() {
		return obj_lng;
	}
	public void setObj_lng(String obj_lng) {
		this.obj_lng = obj_lng;
	}
	public String getObj_type() {
		return obj_type;
	}
	public void setObj_type(String obj_type) {
		this.obj_type = obj_type;
	}
	public Drawable getObj_image() {
		return obj_image;
	}
	public void setObj_image_url(String obj_image_url) {
		HttpManager hm = new HttpManager();
		this.obj_image = hm.LoadImageFromWebOperations(obj_image_url);;
	}
	public String getObj_width() {
		return obj_width;
	}
	public void setObj_width(String obj_width) {
		this.obj_width = obj_width;
	}
	public String getObj_height() {
		return obj_height;
	}
	public void setObj_height(String obj_height) {
		this.obj_height = obj_height;
	}
	public String getObj_angle() {
		return obj_angle;
	}
	public void setObj_angle(String obj_angle) {
		this.obj_angle = obj_angle;
	}

}