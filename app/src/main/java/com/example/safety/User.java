package com.example.safety;

public class User {
    private String name;
    private String profileImageUrl;

    public User() {
        // Empty constructor needed for Firestore deserialization
    }

    public User(String name, String profileImageUrl) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }

    public String getName() {
        return name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
