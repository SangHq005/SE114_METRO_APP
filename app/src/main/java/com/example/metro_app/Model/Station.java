package com.example.metro_app.Model;

import java.io.Serializable;

public class Station implements Serializable {
    public int StopId;
    public String Code;
    public String Name;
    public String StopType;
    public String Zone;
    public String Ward;
    public String AddressNo;
    public String Street;
    public String SupportDisability;
    public String Status;
    public double Lng;
    public double Lat;
    public String Search;
    public String Routes;

    public Station() {}
}
