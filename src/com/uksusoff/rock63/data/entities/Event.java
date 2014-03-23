package com.uksusoff.rock63.data.entities;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;

public class Event {
        
    @DatabaseField(generatedId = true)
    int id;
    @DatabaseField
	String title;
    @DatabaseField
    String body;
    @DatabaseField
	Date start;
    @DatabaseField
	Date end;
    @DatabaseField
	int placeId;
    @DatabaseField
	String mediumThumbUrl;
    @DatabaseField
	String url;
    @DatabaseField(foreign = true,foreignAutoCreate = true,foreignAutoRefresh = true)
    Place place;
	
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
	public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
}
