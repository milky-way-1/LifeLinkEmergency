package com.emergency;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emergency.api.RetrofitClient;
import com.emergency.model.AmbulanceDriver;
import com.emergency.model.ApiResponse;
import com.emergency.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Dashboard extends AppCompatActivity {
    private RecyclerView emergencyRecyclerView;
    private View emptyStateLayout;
    private View registeredDriverLayout;
    private View unregisteredDriverLayout;
    private ProgressBar driverStatusLoading;
    private MaterialButton registerButton;
    private EmergencyAdapter emergencyAdapter;

    private SessionManager sessionManager;

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

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        checkDriverProfile();
        loadNearbyEmergencies();
    }

    private void initializeViews() {
        emergencyRecyclerView = findViewById(R.id.emergencyRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        registeredDriverLayout = findViewById(R.id.registeredDriverLayout);
        unregisteredDriverLayout = findViewById(R.id.unregisteredDriverLayout);
        driverStatusLoading = findViewById(R.id.driverStatusLoading);
        registerButton = findViewById(R.id.registerButton);

        sessionManager = new SessionManager(this);

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
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setupRecyclerView() {
        emergencyAdapter = new EmergencyAdapter(new ArrayList<>(), this::onEmergencyClicked);
        emergencyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        emergencyRecyclerView.setAdapter(emergencyAdapter);

        // Add animation
        emergencyRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Add spacing between items
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.emergency_item_spacing);
        emergencyRecyclerView.addItemDecoration(new SpacingItemDecoration(spacingInPixels));
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

                        if (response.isSuccessful()) {
                            ApiResponse<AmbulanceDriver> apiResponse = response.body();
                            if (apiResponse != null) {
                                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                    updateDriverStatus(true, apiResponse.getData());
                                } else {
                                    Log.d(TAG, "No driver profile found: " + apiResponse.getMessage());
                                    updateDriverStatus(false, null);
                                }
                            } else {
                                showError("Invalid response from server");
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
        new MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void loadNearbyEmergencies() {
        // TODO: Replace with actual API call
        new Handler().postDelayed(() -> {
            List<EmergencyDTO> emergencies = getDummyEmergencies(); // Replace with API call
            updateEmergencyList(emergencies);
        }, 1000);
    }

    private void updateEmergencyList(List<EmergencyDTO> emergencies) {
        if (emergencies.isEmpty()) {
            emergencyRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            emergencyRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            emergencyAdapter.updateEmergencies(emergencies);
        }
    }

    private void onEmergencyClicked(EmergencyDTO emergency) {
//        Intent intent = new Intent(this, EmergencyDetailActivity.class);
//        intent.putExtra("emergency_id", emergency.getId());
//        startActivity(intent);
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    // Temporary method for testing
    private List<EmergencyDTO> getDummyEmergencies() {
        List<EmergencyDTO> emergencies = new ArrayList<>();
        emergencies.add(new EmergencyDTO("1", "123 Main St", "Car accident", "Critical", 2.5, "2 mins ago"));
        emergencies.add(new EmergencyDTO("2", "456 Oak Ave", "Medical emergency", "Stable", 3.1, "5 mins ago"));
        emergencies.add(new EmergencyDTO("3", "789 Pine Rd", "Fire incident", "Unknown", 1.8, "Just now"));
        return emergencies;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDriverProfile();
    }
}