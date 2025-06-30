package com.example.metro_app.Model;

import com.mapbox.geojson.Point;

import java.util.List;
import java.util.Map;

public class RouteResponse {
    public List<Detail> detail;
    public List<Stop> stops;
    public Map<String, List<APIcoord>> coordRoute;
    public String Title;
    public String Desc;

    public List<Detail> getDetail() {
        return detail;
    }

    public void setDetail(List<Detail> detail) {
        this.detail = detail;
    }

    public List<Stop> getStops() {
        return stops;
    }

    public void setStops(List<Stop> stops) {
        this.stops = stops;
    }

    public Map<String, List<APIcoord>> getCoordRoute() {
        return coordRoute;
    }

    public void setCoordRoute(Map<String, List<APIcoord>> coordRoute) {
        this.coordRoute = coordRoute;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }
}