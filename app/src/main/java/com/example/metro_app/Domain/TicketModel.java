package com.example.metro_app.Domain;

public class TicketModel {
    private String Price;

    public TicketModel(String Price) {
        this.Price = Price;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String Price) {
        this.Price = Price;
    }
}
