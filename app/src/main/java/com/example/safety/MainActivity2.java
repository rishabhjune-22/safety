package com.example.safety;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static com.example.safety.AppConstants.KEY_EMAIL;
import static com.example.safety.AppConstants.KEY_GEOFENCE_RADIUS_IN_METERS;
import static com.example.safety.AppConstants.KEY_HOME_GEOFENCE_ID;
import static com.example.safety.AppConstants.KEY_HOME_GEO_COORDINATES;
import static com.example.safety.AppConstants.KEY_IS_LOGGED_IN;
import static com.example.safety.AppConstants.KEY_OFFICE_GEOFENCE_ID;
import static com.example.safety.AppConstants.KEY_PROFILE_IMAGE_URL;
import static com.example.safety.AppConstants.KEY_USER_ID;
import static com.example.safety.AppConstants.KEY_USER_NAME;
import static com.example.safety.AppConstants.KEY_WORK_GEO_COORDINATES;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheSettings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity2 extends AppCompatActivity {
    private String deviceId;

    private EditText email, password;
    private AppCompatButton loginButton;
    private TextView signUpTextView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView internetStatusBanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the internet status banner
        internetStatusBanner = findViewById(R.id.internet_status_banner);

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.Frame_layout_container, new EmployeeFragment())
                    .commit();
        }

    }

}