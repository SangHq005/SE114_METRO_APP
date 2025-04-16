package com.example.metro_app.Domain;

import java.io.Serializable;
import java.util.ArrayList;

public class NewsModel implements Serializable {
    private String date;
    private String description;
    private String pic;
    private String title;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    private String header;
    private String content;
    public NewsModel() {
    }

    public NewsModel(String date, String description, String pic, String title, String header, String content) {
        this.date = date;
        this.description = description;
        this.pic = pic;
        this.title = title;
        this.header = header;
        this.content = content;
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

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }
}
