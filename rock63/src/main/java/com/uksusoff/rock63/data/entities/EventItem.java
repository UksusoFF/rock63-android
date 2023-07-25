package com.uksusoff.rock63.data.entities;

import com.j256.ormlite.field.DatabaseField;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventItem {

    @DatabaseField(id = true)
    public int id;
    @DatabaseField
    public String title;
    @DatabaseField
    public String body;
    @DatabaseField
    public Date start;
    @DatabaseField
    public Date end;
    @DatabaseField
    public String imageUrl;
    @DatabaseField
    public String url;
    @DatabaseField
    public boolean notify;
    @DatabaseField(foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    VenueItem venueItem;

    public VenueItem getVenueItem() {
        return venueItem;
    }

    public void setVenueItem(VenueItem venueItem) {
        this.venueItem = venueItem;
    }

    public String getShareText() {
        return this.getVenueItem() != null
                ? String.format("%s @ %s", this.title, this.getVenueItem().title)
                : this.title;
    }

    public String getDateRange() {
        SimpleDateFormat fDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        SimpleDateFormat fTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

        if (this.end == null) {
            return String.format(
                    "%s %s",
                    fDate.format(this.start),
                    fTime.format(this.start)
            );
        }

        return String.format(
                "%s %s - %s %s",
                fDate.format(this.start),
                fTime.format(this.start),
                fDate.format(this.end),
                fTime.format(this.end)
        );
    }
}
