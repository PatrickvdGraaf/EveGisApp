package crepetete.arcgis.evemapp;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import crepetete.arcgis.evemapp.Event;

public class EventPicker extends Activity {
	
	private EditText eventIdET;
	private Button eventSearchB;
	private Button back;
	private ListView events;
	private List<Event> eventsList;
	private HttpManager hm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_event_picker);
		events = (ListView) findViewById(R.id.eventList);
		eventIdET = (EditText) findViewById(R.id.ETeventId);
		
		eventSearchB = (Button) findViewById(R.id.BeventSearch);
		eventSearchB.setOnClickListener(eventSearchButtonHandler);
		back = (Button) findViewById(R.id.back);
		back.setOnClickListener(backButtonHandler);
		
		hm = new HttpManager();
	}
	
	View.OnClickListener eventSearchButtonHandler = new View.OnClickListener() {
	    public void onClick(View v) {
	    	if(eventIdET.getText() != null){
	    		String eventId = String.valueOf(eventIdET.getText());
	    		try {
					hm.getEvent(eventId, getBaseContext());
					if(eventsList.size() != 0){
						events.setAdapter(new EventListAdapter(getBaseContext(), eventsList));
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
	    	}
	    }
	};
	
	View.OnClickListener backButtonHandler = new View.OnClickListener() {
	    public void onClick(View v) {
	    	EventPicker.this.startActivity(new Intent(EventPicker.this, MainMap.class));
	    }
	};
	
	public class EventListAdapter extends ArrayAdapter<Event> {
		public EventListAdapter(Context context, List<Event> eventsList) {
			super(context, R.layout.eventlistitem, eventsList);
			for (int i = 0; i < eventsList.size(); i++) {
				eventsList.get(i).setAdapter(this);
			}
		}
		@Override
		// Creating the View for an item in the ListView
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.eventlistitem, null);
			}
			// Hier haal ik de Friend objecten uit de friendsList en stop ze per
			// object in de listview
			Event e = eventsList.get(position);
			if (e != null) {
				TextView name = (TextView) view.findViewById(R.id.eventName);
				ImageView picture = (ImageView) view.findViewById(R.id.eventLogo);
				if (name != null) {
					name.setText(e.getName());
				}
				if (picture != null) {
					picture.setImageDrawable(e.getImage());
				}
			}
			return view;
		}
	}
}
