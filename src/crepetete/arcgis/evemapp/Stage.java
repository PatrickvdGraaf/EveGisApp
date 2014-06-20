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

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Stage extends FestivalObject{
	private List<RosterObject> roster;
	
	public Stage(JSONObject info, int i, Activity a) throws JSONException, ParseException {
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
		this.setPosition(i);
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


	public void showDialog(final Dialog dialog) {
		dialog.setContentView(R.layout.stagedialog);
		if(!getDesc().equals("")){
			dialog.setTitle(getDesc());
		}else{
			dialog.setTitle("Stage");
		}
		LinearLayout linearLayout = (LinearLayout) dialog.findViewById(R.id.info);
		RelativeLayout relativeLayout = (RelativeLayout) dialog.findViewById(R.id.rInfo);
		RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			if(roster.size()>0){
				for(int i=0; i<roster.size();i++){
					RosterObject ro = roster.get(i);
					TextView performer = new TextView(dialog.getContext());
					TextView time = new TextView(dialog.getContext());
					performer.setText(ro.getPerformer());
					time.setText(ro.getStartTime()+" - "+ro.getEndTime());	
					performer.setLayoutParams(new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
					time.setLayoutParams(new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
					linearLayout.addView(performer);
					linearLayout.addView(time);
				}
			}else{
				TextView info = new TextView(dialog.getContext());
				info.setLayoutParams(new LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				info.setText("Geen info beschikbaar voor dit podium.");
				linearLayout.addView(info);
			}
			Button dialogButton = new Button(dialog.getContext());
			dialogButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			dialog.setCanceledOnTouchOutside(true);
			dialogButton.setLayoutParams(lp1);
			dialogButton.setId(1);
			dialogButton.setText("Sluit");
			relativeLayout.addView(dialogButton);
		dialog.show();
	}
	
}
