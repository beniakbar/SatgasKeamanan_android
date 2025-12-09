package com.satgaskeamanan.app.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.satgaskeamanan.app.R;
import com.satgaskeamanan.app.api.APIClient;
import com.satgaskeamanan.app.api.APIService;
import com.satgaskeamanan.app.models.AlarmModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAlarmActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmptyState;
    private AdminAlarmAdapter adapter;
    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_alarm);

        // Setup Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Monitoring Alarm Aktif");
        }

        // Init Views
        recyclerView = findViewById(R.id.rv_active_alarms);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        tvEmptyState = findViewById(R.id.tv_empty_state);

        // Init API & Adapter
        apiService = APIClient.getAPIService(this);
        adapter = new AdminAlarmAdapter();
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load Data
        loadActiveAlarms();

        // Listeners
        swipeRefresh.setOnRefreshListener(this::loadActiveAlarms);

        adapter.setListener(alarmId -> markAlarmAsHandled(alarmId));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadActiveAlarms() {
        swipeRefresh.setRefreshing(true);
        apiService.getActiveAlarms("active").enqueue(new Callback<List<AlarmModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<AlarmModel>> call, @NonNull Response<List<AlarmModel>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<AlarmModel> alarms = response.body();
                    adapter.setAlarmList(alarms);
                    
                    if (alarms.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(AdminAlarmActivity.this, "Gagal memuat data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AlarmModel>> call, @NonNull Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(AdminAlarmActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markAlarmAsHandled(int alarmId) {
        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("status", "handled");

        apiService.updateAlarmStatus(alarmId, statusUpdate).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminAlarmActivity.this, "Alarm berhasil ditandai SELESAI.", Toast.LENGTH_SHORT).show();
                    loadActiveAlarms(); // Refresh list
                } else {
                    Toast.makeText(AdminAlarmActivity.this, "Gagal update status.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(AdminAlarmActivity.this, "Koneksi gagal.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
