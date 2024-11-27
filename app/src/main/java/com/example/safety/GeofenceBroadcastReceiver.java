package com.example.safety;

import static android.content.ContentValues.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.*;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String GEOFENCE_CHANNEL_ID = "geofence_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "GeofenceBroadcastReceiver triggered.");

        if (intent == null) {
            Log.e(TAG, "Received null intent.");
            showNotification(context, "Geofence Event Error", "Null intent received.");
            return;
        }

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            Log.e(TAG, "Geofencing event error.");
            return;
        }

        handleGeofenceTransition(context, geofencingEvent);
    }

    private void handleGeofenceTransition(Context context, GeofencingEvent geofencingEvent) {
        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
        if (triggeringGeofences == null || triggeringGeofences.isEmpty()) {
            Log.e(TAG, "No geofences triggered.");
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Log.e(TAG, "No logged-in user.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(currentUser.getUid());

        for (Geofence geofence : triggeringGeofences) {
            String requestId = geofence.getRequestId();

            // Log detailed information about the transition
            Log.d(TAG, "Geofence Transition Detected: ");
            Log.d(TAG, "Transition Type: " + getTransitionString(geofenceTransition));
            Log.d(TAG, "Geofence ID: " + requestId);
            Log.d(TAG, "Timestamp: " + timestamp);

            String message;

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                message = "Entered " + requestId + " at " + timestamp;
                showNotification(context, "Geofence Enter", message);
                Log.d(TAG, "Geofence ENTER Transition for: " + requestId);
                updateFirestoreWithCheckinCheckout(docRef, requestId + "_checkin", timestamp);
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                message = "Exited " + requestId + " at " + timestamp;
                showNotification(context, "Geofence Exit", message);
                Log.d(TAG, "Geofence EXIT Transition for: " + requestId);
                updateFirestoreWithCheckinCheckout(docRef, requestId + "_checkout", timestamp);
            } else {
                Log.d(TAG, "Unknown Geofence Transition: " + geofenceTransition);
            }
        }
    }

    private void updateFirestoreWithCheckinCheckout(DocumentReference docRef, String field, String timestamp) {
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        Log.d(TAG, "Updating Firestore for field: " + field);
        Log.d(TAG, "Date: " + date + ", Time: " + time);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, List<String>> data = (Map<String, List<String>>) documentSnapshot.get(field);
            if (data == null) data = new HashMap<>();

            List<String> timeList = data.getOrDefault(date, new ArrayList<>());
            timeList.add(time);
            data.put(date, timeList);

            docRef.set(Collections.singletonMap(field, data), SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Firestore updated successfully for field: " + field))
                    .addOnFailureListener(e -> Log.e(TAG, "Firestore update failed for field: " + field, e));
        }).addOnFailureListener(e -> Log.e(TAG, "Firestore fetch failed for field: " + field, e));
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

        int uniqueNotificationId = (int) System.currentTimeMillis();
        manager.notify(uniqueNotificationId, builder.build());
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "ENTER";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "EXIT";
            default:
                return "UNKNOWN";
        }
    }
}
