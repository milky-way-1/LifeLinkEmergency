package com.emergency.api;


import com.emergency.model.AmbulanceDriver;
import com.emergency.model.AmbulanceDriverRegistrationDto;
import com.emergency.model.ApiResponse;
import com.emergency.model.Booking;
import com.emergency.model.JwtResponse;
import com.emergency.model.Location;
import com.emergency.model.LocationUpdateDto;
import com.emergency.model.LoginRequest;
import com.emergency.model.MessageResponse;
import com.emergency.model.SignupRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @POST("api/auth/signup")
    Call<MessageResponse> signup(@Body SignupRequest request);

    @POST("api/auth/login")
    Call<JwtResponse> login(@Body LoginRequest request);

    @POST("/api/ambulance/register")
    Call<ApiResponse<MessageResponse>> registerDriver(
            @Header("Authorization") String token,
            @Body AmbulanceDriverRegistrationDto registrationDto
    );

    @GET("/api/ambulance/profile")
    Call<ApiResponse<AmbulanceDriver>> getDriverProfile(@Header("Authorization") String token);

    @PUT("api/ambulance/location")
    Call<Location> updateLocation(
            @Header("Authorization") String token,
            @Body LocationUpdateDto locationDto
    );

    @GET("api/bookings/{driverId}")
    Call<List<Booking>> getDriverBookings(@Header("Authorization") String token,
                                          @Path("driverId") String driverId);

    @GET("api/bookings/{id}")
    Call<Booking> getBookingDetails(
            @Header("Authorization") String token,
            @Path("id") String bookingId
    );


    @POST("api/bookings/{id}/complete")
    Call<Booking> completeBooking(
            @Header("Authorization") String token,
            @Path("id") String bookingId
    );
    @GET("api/ambulance/driver/{email}")
    Call<AmbulanceDriver> getDriverByUserId(
            @Header("Authorization") String token,
            @Path("email") String email
    );

}
