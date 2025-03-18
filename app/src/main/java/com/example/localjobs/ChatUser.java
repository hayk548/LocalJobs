package com.example.localjobs;

public class ChatUser {
    private String userId;
    private String username;

    public ChatUser() {
        // Empty constructor for Firebase
    }

    public ChatUser(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
