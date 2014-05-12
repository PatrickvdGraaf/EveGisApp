package crepetete.arcgis.evemapp;

import java.io.IOException;
import java.io.InputStream;
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
	private int myId = 10001; //Dummy ID
	private double myLat;
	private double myLng;
	
	public int getMyId() {
		return myId;
	}
	public void setMyId(int myId) {
		this.myId = myId;
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

	//POST info naar Mitchell's server 
		public void POST() throws ClientProtocolException, IOException{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://web.insidion.com/request/index.php"); //Mitchells URL

			// Request parameters and other properties.
			List<NameValuePair> params = new ArrayList<NameValuePair>(4);
			params.add(new BasicNameValuePair("id", Integer.toString(getMyId())));
			params.add(new BasicNameValuePair("type", "loc_self"));
			params.add(new BasicNameValuePair("lat", Double.toString(getMyLat())));
			params.add(new BasicNameValuePair("long", Double.toString(getMyLng())));
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
