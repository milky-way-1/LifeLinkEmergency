package com.emergency;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
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
import com.emergency.model.MessageResponse;
import com.emergency.model.SignupRequest;
import com.emergency.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Signup extends AppCompatActivity {

    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton signupButton;
    private TextView loginLink;
    private ImageButton backButton;
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
        setContentView(R.layout.activity_signup);

        initializeViews();
        setupClickListeners();

        apiService = RetrofitClient.getInstance().getApiService();
        sessionManager = new SessionManager(this);
    }

    private void initializeViews() {
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        signupButton = findViewById(R.id.signupButton);
        loginLink = findViewById(R.id.loginLink);
        backButton = findViewById(R.id.backButton);
        statusMessage = findViewById(R.id.statusMessage);
    }

    private void setupClickListeners() {
        signupButton.setOnClickListener(v -> handleSignup());

        loginLink.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void handleSignup() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (validateInput(name, email, password)) {
            signupButton.setEnabled(false);
            showStatus("Creating account...", "#5C6BC0");

            SignupRequest signupRequest = new SignupRequest(name, email, password, "AMBULANCE_DRIVER");

            apiService.signup(signupRequest).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        showStatus("Account created successfully!", "#43A047");

                        loginUser(email, password);
                    } else {
                        signupButton.setEnabled(true);
                        try {
                            JSONObject errorBody = new JSONObject(response.errorBody().string());
                            String errorMessage = errorBody.optString("message", "Signup failed");
                            showStatus(errorMessage, "#B00020");
                        } catch (Exception e) {
                            showStatus("Failed to create account. Please try again.", "#B00020");
                        }
                    }
                }

                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {
                    signupButton.setEnabled(true);
                    String errorMessage;

                    if (t instanceof SocketTimeoutException) {
                        errorMessage = "Request timed out";
                    } else if (t instanceof UnknownHostException) {
                        errorMessage = "No internet connection";
                    } else {
                        errorMessage = "Network error: " + t.getMessage();
                        Log.e("Signup", "Error: ", t);
                    }

                    showStatus(errorMessage, "#B00020");
                }
            });
        }
    }

    private void loginUser(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);

        apiService.login(loginRequest).enqueue(new Callback<JwtResponse>() {
            @Override
            public void onResponse(Call<JwtResponse> call, Response<JwtResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JwtResponse jwtResponse = response.body();
                    sessionManager.saveAuthResponse(jwtResponse);

                    Intent intent = new Intent(Signup.this, Dashboard.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else {
                    showStatus("Account created but login failed. Please login manually.", "#B00020");
                    new Handler().postDelayed(() -> {
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }, 2000);
                }
            }

            @Override
            public void onFailure(Call<JwtResponse> call, Throwable t) {
                showStatus("Account created but login failed. Please login manually.", "#B00020");
                new Handler().postDelayed(() -> {
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                }, 2000);
            }
        });
    }

    private void showStatus(String message, String color) {
        runOnUiThread(() -> {
            statusMessage.setVisibility(View.VISIBLE);
            statusMessage.setText(message);
            statusMessage.setTextColor(Color.parseColor(color));
        });
    }

    private boolean validateInput(String name, String email, String password) {
        boolean isValid = true;

        if (name.isEmpty()) {
            showStatus("Name is required", "#B00020");
            isValid = false;
        }

        if (email.isEmpty()) {
            showStatus("Email is required", "#B00020");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showStatus("Please enter a valid email", "#B00020");
            isValid = false;
        }

        if (password.isEmpty()) {
            showStatus("Password is required", "#B00020");
            isValid = false;
        } else if (password.length() < 6) {
            showStatus("Password must be at least 6 characters", "#B00020");
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}