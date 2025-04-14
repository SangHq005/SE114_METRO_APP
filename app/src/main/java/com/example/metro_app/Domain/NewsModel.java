package com.example.metro_app.Domain;

import java.io.Serializable;
import java.util.ArrayList;

public class NewsModel implements Serializable {
    private String title;
    private String description;
    private String date;
    private ArrayList<String> pic;

    public NewsModel() {
    }

    public NewsModel(String title, String description, String date, ArrayList<String> pic) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.pic = pic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<String> getPic() {
        return pic;
    }

    public void setPic(ArrayList<String> pic) {
        this.pic = pic;
    }
}
