package com.emergency.api;


import com.emergency.model.JwtResponse;
import com.emergency.model.LoginRequest;
import com.emergency.model.MessageResponse;
import com.emergency.model.SignupRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/auth/signup")
    Call<MessageResponse> signup(@Body SignupRequest request);

    @POST("api/auth/login")
    Call<JwtResponse> login(@Body LoginRequest request);

}