package com.example.metro_app.Model;

import com.mapbox.geojson.Point;

import java.util.List;
import java.util.Map;

public class RouteResponse {
    public List<Detail> detail;
    public List<Stop> stops;
    public Map<String, List<RawCoor>> coordRoute;
    public String Title;
    public String Desc;
}