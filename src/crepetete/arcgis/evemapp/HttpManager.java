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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
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
			// creates the params string, encode them using URLEncoder
			Iterator<String> paramIterator = (postParams).keySet().iterator();
			while (paramIterator.hasNext()) {
				String key = paramIterator.next();
				String value = postParams.get(key);
				requestParams.append(URLEncoder.encode(key, "UTF-8"));
				requestParams.append("=").append(URLEncoder.encode(value, "UTF-8"));
				requestParams.append("&");
			}
			// sends POST data
			OutputStreamWriter writer = new OutputStreamWriter(
					httpConn.getOutputStream());
			writer.write(requestParams.toString());
			writer.flush();
		}
		response = readMultipleLinesRespone();
		return httpConn;
	}
	
	public JSONObject postWithJSONResponse(URL url, String source) throws ClientProtocolException, IOException, JSONException {
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
			// sends POST data
			OutputStreamWriter writer = new OutputStreamWriter(
					httpConn.getOutputStream());
			writer.write(requestParams.toString());
			writer.flush();
		}
		response = readMultipleLinesRespone();
		System.out.println(source + ": " + response[0]);
		JSONObject jsonObj = null;
		if(!response[0].contains("<br />")){
			jsonObj = new JSONObject(response[0]);
		}
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
	
	public JSONObject getLocOthers(User user, Context c) throws ClientProtocolException, IOException, JSONException {
		postParams.clear();
		postParams.put("id", user.getMyId());
		postParams.put("event_id", "1");
		url = new URL(c.getString(R.string.backend_loc_others));
		return postWithJSONResponse(url, "getLocOthers");
	}
	
	public JSONObject getEvent(String eventId, Context c) throws ClientProtocolException, IOException, JSONException {
		postParams.clear();
		postParams.put("id", eventId);
		url = new URL(c.getString(R.string.backend_events));
		return postWithJSONResponse(url, "getEvent");
	}
	
	public JSONObject getEventMapInfo(String eventId, Context c) throws ClientProtocolException, IOException, JSONException {
		postParams.clear();
		postParams.put("id", eventId);
		url = new URL(c.getString(R.string.backend_event_map_info));
		return postWithJSONResponse(url, "getEventMapInfo");
	}
	
	public Drawable LoadImageFromWebOperations(String url) {
	    try {
	        InputStream is = (InputStream) new URL(url).getContent();
	        Drawable d = Drawable.createFromStream(is, "info");
	        return d;
	    } catch (Exception e) {
	        return null;
	    }
	}
	
	public void makeRegisterParams(User user, Context c, ArrayList<Friend> friendsList) throws ClientProtocolException, IOException {
		postParams.clear();
		postParams.put("id", user.getMyId());
		for(int i=0; i<friendsList.size();i++){
			postParams.put("friends", friendsList.get(i).getId());
		}
		postParams.put("privacysetting", "full");
		url = new URL(c.getString(R.string.backend_register));
		post(url);
	}
	
	public void makeLoc_SelfParams(User user, Context c) throws ClientProtocolException, IOException {
		postParams.clear();
		postParams.put("id", user.getMyId());
		postParams.put("lat", Double.toString(user.getMyLat()));
		postParams.put("lng", Double.toString(user.getMyLng()));
		postParams.put("event_id", "1");
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
				inputStream
				));
		List<String> response = new ArrayList<String>();

		String line = "";
		while ((line = reader.readLine()) != null) {
			response.add(line);
		}
		reader.close();

		return (String[]) response.toArray(new String[0]);
	}

	public Bitmap getFacebookBitMap(String s) throws IOException {
		URL url_value = new URL(s);
		Bitmap bm = getRoundedShape(BitmapFactory.decodeStream(url_value.openConnection().getInputStream()));
		return bm;
	}
	
	public Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
		int targetWidth = 50;
		int targetHeight = 50;
		Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight,Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(targetBitmap);
		Path path = new Path();
		path.addCircle(((float) targetWidth) / 2,((float) targetHeight) / 2,(Math.min(((float) targetWidth), ((float) targetHeight)) / 2),Path.Direction.CW);
		Paint paint = new Paint(); 
		paint.setColor(Color.GRAY); 
		//paint.setStyle(Paint.Style.STROKE);
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setFilterBitmap(true);
		canvas.drawOval(new RectF(0, 0, targetWidth, targetHeight), paint) ;
		//paint.setColor(Color.TRANSPARENT); 
		canvas.clipPath(path);
		Bitmap sourceBitmap = scaleBitmapImage;
		canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(),sourceBitmap.getHeight()), new RectF(0, 0, targetWidth, targetHeight), paint);
		return targetBitmap;
	}
}
