package crepetete.arcgis.evemapp;

import java.util.HashMap;
import java.util.Map;

public class User {	
	private String myId; //Dummy ID
	private String name;
	private double myLat;
	private double myLng;
	private Map<String, String> myParams = new HashMap<String, String>();

	
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
	
}
