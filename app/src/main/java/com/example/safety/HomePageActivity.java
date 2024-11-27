package com.example.safety;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class HomePageActivity extends AppCompatActivity {

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    View headerView;
    ImageButton trackEmpButton,myActivityBtn;
    String name_txt, profileImageUrl, email_txt, userId;
    TextView cardnameTextView, headerProfileName, headerEmailText;
    Boolean isAdmin;
    ShapeableImageView cardprofileImageView, headerProfileImage;
    private ListenerRegistration logoutListener;
    private FirebaseFirestore db;

    // Request FINE location permission
    private final ActivityResultLauncher<String> fineLocationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    checkBackgroundLocationPermission(); // Proceed to request background permission
                } else {
                    Toast.makeText(this, "Location permission denied. Location tracking will not work.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // Request BACKGROUND location permission
    private final ActivityResultLauncher<String> backgroundLocationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startLocationService();
                } else {
                    Toast.makeText(this, "Background location permission denied. Location tracking will not work in the background.", Toast.LENGTH_SHORT).show();
                }
            }
    );




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
            updateIsActiveStatus(true); // Set isActive to true on login
            setLogoutListener(userId);
        } else {
            redirectToLogin();
        }

        boolean isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        if (isAdmin) {
            setContentView(R.layout.activity_home_page);
            initializeAdminUI();
        } else {
            setContentView(R.layout.activity_home_employee);
            initializeEmployeeUI();
        }

        checkLocationPermission();
    }


    private void updateIsActiveStatus(boolean isActive) {
        if (userId != null) {
            db.collection("users").document(userId)
                    .update("isActive", isActive)
                    .addOnSuccessListener(aVoid -> Log.d("HomePageActivity", "isActive updated to: " + isActive))
                    .addOnFailureListener(e -> {
                        Log.e("HomePageActivity", "Failed to update isActive", e);
                        Toast.makeText(this, "Failed to update session status. Please check your network.", Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void setLogoutListener(String userId) {
        String currentDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (currentDeviceId == null) {
            Log.e("LogoutListener", "Unable to retrieve device ID.");
            return;
        }

        DocumentReference userRef = db.collection("users").document(userId);

        logoutListener = userRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e("LogoutListener", "Error listening to Firestore", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                String storedDeviceId = snapshot.getString("deviceId");
                Boolean isActive = snapshot.getBoolean("isActive");

                // Log values safely
                Log.d("LogoutListener", "DeviceID: " + (storedDeviceId != null ? storedDeviceId : "null")
                        + " ::: isActive: " + (isActive != null ? isActive : "null"));

                // Correct logout condition
                if (Boolean.TRUE.equals(isActive) && !currentDeviceId.equals(storedDeviceId)) {
                    Log.d("LogoutListener", "Another device logged in. Logging out this device.");
                    logOut();
                }
            } else {
                Log.d("LogoutListener", "Snapshot is null or does not exist.");
            }
        });
    }

    private void initializeCommonUI() {
        name_txt = getIntent().getStringExtra("name") != null ? getIntent().getStringExtra("name") : "User";
        profileImageUrl = getIntent().getStringExtra("profileImageUrl") != null ? getIntent().getStringExtra("profileImageUrl") : "";
        email_txt = getIntent().getStringExtra("email") != null ? getIntent().getStringExtra("email") : "";

        navigationView = findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        cardnameTextView = findViewById(R.id.card_name_text);
        cardprofileImageView = findViewById(R.id.card_profile);
        headerEmailText = headerView.findViewById(R.id.header_email_text);
        headerProfileName = headerView.findViewById(R.id.header_profile_name);
        headerProfileImage = headerView.findViewById(R.id.header_profile);

        cardnameTextView.setText("Hello, " + name_txt);
        Glide.with(this).load(profileImageUrl).placeholder(R.drawable.baseline_person_24).into(cardprofileImageView);
        headerEmailText.setText(email_txt);
        headerProfileName.setText(name_txt);
        Glide.with(this).load(profileImageUrl).placeholder(R.drawable.baseline_person_24).into(headerProfileImage);

        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.action_toolbar);
        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_logout) {
                logOut();
            }
            return true;
        });


    }

    private void initializeAdminUI() {

        initializeCommonUI();
        // Track Employee Button Listener
        trackEmpButton = findViewById(R.id.track_activity_btn);
        trackEmpButton = findViewById(R.id.track_activity_btn);
        if (trackEmpButton != null) {
            trackEmpButton.setOnClickListener(v -> startActivity(new Intent(this, trackemployee.class)));
        } else {
            Log.e("HomePageActivity", "trackEmpButton not found in layout.");
        }


        // Navigation Drawer Menu Click Listener

    }
