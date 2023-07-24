package com.uksusoff.rock63.data.entities;

import com.j256.ormlite.field.DatabaseField;

public class Place {

    @DatabaseField(id = true)
    int id;
    @DatabaseField
    String name;
    @DatabaseField
    String address;
    @DatabaseField
    String url;
    @DatabaseField
    String phone;
    @DatabaseField
    String vkUrl;
    @DatabaseField
    String mapImageUrl;

    public String getMapImageUrl() {
        return mapImageUrl;
    }

    public void setMapImageUrl(String mapImageUrl) {
        this.mapImageUrl = mapImageUrl;
    }

    public String getVkUrl() {
        return vkUrl;
    }

    public void setVkUrl(String vkUrl) {
        this.vkUrl = vkUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
