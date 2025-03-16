package com.example.localjobs;

public class Application {
    private String jobId;
    private String userId;
    private long appliedDate;
    private String status;

    public Application(String jobId, String userId, long appliedDate, String status) {
        this.jobId = jobId;
        this.userId = userId;
        this.appliedDate = appliedDate;
        this.status = status;
    }

    public String getJobId() {
        return jobId;
    }

    public String getUserId() {
        return userId;
    }

    public long getAppliedDate() {
        return appliedDate;
    }

    public String getStatus() {
        return status;
    }
}
