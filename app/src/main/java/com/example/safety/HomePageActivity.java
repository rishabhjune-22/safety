package com.example.safety;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;

public class HomePageActivity extends AppCompatActivity {
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    View view,headerView;
    OnBackPressedDispatcher dispatcher;

    ImageButton msgButton;
    ImageButton attendanceButton;
    ImageButton trackEmpButton;
    ImageButton employeesButton ;
    ImageButton leaveApplicationButton;
    String name_txt,profileImageUrl,email_txt ;
    TextView cardnameTextView,headerProfileName,headerEmailText;
    ShapeableImageView cardprofileImageView ,headerProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        View decorView = getWindow().getDecorView();

        decorView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                int left = insets.getSystemWindowInsetLeft();
                int top = insets.getSystemWindowInsetTop();
                int right = insets.getSystemWindowInsetRight();
                int bottom = insets.getSystemWindowInsetBottom();

                // Apply the insets to your views (e.g., padding, margins, etc.)
                v.setPadding(left, top, right, bottom);

                return insets.consumeSystemWindowInsets(); // Prevent default behavior
            }
        });






        // Retrieve the name and profile image URL from the intent
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
        Glide.with(this)
                .load(profileImageUrl)
                .placeholder(R.drawable.baseline_person_24)  // Optional placeholder image
                .into(cardprofileImageView);
        headerEmailText.setText(email_txt);
        headerProfileName.setText(name_txt);
        Glide.with(this)
                .load(profileImageUrl)
                .placeholder(R.drawable.baseline_person_24)  // Optional placeholder image
                .into(headerProfileImage);


        trackEmpButton = findViewById(R.id.track_activity_btn);
        trackEmpButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, trackemployee.class);
            startActivity(intent);
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.action_toolbar);
        navigationView = findViewById(R.id.nav_view);
        dispatcher= getOnBackPressedDispatcher();


        setSupportActionBar(toolbar);

        // Set up the ActionBarDrawerToggle to handle the hamburger icon
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Optional: Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            // Handle navigation view item clicks here
            int id = item.getItemId();
            // For example:
            // if (id == R.id.nav_home) { }
            //drawerLayout.closeDrawers();
            return true;
        });

        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(drawerLayout.isDrawerOpen(GravityCompat.START))
                {
                    drawerLayout.closeDrawer(GravityCompat.START);

                }
                else {

                    setEnabled(false);
                    dispatcher.onBackPressed();
                }

            }
        });


    }
}
