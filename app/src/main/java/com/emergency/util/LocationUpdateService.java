package com.emergency.util;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.emergency.api.ApiService;
import com.emergency.api.RetrofitClient;
import com.emergency.model.Booking;
import com.emergency.model.Location;
import com.emergency.model.LocationUpdateDto;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationUpdateService {
    private static LocationUpdateService instance;
    private final Handler handler = new android.os.Handler(Looper.getMainLooper()) {
    };
    private final long LOCATION_UPDATE_INTERVAL = 5000; // 5 seconds
    private final long BOOKING_CHECK_INTERVAL = 10000; // 10 seconds
    private boolean isRunning = false;
    private final ApiService apiService;
    private final SessionManager sessionManager;
    private final Context context;
    private FusedLocationProviderClient fusedLocationClient;

    private LocationUpdateService(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = RetrofitClient.getInstance().getApiService();
        this.sessionManager = new SessionManager(context);
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public static synchronized LocationUpdateService getInstance(Context context) {
        if (instance == null) {
            instance = new LocationUpdateService(context);
        }
        return instance;
    }

    public void startUpdates() {
        if (!isRunning) {
            isRunning = true;
            startLocationUpdates();
            startBookingChecks();
        }
    }

    public void stopUpdates() {
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
    }

    private void startLocationUpdates() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!isRunning) return;
                updateLocation();
                handler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
            }
        });
    }

    private void startBookingChecks() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!isRunning) return;
                checkForBookings();
                handler.postDelayed(this, BOOKING_CHECK_INTERVAL);
            }
        });
    }

    private void updateLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LocationUpdateDto locationUpdateDto = new LocationUpdateDto(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );

                            RetrofitClient.getInstance()
                                    .getApiService()
                                    .updateLocation("Bearer " + sessionManager.getToken(), locationUpdateDto)
                                    .enqueue(new retrofit2.Callback<Location>() {
                                        @Override
                                        public void onResponse(Call<Location> call, Response<Location> response) {
                                            if (!response.isSuccessful()) {
                                                Log.e(TAG, "Failed to update location: " + response.code());
                                            } else {
                                                Location updatedLocation = response.body();
                                                if (updatedLocation != null) {
                                                    Log.d(TAG, "Location updated successfully: " + updatedLocation);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Location> call, Throwable t) {
                                            Log.e(TAG, "Error updating location", t);
                                        }
                                    });
                        }
                    });
        }
    }

    private void checkForBookings() {
        String driverId = sessionManager.getUserId();
        apiService.getDriverBookings("Bearer " + sessionManager.getToken(), driverId)
                .enqueue(new Callback<List<Booking>>() {
                    @Override
                    public void onResponse(Call<List<Booking>> call,
                                           Response<List<Booking>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Booking> bookings = response.body();
                            if (!bookings.isEmpty()) {
                                // Notify any active observers about new bookings
                                EventBus.getDefault().post(new NewBookingEvent(bookings));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Booking>> call, Throwable t) {
                        Log.e("BookingCheck", "Error: " + t.getMessage());
                    }
                });
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}