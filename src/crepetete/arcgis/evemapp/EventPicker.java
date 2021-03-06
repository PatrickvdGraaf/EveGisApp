package crepetete.arcgis.evemapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import crepetete.arcgis.evemapp.Event;

public class EventPicker extends Activity {

	// Net als de FriendsList laat deze activity met een listview de
	// mogenlijkheden zien, hier Evenementen. Met de zoekbutton wordt
	// er een lijst met evenementen opgehaald. Als een van deze aangeklikt
	// wordt, gaat de app weer terug naar de MainMap en zal loadEvent
	// uitgevoerd worden.

	private Button eventSearchB;
	private Button back;
	private ListView events;
	private List<Event> eventsList;
	private HttpManager hm;
	private TextView name;
	private TextView id;
	private TextView date;
	private TextView description;
	private ImageView picture;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_event_picker);
		hm = new HttpManager();
		events = (ListView) findViewById(R.id.eventList);
		eventSearchB = (Button) findViewById(R.id.BeventSearch);
		back = (Button) findViewById(R.id.back);
		eventsList = new ArrayList<Event>();

		eventSearchB.setOnClickListener(eventSearchButtonHandler);
		back.setOnClickListener(backButtonHandler);

	}

	View.OnClickListener eventSearchButtonHandler = new View.OnClickListener() {
		public void onClick(View v) {
			try {
				JSONObject jsonObj = hm.getEvent("", getBaseContext());
				JSONArray arr = jsonObj.getJSONArray("events");
				for (int i = 0; i < arr.length(); i++) {
					String name = arr.getJSONObject(i).getString("name");
					String id = arr.getJSONObject(i).getString("id");
					String description = arr.getJSONObject(i).getString(
							"description");
					String event_image_url = arr.getJSONObject(i).getString(
							"image");
					JSONObject startTimeArr = (JSONObject) arr.getJSONObject(i)
							.get("start_date");
					String start_date = startTimeArr.getString("date");
					String start_date_timezone_type = startTimeArr
							.getString("timezone_type");
					String start_date_timezone = startTimeArr
							.getString("timezone");
					JSONObject endTimeArr = (JSONObject) arr.getJSONObject(i)
							.get("end_date");
					String end_date = endTimeArr.getString("date");
					String end_date_timezone_type = startTimeArr
							.getString("timezone_type");
					String end_date_timezone = startTimeArr
							.getString("timezone");

					Event e = new Event();
					e.setName(name);
					e.setId(id);
					e.setDescription(description);
					e.setStart_date(start_date);
					e.setImage(hm
							.LoadImageFromWebOperations("http://web.insidion.com"
									+ event_image_url));
					e.setStart_date_timezone_type(start_date_timezone_type);
					e.setStart_date_timezone(start_date_timezone);
					e.setEnd_date(end_date);
					e.setEnd_date_timezone_type(end_date_timezone_type);
					e.setEnd_date_timezone(end_date_timezone);
					eventsList.add(e);
				}
				if (eventsList.size() != 0) {
					events.setAdapter(new EventListAdapter(getBaseContext(),
							eventsList));

					events.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> arg0, View v,
								int position, long id) {
							Event e = eventsList.get(position);
							Intent resultIntent = new Intent();
							resultIntent.putExtra("eventId", e.getId());
							setResult(Activity.RESULT_OK, resultIntent);
							finish();
						}
					});
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};

	View.OnClickListener backButtonHandler = new View.OnClickListener() {
		public void onClick(View v) {
			finish();
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
				name = (TextView) view.findViewById(R.id.eventName);
				id = (TextView) view.findViewById(R.id.eventId);
				date = (TextView) view.findViewById(R.id.eventDate);
				description = (TextView) view
						.findViewById(R.id.eventDescription);
				picture = (ImageView) view.findViewById(R.id.eventLogo);
				id.setVisibility(View.GONE);
				if (name != null) {
					name.setText(e.getName());
				}
				if (id != null) {
					id.setText(e.getId());
				}
				if (picture != null && e.getImage() != null) {
					Drawable d = e.getImage();
					picture.setImageDrawable(d);
				} else {
					picture.setImageDrawable(getResources().getDrawable(
							R.drawable.festival));
				}

				if (description != null) {
					description.setText(e.getDescription());
				}

				if (date != null) {
					date.setText(e.getStart_date() + " - " + e.getEnd_date());
				}
			}
			return view;
		}

		public Bitmap drawableToBitmap(Drawable drawable) {
			Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
					drawable.getIntrinsicHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			drawable.draw(canvas);

			return bitmap;
		}
	}
}
