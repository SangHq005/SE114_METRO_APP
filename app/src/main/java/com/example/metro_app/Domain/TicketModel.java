package com.example.metro_app.Domain;

import java.io.Serializable;

public class TicketModel implements Serializable {
    private String ticketType;
    private String price;
    private String expireDate;

    public TicketModel(String ticketType, String price, String expireDate) {
        this.ticketType = ticketType;
        this.price = price;
        this.expireDate = expireDate;
    }

    public String getTicketType() {
        return ticketType;
    }

    public String getPrice() {
        return price;
    }

    public String getExpireDate() {
        return expireDate;
    }
}