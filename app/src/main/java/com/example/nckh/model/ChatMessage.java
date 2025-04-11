package com.example.nckh.model;

public class ChatMessage {
    private String message;
    private boolean isUser;

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isUserMessage() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }
} 