public void initializeEmployeeUI(){
    initializeCommonUI();

    myActivityBtn = findViewById(R.id.myactivity_btn);
    myActivityBtn.setOnClickListener(v -> {
        // Pass name_txt to MyActivity
        Intent intent = new Intent(HomePageActivity.this, MyActivity.class);
        intent.putExtra("name", name_txt);
        intent.putExtra("profileImageUrl", profileImageUrl);

        // Add name_txt as an extra
        startActivity(intent);
    });

}



//    private void logOut() {
//        if (logoutListener != null) {
//            logoutListener.remove();
//            logoutListener = null;
//        }
//
//        String currentDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
//
//        db.collection("users").document(userId)
//                .update("isActive", false) // Clear the deviceId for this session
//                .addOnCompleteListener(task -> {
//                    Intent stopServiceIntent = new Intent(this, LocationService.class);
//                    stopService(stopServiceIntent);
//
//                    FirebaseAuth.getInstance().signOut();
//
//                    SharedPreferences preferences = getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
//                    SharedPreferences.Editor editor = preferences.edit();
//                    editor.clear();
//                    editor.apply();
//
//                    Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show();
//                    redirectToLogin();
//                });
//    }
//
//
//    private void redirectToLogin() {
//        Intent intent = new Intent(HomePageActivity.this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        finish();
//    }

    private void logOut() {
        // Remove any Firestore listeners to avoid memory leaks
        if (logoutListener != null) {
            logoutListener.remove();
            logoutListener = null;
        }

        // Fetch the device ID
        String currentDeviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Update Firestore to mark the user as inactive
        db.collection("users").document(userId)
                .update("isActive", false)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Successfully updated Firestore
                        stopLocationServiceAndSignOut(); // Stop services and clear auth/session
                    } else {
                        // Log and handle errors if Firestore update fails
                        Log.e("Logout", "Error updating Firestore: ", task.getException());
                        Toast.makeText(this, "Failed to log out. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void stopLocationServiceAndSignOut() {
        // Stop the location service
        Intent stopServiceIntent = new Intent(this, LocationService.class);
        stopService(stopServiceIntent);

        // Sign out from Firebase Auth
        FirebaseAuth.getInstance().signOut();

        // Clear SharedPreferences
        SharedPreferences preferences = getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        // Notify the user and redirect to login
        Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent loginIntent = new Intent(this, MainActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }






    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkBackgroundLocationPermission();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            showPermissionRationale();
        } else {
            fineLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void showPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle("Location Permission Required")
                .setMessage("This app requires location permissions to track your location for safety purposes.")
                .setPositiveButton("Grant Permission", (dialog, which) -> fineLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }


    private void checkBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                startLocationService();
            } else {
                backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        } else {
            startLocationService();
        }
    }

    private void startLocationService() {
        if (hasLocationPermissions()) {
            Intent serviceIntent = new Intent(this, LocationService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        } else {
            Toast.makeText(this, "Location permissions are required.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasLocationPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logoutListener != null) {
            logoutListener.remove();
        }
    }
}
