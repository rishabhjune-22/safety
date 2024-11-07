package com.example.safety;

import com.google.firebase.database.PropertyName;

public class User {
    private String name;
    private String profileImageUrl;
    private String last_seen;

    public User() {
        // Empty constructor needed for Firestore deserialization
    }

    public User(String name, String profileImageUrl,String last_seen) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.last_seen =last_seen;
    }

    public String getName() {
        return name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }



    @PropertyName("last_seen")
    public String getLastSeen() {
        return last_seen;
    }


    @PropertyName("last_seen")
    public void setLastSeen(String last_seen) {
        this.last_seen = last_seen;
    }
}
