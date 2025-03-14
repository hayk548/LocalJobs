package com.example.localjobs;

public class Job {
    private String jobId;
    private String title;
    private String description;
    private String location;
    private String category;
    private String date;
    private String userId;
    private double latitude;
    private double longitude;

    public Job() {
    }

    public Job(String jobId, String title, String description, String location, String date,
               String userId, double latitude, double longitude, String category) {
        this.jobId = jobId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
