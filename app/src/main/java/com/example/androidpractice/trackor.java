package com.example.androidpractice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class trackor extends AppCompatActivity {

    public static final String EXTRA_START_LOCATION = "extra_start_location";
    public static final String EXTRA_DROP_LOCATION = "extra_drop_location";
    public static final String EXTRA_TRAVEL_MODE = "extra_travel_mode";
    public static final String EXTRA_USE_LIVE_GPS = "extra_use_live_gps";

    public static final String EXTRA_START_LAT = "extra_start_lat";
    public static final String EXTRA_START_LNG = "extra_start_lng";
    public static final String EXTRA_DROP_LAT = "extra_drop_lat";
    public static final String EXTRA_DROP_LNG = "extra_drop_lng";
    public static final String EXTRA_HAS_EXACT_START_COORDS = "extra_has_exact_start_coords";
    public static final String EXTRA_HAS_EXACT_DROP_COORDS = "extra_has_exact_drop_coords";

    private EditText etStartLocation;
    private EditText etDropLocation;
    private TextView btnUseCurrentLocation;
    private ImageView btnSwapLocations;
    private ImageView btnBackTrackor;
    private ChipGroup chipGroupMode;
    private Button btnConfirmRoute;

    private RecyclerView rvStartSuggestions;
    private RecyclerView rvDropSuggestions;
    private LocationSuggestionAdapter startSuggestionAdapter;
    private LocationSuggestionAdapter dropSuggestionAdapter;

    private double selectedStartLat = 0.0;
    private double selectedStartLng = 0.0;
    private boolean hasSelectedStartCoords = false;

    private double selectedDropLat = 0.0;
    private double selectedDropLng = 0.0;
    private boolean hasSelectedDropCoords = false;

    private boolean isExplicitGpsRequested = false;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable startSearchRunnable;
    private Runnable dropSearchRunnable;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
                    fetchLiveGpsLocation();
                } else {
                    Toast.makeText(this, "Location permission denied. Type location manually.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trackor);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI Elements
        etStartLocation = findViewById(R.id.etStartLocation);
        etDropLocation = findViewById(R.id.etDropLocation);
        btnUseCurrentLocation = findViewById(R.id.btnUseCurrentLocation);
        btnSwapLocations = findViewById(R.id.btnSwapLocations);
        btnBackTrackor = findViewById(R.id.btnBackTrackor);
        chipGroupMode = findViewById(R.id.chipGroupMode);
        btnConfirmRoute = findViewById(R.id.btnConfirmRoute);

        rvStartSuggestions = findViewById(R.id.rvStartSuggestions);
        rvDropSuggestions = findViewById(R.id.rvDropSuggestions);

        setupSuggestionsRecyclerViews();

        // Setup Traveller Bottom Navigation
        View navHome = findViewById(R.id.navHome);
        View navAddBottom = findViewById(R.id.navAddBottom);
        View navProfile = findViewById(R.id.navProfile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(trackor.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navAddBottom != null) {
            navAddBottom.setOnClickListener(v -> {
                Intent intent = new Intent(trackor.this, AddHiddenSpotActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(trackor.this, TravellerProfileDisplayActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // Pre-fill prompt default
        etStartLocation.setHint("Enter starting location or tap GPS");

        // Watch for manual start location text entry & place suggestions
        etStartLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String currentText = s != null ? s.toString() : "";
                if (!currentText.contains("GPS") && !currentText.contains("Current Location")) {
                    isExplicitGpsRequested = false;
                }
                hasSelectedStartCoords = false;

                if (startSearchRunnable != null) {
                    searchHandler.removeCallbacks(startSearchRunnable);
                }

                if (currentText.trim().length() >= 2 && !currentText.contains("GPS")) {
                    startSearchRunnable = () -> performLocationSearch(currentText.trim(), true);
                    searchHandler.postDelayed(startSearchRunnable, 350);
                } else {
                    rvStartSuggestions.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Watch for manual drop location text entry & place suggestions
        etDropLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String currentText = s != null ? s.toString() : "";
                hasSelectedDropCoords = false;

                if (dropSearchRunnable != null) {
                    searchHandler.removeCallbacks(dropSearchRunnable);
                }

                if (currentText.trim().length() >= 2) {
                    dropSearchRunnable = () -> performLocationSearch(currentText.trim(), false);
                    searchHandler.postDelayed(dropSearchRunnable, 350);
                } else {
                    rvDropSuggestions.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Back action
        btnBackTrackor.setOnClickListener(v -> finish());

        // GPS button click
        btnUseCurrentLocation.setOnClickListener(v -> {
            isExplicitGpsRequested = true;
            checkAndFetchLocation();
        });

        // Swap locations button click
        btnSwapLocations.setOnClickListener(v -> {
            String startText = etStartLocation.getText().toString();
            String dropText = etDropLocation.getText().toString();

            etStartLocation.setText(dropText);
            etDropLocation.setText(startText);
            isExplicitGpsRequested = false;
            hasSelectedStartCoords = false;
            hasSelectedDropCoords = false;
        });

        // Preset Chips listeners
        setupPresetChips();

        // Confirm Route button listener
        btnConfirmRoute.setOnClickListener(v -> {
            String startLoc = etStartLocation.getText().toString().trim();
            String dropLoc = etDropLocation.getText().toString().trim();

            if (TextUtils.isEmpty(startLoc)) {
                etStartLocation.setError("Please enter starting location or tap GPS");
                etStartLocation.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(dropLoc)) {
                etDropLocation.setError("Please enter destination / dropping location");
                etDropLocation.requestFocus();
                return;
            }

            // Get selected travel mode
            String mode = "Driving";
            int selectedChipId = chipGroupMode.getCheckedChipId();
            if (selectedChipId == R.id.chipBike) {
                mode = "Bike / Scooter";
            } else if (selectedChipId == R.id.chipWalk) {
                mode = "Walking";
            }

            boolean useGps = isExplicitGpsRequested || startLoc.contains("GPS") || startLoc.contains("Current Location");

            // Launch MapRouteActivity with extras
            Intent intent = new Intent(trackor.this, MapRouteActivity.class);
            intent.putExtra(EXTRA_START_LOCATION, startLoc);
            intent.putExtra(EXTRA_DROP_LOCATION, dropLoc);
            intent.putExtra(EXTRA_TRAVEL_MODE, mode);
            intent.putExtra(EXTRA_USE_LIVE_GPS, useGps);

            intent.putExtra(EXTRA_START_LAT, selectedStartLat);
            intent.putExtra(EXTRA_START_LNG, selectedStartLng);
            intent.putExtra(EXTRA_DROP_LAT, selectedDropLat);
            intent.putExtra(EXTRA_DROP_LNG, selectedDropLng);
            intent.putExtra(EXTRA_HAS_EXACT_START_COORDS, hasSelectedStartCoords);
            intent.putExtra(EXTRA_HAS_EXACT_DROP_COORDS, hasSelectedDropCoords);

            startActivity(intent);
        });
    }

    private void setupSuggestionsRecyclerViews() {
        rvStartSuggestions.setLayoutManager(new LinearLayoutManager(this));
        startSuggestionAdapter = new LocationSuggestionAdapter(this, new ArrayList<>(), result -> {
            etStartLocation.setText(result.displayName);
            selectedStartLat = result.geoPoint.getLatitude();
            selectedStartLng = result.geoPoint.getLongitude();
            hasSelectedStartCoords = true;
            rvStartSuggestions.setVisibility(View.GONE);
        });
        rvStartSuggestions.setAdapter(startSuggestionAdapter);

        rvDropSuggestions.setLayoutManager(new LinearLayoutManager(this));
        dropSuggestionAdapter = new LocationSuggestionAdapter(this, new ArrayList<>(), result -> {
            etDropLocation.setText(result.displayName);
            selectedDropLat = result.geoPoint.getLatitude();
            selectedDropLng = result.geoPoint.getLongitude();
            hasSelectedDropCoords = true;
            rvDropSuggestions.setVisibility(View.GONE);
        });
        rvDropSuggestions.setAdapter(dropSuggestionAdapter);
    }

    private void performLocationSearch(String query, boolean isStartField) {
        LocationGeocoder.geocodeLocation(this, query, new LocationGeocoder.MultiGeocodeCallback() {
            @Override
            public void onSuccess(List<LocationGeocoder.GeocodeResult> results) {
                runOnUiThread(() -> {
                    if (isStartField) {
                        startSuggestionAdapter.updateSuggestions(results);
                        rvStartSuggestions.setVisibility(results.isEmpty() ? View.GONE : View.VISIBLE);
                    } else {
                        dropSuggestionAdapter.updateSuggestions(results);
                        rvDropSuggestions.setVisibility(results.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (isStartField) {
                        rvStartSuggestions.setVisibility(View.GONE);
                    } else {
                        rvDropSuggestions.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private void checkAndFetchLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLiveGpsLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void fetchLiveGpsLocation() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                Location gpsLoc = null;
                Location netLoc = null;

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }

                Location bestLocation = (gpsLoc != null) ? gpsLoc : netLoc;

                if (bestLocation != null) {
                    double lat = bestLocation.getLatitude();
                    double lng = bestLocation.getLongitude();

                    selectedStartLat = lat;
                    selectedStartLng = lng;
                    hasSelectedStartCoords = true;

                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            Address address = addresses.get(0);
                            String locName = address.getLocality();
                            if (locName == null || locName.isEmpty()) {
                                locName = address.getFeatureName();
                            }
                            if (locName == null || locName.isEmpty()) {
                                locName = String.format(Locale.getDefault(), "Live GPS (%.3f, %.3f)", lat, lng);
                            }
                            etStartLocation.setText(locName + " (GPS)");
                        } else {
                            etStartLocation.setText(String.format(Locale.getDefault(), "Live GPS (%.3f, %.3f)", lat, lng));
                        }
                    } catch (Exception e) {
                        etStartLocation.setText(String.format(Locale.getDefault(), "Live GPS (%.3f, %.3f)", lat, lng));
                    }
                    isExplicitGpsRequested = true;
                    Toast.makeText(this, "Acquired live GPS location", Toast.LENGTH_SHORT).show();
                } else {
                    etStartLocation.setText("Current Location (GPS)");
                    isExplicitGpsRequested = true;
                    Toast.makeText(this, "GPS active. Using Current Location", Toast.LENGTH_SHORT).show();
                }
            } else {
                etStartLocation.setText("Current Location (GPS)");
                isExplicitGpsRequested = true;
            }
        } catch (Exception e) {
            etStartLocation.setText("Current Location (GPS)");
            isExplicitGpsRequested = true;
        }
    }

    private void setupPresetChips() {
        Chip chipAirport = findViewById(R.id.chipAirport);
        Chip chipStation = findViewById(R.id.chipStation);
        Chip chipMall = findViewById(R.id.chipMall);
        Chip chipBeach = findViewById(R.id.chipBeach);
        Chip chipHillStation = findViewById(R.id.chipHillStation);

        if (chipAirport != null) {
            chipAirport.setOnClickListener(v -> etDropLocation.setText("City International Airport"));
        }
        if (chipStation != null) {
            chipStation.setOnClickListener(v -> etDropLocation.setText("Central Railway Station"));
        }
        if (chipMall != null) {
            chipMall.setOnClickListener(v -> etDropLocation.setText("Grand City Shopping Mall"));
        }
        if (chipBeach != null) {
            chipBeach.setOnClickListener(v -> etDropLocation.setText("Sunset Beach Point"));
        }
        if (chipHillStation != null) {
            chipHillStation.setOnClickListener(v -> etDropLocation.setText("Mountain Peak Lookout"));
        }
    }
}