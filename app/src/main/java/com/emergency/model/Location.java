package com.emergency.model;

public class Location {
    private String type;
    private double[] coordinates; // [longitude, latitude]

    public Location() {
        this.type = "Point";
    }

    public Location(double latitude, double longitude) {
        this.type = "Point";
        this.coordinates = new double[]{longitude, latitude};
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public double getLatitude() {
        return coordinates != null && coordinates.length > 1 ? coordinates[1] : 0.0;
    }

    public double getLongitude() {
        return coordinates != null && coordinates.length > 0 ? coordinates[0] : 0.0;
    }

    public boolean isValid() {
        return coordinates != null && coordinates.length == 2;
    }

    @Override
    public String toString() {
        return "Location{" +
                "type='" + type + '\'' +
                ", latitude=" + getLatitude() +
                ", longitude=" + getLongitude() +
                '}';
    }
}
