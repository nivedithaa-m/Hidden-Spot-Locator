package com.example.androidpractice;

import org.osmdroid.util.GeoPoint;

public class NearbyPlace {
    private String name;
    private String category; // "Restaurant", "Cafe", "Fuel", "Spot"
    private double rating;
    private double distanceKm;
    private GeoPoint geoPoint;
    private String address;
    private int iconResId;

    public NearbyPlace(String name, String category, double rating, double distanceKm, GeoPoint geoPoint, String address, int iconResId) {
        this.name = name;
        this.category = category;
        this.rating = rating;
        this.distanceKm = distanceKm;
        this.geoPoint = geoPoint;
        this.address = address;
        this.iconResId = iconResId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getRating() {
        return rating;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public String getAddress() {
        return address;
    }

    public int getIconResId() {
        return iconResId;
    }
}