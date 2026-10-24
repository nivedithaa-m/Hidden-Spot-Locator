package com.example.androidpractice;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationGeocoder {

    private static final String TAG = "HiddenSpotLocator";

    public static class GeocodeResult {
        public GeoPoint geoPoint;
        public String displayName;
        public String addressDetail;

        public GeocodeResult(GeoPoint geoPoint, String displayName, String addressDetail) {
            this.geoPoint = geoPoint;
            this.displayName = displayName;
            this.addressDetail = addressDetail;
        }

        @Override
        public String toString() {
            return displayName + " (" + String.format(Locale.US, "%.4f, %.4f", geoPoint.getLatitude(), geoPoint.getLongitude()) + ")";
        }
    }

    public interface MultiGeocodeCallback {
        void onSuccess(List<GeocodeResult> results);
        void onError(String errorMessage);
    }

    public interface GeocodeCallback {
        void onSuccess(GeoPoint geoPoint, String formattedAddress);
        void onError(String errorMessage);
    }

    public static void geocodeLocation(Context context, String query, MultiGeocodeCallback callback) {
        if (query == null || query.trim().isEmpty()) {
            if (callback != null) callback.onError("Location search text cannot be empty.");
            return;
        }

        final String cleanQuery = query.trim();
        Log.d(TAG, "Geocode request initiated for query: '" + cleanQuery + "'");

        new Thread(() -> {
            List<GeocodeResult> candidates = new ArrayList<>();

            // 1. Try Android System Geocoder first with Kollam / Kerala bounds
            try {
                if (Geocoder.isPresent()) {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    String searchQuery = cleanQuery.toLowerCase().contains("kerala") ? cleanQuery : cleanQuery + ", Kollam, Kerala, India";
                    List<Address> addresses = geocoder.getFromLocationName(searchQuery, 5);

                    if (addresses != null && !addresses.isEmpty()) {
                        for (Address addr : addresses) {
                            GeoPoint point = new GeoPoint(addr.getLatitude(), addr.getLongitude());
                            String name = addr.getFeatureName() != null ? addr.getFeatureName() : cleanQuery;
                            String detail = addr.getAddressLine(0) != null ? addr.getAddressLine(0) : name;

                            Log.d(TAG, "Android Geocoder found candidate: " + name + " -> Lat=" + addr.getLatitude() + ", Lng=" + addr.getLongitude());
                            candidates.add(new GeocodeResult(point, name, detail));
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Android Geocoder error: " + e.getLocalizedMessage());
            }

            // 2. Query OpenStreetMap Nominatim Geocoding API with Kollam bounding box prioritization
            if (candidates.isEmpty()) {
                try {
                    String encodedQuery = URLEncoder.encode(cleanQuery + ", Kollam, Kerala", StandardCharsets.UTF_8.name());
                    // Bounding box for Kollam Region: left=76.40, top=9.25, right=77.25, bottom=8.70
                    String urlString = "https://nominatim.openstreetmap.org/search?format=json&q=" + encodedQuery +
                            "&viewbox=76.40,9.25,77.25,8.70&bounded=0&limit=5&addressdetails=1";

                    Log.d(TAG, "Querying Nominatim Geocoder URL: " + urlString);

                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("User-Agent", "HiddenSpotLocator/1.0 (com.example.androidpractice.newversion)");
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(8000);

                    if (conn.getResponseCode() == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        JSONArray jsonArray = new JSONArray(response.toString());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject place = jsonArray.getJSONObject(0);
                            double lat = place.getDouble("lat");
                            double lon = place.getDouble("lon");
                            String displayName = place.optString("display_name", cleanQuery);

                            Log.d(TAG, "Nominatim Geocoder found candidate: " + displayName + " -> Lat=" + lat + ", Lng=" + lon);
                            candidates.add(new GeocodeResult(new GeoPoint(lat, lon), cleanQuery, displayName));
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Nominatim Geocoder error: " + e.getLocalizedMessage(), e);
                }
            }

            if (!candidates.isEmpty()) {
                if (callback != null) {
                    callback.onSuccess(candidates);
                }
            } else {
                Log.w(TAG, "Geocoding returned NO results for query: '" + cleanQuery + "'");
                if (callback != null) {
                    callback.onError("Location '" + cleanQuery + "' could not be found. Please verify spelling.");
                }
            }
        }).start();
    }

    public static void geocodeDestination(Context context, String query, GeocodeCallback callback) {
        geocodeLocation(context, query, new MultiGeocodeCallback() {
            @Override
            public void onSuccess(List<GeocodeResult> results) {
                if (!results.isEmpty() && callback != null) {
                    GeocodeResult top = results.get(0);
                    Log.d(TAG, "geocodeDestination selected top result: " + top.toString());
                    callback.onSuccess(top.geoPoint, top.addressDetail);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (callback != null) {
                    callback.onError(errorMessage);
                }
            }
        });
    }
}