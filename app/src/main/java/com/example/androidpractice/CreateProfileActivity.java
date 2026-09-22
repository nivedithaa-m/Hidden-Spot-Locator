package com.example.androidpractice;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class CreateProfileActivity extends AppCompatActivity {

    private LinearLayout travellerCard;
    private LinearLayout businessCard;
    private Button continueButton;

    private String selectedRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_profile);

        travellerCard = findViewById(R.id.travellerCard);
        businessCard = findViewById(R.id.businessCard);
        continueButton = findViewById(R.id.continueButton);

        travellerCard.setOnClickListener(v -> {
            selectedRole = "traveller";
            travellerCard.setSelected(true);
            businessCard.setSelected(false);
            enableContinueButton();
        });

        businessCard.setOnClickListener(v -> {
            selectedRole = "business";
            businessCard.setSelected(true);
            travellerCard.setSelected(false);
            enableContinueButton();
        });

        continueButton.setOnClickListener(view -> {
            if (selectedRole.equals("traveller")) {
                startActivity(new Intent(
                        CreateProfileActivity.this,
                        TravellerProfileActivity.class));

            } else if (selectedRole.equals("business")) {
                startActivity(new Intent(
                        CreateProfileActivity.this,
                        BusinessProfileActivity.class));
            }
        });
    }

    private void enableContinueButton() {
        continueButton.setEnabled(true);
        continueButton.setBackgroundTintList(
                ColorStateList.valueOf(Color.parseColor("#1976D2")));
    }
}