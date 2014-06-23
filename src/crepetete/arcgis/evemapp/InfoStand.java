package crepetete.arcgis.evemapp;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class InfoStand extends FestivalObject {

	public InfoStand(JSONObject info, int i) throws JSONException {

		// De Infostand heeft alleen maar een custom showDialog omdat de overige
		// functionaliteit al in een FestivalObject zit.

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
	}

	public void showDialog(final Dialog dialog) {
		dialog.setContentView(R.layout.infostanddialog);
		dialog.setTitle(getObj_type());
		TextView text = (TextView) dialog.findViewById(R.id.text);

		dialog.setTitle("Informatie");
		if(getDesc().equals("")){
			text.setText("Geen info beschikbaar voor deze Informatie Kiosk");
		}else{
			text.setText(getDesc());
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
