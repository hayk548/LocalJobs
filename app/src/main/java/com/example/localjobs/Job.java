package com.example.localjobs;

public class Job {
    private String jobId, title, description, location, date, userId;
    private double latitude, longitude;

    public Job() {}

    public Job(String jobId, String title, String description, String location, String date, String userId, double latitude, double longitude) {
        this.jobId = jobId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getJobId() { return jobId; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
}
