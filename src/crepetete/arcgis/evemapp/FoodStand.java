package crepetete.arcgis.evemapp;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FoodStand extends FestivalObject{
	private List<Food> menu;

	public FoodStand(JSONObject info) throws JSONException {
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
		
		JSONArray arr2 = new JSONArray(info.getString("entries"));
		List<Food> m = new ArrayList<Food>();
		for (int j = 0; j < arr2.length(); j++) {
			Food f = new Food();
			String name = arr2.getJSONObject(j).getString("name");
			Double price =  Double.parseDouble(arr2.getJSONObject(j).getString("price"));
			f.setName(name);
			f.setPrice(price);
			m.add(f);
		}
		this.setMenu(m);
	}

	public List<Food> getMenu() {
		return menu;
	}

	public void setMenu(List<Food> menu) {
		this.menu = menu;
	}
}
