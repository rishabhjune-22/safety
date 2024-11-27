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
import java.util.Map;

public class trackemployee extends AppCompatActivity {

    private static final String TAG = "trackemployee";

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private TextInputEditText searchEditText;
    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;
    private List<User> filteredList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//       EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trackemployee);

//         Set system padding for edge-to-edge display
        findViewById(R.id.main).setOnApplyWindowInsetsListener((v, insets) -> {
            WindowInsetsCompat windowInsets = WindowInsetsCompat.toWindowInsetsCompat(insets);
            Insets systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply padding for system bars
            v.setPadding(systemBarsInsets.left, systemBarsInsets.top, systemBarsInsets.right, systemBarsInsets.bottom);

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
                                Map<String, List<String>> homeCheckinMap = (Map<String, List<String>>) document.get("HOME_GEOFENCE_checkin");
                                if (homeCheckinMap != null) {
                                    user.setHomeCheckIn(homeCheckinMap);
                                    Log.d(TAG, "Home check-in map set: " + homeCheckinMap);
                                } else {
                                    Log.d(TAG, "Home check-in map is null.");
                                }

                                // Handle home_checkout
                                Map<String, List<String>> homeCheckoutMap = (Map<String, List<String>>) document.get("HOME_GEOFENCE_checkout");
                                if (homeCheckoutMap != null) {
                                    user.setHomeCheckOut(homeCheckoutMap);
                                    Log.d(TAG, "Home check-out map set: " + homeCheckoutMap);
                                } else {
                                    Log.d(TAG, "Home check-out map is null.");
                                }

                                // Handle office_checkin
                                Map<String, List<String>> officeCheckinMap = (Map<String, List<String>>) document.get("OFFICE_GEOFENCE_checkin");
                                if (officeCheckinMap != null) {
                                    user.setOfficeCheckIn(officeCheckinMap);
                                    Log.d(TAG, "Office check-in map set: " + officeCheckinMap);
                                } else {
                                    Log.d(TAG, "Office check-in map is null.");
                                }

                                // Handle office_checkout
                                Map<String, List<String>> officeCheckoutMap = (Map<String, List<String>>) document.get("OFFICE_GEOFENCE_checkout");
                                if (officeCheckoutMap != null) {
                                    user.setOfficeCheckOut(officeCheckoutMap);
                                    Log.d(TAG, "Office check-out map set: " + officeCheckoutMap);
                                } else {
                                    Log.d(TAG, "Office check-out map is null.");
                                }


                                user.setLastSeen(document.getString("last_seen"));
                                Log.d(TAG, "User before adding to list: " + user);
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
            if (user.getName() != null && user.getName().toLowerCase().contains(query.toLowerCase())) {
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
