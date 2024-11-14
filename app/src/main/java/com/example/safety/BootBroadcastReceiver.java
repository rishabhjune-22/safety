package com.example.safety;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootBroadcastReceiver", "Device booted - starting LocationService");

            // Start your location tracking service
            Intent serviceIntent = new Intent(context, LocationService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent); // For API level 26 and above
            } else {
                context.startService(serviceIntent); // For API level 25 and below
            }

        }
    }
}
