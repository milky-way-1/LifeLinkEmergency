package com.emergency;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.Manifest;

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
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int DEFAULT_ZOOM = 15;
    private static final long LOCATION_UPDATE_INTERVAL = 3000; // 3 seconds

    private GoogleMap mMap;
    private String bookingId;
    private Location pickupLocation;
    private Location dropLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Polyline currentRoute;
    private Marker pickupMarker;
    private Marker dropMarker;
    private Marker driverMarker;
    private boolean isNavigatingToPickup = true;

    // Views
    private MaterialButton actionButton;
    private TextView statusText;
    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initializeViews();
        getIntentData();
        setupMap();
        setupLocationUpdates();
    }

    private void initializeViews() {
        actionButton = findViewById(R.id.actionButton);
        statusText = findViewById(R.id.statusText);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        actionButton.setOnClickListener(v -> handleActionButtonClick());
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

        if (checkLocationPermission()) {
            setupMapUI();
            addMarkers();
            startLocationTracking();
        } else {
            requestLocationPermission();
        }
    }

    private void setupMapUI() {
        if (mMap != null) {
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            if (checkLocationPermission()) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    private void addMarkers() {
        if (mMap == null) return;

        // Add pickup marker
        pickupMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(pickupLocation.getLatitude(), pickupLocation.getLongitude()))
                .title("Pickup Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Add drop marker
        dropMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(dropLocation.getLatitude(), dropLocation.getLongitude()))
                .title("Drop Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Move camera to show both markers
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickupMarker.getPosition());
        builder.include(dropMarker.getPosition());
        LatLngBounds bounds = builder.build();

        int padding = 100;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
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
        if (!checkLocationPermission()) return;

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_UPDATE_INTERVAL);

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.e(TAG, "Error starting location updates", e);
        }
    }

    private void updateDriverLocation(android.location.Location location) {
        LatLng driverLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        // Update driver marker
        if (driverMarker == null) {
            driverMarker = mMap.addMarker(new MarkerOptions()
                    .position(driverLatLng)
                    .title("Your Location")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ambulance)));
        } else {
            driverMarker.setPosition(driverLatLng);
        }

        // Update route
        LatLng destination = isNavigatingToPickup ?
                new LatLng(pickupLocation.getLatitude(), pickupLocation.getLongitude()) :
                new LatLng(dropLocation.getLatitude(), dropLocation.getLongitude());

        fetchAndDrawRoute(driverLatLng, destination);

        // Update location in backend
        updateLocationInBackend(location.getLatitude(), location.getLongitude());
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
        String status = isNavigatingToPickup ?
                "Navigating to Pickup Location" :
                "Navigating to Drop Location";
        statusText.setText(status);

        actionButton.setText(isNavigatingToPickup ?
                "Start Journey to Drop Location" :
                "Complete Journey");
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
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
                setupMapUI();
                startLocationTracking();
            } else {
                showError("Location permission is required");
                finish();
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
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void fetchAndDrawRoute(LatLng origin, LatLng destination) {
        String url = getDirectionsUrl(origin, destination);
        new FetchDirectionsTask().execute(url);
    }

    private String getDirectionsUrl(LatLng origin, LatLng destination) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + destination.latitude + "," + destination.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String key = "key=" + MapUtils.getApiKey(this);

        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&" + key;
        String output = "json";

        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    private class FetchDirectionsTask extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(data);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            if (result == null) return;

            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);
            }

            if (lineOptions != null) {
                if (currentRoute != null) {
                    currentRoute.remove();
                }
                currentRoute = mMap.addPolyline(lineOptions);
            }
        }
    }

    private String downloadUrl(String strUrl) throws IOException, MalformedURLException {
        String data = "";
        HttpURLConnection urlConnection = null;
        URL url = new URL(strUrl);

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            InputStream iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();
            iStream.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return data;
    }
}