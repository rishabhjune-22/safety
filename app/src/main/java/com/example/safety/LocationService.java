package com.example.safety;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

public class LocationService extends Service {
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
        Log.d(TAG, "LocationService: Service created and starting...");
        startForeground(1, createNotification());

        // Initialize GeofencingClient
        geofencingClient = LocationServices.getGeofencingClient(this);

        // Load geofence data and set up geofences
        loadGeofenceDataFromPreferences();

    }

    private void loadGeofenceDataFromPreferences() {
        Log.d(TAG,"eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
        Log.d(TAG, "Loading geofence data from SharedPreferences...");
        SharedPreferences preferences = getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        try {
            // Extract home coordinates
            String homeGeoCoordinates = preferences.getString(AppConstants.KEY_HOME_GEO_COORDINATES, "Lat: 0.0, Long: 0.0");
            String officeGeoCoordinates = preferences.getString(AppConstants.KEY_WORK_GEO_COORDINATES, "Lat: 0.0, Long: 0.0");
            Log.d(TAG, "Home Geo Coordinates: " + homeGeoCoordinates);
            Log.d(TAG, "Office Geo Coordinates: " + officeGeoCoordinates);

            if (homeGeoCoordinates.equals("Lat: 0.0, Long: 0.0") || officeGeoCoordinates.equals("Lat: 0.0, Long: 0.0")) {
                Log.e(TAG, "Invalid geofence data in SharedPreferences. Fetching from Firebase...");
                fetchGeofenceDataFromFirebase(preferences);
            }
            else {
                String[] homeParts = homeGeoCoordinates.split(",");
                homeLatitude = parseCoordinate(homeParts[0], "Lat: ");
                homeLongitude = parseCoordinate(homeParts[1], "Long: ");
                homeGeofenceId = preferences.getString(AppConstants.KEY_HOME_GEOFENCE_ID, "HOME_GEOFENCE");

                // Extract office coordinates

                String[] officeParts = officeGeoCoordinates.split(",");
                officeLatitude = parseCoordinate(officeParts[0], "Lat: ");
                officeLongitude = parseCoordinate(officeParts[1], "Long: ");
                officeGeofenceId = preferences.getString(AppConstants.KEY_OFFICE_GEOFENCE_ID, "OFFICE_GEOFENCE");

                // Retrieve geofence radius
                String geofenceRadiusString = preferences.getString(AppConstants.KEY_GEOFENCE_RADIUS_IN_METERS, String.valueOf(AppConstants.GEOFENCE_RADIUS_IN_METERS));
                geofenceRadius = Float.parseFloat(geofenceRadiusString);

                Log.d(TAG, "Geofence data loaded: Home (" + homeLatitude + ", " + homeLongitude + "), Office (" + officeLatitude + ", " + officeLongitude + "), Radius: " + geofenceRadius);
                addGeofences();


            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading geofence data from preferences", e);
            homeLatitude = 0.0;
            homeLongitude = 0.0;
            officeLatitude = 0.0;
            officeLongitude = 0.0;
            geofenceRadius = AppConstants.GEOFENCE_RADIUS_IN_METERS;
        }
    }
    private void fetchGeofenceDataFromFirebase(SharedPreferences preferences) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = preferences.getString(AppConstants.KEY_USER_ID, null);

        if (userId == null) {
            Log.e(TAG, "User ID not found in SharedPreferences.");
            return;
        }

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String homeGeoCoordinates = documentSnapshot.getString("homegeoCoordinates");
                        String officeGeoCoordinates = documentSnapshot.getString("officeGeoCoordinates");
                        String geofenceRadiusString = documentSnapshot.getString("geofenceRadiusInMeters");

                        if (homeGeoCoordinates != null && officeGeoCoordinates != null) {
                            // Save to SharedPreferences for next time
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(AppConstants.KEY_HOME_GEO_COORDINATES, homeGeoCoordinates);
                            editor.putString(AppConstants.KEY_WORK_GEO_COORDINATES, officeGeoCoordinates);
                            editor.putString(AppConstants.KEY_GEOFENCE_RADIUS_IN_METERS, geofenceRadiusString);
                            editor.apply();

                            String[] officeParts = officeGeoCoordinates.split(",");
                            String[] homeParts = homeGeoCoordinates.split(",");
                            homeLatitude = parseCoordinate(homeParts[0], "Lat: ");
                            homeLongitude = parseCoordinate(homeParts[1], "Long: ");
                            homeGeofenceId = preferences.getString(AppConstants.KEY_HOME_GEOFENCE_ID, "HOME_GEOFENCE");
                            // Parse and set geofence data
                            officeLatitude = parseCoordinate(officeParts[0], "Lat: ");
                            officeLongitude = parseCoordinate(officeParts[1], "Long: ");
                            officeGeofenceId = preferences.getString(AppConstants.KEY_OFFICE_GEOFENCE_ID, "OFFICE_GEOFENCE");
                            geofenceRadius = Float.parseFloat(geofenceRadiusString);
                            addGeofences();

                        } else {
                            Log.e(TAG, "Invalid geofence data in Firebase.");
                        }
                    } else {
                        Log.e(TAG, "No geofence data found in Firebase.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch geofence data from Firebase.", e));
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

        // Validate geofence data
        if (homeLatitude == 0.0 || homeLongitude == 0.0 || officeLatitude == 0.0 || officeLongitude == 0.0) {
            Log.e(TAG, "Invalid geofence data. Skipping geofence setup.");
            return;
        }

        // Log details of geofences being added
        Log.d(TAG, "Home Geofence: ID = " + homeGeofenceId + ", Lat = " + homeLatitude + ", Long = " + homeLongitude + ", Radius = " + geofenceRadius);
        Log.d(TAG, "Office Geofence: ID = " + officeGeofenceId + ", Lat = " + officeLatitude + ", Long = " + officeLongitude + ", Radius = " + geofenceRadius);

        Geofence homeGeofence = new Geofence.Builder()
                .setRequestId(homeGeofenceId)
                .setCircularRegion(homeLatitude, homeLongitude, geofenceRadius)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL).setLoiteringDelay(10000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();

        Geofence officeGeofence = new Geofence.Builder()
                .setRequestId(officeGeofenceId)
                .setCircularRegion(officeLatitude, officeLongitude, geofenceRadius)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL).setLoiteringDelay(10000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(0) // No immediate trigger
                .addGeofences(Arrays.asList(homeGeofence, officeGeofence))
                .build();

        geofencePendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                new Intent(this, GeofenceBroadcastReceiver.class),
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Check permissions and add geofences
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            NotificationChannel channel = new NotificationChannel(channelId, "Location Service", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Employee Tracking")
                .setContentText("Tracking employee location...")
                .setSmallIcon(R.drawable.baseline_home_24)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (geofencingClient != null && geofencePendingIntent != null) {
            geofencingClient.removeGeofences(geofencePendingIntent)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Geofences removed"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to remove geofences", e));
        }
        Log.d(TAG, "LocationService: Service destroyed, geofences removed.");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.d(TAG, "LocationService restarted with null intent.");
            loadGeofenceDataFromPreferences();
        }
        loadGeofenceDataFromPreferences();
        return START_STICKY;
    }



}
