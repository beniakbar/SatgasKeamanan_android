package com.satgaskeamanan.app.models;

import com.google.gson.annotations.SerializedName;

public class UserModel {

    @SerializedName("id")
    private int id;

    @SerializedName("email")
    private String email;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("profile_picture")
    private String profilePicture;

    // Ambil status peran dari Django API
    @SerializedName("is_admin")
    private boolean isAdmin;

    @SerializedName("is_petugas")
    private boolean isPetugas;

    // =========================
    // Getter
    // =========================

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public boolean isPetugas() {
        return isPetugas;
    }

    // =========================
    // Setter (Optional, but good practice)
    // =========================
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
