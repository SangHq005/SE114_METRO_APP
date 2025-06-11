package com.example.metro_app.Model;

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

    public int getRouteId() { return RouteId; }
    public int getRouteVarId() { return RouteVarId; }
    public String getRouteVarName() { return RouteVarName; }
    public boolean isOutbound() { return Outbound; }

    // Thêm getter khác nếu cần hiển thị
}
