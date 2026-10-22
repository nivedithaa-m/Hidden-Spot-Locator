package com.example.androidpractice;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BusinessLoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private Button signInButton;
    private TextView goToCreateProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailInput = findViewById(R.id.loginEmailInput);
        passwordInput = findViewById(R.id.loginPasswordInput);
        signInButton = findViewById(R.id.btnBusinessSignIn);
        goToCreateProfile = findViewById(R.id.tvGoToCreateProfile);

        // Password Show/Hide Toggle
        passwordInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (passwordInput.getCompoundDrawables()[2] != null
                        && event.getX() >= passwordInput.getWidth()
                        - passwordInput.getCompoundDrawables()[2].getBounds().width()
                        - passwordInput.getPaddingEnd()) {

                    if (passwordInput.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        passwordInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility, 0);
                    } else {
                        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passwordInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0);
                    }
                    passwordInput.setSelection(passwordInput.length());
                    return true;
                }
            }
            return false;
        });

        signInButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty()) {
                emailInput.setError("Please enter your email or restaurant ID");
                return;
            }

            if (password.isEmpty()) {
                passwordInput.setError("Please enter your password");
                return;
            }

            Intent intent = new Intent(BusinessLoginActivity.this, BusinessDashboardActivity.class);
            intent.putExtra("ownerName", email.contains("@") ? email.split("@")[0] : email);
            intent.putExtra("isLoggedIn", true);
            startActivity(intent);
            finish();
        });

        goToCreateProfile.setOnClickListener(v -> {
            Intent intent = new Intent(BusinessLoginActivity.this, BusinessOwnerProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }
}