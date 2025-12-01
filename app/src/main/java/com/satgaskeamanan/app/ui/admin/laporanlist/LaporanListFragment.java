package com.satgaskeamanan.app.ui.admin.laporanlist;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.satgaskeamanan.app.R;
import com.satgaskeamanan.app.api.APIClient;
import com.satgaskeamanan.app.api.APIService;

import com.satgaskeamanan.app.models.AdminLaporanModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LaporanListFragment extends Fragment implements OnStatusChangeListener {

    private RecyclerView recyclerView;
    private AdminLaporanAdapter adapter;
    private APIService apiService;
    private List<AdminLaporanModel> laporanList;

    public LaporanListFragment() {
        super(R.layout.fragment_laporan_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = APIClient.getAPIService(requireContext());

        recyclerView = view.findViewById(R.id.rv_laporan_admin);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchLaporanList();
    }

    private void fetchLaporanList() {
        apiService.getDaftarLaporan().enqueue(new Callback<List<AdminLaporanModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<AdminLaporanModel>> call, @NonNull Response<List<AdminLaporanModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    laporanList = response.body();
                    adapter = new AdminLaporanAdapter(laporanList, LaporanListFragment.this, laporan -> {
                        showLaporanDetailDialog(laporan);
                    });
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat daftar laporan.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AdminLaporanModel>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error Jaringan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLaporanDetailDialog(AdminLaporanModel laporan) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_detail_laporan, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        ImageView ivFoto = dialogView.findViewById(R.id.iv_detail_laporan_foto);
        TextView tvJudul = dialogView.findViewById(R.id.tv_detail_laporan_judul);
        TextView tvPetugas = dialogView.findViewById(R.id.tv_detail_laporan_petugas);
        TextView tvStatus = dialogView.findViewById(R.id.tv_detail_laporan_status);
        TextView tvWaktu = dialogView.findViewById(R.id.tv_detail_laporan_waktu);
        TextView tvLokasi = dialogView.findViewById(R.id.tv_detail_laporan_lokasi);
        TextView tvLokasiNote = dialogView.findViewById(R.id.tv_detail_laporan_lokasi_note);
        Button btnClose = dialogView.findViewById(R.id.btn_close_laporan_dialog);

        tvJudul.setText(laporan.getNote() != null ? laporan.getNote() : "Laporan Tanpa Judul");
        tvPetugas.setText("Oleh: " + laporan.getPetugasName());
        
        String status = laporan.getStatus() != null ? laporan.getStatus().toUpperCase() : "-";
        tvStatus.setText(status);

        // Update Warna Background Status di Dialog
        if ("LAPOR".equals(status)) {
            tvStatus.setBackgroundColor(Color.parseColor("#F44336")); // Merah
            tvStatus.setTextColor(Color.WHITE);
        } else if ("DITANGGAPI".equals(status)) {
            tvStatus.setBackgroundColor(Color.parseColor("#FF9800")); // Oranye
            tvStatus.setTextColor(Color.BLACK);
        } else if ("SELESAI".equals(status)) {
            tvStatus.setBackgroundColor(Color.parseColor("#4CAF50")); // Hijau
            tvStatus.setTextColor(Color.WHITE);
        }

        tvWaktu.setText(laporan.getTimestamp());
        tvLokasi.setText(laporan.getLatitude() + ", " + laporan.getLongitude());
        
        String locNote = laporan.getLocationNote();
        tvLokasiNote.setText((locNote != null && !locNote.isEmpty()) ? locNote : "Tidak ada catatan lokasi");

        String photoUrl = laporan.getPhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(ivFoto);
        } else {
            ivFoto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onStatusUpdateClicked(int laporanId, String newStatus) {
        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("status", newStatus);

        apiService.updateLaporanStatus(laporanId, statusUpdate).enqueue(new Callback<AdminLaporanModel>() {
            @Override
            public void onResponse(@NonNull Call<AdminLaporanModel> call, @NonNull Response<AdminLaporanModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateLaporan(response.body());
                    Toast.makeText(requireContext(), "Status Laporan ID " + laporanId + " update: " + newStatus.toUpperCase(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Gagal update status: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AdminLaporanModel> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error jaringan saat update status.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
