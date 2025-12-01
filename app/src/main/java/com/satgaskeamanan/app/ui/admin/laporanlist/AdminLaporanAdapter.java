package com.satgaskeamanan.app.ui.admin.laporanlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.satgaskeamanan.app.R;
import com.satgaskeamanan.app.models.AdminLaporanModel;

import java.util.List;

public class AdminLaporanAdapter extends RecyclerView.Adapter<AdminLaporanAdapter.LaporanViewHolder> {

    private List<AdminLaporanModel> laporanList;
    private OnStatusChangeListener statusListener;
    private OnItemClickListener itemClickListener;

    // Interface untuk menangani klik item (detail)
    public interface OnItemClickListener {
        void onItemClick(AdminLaporanModel laporan);
    }

    public AdminLaporanAdapter(List<AdminLaporanModel> laporanList, OnStatusChangeListener statusListener, OnItemClickListener itemClickListener) {
        this.laporanList = laporanList;
        this.statusListener = statusListener;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public LaporanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_laporan_admin, parent, false);
        return new LaporanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LaporanViewHolder holder, int position) {
        AdminLaporanModel laporan = laporanList.get(position);
        String status = laporan.getStatus();

        holder.tvPetugasName.setText(laporan.getPetugasName());
        holder.tvNote.setText(laporan.getNote());
        
        // Menampilkan Status (UpperCase)
        holder.tvStatus.setText("Status: " + (status != null ? status.toUpperCase() : "-"));

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(laporan);
            }
        });

        // LOGIKA UPDATE STATUS (Workflow: LAPOR -> DITANGGAPI -> SELESAI)
        if ("lapor".equalsIgnoreCase(status)) {
            holder.btnAksi.setVisibility(View.VISIBLE);
            holder.btnAksi.setText("Tanggapi");
            holder.btnAksi.setOnClickListener(v -> {
                if (statusListener != null) statusListener.onStatusUpdateClicked(laporan.getId(), "ditanggapi");
            });
        } 
        else if ("ditanggapi".equalsIgnoreCase(status)) {
            holder.btnAksi.setVisibility(View.VISIBLE);
            holder.btnAksi.setText("Selesai");
            holder.btnAksi.setOnClickListener(v -> {
                if (statusListener != null) statusListener.onStatusUpdateClicked(laporan.getId(), "selesai");
            });
        } 
        else {
            // Jika sudah 'selesai' atau status tidak dikenal
            holder.btnAksi.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return laporanList.size();
    }

    public void updateLaporan(AdminLaporanModel updatedLaporan) {
        for (int i = 0; i < laporanList.size(); i++) {
            if (laporanList.get(i).getId() == updatedLaporan.getId()) {
                laporanList.set(i, updatedLaporan);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public static class LaporanViewHolder extends RecyclerView.ViewHolder {
        TextView tvPetugasName, tvNote, tvStatus;
        Button btnAksi;

        public LaporanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPetugasName = itemView.findViewById(R.id.tv_petugas_name);
            tvNote = itemView.findViewById(R.id.tv_laporan_note);
            tvStatus = itemView.findViewById(R.id.tv_laporan_status);
            btnAksi = itemView.findViewById(R.id.btn_aksi_status);
        }
    }
}
