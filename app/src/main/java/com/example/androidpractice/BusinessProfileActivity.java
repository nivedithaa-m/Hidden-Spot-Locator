package com.example.androidpractice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BusinessProfileActivity extends AppCompatActivity {

    private static final String TAG = "BusinessProfileActivity";
    private static final String PREFS_NAME = "business_profile";

    private static final String[] CUISINES = {
            "North Indian", "South Indian", "Chinese", "Continental",
            "Italian", "Street Food", "Multi-cuisine", "Other"
    };

    private static final String[] PRICE_RANGES = {
            "₹ Budget-friendly", "₹₹ Mid-range", "₹₹₹ Fine dining"
    };

    private View emailRowLayout, passwordRowLayout;
    private TextInputLayout businessNameLayout, addressLayout, phoneLayout, emailLayout, passwordLayout;
    private TextInputEditText businessNameInput, descriptionInput,
            addressInput, phoneInput, hoursInput, emailInput, passwordInput;
    private Spinner cuisineSpinner, priceSpinner;
    private Button saveButton;

    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_profile);

        isEditMode = getIntent().getBooleanExtra("is_edit_mode", false);

        emailRowLayout = findViewById(R.id.emailRowLayout);
        passwordRowLayout = findViewById(R.id.passwordRowLayout);

        businessNameLayout = findViewById(R.id.businessNameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        addressLayout = findViewById(R.id.addressLayout);
        phoneLayout = findViewById(R.id.phoneLayout);

        businessNameInput = findViewById(R.id.businessNameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        addressInput = findViewById(R.id.addressInput);
        phoneInput = findViewById(R.id.phoneInput);
        hoursInput = findViewById(R.id.hoursInput);

        cuisineSpinner = findViewById(R.id.cuisineSpinner);
        priceSpinner = findViewById(R.id.priceSpinner);
        saveButton = findViewById(R.id.saveButton);

        cuisineSpinner.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, CUISINES));
        priceSpinner.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, PRICE_RANGES));

        if (isEditMode) {
            saveButton.setText("Update Profile");
            if (emailRowLayout != null) emailRowLayout.setVisibility(View.GONE);
            if (passwordRowLayout != null) passwordRowLayout.setVisibility(View.GONE);
            loadSavedProfile();
        } else {
            saveButton.setText("Create Profile");
            if (emailRowLayout != null) emailRowLayout.setVisibility(View.VISIBLE);
            if (passwordRowLayout != null) passwordRowLayout.setVisibility(View.VISIBLE);
            clearForm();
        }

        saveButton.setOnClickListener(v -> {
            if (validateForm()) {
                saveProfile();
            }
        });
    }

    private void clearForm() {
        businessNameInput.setText("");
        if (emailInput != null) emailInput.setText("");
        if (passwordInput != null) passwordInput.setText("");
        descriptionInput.setText("");
        addressInput.setText("");
        phoneInput.setText("");
        hoursInput.setText("");
        cuisineSpinner.setSelection(0);
        priceSpinner.setSelection(0);
    }

    private boolean validateForm() {
        boolean valid = true;

        String name = businessNameInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        if (name.isEmpty()) {
            businessNameLayout.setError("Please enter your restaurant name");
            valid = false;
        } else {
            businessNameLayout.setError(null);
        }

        if (!isEditMode) {
            String email = emailInput != null ? emailInput.getText().toString().trim() : "";
            String password = passwordInput != null ? passwordInput.getText().toString().trim() : "";

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (emailLayout != null) emailLayout.setError("Valid business email required");
                valid = false;
            } else {
                if (emailLayout != null) emailLayout.setError(null);
            }

            if (password.length() < 6) {
                if (passwordLayout != null) passwordLayout.setError("Password must be at least 6 characters");
                valid = false;
            } else {
                if (passwordLayout != null) passwordLayout.setError(null);
            }
        }

        if (address.isEmpty()) {
            addressLayout.setError("Please enter your address");
            valid = false;
        } else {
            addressLayout.setError(null);
        }

        if (phone.length() < 10 || !Patterns.PHONE.matcher(phone).matches()) {
            phoneLayout.setError("Enter a valid phone number");
            valid = false;
        } else {
            phoneLayout.setError(null);
        }

        return valid;
    }

    private void saveProfile() {
        String name = businessNameInput.getText().toString().trim();
        String email = emailInput != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput != null ? passwordInput.getText().toString().trim() : "";
        String description = descriptionInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String hours = hoursInput.getText().toString().trim();

        saveButton.setEnabled(false);

        // Save locally to SharedPreferences
        SharedPreferences.Editor editor =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

        editor.putString("name", name);
        if (!email.isEmpty()) editor.putString("email", email);
        editor.putInt("cuisineIndex", cuisineSpinner.getSelectedItemPosition());
        editor.putInt("priceIndex", priceSpinner.getSelectedItemPosition());
        editor.putString("description", description);
        editor.putString("address", address);
        editor.putString("phone", phone);
        editor.putString("hours", hours);
        editor.apply();

        if (isEditMode) {
            // Updating existing authenticated profile
            if (FirebaseAuthManager.isUserLoggedIn()) {
                String uid = FirebaseAuthManager.getCurrentUserId();
                if (uid != null && !uid.isEmpty()) {
                    try {
                        Map<String, Object> businessData = new HashMap<>();
                        businessData.put("businessName", name);
                        businessData.put("cuisine", CUISINES[cuisineSpinner.getSelectedItemPosition()]);
                        businessData.put("priceRange", PRICE_RANGES[priceSpinner.getSelectedItemPosition()]);
                        businessData.put("description", description);
                        businessData.put("address", address);
                        businessData.put("phone", phone);
                        businessData.put("hours", hours);
                        businessData.put("ownerUid", uid);
                        businessData.put("updatedAt", System.currentTimeMillis());

                        FirebaseFirestore.getInstance()
                                .collection("businesses")
                                .document(uid)
                                .set(businessData);

                    } catch (Exception e) {
                        Log.e(TAG, "Firestore sync exception: " + e.getMessage());
                    }
                }
            }

            Toast.makeText(this, "Business profile updated successfully!", Toast.LENGTH_SHORT).show();
            navigateToBusinessHome(name);

        } else {
            // New business account registration using Firebase Auth
            if (FirebaseAuthManager.isFirebaseReady()) {
                Map<String, Object> extraFields = new HashMap<>();
                extraFields.put("phone", phone);

                FirebaseAuthManager.registerUser(email, password, name, "business", extraFields, new FirebaseAuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(String userId, String userEmail, String role) {
                        // Save business record in businesses/{userId}
                        try {
                            Map<String, Object> businessData = new HashMap<>();
                            businessData.put("ownerUid", userId);
                            businessData.put("businessName", name);
                            businessData.put("email", userEmail);
                            businessData.put("cuisine", CUISINES[cuisineSpinner.getSelectedItemPosition()]);
                            businessData.put("priceRange", PRICE_RANGES[priceSpinner.getSelectedItemPosition()]);
                            businessData.put("description", description);
                            businessData.put("address", address);
                            businessData.put("phone", phone);
                            businessData.put("hours", hours);
                            businessData.put("status", "registered");
                            businessData.put("createdAt", System.currentTimeMillis());

                            FirebaseFirestore.getInstance()
                                    .collection("businesses")
                                    .document(userId)
                                    .set(businessData)
                                    .addOnSuccessListener(aVoid -> {
                                        runOnUiThread(() -> {
                                            Toast.makeText(BusinessProfileActivity.this, "Business account and profile created successfully!", Toast.LENGTH_SHORT).show();
                                            navigateToBusinessHome(name);
                                        });
                                    })
                                    .addOnFailureListener((Exception e) -> {
                                        runOnUiThread(() -> {
                                            saveButton.setEnabled(true);
                                            Toast.makeText(BusinessProfileActivity.this, "Account created, but Firestore profile save failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                        });
                                    });

                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                saveButton.setEnabled(true);
                                Toast.makeText(BusinessProfileActivity.this, "Error saving business record: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            saveButton.setEnabled(true);
                            Toast.makeText(BusinessProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            } else {
                Toast.makeText(this, "Business profile saved locally.", Toast.LENGTH_SHORT).show();
                navigateToBusinessHome(name);
            }
        }
    }

    private void navigateToBusinessHome(String name) {
        String ownerName = getIntent().getStringExtra("ownerName");
        Intent intent = new Intent(BusinessProfileActivity.this, BusinessHomeActivity.class);
        if (ownerName != null && !ownerName.isEmpty()) {
            intent.putExtra("ownerName", ownerName);
        } else {
            intent.putExtra("ownerName", name);
        }
        intent.putExtra("business_name", name);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadSavedProfile() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        businessNameInput.setText(prefs.getString("name", ""));
        if (emailInput != null) emailInput.setText(prefs.getString("email", ""));
        cuisineSpinner.setSelection(prefs.getInt("cuisineIndex", 0));
        priceSpinner.setSelection(prefs.getInt("priceIndex", 0));
        descriptionInput.setText(prefs.getString("description", ""));
        addressInput.setText(prefs.getString("address", ""));
        phoneInput.setText(prefs.getString("phone", ""));
        hoursInput.setText(prefs.getString("hours", ""));
    }
}