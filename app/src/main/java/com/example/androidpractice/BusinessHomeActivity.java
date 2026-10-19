package com.example.androidpractice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class BusinessHomeActivity extends AppCompatActivity {

    private static final String TAG = "BusinessHomeActivity";
    private static final String PREFS_NAME = "business_profile";

    private TextView tvBusinessGreeting;
    private TextView tvStatusBadge;
    private TextView tvRestaurantTitle;
    private TextView tvRestaurantDetails;

    private Button btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_home);

        tvBusinessGreeting = findViewById(R.id.tvBusinessGreeting);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvRestaurantTitle = findViewById(R.id.tvRestaurantTitle);
        tvRestaurantDetails = findViewById(R.id.tvRestaurantDetails);

        btnSignOut = findViewById(R.id.btnSignOut);

        // Load profile
        loadBusinessProfile();

        btnSignOut.setOnClickListener(v -> {
            FirebaseAuthManager.logoutUser();
            Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(BusinessHomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Setup Business Bottom Navigation
        View bizNavHome = findViewById(R.id.bizNavHome);
        View bizNavAdd = findViewById(R.id.bizNavAdd);
        View bizNavUpdate = findViewById(R.id.bizNavUpdate);
        View bizNavProfile = findViewById(R.id.bizNavProfile);

        if (bizNavHome != null) {
            bizNavHome.setOnClickListener(v -> {
                // Already on Business Home
            });
        }

        if (bizNavAdd != null) {
            bizNavAdd.setOnClickListener(v -> {
                Intent intent = new Intent(BusinessHomeActivity.this, AddRestaurantActivity.class);
                startActivity(intent);
            });
        }

        if (bizNavUpdate != null) {
            bizNavUpdate.setOnClickListener(v -> {
                Intent intent = new Intent(BusinessHomeActivity.this, UpdateRestaurantActivity.class);
                startActivity(intent);
            });
        }

        if (bizNavProfile != null) {
            bizNavProfile.setOnClickListener(v -> {
                Intent intent = new Intent(BusinessHomeActivity.this, BusinessProfileActivity.class);
                intent.putExtra("is_edit_mode", true);
                startActivity(intent);
            });
        }
    }

    private void loadBusinessProfile() {
        // Read local SharedPreferences fallback first
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString("name", "");
        String address = prefs.getString("address", "");
        String phone = prefs.getString("phone", "");
        String hours = prefs.getString("hours", "");

        if (!name.isEmpty()) {
            tvBusinessGreeting.setText("Hi, " + name + " 👋");
            tvRestaurantTitle.setText(name);
            tvRestaurantDetails.setText("Address: " + address + "\nPhone: " + phone + "\nHours: " + hours);
            tvStatusBadge.setText("REGISTERED");
        } else {
            String passedName = getIntent().getStringExtra("business_name");
            if (passedName != null && !passedName.isEmpty()) {
                tvBusinessGreeting.setText("Hi, " + passedName + " 👋");
                tvRestaurantTitle.setText(passedName);
            }
        }

        // Fetch authenticated Firestore profile
        if (FirebaseAuthManager.isUserLoggedIn()) {
            String uid = FirebaseAuthManager.getCurrentUserId();
            if (uid != null && !uid.isEmpty()) {
                FirebaseFirestore.getInstance()
                        .collection("businesses")
                        .document(uid)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String firestoreName = documentSnapshot.getString("businessName");
                                String firestoreAddress = documentSnapshot.getString("address");
                                String firestorePhone = documentSnapshot.getString("phone");
                                String firestoreHours = documentSnapshot.getString("hours");
                                String status = documentSnapshot.getString("status");

                                if (firestoreName != null && !firestoreName.isEmpty()) {
                                    tvBusinessGreeting.setText("Hi, " + firestoreName + " 👋");
                                    tvRestaurantTitle.setText(firestoreName);
                                    tvRestaurantDetails.setText("Address: " + firestoreAddress + "\nPhone: " + firestorePhone + "\nHours: " + firestoreHours);
                                }

                                if (status != null && !status.isEmpty()) {
                                    tvStatusBadge.setText(status.toUpperCase());
                                } else {
                                    tvStatusBadge.setText("PENDING REVIEW");
                                }
                            }
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to load business profile: " + e.getMessage()));
            }
        }
    }
}