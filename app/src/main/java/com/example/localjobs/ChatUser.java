package com.example.localjobs;

public class ChatUser {
    private String chatId;      // Added to identify the chat
    private String userId;      // Receiver's UID
    private String username;    // Username to display
    private String lastMessage;
    private String jobTitle;

    public ChatUser(String chatId, String userId, String username, String lastMessage, String jobTitle) {
        this.chatId = chatId;
        this.userId = userId;
        this.username = username;
        this.lastMessage = lastMessage;
        this.jobTitle = jobTitle;
    }

    public String getChatId() { return chatId; }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getLastMessage() { return lastMessage; }
    public String getJobTitle() { return jobTitle; }
}