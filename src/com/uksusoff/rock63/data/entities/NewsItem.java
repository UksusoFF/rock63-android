package com.uksusoff.rock63.data.entities;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;

public class NewsItem {
	    
    @DatabaseField(generatedId = true)
    int id;
    @DatabaseField
	String title;
    @DatabaseField
	String body;
    @DatabaseField
	Date date;
    @DatabaseField
	String smallThumbUrl;
    @DatabaseField
	String mediumThumbUrl;
    @DatabaseField
    boolean isNew;
	
	public String getSmallThumbUrl() {
        return smallThumbUrl;
    }
    public void setSmallThumbUrl(String smallThumbUrl) {
        this.smallThumbUrl = smallThumbUrl;
    }
    public String getMediumThumbUrl() {
        return mediumThumbUrl;
    }
    public void setMediumThumbUrl(String mediumThumbUrl) {
        this.mediumThumbUrl = mediumThumbUrl;
    }
	public boolean isNew() {
		return isNew;
	}
	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}
	public String getImageCacheName() {
        return "news_" + Integer.toString(id);
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
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
}
