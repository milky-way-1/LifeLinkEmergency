package com.emergency;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.emergency.api.RetrofitClient;
import com.emergency.model.AmbulanceDriver;
import com.emergency.model.ApiResponse;
import com.emergency.model.Booking;
import com.emergency.model.Location;
import com.emergency.model.LocationUpdateDto;
import com.emergency.util.LocationUpdateService;
import com.emergency.util.NewBookingEvent;
import com.emergency.util.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Dashboard extends AppCompatActivity implements BookingAdapter.OnBookingClickListener {
    private RecyclerView bookingRecyclerView;
    private View emptyStateLayout;
    private View registeredDriverLayout;
    private View unregisteredDriverLayout;
    private ProgressBar driverStatusLoading;
    private MaterialButton registerButton;
    private BookingAdapter bookingAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private SessionManager sessionManager;
    private TextView driverNameText;
    private TextView vehicleTypeText;
    private TextView plateNumberText;

    private static final int LOCATION_UPDATE_INTERVAL = 5000; // 5 seconds
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private Handler locationHandler;
    private Runnable locationRunnable;
    private boolean isRegisteredDriver = false;
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        try {
            sessionManager = new SessionManager(this);
            locationHandler = new Handler(Looper.getMainLooper());
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            initializeViews();
            setupRecyclerView();
            checkDriverProfile();
        } catch (Exception e) {

        }
    }

    private void initializeViews() {
        try {
            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
            bookingRecyclerView = findViewById(R.id.bookingRecyclerView);
            emptyStateLayout = findViewById(R.id.emptyStateLayout);
            registeredDriverLayout = findViewById(R.id.registeredDriverLayout);
            unregisteredDriverLayout = findViewById(R.id.unregisteredDriverLayout);
            driverStatusLoading = findViewById(R.id.driverStatusLoading);

            // Initialize TextViews from registeredDriverLayout
            if (registeredDriverLayout != null) {
                driverNameText = registeredDriverLayout.findViewById(R.id.driverNameText);
                vehicleTypeText = registeredDriverLayout.findViewById(R.id.vehicleTypeText);
                plateNumberText = registeredDriverLayout.findViewById(R.id.plateNumberText);
            }

            // Initialize register button from unregisteredDriverLayout
            if (unregisteredDriverLayout != null) {
                registerButton = unregisteredDriverLayout.findViewById(R.id.registerButton);
                if (registerButton != null) {
                    registerButton.setOnClickListener(v -> {
                        Intent intent = new Intent(this, DriverRegistration.class);
                        startActivity(intent);
                    });
                }
            }

            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setOnRefreshListener(this::refreshData);
                swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright);
            }
        } catch (Exception e) {

        }
    }

    private void startLocationUpdates() {
        if (!isRegisteredDriver) {
            Log.d(TAG, "Not starting location updates - driver not registered");
            return;
        }

        // Check and request location permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting location permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        Log.d(TAG, "Starting location updates");

        // Create location request
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_UPDATE_INTERVAL)
                .setFastestInterval(LOCATION_UPDATE_INTERVAL);

        // Create location callback
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (android.location.Location location : locationResult.getLocations()) {
                    updateDriverLocationWithLocation(location);
                }
            }
        };

        // Request location updates
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());

            Log.d(TAG, "Location updates started successfully");
        } catch (SecurityException e) {
            Log.e(TAG, "Error starting location updates: " + e.getMessage());
        }
    }

    private void updateDriverLocationWithLocation(android.location.Location location) {
        if (location == null) return;

        String token = sessionManager.getToken();
        if (token == null || !isRegisteredDriver) return;

        // Create location DTO with actual coordinates
        LocationUpdateDto locationDto = new LocationUpdateDto(
                location.getLatitude(),
                location.getLongitude()
        );

        // Make API call
        RetrofitClient.getInstance()
                .getApiService()
                .updateLocation("Bearer " + token, locationDto)
                .enqueue(new Callback<Location>() {
                    @Override
                    public void onResponse(Call<Location> call,
                                           Response<Location> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Location updated successfully");
                        } else {
                            Log.e(TAG, "Failed to update location: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Location> call, Throwable t) {
                        Log.e(TAG, "Error updating location: " + t.getMessage());
                    }
                });
    }

    private void updateDriverLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            String token = sessionManager.getToken();
            if (token == null || !isRegisteredDriver) return;

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // Create location DTO with actual coordinates
                            LocationUpdateDto locationDto = new LocationUpdateDto(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );

                            // Make API call
                            RetrofitClient.getInstance()
                                    .getApiService()
                                    .updateLocation("Bearer " + token, locationDto)
                                    .enqueue(new Callback<Location>() {
                                        @Override
                                        public void onResponse(Call<Location> call,
                                                               Response<Location> response) {
                                            if (!response.isSuccessful()) {
                                                Log.e(TAG, "Failed to update location: " +
                                                        response.code());
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Location> call, Throwable t) {
                                            Log.e(TAG, "Error updating location: " +
                                                    t.getMessage());
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Error getting location: " + e.getMessage()));

        } catch (Exception e) {
            Log.e(TAG, "Error in updateDriverLocation: " + e.getMessage());
        }
    }

    private void setupRecyclerView() {
        try {
            bookingAdapter = new BookingAdapter(new ArrayList<>(), this);


            bookingRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            bookingRecyclerView.setAdapter(bookingAdapter);


        } catch (Exception e) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                startLocationUpdates();
            } else {
                Toast.makeText(this,
                        "Location permission is required for driver updates",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void refreshData() {
        try {
            String token = sessionManager.getToken();
            if (token == null) {
                swipeRefreshLayout.setRefreshing(false);

                return;
            }


            RetrofitClient.getInstance()
                    .getApiService()
                    .getDriverByUserId("Bearer " + token, sessionManager.getEmail())
                    .enqueue(new Callback<AmbulanceDriver>() {
                        @Override
                        public void onResponse(Call<AmbulanceDriver> call, Response<AmbulanceDriver> response) {
                            if(response.isSuccessful() && response.body() != null) {
                                fetchBookings(response.body().getId(), token);
                            } else {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }

                        @Override
                        public void onFailure(Call<AmbulanceDriver> call, Throwable t) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
        } catch (Exception e) {
        }
    }

    private void fetchBookings(String driverId, String token) {
        RetrofitClient.getInstance()
                .getApiService()
                .getDriverBookings("Bearer " + token, driverId)
                .enqueue(new Callback<List<Booking>>() {
                    @Override
                    public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                        swipeRefreshLayout.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            updateBookingList(response.body());
                        } else {
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Booking>> call, Throwable t) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void checkDriverProfile() {
        showLoadingState();
        String token = sessionManager.getToken();
        if (token == null) {
            hideLoadingState();
            updateDriverStatus(false, null);
            return;
        }

        RetrofitClient.getInstance()
                .getApiService()
                .getDriverProfile("Bearer " + token)
                .enqueue(new Callback<ApiResponse<AmbulanceDriver>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AmbulanceDriver>> call,
                                           Response<ApiResponse<AmbulanceDriver>> response) {
                        hideLoadingState();
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<AmbulanceDriver> apiResponse = response.body();
                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                isRegisteredDriver = true;
                                updateDriverStatus(true, apiResponse.getData());
                                refreshData();
                            } else {
                                updateDriverStatus(false, null);

                            }
                        } else {
                            updateDriverStatus(false, null);

                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AmbulanceDriver>> call, Throwable t) {
                        hideLoadingState();
                        updateDriverStatus(false, null);
                        Toast.makeText(Dashboard.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateDriverStatus(boolean isRegistered, AmbulanceDriver driver) {
        runOnUiThread(() -> {
            try {
                isRegisteredDriver = isRegistered;
                if (isRegistered && driver != null) {
                    if (registeredDriverLayout != null) {
                        registeredDriverLayout.setVisibility(View.VISIBLE);
                        driverNameText.setText(driver.getFullName());
                        vehicleTypeText.setText(driver.getLicenseType());
                        plateNumberText.setText(driver.getVehicleRegistrationNumber());
                    }
                    if (unregisteredDriverLayout != null) {
                        unregisteredDriverLayout.setVisibility(View.GONE);
                    }
                    if (bookingRecyclerView != null) {
                        bookingRecyclerView.setVisibility(View.VISIBLE);
                    }
                    startLocationUpdates();
                } else {
                    if (registeredDriverLayout != null) {
                        registeredDriverLayout.setVisibility(View.GONE);
                    }
                    if (unregisteredDriverLayout != null) {
                        unregisteredDriverLayout.setVisibility(View.VISIBLE);
                    }
                    if (bookingRecyclerView != null) {
                        bookingRecyclerView.setVisibility(View.GONE);
                    }
                    if (emptyStateLayout != null) {
                        emptyStateLayout.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                Toast.makeText(Dashboard.this,
                        "Error updating UI: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBookingList(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            bookingRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            bookingRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            bookingAdapter.updateBookings(bookings);
        }
    }

    private void showLoadingState() {
        driverStatusLoading.setVisibility(View.VISIBLE);
        registeredDriverLayout.setVisibility(View.GONE);
        unregisteredDriverLayout.setVisibility(View.GONE);
    }

    private void hideLoadingState() {
        driverStatusLoading.setVisibility(View.GONE);
    }

    @Override
    public void onBookingClick(Booking booking) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("booking_id", booking.getId());
        intent.putExtra("pickup_lat", booking.getPickupLocation().getLatitude());
        intent.putExtra("pickup_lng", booking.getPickupLocation().getLongitude());
        intent.putExtra("drop_lat", booking.getDestinationLocation().getLatitude());
        intent.putExtra("drop_lng", booking.getDestinationLocation().getLongitude());
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDriverProfile();
        if (isRegisteredDriver) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        try {
            // Remove handler callbacks
            if (locationHandler != null && locationRunnable != null) {
                locationHandler.removeCallbacks(locationRunnable);
            }

            // Remove location updates from FusedLocationClient
            if (fusedLocationClient != null) {
                fusedLocationClient.removeLocationUpdates(new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                    }
                });
            }

            Log.d(TAG, "Location updates stopped successfully");

        } catch (SecurityException e) {
            Log.e(TAG, "Error stopping location updates (Security): " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error stopping location updates: " + e.getMessage());
        }
    }
}