package com.example.safety;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomePageActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2001;

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    View headerView;
    ImageButton trackEmpButton;
    String name_txt, profileImageUrl, email_txt,userID;
    TextView cardnameTextView, headerProfileName, headerEmailText;
    ShapeableImageView cardprofileImageView, headerProfileImage;

    // Request FINE location permission
    private final ActivityResultLauncher<String> fineLocationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    checkBackgroundLocationPermission(); // Proceed to request background permission
                } else {
                    Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // Request BACKGROUND location permission
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
        setContentView(R.layout.activity_home_page);

        // UI initialization
        initializeUI();

        // Check and request location permissions, then start location service
        checkLocationPermission();
    }

    private void initializeUI() {
        // Initialize UI elements and toolbar
        name_txt = getIntent().getStringExtra("name") != null ? getIntent().getStringExtra("name") : "User";
        profileImageUrl = getIntent().getStringExtra("profileImageUrl") != null ? getIntent().getStringExtra("profileImageUrl") : "";
        email_txt = getIntent().getStringExtra("email") != null ? getIntent().getStringExtra("email") : "";
        userID = getIntent().getStringExtra("userId") != null ? getIntent().getStringExtra("userId") : "";

        navigationView = findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        cardnameTextView = findViewById(R.id.card_name_text);
        cardprofileImageView = findViewById(R.id.card_profile);
        headerEmailText = headerView.findViewById(R.id.header_email_text);
        headerProfileName = headerView.findViewById(R.id.header_profile_name);
        headerProfileImage = headerView.findViewById(R.id.header_profile);

        // Set user info on UI
        cardnameTextView.setText("Hello, " + name_txt);
        Glide.with(this).load(profileImageUrl).placeholder(R.drawable.baseline_person_24).into(cardprofileImageView);
        headerEmailText.setText(email_txt);
        headerProfileName.setText(name_txt);
        Glide.with(this).load(profileImageUrl).placeholder(R.drawable.baseline_person_24).into(headerProfileImage);

        // Initialize Drawer and Toolbar
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.action_toolbar);
        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Track Employee Button Listener
        trackEmpButton = findViewById(R.id.track_activity_btn);
        trackEmpButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, trackemployee.class);
            startActivity(intent);
        });

        // Navigation Drawer Menu Click Listener
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_logout) {
                logOut();
            }
            return true;
        });
    }

    private void logOut() {
        SharedPreferences preferences = getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(HomePageActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkBackgroundLocationPermission(); // Check background location permission if fine location is granted
        } else {
            fineLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION); // Request fine location permission
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
            startLocationService(); // No need for background permission below Android 10
        }
    }

    private void startLocationService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        serviceIntent.putExtra("geoCoordinates",userID );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
}
