package com.example.safety;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.util.Calendar;
import android.app.DatePickerDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignupFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignupFragment newInstance(String param1, String param2) {
        SignupFragment fragment = new SignupFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    private View view;
    private EditText fname,lname,email,password,confirm_password,mobile,address,dob;
    private TextView back_to_login;
    private AppCompatButton sign_expand_btn;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_signup, container, false);
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        fname = view.findViewById(R.id.fname_text);
        lname = view.findViewById(R.id.lname_text);
        email = view.findViewById(R.id.email_txt);
        password = view.findViewById(R.id.pass_text);
        confirm_password = view.findViewById(R.id.confirm_pass_txt);
        mobile = view.findViewById(R.id.mobile_txt);
        address = view.findViewById(R.id.address_txt);
        dob = view.findViewById(R.id.date_select);
        sign_expand_btn = view.findViewById(R.id.sign_expand_btn);
        back_to_login = view.findViewById(R.id.back_to_login);
        progressBar = view.findViewById(R.id.progress_bar);
        String already_registered_text = getString(R.string.back_to_login);
        SpannableString spannableString = new SpannableString(already_registered_text);
        int color = ContextCompat.getColor(requireContext(), R.color.click_back_to_login);
        int startIndex = already_registered_text.indexOf("Click here");
        int endIndex = startIndex + "Click here".length();
        spannableString.setSpan(new ForegroundColorSpan(color), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        back_to_login.setText(spannableString);
        back_to_login.setOnClickListener(v -> switchToLoginFragment());
        dob.setOnClickListener(v -> showDatePickerDialog());
        sign_expand_btn.setOnClickListener(v -> {
            String fname_txt = fname.getText().toString().trim();
            String lname_txt = lname.getText().toString().trim();
            String email_txt = email.getText().toString().trim();
            String password_txt = password.getText().toString().trim();
            String confirm_password_txt = confirm_password.getText().toString().trim();
            String mobile_txt = mobile.getText().toString().trim();
            String address_txt = address.getText().toString().trim();
            String dob_txt = dob.getText().toString().trim();

            // Validate input fields
            if (!validateInputFields(fname_txt, lname_txt, email_txt, password_txt, confirm_password_txt, mobile_txt, address_txt, dob_txt)) {
                return; // Stop the execution if validation fails
            }

            // First, check if the email or mobile already exists
            checkAccountExists(email_txt, mobile_txt, password_txt, fname_txt, lname_txt, address_txt, dob_txt);
        });
        return view;
    }


    private void switchToLoginFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.Frame_layout_container, new LoginFragment());
        fragmentTransaction.addToBackStack(null); // Add to back stack for navigation
        fragmentTransaction.commit();
    }
    private boolean validateInputFields(String fname, String lname, String email, String password, String confirmPassword, String mobile, String address, String dob) {
        if (fname.isEmpty() || !isValidName(fname)) {
            Toast.makeText(requireContext(), "Please enter a valid first name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (lname.isEmpty() || !isValidName(lname)) {
            Toast.makeText(requireContext(), "Please enter a valid last name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (email.isEmpty() || !isValidEmail(email)) {
            Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty() || !isValidPassword(password)) {
            Toast.makeText(requireContext(), "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mobile.isEmpty() || !isValidMobile(mobile)) {
            Toast.makeText(requireContext(), "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (dob.isEmpty()) {
            Toast.makeText(requireContext(), "Please select your date of birth", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void checkAccountExists(String email_txt, String mobile_txt, String password_txt, String fname_txt, String lname_txt, String address_txt, String dob_txt) {
        // Show ProgressBar while checking the account
        progressBar.setVisibility(View.VISIBLE);
        sign_expand_btn.setEnabled(false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Check if mobile number exists first
        checkForExistingField(db, "mobile", mobile_txt, () -> {
            // If mobile doesn't exist, check for email
            checkForExistingField(db, "email", email_txt, () -> {
                // If neither exists, proceed with registration
                registerUser(email_txt, password_txt, fname_txt, lname_txt, mobile_txt, address_txt, dob_txt);
            });
        });
    }

    // Method to check if a field (mobile or email) already exists in Firestore
    private void checkForExistingField(FirebaseFirestore db, String field, String value, Runnable onSuccess) {
        db.collection("users")
                .whereEqualTo(field, value)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Field exists (either mobile or email)
                        Toast.makeText(requireContext(), "Mobile or email already exists", Toast.LENGTH_SHORT).show();
                        sign_expand_btn.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    } else {
                        // Field does not exist, continue with the next step
                        onSuccess.run();
                    }
                });
    }


    private void registerUser(String email, String password, String fname, String lname, String mobile, String address, String dob) {
        // Now, create Firebase Authentication account after checking Firestore
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Registration successful, save user data to Firestore
                        saveUserToFirestore(email, password, fname, lname, mobile, address, dob);
                    } else {
                        // Registration failed
                        Toast.makeText(requireContext(), "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        sign_expand_btn.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void saveUserToFirestore(String email, String password, String fname, String lname, String mobile, String address, String dob) {
        // Get Firebase user ID
        String userId = mAuth.getCurrentUser().getUid();

        // Store additional user data in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", fname);
        user.put("lastName", lname);
        user.put("email", email);
        user.put("mobile", mobile);
        user.put("address", address);
        user.put("dob", dob);

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    // Firestore success
                    Log.d("SIGNUP", "User data added to Firestore");
                    Toast.makeText(requireContext(), "User Registered Successfully", Toast.LENGTH_SHORT).show();
                    sign_expand_btn.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    // Firestore failure
                    Toast.makeText(requireContext(), "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    sign_expand_btn.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                });
    }

    // Helper methods for validation
    public boolean isValidEmail(String email) {
        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public boolean isValidName(String name) {
        String namePattern = "^[A-Za-z\\s]+$";
        Pattern pattern = Pattern.compile(namePattern);
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    public boolean isValidMobile(String mobile) {
        String mobilePattern = "^[0-9]{10}$";
        Pattern pattern = Pattern.compile(mobilePattern);
        Matcher matcher = pattern.matcher(mobile);
        return matcher.matches();
    }

    public boolean isValidPassword(String password) {
        return password.length() >= 8;
    }

    // DatePickerDialog for DOB field
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year1, month1, dayOfMonth) -> {
                    dob.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1);
                },
                year, month, day
        );

        datePickerDialog.show();
    }
}