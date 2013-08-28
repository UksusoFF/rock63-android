package com.uksusoff.rock63.data.entities;

import java.io.Serializable;
import java.util.Date;

public class Event implements Serializable {
    
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private int id;
	private String title;
	private String body;
	private Date start;
	private Date end;
	private int placeId;
	private String mediumThumbUrl;
	
    private Place place;
	
    public String getMediumThumbUrl() {
        return mediumThumbUrl;
    }
    public void setMediumThumbUrl(String mediumThumbUrl) {
        this.mediumThumbUrl = mediumThumbUrl;
    }
	public Place getPlace() {
        return place;
    }
    public void setPlace(Place place) {
        this.place = place;
    }
    public int getPlaceId() {
		return placeId;
	}
	public void setPlaceId(int placeId) {
		this.placeId = placeId;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
}
