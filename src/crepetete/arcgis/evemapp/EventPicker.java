package crepetete.arcgis.evemapp;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.Activity;

public class EventPicker extends Activity {
	
	private EditText eventIdET;
	private Button eventSearchB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_picker);
		
		eventIdET = (EditText) findViewById(R.id.ETeventId);
		
		eventSearchB = (Button) findViewById(R.id.friends);
		eventSearchB.setOnClickListener(eventSearchButtonHandler);
	}
	
	View.OnClickListener eventSearchButtonHandler = new View.OnClickListener() {
	    public void onClick(View v) {
	    	if(eventIdET.getText() != null){
	    		String eventId = String.valueOf(eventIdET.getText());
	    		HttpManager hm = new HttpManager();
	    		try {
					hm.getEvent(eventId, getBaseContext());
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    }
	};
}
