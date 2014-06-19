package crepetete.arcgis.evemapp;

import org.json.JSONException;
import org.json.JSONObject;

public class InfoStand extends FestivalObject{

	public InfoStand(JSONObject info) throws JSONException {
		
		String obj_lat = info.getString("lat");
		String obj_lng = info.getString("lng");
		String obj_type = info.getString("type");
		String obj_image_url = "http://web.insidion.com" + info.getString("image_url");
		String obj_width =info.getString("width");
		String obj_height = info.getString("height");
		String obj_angle = info.getString("angle");
		String obj_desc = info.getString("desc");
		
		this.setObj_angle(obj_angle);
		this.setObj_height(obj_height);
		this.setObj_image_url(obj_image_url);
		this.setObj_lat(obj_lat);
		this.setObj_lng(obj_lng);
		this.setObj_type(obj_type);
		this.setObj_width(obj_width);
		this.setDesc(obj_desc);
	}

	
}
