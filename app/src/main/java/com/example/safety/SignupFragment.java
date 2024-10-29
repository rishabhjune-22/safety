package com.example.safety;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.app.DatePickerDialog;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageView;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.yalantis.ucrop.UCrop;
import android.Manifest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.AspectRatio;

import java.io.File;


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
    private View view;
    private EditText  name,email,password,confirm_password,mobile,address,dob;
    private TextView back_to_login;
    private AppCompatButton sign_expand_btn;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private ShapeableImageView profileImage;
    private FloatingActionButton btn;
    private Uri photoUri;

    private FirebaseFirestore db;


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

    ActivityResultLauncher<Intent> cropImageResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    final Uri resultUri = UCrop.getOutput(result.getData());
                    if (resultUri != null) {
                        photoUri=resultUri;
                        profileImage.setImageURI(resultUri);
                    } else {
                        Toast.makeText(requireContext(), "Cropping failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uri != null) {
                    cropPhoto(uri);

                } else {
                    Toast.makeText(requireContext(),"No Image Selected", Toast.LENGTH_LONG).show();
                }
            });

    public void cropPhoto(Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(requireContext().getCacheDir(), "croppedImage.jpg"));

        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setAspectRatioOptions(0, new AspectRatio("1:1", 1, 1));
        options.setCompressionQuality(80);

        Intent uCropIntent = UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .getIntent(requireContext());

        // Clear ImageView cache to ensure the new image is visible
        profileImage.setImageURI(null);  // Clear the previous image
        cropImageResultLauncher.launch(uCropIntent); // Start uCrop activity
    }

    private ActivityResultLauncher<Intent> startCamera =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                    Uri imageUri = saveBitmapToFile(bitmap);
                    cropPhoto(imageUri);// Display the captured image
                } else {
                    Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            });

    private Uri saveBitmapToFile(Bitmap bitmap) {
        File cacheDir = requireContext().getCacheDir();
        File file = new File(cacheDir, "captured_image_" + System.currentTimeMillis() + ".jpg");

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show(); // Notify user of error
            e.printStackTrace();
        }

        return FileProvider.getUriForFile(requireContext(), "com.example.safety.fileprovider", file);
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted, launch the camera
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startCamera.launch(intent);
                } else {
                    Toast.makeText(requireContext(), "Camera permission is required to take photos.", Toast.LENGTH_SHORT).show();
                }
            });




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_signup, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        profileImage = view.findViewById(R.id.profile_photo);
        btn = view.findViewById(R.id.camera_ic);
        // Initialize UI elements
        name = view.findViewById(R.id.name_text);
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
            String name_txt = name.getText().toString().trim();
            String email_txt = email.getText().toString().trim();
            String password_txt = password.getText().toString().trim();
            String confirm_password_txt = confirm_password.getText().toString().trim();
            String mobile_txt = mobile.getText().toString().trim();
            String address_txt = address.getText().toString().trim();
            String dob_txt = dob.getText().toString().trim();

            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name_txt); userData.put("email", email_txt); userData.put("mobile", mobile_txt); userData.put("address", address_txt); userData.put("dob", dob_txt); userData.put("password", password_txt); userData.put("confirmPassword", confirm_password_txt);

            // Validate input fields
            if (!validateInputFields(userData)) {
                return; // Stop the execution if validation fails
            }

            // First, check if the email or mobile already exists
            checkAccountExists(userData);
        });

        btn.setOnClickListener(v -> {
            String[] options = {"Take Photo", "Choose from Gallery"};
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Select Image")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            // Request camera permission before taking a photo
                            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                // If permission is already granted, launch the camera
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startCamera.launch(intent);
                            } else {
                                // Request permission if not already granted
                                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                            }
                        } else if (which == 1) {
                            // Option to pick image from gallery
                            pickMedia.launch(new PickVisualMediaRequest.Builder()
                                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
                        }
                    }).show();
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
    private boolean validateInputFields(Map<String, Object> userData) {


        if (Objects.requireNonNull(userData.get("name")).toString().isEmpty() || !isValidName(Objects.requireNonNull(userData.get("name")).toString())) {
            Toast.makeText(requireContext(), "Please enter a valid name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (Objects.requireNonNull(userData.get("email")).toString().isEmpty() || !isValidEmail(Objects.requireNonNull(userData.get("email")).toString())) {
            Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (Objects.requireNonNull(userData.get("password")).toString().isEmpty() || !isValidPassword(Objects.requireNonNull(userData.get("password")).toString())) {
            Toast.makeText(requireContext(), "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Objects.requireNonNull(userData.get("password")).toString().equals(Objects.requireNonNull(userData.get("confirmPassword")).toString().trim())) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (Objects.requireNonNull(userData.get("mobile")).toString().isEmpty() || !isValidMobile(Objects.requireNonNull(userData.get("mobile")).toString())) {
            Toast.makeText(requireContext(), "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if ( Objects.requireNonNull(userData.get("dob")).toString().isEmpty()) {
            Toast.makeText(requireContext(), "Please select your date of birth", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void checkAccountExists(Map<String, Object> userData) {
        // Show ProgressBar while checking the account
        progressBar.setVisibility(View.VISIBLE);
        sign_expand_btn.setEnabled(false);


        // Check if mobile number exists first
        checkForExistingField(db, "mobile", Objects.requireNonNull(userData.get("mobile")).toString(), () -> {
            // If mobile doesn't exist, check for email
            checkForExistingField(db, "email", Objects.requireNonNull(userData.get("email")).toString(), () -> {
                // If neither exists, proceed with registration
                registerUser(userData);
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


    private void registerUser(Map<String, Object> userData) {
        if (photoUri == null) {
            Toast.makeText(requireContext(), "Please select an image before registering.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            sign_expand_btn.setEnabled(true);
            return;
        }


        // Now, create Firebase Authentication account after checking Firestore
        mAuth.createUserWithEmailAndPassword(Objects.requireNonNull(userData.get("email")).toString(), Objects.requireNonNull(userData.get("password")).toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Registration successful, save user data to Firestore
                        saveUserToFirestore(userData);
                    } else {
                        // Registration failed
                        Toast.makeText(requireContext(), "Authentication failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        sign_expand_btn.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void saveUserToFirestore(Map<String, Object> userData) {


        // Get Firebase user ID
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images/" + System.currentTimeMillis() + ".jpg");
        storageRef.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL after successful upload
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        userData.put("profileImageUrl", imageUrl); // Add image URL to user data

                        // Get Firebase user ID
                        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        // Store additional user data in Firestore


                        db.collection("users").document(userId)
                                .set(userData)
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
                    });
                }).addOnFailureListener(e -> {
                    // Image upload failure
                    Toast.makeText(requireContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    sign_expand_btn.setEnabled(true);
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
                    calendar.set(year1, month1, dayOfMonth); // Update calendar with selected date
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    dob.setText(sdf.format(calendar.getTime())); // Set formatted date to TextView
                },
                year, month, day
        );

        datePickerDialog.show();
    }
}