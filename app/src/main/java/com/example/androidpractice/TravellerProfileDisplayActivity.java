package com.example.androidpractice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class TravellerProfileDisplayActivity extends AppCompatActivity {

    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private Button btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traveller_profile_display);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        btnSignOut = findViewById(R.id.btnSignOut);

        loadProfileDetails();

        btnSignOut.setOnClickListener(v -> {
            FirebaseAuthManager.logoutUser();
            Toast.makeText(this, "Signed out successfully.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(TravellerProfileDisplayActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        View navHome = findViewById(R.id.navHome);
        View navExploreBottom = findViewById(R.id.navExploreBottom);
        View navAddBottom = findViewById(R.id.navAddBottom);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(TravellerProfileDisplayActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navExploreBottom != null) {
            navExploreBottom.setOnClickListener(v -> {
                Intent intent = new Intent(TravellerProfileDisplayActivity.this, trackor.class);
                startActivity(intent);
                finish();
            });
        }

        if (navAddBottom != null) {
            navAddBottom.setOnClickListener(v -> {
                Intent intent = new Intent(TravellerProfileDisplayActivity.this, AddHiddenSpotActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void loadProfileDetails() {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String name = prefs.getString("username", "Traveller");
        String email = prefs.getString("email", "traveller@example.com");

        tvProfileName.setText(name);
        tvProfileEmail.setText(email);

        if (FirebaseAuthManager.isUserLoggedIn()) {
            String uid = FirebaseAuthManager.getCurrentUserId();
            if (uid != null && !uid.isEmpty()) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                String firestoreName = doc.getString("username");
                                if (firestoreName == null) firestoreName = doc.getString("displayName");
                                String firestoreEmail = doc.getString("email");

                                if (firestoreName != null && !firestoreName.isEmpty()) tvProfileName.setText(firestoreName);
                                if (firestoreEmail != null && !firestoreEmail.isEmpty()) tvProfileEmail.setText(firestoreEmail);
                            }
                        });
            }
        }
    }
}