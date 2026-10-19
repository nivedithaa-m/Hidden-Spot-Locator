package com.example.androidpractice;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

public class PracticeApplication extends Application {

    private static final String TAG = "PracticeApplication";
    private static PracticeApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
                Log.d(TAG, "FirebaseApp initialized successfully in PracticeApplication.");
            }
        } catch (Exception e) {
            Log.w(TAG, "FirebaseApp initialization notice: " + e.getMessage());
        }
    }

    public static PracticeApplication getInstance() {
        return instance;
    }
}