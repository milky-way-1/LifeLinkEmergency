package com.emergency;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class Login extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private TextView signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make status bar transparent
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_login);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signupLink = findViewById(R.id.signupLink);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> handleLogin());

        signupLink.setOnClickListener(v -> {
            // Navigate to SignupActivity
            Intent intent = new Intent(Login.this, Signup.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (validateInput(email, password)) {
            // Show loading state
            loginButton.setEnabled(false);
            loginButton.setText("Logging in...");

            // TODO: Implement actual login logic here
            new Handler().postDelayed(() -> {
                // For demo, navigate to Dashboard after delay
                Intent intent = new Intent(Login.this, Dashboard.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }, 2000);
        }
    }

    private boolean validateInput(String email, String password) {
        return true;
//        boolean isValid = true;
//
//        if (email.isEmpty()) {
//            emailInput.setError("Email is required");
//            isValid = false;
//        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            emailInput.setError("Please enter a valid email");
//            isValid = false;
//        }
//
//        if (password.isEmpty()) {
//            passwordInput.setError("Password is required");
//            isValid = false;
//        } else if (password.length() < 6) {
//            passwordInput.setError("Password must be at least 6 characters");
//            isValid = false;
//        }
//
//        return isValid;
    }
}