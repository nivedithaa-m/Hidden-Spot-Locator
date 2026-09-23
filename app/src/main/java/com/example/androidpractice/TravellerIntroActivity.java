package com.example.androidpractice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TravellerIntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_traveller_intro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button continueButton = findViewById(R.id.btnTravellerContinue);
        TextView skipButton = findViewById(R.id.tvTravellerSkip);

        continueButton.setOnClickListener(v -> {
            Intent intent = new Intent(TravellerIntroActivity.this, TravellerProfileActivity.class);
            startActivity(intent);
        });
        skipButton.setOnClickListener(v -> {
            // We will connect Skip to the next part of the flow later.
        });
    }
}