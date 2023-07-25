package com.uksusoff.rock63.data.entities;

import com.j256.ormlite.field.DatabaseField;
import com.uksusoff.rock63.utils.StringUtils;

import java.util.Date;

public class NewsItem {

    @DatabaseField(id = true)
    public int id;
    @DatabaseField
    public String title;
    @DatabaseField
    public String body;
    @DatabaseField
    public String ext;
    @DatabaseField
    public Date date;
    @DatabaseField
    public String thumbnailSmall;
    @DatabaseField
    public String thumbnailMiddle;
    @DatabaseField
    public boolean isNew;
    @DatabaseField
    public String url;

    public String getShortDescriptionText() {
        return StringUtils.crop(StringUtils.cleanHtml(this.body), 50, true);
    }

    public String getDescriptionText() {
        return this.body + this.ext;
    }

    public String getShareSubject() {
        return this.title + "\n\n" + StringUtils.fromHtml(this.body);
    }

    public String getShareText() {
        return this.url;
    }
}
