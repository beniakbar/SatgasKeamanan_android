package com.satgaskeamanan.app.models;

import com.google.gson.annotations.SerializedName;

public class AlarmModel {
    @SerializedName("id")
    private int id;

    @SerializedName("petugas_name")
    private String petugasName;

    @SerializedName("category")
    private String category;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("status")
    private String status;

    @SerializedName("timestamp")
    private String timestamp;

    public int getId() { return id; }
    public String getPetugasName() { return petugasName; }
    public String getCategory() { return category; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getStatus() { return status; }
    public String getTimestamp() { return timestamp; }
}
