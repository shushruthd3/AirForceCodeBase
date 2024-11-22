package com.example.pocapp;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

//Interface to make or create the Retrofit API Calls.
public interface APIInterface {


    @POST()
    Call<User> createUser(@Url String url, @Body JsonObject user);
    @POST
    Call<RegisterUser>register(@Url String url,@Body JsonObject reg);


}
