package crepetete.arcgis.evemapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import crepetete.arcgis.evemapp.R;

public class LogInActivity extends Activity {
	
	static final String USER_ID = "com.crepetete.arcgis.evemapp.USER_ID";
	
	private static HttpURLConnection httpConn;

	final Context context = this;
	
	private Button login;
	private EditText id_field;
	private ProgressBar login_spinner;
	private String id;
	private String[] response;
	private String errorMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginscreen);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		login = (Button) findViewById(R.id.login_button);
		id_field = (EditText) findViewById(R.id.login_id_field);
		login_spinner = (ProgressBar) findViewById(R.id.login_spinner);
		login_spinner.setVisibility(View.GONE);
		login.setOnClickListener(new View.OnClickListener(){
			public void onClick(View arg0) {
				if(!id_field.getText().toString().matches("") || id_field.getText().toString().contains(" ")){
					login_spinner.setVisibility(View.VISIBLE);
					id = id_field.getText().toString();
					AsyncTaskRunner runner = new AsyncTaskRunner();
					runner.execute();					
				}else{
					errorMessage = "Er is geen of een ongeldige ID ingevoerd";
					showErrorDialog(errorMessage);
				}
			}
		});
	}
	
	private class AsyncTaskRunner extends AsyncTask<String, String, String> {

		  @Override
		  protected String doInBackground(String... params) {
			  String postResult;
			   try {
					LogIn(id);
					postResult = response[0];
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					postResult = e.getMessage();
				} catch (IOException e) {
					e.printStackTrace();
					postResult = e.getMessage();
				}
			   return postResult;
		  }
		 @Override
		 protected void onPostExecute(String result) {
				if(result.equals("true")){
					Intent myIntent = new Intent(LogInActivity.this, MainMap.class);
					myIntent.putExtra("id", id); 
					LogInActivity.this.startActivity(myIntent);
				}else if(result.equals("false")){
					errorMessage = "Er is een foute ID ingevoerd";
				}else{
					errorMessage = "Er was geen of een foute response van de server: " + result;
				}
				showErrorDialog(errorMessage);
				login_spinner.setVisibility(View.GONE);
		    }
	}
			
	public HttpURLConnection LogIn(String id) throws ClientProtocolException, IOException{
		URL url = new URL(getString(R.string.backend_site));
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
 
        httpConn.setDoInput(true); // true indicates the server returns response
 
        StringBuffer requestParams = new StringBuffer();
        Map<String, String> postParams = makeParams();
        if (postParams != null && postParams.size() > 0) {
        	
            httpConn.setDoOutput(true); // true indicates POST request
			System.out.println(postParams);
            // creates the params string, encode them using URLEncoder
            Iterator<String> paramIterator = (postParams).keySet().iterator();
            while (paramIterator.hasNext()) {
                String key = paramIterator.next();
                String value = postParams.get(key);
                requestParams.append(URLEncoder.encode(key, "UTF-8"));
                requestParams.append("=").append(
                        URLEncoder.encode(value, "UTF-8"));
                requestParams.append("&");
            }
            System.out.println(requestParams.toString());
            // sends POST data
            OutputStreamWriter writer = new OutputStreamWriter(
                    httpConn.getOutputStream());
            writer.write(requestParams.toString());
            writer.flush();
        }
        response = readMultipleLinesRespone();
 
        return httpConn;
    }
	
	public Map<String, String> makeParams() throws UnsupportedEncodingException{
		// Request parameters and other properties.
				Map<String, String> params = new HashMap<String, String>();
				params.put("id", id);
				params.put("type", "register");
				params.put("friends", "");
				params.put("privacysetting", "full");
				return params;
	}
	
	private void showErrorDialog(String errorMessage) {
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.infodialog);
			dialog.setTitle("Er ging iets mis");

			TextView text = (TextView) dialog.findViewById(R.id.text);
			text.setText(errorMessage);
			ImageView image = (ImageView) dialog.findViewById(R.id.image);
			image.setVisibility(View.INVISIBLE);

			Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
			// if button is clicked, close the custom dialog
			dialogButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			dialog.show();
	}
	
	public static String[] readMultipleLinesRespone() throws IOException {
        InputStream inputStream = null;
        if (httpConn != null) {
            inputStream = httpConn.getInputStream();
        } else {
            throw new IOException("Connection is not established.");
        }
 
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        List<String> response = new ArrayList<String>();
 
        String line = "";
        while ((line = reader.readLine()) != null) {
            response.add(line);
        }
        reader.close();
 
        return (String[]) response.toArray(new String[0]);
    }
}
