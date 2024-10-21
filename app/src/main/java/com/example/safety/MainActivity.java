package com.example.safety;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.Group;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {
    private View view;
    private FrameLayout frameLayout;
    private TabLayout tabLayout;
    private EditText email;
    private EditText password;
    private AppCompatButton login_button;
    private Group sign_btn_group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.Frame_layout_container);
        tabLayout = view.findViewById(R.id.tabLayout);
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.pass);
        login_button = view.findViewById(R.id.login_btn);
        sign_btn_group = view.findViewById(R.id.sign_btn_group);
        frameLayout = view.findViewById(R.id.Frame_layout_container);

        // Apply edge-to-edge insets to the FrameLayout container


        // Avoid recreating fragment if savedInstanceState is not null
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.Frame_layout_container, new LoginFragment())
                    .commit();
        }

    }





}
