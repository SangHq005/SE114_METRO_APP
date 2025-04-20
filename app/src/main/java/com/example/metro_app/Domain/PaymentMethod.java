package com.example.metro_app.Domain;

public class PaymentMethod {
    public String name;
    public String subtitle;
    public int iconResId;

    public PaymentMethod(String name, String subtitle, int iconResId) {
        this.name = name;
        this.subtitle = subtitle;
        this.iconResId = iconResId;
    }
}
