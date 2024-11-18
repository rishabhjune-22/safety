package com.example.safety;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String name_txt, profileImageUrl;
    private TextView nameTextview, homecheckInTextView, homecheckOutTextView, officecheckInTextView, officecheckOutTextView;
    private ShapeableImageView headerProfileImage;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        progressBar = findViewById(R.id.progress_bar);
        nameTextview = findViewById(R.id.user_name);
        headerProfileImage = findViewById(R.id.header_profile);
        homecheckInTextView = findViewById(R.id.home_checkin);
        homecheckOutTextView = findViewById(R.id.home_checkout);
        officecheckInTextView = findViewById(R.id.office_checkin);
        officecheckOutTextView = findViewById(R.id.office_checkout);

        // Set default placeholders
        homecheckInTextView.setText("--:--");
        homecheckOutTextView.setText("--:--");
        officecheckInTextView.setText("--:--");
        officecheckOutTextView.setText("--:--");

        progressBar.setVisibility(View.VISIBLE);

        // Get intent data
        profileImageUrl = getIntent().getStringExtra("profileImageUrl") != null ? getIntent().getStringExtra("profileImageUrl") : "";
        name_txt = getIntent().getStringExtra("name") != null ? getIntent().getStringExtra("name") : "User";

        nameTextview.setText(name_txt);
        Glide.with(this).load(profileImageUrl).placeholder(R.drawable.baseline_person_24).into(headerProfileImage);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Firestore listener
            db.collection("users").document(userId).addSnapshotListener((documentSnapshot, e) -> {
                progressBar.setVisibility(View.GONE);
                if (e != null) {
                    Log.e("MyActivity", "Error listening to Firestore updates", e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String homeCheckInTime = formatCheckinCheckout(safeGetString(documentSnapshot, "home_check_in"));
                    String homeCheckOutTime = formatCheckinCheckout(safeGetString(documentSnapshot, "home_check_out"));
                    String officeCheckInTime = formatCheckinCheckout(safeGetString(documentSnapshot, "office_check_in"));
                    String officeCheckOutTime = formatCheckinCheckout(safeGetString(documentSnapshot, "office_check_out"));

                    homecheckInTextView.setText(homeCheckInTime);
                    homecheckOutTextView.setText(homeCheckOutTime);
                    officecheckInTextView.setText(officeCheckInTime);
                    officecheckOutTextView.setText(officeCheckOutTime);
                } else {
                    Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private String safeGetString(DocumentSnapshot document, String key) {
        return document.getString(key) != null ? document.getString(key) : "";
    }

    public String formatCheckinCheckout(String time) {
        if (time == null || time.isEmpty()) {
            return "Not Available";
        }

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        outputFormat.setDateFormatSymbols(new java.text.DateFormatSymbols(Locale.getDefault()) {
            @Override
            public String[] getAmPmStrings() {
                return new String[]{"AM", "PM"};
            }
        });

        try {
            Date date = inputFormat.parse(time);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e("FormatError", "Error parsing time: " + time, e);
            return "Invalid Time";
        }
    }
}
