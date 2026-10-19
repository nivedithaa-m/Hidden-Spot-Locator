package com.example.androidpractice;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UpdateRestaurantActivity extends AppCompatActivity {

    private static final String TAG = "UpdateRestaurantActivity";

    private RadioGroup rgRequestType;
    private EditText etUpdateDetails;
    private Button btnSubmitUpdateRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_restaurant);

        rgRequestType = findViewById(R.id.rgRequestType);
        etUpdateDetails = findViewById(R.id.etUpdateDetails);
        btnSubmitUpdateRequest = findViewById(R.id.btnSubmitUpdateRequest);

        btnSubmitUpdateRequest.setOnClickListener(v -> submitUpdateRequest());

        // Setup Business Bottom Navigation
        View bizNavHome = findViewById(R.id.bizNavHome);
        View bizNavAdd = findViewById(R.id.bizNavAdd);
        View bizNavProfile = findViewById(R.id.bizNavProfile);

        if (bizNavHome != null) {
            bizNavHome.setOnClickListener(v -> {
                Intent intent = new Intent(UpdateRestaurantActivity.this, BusinessHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (bizNavAdd != null) {
            bizNavAdd.setOnClickListener(v -> {
                Intent intent = new Intent(UpdateRestaurantActivity.this, AddRestaurantActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (bizNavProfile != null) {
            bizNavProfile.setOnClickListener(v -> {
                Intent intent = new Intent(UpdateRestaurantActivity.this, BusinessProfileActivity.class);
                intent.putExtra("is_edit_mode", true);
                startActivity(intent);
                finish();
            });
        }
    }

    private void submitUpdateRequest() {
        String details = etUpdateDetails.getText().toString().trim();
        if (details.isEmpty()) {
            etUpdateDetails.setError("Please describe the requested changes or reason");
            etUpdateDetails.requestFocus();
            return;
        }

        String requestType = "update_details";
        int checkedId = rgRequestType.getCheckedRadioButtonId();
        if (checkedId == R.id.rbRelocation) {
            requestType = "relocation";
        } else if (checkedId == R.id.rbClosure) {
            requestType = "closure";
        }

        String ownerUid = FirebaseAuthManager.getCurrentUserId();
        if (ownerUid.isEmpty()) {
            ownerUid = "guest_owner";
        }

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("ownerUid", ownerUid);
        requestData.put("requestType", requestType);
        requestData.put("details", details);
        requestData.put("status", "pending");
        requestData.put("createdAt", System.currentTimeMillis());

        if (FirebaseAuthManager.isUserLoggedIn()) {
            try {
                FirebaseFirestore.getInstance()
                        .collection("update_requests")
                        .add(requestData)
                        .addOnSuccessListener(doc -> Log.d(TAG, "Update request submitted with ID: " + doc.getId()))
                        .addOnFailureListener(e -> Log.e(TAG, "Update request error: " + e.getMessage()));
            } catch (Exception e) {
                Log.e(TAG, "Firestore exception: " + e.getMessage());
            }
        }

        Toast.makeText(this, "Update request submitted for admin review!", Toast.LENGTH_LONG).show();
        finish();
    }
}