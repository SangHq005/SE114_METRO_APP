package com.example.metro_app.Domain;

import java.io.Serializable;

public class RouteModel implements Serializable {
    private String id;
    private String fromStation;
    private String toStation;
    private Double price;

    public RouteModel(String id, String fromStation, String toStation, Double price) {
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}