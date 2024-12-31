package com.emergency;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.emergency.api.ApiService;
import com.emergency.api.RetrofitClient;
import com.emergency.model.JwtResponse;
import com.emergency.model.LoginRequest;
import com.emergency.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private TextView signupLink;
    private TextView statusMessage;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToDashboard();
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();

        // Get API service instance
        apiService = RetrofitClient.getInstance().getApiService();
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signupLink = findViewById(R.id.signupLink);
        statusMessage = findViewById(R.id.statusMessage);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> handleLogin());

        signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Signup.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (validateInput(email, password)) {
            loginButton.setEnabled(false);
            showStatus("Logging in...", "#5C6BC0");

            LoginRequest loginRequest = new LoginRequest(email, password);

            apiService.login(loginRequest).enqueue(new Callback<JwtResponse>() {
                @Override
                public void onResponse(Call<JwtResponse> call, Response<JwtResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JwtResponse jwtResponse = response.body();

                        // Save auth details using SessionManager
                        sessionManager.saveAuthResponse(jwtResponse);

                        showStatus("Welcome back, " + jwtResponse.getName(), "#43A047");

                        new Handler().postDelayed(() -> {
                            statusMessage.setVisibility(View.GONE);
                            navigateToDashboard();
                        }, 1000);
                    } else {
                        loginButton.setEnabled(true);
                        try {
                            JSONObject errorBody = new JSONObject(response.errorBody().string());
                            String errorMessage = errorBody.optString("message", "Invalid credentials");
                            showStatus(errorMessage, "#B00020");
                        } catch (Exception e) {
                            showStatus("Login failed. Please try again.", "#B00020");
                        }
                    }
                }

                @Override
                public void onFailure(Call<JwtResponse> call, Throwable t) {
                    loginButton.setEnabled(true);
                    String errorMessage;

                    if (t instanceof SocketTimeoutException) {
                        errorMessage = "Request timed out";
                    } else if (t instanceof UnknownHostException) {
                        errorMessage = "No internet connection";
                    } else {
                        errorMessage = "Network error: " + t.getMessage();
                        Log.e("Login", "Error: ", t);
                    }

                    showStatus(errorMessage, "#B00020");
                }
            });
        }
    }

    private void showStatus(String message, String color) {
        runOnUiThread(() -> {
            statusMessage.setVisibility(View.VISIBLE);
            statusMessage.setText(message);
            statusMessage.setTextColor(Color.parseColor(color));
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            showStatus("Please enter your email", "#B00020");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showStatus("Please enter a valid email", "#B00020");
            return false;
        }
        if (password.isEmpty()) {
            showStatus("Please enter your password", "#B00020");
            return false;
        }
        return true;
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(Login.this, Dashboard.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}