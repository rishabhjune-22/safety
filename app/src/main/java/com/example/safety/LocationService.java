package com.example.safety;

import static android.content.ContentValues.TAG;

import android.content.SharedPreferences;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


import java.util.Arrays;


public class LocationService extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    private double homeLatitude;
    private double homeLongitude;
    private String homeGeofenceId;
    private double officeLatitude;
    private double officeLongitude;
    private String officeGeofenceId;
    private float geofenceRadius;


    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1, createNotification());
        Log.d("LocationService", "Service started");

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);
        loadGeofenceDataFromPreferences();
        // Create a LocationCallback to receive location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        Log.d("LocationService", "Location received: " + location.getLatitude() + ", " + location.getLongitude());
                        updateLocationInFirebase(location);
                    }
                } else {
                    Log.d("LocationService", "No location received in location callback.");
                }
            }
        };

        // Start location updates
        startLocationUpdates();
        addGeofences();

        // Start the service as a foreground service

    }
    private void loadGeofenceDataFromPreferences() {
        SharedPreferences preferences = getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        try {
        // Extract home coordinates
        String homeGeoCoordinates = preferences.getString(AppConstants.KEY_HOME_GEO_COORDINATES, "Lat: 0.0, Long: 0.0");
        String[] homeParts = homeGeoCoordinates.split(",");
        homeLatitude = parseCoordinate(homeParts[0], "Lat: ");
        homeLongitude = parseCoordinate(homeParts[1], "Long: ");
        homeGeofenceId = preferences.getString(AppConstants.KEY_HOME_GEOFENCE_ID, "HOME_GEOFENCE");

        // Extract office coordinates
        String officeGeoCoordinates = preferences.getString(AppConstants.KEY_WORK_GEO_COORDINATES, "Lat: 0.0, Long: 0.0");
        String[] officeParts = officeGeoCoordinates.split(",");
        officeLatitude = parseCoordinate(officeParts[0], "Lat: ");
        officeLongitude = parseCoordinate(officeParts[1], "Long: ");
        officeGeofenceId = preferences.getString(AppConstants.KEY_OFFICE_GEOFENCE_ID, "OFFICE_GEOFENCE");

        // Retrieve geofenceRadius as a String and convert it to a float
        String geofenceRadiusString = preferences.getString(AppConstants.KEY_GEOFENCE_RADIUS_IN_METERS, String.valueOf(AppConstants.GEOFENCE_RADIUS_IN_METERS));
        geofenceRadius = Float.parseFloat(geofenceRadiusString);

        }catch (Exception e) {
            Log.e(TAG, "Error loading geofence data from preferences", e);
            homeLatitude = 0.0;
            homeLongitude = 0.0;
            officeLatitude = 0.0;
            officeLongitude = 0.0;
            geofenceRadius = AppConstants.GEOFENCE_RADIUS_IN_METERS;
        }
    }


    // Helper method to parse individual coordinate parts
    private double parseCoordinate(String part, String prefix) {
        try {
            return Double.parseDouble(part.replace(prefix, "").trim());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Failed to parse coordinate: " + part, e);
            return 0.0;
        }
    }





    private void addGeofences() {
        Log.d(TAG, "Attempting to add geofences...");

        Geofence homeGeofence = new Geofence.Builder()
                .setRequestId(homeGeofenceId)
                .setCircularRegion(homeLatitude, homeLongitude, geofenceRadius)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();

        Geofence officeGeofence = new Geofence.Builder()
                .setRequestId(officeGeofenceId)
                .setCircularRegion(officeLatitude, officeLongitude, geofenceRadius)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(Arrays.asList(homeGeofence, officeGeofence))
                .build();

        geofencePendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                new Intent(this, GeofenceBroadcastReceiver.class),
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permissions are not granted. Cannot add geofences.");
            return;
        }

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Geofences added successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to add geofences", e));
    }

    private Notification createNotification() {
        String channelId = "location_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Location Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Employee Tracking")
                .setContentText("Tracking employee location...")
                .setSmallIcon(R.drawable.baseline_home_24)
                .build();
    }

    private void startLocationUpdates() {
        Log.d("LocationService", "Attempting to start location updates...");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                    .setMinUpdateIntervalMillis(5000)
                    .build();

            try {
                Log.d("LocationService", "Requesting location updates...");
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            } catch (SecurityException e) {
                Log.e("LocationService", "SecurityException: Missing location permission", e);
            }
        } else {
            Log.e("LocationService", "Permissions are not granted for location updates.");
            stopSelf();
        }
    }

    private void updateLocationInFirebase(Location location) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Log.e("LocationService", "No user is currently logged in.");
            return;
        }

        String employeeId = currentUser.getUid();
        Log.d("LocationService", "Updating location in Firestore for employee ID: " + employeeId);

        if (location == null) {
            Log.e("LocationService", "Location is null. Cannot update in Firestore.");
            return;
        }

        String geoCoordinates = "Lat: " + location.getLatitude() + ", Long: " + location.getLongitude();
        DocumentReference docRef = db.collection("users").document(employeeId);

//        docRef.update("geoCoordinates", geoCoordinates)
//                .addOnSuccessListener(aVoid -> Log.d("LocationService", "Location updated in Firestore"))
//                .addOnFailureListener(e -> Log.e("LocationService", "Failed to update location in Firestore", e));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (geofencingClient != null && geofencePendingIntent != null) {
            geofencingClient.removeGeofences(geofencePendingIntent)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Geofences removed"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to remove geofences", e));
        }
        Log.d("LocationService", "Location tracking stopped.");
    }
}

