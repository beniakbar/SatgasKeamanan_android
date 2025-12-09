package com.satgaskeamanan.app.api;

import com.satgaskeamanan.app.models.TokenResponse;
import com.satgaskeamanan.app.models.PetugasModel;
import com.satgaskeamanan.app.models.AdminLaporanModel;
import com.satgaskeamanan.app.models.HarianPresensiReportModel;
import com.satgaskeamanan.app.models.DashboardStatsModel; 
import com.satgaskeamanan.app.models.UserModel;
import com.satgaskeamanan.app.models.LoginRequest;
import com.satgaskeamanan.app.models.RegisterRequest;
import com.satgaskeamanan.app.models.RegisterResponse;
import com.satgaskeamanan.app.models.AlarmRequest;
import com.satgaskeamanan.app.models.AlarmModel;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.Part;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIService {

    // ---------------------------
    // AUTENTIKASI
    // ---------------------------
    @POST("token/")
    Call<TokenResponse> login(@Body LoginRequest loginRequest);

    @FormUrlEncoded
    @POST("token/refresh/")
    Call<TokenResponse> refreshToken(
            @Field("refresh") String refreshToken
    );

    @Headers("No-Authentication: true")
    @POST("user/register/")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    // ---------------------------
    // PROFIL PENGGUNA
    // ---------------------------
    @GET("user/profile/")
    Call<UserModel> getUserProfile();

    @Multipart
    @PATCH("user/profile/")
    Call<UserModel> updateProfile(
            @Part("first_name") RequestBody firstName,
            @Part("last_name") RequestBody lastName,
            @Part("phone_number") RequestBody phoneNumber,
            @Part MultipartBody.Part profilePicture 
    );


    // ---------------------------
    // PRESENSI - PETUGAS
    // ---------------------------
    @Multipart
    @POST("presensi/")
    Call<Void> kirimPresensi(
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("location_note") RequestBody locationNote,
            @Part("note") RequestBody note,
            @Part MultipartBody.Part selfiePhoto
    );


    // ---------------------------
    // LAPORAN - PETUGAS
    // ---------------------------
    @Multipart
    @POST("laporan/")
    Call<Void> kirimLaporan(
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("location_note") RequestBody locationNote,
            @Part("note") RequestBody note,
            @Part MultipartBody.Part photo
    );

    // ---------------------------
    // EMERGENCY ALARM - PETUGAS
    // ---------------------------
    @POST("alarm/")
    Call<Void> triggerAlarm(@Body AlarmRequest request);
    
    // Get Active Alarms (Untuk Polling Petugas & Admin)
    @GET("alarm/")
    Call<List<AlarmModel>> getActiveAlarms(@Query("status") String status);

    // ---------------------------
    // ADMIN
    // ---------------------------
    
    // Update Status Alarm (Mark as Handled)
    @PATCH("alarm/{id}/")
    Call<Void> updateAlarmStatus(
            @Path("id") int alarmId,
            @Body Map<String, String> statusUpdate
    );

    @GET("admin/petugas/")
    Call<List<PetugasModel>> getDaftarPetugas();

    @GET("admin/laporan/")
    Call<List<AdminLaporanModel>> getDaftarLaporan();

    @PATCH("admin/laporan/{id}/")
    Call<AdminLaporanModel> updateLaporanStatus(
            @Path("id") int laporanId,
            @Body Map<String, String> statusUpdate
    );

    @GET("admin/laporan/harian/")
    Call<HarianPresensiReportModel> getHarianPresensiReport(@Query("date") String date);

    @GET("admin/dashboard/stats/")
    Call<DashboardStatsModel> getDashboardStats();
}
