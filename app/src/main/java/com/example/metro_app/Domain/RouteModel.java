package com.example.metro_app.Domain;

public class RouteModel {
    private String fromStation;
    private String toStation;
    private String fromTime;
    private String toTime;

    public RouteModel(String fromStation, String toStation, String fromTime, String toTime) {
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.fromTime = fromTime;
        this.toTime = toTime;
    }

    // Getters and Setters
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

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.toTime = toTime;
    }
}
