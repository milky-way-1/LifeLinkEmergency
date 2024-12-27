package com.emergency;

public class EmergencyDTO {
    private String id;
    private String location;
    private String description;
    private String patientCondition;
    private double distance;
    private String timestamp;
    private double latitude;
    private double longitude;
    private String contactName;
    private String contactPhone;
    private String emergencyType;
    private int numberOfPatients;
    private boolean requiresSpecialEquipment;

    // Default Constructor
    public EmergencyDTO() {
    }

    // Constructor with essential fields
    public EmergencyDTO(String id, String location, String description, String patientCondition,
                        double distance, String timestamp) {
        this.id = id;
        this.location = location;
        this.description = description;
        this.patientCondition = patientCondition;
        this.distance = distance;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPatientCondition() {
        return patientCondition;
    }

    public void setPatientCondition(String patientCondition) {
        this.patientCondition = patientCondition;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getEmergencyType() {
        return emergencyType;
    }

    public void setEmergencyType(String emergencyType) {
        this.emergencyType = emergencyType;
    }

    public int getNumberOfPatients() {
        return numberOfPatients;
    }

    public void setNumberOfPatients(int numberOfPatients) {
        this.numberOfPatients = numberOfPatients;
    }

    public boolean isRequiresSpecialEquipment() {
        return requiresSpecialEquipment;
    }

    public void setRequiresSpecialEquipment(boolean requiresSpecialEquipment) {
        this.requiresSpecialEquipment = requiresSpecialEquipment;
    }

    @Override
    public String toString() {
        return "EmergencyDTO{" +
                "id='" + id + '\'' +
                ", location='" + location + '\'' +
                ", description='" + description + '\'' +
                ", patientCondition='" + patientCondition + '\'' +
                ", distance=" + distance +
                ", timestamp='" + timestamp + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", contactName='" + contactName + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", emergencyType='" + emergencyType + '\'' +
                ", numberOfPatients=" + numberOfPatients +
                ", requiresSpecialEquipment=" + requiresSpecialEquipment +
                '}';
    }
}
