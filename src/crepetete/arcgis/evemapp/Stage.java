package crepetete.arcgis.evemapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.esri.core.map.Graphic;
import com.facebook.widget.ProfilePictureView;

import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Stage extends FestivalObject{
	private List<RosterObject> roster;
	
	public Stage(JSONObject info) throws JSONException, ParseException {
		String obj_lat = info.getString("lat");
		String obj_lng = info.getString("lng");
		String obj_type = info.getString("type");
		String obj_image_url = "http://web.insidion.com" + info.getString("image_url");
		String obj_width =info.getString("width");
		String obj_height = info.getString("height");
		String obj_angle = info.getString("angle");
		String obj_desc = info.getString("desc");
		
		JSONArray arr2 = new JSONArray(info.getString("entries"));
		List<RosterObject> m = new ArrayList<RosterObject>();
		for (int j = 0; j < arr2.length(); j++) {
			RosterObject ro = new RosterObject();
			String performer = arr2.getJSONObject(j).getString("performer");
			Date startTime =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(arr2.getJSONObject(j).getJSONObject("startTime").getString("date"));
			Date endTime =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(arr2.getJSONObject(j).getJSONObject("endTime").getString("date"));
			ro.setEndTime(endTime);
			ro.setPerformer(performer);
			ro.setStartTime(startTime);
			m.add(ro);
		}
		this.setRoster(m);
		this.setObj_angle(obj_angle);
		this.setObj_height(obj_height);
		this.setObj_image_url(obj_image_url);
		this.setObj_lat(obj_lat);
		this.setObj_lng(obj_lng);
		this.setObj_type(obj_type);
		this.setObj_width(obj_width);
		this.setDesc(obj_desc);
		
	}
	public List<RosterObject> getRoster() {
		return roster;
	}
	public void setRoster(List<RosterObject> roster) {
		this.roster = roster;
	}
	public void showDialog(final Dialog dialog, Graphic foundGraphic) {
		// custom dialog
		dialog.setContentView(R.layout.infodialog);
		dialog.setTitle("Informatie");

		TextView text = (TextView) dialog.findViewById(R.id.text);
		text.setText((String) foundGraphic.getAttributeValue("name"));
		ProfilePictureView dialogPicture = (ProfilePictureView) dialog
				.findViewById(R.id.dialog_profile_pic);

		String id = (String) foundGraphic.getAttributeValue("id");
		dialogPicture.setProfileId(id);
		Button dialogButton = (Button) dialog
				.findViewById(R.id.dialogButtonOK);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});		
	}
}
