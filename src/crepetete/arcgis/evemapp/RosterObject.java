package crepetete.arcgis.evemapp;

import java.util.Date;

import android.widget.BaseAdapter;

public class RosterObject {

	// Een Rosterobject bevat de informatie van een bepaalde performer om een
	// bepaalde tijd bij een Stage. Een Stage heeft een lijst met
	// RosterObjecten, zodat een Rooster weergegeven kan worden per podium.

	private String performer;
	private Date startTime;
	private Date endTime;
	private BaseAdapter adapter;

	public String getPerformer() {
		return performer;
	}

	public void setPerformer(String performer) {
		this.performer = performer;
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	public void setAdapter(BaseAdapter adapter) {
		this.adapter = adapter;
	}
}
