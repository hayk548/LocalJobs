package com.example.localjobs;

public class ChatUser {
    private String userEmail;
    private String lastMessage;

    public ChatUser(String userEmail, String lastMessage) {
        this.userEmail = userEmail;
        this.lastMessage = lastMessage;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getLastMessage() {
        return lastMessage;
    }
}