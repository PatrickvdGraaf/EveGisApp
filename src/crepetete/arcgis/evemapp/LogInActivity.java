package crepetete.arcgis.evemapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import crepetete.samples.helloworld.R;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class LogInActivity extends Activity {
	
	static final String USER_ID = "com.crepetete.arcgis.evemapp.USER_ID";

	final Context context = this;
	
	private Button login;
	private EditText id_field;
	private String id;
	private String errorMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inlogscreen);
		login = (Button) findViewById(R.id.inlog_button);
		id_field = (EditText) findViewById(R.id.inlog_id_field);
		login.setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg0) {
				if(id_field.getText().toString() != null || id_field.getText().toString() != ""){
					id = id_field.getText().toString();
					try {
						LogIn(id);
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}else{
					errorMessage = "Er is geen ID ingevoerd";
					ShowErrorDialog(errorMessage);
				}
			}
		});
	}
	
	public void LogIn(String id) throws ClientProtocolException, IOException{
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(getString(R.string.backend_site)); //Mitchells URL

		// Request parameters and other properties.
		List<NameValuePair> params = new ArrayList<NameValuePair>(3);
		params.add(new BasicNameValuePair("id", id));
		params.add(new BasicNameValuePair("type", "register"));
		params.add(new BasicNameValuePair("friends", "test"));
		httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

		//Execute and get the response.
		HttpResponse response = httpclient.execute(httppost);
		System.out.println("requested");
		HttpEntity entity = response.getEntity();

		if (entity != null) {
		   if(entity.toString() == "true"){
			   Intent intent = new Intent(this, MainMap.class);
			   intent.putExtra(USER_ID, id);
			   startActivity(intent);
		   }else if(entity.toString() == "false"){
			   errorMessage = "Er is een fout ID ingevoerd";
			   ShowErrorDialog(errorMessage);
		   }else{
			   errorMessage = "Er was geen of een onbekende response";
			   ShowErrorDialog(errorMessage);
		   }
		}
	}
	
	private void ShowErrorDialog(String errorMessage) {
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.infodialog);
			dialog.setTitle("Er ging iets mis");

			TextView text = (TextView) dialog.findViewById(R.id.text);
			text.setText(errorMessage);
			ImageView image = (ImageView) dialog.findViewById(R.id.image);
			image.setVisibility(View.GONE);

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
