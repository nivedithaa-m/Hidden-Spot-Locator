package com.example.androidpractice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
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

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

public class BusinessOwnerProfileActivity extends AppCompatActivity {

    private ImageView ownerProfileImage;
    private Button btnUploadOwnerPhoto;

    private EditText ownerNameInput;
    private EditText ownerEmailInput;
    private EditText ownerPhoneInput;
    private EditText ownerDobInput;
    private EditText ownerPasswordInput;

    private Button btnNextRestaurantDetails;
    private boolean photoSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_owner_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ownerProfileImage = findViewById(R.id.ownerProfileImage);
        btnUploadOwnerPhoto = findViewById(R.id.btnUploadOwnerPhoto);

        ownerNameInput = findViewById(R.id.ownerNameInput);
        ownerEmailInput = findViewById(R.id.ownerEmailInput);
        ownerPhoneInput = findViewById(R.id.ownerPhoneInput);
        ownerDobInput = findViewById(R.id.ownerDobInput);
        ownerPasswordInput = findViewById(R.id.ownerPasswordInput);

        btnNextRestaurantDetails = findViewById(R.id.btnNextRestaurantDetails);
        btnNextRestaurantDetails.setEnabled(false);

        // Photo Upload Options
        btnUploadOwnerPhoto.setOnClickListener(v -> {
            String[] options = {"Camera", "Gallery"};
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Choose Profile Photo")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                openCamera();
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                            }
                        } else {
                            String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                    ? Manifest.permission.READ_MEDIA_IMAGES
                                    : Manifest.permission.READ_EXTERNAL_STORAGE;

                            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                                openGallery();
                            } else {
                                galleryPermissionLauncher.launch(permission);
                            }
                        }
                    })
                    .show();
        });

        // Password Show/Hide Toggle
        ownerPasswordInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (ownerPasswordInput.getCompoundDrawables()[2] != null
                        && event.getX() >= ownerPasswordInput.getWidth()
                        - ownerPasswordInput.getCompoundDrawables()[2].getBounds().width()
                        - ownerPasswordInput.getPaddingEnd()) {

                    if (ownerPasswordInput.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        ownerPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        ownerPasswordInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility, 0);
                    } else {
                        ownerPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        ownerPasswordInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off, 0);
                    }
                    ownerPasswordInput.setSelection(ownerPasswordInput.length());
                    return true;
                }
            }
            return false;
        });

        // Email & Phone Validation
        ownerEmailInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String email = ownerEmailInput.getText().toString().trim();
                if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    ownerEmailInput.setError("Invalid email");
                }
            }
        });

        ownerPhoneInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String phone = ownerPhoneInput.getText().toString().trim();
                if (!phone.isEmpty() && !phone.matches("[6-9][0-9]{9}")) {
                    ownerPhoneInput.setError("Invalid phone number");
                }
            }
        });

        // Date of Birth Dialog
        ownerDobInput.setOnClickListener(v -> showDobPicker());

        // Field Watchers
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { checkFields(); }
            @Override public void afterTextChanged(Editable s) {}
        };

        ownerNameInput.addTextChangedListener(textWatcher);
        ownerEmailInput.addTextChangedListener(textWatcher);
        ownerPhoneInput.addTextChangedListener(textWatcher);
        ownerDobInput.addTextChangedListener(textWatcher);
        ownerPasswordInput.addTextChangedListener(textWatcher);

        // Next Button Click -> Go to Restaurant Details
        btnNextRestaurantDetails.setOnClickListener(v -> {
            String name = ownerNameInput.getText().toString().trim();
            Intent intent = new Intent(BusinessOwnerProfileActivity.this, BusinessProfileActivity.class);
            intent.putExtra("ownerName", name);

            if (ownerProfileImage.getTag() instanceof String) {
                intent.putExtra("profileImageUri", (String) ownerProfileImage.getTag());
            } else if (ownerProfileImage.getTag() instanceof byte[]) {
                intent.putExtra("profileImageBytes", (byte[]) ownerProfileImage.getTag());
            }

            startActivity(intent);
        });
    }

    private void showDobPicker() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        String[] years = new String[currentYear - 1900 + 1];
        for (int i = 0; i < years.length; i++) {
            years[i] = String.valueOf(currentYear - i);
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("Select Year")
                .setItems(years, (dialog, which) -> {
                    int selectedYear = Integer.parseInt(years[which]);
                    String[] months = {"January", "February", "March", "April", "May", "June",
                            "July", "August", "September", "October", "November", "December"};

                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Select Month")
                            .setItems(months, (mDialog, monthWhich) -> {
                                Calendar selDate = Calendar.getInstance();
                                selDate.set(selectedYear, monthWhich, 1);
                                int daysInMonth = selDate.getActualMaximum(Calendar.DAY_OF_MONTH);
                                String[] days = new String[daysInMonth];
                                for (int d = 0; d < daysInMonth; d++) {
                                    days[d] = String.valueOf(d + 1);
                                }

                                new android.app.AlertDialog.Builder(this)
                                        .setTitle("Select Day")
                                        .setItems(days, (dDialog, dayWhich) -> {
                                            selDate.set(selectedYear, monthWhich, dayWhich + 1);
                                            if (selDate.after(Calendar.getInstance())) {
                                                ownerDobInput.setError("Invalid date of birth");
                                                return;
                                            }
                                            String dateStr = String.format(java.util.Locale.US, "%02d/%02d/%04d", dayWhich + 1, monthWhich + 1, selectedYear);
                                            ownerDobInput.setText(dateStr);
                                        })
                                        .show();
                            })
                            .show();
                })
                .show();
    }

    private void checkFields() {
        boolean allFilled = !ownerNameInput.getText().toString().trim().isEmpty()
                && !ownerEmailInput.getText().toString().trim().isEmpty()
                && !ownerPhoneInput.getText().toString().trim().isEmpty()
                && !ownerDobInput.getText().toString().trim().isEmpty()
                && !ownerPasswordInput.getText().toString().trim().isEmpty()
                && photoSelected;

        btnNextRestaurantDetails.setEnabled(allFilled);
    }

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        ownerProfileImage.setImageURI(imageUri);
                        ownerProfileImage.setTag(imageUri.toString());
                        photoSelected = true;
                        checkFields();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getExtras() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    if (photo != null) {
                        ownerProfileImage.setImageBitmap(photo);
                        photoSelected = true;
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        photo.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                        ownerProfileImage.setTag(stream.toByteArray());
                        checkFields();
                    }
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openCamera();
            });

    private final ActivityResultLauncher<String> galleryPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openGallery();
            });

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        galleryLauncher.launch(intent);
    }
}