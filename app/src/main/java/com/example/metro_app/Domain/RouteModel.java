package com.example.metro_app.Domain;

import java.io.Serializable;

public class RouteModel implements Serializable {
    private String id; // Added to store Firestore document ID
    private String fromStation;
    private String toStation;
    private String price;

    public RouteModel(String id, String fromStation, String toStation, String price) {
        this.id = id;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.price = price;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromStation() {
        return fromStation;
    }

    public void setFromStation(String fromStation) {
        this.fromStation = fromStation;
    }

    public String getToStation() {
        return toStation;
    }

    public void setToStation(String toStation) {
        this.toStation = toStation;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}