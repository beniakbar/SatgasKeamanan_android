package com.satgaskeamanan.app.ui.admin.laporanharian;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.satgaskeamanan.app.R;
import com.satgaskeamanan.app.models.AdminPresensiModel;
import com.satgaskeamanan.app.models.PetugasStatusPresensiModel;

import java.util.List;

public class HarianPresensiAdapter extends RecyclerView.Adapter<HarianPresensiAdapter.ViewHolder> {

    private final List<PetugasStatusPresensiModel> petugasList;
    private final Context context;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(PetugasStatusPresensiModel petugas);
    }

    public HarianPresensiAdapter(Context context, List<PetugasStatusPresensiModel> petugasList, OnItemClickListener listener) {
        this.context = context;
        this.petugasList = petugasList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_harian_presensi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PetugasStatusPresensiModel petugas = petugasList.get(position);
        AdminPresensiModel presensi = petugas.getLastPresensi();

        holder.tvNama.setText(petugas.getFullName());

        // Logika Status Validasi
        if (petugas.isHasPresensiToday() && presensi != null) {
            String statusValidasi = presensi.getStatusValidasi();
            
            if (statusValidasi == null || "hadir".equalsIgnoreCase(statusValidasi)) {
                // HADIR (Valid)
                holder.tvStatus.setText("HADIR");
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Hijau
            } else if ("diluar_lokasi".equalsIgnoreCase(statusValidasi)) {
                // DILUAR LOKASI
                holder.tvStatus.setText("DILUAR LOKASI");
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800")); // Oranye
            } else {
                // TIDAK HADIR (Dibatalkan/Tidak Valid)
                holder.tvStatus.setText("TIDAK HADIR (INVALID)");
                holder.tvStatus.setTextColor(Color.RED);
            }

            holder.tvWaktu.setText("Waktu: " + presensi.getTimestamp());
            
            // Tetap bisa diklik untuk lihat detail/validasi
            holder.itemView.setOnClickListener(v -> listener.onItemClick(petugas));
            holder.itemView.setClickable(true);
        } else {
            // Belum Presensi
            holder.tvStatus.setText("TIDAK HADIR");
            holder.tvStatus.setTextColor(Color.RED);
            holder.tvWaktu.setText("Belum melakukan presensi.");
            
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);
        }
    }

    @Override
    public int getItemCount() {
        return petugasList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvStatus, tvWaktu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tv_petugas_nama);
            tvStatus = itemView.findViewById(R.id.tv_status_hadir);
            tvWaktu = itemView.findViewById(R.id.tv_presensi_waktu);
        }
    }
}
