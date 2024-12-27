package com.emergency;

public class DriverRegistrationDTO {
    private String fullName;
    private String dateOfBirth;
    private String phoneNumber;
    private String address;
    private String licenseNumber;
    private String licenseType;
    private String licenseExpiry;
    private int drivingExperience;
    private boolean hasEmergencyExperience;
    private int emergencyExperienceYears;
    private String vehicleRegistration;
    private boolean hasAC;
    private boolean hasOxygenHolder;
    private boolean hasStretcher;
    private String insurancePolicy;
    private String insuranceExpiry;

    // Constructor
    public DriverRegistrationDTO(String fullName, String dateOfBirth, String phoneNumber,
                                 String address, String licenseNumber, String licenseType,
                                 String licenseExpiry, int drivingExperience,
                                 boolean hasEmergencyExperience, int emergencyExperienceYears,
                                 String vehicleRegistration, boolean hasAC,
                                 boolean hasOxygenHolder, boolean hasStretcher,
                                 String insurancePolicy, String insuranceExpiry) {
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.licenseNumber = licenseNumber;
        this.licenseType = licenseType;
        this.licenseExpiry = licenseExpiry;
        this.drivingExperience = drivingExperience;
        this.hasEmergencyExperience = hasEmergencyExperience;
        this.emergencyExperienceYears = emergencyExperienceYears;
        this.vehicleRegistration = vehicleRegistration;
        this.hasAC = hasAC;
        this.hasOxygenHolder = hasOxygenHolder;
        this.hasStretcher = hasStretcher;
        this.insurancePolicy = insurancePolicy;
        this.insuranceExpiry = insuranceExpiry;
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public String getLicenseExpiry() {
        return licenseExpiry;
    }

    public void setLicenseExpiry(String licenseExpiry) {
        this.licenseExpiry = licenseExpiry;
    }

    public int getDrivingExperience() {
        return drivingExperience;
    }

    public void setDrivingExperience(int drivingExperience) {
        this.drivingExperience = drivingExperience;
    }

    public boolean isHasEmergencyExperience() {
        return hasEmergencyExperience;
    }

    public void setHasEmergencyExperience(boolean hasEmergencyExperience) {
        this.hasEmergencyExperience = hasEmergencyExperience;
    }

    public int getEmergencyExperienceYears() {
        return emergencyExperienceYears;
    }

    public void setEmergencyExperienceYears(int emergencyExperienceYears) {
        this.emergencyExperienceYears = emergencyExperienceYears;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
    }

    public boolean isHasAC() {
        return hasAC;
    }

    public void setHasAC(boolean hasAC) {
        this.hasAC = hasAC;
    }

    public boolean isHasOxygenHolder() {
        return hasOxygenHolder;
    }

    public void setHasOxygenHolder(boolean hasOxygenHolder) {
        this.hasOxygenHolder = hasOxygenHolder;
    }

    public boolean isHasStretcher() {
        return hasStretcher;
    }

    public void setHasStretcher(boolean hasStretcher) {
        this.hasStretcher = hasStretcher;
    }

    public String getInsurancePolicy() {
        return insurancePolicy;
    }

    public void setInsurancePolicy(String insurancePolicy) {
        this.insurancePolicy = insurancePolicy;
    }

    public String getInsuranceExpiry() {
        return insuranceExpiry;
    }

    public void setInsuranceExpiry(String insuranceExpiry) {
        this.insuranceExpiry = insuranceExpiry;
    }

    @Override
    public String toString() {
        return "DriverRegistrationDTO{" +
                "fullName='" + fullName + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", licenseType='" + licenseType + '\'' +
                ", licenseExpiry='" + licenseExpiry + '\'' +
                ", drivingExperience=" + drivingExperience +
                ", hasEmergencyExperience=" + hasEmergencyExperience +
                ", emergencyExperienceYears=" + emergencyExperienceYears +
                ", vehicleRegistration='" + vehicleRegistration + '\'' +
                ", hasAC=" + hasAC +
                ", hasOxygenHolder=" + hasOxygenHolder +
                ", hasStretcher=" + hasStretcher +
                ", insurancePolicy='" + insurancePolicy + '\'' +
                ", insuranceExpiry='" + insuranceExpiry + '\'' +
                '}';
    }
}
