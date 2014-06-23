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
import android.widget.TextView;

public class MarketStall extends FestivalObject {
	private List<Food> menu;

	// MarketStall verkoopt misschien niet hetzelfde als een etenstentje, maar
	// zijn objecten hebben ook alleen maar een naam en prijs,
	// en daarom gebruiken we voorlopig grotendeels de functies van de
	// FoodStand, en zijn de MarketStalls Objecten Food.

	public MarketStall(JSONObject info, int i) throws JSONException {
		String obj_lat = info.getString("lat");
		String obj_lng = info.getString("lng");
		String obj_type = info.getString("type");
		String obj_image_url = "http://web.insidion.com"
				+ info.getString("image_url");
		String obj_width = info.getString("width");
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
			Double price = Double.parseDouble(arr2.getJSONObject(j).getString(
					"price"));
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
		if (!getDesc().equals("")) {
			dialog.setTitle(getDesc());
		} else {
			dialog.setTitle("Kraampje");
		}

		TextView name = (TextView) dialog.findViewById(R.id.text);
		TextView price = (TextView) dialog.findViewById(R.id.price);

		if (menu.size() > 0) {
			String Sname = "";
			String Sprice = "";
			for (int i = 0; i < menu.size(); i++) {
				Food f = menu.get(i);
				Sname = Sname + "\r\n" + f.getName();
				Sprice = Sprice + "\r\n" + f.getPrice();
			}
			name.setText(Sname);
			price.setText(Sprice);
		} else {
			name.setText("Geen info beschikbaar voor dit kraampje.");
		}

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
