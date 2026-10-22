package com.example.androidpractice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BusinessDashboardActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "business_profile";
    private static final String PREFS_MENU = "business_menu";

    private static final String[] CUISINES = {
            "North Indian", "South Indian", "Chinese", "Continental",
            "Italian", "Street Food", "Multi-cuisine", "Other"
    };

    private static final String[] PRICE_RANGES = {
            "₹ Budget-friendly", "₹₹ Mid-range", "₹₹₹ Fine dining"
    };

    private TextView tvWelcomeUser;
    private TextView tvVisitedCount, tvEnquiredCount;
    private TextView tvRestaurantName, tvCuisinePrice, tvDescription, tvAddress, tvPhone, tvHours;
    private TextView tvMenuList;

    private Button btnUpdateMenu, btnEditRestaurantDetails;

    private String defaultMenu = "• Chef Special Thali — ₹220\n• Woodfired Pizza — ₹350\n• Fresh Forest Coffee — ₹90\n• Homemade Dessert — ₹120";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvVisitedCount = findViewById(R.id.tvVisitedCount);
        tvEnquiredCount = findViewById(R.id.tvEnquiredCount);

        tvRestaurantName = findViewById(R.id.tvRestaurantName);
        tvCuisinePrice = findViewById(R.id.tvCuisinePrice);
        tvDescription = findViewById(R.id.tvDescription);
        tvAddress = findViewById(R.id.tvAddress);
        tvPhone = findViewById(R.id.tvPhone);
        tvHours = findViewById(R.id.tvHours);

        tvMenuList = findViewById(R.id.tvMenuList);
        btnUpdateMenu = findViewById(R.id.btnUpdateMenu);
        btnEditRestaurantDetails = findViewById(R.id.btnEditRestaurantDetails);

        String ownerName = getIntent().getStringExtra("ownerName");
        if (ownerName != null && !ownerName.isEmpty()) {
            tvWelcomeUser.setText("Welcome, " + ownerName + " 👋");
        } else {
            tvWelcomeUser.setText("Welcome, Owner 👋");
        }

        loadRestaurantDetails();
        loadMenu();

        btnUpdateMenu.setOnClickListener(v -> showUpdateMenuDialog());

        btnEditRestaurantDetails.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessDashboardActivity.this, BusinessProfileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRestaurantDetails();
        loadMenu();
    }

    private void loadRestaurantDetails() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String name = prefs.getString("name", "The Hidden Valley Cafe");
        int cuisineIndex = prefs.getInt("cuisineIndex", 6); // default Multi-cuisine
        int priceIndex = prefs.getInt("priceIndex", 1); // default Mid-range
        String description = prefs.getString("description", "Cozy restaurant serving fresh local specials with scenic waterfall views.");
        String address = prefs.getString("address", "Near Waterfall Point, Main Highway");
        String phone = prefs.getString("phone", "+91 98765 43210");
        String hours = prefs.getString("hours", "10:00 AM - 10:00 PM");

        String cuisineStr = (cuisineIndex >= 0 && cuisineIndex < CUISINES.length) ? CUISINES[cuisineIndex] : "Multi-cuisine";
        String priceStr = (priceIndex >= 0 && priceIndex < PRICE_RANGES.length) ? PRICE_RANGES[priceIndex] : "₹₹ Mid-range";

        if (name.isEmpty()) name = "The Hidden Valley Cafe";
        if (description.isEmpty()) description = "Cozy restaurant serving fresh local specials with scenic waterfall views.";
        if (address.isEmpty()) address = "Near Waterfall Point, Main Highway";
        if (phone.isEmpty()) phone = "+91 98765 43210";
        if (hours.isEmpty()) hours = "10:00 AM - 10:00 PM";

        tvRestaurantName.setText(name);
        tvCuisinePrice.setText(cuisineStr + " • " + priceStr);
        tvDescription.setText(description);
        tvAddress.setText("📍 Location: " + address);
        tvPhone.setText("📞 Phone: " + phone);
        tvHours.setText("⏰ Hours: " + hours);

        // Stats (can be incremented / stored in prefs)
        int visits = prefs.getInt("visits", 148);
        int enquiries = prefs.getInt("enquiries", 42);

        tvVisitedCount.setText(String.valueOf(visits));
        tvEnquiredCount.setText(String.valueOf(enquiries));
    }

    private void loadMenu() {
        SharedPreferences prefs = getSharedPreferences(PREFS_MENU, MODE_PRIVATE);
        String menuText = prefs.getString("menu_items", defaultMenu);
        tvMenuList.setText(menuText);
    }

    private void showUpdateMenuDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_MENU, MODE_PRIVATE);
        String currentMenu = prefs.getString("menu_items", defaultMenu);

        EditText input = new EditText(this);
        input.setText(currentMenu);
        input.setLines(6);
        input.setPadding(32, 24, 32, 24);

        new AlertDialog.Builder(this)
                .setTitle("Update Menu & Prices")
                .setMessage("Add or edit your restaurant's menu items and prices below:")
                .setView(input)
                .setPositiveButton("Save Menu", (dialog, which) -> {
                    String updatedMenu = input.getText().toString().trim();
                    if (!updatedMenu.isEmpty()) {
                        prefs.edit().putString("menu_items", updatedMenu).apply();
                        tvMenuList.setText(updatedMenu);
                        Toast.makeText(this, "Menu updated successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}