package com.example.androidpractice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BusinessIntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_intro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnCreateProfile = findViewById(R.id.btnCreateProfile);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessIntroActivity.this, BusinessLoginActivity.class);
            startActivity(intent);
        });

        btnCreateProfile.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessIntroActivity.this, BusinessOwnerProfileActivity.class);
            startActivity(intent);
        });
    }
}