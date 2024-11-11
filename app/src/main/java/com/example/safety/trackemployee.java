package com.example.safety;
import com.google.firebase.firestore.ListenerRegistration;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class trackemployee extends AppCompatActivity {

    private static final String TAG = "trackemployee";

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private TextInputEditText searchEditText;
    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

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

        // Initialize user list and set up RecyclerView
        userList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        db = FirebaseFirestore.getInstance();

        // Fetch users from Firestore
        fetchUsersFromFirestore();

        // Set up real-time filtering based on the search query
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchUsersFromFirestore() {
        listenerRegistration = db.collection("users")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed: ", e);
                        return;
                    }

                    if (querySnapshot != null) {
                        userList.clear(); // Clear the list to avoid duplicates
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            User user = document.toObject(User.class);
                            if (user != null) {
                                user.setLastSeen(document.getString("last_seen"));
                                userList.add(user);
                            }
                        }
                        // Apply current search filter to the updated user list
                        applyFilter(searchEditText.getText().toString());
                    }
                });
    }

    private void applyFilter(String query) {
        List<User> filteredList = new ArrayList<>();
        for (User user : userList) {
            if (user.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }

        // Update the adapter with the filtered list
        if (userAdapter == null) {
            userAdapter = new UserAdapter(this, filteredList);
            recyclerView.setAdapter(userAdapter);
        } else {
            userAdapter.updateUserList(filteredList);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove(); // Stop listening for updates when the activity is destroyed
        }
    }
}
