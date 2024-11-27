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
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

                    Map<String, List<String>> homeCheckinMap = (Map<String, List<String>>) documentSnapshot.get("HOME_GEOFENCE_checkin");
                    Map<String, List<String>> homeCheckoutMap = (Map<String, List<String>>) documentSnapshot.get("HOME_GEOFENCE_checkout");
                    Map<String, List<String>> officeCheckinMap = (Map<String, List<String>>) documentSnapshot.get("OFFICE_GEOFENCE_checkin");
                    Map<String, List<String>> officeCheckoutMap = (Map<String, List<String>>) documentSnapshot.get("OFFICE_GEOFENCE_checkout");

                    //String latestDate = getLatestDate(officeCheckinMap, officeCheckoutMap, homeCheckinMap, homeCheckoutMap);
                    String currentDate = getCurrentDate();
                    String earliestOfficeCheckin = formatTime(  getEarliestTime(officeCheckinMap, currentDate));
                    String latestOfficeCheckout = formatTime(getLatestTime(officeCheckoutMap, currentDate));
                    String latestHomeCheckin = formatTime( getLatestTime(homeCheckinMap, currentDate));
                    String latestHomeCheckout = formatTime(getLatestTime(homeCheckoutMap, currentDate));

                    homecheckInTextView.setText(latestHomeCheckin);
                    homecheckOutTextView.setText(latestHomeCheckout);
                    officecheckInTextView.setText(earliestOfficeCheckin);
                    officecheckOutTextView.setText(latestOfficeCheckout);
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

    private String getLatestDate(Map<String, List<String>>... maps) {
        String latestDate = null;
        for (Map<String, List<String>> map : maps) {
            if (map != null) {
                for (String date : map.keySet()) {
                    if (latestDate == null || date.compareTo(latestDate) > 0) {
                        latestDate = date;
                    }
                }
            }
        }
        return latestDate;
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(new Date());
    }
    private String getEarliestTime(Map<String, List<String>> map, String date) {
        if (map == null || date == null || !map.containsKey(date)) {
            return "N/A"; // Return "N/A" if the current date is not found
        }
        List<String> times = map.get(date);
        if (times == null || times.isEmpty()) {
            return "N/A"; // Return "N/A" if no times are available for the current date
        }
        return times.stream().min(String::compareTo).orElse("N/A");
    }

    private String getLatestTime(Map<String, List<String>> map, String date) {
        if (map == null || date == null || !map.containsKey(date)) {
            return "N/A"; // Return "N/A" if the current date is not found
        }
        List<String> times = map.get(date);
        if (times == null || times.isEmpty()) {
            return "N/A"; // Return "N/A" if no times are available for the current date
        }
        return times.stream().max(String::compareTo).orElse("N/A");
    }



    private String formatTime(String time) {
        if (time == null || time.isEmpty()  || time.equals("N/A")) {
            Log.d("nullvalue","nullvalue");
            return "N/A";

        }
        Log.d("formatTime", "Input time: " + time);

        Log.d("invalidNA:","invalidNA");
        try {
            // Input format from Firestore (assuming "HH:mm:ss")
            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(time);

            // Output format with uppercase AM/PM
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            outputFormat.setDateFormatSymbols(new java.text.DateFormatSymbols(Locale.getDefault()) {
                @Override
                public String[] getAmPmStrings() {
                    return new String[]{"AM", "PM"}; // Capitalize AM and PM
                }
            });

            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("invalidtime:","invalidtime");

            return "Invalid Time"; // Return this if parsing fails

        }


    }}
