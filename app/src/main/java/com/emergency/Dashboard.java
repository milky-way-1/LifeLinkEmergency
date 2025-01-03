package com.emergency;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emergency.api.RetrofitClient;
import com.emergency.model.AmbulanceDriver;
import com.emergency.model.ApiResponse;
import com.emergency.model.Booking;
import com.emergency.util.LocationUpdateService;
import com.emergency.util.NewBookingEvent;
import com.emergency.util.SessionManager;
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
    private static final String TAG = "Dashboard";
    private static final long BOOKING_CHECK_INTERVAL = 10000; // 10 seconds

    private RecyclerView bookingRecyclerView;
    private View emptyStateLayout;
    private View registeredDriverLayout;
    private View unregisteredDriverLayout;
    private ProgressBar driverStatusLoading;
    private MaterialButton registerButton;
    private com.emergency.BookingAdapter bookingAdapter;

    private SessionManager sessionManager;
    private LocationUpdateService locationService;
    private Handler bookingHandler;
    private boolean isCheckingBookings = false;

    // Driver info TextViews
    private TextView driverNameText;
    private TextView vehicleTypeText;
    private TextView plateNumberText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Make status bar transparent
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        locationService = LocationUpdateService.getInstance(this);
        bookingHandler = new Handler(Looper.getMainLooper());
        sessionManager = new SessionManager(this);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        checkDriverProfile();
    }

    private void initializeViews() {
        bookingRecyclerView = findViewById(R.id.bookingRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        registeredDriverLayout = findViewById(R.id.registeredDriverLayout);
        unregisteredDriverLayout = findViewById(R.id.unregisteredDriverLayout);
        driverStatusLoading = findViewById(R.id.driverStatusLoading);
        registerButton = findViewById(R.id.registerButton);

        driverNameText = findViewById(R.id.driverNameText);
        vehicleTypeText = findViewById(R.id.vehicleTypeText);
        plateNumberText = findViewById(R.id.plateNumberText);

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DriverRegistration.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        bookingRecyclerView.setLayoutManager(layoutManager);

        // Initialize adapter with empty list
        bookingAdapter = new com.emergency.BookingAdapter(new ArrayList<>(), this);
        bookingRecyclerView.setAdapter(bookingAdapter);

        // Add item decoration for spacing
        int spacing = getResources().getDimensionPixelSize(R.dimen.booking_item_spacing);
        bookingRecyclerView.addItemDecoration(new SpacingItemDecoration(spacing));

        // Enable animations
        bookingRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Optimize performance
        bookingRecyclerView.setHasFixedSize(true);
    }

    private void checkDriverProfile() {
        showLoadingState();

        String token = sessionManager.getToken();
        if (token == null) {
            showError("Authentication error. Please login again.");
            sessionManager.logout();
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
                                updateDriverStatus(true, apiResponse.getData());
                                startServices();
                            } else {
                                Log.d(TAG, "No driver profile found: " + apiResponse.getMessage());
                                updateDriverStatus(false, null);
                            }
                        } else {
                            handleErrorResponse(response);
                            updateDriverStatus(false, null);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AmbulanceDriver>> call, Throwable t) {
                        Log.e(TAG, "Network error", t);
                        hideLoadingState();
                        showError("Network error. Please check your connection.");
                        updateDriverStatus(false, null);
                    }
                });
    }

    private void startServices() {
        locationService.startUpdates();
        startBookingChecks();
    }

    private void stopServices() {
        locationService.stopUpdates();
        stopBookingChecks();
    }

    private void startBookingChecks() {
        isCheckingBookings = true;
        checkForBookings();
    }

    private void stopBookingChecks() {
        isCheckingBookings = false;
        bookingHandler.removeCallbacksAndMessages(null);
    }

    private void checkForBookings() {
        if (!isCheckingBookings) {
            Log.d(TAG, "Booking checks stopped");
            return;
        }

        String token = sessionManager.getToken();
        if (token == null) {
            Log.e(TAG, "No token available");
            return;
        }

        String driverId = sessionManager.getUserId();
        Log.d(TAG, "Checking bookings for driver: " + driverId);

        RetrofitClient.getInstance()
                .getApiService()
                .getDriverBookings("Bearer " + token, driverId)
                .enqueue(new Callback<List<Booking>>() {
                    @Override
                    public void onResponse(Call<List<Booking>> call,
                                           Response<List<Booking>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Booking> bookings = response.body();
                            Log.d(TAG, "Received " + bookings.size() + " bookings");
                            updateBookingList(bookings);
                        } else {
                            Log.e(TAG, "Error response: " + response.code());
                        }

                        scheduleNextBookingCheck();
                    }

                    @Override
                    public void onFailure(Call<List<Booking>> call, Throwable t) {
                        Log.e(TAG, "Network error checking bookings", t);
                        scheduleNextBookingCheck();
                    }
                });
    }

    private void scheduleNextBookingCheck() {
        if (isCheckingBookings) {
            bookingHandler.postDelayed(this::checkForBookings, BOOKING_CHECK_INTERVAL);
        }
    }



    private void updateBookingList(List<Booking> bookings) {
        if (bookings == null) {
            Log.e(TAG, "Received null booking list");
            return;
        }

        Log.d(TAG, "Updating booking list with " + bookings.size() + " bookings");

        runOnUiThread(() -> {
            try {
                if (bookings.isEmpty()) {
                    Log.d(TAG, "Booking list is empty");
                    bookingRecyclerView.setVisibility(View.GONE);
                    emptyStateLayout.setVisibility(View.VISIBLE);
                } else {
                    Log.d(TAG, "Showing bookings in RecyclerView");
                    bookingRecyclerView.setVisibility(View.VISIBLE);
                    emptyStateLayout.setVisibility(View.GONE);
                    bookingAdapter.updateBookings(bookings);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating booking list", e);
                showError("Error updating bookings: " + e.getMessage());
            }
        });
    }

    private void showLoadingState() {
        driverStatusLoading.setVisibility(View.VISIBLE);
        registeredDriverLayout.setVisibility(View.GONE);
        unregisteredDriverLayout.setVisibility(View.GONE);
    }

    private void hideLoadingState() {
        driverStatusLoading.setVisibility(View.GONE);
    }

    private void updateDriverStatus(boolean isRegistered, AmbulanceDriver driver) {
        if (isRegistered && driver != null) {
            registeredDriverLayout.setVisibility(View.VISIBLE);
            unregisteredDriverLayout.setVisibility(View.GONE);

            driverNameText.setText(driver.getFullName());
            vehicleTypeText.setText(formatLicenseType(driver.getLicenseType()));
            plateNumberText.setText(driver.getVehicleRegistrationNumber());
        } else {
            registeredDriverLayout.setVisibility(View.GONE);
            unregisteredDriverLayout.setVisibility(View.VISIBLE);
        }
    }

    private String formatLicenseType(String licenseType) {
        if (licenseType == null) return "N/A";
        return licenseType.replace("_", " ")
                .toLowerCase()
                .replace("drivers", "Driver's")
                .replace("license", "License");
    }

    private void handleErrorResponse(Response<?> response) {
        try {
            String errorBody = response.errorBody().string();
            Log.e(TAG, "Error response: " + errorBody);
            Gson gson = new Gson();
            ApiResponse<?> errorResponse = gson.fromJson(errorBody, ApiResponse.class);
            showError(errorResponse.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error response", e);
            showError("Failed to fetch profile");
        }
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Error")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        });
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
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDriverProfile();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            stopServices();
        }
    }

    @Override
    protected void onDestroy() {
        stopServices();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewBooking(NewBookingEvent event) {
        Log.d(TAG, "Received new booking event");
        List<Booking> bookings = event.getBookings();
        updateBookingList(bookings);
    }
}