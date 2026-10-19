package com.example.androidpractice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class AddHiddenSpotActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hidden_spot);

        View navHome = findViewById(R.id.navHome);
        View navExploreBottom = findViewById(R.id.navExploreBottom);
        View navProfile = findViewById(R.id.navProfile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(AddHiddenSpotActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navExploreBottom != null) {
            navExploreBottom.setOnClickListener(v -> {
                Intent intent = new Intent(AddHiddenSpotActivity.this, trackor.class);
                startActivity(intent);
                finish();
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(AddHiddenSpotActivity.this, TravellerProfileDisplayActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}