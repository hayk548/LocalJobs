package com.example.localjobs;

public class Application {
    private String jobId;
    private String userId;
    private long timestamp;
    private String status;
    private String jobTitle;
    private String creatorId;

    public Application(String jobId, String userId, long timestamp, String status, String jobTitle, String creatorId) {
        this.jobId = jobId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.status = status;
        this.jobTitle = jobTitle;
        this.creatorId = creatorId;
    }

    // Empty constructor for Firestore
    public Application() {}

    // Getters
    public String getJobId() { return jobId; }
    public String getUserId() { return userId; }
    public long getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public String getJobTitle() { return jobTitle; }
    public String getCreatorId() { return creatorId; }

    // Setters (if needed)
    public void setJobId(String jobId) { this.jobId = jobId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setStatus(String status) { this.status = status; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
}