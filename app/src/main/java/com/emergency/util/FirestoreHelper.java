package com.emergency.util;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {
    private static final String TAG = "FirestoreHelper";
    private final FirebaseFirestore db;
    private static FirestoreHelper instance;

    private FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirestoreHelper getInstance() {
        if (instance == null) {
            instance = new FirestoreHelper();
        }
        return instance;
    }

    // Create initial booking document with driver location
    public void createInitialDriverLocation(String bookingId, LatLng initialLocation) {
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", initialLocation.latitude);
        locationData.put("longitude", initialLocation.longitude);

        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("driverLocation", locationData);

        db.collection("bookings")
                .document(bookingId)
                .set(bookingData)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Initial driver location set successfully"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error setting initial location", e));
    }

    // Update driver location
    public void updateDriverLocation(String bookingId, LatLng location) {
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", location.latitude);
        locationData.put("longitude", location.longitude);

        db.collection("bookings")
                .document(bookingId)
                .update("driverLocation", locationData)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Driver location updated successfully"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error updating driver location", e));
    }

    // Listen to driver location (for user app)
    public ListenerRegistration listenToDriverLocation(String bookingId,
                                                       EventListener<DocumentSnapshot> listener) {
        return db.collection("bookings")
                .document(bookingId)
                .addSnapshotListener(listener);
    }
}