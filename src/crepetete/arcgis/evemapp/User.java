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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class User {	
	private String myId; //Dummy ID
	private String name;
	private double myLat;
	private double myLng;
	private String webUrl;
	private Map<String, String> myParams = new HashMap<String, String>();
	private static HttpURLConnection httpConn;
	private String[] response;
	
	public String getMyId() {
		return myId;
	}
	public void setMyId(String string) {
		System.out.println(string);
		this.myId = string;
	}
	public String getMyName() {
		return name;
	}
	public void setMyName(String name) {
		System.out.println(name);
		this.name = name;
	}
	public double getMyLat() {
		return myLat;
	}
	public void setMyLat(double myLat) {
		this.myLat = myLat;
	}
	public double getMyLng() {
		return myLng;
	}
	public void setMyLng(double myLng) {
		this.myLng = myLng;
	}
	public Map<String, String> getMyParams() {
		return myParams;
	}
	public void setMyParams(Map<String, String> params) {
		this.myParams = params;
	}
	
	public User(String string){
		webUrl = string;
	}
		
	public void makeLoc_SelfParams(String url) throws ClientProtocolException, IOException{
		myParams.clear();
		myParams.put("id", getMyId());
		myParams.put("type",  "loc_self");
		myParams.put("lat", Double.toString(getMyLat()));
		myParams.put("long", Double.toString(getMyLng()));
		
		LogIn(url, myParams);
		return;
	}
		
	public HttpURLConnection LogIn(String u, Map<String, String> params) throws ClientProtocolException, IOException{
			URL url = new URL(u);
		      httpConn = (HttpURLConnection) url.openConnection();
		      httpConn.setUseCaches(false);

		      httpConn.setDoInput(true); // true indicates the server returns response

		      StringBuffer requestParams = new StringBuffer();
		      Map<String, String> postParams = params;
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
		      System.out.println(response);
		      
		      return httpConn;
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
