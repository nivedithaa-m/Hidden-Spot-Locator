package com.example.androidpractice;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseAuthManager {

    private static final String TAG = "FirebaseAuthManager";

    public interface AuthCallback {
        void onSuccess(String userId, String email, String role);
        void onError(String errorMessage);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public static boolean isFirebaseReady() {
        try {
            Context context = PracticeApplication.getInstance();
            if (context != null) {
                List<FirebaseApp> apps = FirebaseApp.getApps(context);
                return !apps.isEmpty();
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isUserLoggedIn() {
        try {
            if (!isFirebaseReady()) return false;
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            return user != null;
        } catch (Exception e) {
            Log.w(TAG, "isUserLoggedIn check exception: " + e.getMessage());
            return false;
        }
    }

    public static FirebaseUser getCurrentUser() {
        try {
            if (!isFirebaseReady()) return null;
            return FirebaseAuth.getInstance().getCurrentUser();
        } catch (Exception e) {
            Log.w(TAG, "getCurrentUser exception: " + e.getMessage());
            return null;
        }
    }

    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : "";
    }

    public static String getCurrentUserEmail() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getEmail() : "";
    }

    public static void registerUser(String email, String password, String displayName, String role, AuthCallback callback) {
        registerUser(email, password, displayName, role, null, callback);
    }

    public static void registerUser(String email, String password, String displayName, String role, Map<String, Object> extraFields, AuthCallback callback) {
        try {
            if (!isFirebaseReady()) {
                if (callback != null) callback.onError("Firebase service is unavailable.");
                return;
            }
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = authResult.getUser();
                        if (user != null) {
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("uid", user.getUid());
                            userData.put("email", email);
                            userData.put("displayName", displayName);
                            userData.put("username", displayName);
                            userData.put("role", role != null ? role : "traveller");
                            userData.put("createdAt", System.currentTimeMillis());

                            if (extraFields != null) {
                                userData.putAll(extraFields);
                            }

                            try {
                                FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(user.getUid())
                                        .set(userData)
                                        .addOnSuccessListener(unused -> {
                                            Log.d(TAG, "User document successfully created in Firestore for UID: " + user.getUid());
                                            if (callback != null) {
                                                callback.onSuccess(user.getUid(), email, role);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Firestore write failed: " + e.getMessage());
                                            if (callback != null) {
                                                callback.onError("Account created in Auth, but Firestore write failed: " + e.getLocalizedMessage());
                                            }
                                        });
                            } catch (Exception e) {
                                Log.e(TAG, "Firestore exception: " + e.getMessage());
                                if (callback != null) {
                                    callback.onError("Firestore exception: " + e.getLocalizedMessage());
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) {
                            callback.onError("Registration failed: " + e.getLocalizedMessage());
                        }
                    });
        } catch (Exception e) {
            if (callback != null) callback.onError("Firebase error: " + e.getLocalizedMessage());
        }
    }

    public static void loginUser(String email, String password, AuthCallback callback) {
        try {
            if (!isFirebaseReady()) {
                if (callback != null) callback.onError("Firebase service is unavailable.");
                return;
            }
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = authResult.getUser();
                        if (user != null) {
                            fetchUserRole(user.getUid(), callback);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) {
                            callback.onError("Login failed: " + e.getLocalizedMessage());
                        }
                    });
        } catch (Exception e) {
            if (callback != null) callback.onError("Firebase error: " + e.getLocalizedMessage());
        }
    }

    private static void fetchUserRole(String uid, AuthCallback callback) {
        try {
            if (!isFirebaseReady()) {
                if (callback != null) callback.onSuccess(uid, getCurrentUserEmail(), "traveller");
                return;
            }
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String role = "traveller";
                        if (doc.exists() && doc.getString("role") != null) {
                            role = doc.getString("role");
                        }
                        if (callback != null) {
                            callback.onSuccess(uid, getCurrentUserEmail(), role);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) {
                            callback.onSuccess(uid, getCurrentUserEmail(), "traveller");
                        }
                    });
        } catch (Exception e) {
            if (callback != null) callback.onSuccess(uid, getCurrentUserEmail(), "traveller");
        }
    }

    public static void logoutUser() {
        try {
            if (isFirebaseReady()) {
                FirebaseAuth.getInstance().signOut();
            }
        } catch (Exception ignored) {}
    }

    public static void submitUserHiddenSpot(String spotName, String description, double lat, double lng, String address, ActionCallback callback) {
        try {
            if (!isFirebaseReady()) {
                if (callback != null) callback.onError("Firebase service unavailable.");
                return;
            }
            FirebaseUser user = getCurrentUser();
            String userId = user != null ? user.getUid() : "guest";

            Map<String, Object> spotData = new HashMap<>();
            spotData.put("name", spotName);
            spotData.put("description", description);
            spotData.put("latitude", lat);
            spotData.put("longitude", lng);
            spotData.put("address", address);
            spotData.put("submittedBy", userId);
            spotData.put("status", "pending");
            spotData.put("createdAt", System.currentTimeMillis());

            FirebaseFirestore.getInstance()
                    .collection("pending_submissions")
                    .add(spotData)
                    .addOnSuccessListener(doc -> {
                        if (callback != null) callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onError("Failed to submit spot: " + e.getLocalizedMessage());
                    });
        } catch (Exception e) {
            if (callback != null) callback.onError("Firebase error: " + e.getLocalizedMessage());
        }
    }
}