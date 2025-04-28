package com.example.metro_app.Model;

public class UserModel {
    public String uid;
    public String email;
    public String name;
    public String role;

    public UserModel() {}

    public UserModel(String uid, String email, String name, String role) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.role = role;
    }
}
