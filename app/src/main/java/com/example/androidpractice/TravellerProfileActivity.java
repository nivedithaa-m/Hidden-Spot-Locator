package com.example.androidpractice;

import java.io.ByteArrayOutputStream;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class TravellerProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private Button uploadPhotoButton;

    private boolean photoSelected = false;

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
        profileImage = findViewById(R.id.profileImage);
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton);

        uploadPhotoButton.setOnClickListener(v -> {

            String[] options = {"Camera", "Gallery"};

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Choose Profile Photo")
                    .setItems(options, (dialog, which) -> {

                        if (which == 0) {

                            // Camera selected
                            if (ContextCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED) {

                                openCamera();

                            } else {

                                cameraPermissionLauncher.launch(
                                        Manifest.permission.CAMERA
                                );
                            }

                        } else {

                            // Gallery selected
                            if (ContextCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.READ_MEDIA_IMAGES
                            ) == PackageManager.PERMISSION_GRANTED) {

                                openGallery();

                            } else {

                                galleryPermissionLauncher.launch(
                                        Manifest.permission.READ_MEDIA_IMAGES
                                );
                            }
                        }
                    })
                    .show();
        });

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
                            == (InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {

                        // Show password
                        passwordInput.setInputType(
                                InputType.TYPE_CLASS_TEXT
                                        | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        );

                        // Change to open eye
                        passwordInput.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_visibility,
                                0
                        );

                    } else {

                        // Hide password
                        passwordInput.setInputType(
                                InputType.TYPE_CLASS_TEXT
                                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
                        );

                        // Change to closed eye
                        passwordInput.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_visibility_off,
                                0
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

                String email =
                        emailInput.getText().toString().trim();

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

                String phone =
                        phoneInput.getText().toString().trim();

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

            int currentYear =
                    calendar.get(Calendar.YEAR);

            // Step 1: Select Year
            final int[] selectedYear = new int[1];

            String[] years =
                    new String[currentYear - 1900 + 1];

            for (int i = 0; i < years.length; i++) {
                years[i] =
                        String.valueOf(currentYear - i);
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

                                    int selectedMonth =
                                            monthWhich;

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

        createProfileButton.setOnClickListener(v -> {

            String username =
                    nameInput.getText().toString().trim();

            String password =
                    passwordInput.getText().toString().trim();

            // Save username and password
            getSharedPreferences(
                    "UserData",
                    MODE_PRIVATE
            )
                    .edit()
                    .putString("username", username)
                    .putString("password", password)
                    .apply();

            Intent intent = new Intent(
                    TravellerProfileActivity.this,
                    HomeActivity.class
            );

            intent.putExtra("username", username);
            intent.putExtra("password", password);

            if (profileImage.getTag() instanceof String) {

                intent.putExtra(
                        "profileImageUri",
                        (String) profileImage.getTag()
                );

            } else if (profileImage.getTag() instanceof byte[]) {

                intent.putExtra(
                        "profileImageBytes",
                        (byte[]) profileImage.getTag()
                );
            }

            startActivity(intent);
            finish();
        });

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
                        && !password.isEmpty()
                        && photoSelected;

        createProfileButton.setEnabled(allFilled);
    }

    // -------------------------------------------------
    // GALLERY
    // -------------------------------------------------

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {

                            Uri imageUri =
                                    result.getData().getData();

                            if (imageUri != null) {

                                profileImage.setImageURI(imageUri);
                                profileImage.setTag(imageUri.toString());

                                photoSelected = true;
                                checkFields();
                            }
                        }
                    }
            );

    // -------------------------------------------------
    // CAMERA
    // -------------------------------------------------

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {

                            Bitmap photo =
                                    (Bitmap) result.getData()
                                            .getExtras()
                                            .get("data");

                            if (photo != null) {

                                profileImage.setImageBitmap(photo);
                                photoSelected = true;

                                ByteArrayOutputStream stream =
                                        new ByteArrayOutputStream();

                                photo.compress(
                                        Bitmap.CompressFormat.JPEG,
                                        80,
                                        stream
                                );

                                byte[] photoBytes =
                                        stream.toByteArray();

                                profileImage.setTag(photoBytes);

                                checkFields();
                            }
                        }
                    }
            );

    // -------------------------------------------------
    // CAMERA PERMISSION
    // -------------------------------------------------

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {

                        if (isGranted) {
                            openCamera();
                        }
                    }
            );

    // -------------------------------------------------
    // GALLERY PERMISSION
    // -------------------------------------------------

    private final ActivityResultLauncher<String> galleryPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {

                        if (isGranted) {
                            openGallery();
                        }
                    }
            );

    // -------------------------------------------------
    // OPEN CAMERA
    // -------------------------------------------------

    private void openCamera() {

        Intent intent =
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        cameraLauncher.launch(intent);
    }

    // -------------------------------------------------
    // OPEN GALLERY
    // -------------------------------------------------

    private void openGallery() {

        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );

        intent.setType("image/*");

        galleryLauncher.launch(intent);
    }
}