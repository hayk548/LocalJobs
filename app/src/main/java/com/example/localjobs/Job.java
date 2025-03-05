package com.example.localjobs;

public class Job {
    private String jobId, title, description, location, salary, userId;

    public Job() {} // Empty constructor for Firestore

    public Job(String jobId, String title, String description, String location, String salary, String userId) {
        this.jobId = jobId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.salary = salary;
        this.userId = userId;
    }

    public String getJobId() { return jobId; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getSalary() { return salary; }
}
