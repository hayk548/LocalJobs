package com.example.localjobs;

public class User {

    private String userId;
    private String username;
    private String profileImage;

    public User() {

    }

    public User(String userId, String username, String profileImage) {
        this.userId = userId;
        this.username = username;
        this.profileImage = profileImage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
