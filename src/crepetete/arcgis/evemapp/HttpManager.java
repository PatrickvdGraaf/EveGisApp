package crepetete.arcgis.evemapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;

public class HttpManager {

	private Map<String, String> postParams = new HashMap<String, String>();

	private static HttpURLConnection httpConn;
	
	private String[] response;
	private URL url;

	public HttpURLConnection post(URL url) throws ClientProtocolException, IOException{
		httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setUseCaches(false);

		httpConn.setDoInput(true); // true indicates the server returns response

		StringBuffer requestParams = new StringBuffer();
		if (postParams != null && postParams.size() > 0) {

			httpConn.setDoOutput(true); // true indicates POST request
			System.out.println("postparams " + postParams);
			// creates the params string, encode them using URLEncoder
			Iterator<String> paramIterator = (postParams).keySet().iterator();
			while (paramIterator.hasNext()) {
				String key = paramIterator.next();
				String value = postParams.get(key);
				requestParams.append(URLEncoder.encode(key, "UTF-8"));
				requestParams.append("=").append(URLEncoder.encode(value, "UTF-8"));
				requestParams.append("&");
			}
			System.out.println("requestParams " + requestParams.toString());
			// sends POST data
			OutputStreamWriter writer = new OutputStreamWriter(
					httpConn.getOutputStream());
			writer.write(requestParams.toString());
			writer.flush();
		}
		response = readMultipleLinesRespone();
		System.out.println("response " + response[0]);
		return httpConn;
	}
	
	public JSONObject postWithJSONResponse(URL url) throws ClientProtocolException, IOException, JSONException {
		httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setUseCaches(false);

		httpConn.setDoInput(true); // true indicates the server returns response

		StringBuffer requestParams = new StringBuffer();
		if (postParams != null && postParams.size() > 0) {

			httpConn.setDoOutput(true); // true indicates POST request
			// creates the params string, encode them using URLEncoder
			Iterator<String> paramIterator = (postParams).keySet().iterator();
			while (paramIterator.hasNext()) {
				String key = paramIterator.next();
				String value = postParams.get(key);
				requestParams.append(URLEncoder.encode(key, "UTF-8"));
				requestParams.append("=").append(URLEncoder.encode(value, "UTF-8"));
				requestParams.append("&");
			}
			System.out.println("requestParams " + requestParams.toString());
			// sends POST data
			OutputStreamWriter writer = new OutputStreamWriter(
					httpConn.getOutputStream());
			writer.write(requestParams.toString());
			writer.flush();
		}
		response = readMultipleLinesRespone();
		JSONObject jsonObj = new JSONObject(response[0]);
		
		return jsonObj;
	}
	
	// Check of er internet is
	public boolean isOnline(MainMap mainMap) {
		ConnectivityManager cm = (ConnectivityManager) mainMap.getSystemService(FragmentActivity.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}
	
	public void makeRegisterParams(User user, Context c) throws ClientProtocolException, IOException {
		postParams.clear();
		postParams.put("id", user.getMyId());
		postParams.put("friends", "10202127732734664");
		postParams.put("privacysetting", "full");
		url = new URL(c.getString(R.string.backend_register));
		post(url);
	}
	
	public JSONObject getLocOthers(User user, Context c) throws ClientProtocolException, IOException, JSONException {
		postParams.clear();
		postParams.put("id", user.getMyId());
		url = new URL(c.getString(R.string.backend_loc_others));
		return postWithJSONResponse(url);
	}
	
	public void makeLoc_SelfParams(User user, Context c) throws ClientProtocolException, IOException {
		postParams.clear();
		postParams.put("id", user.getMyId());
		postParams.put("lat", Double.toString(user.getMyLat()));
		postParams.put("lng", Double.toString(user.getMyLng()));
		url = new URL(c.getString(R.string.backend_location_self));
		post(url);
	}
	
	public String[] readMultipleLinesRespone() throws IOException {
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
