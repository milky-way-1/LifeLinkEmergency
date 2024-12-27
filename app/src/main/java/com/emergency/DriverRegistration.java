package com.emergency;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DriverRegistration extends AppCompatActivity {
    private TextInputEditText fullNameInput;
    private TextInputEditText dobInput;
    private TextInputEditText phoneInput;
    private TextInputEditText addressInput;
    private TextInputEditText licenseNumberInput;
    private AutoCompleteTextView licenseTypeDropdown;
    private TextInputEditText licenseExpiryInput;
    private TextInputEditText experienceInput;
    private SwitchMaterial emergencyExperienceSwitch;
    private TextInputLayout emergencyExperienceLayout;
    private TextInputEditText emergencyExperienceInput;
    private TextInputEditText vehicleRegInput;
    private MaterialCheckBox acCheckbox;
    private MaterialCheckBox oxygenCheckbox;
    private MaterialCheckBox stretcherCheckbox;
    private TextInputEditText insurancePolicyInput;
    private TextInputEditText insuranceExpiryInput;
    private MaterialButton submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_registration);

        initializeViews();
        setupToolbar();
        setupDatePickers();
        setupLicenseTypeDropdown();
        setupEmergencyExperienceSwitch();
        setupSubmitButton();
    }

    private void initializeViews() {
        fullNameInput = findViewById(R.id.fullNameInput);
        dobInput = findViewById(R.id.dobInput);
        phoneInput = findViewById(R.id.phoneInput);
        addressInput = findViewById(R.id.addressInput);
        licenseNumberInput = findViewById(R.id.licenseNumberInput);
        licenseTypeDropdown = findViewById(R.id.licenseTypeDropdown);
        licenseExpiryInput = findViewById(R.id.licenseExpiryInput);
        experienceInput = findViewById(R.id.experienceInput);
        emergencyExperienceSwitch = findViewById(R.id.emergencyExperienceSwitch);
        emergencyExperienceLayout = findViewById(R.id.emergencyExperienceLayout);
        emergencyExperienceInput = findViewById(R.id.emergencyExperienceInput);
        vehicleRegInput = findViewById(R.id.vehicleRegInput);
        acCheckbox = findViewById(R.id.acCheckbox);
        oxygenCheckbox = findViewById(R.id.oxygenCheckbox);
        stretcherCheckbox = findViewById(R.id.stretcherCheckbox);
        insurancePolicyInput = findViewById(R.id.insurancePolicyInput);
        insuranceExpiryInput = findViewById(R.id.insuranceExpiryInput);
        submitButton = findViewById(R.id.submitButton);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupDatePickers() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        // DOB Picker
        dobInput.setOnClickListener(v -> {
            datePicker.show(getSupportFragmentManager(), "DOB_PICKER");
            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                dobInput.setText(sdf.format(new Date(selection)));
            });
        });

        // License Expiry Picker
        licenseExpiryInput.setOnClickListener(v -> {
            datePicker.show(getSupportFragmentManager(), "LICENSE_EXPIRY_PICKER");
            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                licenseExpiryInput.setText(sdf.format(new Date(selection)));
            });
        });

        // Insurance Expiry Picker
        insuranceExpiryInput.setOnClickListener(v -> {
            datePicker.show(getSupportFragmentManager(), "INSURANCE_EXPIRY_PICKER");
            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                insuranceExpiryInput.setText(sdf.format(new Date(selection)));
            });
        });
    }

    private void setupLicenseTypeDropdown() {
        String[] licenseTypes = new String[]{
                "Commercial Driver's License",
                "Emergency Vehicle License",
                "Professional Driver's License"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.dropdown_item,
                licenseTypes
        ) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(getResources().getColor(R.color.black));
                return view;
            }
        };
        licenseTypeDropdown.setAdapter(adapter);

        // Set the selected item text color to white
        licenseTypeDropdown.setTextColor(Color.WHITE);

        // Optional: Change the background color of the dropdown popup
        licenseTypeDropdown.setDropDownBackgroundResource(R.color.white);
        licenseTypeDropdown.setAdapter(adapter);
    }

    private void setupEmergencyExperienceSwitch() {
        emergencyExperienceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            emergencyExperienceLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                emergencyExperienceInput.setText("");
            }
        });
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {
            if (validateForm()) {
                submitRegistration();
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate Personal Information
        if (fullNameInput.getText().toString().trim().isEmpty()) {
            fullNameInput.setError("Name is required");
            isValid = false;
        }

        if (dobInput.getText().toString().trim().isEmpty()) {
            dobInput.setError("Date of Birth is required");
            isValid = false;
        }

        String phone = phoneInput.getText().toString().trim();
        if (phone.isEmpty() || phone.length() < 10) {
            phoneInput.setError("Valid phone number is required");
            isValid = false;
        }

        if (addressInput.getText().toString().trim().isEmpty()) {
            addressInput.setError("Address is required");
            isValid = false;
        }

        // Validate Professional Information
        if (licenseNumberInput.getText().toString().trim().isEmpty()) {
            licenseNumberInput.setError("License number is required");
            isValid = false;
        }

        if (licenseTypeDropdown.getText().toString().trim().isEmpty()) {
            licenseTypeDropdown.setError("License type is required");
            isValid = false;
        }

        if (licenseExpiryInput.getText().toString().trim().isEmpty()) {
            licenseExpiryInput.setError("License expiry date is required");
            isValid = false;
        }

        if (experienceInput.getText().toString().trim().isEmpty()) {
            experienceInput.setError("Years of experience is required");
            isValid = false;
        }

        // Validate Vehicle Information
        if (vehicleRegInput.getText().toString().trim().isEmpty()) {
            vehicleRegInput.setError("Vehicle registration number is required");
            isValid = false;
        }

        if (insurancePolicyInput.getText().toString().trim().isEmpty()) {
            insurancePolicyInput.setError("Insurance policy number is required");
            isValid = false;
        }

        if (insuranceExpiryInput.getText().toString().trim().isEmpty()) {
            insuranceExpiryInput.setError("Insurance expiry date is required");
            isValid = false;
        }

        return isValid;
    }

    private void submitRegistration() {
        // Show loading state
        submitButton.setEnabled(false);
        submitButton.setText("Submitting...");

        // Create registration data object
        DriverRegistrationDTO registration = new DriverRegistrationDTO(
                fullNameInput.getText().toString(),
                dobInput.getText().toString(),
                phoneInput.getText().toString(),
                addressInput.getText().toString(),
                licenseNumberInput.getText().toString(),
                licenseTypeDropdown.getText().toString(),
                licenseExpiryInput.getText().toString(),
                Integer.parseInt(experienceInput.getText().toString()),
                emergencyExperienceSwitch.isChecked(),
                emergencyExperienceSwitch.isChecked() ?
                        Integer.parseInt(emergencyExperienceInput.getText().toString()) : 0,
                vehicleRegInput.getText().toString(),
                acCheckbox.isChecked(),
                oxygenCheckbox.isChecked(),
                stretcherCheckbox.isChecked(),
                insurancePolicyInput.getText().toString(),
                insuranceExpiryInput.getText().toString()
        );

        // TODO: Send registration to backend
        new Handler().postDelayed(() -> {
            // Simulate API call
            runOnUiThread(() -> {
                showSuccessDialog();
            });
        }, 2000);
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Registration Successful")
                .setMessage("Your registration has been submitted successfully. We will review your information and get back to you soon.")
                .setPositiveButton("OK", (dialog, which) -> {
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}