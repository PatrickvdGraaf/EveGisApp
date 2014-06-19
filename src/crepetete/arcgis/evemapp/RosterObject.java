package crepetete.arcgis.evemapp;

import java.util.Date;
import java.util.List;

public class RosterObject {
	private String performer;
	private Date startTime;
	private Date endTime;
	
	
	public String getPerformer() {
		return performer;
	}
	public void setPerformer(String performer) {
		this.performer = performer;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}	
}
