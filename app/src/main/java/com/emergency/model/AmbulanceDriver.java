package com.emergency.model;

import com.google.gson.annotations.SerializedName;

public class AmbulanceDriver {
    @SerializedName("id")
    private String id;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("dateOfBirth")
    private String dateOfBirth;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("currentAddress")
    private String currentAddress;

    @SerializedName("driversLicenseNumber")
    private String driversLicenseNumber;

    @SerializedName("licenseType")
    private String licenseType;

    @SerializedName("experienceWithEmergencyVehicle")
    private boolean experienceWithEmergencyVehicle;

    @SerializedName("yearsOfEmergencyExperience")
    private Integer yearsOfEmergencyExperience;

    @SerializedName("vehicleRegistrationNumber")
    private String vehicleRegistrationNumber;

    @SerializedName("hasAirConditioning")
    private boolean hasAirConditioning;

    @SerializedName("hasOxygenCylinderHolder")
    private boolean hasOxygenCylinderHolder;

    @SerializedName("hasStretcher")
    private boolean hasStretcher;

    @SerializedName("insurancePolicyNumber")
    private String insurancePolicyNumber;

    @SerializedName("insuranceExpiryDate")
    private String insuranceExpiryDate;

    @SerializedName("verificationStatus")
    private String verificationStatus;

    @SerializedName("verificationComment")
    private String verificationComment;

    // Default constructor
    public AmbulanceDriver() {
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public String getDriversLicenseNumber() {
        return driversLicenseNumber;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public boolean isExperienceWithEmergencyVehicle() {
        return experienceWithEmergencyVehicle;
    }

    public Integer getYearsOfEmergencyExperience() {
        return yearsOfEmergencyExperience;
    }

    public String getVehicleRegistrationNumber() {
        return vehicleRegistrationNumber;
    }

    public boolean isHasAirConditioning() {
        return hasAirConditioning;
    }

    public boolean isHasOxygenCylinderHolder() {
        return hasOxygenCylinderHolder;
    }

    public boolean isHasStretcher() {
        return hasStretcher;
    }

    public String getInsurancePolicyNumber() {
        return insurancePolicyNumber;
    }

    public String getInsuranceExpiryDate() {
        return insuranceExpiryDate;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public String getVerificationComment() {
        return verificationComment;
    }

    // Optional: toString method for debugging
    @Override
    public String toString() {
        return "AmbulanceDriver{" +
                "id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", currentAddress='" + currentAddress + '\'' +
                ", driversLicenseNumber='" + driversLicenseNumber + '\'' +
                ", licenseType='" + licenseType + '\'' +
                ", experienceWithEmergencyVehicle=" + experienceWithEmergencyVehicle +
                ", yearsOfEmergencyExperience=" + yearsOfEmergencyExperience +
                ", vehicleRegistrationNumber='" + vehicleRegistrationNumber + '\'' +
                ", hasAirConditioning=" + hasAirConditioning +
                ", hasOxygenCylinderHolder=" + hasOxygenCylinderHolder +
                ", hasStretcher=" + hasStretcher +
                ", insurancePolicyNumber='" + insurancePolicyNumber + '\'' +
                ", insuranceExpiryDate='" + insuranceExpiryDate + '\'' +
                ", verificationStatus='" + verificationStatus + '\'' +
                ", verificationComment='" + verificationComment + '\'' +
                '}';
    }
}
