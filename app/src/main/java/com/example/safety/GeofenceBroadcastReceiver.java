package com.example.safety;

import static com.example.safety.AppConstants.HOME_GEOFENCE_ID;
import static com.example.safety.AppConstants.OFFICE_GEOFENCE_ID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        assert geofencingEvent != null;
        if (geofencingEvent.hasError()) {
            Log.e("GEOFENCE", "Geofencing error: " + geofencingEvent.getErrorCode());
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

        assert triggeringGeofences != null;
        for (Geofence geofence : triggeringGeofences) {
            String geofenceId = geofence.getRequestId();
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                if (geofenceId.equals(OFFICE_GEOFENCE_ID)) {
                    // Office Check-In
                    saveCheckIn("Office");
                } else if (geofenceId.equals(HOME_GEOFENCE_ID)) {
                    // Home Check-In
                    saveCheckIn("Home");
                }
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                if (geofenceId.equals(OFFICE_GEOFENCE_ID)) {
                    // Office Check-Out
                    saveCheckOut("Office");
                } else if (geofenceId.equals(HOME_GEOFENCE_ID)) {
                    // Home Check-Out
                    saveCheckOut("Home");
                }
            }
        }
    }

    private void saveCheckIn(String location) {
        String checkInTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        // Save check-in time to the database with location (Office or Home)
        Log.d("GEOFENCE", location + " Check-In at: " + checkInTime);
    }

    private void saveCheckOut(String location) {
        String checkOutTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        // Save check-out time to the database with location (Office or Home)
        Log.d("GEOFENCE", location + " Check-Out at: " + checkOutTime);
    }

}
