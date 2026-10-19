package com.example.androidpractice;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout navHome;
    private View navExplore;
    private View navAdd;
    private LinearLayout navProfile;

    private TextView greetingText;
    private ImageView homeProfileImage;

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
        homeProfileImage = findViewById(R.id.homeProfileImage);

        // Load user name
        String username = getIntent().getStringExtra("username");
        if (username != null && !username.isEmpty()) {
            greetingText.setText("Hi, " + username + " 👋");
        } else if (FirebaseAuthManager.isUserLoggedIn()) {
            fetchUserProfileFromFirestore();
        }

        // Load profile photo
        String profileImageUri = getIntent().getStringExtra("profileImageUri");
        if (profileImageUri != null && !profileImageUri.isEmpty()) {
            homeProfileImage.setImageURI(Uri.parse(profileImageUri));
        } else {
            byte[] profileImageBytes = getIntent().getByteArrayExtra("profileImageBytes");
            if (profileImageBytes != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(profileImageBytes, 0, profileImageBytes.length);
                homeProfileImage.setImageBitmap(bitmap);
            }
        }

        // HOME TAB
        navHome.setOnClickListener(v -> {
            // Already on Home
        });

        // EXPLORE TAB & EXPLORE CARD
        LinearLayout navExploreBottom = findViewById(R.id.navExploreBottom);
        View.OnClickListener openExploreListener = v -> {
            Intent intent = new Intent(HomeActivity.this, trackor.class);
            startActivity(intent);
        };

        if (navExplore != null) navExplore.setOnClickListener(openExploreListener);
        if (navExploreBottom != null) navExploreBottom.setOnClickListener(openExploreListener);

        // ADD TAB & ADD CARD
        LinearLayout navAddBottom = findViewById(R.id.navAddBottom);
        View.OnClickListener openAddListener = v -> {
            Intent intent = new Intent(HomeActivity.this, AddHiddenSpotActivity.class);
            startActivity(intent);
        };

        if (navAdd != null) navAdd.setOnClickListener(openAddListener);
        if (navAddBottom != null) navAddBottom.setOnClickListener(openAddListener);

        // PROFILE TAB
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, TravellerProfileDisplayActivity.class);
                startActivity(intent);
            });
        }
    }

    private void fetchUserProfileFromFirestore() {
        String uid = FirebaseAuthManager.getCurrentUserId();
        if (uid != null && !uid.isEmpty()) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("username");
                            if (name == null) name = doc.getString("displayName");
                            if (name != null && !name.isEmpty()) {
                                greetingText.setText("Hi, " + name + " 👋");
                            }
                        }
                    });
        }
    }
}