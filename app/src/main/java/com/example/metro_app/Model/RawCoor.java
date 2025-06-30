package com.example.metro_app.Model;

public class RawCoor {
    public double lat;
    public double lng;
    public RawCoor() {}
    public RawCoor(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
    @Override
    public String toString() {
        return "RawCoor{lat=" + lat + ", lng=" + lng + "}";
    }

}
