package com.example.androidpractice;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoadRoutingService {

    private static final String TAG = "HiddenSpotLocator";

    public static class RouteResult {
        public List<GeoPoint> routePolylinePoints;
        public double roadDistanceKm;
        public int estimatedDurationMins;
        public String distanceFormatted;
        public String durationFormatted;

        public RouteResult(List<GeoPoint> points, double distanceMeters, double durationSeconds) {
            this.routePolylinePoints = points;
            this.roadDistanceKm = distanceMeters / 1000.0;
            this.estimatedDurationMins = (int) Math.round(durationSeconds / 60.0);

            this.distanceFormatted = String.format(Locale.getDefault(), "%.1f km", roadDistanceKm);
            if (estimatedDurationMins >= 60) {
                int hours = estimatedDurationMins / 60;
                int mins = estimatedDurationMins % 60;
                this.durationFormatted = String.format(Locale.getDefault(), "%dh %d mins", hours, mins);
            } else {
                this.durationFormatted = String.format(Locale.getDefault(), "%d mins", estimatedDurationMins);
            }
        }
    }

    public interface RouteCallback {
        void onRouteCalculated(RouteResult result);
        void onError(String errorMessage);
    }

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void fetchRoadRoute(GeoPoint startPoint, GeoPoint destinationPoint, RouteCallback callback) {
        if (startPoint == null || destinationPoint == null) {
            if (callback != null) callback.onError("Invalid start or destination location.");
            return;
        }

        Log.d(TAG, String.format(Locale.US, "OSRM Route Request Parameters: Start=(Lat: %.6f, Lng: %.6f) -> Dest=(Lat: %.6f, Lng: %.6f)",
                startPoint.getLatitude(), startPoint.getLongitude(), destinationPoint.getLatitude(), destinationPoint.getLongitude()));

        executor.execute(() -> {
            try {
                // OSRM format: /route/v1/driving/{lng1},{lat1};{lng2},{lat2}?overview=full&geometries=geojson
                String urlString = String.format(Locale.US,
                        "https://router.project-osrm.org/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson",
                        startPoint.getLongitude(), startPoint.getLatitude(),
                        destinationPoint.getLongitude(), destinationPoint.getLatitude());

                Log.d(TAG, "OSRM Routing Request URL: " + urlString);

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "HiddenSpotLocator/1.0 (com.example.androidpractice.newversion)");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    reader.close();

                    JSONObject root = new JSONObject(builder.toString());
                    JSONArray routes = root.optJSONArray("routes");

                    if (routes != null && routes.length() > 0) {
                        JSONObject route = routes.getJSONObject(0);
                        double distanceMeters = route.optDouble("distance", 0.0);
                        double durationSeconds = route.optDouble("duration", 0.0);

                        JSONObject geometry = route.optJSONObject("geometry");
                        List<GeoPoint> pointsList = new ArrayList<>();

                        if (geometry != null) {
                            JSONArray coords = geometry.optJSONArray("coordinates");
                            if (coords != null) {
                                for (int i = 0; i < coords.length(); i++) {
                                    JSONArray pair = coords.getJSONArray(i);
                                    double lng = pair.getDouble(0);
                                    double lat = pair.getDouble(1);
                                    pointsList.add(new GeoPoint(lat, lng));
                                }
                            }
                        }

                        if (pointsList.isEmpty()) {
                            pointsList.add(startPoint);
                            pointsList.add(destinationPoint);
                        }

                        RouteResult result = new RouteResult(pointsList, distanceMeters, durationSeconds);
                        Log.d(TAG, String.format(Locale.US, "OSRM Route Calculated Successfully: Distance=%.2f km (%s), Duration=%d mins (%s), GeometryPoints=%d",
                                result.roadDistanceKm, result.distanceFormatted, result.estimatedDurationMins, result.durationFormatted, pointsList.size()));

                        mainHandler.post(() -> {
                            if (callback != null) callback.onRouteCalculated(result);
                        });
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "OSRM Routing Error: " + e.getLocalizedMessage(), e);
                mainHandler.post(() -> {
                    if (callback != null) callback.onError("Unable to calculate road route: " + e.getLocalizedMessage());
                });
                return;
            }

            Log.w(TAG, "OSRM Routing: No driving route found in API response.");
            mainHandler.post(() -> {
                if (callback != null) callback.onError("No driving route found between starting point and destination.");
            });
        });
    }
}