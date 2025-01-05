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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Toast.makeText(this, "1. Starting onCreate", Toast.LENGTH_SHORT).show();

            setContentView(R.layout.activity_dashboard);
            Toast.makeText(this, "2. Layout set", Toast.LENGTH_SHORT).show();

            sessionManager = new SessionManager(this);
            Toast.makeText(this, "3. Session manager created", Toast.LENGTH_SHORT).show();

            initializeViews();
            Toast.makeText(this, "4. Views initialized", Toast.LENGTH_SHORT).show();

            setupRecyclerView();
            Toast.makeText(this, "5. RecyclerView setup", Toast.LENGTH_SHORT).show();

            checkDriverProfile();
            Toast.makeText(this, "6. Driver profile check started", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "ERROR in onCreate: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        try {
            Toast.makeText(this, "Init Views 1: Finding SwipeRefresh", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

            Toast.makeText(this, "Init Views 2: Finding RecyclerView", Toast.LENGTH_SHORT).show();
            bookingRecyclerView = findViewById(R.id.bookingRecyclerView);

            Toast.makeText(this, "Init Views 3: Finding other layouts", Toast.LENGTH_SHORT).show();
            emptyStateLayout = findViewById(R.id.emptyStateLayout);
            registeredDriverLayout = findViewById(R.id.registeredDriverLayout);
            unregisteredDriverLayout = findViewById(R.id.unregisteredDriverLayout);
            driverStatusLoading = findViewById(R.id.driverStatusLoading);

            Toast.makeText(this, "Init Views 4: Finding TextViews", Toast.LENGTH_SHORT).show();
            driverNameText = findViewById(R.id.driverNameText);
            vehicleTypeText = findViewById(R.id.vehicleTypeText);
            plateNumberText = findViewById(R.id.plateNumberText);
            registerButton = findViewById(R.id.registerButton);

            Toast.makeText(this, "Init Views 5: Setting up register button", Toast.LENGTH_SHORT).show();
            if (registerButton != null) {
                registerButton.setOnClickListener(v -> {
                    Intent intent = new Intent(this, DriverRegistration.class);
                    startActivity(intent);
                });
            }

            Toast.makeText(this, "Init Views 6: Setting up SwipeRefresh", Toast.LENGTH_SHORT).show();
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setOnRefreshListener(this::refreshData);
                swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright);
            }

            Toast.makeText(this, "Init Views Complete", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "ERROR in initializeViews: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupRecyclerView() {
        try {
            Toast.makeText(this, "Setup RecyclerView 1: Creating adapter", Toast.LENGTH_SHORT).show();
            bookingAdapter = new BookingAdapter(new ArrayList<>(), this);

            Toast.makeText(this, "Setup RecyclerView 2: Setting layout manager", Toast.LENGTH_SHORT).show();
            bookingRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            Toast.makeText(this, "Setup RecyclerView 3: Setting adapter", Toast.LENGTH_SHORT).show();
            bookingRecyclerView.setAdapter(bookingAdapter);

            Toast.makeText(this, "Setup RecyclerView Complete", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "ERROR in setupRecyclerView: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void refreshData() {
        try {
            Toast.makeText(this, "Refresh 1: Getting token", Toast.LENGTH_SHORT).show();
            String token = sessionManager.getToken();
            if (token == null) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Refresh 2: Making API call", Toast.LENGTH_SHORT).show();
            RetrofitClient.getInstance()
                    .getApiService()
                    .getDriverByUserId("Bearer " + token, sessionManager.getEmail())
                    .enqueue(new Callback<AmbulanceDriver>() {
                        @Override
                        public void onResponse(Call<AmbulanceDriver> call, Response<AmbulanceDriver> response) {
                            Toast.makeText(Dashboard.this, "Refresh 3: Got driver ID response", Toast.LENGTH_SHORT).show();
                            if(response.isSuccessful() && response.body() != null) {
                                fetchBookings(response.body().getId(), token);
                            } else {
                                swipeRefreshLayout.setRefreshing(false);
                                Toast.makeText(Dashboard.this, "Error fetching driver ID", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<AmbulanceDriver> call, Throwable t) {
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(Dashboard.this, "Network Error in refresh", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "ERROR in refreshData: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(Dashboard.this, "Error fetching bookings", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Booking>> call, Throwable t) {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(Dashboard.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkDriverProfile() {
        showLoadingState();
        String token = sessionManager.getToken();
        if (token == null) {
            hideLoadingState();
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
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
                                refreshData(); // Initial data load
                            } else {
                                updateDriverStatus(false, null);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AmbulanceDriver>> call, Throwable t) {
                        hideLoadingState();
                        Toast.makeText(Dashboard.this, "Network error", Toast.LENGTH_SHORT).show();
                        updateDriverStatus(false, null);
                    }
                });
    }

    private void updateDriverStatus(boolean isRegistered, AmbulanceDriver driver) {
        if (isRegistered && driver != null) {
            registeredDriverLayout.setVisibility(View.VISIBLE);
            unregisteredDriverLayout.setVisibility(View.GONE);
            driverNameText.setText(driver.getFullName());
            vehicleTypeText.setText(driver.getLicenseType());
            plateNumberText.setText(driver.getVehicleRegistrationNumber());
        } else {
            registeredDriverLayout.setVisibility(View.GONE);
            unregisteredDriverLayout.setVisibility(View.VISIBLE);
        }
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
}