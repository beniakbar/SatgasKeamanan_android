package com.satgaskeamanan.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DashboardStatsModel {
    @SerializedName("total_petugas")
    private int totalPetugas;

    @SerializedName("hadir_today")
    private int hadirToday;

    @SerializedName("belum_hadir")
    private int belumHadir;

    @SerializedName("laporan_baru")
    private int laporanBaru;

    // Data List
    @SerializedName("recent_presensi")
    private List<AdminPresensiModel> recentPresensi;

    @SerializedName("open_laporan")
    private List<AdminLaporanModel> openLaporan;

    public int getTotalPetugas() { return totalPetugas; }
    public int getHadirToday() { return hadirToday; }
    public int getBelumHadir() { return belumHadir; }
    public int getLaporanBaru() { return laporanBaru; }
    
    public List<AdminPresensiModel> getRecentPresensi() { return recentPresensi; }
    public List<AdminLaporanModel> getOpenLaporan() { return openLaporan; }
}
