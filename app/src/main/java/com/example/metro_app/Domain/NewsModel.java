package com.example.metro_app.Domain;

import java.io.Serializable;

public class NewsModel implements Serializable {
    private String date;
    private String description;
    private String pic;
    private String title;
    private String userid;
    private String status;
    private String documentId;

    public NewsModel() {}

    public NewsModel(String date, String description, String pic, String title, String userid, String status) {
        this.date = date;
        this.description = description;
        this.pic = pic;
        this.title = title;
        this.userid = userid;
        this.status = status;
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

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}