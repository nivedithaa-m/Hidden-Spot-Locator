package com.example.androidpractice;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class BusinessProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "business_profile";

    private static final String[] CUISINES = {
            "North Indian", "South Indian", "Chinese", "Continental",
            "Italian", "Street Food", "Multi-cuisine", "Other"
    };

    private static final String[] PRICE_RANGES = {
            "₹ Budget-friendly", "₹₹ Mid-range", "₹₹₹ Fine dining"
    };

    private TextInputLayout businessNameLayout, addressLayout, phoneLayout;
    private TextInputEditText businessNameInput, descriptionInput,
            addressInput, phoneInput, hoursInput;
    private Spinner cuisineSpinner, priceSpinner;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_profile);

        businessNameLayout = findViewById(R.id.businessNameLayout);
        addressLayout = findViewById(R.id.addressLayout);
        phoneLayout = findViewById(R.id.phoneLayout);

        businessNameInput = findViewById(R.id.businessNameInput);
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

        loadSavedProfile();

        saveButton.setOnClickListener(v -> {
            if (validateForm()) {
                saveProfile();
            }
        });
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
        SharedPreferences.Editor editor =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

        editor.putString("name", businessNameInput.getText().toString().trim());
        editor.putInt("cuisineIndex", cuisineSpinner.getSelectedItemPosition());
        editor.putInt("priceIndex", priceSpinner.getSelectedItemPosition());
        editor.putString("description", descriptionInput.getText().toString().trim());
        editor.putString("address", addressInput.getText().toString().trim());
        editor.putString("phone", phoneInput.getText().toString().trim());
        editor.putString("hours", hoursInput.getText().toString().trim());
        editor.apply();

        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void loadSavedProfile() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        businessNameInput.setText(prefs.getString("name", ""));
        cuisineSpinner.setSelection(prefs.getInt("cuisineIndex", 0));
        priceSpinner.setSelection(prefs.getInt("priceIndex", 0));
        descriptionInput.setText(prefs.getString("description", ""));
        addressInput.setText(prefs.getString("address", ""));
        phoneInput.setText(prefs.getString("phone", ""));
        hoursInput.setText(prefs.getString("hours", ""));
    }
}