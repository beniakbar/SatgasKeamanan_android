package com.satgaskeamanan.app.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.satgaskeamanan.app.LoginActivity;
import com.satgaskeamanan.app.R;
import com.satgaskeamanan.app.api.APIClient;
import com.satgaskeamanan.app.api.APIService;
import com.satgaskeamanan.app.models.DashboardStatsModel;
import com.satgaskeamanan.app.ui.admin.laporanharian.HarianPresensiFragment;
import com.satgaskeamanan.app.ui.admin.laporanlist.LaporanListFragment;
import com.satgaskeamanan.app.ui.admin.petugaslist.PetugasListFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardFragment extends Fragment {

    private TextView tvTotalPetugas, tvHadirToday, tvBelumHadir, tvLaporanBaru, tvActiveAlarmCount;
    private CardView cvTotalPetugas, cvHadirToday, cvBelumHadir, cvLaporanBaru, cvMonitoringAlarm;
    private RecyclerView rvRecentPresensi, rvOpenLaporan;
    private ProgressBar progressBar;
    private Button btnRefresh, btnLogout;
    private APIService apiService;

    public AdminDashboardFragment() {
        super(R.layout.fragment_admin_dashboard);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        tvTotalPetugas = view.findViewById(R.id.tv_total_petugas);
        tvHadirToday = view.findViewById(R.id.tv_hadir_today);
        tvBelumHadir = view.findViewById(R.id.tv_belum_hadir);
        tvLaporanBaru = view.findViewById(R.id.tv_laporan_baru);
        tvActiveAlarmCount = view.findViewById(R.id.tv_active_alarm_count);
        
        cvTotalPetugas = view.findViewById(R.id.cv_total_petugas);
        cvHadirToday = view.findViewById(R.id.cv_hadir_today);
        cvBelumHadir = view.findViewById(R.id.cv_belum_hadir);
        cvLaporanBaru = view.findViewById(R.id.cv_laporan_baru);
        cvMonitoringAlarm = view.findViewById(R.id.cv_monitoring_alarm);
        
        rvRecentPresensi = view.findViewById(R.id.rv_recent_presensi);
        rvOpenLaporan = view.findViewById(R.id.rv_open_laporan);
        
        progressBar = view.findViewById(R.id.pb_dashboard);
        btnRefresh = view.findViewById(R.id.btn_refresh_dashboard);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Setup RecyclerView
        rvRecentPresensi.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOpenLaporan.setLayoutManager(new LinearLayoutManager(getContext()));

        apiService = APIClient.getAPIService(requireContext());

        // --- SET CLICK LISTENERS FOR CARDS ---
        cvTotalPetugas.setOnClickListener(v -> navigateTo(new PetugasListFragment()));
        cvHadirToday.setOnClickListener(v -> navigateTo(new HarianPresensiFragment()));
        cvBelumHadir.setOnClickListener(v -> navigateTo(new HarianPresensiFragment()));
        cvLaporanBaru.setOnClickListener(v -> navigateTo(new LaporanListFragment()));
        
        // Listener Monitoring Alarm
        cvMonitoringAlarm.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AdminAlarmActivity.class));
        });

        btnRefresh.setOnClickListener(v -> loadDashboardStats());
        btnLogout.setOnClickListener(v -> performLogout());

        loadDashboardStats();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadDashboardStats(); // Auto refresh saat kembali dari activity lain
    }

    private void performLogout() {
        SharedPreferences settings = requireContext().getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(requireContext(), "Logout Berhasil.", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void navigateTo(Fragment fragment) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }

    private void loadDashboardStats() {
        progressBar.setVisibility(View.VISIBLE);
        
        apiService.getDashboardStats().enqueue(new Callback<DashboardStatsModel>() {
            @Override
            public void onResponse(@NonNull Call<DashboardStatsModel> call, @NonNull Response<DashboardStatsModel> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    DashboardStatsModel stats = response.body();
                    
                    tvTotalPetugas.setText(String.valueOf(stats.getTotalPetugas()));
                    tvHadirToday.setText(String.valueOf(stats.getHadirToday()));
                    tvBelumHadir.setText(String.valueOf(stats.getBelumHadir()));
                    tvLaporanBaru.setText(String.valueOf(stats.getLaporanBaru()));
                    
                    // Update Alarm Count
                    tvActiveAlarmCount.setText(stats.getActiveAlarms() + " Alarm Aktif");
                    
                    if (stats.getRecentPresensi() != null) {
                        rvRecentPresensi.setAdapter(new RecentPresensiAdapter(stats.getRecentPresensi()));
                    }
                    
                    if (stats.getOpenLaporan() != null) {
                        rvOpenLaporan.setAdapter(new OpenLaporanAdapter(stats.getOpenLaporan()));
                    }

                } else {
                    Toast.makeText(getContext(), "Gagal memuat data: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardStatsModel> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
