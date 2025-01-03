package com.emergency;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.emergency.api.RetrofitClient;
import com.emergency.model.ApiResponse;
import com.emergency.model.Booking;
import com.emergency.model.BookingStatus;
import com.emergency.model.Location;
import com.emergency.model.LocationUpdateDto;
import com.emergency.util.DirectionsJSONParser;
import com.emergency.util.MapUtils;
import com.emergency.util.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.Manifest;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final float NAVIGATION_ZOOM = 18f;
    private static final float OVERVIEW_ZOOM = 15f;
    private static final float TILT_LEVEL = 45f;
    private static final int LOCATION_UPDATE_INTERVAL = 3000; // 3 seconds
    private static final int FASTEST_UPDATE_INTERVAL = 2000; // 2 seconds

    // Map and Location related
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private android.location.Location lastKnownLocation;

    // Markers and Route
    private Marker pickupMarker;
    private Marker dropMarker;
    private Marker driverMarker;
    private Polyline currentRoute;

    // Location Data
    private Location pickupLocation;
    private Location dropLocation;
    private String bookingId;

    // UI Elements
    private TextView statusText;
    private TextView addressText;
    private TextView navigationInstructions;
    private TextView distanceText;
    private ProgressBar loadingIndicator;
    private View navigationPanel;
    private MaterialButton actionButton;

    // State Management
    private boolean isNavigating = false;
    private boolean isNavigatingToPickup = true;
    private boolean isCompassMode = false;

    // Geocoding
    private Geocoder geocoder;

    // Handler for UI updates
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    // Optional: For address caching
    private String pickupAddress;
    private String dropAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        // Get and validate intent data first
        if (!validateAndGetIntentData()) {

            finish();
            return;
        }

        // Initialize views
        initializeViews();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);

        } else {

        }
    }

    private void initializeViews() {
        // Find views
        statusText = findViewById(R.id.statusText);
        addressText = findViewById(R.id.addressText);
        navigationInstructions = findViewById(R.id.navigationInstructions);
        distanceText = findViewById(R.id.distanceText);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        navigationPanel = findViewById(R.id.navigationPanel);

        // Setup buttons
        MaterialButton navigateButton = findViewById(R.id.navigateButton);
        MaterialButton compassButton = findViewById(R.id.compassButton);
        MaterialButton actionButton = findViewById(R.id.actionButton);
        FloatingActionButton recenterButton = findViewById(R.id.recenterButton);

        // Set click listeners
        navigateButton.setOnClickListener(v -> {
            if (!isNavigating) {
                startNavigation();
                navigateButton.setText("Stop Navigation");
            } else {
                stopNavigation();
                navigateButton.setText("Start Navigation");
            }
        });

        compassButton.setOnClickListener(v -> {
            if (isNavigating) {
                toggleCompassMode();
            } else {

            }
        });

        actionButton.setOnClickListener(v -> handleActionButtonClick());

        recenterButton.setOnClickListener(v -> {
            if (driverMarker != null) {
                updateNavigationView(lastKnownLocation);
            }
        });
    }


    private void startNavigation() {
        try {


            if (!checkLocationPermission()) {

                requestLocationPermission();
                return;
            }

            isNavigating = true;
            isCompassMode = false;

            // Update UI first
            updateUI();


            // Setup location updates if not already done
            if (fusedLocationClient == null) {
                setupLocationTracking();
            }

            // Create location request
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(3000)
                    .setFastestInterval(2000);

            // Create location callback if null
            if (locationCallback == null) {
                locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        for (android.location.Location location : locationResult.getLocations()) {
                            updateDriverLocation(location);
                        }
                    }
                };
            }

            // Request location updates
            try {
                fusedLocationClient.requestLocationUpdates(locationRequest,
                        locationCallback,
                        Looper.getMainLooper());

            } catch (SecurityException e) {

            }

        } catch (Exception e) {

            isNavigating = false;
            updateUI();
        }
    }


    private void stopNavigation() {
        isNavigating = false;
        isCompassMode = false;


        // Reset camera to show all markers
        showBothLocations();
    }



    private void getIntentData() {
        Intent intent = getIntent();
        bookingId = intent.getStringExtra("booking_id");
        pickupLocation = new Location(
                intent.getDoubleExtra("pickup_lat", 0),
                intent.getDoubleExtra("pickup_lng", 0)
        );
        dropLocation = new Location(
                intent.getDoubleExtra("drop_lat", 0),
                intent.getDoubleExtra("drop_lng", 0)
        );

        updateUI();
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        try {
            // Basic UI setup first
            setupMapUI();


            // Move camera to a default position first
            try {
                LatLng defaultLocation = new LatLng(pickupLocation.getLatitude(), pickupLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f));

            } catch (Exception e) {

            }

            // Add markers with delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    addMarkersToMap();
                } catch (Exception e) {

                }
            }, 1000);

        } catch (Exception e) {

        }
    }

    private void toggleCompassMode() {
        isCompassMode = !isCompassMode;
        if (isCompassMode) {
            // Switch to compass (north-up) mode
            CameraPosition position = new CameraPosition.Builder()
                    .target(driverMarker.getPosition())
                    .zoom(NAVIGATION_ZOOM)
                    .tilt(0f)
                    .bearing(0f)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        } else {
            // Switch back to navigation mode
            if (driverMarker != null && isNavigating) {
                updateNavigationView(lastKnownLocation);
            }
        }

    }

    private void updateNavigationView(android.location.Location location) {
        if (mMap == null || location == null) return;

        try {
            LatLng driverLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(driverLatLng)
                    .zoom(NAVIGATION_ZOOM)
                    .bearing(location.getBearing())
                    .tilt(TILT_LEVEL)
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } catch (Exception e) {

        }
    }

    private void addMarkers() {
        try {
            // Add pickup marker
            LatLng pickupLatLng = new LatLng(pickupLocation.getLatitude(), pickupLocation.getLongitude());
            pickupMarker = mMap.addMarker(new MarkerOptions()
                    .position(pickupLatLng)
                    .title("Pickup Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));


            // Add drop marker
            LatLng dropLatLng = new LatLng(dropLocation.getLatitude(), dropLocation.getLongitude());
            dropMarker = mMap.addMarker(new MarkerOptions()
                    .position(dropLatLng)
                    .title("Drop Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));


            // Show both markers
            showBothLocations();

            fetchAndDrawRoute(pickupLatLng, dropLatLng);

        } catch (Exception e) {

        }
    }
    private void showBothLocations() {
        try {
            if (pickupMarker == null || dropMarker == null) {
                return;
            }

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(pickupMarker.getPosition());
            builder.include(dropMarker.getPosition());
            final LatLngBounds bounds = builder.build();

            // Get the width and height of the screen
            View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
            if (mapView.getWidth() > 0) {
                // If view is already laid out
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

            } else {
                // Wait for layout
                mapView.post(() -> {
                    try {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

                    } catch (Exception e) {

                    }
                });
            }
        } catch (Exception e) {

        }
    }

    private void addMarkersAndAdjustCamera() {
        try {
            // Add pickup marker
            LatLng pickupLatLng = new LatLng(pickupLocation.getLatitude(), pickupLocation.getLongitude());
            pickupMarker = mMap.addMarker(new MarkerOptions()
                    .position(pickupLatLng)
                    .title("Pickup Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
           ;

            // Add drop marker
            LatLng dropLatLng = new LatLng(dropLocation.getLatitude(), dropLocation.getLongitude());
            dropMarker = mMap.addMarker(new MarkerOptions()
                    .position(dropLatLng)
                    .title("Drop Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));


            // Calculate bounds
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(pickupLatLng);
            builder.include(dropLatLng);
            final LatLngBounds bounds = builder.build();

            // Get map view width and height
            View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
            if (mapView.getWidth() > 0) {
                // If view is already laid out, move camera immediately
                moveCameraToShowMarkers(bounds);
            } else {
                // Wait for layout
                mapView.post(() -> moveCameraToShowMarkers(bounds));
            }

        } catch (Exception e) {

        }
    }

    private void moveCameraToShowMarkers(LatLngBounds bounds) {
        try {
            int padding = 100;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {

                    // Start location tracking after camera is set
                    if (checkLocationPermission()) {
                        try {
                            mMap.setMyLocationEnabled(true);
                            startLocationTracking();
                        } catch (SecurityException e) {

                        }
                    } else {
                        requestLocationPermission();
                    }
                }

                @Override
                public void onCancel() {

                }
            });
        } catch (Exception e) {

            // Fallback to simple camera move
            try {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 12f));
            } catch (Exception e2) {

            }
        }
    }

    private void addMarkersDelayed() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                // Try adding just one marker first
                LatLng pickup = new LatLng(pickupLocation.getLatitude(), pickupLocation.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(pickup)
                        .title("Pickup"));


                // Wait a bit before adding second marker
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        LatLng drop = new LatLng(dropLocation.getLatitude(), dropLocation.getLongitude());
                        mMap.addMarker(new MarkerOptions()
                                .position(drop)
                                .title("Drop"));

                    } catch (Exception e) {

                    }
                }, 1000);

            } catch (Exception e) {

            }
        }, 1000);
    }
    private void addMarkersSimple() {
        try {
            // Add pickup marker
            LatLng pickup = new LatLng(pickupLocation.getLatitude(), pickupLocation.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(pickup)
                    .title("Pickup")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));


            // Add drop marker
            LatLng drop = new LatLng(dropLocation.getLatitude(), dropLocation.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(drop)
                    .title("Drop")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));


            // Simple camera update
            showBothLocations(pickup, drop);


        } catch (Exception e) {

        }
    }
    private void showBothLocations(LatLng pickup, LatLng drop) {
        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(pickup);
            builder.include(drop);

            int padding = 200; // Larger padding to ensure markers are visible
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                    builder.build(), padding
            ));
        } catch (Exception e) {

            // Fallback to showing just pickup location
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickup, 12f));
        }
    }

    private boolean isValidLatLng(double lat, double lng) {
        return lat != 0 && lng != 0 &&
                lat >= -90 && lat <= 90 &&
                lng >= -180 && lng <= 180;
    }

    private void moveCameraToShowMarkers(LatLng pickup, LatLng drop) {
        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(pickup);
            builder.include(drop);
            final LatLngBounds bounds = builder.build();

            // Get the map's view width and height
            View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
            if (mapView == null) {

                return;
            }

            if (mapView.getWidth() == 0) {
                // Wait for layout if width is zero
                mapView.post(() -> moveCamera(bounds));
            } else {
                moveCamera(bounds);
            }
        } catch (Exception e) {

        }
    }

    private void moveCamera(LatLngBounds bounds) {
        try {
            int padding = 100;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);

        } catch (Exception e) {

            // Fallback to a default location or zoom level
            try {
                LatLng center = bounds.getCenter();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 12f));

            } catch (Exception e2) {

            }
        }
    }

    private boolean validateAndGetIntentData() {
        try {
            Intent intent = getIntent();
            if (intent == null) {

                return false;
            }

            // Get and validate coordinates
            double pickupLat = intent.getDoubleExtra("pickup_lat", 0.0);
            double pickupLng = intent.getDoubleExtra("pickup_lng", 0.0);
            double dropLat = intent.getDoubleExtra("drop_lat", 0.0);
            double dropLng = intent.getDoubleExtra("drop_lng", 0.0);



            // Validate coordinates
            if (!isValidCoordinate(pickupLat, pickupLng) ||
                    !isValidCoordinate(dropLat, dropLng)) {

                return false;
            }

            // Create location objects
            pickupLocation = new Location(pickupLat, pickupLng);
            dropLocation = new Location(dropLat, dropLng);

            // Get booking ID
            bookingId = intent.getStringExtra("booking_id");
            if (bookingId == null || bookingId.isEmpty()) {

                return false;
            }

            return true;
        } catch (Exception e) {

            return false;
        }
    }
    private boolean isValidCoordinate(double lat, double lng) {
        return lat != 0.0 && lng != 0.0 &&
                lat >= -90 && lat <= 90 &&
                lng >= -180 && lng <= 180;
    }


    private boolean validateIntentData() {
        Intent intent = getIntent();
        if (intent == null) {

            return false;
        }

        bookingId = intent.getStringExtra("booking_id");
        double pickupLat = intent.getDoubleExtra("pickup_lat", 0);
        double pickupLng = intent.getDoubleExtra("pickup_lng", 0);
        double dropLat = intent.getDoubleExtra("drop_lat", 0);
        double dropLng = intent.getDoubleExtra("drop_lng", 0);



        if (bookingId == null || pickupLat == 0 || pickupLng == 0 || dropLat == 0 || dropLng == 0) {

            return false;
        }

        pickupLocation = new Location(pickupLat, pickupLng);
        dropLocation = new Location(dropLat, dropLng);
        return true;
    }


    private void fetchAndDrawRoute(LatLng origin, LatLng destination) {
        String url = getDirectionsUrl(origin, destination);
        new FetchDirectionsTask().execute(url);
    }

    private void setupLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                android.location.Location location = locationResult.getLastLocation();
                updateDriverLocation(location);
                checkDestinationReached(location);
            }
        };
    }

    private void startLocationTracking() {
        if (!checkLocationPermission()) {

            return;
        }

        try {
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(LOCATION_UPDATE_INTERVAL);

            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.getMainLooper());

        } catch (SecurityException e) {

        } catch (Exception e) {

        }
    }

    private void updateDriverLocation(android.location.Location location) {
        if (location == null) return;

        try {
            LatLng driverLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            lastKnownLocation = location;

            // Update driver marker
            if (driverMarker == null) {
                MarkerOptions driverOptions = new MarkerOptions()
                        .position(driverLatLng)
                        .title("Your Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        .anchor(0.5f, 0.5f)
                        .flat(true);
                driverMarker = mMap.addMarker(driverOptions);
            } else {
                driverMarker.setPosition(driverLatLng);
                driverMarker.setRotation(location.getBearing());
            }

            // Update route if navigating
            if (isNavigating) {
                LatLng destination = isNavigatingToPickup ?
                        pickupMarker.getPosition() :
                        dropMarker.getPosition();
                fetchAndDrawRoute(driverLatLng, destination);
            }

            // Update camera if in navigation mode
            if (isNavigating && !isCompassMode) {
                updateNavigationView(location);
            }

            updateDistanceInfo(location);

        } catch (Exception e) {

        }
    }


    private void updateLocationInBackend(double latitude, double longitude) {
        LocationUpdateDto locationDto = new LocationUpdateDto(latitude, longitude);
        RetrofitClient.getInstance()
                .getApiService()
                .updateLocation("Bearer " + new SessionManager(this).getToken(), locationDto)
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

    private void checkDestinationReached(android.location.Location currentLocation) {
        Location targetLocation = isNavigatingToPickup ? pickupLocation : dropLocation;
        float[] results = new float[1];
        android.location.Location.distanceBetween(
                currentLocation.getLatitude(), currentLocation.getLongitude(),
                targetLocation.getLatitude(), targetLocation.getLongitude(),
                results
        );

        if (results[0] < 50) { // Within 50 meters
            if (isNavigatingToPickup) {
                showPickupReachedDialog();
            } else {
                showDestinationReachedDialog();
            }
        }
    }

    private void handleActionButtonClick() {
        if (isNavigatingToPickup) {
            isNavigatingToPickup = false;
            if (isNavigating) {
                // Restart navigation to new destination
                startNavigation();
            }
            updateUI();
        } else {
            completeBooking();
        }
    }


    private void completeBooking() {
        loadingIndicator.setVisibility(View.VISIBLE);
        actionButton.setEnabled(false);

        RetrofitClient.getInstance()
                .getApiService()
                .completeBooking("Bearer " + new SessionManager(this).getToken(), bookingId)
                .enqueue(new Callback<Booking>() {
                    @Override
                    public void onResponse(Call<Booking> call, Response<Booking> response) {
                        loadingIndicator.setVisibility(View.GONE);
                        actionButton.setEnabled(true);

                        if (response.isSuccessful()) {
                            finish();
                        } else {
                            showError("Failed to complete booking");
                        }
                    }

                    @Override
                    public void onFailure(Call<Booking> call, Throwable t) {
                        loadingIndicator.setVisibility(View.GONE);
                        actionButton.setEnabled(true);
                        showError("Network error while completing booking");
                    }
                });
    }

    private void updateUI() {
        try {
            // Update navigation button text
            MaterialButton navigateButton = findViewById(R.id.navigateButton);
            navigateButton.setText(isNavigating ? "Stop Navigation" : "Start Navigation");

            // Update status text
            TextView statusText = findViewById(R.id.statusText);
            String status = isNavigating ?
                    (isNavigatingToPickup ? "Navigating to Pickup" : "Navigating to Drop") :
                    "Navigation Stopped";
            statusText.setText(status);

            // Show/hide navigation panel
            View navigationPanel = findViewById(R.id.navigationPanel);
            navigationPanel.setVisibility(isNavigating ? View.VISIBLE : View.GONE);

        } catch (Exception e) {

        }
    }

    private void showPickupReachedDialog() {
        if (!isNavigatingToPickup) return;

        new MaterialAlertDialogBuilder(this)
                .setTitle("Pickup Location Reached")
                .setMessage("You have reached the pickup location. Start journey to drop location?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    isNavigatingToPickup = false;
                    updateUI();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showDestinationReachedDialog() {
        if (isNavigatingToPickup) return;

        new MaterialAlertDialogBuilder(this)
                .setTitle("Destination Reached")
                .setMessage("You have reached the destination. Complete the journey?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    completeBooking();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private boolean checkLocationPermission() {
        boolean hasPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        return hasPermission;
    }

    private void showDetailedError(String title, String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setNegativeButton("Try Again", (dialog, which) -> {
                    // Retry logic
                    if (mMap != null) {
                        setupMapUI();
                        addMarkers();
                        startLocationTracking();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void requestLocationPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                setupLocationTracking();
            } else {

                // Show dialog explaining why location is needed
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs location permission to navigate you to the destination")
                        .setPositiveButton("OK", (dialog, which) -> requestLocationPermission())
                        .setNegativeButton("Cancel", (dialog, which) -> finish())
                        .create()
                        .show();
            }
        }
    }

    private void showError(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up markers
        if (pickupMarker != null) pickupMarker.remove();
        if (dropMarker != null) dropMarker.remove();
        if (driverMarker != null) driverMarker.remove();
        if (currentRoute != null) currentRoute.remove();
    }
    private void addDropMarker() {
        if (mMap == null || dropLocation == null) {

            return;
        }

        try {
            double lat = dropLocation.getLatitude();
            double lng = dropLocation.getLongitude();


            LatLng dropLatLng = new LatLng(lat, lng);


            dropMarker = mMap.addMarker(new MarkerOptions()
                    .position(dropLatLng)
                    .title("Drop Location"));


        } catch (Exception e) {

        }
    }

    private void addPickupMarker() {
        if (mMap == null || pickupLocation == null) return;

        LatLng pickupLatLng = new LatLng(pickupLocation.getLatitude(), pickupLocation.getLongitude());
        MarkerOptions pickupOptions = new MarkerOptions()
                .position(pickupLatLng)
                .title("Pickup Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        pickupMarker = mMap.addMarker(pickupOptions);
    }

    private void setupMapUI() {
        if (mMap == null) return;

        try {
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setRotateGesturesEnabled(true);
            mMap.getUiSettings().setTiltGesturesEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            if (checkLocationPermission()) {
                mMap.setMyLocationEnabled(true);
            }

        } catch (Exception e) {

        }
    }




    private class FetchDirectionsTask extends AsyncTask<String, Void, List<LatLng>> {
        @Override
        protected List<LatLng> doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
                return parseDirections(data);
            } catch (Exception e) {

                return null;
            }
        }

        @Override
        protected void onPostExecute(List<LatLng> result) {
            if (result != null && !result.isEmpty()) {
                drawRoute(result);
            }
        }
    }
    private List<LatLng> parseDirections(String jsonData) {
        List<LatLng> points = new ArrayList<>();
        try {
            JSONObject jObject = new JSONObject(jsonData);
            JSONArray routes = jObject.getJSONArray("routes");

            for (int i = 0; i < routes.length(); i++) {
                JSONObject route = routes.getJSONObject(i);
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String encodedPath = overviewPolyline.getString("points");
                points.addAll(decodePoly(encodedPath));
            }
        } catch (Exception e) {

        }
        return points;
    }
    private void drawRoute(List<LatLng> points) {
        try {
            // Remove existing polyline
            if (currentRoute != null) {
                currentRoute.remove();
            }

            PolylineOptions lineOptions = new PolylineOptions()
                    .addAll(points)
                    .width(12)
                    .color(Color.BLUE)
                    .geodesic(true);

            currentRoute = mMap.addPolyline(lineOptions);


        } catch (Exception e) {

        }
    }



    private String getDirectionsUrl(LatLng origin, LatLng destination) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + destination.latitude + "," + destination.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&key=" + MapUtils.getApiKey(this);

        // Output format
        String output = "json";

        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            double latitude = lat * 1e-5;
            double longitude = lng * 1e-5;
            poly.add(new LatLng(latitude, longitude));
        }
        return poly;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        HttpURLConnection urlConnection = null;
        BufferedReader br = null;

        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

        } finally {
            if (br != null) {
                br.close();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return data;
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) return "";

        StringBuilder data = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }
        }
        return data.toString();
    }
    private void showDebug(String message) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
        }
        Log.d(TAG, message);
    }

    private void addMarkersToMap() {

        try {
            // Remove existing markers if any
            clearExistingMarkers();

            // Add pickup marker
            LatLng pickupLatLng = new LatLng(pickupLocation.getLatitude(), pickupLocation.getLongitude());
            MarkerOptions pickupOptions = new MarkerOptions()
                    .position(pickupLatLng)
                    .title("Pickup Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            pickupMarker = mMap.addMarker(pickupOptions);

            // Add drop marker
            LatLng dropLatLng = new LatLng(dropLocation.getLatitude(), dropLocation.getLongitude());
            MarkerOptions dropOptions = new MarkerOptions()
                    .position(dropLatLng)
                    .title("Drop Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            dropMarker = mMap.addMarker(dropOptions);

            // Show both markers
            showBothLocations();

        } catch (Exception e) {

        }
    }
    private void clearExistingMarkers() {
        if (pickupMarker != null) {
            pickupMarker.remove();
            pickupMarker = null;
        }
        if (dropMarker != null) {
            dropMarker.remove();
            dropMarker = null;
        }
        if (driverMarker != null) {
            driverMarker.remove();
            driverMarker = null;
        }
    }

    private void setupLocationTracking() {
        try {


            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            if (checkLocationPermission()) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                updateDriverLocation(location);

                            } else {

                            }
                        })
                        .addOnFailureListener(e ->
                                e.printStackTrace());
            } else {

            }

        } catch (Exception e) {

        }
    }

    private BitmapDescriptor getBitmapDescriptor(@DrawableRes int vectorDrawableResourceId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorDrawableResourceId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    private void updateDistanceInfo(android.location.Location driverLocation) {
        try {
            LatLng destination = isNavigatingToPickup ?
                    pickupMarker.getPosition() :
                    dropMarker.getPosition();

            float[] results = new float[1];
            android.location.Location.distanceBetween(
                    driverLocation.getLatitude(), driverLocation.getLongitude(),
                    destination.latitude, destination.longitude,
                    results
            );

            String distanceText = results[0] > 1000 ?
                    String.format("%.1f km", results[0] / 1000) :
                    String.format("%d m", (int) results[0]);

            TextView distanceTextView = findViewById(R.id.distanceText);
            distanceTextView.setText("Distance: " + distanceText);

        } catch (Exception e) {
        }
    }
}