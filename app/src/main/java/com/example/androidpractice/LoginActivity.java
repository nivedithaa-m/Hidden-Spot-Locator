package com.example.androidpractice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ProgressBar loginProgressBar;
    private TextView tvCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        loginProgressBar = findViewById(R.id.loginProgressBar);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);

        // Subtle entrance animation for the welcome heading
        View welcomeContainer = findViewById(R.id.welcomeContainer);
        if (welcomeContainer != null) {
            welcomeContainer.setAlpha(0f);
            welcomeContainer.setTranslationY(30f);
            welcomeContainer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        btnLogin.setOnClickListener(v -> handleLoginClick());

        tvCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, TravellerIntroActivity.class);
            startActivity(intent);
        });
    }

    private void handleLoginClick() {
        etUsername.setError(null);
        etPassword.setError(null);

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Please enter your username");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Please enter your password");
            etPassword.requestFocus();
            return;
        }

        setLoading(true);

        SharedPreferences travellerPrefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String savedUsername = travellerPrefs.getString("username", "");
        String savedPassword = travellerPrefs.getString("password", "");
        String savedEmail = travellerPrefs.getString("email", "");

        SharedPreferences businessPrefs = getSharedPreferences("business_profile", MODE_PRIVATE);
        String savedBusinessName = businessPrefs.getString("name", "");

        boolean isTravellerMatch = (!savedUsername.isEmpty() && username.equalsIgnoreCase(savedUsername) && password.equals(savedPassword))
                || (!savedEmail.isEmpty() && username.equalsIgnoreCase(savedEmail) && password.equals(savedPassword));
        boolean isBusinessMatch = !savedBusinessName.isEmpty() && username.equalsIgnoreCase(savedBusinessName);

        if (isBusinessMatch) {
            setLoading(false);
            Toast.makeText(LoginActivity.this, "Welcome back, " + savedBusinessName + "!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, BusinessHomeActivity.class);
            intent.putExtra("business_name", savedBusinessName);
            startActivity(intent);
            finish();
            return;
        }

        if (isTravellerMatch) {
            setLoading(false);
            Toast.makeText(LoginActivity.this, "Welcome back, " + savedUsername + "!", Toast.LENGTH_SHORT).show();
            navigateAfterLogin(savedUsername);
            return;
        }

        // Firebase Authentication login fallback
        if (FirebaseAuthManager.isFirebaseReady()) {
            String loginEmail = username;
            if (!loginEmail.contains("@") && !savedEmail.isEmpty() && username.equalsIgnoreCase(savedUsername)) {
                loginEmail = savedEmail;
            }

            FirebaseAuthManager.loginUser(loginEmail, password, new FirebaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(String userId, String email, String role) {
                    runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        if ("business".equalsIgnoreCase(role)) {
                            Intent intent = new Intent(LoginActivity.this, BusinessHomeActivity.class);
                            startActivity(intent);
                        } else {
                            navigateAfterLogin(username);
                        }
                        finish();
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            });
        } else {
            setLoading(false);
            Toast.makeText(LoginActivity.this, "Invalid credentials. Please try again or create an account.", Toast.LENGTH_LONG).show();
        }
    }

    private void setLoading(boolean loading) {
        if (btnLogin != null) {
            btnLogin.setEnabled(!loading);
        }
        if (loginProgressBar != null) {
            loginProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void navigateAfterLogin(String username) {
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
            Intent intent = new Intent(LoginActivity.this, TravellerIntroActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        }
        finish();
    }
}