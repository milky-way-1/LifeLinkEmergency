package com.emergency;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.emergency.api.RetrofitClient;
import com.emergency.model.AmbulanceDriverRegistrationDto;
import com.emergency.model.ApiResponse;
import com.emergency.model.MessageResponse;
import com.emergency.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        submitButton.setEnabled(false);
        submitButton.setText("Submitting...");

        // Get session manager
        SessionManager sessionManager = new SessionManager(this);

        // Convert license type
        String licenseType;
        String licenseTypeStr = licenseTypeDropdown.getText().toString();
        switch (licenseTypeStr) {
            case "Commercial Driver's License":
                licenseType = "COMMERCIAL_DRIVERS_LICENSE";
                break;
            case "Emergency Vehicle License":
                licenseType = "EMERGENCY_VEHICLE_LICENSE";
                break;
            case "Professional Driver's License":
                licenseType = "PROFESSIONAL_DRIVERS_LICENSE";
                break;
            default:
                showErrorDialog("Please select a license type");
                submitButton.setEnabled(true);
                submitButton.setText("Submit");
                return;
        }

        // Convert dates from dd/MM/yyyy to yyyy-MM-dd
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String dob = "", insuranceExpiry = "";
        try {
            Date date = inputFormat.parse(dobInput.getText().toString());
            dob = outputFormat.format(date);

            date = inputFormat.parse(insuranceExpiryInput.getText().toString());
            insuranceExpiry = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            showErrorDialog("Invalid date format");
            submitButton.setEnabled(true);
            submitButton.setText("Submit");
            return;
        }

        // Create registration DTO
        AmbulanceDriverRegistrationDto registration = new AmbulanceDriverRegistrationDto();
        registration.setFullName(fullNameInput.getText().toString().trim());
        registration.setDateOfBirth(dob);
        registration.setPhoneNumber(phoneInput.getText().toString().trim());
        registration.setCurrentAddress(addressInput.getText().toString().trim());
        registration.setDriversLicenseNumber(licenseNumberInput.getText().toString().trim());
        registration.setLicenseType(licenseType);
        registration.setExperienceWithEmergencyVehicle(emergencyExperienceSwitch.isChecked());

        if (emergencyExperienceSwitch.isChecked() && !emergencyExperienceInput.getText().toString().trim().isEmpty()) {
            registration.setYearsOfEmergencyExperience(
                    Integer.parseInt(emergencyExperienceInput.getText().toString().trim())
            );
        } else {
            registration.setYearsOfEmergencyExperience(0);
        }

        registration.setVehicleRegistrationNumber(vehicleRegInput.getText().toString().trim());
        registration.setHasAirConditioning(acCheckbox.isChecked());
        registration.setHasOxygenCylinderHolder(oxygenCheckbox.isChecked());
        registration.setHasStretcher(stretcherCheckbox.isChecked());
        registration.setInsurancePolicyNumber(insurancePolicyInput.getText().toString().trim());
        registration.setInsuranceExpiryDate(insuranceExpiry);

        // Make API call using RetrofitClient
        RetrofitClient.getInstance()
                .getApiService()
                .registerDriver("Bearer " + sessionManager.getToken(), registration)
                .enqueue(new Callback<ApiResponse<MessageResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<MessageResponse>> call,
                                           Response<ApiResponse<MessageResponse>> response) {
                        submitButton.setEnabled(true);
                        submitButton.setText("Submit");

                        if (response.isSuccessful()) {
                            ApiResponse<MessageResponse> apiResponse = response.body();
                            if (apiResponse != null && apiResponse.isSuccess()) {
                                showSuccessDialog();
                            } else {
                                String errorMessage = apiResponse != null ?
                                        apiResponse.getMessage() :
                                        "Registration failed";
                                showErrorDialog(errorMessage);
                            }
                        } else {
                            try {
                                // Try to parse error response
                                Gson gson = new Gson();
                                ApiResponse<?> errorResponse = gson.fromJson(
                                        response.errorBody().string(),
                                        ApiResponse.class
                                );
                                showErrorDialog(errorResponse.getMessage());
                            } catch (Exception e) {
                                showErrorDialog("Registration failed. Please try again.");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<MessageResponse>> call, Throwable t) {
                        submitButton.setEnabled(true);
                        submitButton.setText("Submit");
                        Log.e("API_ERROR", "Registration failed", t);
                        showErrorDialog("Network error. Please check your connection.");
                    }
                });
    }

    private void showErrorDialog(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Registration Failed")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
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