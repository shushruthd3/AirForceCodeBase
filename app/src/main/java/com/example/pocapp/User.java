package com.example.pocapp;

import com.google.gson.annotations.SerializedName;
//Call to send Validate API data
public class User {
    @SerializedName("androidId")
    public String android_Id;

    public User(String android_Id) {

        this.android_Id = android_Id;


    }

    @Override
    public String toString() {
        return "User{" +
                "android_Id='" + android_Id + '\'' +
                '}';
    }
}
