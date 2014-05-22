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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;

public class HttpManager {

	private Map<String, String> postParams = new HashMap<String, String>();

	private static HttpURLConnection httpConn;
	
	private String[] response;

	public HttpURLConnection post(Context context) throws ClientProtocolException, IOException {
		URL url = new URL(context.getString(R.string.backend_site));
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
		System.out.println("response " + postParams.get("type") + ": "
				+ response[0]);
		return httpConn;
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
		postParams.put("type", "register");
		postParams.put("friends", "10000");
		postParams.put("privacysetting", "full");
		//runner = new AsyncTaskRunner();
		//runner.execute();
		post(c);
	}
	
	public void makeLoc_SelfParams(User user, Context c) throws ClientProtocolException, IOException {
		postParams.clear();
		postParams.put("id", user.getMyId());
		postParams.put("type", "loc_self");
		postParams.put("lat", Double.toString(user.getMyLat()));
		postParams.put("long", Double.toString(user.getMyLng()));
		//runner = new AsyncTaskRunner();
		//runner.execute();'
		post(c);
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
