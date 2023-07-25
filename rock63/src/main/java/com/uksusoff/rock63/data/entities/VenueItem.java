package com.uksusoff.rock63.data.entities;

import android.text.Spanned;

import com.j256.ormlite.field.DatabaseField;
import com.uksusoff.rock63.utils.StringUtils;

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

    public Spanned getMapAddress() {
        String code = String.format(
                "<a href=\"geo:%s,%s\" target=\"_blank\">%s</a>",
                this.latitude,
                this.longitude,
                this.address
        );

        return StringUtils.fromHtml(code);
    }
}
