package com.uksusoff.rock63.data.entities;

import java.io.Serializable;
import java.util.Date;

import android.graphics.Bitmap;

public class NewsItem implements Serializable {
	
	private int id;
	private String title;
	private String body;
	private Date date;
	private String smallThumbUrl;
	private String mediumThumbUrl;
    private boolean isNew;
	
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
