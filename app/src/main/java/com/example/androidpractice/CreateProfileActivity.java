package com.example.androidpractice;

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

        // Traveller selected
        travellerCard.setOnClickListener(v -> {

            selectedRole = "traveller";

            travellerCard.setSelected(true);
            businessCard.setSelected(false);

            continueButton.setEnabled(true);
            continueButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#1976D2")
                    )
            );
        });

        // Business selected
        businessCard.setOnClickListener(v -> {

            selectedRole = "business";

            businessCard.setSelected(true);
            travellerCard.setSelected(false);

            continueButton.setEnabled(true);
            continueButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#1976D2")
                    )
            );
        });
    }
}