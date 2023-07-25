package com.uksusoff.rock63.data.entities;

import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

public class EventItem {

    @DatabaseField(id = true)
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
    String mediumThumbUrl;
    @DatabaseField
    String url;
    @DatabaseField(foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    VenueItem venueItem;
    @DatabaseField
    boolean notify;

    public String getMediumThumbUrl() {
        return mediumThumbUrl;
    }

    public void setMediumThumbUrl(String mediumThumbUrl) {
        this.mediumThumbUrl = mediumThumbUrl;
    }

    public VenueItem getVenueItem() {
        return venueItem;
    }

    public void setVenueItem(VenueItem venueItem) {
        this.venueItem = venueItem;
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

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public String getShareText() {
        return this.getVenueItem() != null
                ? String.format("%s @ %s", this.getTitle(), this.getVenueItem().title)
                : this.getTitle();
    }
}
