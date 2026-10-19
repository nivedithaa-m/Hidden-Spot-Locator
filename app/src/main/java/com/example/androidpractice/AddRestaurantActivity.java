package com.example.androidpractice;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddRestaurantActivity extends AppCompatActivity {

    private static final String TAG = "AddRestaurantActivity";

    private static final String[] CUISINES = {
            "North Indian", "South Indian", "Chinese", "Continental",
            "Italian", "Street Food", "Multi-cuisine", "Other"
    };

    private static final String[] PRICE_RANGES = {
            "₹ Budget-friendly", "₹₹ Mid-range", "₹₹₹ Fine dining"
    };

    private EditText etRestaurantName, etDescription, etAddress, etPhone, etHours;
    private EditText etMenuItemName, etMenuItemPrice;
    private Spinner spinnerCuisine, spinnerPrice;
    private Button btnSubmitRestaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_restaurant);

        etRestaurantName = findViewById(R.id.etRestaurantName);
        etDescription = findViewById(R.id.etDescription);
        etAddress = findViewById(R.id.etAddress);
        etPhone = findViewById(R.id.etPhone);
        etHours = findViewById(R.id.etHours);

        etMenuItemName = findViewById(R.id.etMenuItemName);
        etMenuItemPrice = findViewById(R.id.etMenuItemPrice);

        spinnerCuisine = findViewById(R.id.spinnerCuisine);
        spinnerPrice = findViewById(R.id.spinnerPrice);

        btnSubmitRestaurant = findViewById(R.id.btnSubmitRestaurant);

        spinnerCuisine.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, CUISINES));
        spinnerPrice.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, PRICE_RANGES));

        btnSubmitRestaurant.setOnClickListener(v -> submitRestaurantApplication());

        // Setup Business Bottom Navigation
        View bizNavHome = findViewById(R.id.bizNavHome);
        View bizNavUpdate = findViewById(R.id.bizNavUpdate);
        View bizNavProfile = findViewById(R.id.bizNavProfile);

        if (bizNavHome != null) {
            bizNavHome.setOnClickListener(v -> {
                Intent intent = new Intent(AddRestaurantActivity.this, BusinessHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (bizNavUpdate != null) {
            bizNavUpdate.setOnClickListener(v -> {
                Intent intent = new Intent(AddRestaurantActivity.this, UpdateRestaurantActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (bizNavProfile != null) {
            bizNavProfile.setOnClickListener(v -> {
                Intent intent = new Intent(AddRestaurantActivity.this, BusinessProfileActivity.class);
                intent.putExtra("is_edit_mode", true);
                startActivity(intent);
                finish();
            });
        }
    }

    private void submitRestaurantApplication() {
        String name = etRestaurantName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String hours = etHours.getText().toString().trim();

        String menuItemName = etMenuItemName.getText().toString().trim();
        String menuItemPrice = etMenuItemPrice.getText().toString().trim();

        if (name.isEmpty()) {
            etRestaurantName.setError("Restaurant name required");
            etRestaurantName.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            etAddress.setError("Address location required");
            etAddress.requestFocus();
            return;
        }

        if (phone.isEmpty() || phone.length() < 10) {
            etPhone.setError("Valid 10-digit phone number required");
            etPhone.requestFocus();
            return;
        }

        String ownerUid = FirebaseAuthManager.getCurrentUserId();
        if (ownerUid.isEmpty()) {
            ownerUid = "guest_owner";
        }

        Map<String, Object> submissionData = new HashMap<>();
        submissionData.put("name", name);
        submissionData.put("description", description);
        submissionData.put("address", address);
        submissionData.put("phone", phone);
        submissionData.put("hours", hours);
        submissionData.put("cuisine", CUISINES[spinnerCuisine.getSelectedItemPosition()]);
        submissionData.put("priceRange", PRICE_RANGES[spinnerPrice.getSelectedItemPosition()]);
        submissionData.put("menuItemName", menuItemName);
        submissionData.put("menuItemPrice", menuItemPrice);
        submissionData.put("ownerUid", ownerUid);
        submissionData.put("status", "pending");
        submissionData.put("createdAt", System.currentTimeMillis());

        if (FirebaseAuthManager.isUserLoggedIn()) {
            try {
                FirebaseFirestore.getInstance()
                        .collection("pending_submissions")
                        .add(submissionData)
                        .addOnSuccessListener(doc -> Log.d(TAG, "Application submitted with ID: " + doc.getId()))
                        .addOnFailureListener(e -> Log.e(TAG, "Submission failure: " + e.getMessage()));
            } catch (Exception e) {
                Log.e(TAG, "Firestore exception: " + e.getMessage());
            }
        }

        Toast.makeText(this, "Restaurant application submitted for admin review!", Toast.LENGTH_LONG).show();
        finish();
    }
}