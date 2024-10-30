package com.example.safety;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trackemployee);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView_users);
        searchView = findViewById(R.id.searchView);

        // Initialize the list and set up the RecyclerView
        userList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch users from Firestore and set up the adapter once data is available
        fetchUsersFromFirestore();

        // Set up search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (userAdapter != null) {
                    userAdapter.filter(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (userAdapter != null) {
                    userAdapter.filter(newText);
                }
                return true;
            }
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

                            // Initialize adapter with loaded data
                            userAdapter = new UserAdapter(this, userList);
                            recyclerView.setAdapter(userAdapter);
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
