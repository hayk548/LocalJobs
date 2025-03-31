package com.example.localjobs;

public class ChatMetadata {
    private String chatId;
    private String senderEmail;
    private String receiverEmail;
    private String lastMessage;
    private long timestamp;

    public ChatMetadata() {} // Empty constructor for Firestore

    public ChatMetadata(String chatId, String senderEmail, String receiverEmail, String lastMessage, long timestamp) {
        this.chatId = chatId;
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    public String getChatId() { return chatId; }
    public String getSenderEmail() { return senderEmail; }
    public String getReceiverEmail() { return receiverEmail; }
    public String getLastMessage() { return lastMessage; }
    public long getTimestamp() { return timestamp; }
}
