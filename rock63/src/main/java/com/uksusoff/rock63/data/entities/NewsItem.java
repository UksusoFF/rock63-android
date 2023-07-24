package com.uksusoff.rock63.data.entities;

import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

public class NewsItem {

    @DatabaseField(id = true)
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
    @DatabaseField
    String url;

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
        return "news_" + id;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
