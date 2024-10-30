package com.example.safety;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class trackemployee extends AppCompatActivity {

    private static final String TAG = "trackemployee";

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private TextInputEditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trackemployee);

        // Set system padding for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView_users);
        searchEditText = findViewById(R.id.search_bar_edittext);

        // Initialize list and set up RecyclerView layout and animations
        userList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Fetch users from Firestore and set up the adapter
        fetchUsersFromFirestore();

        // Set up real-time filtering as user types
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (userAdapter != null) {
                    userAdapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchUsersFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            userList.clear();
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                User user = document.toObject(User.class);
                                if (user != null) {
                                    userList.add(user);
                                }
                            }
                            Log.d(TAG, "fetchUsersFromFirestore: Loaded " + userList.size() + " users.");

                            // Initialize the adapter after data is loaded, or update if already initialized
                            if (userAdapter == null) {
                                userAdapter = new UserAdapter(this, userList);
                                recyclerView.setAdapter(userAdapter);
                            } else {
                                userAdapter.updateUserList(userList);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch users", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching users: ", task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching users: ", e);
                });
    }
}
