package com.example.metro_app.Model;

import java.io.Serializable;

public class TicketType implements Serializable {
    private String id;
    private String name;
    private String price;
    private String active; // Số ngày kích hoạt
    private String autoActive; // Số ngày tự động kích hoạt
    private String status;

    public TicketType() {}

    public TicketType(String id, String name, String price, String active, String autoActive, String status) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.active = active;
        this.autoActive = autoActive;
        this.status = status;
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
}