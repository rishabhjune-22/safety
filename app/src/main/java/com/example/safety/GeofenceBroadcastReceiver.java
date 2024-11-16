package com.example.safety;

import static android.content.ContentValues.TAG;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String GEOFENCE_CHANNEL_ID = "geofence_channel";
    private static final int GEOFENCE_NOTIFICATION_ID = 1002;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "GeofenceBroadcastReceiver triggered.");

        if (intent == null) {
            Log.e(TAG, "Received null intent in onReceive.");
            showNotification(context, "Geofence Event Null", "Received a null intent");
            return;
        }

        SharedPreferences preferences = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        String homeGeofenceId = preferences.getString(AppConstants.KEY_HOME_GEOFENCE_ID, "HOME_GEOFENCE");
        String officeGeofenceId = preferences.getString(AppConstants.KEY_OFFICE_GEOFENCE_ID, "OFFICE_GEOFENCE");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent == null) {
            Log.e(TAG, "GeofencingEvent is null.");
            showNotification(context, "Geofence Event Null", "GeofencingEvent is null, geofence not triggered");
            return;
        }

        if (geofencingEvent.hasError()) {
            int errorCode = geofencingEvent.getErrorCode();
            Log.e(TAG, "Geofence error: " + GeofenceStatusCodes.getStatusCodeString(errorCode));
            showNotification(context, "Geofence Error", "Error code: " + errorCode);
            return;
        }

        handleGeofenceTransition(context, geofencingEvent, homeGeofenceId, officeGeofenceId);
    }

    private void handleGeofenceTransition(Context context, GeofencingEvent geofencingEvent, String homeGeofenceId, String officeGeofenceId) {
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No current user detected during geofence transition.");
            return;
        }

        String employeeId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(employeeId);

        for (Geofence geofence : Objects.requireNonNull(geofencingEvent.getTriggeringGeofences())) {
            String requestId = geofence.getRequestId();

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                if (requestId.equals(homeGeofenceId)) {
                    updateFirestore(docRef, "home_check_in", timestamp, "Home check-in");
                } else if (requestId.equals(officeGeofenceId)) {
                    updateFirestore(docRef, "office_check_in", timestamp, "Office check-in");
                }
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                if (requestId.equals(homeGeofenceId)) {
                    updateFirestore(docRef, "home_check_out", timestamp, "Home check-out");
                } else if (requestId.equals(officeGeofenceId)) {
                    updateFirestore(docRef, "office_check_out", timestamp, "Office check-out");
                }
            }
        }
    }

    private void updateFirestore(DocumentReference docRef, String field, String value, String logMessage) {
        docRef.update(field, value)
                .addOnSuccessListener(aVoid -> Log.d(TAG, logMessage + " time updated"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update " + logMessage.toLowerCase(), e));
    }

    private void showNotification(Context context, String title, String message) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(GEOFENCE_CHANNEL_ID, "Geofence Events", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, GEOFENCE_CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_home_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        manager.notify(GEOFENCE_NOTIFICATION_ID, builder.build());
    }
}
