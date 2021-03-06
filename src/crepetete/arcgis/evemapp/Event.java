package crepetete.arcgis.evemapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.widget.BaseAdapter;

public class Event implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Onze eigen Event class is een container met de nodige informatie. Dit
	// event wordt geselecteerd in de EventList, en aan de hand
	// daarvan wordt de info hier opgevuld. Vooral de lijst met FestivalObjecten
	// wordt vaak gebruikt.

	private String name;
	private String id;
	private String description;
	private String start_date;
	private String start_date_timezone_type;
	private String start_date_timezone;
	private String end_date;
	private String end_date_timezone_type;
	private String end_date_timezone;
	private Drawable image;
	private BaseAdapter adapter;
	private List<FestivalObject> objects;

	public Event() {
		objects = new ArrayList<FestivalObject>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<FestivalObject> getObjects() {
		return objects;
	}

	public void setObjects(List<FestivalObject> objects) {
		this.objects = objects;
	}

	public String getStart_date_timezone_type() {
		return start_date_timezone_type;
	}

	public void setStart_date_timezone_type(String start_date_timezone_type) {
		this.start_date_timezone_type = start_date_timezone_type;
	}

	public String getStart_date_timezone() {
		return start_date_timezone;
	}

	public void setStart_date_timezone(String start_date_timezone) {
		this.start_date_timezone = start_date_timezone;
	}

	public String getEnd_date_timezone_type() {
		return end_date_timezone_type;
	}

	public void setEnd_date_timezone_type(String end_date_timezone_type) {
		this.end_date_timezone_type = end_date_timezone_type;
	}

	public String getEnd_date_timezone() {
		return end_date_timezone;
	}

	public void setEnd_date_timezone(String end_date_timezone) {
		this.end_date_timezone = end_date_timezone;
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

	public Drawable getImage() {
		return image;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStart_date() {
		return start_date;
	}

	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}

	public String getEnd_date() {
		return end_date;
	}

	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}

	public void setImage(Drawable image) {
		this.image = image;
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	public void setAdapter(BaseAdapter adapter) {
		this.adapter = adapter;
	}
}
