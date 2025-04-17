package com.example.metro_app.Domain;

public class UserModel {
    private String Username;

    public UserModel(String Username){
        this.Username = Username;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String Username) {
        this.Username = Username;
    }
}
