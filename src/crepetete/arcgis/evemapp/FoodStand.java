package crepetete.arcgis.evemapp;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class FoodStand extends FestivalObject{
	private List<Food> menu;

	public FoodStand(JSONObject info, int i) throws JSONException {
		String obj_lat = info.getString("lat");
		String obj_lng = info.getString("lng");
		String obj_type = info.getString("type");
		String obj_image_url = "http://web.insidion.com" + info.getString("image_url");
		String obj_width =info.getString("width");
		String obj_height = info.getString("height");
		String obj_angle = info.getString("angle");
		String obj_desc = info.getString("desc");
		
		this.setObj_angle(obj_angle);
		this.setPosition(i);
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

	public void showDialog(final Dialog dialog) {
		dialog.setContentView(R.layout.fooddialog);
		if(!getDesc().equals("")){
			dialog.setTitle(getDesc());
		}else{
			dialog.setTitle("Voedsel kraampje");
		}
		LinearLayout linearLayout = (LinearLayout) dialog.findViewById(R.id.info);
		RelativeLayout relativeLayout = (RelativeLayout) dialog.findViewById(R.id.rInfo);
		RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			if(menu.size()>0){
				for(int i=0; i<menu.size();i++){
					Food f = menu.get(i);
					TextView name = new TextView(dialog.getContext());
					TextView price = new TextView(dialog.getContext());
					name.setText(f.getName());
					price.setText(String.valueOf(f.getPrice()));	
					name.setLayoutParams(new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
					price.setLayoutParams(new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
					linearLayout.addView(name);
					linearLayout.addView(price);
				}
			}else{
				TextView info = new TextView(dialog.getContext());
				info.setLayoutParams(new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				info.setText("Geen info beschikbaar voor dit kraampje.");
				linearLayout.addView(info);
			}
			Button dialogButton = new Button(dialog.getContext());
			dialogButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			
			dialogButton.setLayoutParams(lp1);
			dialogButton.setId(1);
			dialogButton.setText("Sluit");
			relativeLayout.addView(dialogButton);
		dialog.show();
		
	}
}
