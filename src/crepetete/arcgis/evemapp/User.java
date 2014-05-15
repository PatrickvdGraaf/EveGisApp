package crepetete.arcgis.evemapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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

public class User {	
	private String myId; //Dummy ID
	private String name;
	private double myLat;
	private double myLng;
	private String webUrl;
	private List<NameValuePair> myParams = new ArrayList<NameValuePair>();
	
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
		System.out.println(myLat);
		this.myLat = myLat;
	}
	public double getMyLng() {
		return myLng;
	}
	public void setMyLng(double myLng) {
		System.out.println(myLng);
		this.myLng = myLng;
	}
	public List<NameValuePair> getMyParams() {
		return myParams;
	}
	public void setMyParams(List<NameValuePair> params) {
		this.myParams = params;
	}
	
	public User(String string){
		webUrl = string;
	}
		
	public void makeLoc_SelfParams() throws UnsupportedEncodingException{
		// Request parameters and other properties.
		System.out.println("Gonn' make sum Loc_Self Params");
		myParams.clear();
		myParams.add(new BasicNameValuePair("id", getMyId()));
		myParams.add(new BasicNameValuePair("type", "loc_self"));
		myParams.add(new BasicNameValuePair("lat", Double.toString(getMyLat())));
		myParams.add(new BasicNameValuePair("long", Double.toString(getMyLng())));
		setMyParams(myParams);
		return;
	}
		
	//POST info naar Mitchell's server 
			public void POST() throws ClientProtocolException, IOException{
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(webUrl); //Mitchells URL
				List<NameValuePair> params = getMyParams();
				// Request parameters and other properties.
				httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

				//Execute and get the response.
				HttpResponse response = httpclient.execute(httppost);
				System.out.println("posted");
				HttpEntity entity = response.getEntity();
				if (entity != null) {
				    InputStream instream = entity.getContent();
				    try {
				        System.out.println(instream.toString());
				    } finally {
				        instream.close();
				    }
				}
			}
	

}
