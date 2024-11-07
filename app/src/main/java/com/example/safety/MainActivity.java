package com.example.safety;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {

    private boolean askedForPermissions = false; // Flag to track if permissions were requested
    private boolean redirectedToSettings = false; // Flag to track if settings page was already opened
    private BroadcastReceiver statusReceiver; // BroadcastReceiver for GPS and internet status changes

    private final ActivityResultLauncher<String> fineLocationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    checkBackgroundLocationPermission(); // Proceed to request background permission
                } else {
                    Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<String> backgroundLocationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startLocationService();
                } else {
                    Toast.makeText(this, "Background location permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.Frame_layout_container, new LoginFragment())
                    .commit();
        }

        // Initialize and register the BroadcastReceiver
        registerStatusReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusReceiver != null) {
            unregisterReceiver(statusReceiver); // Unregister the BroadcastReceiver to avoid memory leaks
        }
    }

    private void registerStatusReceiver() {
        statusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Check both internet and GPS status whenever there's a change
                if (isInternetAvailable() && isGpsEnabled()) {
                    redirectedToSettings = false; // Reset flag once both conditions are met
                    checkLocationPermission(); // Request permissions if both conditions are met
                } else {
                    if (!isInternetAvailable()) {
                        Toast.makeText(MainActivity.this, "Internet connection is not available.", Toast.LENGTH_SHORT).show();
                    }
                    if (!isGpsEnabled() && !redirectedToSettings) {
                        Toast.makeText(MainActivity.this, "GPS is turned off. Please enable it in settings.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); // Open location settings
                        redirectedToSettings = true; // Set the flag to avoid repeated redirection
                    }
                }
            }
        };

        // Register the receiver for connectivity and GPS provider changes
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(statusReceiver, filter);
    }

    private void checkLocationPermission() {
        if (!askedForPermissions) { // Only request permissions if not already asked
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                checkBackgroundLocationPermission(); // If fine location is granted, check background location permission
            } else {
                fineLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION); // Request fine location permission
            }
            askedForPermissions = true; // Set the flag to true after requesting
        }
    }

    private void checkBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Only required for Android 10 and above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                startLocationService();
            } else {
                backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION); // Request background location permission
            }
        } else {
            startLocationService(); // Start service directly if background permission isn't needed
        }
    }

    public void startLocationService() {
        Intent serviceIntent = new Intent(this, StatusCheckService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    public boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                return networkCapabilities != null &&
                        (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            } else {
                return connectivityManager.getActiveNetworkInfo() != null &&
                        connectivityManager.getActiveNetworkInfo().isConnected();
            }
        }
        return false;
    }

    public boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }
}
