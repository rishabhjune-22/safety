package com.example.safety;

import static android.content.Context.MODE_PRIVATE;
import static com.example.safety.AppConstants.KEY_IS_LOGGED_IN;
import static com.example.safety.AppConstants.PREFS_NAME;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootBroadcastReceiver", "Device booted - checking login status");

            // Check if the user is logged in
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);

            if (isLoggedIn) {
                // Start the LocationService only if the user is logged in
                Intent serviceIntent = new Intent(context, LocationService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent); // For API level 26 and above
                } else {
                    context.startService(serviceIntent); // For API level 25 and below
                }
            } else {
                Log.d("BootBroadcastReceiver", "User is not logged in. Not starting LocationService.");
            }
        }
    }

}
