package com.example.androidpractice;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;

import java.util.Calendar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TravellerProfileActivity extends AppCompatActivity {

    private EditText nameInput;
    private EditText emailInput;
    private EditText phoneInput;
    private EditText dobInput;
    private EditText passwordInput;

    private Button createProfileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_traveller_profile);

        // Connect input fields
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        dobInput = findViewById(R.id.dobInput);
        passwordInput = findViewById(R.id.passwordInput);

        // Connect Create Profile button
        createProfileButton = findViewById(R.id.createProfileButton);

        // Initially disable the button
        createProfileButton.setEnabled(false);

        // -------------------------------------------------
        // PASSWORD SHOW / HIDE
        // -------------------------------------------------

        passwordInput.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_UP) {

                if (event.getX() >= passwordInput.getWidth()
                        - passwordInput.getCompoundDrawables()[2].getBounds().width()
                        - passwordInput.getPaddingEnd()) {

                    if (passwordInput.getInputType()
                            == (InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD)) {

                        // Show password
                        passwordInput.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        );

                        // Change to open eye
                        passwordInput.setCompoundDrawablesWithIntrinsicBounds(
                                0, 0, R.drawable.ic_visibility, 0
                        );

                    } else {

                        // Hide password
                        passwordInput.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_PASSWORD
                        );

                        // Change to closed eye
                        passwordInput.setCompoundDrawablesWithIntrinsicBounds(
                                0, 0, R.drawable.ic_visibility_off, 0
                        );
                    }

                    // Keep cursor at the end
                    passwordInput.setSelection(passwordInput.length());

                    return true;
                }
            }

            return false;
        });

        // -------------------------------------------------
        // EMAIL VALIDATION
        // -------------------------------------------------

        emailInput.setOnFocusChangeListener((view, hasFocus) -> {

            if (!hasFocus) {

                String email = emailInput.getText().toString().trim();

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailInput.setError("Invalid email");
                }
            }
        });

        // -------------------------------------------------
        // PHONE VALIDATION
        // -------------------------------------------------

        phoneInput.setOnFocusChangeListener((view, hasFocus) -> {

            if (!hasFocus) {

                String phone = phoneInput.getText().toString().trim();

                if (!phone.matches("[6-9][0-9]{9}")) {
                    phoneInput.setError("Invalid phone number");
                }
            }
        });

        // -------------------------------------------------
        // DATE OF BIRTH
        // YEAR → MONTH → DAY
        // -------------------------------------------------

        dobInput.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            int currentYear = calendar.get(Calendar.YEAR);

            // Step 1: Select Year

            final int[] selectedYear = new int[1];

            String[] years = new String[currentYear - 1900 + 1];

            for (int i = 0; i < years.length; i++) {
                years[i] = String.valueOf(currentYear - i);
            }

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Select Year")
                    .setItems(years, (dialog, which) -> {

                        selectedYear[0] =
                                Integer.parseInt(years[which]);

                        // Step 2: Select Month

                        String[] months = {
                                "January",
                                "February",
                                "March",
                                "April",
                                "May",
                                "June",
                                "July",
                                "August",
                                "September",
                                "October",
                                "November",
                                "December"
                        };

                        new android.app.AlertDialog.Builder(this)
                                .setTitle("Select Month")
                                .setItems(months, (monthDialog, monthWhich) -> {

                                    int selectedMonth = monthWhich;

                                    // Step 3: Select Day

                                    Calendar selectedDate =
                                            Calendar.getInstance();

                                    selectedDate.set(
                                            selectedYear[0],
                                            selectedMonth,
                                            1
                                    );

                                    int daysInMonth =
                                            selectedDate.getActualMaximum(
                                                    Calendar.DAY_OF_MONTH
                                            );

                                    String[] days =
                                            new String[daysInMonth];

                                    for (int i = 0;
                                         i < daysInMonth;
                                         i++) {

                                        days[i] =
                                                String.valueOf(i + 1);
                                    }

                                    new android.app.AlertDialog.Builder(this)
                                            .setTitle("Select Day")
                                            .setItems(days, (dayDialog, dayWhich) -> {

                                                int selectedDay =
                                                        dayWhich + 1;

                                                // Set selected date

                                                selectedDate.set(
                                                        selectedYear[0],
                                                        selectedMonth,
                                                        selectedDay
                                                );

                                                // Get today's date

                                                Calendar today =
                                                        Calendar.getInstance();

                                                // Prevent future DOB

                                                if (selectedDate.after(today)) {

                                                    dobInput.setError(
                                                            "Invalid date of birth"
                                                    );

                                                    return;
                                                }

                                                // Format DOB

                                                String date =
                                                        String.format(
                                                                "%02d/%02d/%04d",
                                                                selectedDay,
                                                                selectedMonth + 1,
                                                                selectedYear[0]
                                                        );

                                                dobInput.setText(date);
                                            })
                                            .show();
                                })
                                .show();
                    })
                    .show();
        });

        // -------------------------------------------------
        // CREATE PROFILE BUTTON
        // -------------------------------------------------

        TextWatcher textWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                checkFields();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        // Watch all fields

        nameInput.addTextChangedListener(textWatcher);
        emailInput.addTextChangedListener(textWatcher);
        phoneInput.addTextChangedListener(textWatcher);
        dobInput.addTextChangedListener(textWatcher);
        passwordInput.addTextChangedListener(textWatcher);

        // -------------------------------------------------
        // WINDOW INSETS
        // -------------------------------------------------

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );
    }

    // -------------------------------------------------
    // CHECK WHETHER ALL FIELDS ARE FILLED
    // -------------------------------------------------

    private void checkFields() {

        String name =
                nameInput.getText().toString().trim();

        String email =
                emailInput.getText().toString().trim();

        String phone =
                phoneInput.getText().toString().trim();

        String dob =
                dobInput.getText().toString().trim();

        String password =
                passwordInput.getText().toString().trim();

        boolean allFilled =
                !name.isEmpty()
                        && !email.isEmpty()
                        && !phone.isEmpty()
                        && !dob.isEmpty()
                        && !password.isEmpty();

        createProfileButton.setEnabled(allFilled);
    }
}