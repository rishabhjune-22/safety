package com.example.safety;
import static android.content.Intent.getIntent;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.type.LatLng;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LocationService extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    String userID;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("LocationService", "Service started");

       // userID = getIntent().getStringExtra("userId") != null ? getIntent().getStringExtra("userId") : "";

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Create a LocationCallback to receive location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        Log.d("LocationService", "Location received: " + location.getLatitude() + ", " + location.getLongitude());
                        updateLocationInFirebase(location);
                    }
                }
            }
        };

        // Start location updates
        startLocationUpdates();

        // Start the service as a foreground service
        startForeground(1, createNotification());
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Create LocationRequest using the new Builder pattern
            LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000 ) // 5 minutes interval
                    .setMinUpdateIntervalMillis(5000) // 5 seconds for the fastest interval
                    .build();

            try {
                Log.d("LocationService", "Requesting location updates...");
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            } catch (SecurityException e) {
                e.printStackTrace();
                Log.e("LocationService", "SecurityException: Missing location permission");
            }

        } else {
            Log.e("LocationService", "Permissions are not granted for location updates.");
            stopSelf();
        }
    }





    private void updateLocationInFirebase(Location location) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Check if the user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e("LocationService", "No user is currently logged in.");
            return; // Exit the method if there is no logged-in user
        }

        String employeeId = ((FirebaseUser) currentUser).getUid();

        // Ensure the location is not null
        if (location == null) {
            Log.e("LocationService", "Location is null. Cannot update in Firestore.");
            return; // Exit the method if location is null
        }

        // Document path in Firestore (e.g., "users/{employeeId}")
        DocumentReference docRef = db.collection("users").document(employeeId);

        // Prepare the geoCoordinates field as a single string
        String geoCoordinates = "Lat: " + location.getLatitude() + ", Long: " + location.getLongitude();

        // Update only the geoCoordinates field in Firestore
        docRef.update("geoCoordinates", geoCoordinates)
                .addOnSuccessListener(aVoid -> Log.d("LocationService", "Location updated in Firestore"))
                .addOnFailureListener(e -> Log.e("LocationService", "Failed to update location in Firestore", e));
    }



//    private String getEmployeeId() {
//        // Placeholder for employee ID retrieval logic
//        return "n2GMGAcq71WuDbu3wiorMpc71y52"; // Replace with dynamic retrieval logic
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop location updates to save resources
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        Log.d("LocationService", "Location tracking stopped.");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
