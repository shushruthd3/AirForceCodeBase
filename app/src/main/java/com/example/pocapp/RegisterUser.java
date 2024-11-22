package com.example.pocapp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

//Class for Register User/Device.
public class RegisterUser  {
    @SerializedName("androidId")
    String mAndroidId;
    @SerializedName("software_ver")
    Integer mSoftwareVersion;
    @SerializedName("fingerprint")
    String mFingerPrint;

    @Override
    public String toString() {
        return "RegisterUser{" +
                "mAndroidId='" + mAndroidId + '\'' +
                ", mSoftwareVersion='" + mSoftwareVersion + '\'' +
                ", mFingerPrint='" + mFingerPrint + '\'' +
                '}';
    }
}
