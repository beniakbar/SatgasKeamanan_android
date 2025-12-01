package com.satgaskeamanan.app.ui.admin;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.satgaskeamanan.app.R;
import com.satgaskeamanan.app.models.AdminLaporanModel;

import java.util.List;

public class OpenLaporanAdapter extends RecyclerView.Adapter<OpenLaporanAdapter.ViewHolder> {

    private List<AdminLaporanModel> list;

    public OpenLaporanAdapter(List<AdminLaporanModel> list) {
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
        AdminLaporanModel item = list.get(position);
        
        String title = item.getNote() != null ? item.getNote() : "Laporan Tanpa Judul";
        holder.text1.setText(title);
        holder.text1.setTextColor(Color.RED); // Merah untuk status Lapor
        
        String name = (item.getPetugasName() != null && !item.getPetugasName().isEmpty()) ? item.getPetugasName() : item.getPetugasEmail();
        String detail = "Oleh: " + name + "\n" + item.getTimestamp();
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
