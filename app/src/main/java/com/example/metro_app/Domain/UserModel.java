package com.example.metro_app.Domain;

import java.io.Serializable;

public class UserModel implements Serializable {
    private String fullName;
    private String email;
    private String phoneNumber;

    public UserModel(String fullName, String email, String phoneNumber) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // Constructor for backward compatibility with existing data
    public UserModel(String fullName) {
        this.fullName = "";
        this.email = "";
        this.phoneNumber = "";
    }
    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}