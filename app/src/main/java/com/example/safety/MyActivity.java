package com.example.safety;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
        String userId = currentUser.getUid();
            db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {



                            // Pass name and image URL to HomePageActivity
                            // Optional: close the login activity
                        } else {
                            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        }
        else {

            Toast.makeText(this, "Unable to Fetch data", Toast.LENGTH_SHORT).show();

        }





    }
}