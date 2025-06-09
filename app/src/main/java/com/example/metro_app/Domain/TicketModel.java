package com.example.metro_app.Domain;

import java.io.Serializable;
import java.util.Date;

public class TicketModel implements Serializable {
    private String ticketId;
    private String ticketType;
    private String price;
    private String expireDate;
    private String status;
    private Date issueDate;
    private String userName;
    private String ticketCode;
    private String expirationDate;
    private String userId; // Thêm field userId

    public TicketModel(String ticketId, String ticketType, String price, String expireDate, String status, Date issueDate, String userName, String ticketCode, String expirationDate, String userId) {
        this.ticketId = ticketId;
        this.ticketType = ticketType;
        this.price = price;
        this.expireDate = expireDate;
        this.status = status;
        this.issueDate = issueDate;
        this.userName = userName;
        this.ticketCode = ticketCode;
        this.expirationDate = expirationDate;
        this.userId = userId;
    }

    public TicketModel(String ticketType, String price, String expireDate, String status) {
        this.ticketType = ticketType;
        this.price = price;
        this.expireDate = expireDate;
        this.status = status;
    }

    public String getTicketId() {
        return ticketId;
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

    public String getStatus() {
        return status;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public String getUserName() {
        return userName;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setId(String id) { this.ticketId = id;
    }
}