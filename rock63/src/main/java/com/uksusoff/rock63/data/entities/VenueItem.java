package com.uksusoff.rock63.data.entities;

import com.j256.ormlite.field.DatabaseField;

public class VenueItem {

    @DatabaseField(id = true)
    public int id;
    @DatabaseField
    public String title;
    @DatabaseField
    public String address;
    @DatabaseField
    public String url;
    @DatabaseField
    public String phone;
    @DatabaseField
    public String vk;
    @DatabaseField
    public String latitude;
    @DatabaseField
    public String longitude;
}
