package com.example.androidpractice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout navHome;
    private LinearLayout navExplore;
    private LinearLayout navAdd;
    private LinearLayout navProfile;

    private TextView greetingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Connect views
        navHome = findViewById(R.id.navHome);
        navExplore = findViewById(R.id.navExplore);
        navAdd = findViewById(R.id.navAdd);
        navProfile = findViewById(R.id.navProfile);

        greetingText = findViewById(R.id.greetingText);

        // Get username from CreateProfileActivity
        String username = getIntent().getStringExtra("username");

        if (username != null && !username.isEmpty()) {
            greetingText.setText("Hi, " + username + " 👋");
        }

        // HOME
        navHome.setOnClickListener(v -> {
            // Already on Home
        });

        // EXPLORE
        navExplore.setOnClickListener(v -> {

            // Explore page will be connected here later

        });

        // ADD SPOT
        navAdd.setOnClickListener(v -> {

            // Add Hidden Spot page will be connected here later

        });

        // PROFILE
        navProfile.setOnClickListener(v -> {

            // Profile page will be connected here later

        });
    }
}