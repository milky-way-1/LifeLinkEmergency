package com.emergency.model;

public class LocationUpdateDto {
    double latitude;
    double longitude;

    public LocationUpdateDto(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLongitude() {return this.longitude;}

    public double getLatitude() {return this.latitude;}
}
