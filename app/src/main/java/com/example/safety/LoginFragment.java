package com.example.safety;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginFragment extends Fragment {

    private View view;
    private FrameLayout frameLayout;
    private TabLayout tabLayout;
    private EditText email, password;
    private AppCompatButton loginButton;
    private Group signBtnGroup;
    private TextView signUpTextView;
    private FirebaseAuth mAuth;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();  // Initialize FirebaseAuth
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize views
        frameLayout = view.findViewById(R.id.Frame_layout_container);
        tabLayout = view.findViewById(R.id.tabLayout);
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.pass);
        loginButton = view.findViewById(R.id.login_btn);
        signBtnGroup = view.findViewById(R.id.sign_btn_group);
        signUpTextView = view.findViewById(R.id.sign_btn);

        // Set up login button click listener
        loginButton.setOnClickListener(v -> {
            String emailText = email.getText().toString().trim();
            String passwordText = password.getText().toString().trim();
            Intent intent = new Intent(requireContext(), HomePageActivity.class);
            startActivity(intent);
            requireActivity().finish();

//            if (emailText.isEmpty() || passwordText.isEmpty()) {
//                Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show();
//            } else if (!isValidEmail(emailText)) {
//                Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
//            } else {
//                checkAccountExists(emailText, passwordText);  // Handle Firebase login logic
//            }
        });

        // Handle "Sign Up" click to switch fragments
        signUpTextView.setOnClickListener(v -> switchToSignUpFragment());

        // Set initial tab icon colors with safety checks
        if (tabLayout.getTabCount() >= 2) {
            setTabIconsColor(tabLayout.getTabAt(0), R.color.TabSelected);
            setTabIconsColor(tabLayout.getTabAt(1), R.color.TabUnselected);
        }

        // Add tab selection listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                setTabIconsColor(tab, R.color.TabSelected);
                signBtnGroup.setVisibility(tab.getPosition() == 1 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onTabUnselected(@NonNull TabLayout.Tab tab) {
                setTabIconsColor(tab, R.color.TabUnselected);
            }

            @Override
            public void onTabReselected(@NonNull TabLayout.Tab tab) {
                // Optional: Handle reselection
            }
        });

        return view;
    }

    // Helper method to set the tab icon color
    private void setTabIconsColor(TabLayout.Tab tab, int color) {
        if (tab != null && tab.getIcon() != null) {
            Drawable icon = tab.getIcon();
            icon.setColorFilter(ContextCompat.getColor(requireContext(), color), PorterDuff.Mode.SRC_IN);
        }
    }

    // Validate email format
    public boolean isValidEmail(String email) {
        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // Firebase login logic to check if account exists
    public void checkAccountExists(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(requireContext(), HomePageActivity.class);
                            startActivity(intent);
                            requireActivity().finish();  // Optional: close the login activity

                        }
                    } else {
                        Toast.makeText(requireContext(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to switch to the SignUpFragment
    private void switchToSignUpFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.Frame_layout_container, new SignupFragment());
        fragmentTransaction.addToBackStack(null); // Allows navigating back to LoginFragment
        fragmentTransaction.commit();
    }
}
