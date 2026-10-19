package com.example.androidpractice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapRouteActivity extends AppCompatActivity {

    private static final String TAG = "HiddenSpotLocator";

    private MapView osmMapView;
    private TextView tvMapRouteTitle;
    private TextView tvMapRouteSubtitle;
    private TextView tvTravelModeBadge;
    private TextView tvTotalDistance;
    private TextView tvEstTime;

    private Button btnStartJourney;
    private Button btnEndJourney;
    private CardView cardOffRouteBanner;
    private TextView tvOffRouteText;

    private ExtendedFloatingActionButton btnToggleRadar;
    private FloatingActionButton btnRecenterMap;
    private CardView cardRadarControls;

    private TextView tvRadarRadiusValue;
    private Slider sliderRadarDistance;
    private ChipGroup chipGroupRadius;
    private TextView tvRadarStatusText;

    private TextView tvPlaceCountBadge;
    private ChipGroup chipGroupFilter;
    private RecyclerView rvNearbyPlaces;
    private NearbyPlaceAdapter adapter;

    // Route & Position
    private GeoPoint startPoint;
    private GeoPoint destinationPoint;
    private GeoPoint currentTravelerPoint;
    private long activeRouteRequestId = 0;

    // Active Navigation Journey State
    private boolean isJourneyActive = false;
    private LocationManager locationManager;
    private LocationListener journeyLocationListener;

    // Overlays
    private Polygon radarCircleOverlay;
    private Polygon radarInnerCircleOverlay;
    private Marker startMarker;
    private Marker destMarker;
    private Marker travelerMarker;
    private final List<Marker> placeMarkers = new ArrayList<>();
    private Polyline routePolyline;
    private List<GeoPoint> activeRoutePoints = new ArrayList<>();

    // Radar State
    private boolean isRadarEnabled = true;
    private double currentRadarRadiusKm = 3.5;
    private String selectedCategoryFilter = "All";

    // All sample & verified places database
    private final List<NearbyPlace> allSamplePlaces = new ArrayList<>();
    private final List<NearbyPlace> activePlacesList = new ArrayList<>();

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
                    if (isJourneyActive) {
                        startJourneyNavigation();
                    } else {
                        moveToLiveGpsPosition();
                    }
                } else {
                    showLocationPermissionRequiredDialog();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Initialize MapTileConfig (Fixes OpenStreetMap tile configuration)
        MapTileConfig.initializeMapConfiguration(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map_route);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mapRootLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        initViews();

        // Configure MapView
        MapTileConfig.configureMapView(osmMapView);

        // Get Intent Extras
        String startLocName = getIntent().getStringExtra(trackor.EXTRA_START_LOCATION);
        String dropLocName = getIntent().getStringExtra(trackor.EXTRA_DROP_LOCATION);
        String mode = getIntent().getStringExtra(trackor.EXTRA_TRAVEL_MODE);
        boolean isUseLiveGps = getIntent().getBooleanExtra(trackor.EXTRA_USE_LIVE_GPS, false);

        if (startLocName == null || startLocName.isEmpty()) startLocName = "Karicode";
        if (dropLocName == null || dropLocName.isEmpty()) dropLocName = "Kollam";
        if (mode == null) mode = "Driving";

        Log.d(TAG, "--------------------------------------------------");
        Log.d(TAG, "MapRouteActivity onCreate - Intent Inputs: StartLocation='" + startLocName + "', DestinationLocation='" + dropLocName + "', TravelMode='" + mode + "', isUseLiveGps=" + isUseLiveGps);

        tvMapRouteTitle.setText(startLocName + " ➔ " + dropLocName);
        tvMapRouteSubtitle.setText("Initializing Route Geocoding...");
        tvTravelModeBadge.setText(mode);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup Radar Controls
        setupRadarControls();

        // Setup Initial Location & Geocoding Search
        setupRouteAndGeocoding(startLocName, dropLocName, isUseLiveGps);

        // Save search to user search history if logged in
        saveSearchToUserHistory(startLocName, dropLocName);
    }

    private void saveSearchToUserHistory(String startLoc, String dropLoc) {
        if (FirebaseAuthManager.isUserLoggedIn()) {
            String uid = FirebaseAuthManager.getCurrentUserId();
            if (uid != null && !uid.isEmpty()) {
                try {
                    java.util.Map<String, Object> historyData = new java.util.HashMap<>();
                    historyData.put("origin", startLoc);
                    historyData.put("destination", dropLoc);
                    historyData.put("timestamp", System.currentTimeMillis());

                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .collection("search_history")
                            .add(historyData);
                } catch (Exception e) {
                    Log.e(TAG, "Search history save error: " + e.getMessage());
                }
            }
        }
    }

    private void initViews() {
        osmMapView = findViewById(R.id.osmMapView);
        tvMapRouteTitle = findViewById(R.id.tvMapRouteTitle);
        tvMapRouteSubtitle = findViewById(R.id.tvMapRouteSubtitle);
        tvTravelModeBadge = findViewById(R.id.tvTravelModeBadge);
        tvTotalDistance = findViewById(R.id.tvTotalDistance);
        tvEstTime = findViewById(R.id.tvEstTime);
        ImageView btnMapBack = findViewById(R.id.btnMapBack);

        btnStartJourney = findViewById(R.id.btnStartJourney);
        btnEndJourney = findViewById(R.id.btnEndJourney);
        cardOffRouteBanner = findViewById(R.id.cardOffRouteBanner);
        tvOffRouteText = findViewById(R.id.tvOffRouteText);
        Button btnRecalculateRoute = findViewById(R.id.btnRecalculateRoute);

        btnToggleRadar = findViewById(R.id.btnToggleRadar);
        btnRecenterMap = findViewById(R.id.btnRecenterMap);
        cardRadarControls = findViewById(R.id.cardRadarControls);

        tvRadarRadiusValue = findViewById(R.id.tvRadarRadiusValue);
        sliderRadarDistance = findViewById(R.id.sliderRadarDistance);
        chipGroupRadius = findViewById(R.id.chipGroupRadius);
        tvRadarStatusText = findViewById(R.id.tvRadarStatusText);

        tvPlaceCountBadge = findViewById(R.id.tvPlaceCountBadge);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        rvNearbyPlaces = findViewById(R.id.rvNearbyPlaces);

        btnMapBack.setOnClickListener(v -> finish());

        // Journey Controls
        btnStartJourney.setOnClickListener(v -> checkAndStartJourney());
        btnEndJourney.setOnClickListener(v -> stopJourneyNavigation());
        if (btnRecalculateRoute != null) {
            btnRecalculateRoute.setOnClickListener(v -> recalculateRouteFromCurrentPosition());
        }
    }

    private boolean isGpsKeyword(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        return lower.contains("current location") || lower.contains("live gps") || lower.equals("gps");
    }

    private void setupRouteAndGeocoding(String startLocName, String dropLocName, boolean isUseLiveGps) {
        final long requestId = ++activeRouteRequestId;
        Log.d(TAG, "Route Calculation Sequence Token #" + requestId + " started.");

        boolean hasStartCoords = getIntent().getBooleanExtra(trackor.EXTRA_HAS_EXACT_START_COORDS, false);
        boolean hasDropCoords = getIntent().getBooleanExtra(trackor.EXTRA_HAS_EXACT_DROP_COORDS, false);

        double startLat = getIntent().getDoubleExtra(trackor.EXTRA_START_LAT, 0.0);
        double startLng = getIntent().getDoubleExtra(trackor.EXTRA_START_LNG, 0.0);
        double dropLat = getIntent().getDoubleExtra(trackor.EXTRA_DROP_LAT, 0.0);
        double dropLng = getIntent().getDoubleExtra(trackor.EXTRA_DROP_LNG, 0.0);

        if (hasStartCoords && startLat != 0.0) {
            startPoint = new GeoPoint(startLat, startLng);
            currentTravelerPoint = startPoint;
            Log.d(TAG, String.format(Locale.US, "Using exact intent start coordinates: Lat=%.6f, Lng=%.6f", startLat, startLng));
        }

        if (hasDropCoords && dropLat != 0.0) {
            destinationPoint = new GeoPoint(dropLat, dropLng);
            Log.d(TAG, String.format(Locale.US, "Using exact intent destination coordinates: Lat=%.6f, Lng=%.6f", dropLat, dropLng));
        }

        if (startPoint != null && destinationPoint != null) {
            tvMapRouteSubtitle.setText("Route: " + startLocName + " ➔ " + dropLocName);
            calculateAndDrawRoadRoute(requestId);
            return;
        }

        boolean isStartGps = isUseLiveGps || isGpsKeyword(startLocName);

        if (isStartGps) {
            Location bestGps = getDeviceGpsLocation();
            if (bestGps != null) {
                startPoint = new GeoPoint(bestGps.getLatitude(), bestGps.getLongitude());
                currentTravelerPoint = startPoint;
                Log.d(TAG, String.format(Locale.US, "Start Location set to Live GPS: Lat=%.6f, Lng=%.6f", startPoint.getLatitude(), startPoint.getLongitude()));
                geocodeDestinationAndCalculateRoute(dropLocName, requestId);
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("GPS Location Unavailable")
                        .setMessage("Live GPS location is currently unavailable. Please enable GPS services or type a starting location manually.")
                        .setPositiveButton("Retry GPS", (dialog, which) -> checkAndMoveToLiveGps())
                        .setNegativeButton("Cancel", (dialog, which) -> finish())
                        .show();
            }
        } else {
            // Geocode manually entered starting location
            tvMapRouteSubtitle.setText("Finding starting point: " + startLocName + "...");
            LocationGeocoder.geocodeLocation(this, startLocName, new LocationGeocoder.MultiGeocodeCallback() {
                @Override
                public void onSuccess(List<LocationGeocoder.GeocodeResult> results) {
                    if (requestId != activeRouteRequestId) {
                        Log.d(TAG, "Ignoring stale start geocode callback #" + requestId);
                        return;
                    }

                    if (results.size() > 1) {
                        runOnUiThread(() -> showAmbiguousLocationDialog("Select Starting Location", results, selectedResult -> {
                            startPoint = selectedResult.geoPoint;
                            currentTravelerPoint = startPoint;
                            Log.d(TAG, String.format(Locale.US, "Selected Start Point '%s': Lat=%.6f, Lng=%.6f", selectedResult.displayName, startPoint.getLatitude(), startPoint.getLongitude()));
                            geocodeDestinationAndCalculateRoute(dropLocName, requestId);
                        }));
                    } else {
                        LocationGeocoder.GeocodeResult top = results.get(0);
                        startPoint = top.geoPoint;
                        currentTravelerPoint = startPoint;
                        Log.d(TAG, String.format(Locale.US, "Geocoded Start Point '%s': Lat=%.6f, Lng=%.6f", top.displayName, startPoint.getLatitude(), startPoint.getLongitude()));
                        runOnUiThread(() -> geocodeDestinationAndCalculateRoute(dropLocName, requestId));
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    if (requestId != activeRouteRequestId) return;
                    runOnUiThread(() -> new AlertDialog.Builder(MapRouteActivity.this)
                            .setTitle("Starting Location Not Found")
                            .setMessage("Unable to find coordinates for starting location '" + startLocName + "'.\n" + errorMessage)
                            .setPositiveButton("Edit Location", (dialog, which) -> finish())
                            .setNegativeButton("Cancel", null)
                            .show());
                }
            });
        }
    }

    private void geocodeDestinationAndCalculateRoute(String dropLocName, final long requestId) {
        if (requestId != activeRouteRequestId) return;
        tvMapRouteSubtitle.setText("Finding destination: " + dropLocName + "...");

        LocationGeocoder.geocodeLocation(this, dropLocName, new LocationGeocoder.MultiGeocodeCallback() {
            @Override
            public void onSuccess(List<LocationGeocoder.GeocodeResult> results) {
                if (requestId != activeRouteRequestId) {
                    Log.d(TAG, "Ignoring stale destination geocode callback #" + requestId);
                    return;
                }

                if (results.size() > 1) {
                    runOnUiThread(() -> showAmbiguousLocationDialog("Select Destination", results, selectedResult -> {
                        destinationPoint = selectedResult.geoPoint;
                        Log.d(TAG, String.format(Locale.US, "Selected Destination Point '%s': Lat=%.6f, Lng=%.6f", selectedResult.displayName, destinationPoint.getLatitude(), destinationPoint.getLongitude()));
                        tvMapRouteSubtitle.setText("Route: " + selectedResult.displayName);
                        calculateAndDrawRoadRoute(requestId);
                    }));
                } else {
                    LocationGeocoder.GeocodeResult top = results.get(0);
                    destinationPoint = top.geoPoint;
                    Log.d(TAG, String.format(Locale.US, "Geocoded Destination Point '%s': Lat=%.6f, Lng=%.6f", top.displayName, destinationPoint.getLatitude(), destinationPoint.getLongitude()));
                    runOnUiThread(() -> {
                        tvMapRouteSubtitle.setText("Route: " + top.displayName);
                        calculateAndDrawRoadRoute(requestId);
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (requestId != activeRouteRequestId) return;
                runOnUiThread(() -> new AlertDialog.Builder(MapRouteActivity.this)
                        .setTitle("Destination Not Found")
                        .setMessage("Unable to find coordinates for destination '" + dropLocName + "'.\n" + errorMessage)
                        .setPositiveButton("Edit Destination", (dialog, which) -> finish())
                        .setNegativeButton("Cancel", null)
                        .show());
            }
        });
    }

    private interface OnLocationSelectedListener {
        void onLocationSelected(LocationGeocoder.GeocodeResult result);
    }

    private void showAmbiguousLocationDialog(String title, List<LocationGeocoder.GeocodeResult> candidates, OnLocationSelectedListener listener) {
        String[] items = new String[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            items[i] = candidates.get(i).toString();
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(items, (dialog, which) -> {
                    if (listener != null) {
                        listener.onLocationSelected(candidates.get(which));
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void calculateAndDrawRoadRoute(final long requestId) {
        if (requestId != activeRouteRequestId) return;
        if (startPoint == null || destinationPoint == null) return;

        Log.d(TAG, String.format(Locale.US, "Executing fetchRoadRoute #" + requestId + " from Start(Lat: %.6f, Lng: %.6f) to Dest(Lat: %.6f, Lng: %.6f)",
                startPoint.getLatitude(), startPoint.getLongitude(), destinationPoint.getLatitude(), destinationPoint.getLongitude()));

        RoadRoutingService.fetchRoadRoute(startPoint, destinationPoint, new RoadRoutingService.RouteCallback() {
            @Override
            public void onRouteCalculated(RoadRoutingService.RouteResult result) {
                if (requestId != activeRouteRequestId) {
                    Log.d(TAG, "Ignoring stale OSRM route result #" + requestId);
                    return;
                }

                activeRoutePoints = result.routePolylinePoints;

                // Update UI Stats with true OSRM road distance
                tvTotalDistance.setText(result.distanceFormatted);
                tvEstTime.setText(result.durationFormatted);

                Log.d(TAG, String.format(Locale.US, "Displayed Route Stats for Request #%d: RoadDistance=%s, Duration=%s", requestId, result.distanceFormatted, result.durationFormatted));

                // Draw map route polyline and markers
                renderRouteOnMap();

                // Load verified Kollam places & filter by actual route corridor proximity
                loadVerifiedPlacesAlongRoute();

                // Update Radar Circle & Markers
                updateRadarAndMarkers();
            }

            @Override
            public void onError(String errorMessage) {
                if (requestId != activeRouteRequestId) return;
                Toast.makeText(MapRouteActivity.this, "Route Notice: " + errorMessage, Toast.LENGTH_SHORT).show();

                activeRoutePoints.clear();
                activeRoutePoints.add(startPoint);
                activeRoutePoints.add(destinationPoint);

                renderRouteOnMap();
                loadVerifiedPlacesAlongRoute();
                updateRadarAndMarkers();
            }
        });
    }

    private void renderRouteOnMap() {
        osmMapView.getOverlays().clear();

        // Add Start Marker
        startMarker = new Marker(osmMapView);
        startMarker.setPosition(startPoint);
        startMarker.setTitle("Starting Location");
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        osmMapView.getOverlays().add(startMarker);

        // Add Destination Marker
        destMarker = new Marker(osmMapView);
        destMarker.setPosition(destinationPoint);
        destMarker.setTitle("Destination Point");
        destMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        osmMapView.getOverlays().add(destMarker);

        // Add Traveler Position Marker
        travelerMarker = new Marker(osmMapView);
        travelerMarker.setPosition(currentTravelerPoint);
        travelerMarker.setTitle("Traveler Location");
        travelerMarker.setSnippet("Current Position");
        travelerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        osmMapView.getOverlays().add(travelerMarker);

        // Draw Route Polyline
        routePolyline = new Polyline();
        routePolyline.getOutlinePaint().setColor(Color.parseColor("#2563EB"));
        routePolyline.getOutlinePaint().setStrokeWidth(12f);
        routePolyline.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);
        routePolyline.setPoints(activeRoutePoints);

        osmMapView.getOverlays().add(routePolyline);

        // Fit map camera bounding box around route
        recenterMapOnRoute();
    }

    private void recenterMapOnRoute() {
        if (activeRoutePoints == null || activeRoutePoints.isEmpty()) return;
        BoundingBox boundingBox = BoundingBox.fromGeoPoints(activeRoutePoints);
        osmMapView.post(() -> osmMapView.zoomToBoundingBox(boundingBox, true, 100));
    }

    // -----------------------------------------------------------------
    // ACTIVE JOURNEY NAVIGATION EXPERIENCE
    // -----------------------------------------------------------------

    private void checkAndStartJourney() {
        if (!FirebaseAuthManager.isUserLoggedIn()) {
            new AlertDialog.Builder(this)
                    .setTitle("Sign In Required")
                    .setMessage("You need to sign in to start live GPS journey navigation and tracking.")
                    .setPositiveButton("Sign In", (dialog, which) -> {
                        Intent intent = new Intent(MapRouteActivity.this, LoginActivity.class);
                        intent.putExtra("isReturnToMap", true);
                        intent.putExtra(trackor.EXTRA_START_LOCATION, getIntent().getStringExtra(trackor.EXTRA_START_LOCATION));
                        intent.putExtra(trackor.EXTRA_DROP_LOCATION, getIntent().getStringExtra(trackor.EXTRA_DROP_LOCATION));
                        intent.putExtra(trackor.EXTRA_TRAVEL_MODE, getIntent().getStringExtra(trackor.EXTRA_TRAVEL_MODE));
                        intent.putExtra(trackor.EXTRA_USE_LIVE_GPS, getIntent().getBooleanExtra(trackor.EXTRA_USE_LIVE_GPS, false));
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startJourneyNavigation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void startJourneyNavigation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null || (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            showLocationDisabledDialog();
            return;
        }

        Location liveGps = getDeviceGpsLocation();
        if (liveGps == null) {
            showLocationDisabledDialog();
            return;
        }

        // Switch origin to actual current GPS location for journey navigation
        startPoint = new GeoPoint(liveGps.getLatitude(), liveGps.getLongitude());
        currentTravelerPoint = startPoint;
        Log.d(TAG, String.format(Locale.US, "Active Journey started from real GPS: Lat=%.6f, Lng=%.6f", startPoint.getLatitude(), startPoint.getLongitude()));

        isJourneyActive = true;
        btnStartJourney.setVisibility(View.GONE);
        btnEndJourney.setVisibility(View.VISIBLE);

        // Recalculate road route from live GPS position to destination
        calculateAndDrawRoadRoute(++activeRouteRequestId);

        journeyLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (isJourneyActive) {
                    onLiveJourneyLocationUpdated(location);
                }
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                if (isJourneyActive) {
                    Toast.makeText(MapRouteActivity.this, "GPS signal unavailable", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {}
        };

        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5, journeyLocationListener);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, journeyLocationListener);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Location update error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(this, "Active Journey Navigation Started 🚀", Toast.LENGTH_SHORT).show();
    }

    private void onLiveJourneyLocationUpdated(Location location) {
        if (location == null || destinationPoint == null) return;

        currentTravelerPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        if (travelerMarker != null) {
            travelerMarker.setPosition(currentTravelerPoint);
        }

        // Animate camera to follow live traveler
        osmMapView.getController().animateTo(currentTravelerPoint);
        osmMapView.getController().setZoom(16.0);

        // Update Remaining Road Distance & Travel Time
        double remainingMeters = currentTravelerPoint.distanceToAsDouble(destinationPoint);
        double remainingKm = remainingMeters / 1000.0;
        int remainingMins = (int) Math.round((remainingKm / 40.0) * 60.0);

        tvTotalDistance.setText(String.format(Locale.getDefault(), "%.1f km left", remainingKm));
        tvEstTime.setText(remainingMins > 0 ? remainingMins + " mins left" : "< 1 min");

        // Route Deviation Check (Threshold = 180 meters)
        if (activeRoutePoints != null && !activeRoutePoints.isEmpty()) {
            double offRouteMeters = KollamPlacesRepository.computeMinDistanceToRouteKm(currentTravelerPoint, activeRoutePoints) * 1000.0;
            if (offRouteMeters > 180.0) {
                tvOffRouteText.setText(String.format(Locale.getDefault(), "⚠️ Off route detected (%.0fm away)", offRouteMeters));
                cardOffRouteBanner.setVisibility(View.VISIBLE);
            } else {
                cardOffRouteBanner.setVisibility(View.GONE);
            }
        }

        // Update Radar Circle & Nearby Places around live position
        updateRadarAndMarkers();
    }

    private void recalculateRouteFromCurrentPosition() {
        cardOffRouteBanner.setVisibility(View.GONE);
        if (currentTravelerPoint == null) return;

        startPoint = currentTravelerPoint;
        Toast.makeText(this, "Recalculating road route from current GPS position...", Toast.LENGTH_SHORT).show();
        calculateAndDrawRoadRoute(++activeRouteRequestId);
    }

    private void stopJourneyNavigation() {
        isJourneyActive = false;

        if (locationManager != null && journeyLocationListener != null) {
            try {
                locationManager.removeUpdates(journeyLocationListener);
            } catch (Exception ignored) {}
        }

        btnEndJourney.setVisibility(View.GONE);
        cardOffRouteBanner.setVisibility(View.GONE);
        btnStartJourney.setVisibility(View.VISIBLE);

        recenterMapOnRoute();
        Toast.makeText(this, "Journey navigation ended.", Toast.LENGTH_SHORT).show();
    }

    // -----------------------------------------------------------------
    // LOCATION HELPERS
    // -----------------------------------------------------------------

    private Location getDeviceGpsLocation() {
        try {
            LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locManager != null) {
                Location gpsLoc = null;
                Location netLoc = null;

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    gpsLoc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    netLoc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                return (gpsLoc != null) ? gpsLoc : netLoc;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void checkAndMoveToLiveGps() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            moveToLiveGpsPosition();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void moveToLiveGpsPosition() {
        Location bestLocation = getDeviceGpsLocation();
        if (bestLocation != null) {
            currentTravelerPoint = new GeoPoint(bestLocation.getLatitude(), bestLocation.getLongitude());
            if (travelerMarker != null) {
                travelerMarker.setPosition(currentTravelerPoint);
            }

            // Animate camera to live GPS
            osmMapView.getController().animateTo(currentTravelerPoint);
            osmMapView.getController().setZoom(15.0);

            // Re-calculate route from new GPS position to destination
            startPoint = currentTravelerPoint;
            calculateAndDrawRoadRoute(++activeRouteRequestId);

            Toast.makeText(this, String.format(Locale.getDefault(), "Acquired GPS: %.4f, %.4f", bestLocation.getLatitude(), bestLocation.getLongitude()), Toast.LENGTH_SHORT).show();
        } else {
            showLocationDisabledDialog();
        }
    }

    private void showLocationPermissionRequiredDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Location Permission Required")
                .setMessage("Hidden Spot Locator needs location access to show your actual GPS position and calculate road routes.")
                .setPositiveButton("Grant Permission", (dialog, which) -> checkAndMoveToLiveGps())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLocationDisabledDialog() {
        new AlertDialog.Builder(this)
                .setTitle("GPS Location Unavailable")
                .setMessage("Unable to acquire live GPS location. Please ensure location services are enabled on your device.")
                .setPositiveButton("Retry", (dialog, which) -> checkAndMoveToLiveGps())
                .setNegativeButton("OK", null)
                .show();
    }

    private void loadVerifiedPlacesAlongRoute() {
        allSamplePlaces.clear();

        // 1. Genuine Kollam Hidden Spots
        List<KollamPlacesRepository.PlaceItem> hiddenSpots = KollamPlacesRepository.getVerifiedKollamHiddenSpots();
        for (KollamPlacesRepository.PlaceItem spot : hiddenSpots) {
            double routeDistance = KollamPlacesRepository.computeMinDistanceToRouteKm(spot.geoPoint, activeRoutePoints);
            NearbyPlace place = new NearbyPlace(spot.name, "Hidden Spot", spot.rating, routeDistance,
                    spot.geoPoint, spot.address, R.drawable.ic_place);
            allSamplePlaces.add(place);
        }

        // 2. Real Kollam Restaurants
        List<KollamPlacesRepository.PlaceItem> restaurants = KollamPlacesRepository.getVerifiedKollamRestaurants();
        for (KollamPlacesRepository.PlaceItem rest : restaurants) {
            double routeDistance = KollamPlacesRepository.computeMinDistanceToRouteKm(rest.geoPoint, activeRoutePoints);
            NearbyPlace place = new NearbyPlace(rest.name, rest.category, rest.rating, routeDistance,
                    rest.geoPoint, rest.address, rest.category.equalsIgnoreCase("Cafe") ? R.drawable.ic_cafe : R.drawable.ic_restaurant);
            allSamplePlaces.add(place);
        }
    }

    private void setupRecyclerView() {
        rvNearbyPlaces.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NearbyPlaceAdapter(this, activePlacesList, place -> {
            // Focus map on clicked place marker
            osmMapView.getController().animateTo(place.getGeoPoint());
            osmMapView.getController().setZoom(15.5);
            Toast.makeText(this, "Focused on " + place.getName(), Toast.LENGTH_SHORT).show();
        });
        rvNearbyPlaces.setAdapter(adapter);
    }

    private void setupRadarControls() {
        // Toggle Radar button
        btnToggleRadar.setOnClickListener(v -> {
            isRadarEnabled = !isRadarEnabled;
            if (isRadarEnabled) {
                btnToggleRadar.setText("Radar: ON");
                btnToggleRadar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#9DE7DF")));
                cardRadarControls.setVisibility(View.VISIBLE);
            } else {
                btnToggleRadar.setText("Radar: OFF");
                btnToggleRadar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#64748B")));
                cardRadarControls.setVisibility(View.GONE);
            }
            updateRadarAndMarkers();
        });

        // Recenter Map / GPS button
        btnRecenterMap.setOnClickListener(v -> checkAndMoveToLiveGps());

        // Radar Distance Slider change listener
        sliderRadarDistance.addOnChangeListener((slider, value, fromUser) -> {
            currentRadarRadiusKm = value;
            tvRadarRadiusValue.setText(String.format(Locale.getDefault(), "%.1f km", value));
            syncRadiusChipsWithSlider(value);
            updateRadarAndMarkers();
        });

        // Radius Preset Chips
        Chip chipRad1 = findViewById(R.id.chipRad1);
        Chip chipRad25 = findViewById(R.id.chipRad25);
        Chip chipRad5 = findViewById(R.id.chipRad5);
        Chip chipRad10 = findViewById(R.id.chipRad10);

        if (chipRad1 != null) chipRad1.setOnClickListener(v -> sliderRadarDistance.setValue(1.0f));
        if (chipRad25 != null) chipRad25.setOnClickListener(v -> sliderRadarDistance.setValue(2.5f));
        if (chipRad5 != null) chipRad5.setOnClickListener(v -> sliderRadarDistance.setValue(5.0f));
        if (chipRad10 != null) chipRad10.setOnClickListener(v -> sliderRadarDistance.setValue(10.0f));

        // Filter Category Chips
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.filterFood)) {
                selectedCategoryFilter = "Restaurant";
            } else if (checkedIds.contains(R.id.filterCafe)) {
                selectedCategoryFilter = "Cafe";
            } else if (checkedIds.contains(R.id.filterGas)) {
                selectedCategoryFilter = "Fuel";
            } else if (checkedIds.contains(R.id.filterSpot)) {
                selectedCategoryFilter = "Hidden Spot";
            } else {
                selectedCategoryFilter = "All";
            }
            updateRadarAndMarkers();
        });
    }

    private void syncRadiusChipsWithSlider(float value) {
        if (Math.abs(value - 1.0f) < 0.2f) {
            chipGroupRadius.check(R.id.chipRad1);
        } else if (Math.abs(value - 2.5f) < 0.2f) {
            chipGroupRadius.check(R.id.chipRad25);
        } else if (Math.abs(value - 5.0f) < 0.2f) {
            chipGroupRadius.check(R.id.chipRad5);
        } else if (Math.abs(value - 10.0f) < 0.2f) {
            chipGroupRadius.check(R.id.chipRad10);
        } else {
            chipGroupRadius.clearCheck();
        }
    }

    private void updateRadarAndMarkers() {
        // Calculate dynamic distances from route & traveler position
        activePlacesList.clear();

        for (NearbyPlace place : allSamplePlaces) {
            double distanceKm = place.getDistanceKm();
            boolean isWithinRadar = !isRadarEnabled || (distanceKm <= currentRadarRadiusKm);
            boolean isCategoryMatch = selectedCategoryFilter.equals("All") || place.getCategory().equalsIgnoreCase(selectedCategoryFilter);

            if (isWithinRadar && isCategoryMatch) {
                activePlacesList.add(place);
            }
        }

        // Draw Radar Circle Polygon on OpenStreetMap
        drawRadarCircle();

        // Draw Markers for filtered places on OpenStreetMap
        drawPlaceMarkers();

        // Update Adapter Data
        adapter.updateData(activePlacesList);

        // Update Header & Badge
        tvPlaceCountBadge.setText(activePlacesList.size() + " found");
        tvRadarStatusText.setText(String.format(Locale.getDefault(), "Showing %d places along route within %.1f km corridor", activePlacesList.size(), currentRadarRadiusKm));

        osmMapView.invalidate();
    }

    private void drawRadarCircle() {
        // Remove previous radar overlays
        if (radarCircleOverlay != null) {
            osmMapView.getOverlays().remove(radarCircleOverlay);
            radarCircleOverlay = null;
        }

        if (radarInnerCircleOverlay != null) {
            osmMapView.getOverlays().remove(radarInnerCircleOverlay);
            radarInnerCircleOverlay = null;
        }

        if (!isRadarEnabled || currentTravelerPoint == null) return;

        // Draw outer Radar Circle Polygon on MapView
        radarCircleOverlay = new Polygon();
        List<GeoPoint> circlePoints = Polygon.pointsAsCircle(currentTravelerPoint, currentRadarRadiusKm * 1000.0);
        radarCircleOverlay.setPoints(circlePoints);
        radarCircleOverlay.getFillPaint().setStyle(Paint.Style.FILL);
        radarCircleOverlay.getFillPaint().setColor(Color.argb(70, 37, 99, 235)); // Bright semi-transparent blue fill
        radarCircleOverlay.getOutlinePaint().setStyle(Paint.Style.STROKE);
        radarCircleOverlay.getOutlinePaint().setColor(Color.parseColor("#2563EB")); // Solid blue stroke
        radarCircleOverlay.getOutlinePaint().setStrokeWidth(6.0f);

        // Draw inner Radar Pulse Circle Polygon (50% radius)
        radarInnerCircleOverlay = new Polygon();
        List<GeoPoint> innerCirclePoints = Polygon.pointsAsCircle(currentTravelerPoint, (currentRadarRadiusKm * 1000.0) * 0.5);
        radarInnerCircleOverlay.setPoints(innerCirclePoints);
        radarInnerCircleOverlay.getFillPaint().setStyle(Paint.Style.FILL);
        radarInnerCircleOverlay.getFillPaint().setColor(Color.argb(45, 96, 165, 250));
        radarInnerCircleOverlay.getOutlinePaint().setStyle(Paint.Style.STROKE);
        radarInnerCircleOverlay.getOutlinePaint().setColor(Color.parseColor("#60A5FA"));
        radarInnerCircleOverlay.getOutlinePaint().setStrokeWidth(4.0f);

        // Add circles to map overlays at index 1 so they render under markers and above map tiles/route
        int insertIndex = Math.min(1, osmMapView.getOverlays().size());
        osmMapView.getOverlays().add(insertIndex, radarCircleOverlay);
        osmMapView.getOverlays().add(insertIndex + 1, radarInnerCircleOverlay);
    }

    private void drawPlaceMarkers() {
        // Clear old place markers
        for (Marker marker : placeMarkers) {
            osmMapView.getOverlays().remove(marker);
        }
        placeMarkers.clear();

        // Add markers for current active places
        for (NearbyPlace place : activePlacesList) {
            Marker placeMarker = new Marker(osmMapView);
            placeMarker.setPosition(place.getGeoPoint());
            placeMarker.setTitle(place.getName() + " (" + place.getCategory() + ")");
            placeMarker.setSnippet(String.format(Locale.getDefault(), "Rating: %.1f ⭐ • %.1f km from route", place.getRating(), place.getDistanceKm()));
            placeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            placeMarker.setOnMarkerClickListener((marker, mapView) -> {
                marker.showInfoWindow();
                Toast.makeText(MapRouteActivity.this, "Selected: " + place.getName(), Toast.LENGTH_SHORT).show();
                return true;
            });

            osmMapView.getOverlays().add(placeMarker);
            placeMarkers.add(placeMarker);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (osmMapView != null) osmMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopJourneyNavigation();
        if (osmMapView != null) osmMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopJourneyNavigation();
    }
}