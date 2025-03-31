package com.example.localjobs;

public class ChatUser {
    private String userId;
    private String lastMessage;
    private String email;

    public ChatUser(String userId, String lastMessage) {
        this.userId = userId;
        this.lastMessage = lastMessage;
    }

    public String getUserId() {
        return userId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getEmail() {
        return email;
    }
}
