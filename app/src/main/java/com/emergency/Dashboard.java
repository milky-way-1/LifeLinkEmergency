package com.emergency;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class Dashboard extends AppCompatActivity {
    private RecyclerView emergencyRecyclerView;
    private View emptyStateLayout;
    private View registeredDriverLayout;
    private View unregisteredDriverLayout;
    private ProgressBar driverStatusLoading;
    private MaterialButton registerButton;
    private EmergencyAdapter emergencyAdapter;

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
        checkDriverStatus();
        loadNearbyEmergencies();
    }

    private void initializeViews() {
        emergencyRecyclerView = findViewById(R.id.emergencyRecyclerView);
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

    private void checkDriverStatus() {
        // Show loading state
        driverStatusLoading.setVisibility(View.VISIBLE);
        registeredDriverLayout.setVisibility(View.GONE);
        unregisteredDriverLayout.setVisibility(View.GONE);

        // TODO: Replace with actual API call
        new Handler().postDelayed(() -> {
            // Simulate API call
            boolean isRegistered = false; // Get this from API
            updateDriverStatus(isRegistered);
        }, 1500);
    }

    private void updateDriverStatus(boolean isRegistered) {
        driverStatusLoading.setVisibility(View.GONE);

        if (isRegistered) {
            registeredDriverLayout.setVisibility(View.VISIBLE);
            unregisteredDriverLayout.setVisibility(View.GONE);

            // TODO: Replace with actual driver data
            driverNameText.setText("John Doe");
            vehicleTypeText.setText("Ambulance Type II");
            plateNumberText.setText("ABC 123");
        } else {
            registeredDriverLayout.setVisibility(View.GONE);
            unregisteredDriverLayout.setVisibility(View.VISIBLE);
        }
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
        checkDriverStatus();
    }
}