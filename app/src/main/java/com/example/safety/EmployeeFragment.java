package com.example.safety;
import android.Manifest;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmployeeFragment extends Fragment {
    private static final String TAG = "EmployeeFragment";
    private static final int LOCATION_PERMISSIONS_REQUEST_CODE = 1001;

    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_PROFILE_IMAGE_URL = "profileImageUrl";
    private static final String KEY_EMAIL = "email";

    private EditText email, password;
    private AppCompatButton loginButton;
    private TextView signUpTextView;
    private FirebaseAuth mAuth;


    public EmployeeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employee, container, false);
        mAuth = FirebaseAuth.getInstance();
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.pass);
        loginButton = view.findViewById(R.id.login_btn);
        signUpTextView = view.findViewById(R.id.new_emp_click_here);

        signUpTextView.setOnClickListener(v -> switchToSignUpFragment());
        if (isUserLoggedIn()) {
            launchHomePage();
        }


        loginButton.setOnClickListener(v -> {
            String emailText = email.getText().toString().trim();

            String passwordText = password.getText().toString().trim();
//            Intent intent = new Intent(requireContext(), HomePageActivity.class);
//            startActivity(intent);
            //requireActivity().finish();

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show();
            } else if (!isValidEmail(emailText)) {
                Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            } else {
                checkAccountExists(emailText, passwordText);  // Handle Firebase login logic
            }
        });
        return view;
    }

    public boolean isValidEmail(String email) {
        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    private boolean isUserLoggedIn() {
        SharedPreferences preferences = requireContext().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    // Firebase login logic to check if account exists
    public void checkAccountExists(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                            fetchUserDataAndLaunchHomePage(userId);

                        }
                    } else {
                        Toast.makeText(requireContext(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to switch to the SignUpFragment
    private void switchToSignUpFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.Frame_layout_container, new SignupFragment());
        fragmentTransaction.addToBackStack(null); // Allows navigating back to LoginFragment
        fragmentTransaction.commit();
    }
    private void fetchUserDataAndLaunchHomePage(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        String email= documentSnapshot.getString("email");
                        saveLoginStatus(userId, name, profileImageUrl, email);
                        launchHomePage();
                        // Pass name and image URL to HomePageActivity
                    // Optional: close the login activity
                    } else {
                        Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void saveLoginStatus(String userId, String name, String profileImageUrl, String email) {
        SharedPreferences preferences = requireContext().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();// tomake it editable
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_PROFILE_IMAGE_URL, profileImageUrl);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    private void launchHomePage() {
        SharedPreferences preferences = requireContext().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE); //prefsname-filename of shared pref
        Intent intent = new Intent(requireContext(), HomePageActivity.class);
        intent.putExtra("name", preferences.getString(KEY_USER_NAME, ""));
        intent.putExtra("profileImageUrl", preferences.getString(KEY_PROFILE_IMAGE_URL, ""));
        intent.putExtra("email", preferences.getString(KEY_EMAIL, ""));
        intent.putExtra("userId", preferences.getString(KEY_USER_ID, ""));

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        requireActivity().finish();
    }
}
