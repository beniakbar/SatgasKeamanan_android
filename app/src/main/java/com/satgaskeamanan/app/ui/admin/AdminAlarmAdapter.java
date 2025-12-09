package com.satgaskeamanan.app.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.satgaskeamanan.app.R;
import com.satgaskeamanan.app.models.AlarmModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminAlarmAdapter extends RecyclerView.Adapter<AdminAlarmAdapter.ViewHolder> {

    private List<AlarmModel> alarmList = new ArrayList<>();
    private OnAlarmActionListener listener;

    public interface OnAlarmActionListener {
        void onMarkHandled(int alarmId);
    }

    public void setListener(OnAlarmActionListener listener) {
        this.listener = listener;
    }

    public void setAlarmList(List<AlarmModel> list) {
        this.alarmList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_alarm, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlarmModel alarm = alarmList.get(position);
        
        holder.tvCategory.setText(alarm.getCategory().toUpperCase());
        holder.tvPetugas.setText("Pemicu: " + alarm.getPetugasName());
        holder.tvTime.setText(alarm.getTimestamp().substring(11, 16)); // Ambil jam HH:MM sederhana
        holder.tvLocation.setText(String.format(Locale.US, "Lokasi: %.6f, %.6f", alarm.getLatitude(), alarm.getLongitude()));

        holder.btnHandled.setOnClickListener(v -> {
            if (listener != null) listener.onMarkHandled(alarm.getId());
        });
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvPetugas, tvTime, tvLocation;
        Button btnHandled;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_alarm_category);
            tvPetugas = itemView.findViewById(R.id.tv_alarm_petugas);
            tvTime = itemView.findViewById(R.id.tv_alarm_time);
            tvLocation = itemView.findViewById(R.id.tv_alarm_location);
            btnHandled = itemView.findViewById(R.id.btn_mark_handled);
        }
    }
}
