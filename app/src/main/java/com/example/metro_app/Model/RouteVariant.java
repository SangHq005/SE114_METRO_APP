package com.example.metro_app.Model;

import com.mapbox.geojson.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteVariant {
    private int RouteId;
    private int RouteVarId;
    private String RouteVarName;
    private String RouteVarShortName;
    private String RouteNo;
    private String StartStop;
    private String EndStop;
    private boolean Outbound;
    private double Distance;
    private int RunningTime;
    private List<BusStop> Stops;
    private List<RawCoor> Path;

    // ----- Getter & Setter -----

    public int getRouteId() {
        return RouteId;
    }

    public void setRouteId(int routeId) {
        RouteId = routeId;
    }

    public int getRouteVarId() {
        return RouteVarId;
    }

    public void setRouteVarId(int routeVarId) {
        RouteVarId = routeVarId;
    }

    public String getRouteVarName() {
        return RouteVarName;
    }

    public void setRouteVarName(String routeVarName) {
        RouteVarName = routeVarName;
    }

    public String getRouteVarShortName() {
        return RouteVarShortName;
    }

    public void setRouteVarShortName(String routeVarShortName) {
        RouteVarShortName = routeVarShortName;
    }

    public String getRouteNo() {
        return RouteNo;
    }

    public void setRouteNo(String routeNo) {
        RouteNo = routeNo;
    }

    public String getStartStop() {
        return StartStop;
    }

    public void setStartStop(String startStop) {
        StartStop = startStop;
    }

    public String getEndStop() {
        return EndStop;
    }

    public void setEndStop(String endStop) {
        EndStop = endStop;
    }

    public boolean isOutbound() {
        return Outbound;
    }

    public void setOutbound(boolean outbound) {
        Outbound = outbound;
    }

    public double getDistance() {
        return Distance;
    }

    public void setDistance(double distance) {
        Distance = distance;
    }

    public int getRunningTime() {
        return RunningTime;
    }

    public void setRunningTime(int runningTime) {
        RunningTime = runningTime;
    }

    public List<BusStop> getStops() {
        return Stops;
    }

    public void setStops(List<BusStop> stops) {
        Stops = stops;
    }

    public List<RawCoor> getPath() {
        return Path;
    }

    public void setPath(List<RawCoor> path) {
        Path = path;
    }

    // ----- Serialize for Firestore -----
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("RouteId", RouteId);
        map.put("RouteVarId", RouteVarId);
        map.put("RouteVarName", RouteVarName);
        map.put("RouteVarShortName", RouteVarShortName);
        map.put("RouteNo", RouteNo);
        map.put("StartStop", StartStop);
        map.put("EndStop", EndStop);
        map.put("Outbound", Outbound);
        map.put("Distance", Distance);
        map.put("RunningTime", RunningTime);
        map.put("Stops", Stops);
        map.put("Path", Path); // RawCoor is serializable directly
        return map;
    }

    // ----- Utility Methods -----

    // Convert from RawCoor to Point for Mapbox display
    public List<Point> getPointPath() {
        List<Point> result = new ArrayList<>();
        if (Path != null) {
            for (RawCoor c : Path) {
                result.add(Point.fromLngLat(c.lng, c.lat));
            }
        }
        return result;
    }

    public void setPathFromPointList(List<Point> pointList) {
        List<RawCoor> rawList = new ArrayList<>();
        for (Point p : pointList) {
            RawCoor c = new RawCoor();
            c.lat = p.latitude();
            c.lng = p.longitude();
            rawList.add(c);
        }
        this.Path = rawList;
    }
}
