package com.satgaskeamanan.app.models;

import com.google.gson.annotations.SerializedName;

public class AlarmRequest {
    @SerializedName("category")
    private String category;

    @SerializedName("latitude")
    private String latitude; // Changed to String for formatting

    @SerializedName("longitude")
    private String longitude; // Changed to String for formatting

    @SerializedName("description")
    private String description;

    public AlarmRequest(String category, String latitude, String longitude, String description) {
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }
}
