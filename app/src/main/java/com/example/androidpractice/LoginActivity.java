package com.example.androidpractice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);

        btnLogin.setOnClickListener(v -> {

            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty()) {
                etUsername.setError("Please enter your username or email");
                etUsername.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Please enter your password");
                etPassword.requestFocus();
                return;
            }

            SharedPreferences travellerPrefs = getSharedPreferences("UserData", MODE_PRIVATE);
            String savedUsername = travellerPrefs.getString("username", "");
            String savedPassword = travellerPrefs.getString("password", "");

            SharedPreferences businessPrefs = getSharedPreferences("business_profile", MODE_PRIVATE);
            String savedBusinessName = businessPrefs.getString("name", "");

            boolean isTravellerMatch = !savedUsername.isEmpty() && username.equalsIgnoreCase(savedUsername) && password.equals(savedPassword);
            boolean isBusinessMatch = !savedBusinessName.isEmpty() && username.equalsIgnoreCase(savedBusinessName);

            if (isBusinessMatch) {
                Toast.makeText(LoginActivity.this, "Welcome back, " + savedBusinessName + "!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, BusinessHomeActivity.class);
                intent.putExtra("business_name", savedBusinessName);
                startActivity(intent);
                finish();
                return;
            }

            if (isTravellerMatch) {
                Toast.makeText(LoginActivity.this, "Welcome back, " + savedUsername + "!", Toast.LENGTH_SHORT).show();
                checkMapReturnOrRouteToHome(savedUsername);
                return;
            }

            // Firebase Authentication login fallback
            if (FirebaseAuthManager.isFirebaseReady() && username.contains("@")) {
                FirebaseAuthManager.loginUser(username, password, new FirebaseAuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(String userId, String email, String role) {
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            if ("business".equalsIgnoreCase(role)) {
                                Intent intent = new Intent(LoginActivity.this, BusinessHomeActivity.class);
                                startActivity(intent);
                            } else {
                                checkMapReturnOrRouteToHome(username);
                            }
                            finish();
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
                    }
                });
            } else {
                // If local credentials exist for traveller or guest fallback
                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                checkMapReturnOrRouteToHome(username);
            }
        });

        tvCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, TravellerIntroActivity.class);
            startActivity(intent);
        });
    }

    private void checkMapReturnOrRouteToHome(String username) {
        boolean isReturnToMap = getIntent().getBooleanExtra("isReturnToMap", false);
        if (isReturnToMap) {
            Intent intent = new Intent(LoginActivity.this, MapRouteActivity.class);
            intent.putExtra(trackor.EXTRA_START_LOCATION, getIntent().getStringExtra(trackor.EXTRA_START_LOCATION));
            intent.putExtra(trackor.EXTRA_DROP_LOCATION, getIntent().getStringExtra(trackor.EXTRA_DROP_LOCATION));
            intent.putExtra(trackor.EXTRA_TRAVEL_MODE, getIntent().getStringExtra(trackor.EXTRA_TRAVEL_MODE));
            intent.putExtra(trackor.EXTRA_USE_LIVE_GPS, getIntent().getBooleanExtra(trackor.EXTRA_USE_LIVE_GPS, false));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        }
        finish();
    }
}