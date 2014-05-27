package crepetete.arcgis.evemapp;

import java.io.Serializable;

import android.widget.BaseAdapter;

public class Friend implements Serializable{
	
	//Object om een vriend te representeren. Via getters en setters wordt dit object aangemaakt, om vervolgens in een lijst te stoppen en via die lijst in de listview terecht te komen.
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String name;
	private String birthday;
	private String work;

	private BaseAdapter adapter;
	
	
	public Friend(String name, String birthday, String id, String work) {
		super();
		this.name = name;
		this.birthday = birthday;
		this.id = id;
		this.work = work;
	}
	
	public String getWork() {
		return work;
	}
	public void setWork(String work) {
		this.work = work;
		if (adapter != null) {
		    adapter.notifyDataSetChanged();
		}
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
		if (adapter != null) {
		    adapter.notifyDataSetChanged();
		}
	}
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
		if (adapter != null) {
		    adapter.notifyDataSetChanged();
		}
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
		if (adapter != null) {
		    adapter.notifyDataSetChanged();
		}
	}
	public void setAdapter(BaseAdapter adapter) {
	    this.adapter = adapter;
	}

}
