package com.example.metro_app.Model;

import java.io.Serializable;

public class UserModel implements Serializable {
    private String uid;
    private String Email;
    private String Name;
    private String Role;
    private String CCCD;
    private String avatarUrl;

    public UserModel() {}

    // Getter/Setter
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return Name; }
    public void setName(String name) { this.Name = name; }

    public String getEmail() { return Email; }
    public void setEmail(String email) { this.Email = email; }

    public String getRole() { return Role; }
    public void setRole(String role) { this.Role = role; }

    public String getCCCD() { return CCCD; }
    public void setCCCD(String CCCD) { this.CCCD = CCCD; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}