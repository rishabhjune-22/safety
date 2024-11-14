package com.example.safety;

import android.util.Log;

public class AppConstants {
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_NAME = "name";
    public static final String KEY_PROFILE_IMAGE_URL = "profileImageUrl";
    public static final String KEY_EMAIL = "email";
    public static final String PREFS_NAME = "com.example.safety.PREFERENCES";
    public static final String KEY_OFFICE_GEOFENCE_ID = "OFFICE_GEOFENCE";
    public static final String KEY_HOME_GEOFENCE_ID = "HOME_GEOFENCE";

    public static final double OFFICE_LATITUDE = 21.210865;
    public static final double OFFICE_LONGITUDE = 81.322846;

    public static volatile Double HOME_LATITUDE = null;
    public static volatile Double HOME_LONGITUDE = null;

    public static final float GEOFENCE_RADIUS_IN_METERS = 100;

    public static final String KEY_HOME_GEO_COORDINATES = "homeGeoCoordinates";
    public static final String KEY_WORK_GEO_COORDINATES = "workGeoCoordinates";
    public static final String KEY_GEOFENCE_RADIUS_IN_METERS = "geofenceRadiusInMeters";



    public static void setHomeCoordinates(Double latitude, Double longitude) {
        HOME_LATITUDE = latitude;
        HOME_LONGITUDE = longitude;
        Log.d("AppConstants", "Home Latitude set to: " + HOME_LATITUDE + ", Home Longitude set to: " + HOME_LONGITUDE);
    }
}
