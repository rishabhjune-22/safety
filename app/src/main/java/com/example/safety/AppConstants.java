package com.example.safety;

import android.util.Log;

public class AppConstants {

    public static final String PREFS_NAME = "com.example.safety.PREFERENCES";
    public static final String OFFICE_GEOFENCE_ID = "OFFICE_GEOFENCE";
    public static final String HOME_GEOFENCE_ID = "HOME_GEOFENCE";

    public static final double OFFICE_LATITUDE = 21.210865;
    public static final double OFFICE_LONGITUDE = 81.322846;

    public static volatile Double HOME_LATITUDE = null;
    public static volatile Double HOME_LONGITUDE = null;

    public static final float GEOFENCE_RADIUS_IN_METERS = 100;

    public static void setHomeCoordinates(Double latitude, Double longitude) {
        HOME_LATITUDE = latitude;
        HOME_LONGITUDE = longitude;
        Log.d("AppConstants", "Home Latitude set to: " + HOME_LATITUDE + ", Home Longitude set to: " + HOME_LONGITUDE);
    }
}
