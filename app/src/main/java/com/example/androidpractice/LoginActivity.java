package com.example.androidpractice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin;
    TextView tvCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);

        // Login button
        btnLogin.setOnClickListener(v -> {

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

            // Temporary login
            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);

            String savedUsername = prefs.getString("username", "");
            String savedPassword = prefs.getString("password", "");

            if (username.equals(savedUsername) &&
                    password.equals(savedPassword)) {

                Toast.makeText(
                        LoginActivity.this,
                        "Login successful",
                        Toast.LENGTH_SHORT
                ).show();

                Intent intent = new Intent(
                        LoginActivity.this,
                        HomeActivity.class
                );

                startActivity(intent);
                finish();

            } else {
                Toast.makeText(
                        LoginActivity.this,
                        "Invalid username or password",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Create Account
        tvCreateAccount.setOnClickListener(v -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    TravellerIntroActivity.class
            );

            startActivity(intent);
        });
    }
}