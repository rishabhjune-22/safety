package com.example.safety;

import static android.content.ContentValues.TAG;
import static com.example.safety.AppConstants.HOME_GEOFENCE_ID;
import static com.example.safety.AppConstants.OFFICE_GEOFENCE_ID;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;

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
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "GeofenceBroadcastReceiver triggered.");

        if (intent == null) {
            Log.e(TAG, "Received null intent in onReceive.");
            return;
        }

        // Retrieve the GeofencingEvent from the intent
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        // Check if the GeofencingEvent is null
        if (geofencingEvent == null) {
            Log.e(TAG, "GeofencingEvent is null. Exiting onReceive.");

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "geofence_channel")
                    .setSmallIcon(R.drawable.baseline_home_24)
                    .setContentTitle("Geofence Event Null")
                    .setContentText("GeofencingEvent is null, geofence not triggered")
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("geofence_channel", "Geofence Events", NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(channel);
            }
            manager.notify(1002, builder.build());
            return;
        }

        // Check if there's an error in the GeofencingEvent
        if (geofencingEvent.hasError()) {
            int errorCode = geofencingEvent.getErrorCode();
            Log.e(TAG, "Geofence error: " + GeofenceStatusCodes.getStatusCodeString(errorCode));
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        Log.d(TAG, "Geofence transition detected: " + geofenceTransition);

        for (Geofence geofence : Objects.requireNonNull(geofencingEvent.getTriggeringGeofences())) {
            String requestId = geofence.getRequestId();
            Log.d(TAG, "Processing geofence with request ID: " + requestId);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser == null) {
                Log.e(TAG, "No current user detected during geofence transition.");
                return;
            }

            String employeeId = currentUser.getUid();
            DocumentReference docRef = db.collection("users").document(employeeId);
            Log.d(TAG, "Updating Firestore document for user ID: " + employeeId);

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                if (requestId.equals(HOME_GEOFENCE_ID)) {
                    Log.d(TAG, "Home geofence ENTER detected.");
                    docRef.update("home_check_in", timestamp)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Home check-in time updated"))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to update home check-in", e));
                } else if (requestId.equals(OFFICE_GEOFENCE_ID)) {
                    Log.d(TAG, "Office geofence ENTER detected.");
                    docRef.update("office_check_in", timestamp)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Office check-in time updated"))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to update office check-in", e));
                }
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                if (requestId.equals(HOME_GEOFENCE_ID)) {
                    Log.d(TAG, "Home geofence EXIT detected.");
                    docRef.update("home_check_out", timestamp)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Home check-out time updated"))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to update home check-out", e));
                } else if (requestId.equals(OFFICE_GEOFENCE_ID)) {
                    Log.d(TAG, "Office geofence EXIT detected.");
                    docRef.update("office_check_out", timestamp)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Office check-out time updated"))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to update office check-out", e));
                }
            }
        }
    }
}
