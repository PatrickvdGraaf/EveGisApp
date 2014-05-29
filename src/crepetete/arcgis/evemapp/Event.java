package crepetete.arcgis.evemapp;

import java.io.Serializable;
import java.util.Date;

import android.graphics.drawable.Drawable;
import android.widget.BaseAdapter;

public class Event implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;
	private Date date;
	private Drawable image;
	
	private BaseAdapter adapter;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		if (adapter != null) {
		    adapter.notifyDataSetChanged();
		}
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
		if (adapter != null) {
		    adapter.notifyDataSetChanged();
		}
	}
	
	public Drawable getImage() {
		return image;
	}

	public void setImage(Drawable image) {
		this.image= image;
		if (adapter != null) {
		    adapter.notifyDataSetChanged();
		}
	}

	public void setAdapter(BaseAdapter adapter) {
	    this.adapter = adapter;
	}
}
