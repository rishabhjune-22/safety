package com.example.safety;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class HomePageActivity extends AppCompatActivity {
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    View view;
    OnBackPressedDispatcher dispatcher;

    ImageButton msgButton;
    ImageButton attendanceButton;
    ImageButton trackEmpButton;
    ImageButton employeesButton ;
    ImageButton leaveApplicationButton;

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
