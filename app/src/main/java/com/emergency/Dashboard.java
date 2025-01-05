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


            setContentView(R.layout.activity_dashboard);


            sessionManager = new SessionManager(this);

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


            driverNameText = findViewById(R.id.driverNameText);
            vehicleTypeText = findViewById(R.id.vehicleTypeText);
            plateNumberText = findViewById(R.id.plateNumberText);
            registerButton = findViewById(R.id.registerButton);


            if (registerButton != null) {
                registerButton.setOnClickListener(v -> {
                    Intent intent = new Intent(this, DriverRegistration.class);
                    startActivity(intent);
                });
            }


            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setOnRefreshListener(this::refreshData);
                swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright);
            }



        } catch (Exception e) {

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