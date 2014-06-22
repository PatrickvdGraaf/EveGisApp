package crepetete.arcgis.evemapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Toilet extends FestivalObject{
	
	//Ons Toilet Object heeft alleen een prijs en naam, die weergegeven worden in de custom showDialog method.
	
	private String name;
	private Double price;
	public Toilet(JSONObject info, int i) throws JSONException {
		
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
		this.setPosition(i);
		
		JSONArray arr2 = new JSONArray(info.getString("entries"));
		for (int j = 0; j < arr2.length(); j++) {
			String name = arr2.getJSONObject(j).getString("name");
			Double price = Double.parseDouble(arr2.getJSONObject(j).getString("price"));
			this.setName(name);
			this.setPrice(price);
		}
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public void showDialog(final Dialog dialog) {
		dialog.setContentView(R.layout.toiletdialog);
		dialog.setTitle(getObj_type());
		TextView text = (TextView) dialog.findViewById(R.id.text);
		TextView price = (TextView) dialog.findViewById(R.id.price);
		
		if(!getDesc().equals("")){
			dialog.setTitle(getDesc());
		}else{
			dialog.setTitle("Toilet");
		}
		
		text.setText("Prijs: ");
		price.setText(String.valueOf(getPrice()));
		
		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
}
