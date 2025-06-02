package com.example.metro_app.Model;

import java.io.Serializable;

public class TicketType implements Serializable {
    private String id;
    private String name;
    private String price;
    private String active;
    private String autoActive;
    private String status;
    private String type;
    private String startStation;
    private String endStation;

    public TicketType() {}

    // Constructor for long-term ticket
    public TicketType(String id, String name, String price, String active, String autoActive, String status) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.active = active;
        this.autoActive = autoActive;
        this.status = status;
        this.type = "Vé dài hạn";
    }

    // Constructor for single-trip ticket
    public TicketType(String id, String startStation, String endStation, String price, String name) {
        this.id = id;
        this.startStation = startStation;
        this.endStation = endStation;
        this.price = price;
        this.name = name;
        this.active = "0";
        this.autoActive = "30";
        this.type = "Vé lượt";
    }

    public TicketType(String id, String name, String price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getActive() { return active; }
    public void setActive(String active) { this.active = active; }
    public String getAutoActive() { return autoActive; }
    public void setAutoActive(String autoActive) { this.autoActive = autoActive; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStartStation() { return startStation; }
    public void setStartStation(String startStation) { this.startStation = startStation; }
    public String getEndStation() { return endStation; }
    public void setEndStation(String endStation) { this.endStation = endStation; }
}