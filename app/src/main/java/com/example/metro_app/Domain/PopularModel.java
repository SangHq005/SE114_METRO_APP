package com.example.metro_app.Domain;

import java.io.Serializable;

public class PopularModel implements Serializable {
    private String date;
    private String description;
    private String pic;
    private String title;

    public PopularModel() {
    }

    public PopularModel(String date, String description, String pic, String title) {
        this.date = date;
        this.description = description;
        this.pic = pic;
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
