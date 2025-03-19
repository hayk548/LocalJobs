package com.example.localjobs;

public class ChatUser {
    private String userId;
    private String lastMessage;

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
}
