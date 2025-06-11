package com.example.metro_app.Model;

public class RawRoute {
    private int RouteId;
    private String RouteNo;
    private String RouteName;

    public int getRouteId() { return RouteId; }
    public String getRouteNo() { return RouteNo; }
    public String getRouteName() { return RouteName; }

    @Override
    public String toString() {
        return RouteNo + " - " + RouteName;
    }
}
