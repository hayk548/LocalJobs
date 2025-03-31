package com.example.localjobs;

public class ChatMessage {
    private String senderId;
    private String senderEmail;
    private String receiverId;
    private String receiverEmail;
    private String message;
    private long timestamp;

    public ChatMessage() {} // Empty constructor for Firestore

    public ChatMessage(String senderId, String senderEmail, String receiverId, String receiverEmail, String message, long timestamp) {
        this.senderId = senderId;
        this.senderEmail = senderEmail;
        this.receiverId = receiverId;
        this.receiverEmail = receiverEmail;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }
    public String getSenderEmail() { return senderEmail; }
    public String getReceiverId() { return receiverId; }
    public String getReceiverEmail() { return receiverEmail; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}
