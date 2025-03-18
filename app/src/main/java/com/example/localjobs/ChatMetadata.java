package com.example.localjobs;

public class ChatMetadata {
    private String chatId;
    private String senderId;
    private String receiverId;
    private String lastMessage;
    private long timestamp;

    public ChatMetadata() {
        // Default constructor for Firestore
    }

    public ChatMetadata(String chatId, String senderId, String receiverId, String lastMessage, long timestamp) {
        this.chatId = chatId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
