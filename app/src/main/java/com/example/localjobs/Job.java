package com.example.localjobs;

public class Job {
    private String jobId;
    private String title;
    private String description;
    private String location;
    private String salary;
    private String employerId;

    public Job() {}

    public Job(String jobId, String title, String description, String location, String salary, String employerId) {
        this.jobId = jobId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.salary = salary;
        this.employerId = employerId;
    }

    // Getters
    public String getJobId() { return jobId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getSalary() { return salary; }
    public String getEmployerId() { return employerId; }
}
