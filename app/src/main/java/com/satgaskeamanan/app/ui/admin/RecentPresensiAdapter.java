package com.satgaskeamanan.app.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.satgaskeamanan.app.R;
import com.satgaskeamanan.app.models.AdminPresensiModel;

import java.util.List;

public class RecentPresensiAdapter extends RecyclerView.Adapter<RecentPresensiAdapter.ViewHolder> {

    private List<AdminPresensiModel> list;

    public RecentPresensiAdapter(List<AdminPresensiModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminPresensiModel item = list.get(position);
        
        // Menampilkan Nama dan Email jika nama kosong
        String name = (item.getPetugasName() != null && !item.getPetugasName().isEmpty()) ? item.getPetugasName() : item.getPetugasEmail();
        holder.text1.setText(name);
        
        // Menampilkan Waktu dan Lokasi
        String detail = item.getTimestamp() + " - " + (item.getLocationNote() != null ? item.getLocationNote() : "Lokasi tidak ada");
        holder.text2.setText(detail);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
