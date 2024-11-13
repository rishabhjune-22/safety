package com.example.safety;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {
    private final ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Foreground Location permission granted. Checking for Background location permission.", Toast.LENGTH_SHORT).show();
                    checkBackgroundLocationPermission();
                } else {
                    Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<String> backgroundLocationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Background location permission granted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Background location permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private AlertDialog gpsDialog;
    private TextView internetStatusBanner;

    private final BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
                if (!isGpsEnabled()) {
                    showGpsSettingsDialog();
                } else if (gpsDialog != null && gpsDialog.isShowing()) {
                    gpsDialog.dismiss();
                }
            }
        }
    };

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateInternetStatusBanner(isInternetConnected());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the internet status banner
        internetStatusBanner = findViewById(R.id.internet_status_banner);

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.Frame_layout_container, new LoginFragment())
                    .commit();
        }

        // Register receivers for GPS and network changes
        registerReceiver(gpsReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        checkGpsStatusAndPrompt();
        checkForegroundLocationPermission();
        updateInternetStatusBanner(isInternetConnected());
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkGpsStatusAndPrompt();
        updateInternetStatusBanner(isInternetConnected());
    }

    private void checkGpsStatusAndPrompt() {
        if (!isGpsEnabled()) {
            showGpsSettingsDialog();
        } else if (gpsDialog != null && gpsDialog.isShowing()) {
            gpsDialog.dismiss();
        }
    }

    private boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showGpsSettingsDialog() {
        if (gpsDialog == null || !gpsDialog.isShowing()) {
            gpsDialog = new AlertDialog.Builder(this)
                    .setTitle("Enable GPS")
                    .setMessage("GPS is required for this app. Please enable GPS in settings.")
                    .setCancelable(false)
                    .setPositiveButton("Go to Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("Exit App", (dialog, which) -> {
                        Toast.makeText(this, "GPS is required for tracking. Exiting app.", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .create();
            gpsDialog.show();
        }
    }

    public void checkForegroundLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkBackgroundLocationPermission();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void checkBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        }
    }

    private void updateInternetStatusBanner(boolean isConnected) {
        internetStatusBanner.setVisibility(isConnected ? TextView.GONE : TextView.VISIBLE);
    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connectivityManager.getActiveNetwork();
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            } else {
                android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected();
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(gpsReceiver);
        unregisterReceiver(networkReceiver);
    }
}
