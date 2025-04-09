package com.example.nckh.model;

public class User {
    private String email;
    private String avatar;
    private String name;

    public User(String email, String avatar, String name) {
        this.email = email;
        this.avatar = avatar;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getName() {
        return name;
    }
} 