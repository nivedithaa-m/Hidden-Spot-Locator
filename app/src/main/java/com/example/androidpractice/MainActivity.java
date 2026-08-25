package com.example.androidpractice;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText emailInput;
    EditText passwordInput;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Connect Java variables to the XML views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        // What happens when LOGIN is clicked
        loginButton.setOnClickListener(view -> {

            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                        MainActivity.this,
                        "Please enter email and password",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                        MainActivity.this,
                        "Login successful!",
                        Toast.LENGTH_SHORT
                ).show();

            }

        });
    }
}