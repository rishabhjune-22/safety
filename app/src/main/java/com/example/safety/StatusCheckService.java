package com.example.safety;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StatusCheckService extends Service {

    private static final String CHANNEL_ID = "StatusCheckServiceChannel";
    private Handler handler;
    private Runnable checkStatusRunnable;

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler(Looper.getMainLooper());

        // Define a periodic status check
        checkStatusRunnable = new Runnable() {
            @Override
            public void run() {
                boolean isLocationEnabled = isLocationEnabled();
                updateStatusInFirebase(isLocationEnabled);

                // Schedule the next check (every 10 seconds or as needed)
                handler.postDelayed(this, 3000);
            }
        };

        // Start the periodic status check
        handler.post(checkStatusRunnable);

        // Start the service as a foreground service
        startForeground(1, createNotification());
    }

    private Notification createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Status Check Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Employee Safety App")
                .setContentText("Checking location status and updating last seen...")
                .setSmallIcon(R.drawable.baseline_home_24) // Replace with your app's icon
                .build();
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void updateStatusInFirebase(boolean isLocationEnabled) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String employeeId = "BE5cYPeqWiQurUwFN6qC0MwT0xi2"; // Replace or dynamically obtain the employee ID
        DocumentReference docRef = db.collection("users").document(employeeId);
        Date lastSeenDate = new Date();
        String formattedDate = formatDate(lastSeenDate);
        // Prepare fields to update
        docRef.update("is_loc_enabled", isLocationEnabled, "last_seen", formattedDate)
                .addOnSuccessListener(aVoid -> Log.d("StatusCheckService", "Status and last_seen updated in Firestore"))
                .addOnFailureListener(e -> Log.e("StatusCheckService", "Failed to update Firestore", e));
    }
    private String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return dateFormat.format(date);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(checkStatusRunnable); // Stop periodic checks
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
