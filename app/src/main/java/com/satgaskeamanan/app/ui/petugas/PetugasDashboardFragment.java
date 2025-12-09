package com.satgaskeamanan.app.ui.petugas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.satgaskeamanan.app.LoginActivity;
import com.satgaskeamanan.app.LaporanActivity;
import com.satgaskeamanan.app.PresensiActivity;
import com.satgaskeamanan.app.R;
import com.satgaskeamanan.app.api.APIClient;
import com.satgaskeamanan.app.api.APIService;
import com.satgaskeamanan.app.models.AlarmModel;
import com.satgaskeamanan.app.models.AlarmRequest;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class PetugasDashboardFragment extends Fragment {

    // UI Views
    private CardView cardPresensi, cardLaporan, cardProfile, cardPanic, cardAlarmBanner;
    private TextView tvWelcome, tvAlarmDetail, tvAlarmTime;
    private FrameLayout mapContainer;
    private Button btnLogout, btnStopSiren;

    // Logic
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable panicRunnable;
    private boolean isPanicTriggered = false;

    // Polling & Siren
    private Runnable pollingRunnable;
    private MediaPlayer mediaPlayer;
    private boolean isSirenPlaying = false;
    private boolean isSirenMuted = false;
    private int currentAlarmId = -1;
    private AlarmModel currentAlarmData;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private APIService apiService;

    private static final String TAG = "PetugasDashboard";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_petugas_dashboard, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = APIClient.getAPIService(requireContext());
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialize Views
        cardPresensi = view.findViewById(R.id.card_presensi);
        cardLaporan = view.findViewById(R.id.card_laporan);
        cardProfile = view.findViewById(R.id.card_profile);
        cardPanic = view.findViewById(R.id.card_panic);
        cardAlarmBanner = view.findViewById(R.id.card_alarm_banner);
        tvAlarmDetail = view.findViewById(R.id.tv_alarm_detail);
        tvAlarmTime = view.findViewById(R.id.tv_alarm_time);
        mapContainer = view.findViewById(R.id.map_container);
        btnStopSiren = view.findViewById(R.id.btn_stop_siren);
        btnLogout = view.findViewById(R.id.btn_logout_petugas);
        tvWelcome = view.findViewById(R.id.tv_welcome_petugas);

        // Listeners
        setupNavigationListeners();
        setupPanicButton();
        setupAlarmBannerListener();

        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                checkActiveAlarms();
                handler.postDelayed(this, 5000);
            }
        };
    }

    private void setupNavigationListeners() {
        cardPresensi.setOnClickListener(v -> startActivity(new Intent(getActivity(), PresensiActivity.class)));
        cardLaporan.setOnClickListener(v -> startActivity(new Intent(getActivity(), LaporanActivity.class)));
        cardProfile.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .addToBackStack(null).commit();
            }
        });
        btnLogout.setOnClickListener(v -> {
            stopSiren();
            SharedPreferences settings = requireActivity().getSharedPreferences("SatgasPrefs", MODE_PRIVATE);
            settings.edit().clear().apply();
            startActivity(new Intent(getActivity(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            requireActivity().finish();
        });
    }

    private void setupPanicButton() {
        panicRunnable = () -> {
            isPanicTriggered = true;
            checkPermissionAndTriggerAlarm();
        };

        cardPanic.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isPanicTriggered = false;
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    handler.postDelayed(panicRunnable, 2000);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    handler.removeCallbacks(panicRunnable);
                    if (!isPanicTriggered) Toast.makeText(getContext(), "Tahan tombol 2 detik!", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        });
    }

    private void setupAlarmBannerListener() {
        mapContainer.setOnClickListener(v -> openGoogleMaps());
        btnStopSiren.setOnClickListener(v -> {
            stopSiren();
            isSirenMuted = true;
            Toast.makeText(getContext(), "Sirine dimatikan.", Toast.LENGTH_SHORT).show();
        });
    }

    private void openGoogleMaps() {
        if (currentAlarmData != null) {
            double lat = currentAlarmData.getLatitude();
            double lon = currentAlarmData.getLongitude();
            String uri = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f", lat, lon);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            mapIntent.setPackage("com.google.android.apps.maps");
            try {
                startActivity(mapIntent);
            } catch (ActivityNotFoundException e) {
                String browserUri = String.format(Locale.ENGLISH, "https://www.google.com/maps/dir/?api=1&destination=%f,%f", lat, lon);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(browserUri));
                startActivity(browserIntent);
            }
        } else {
            Toast.makeText(getContext(), "Data lokasi alarm tidak tersedia.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkActiveAlarms() {
        apiService.getActiveAlarms("active").enqueue(new Callback<List<AlarmModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<AlarmModel>> call, @NonNull Response<List<AlarmModel>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentAlarmData = response.body().get(0);
                    displayAlarmBanner(currentAlarmData);
                } else {
                    hideAlarmBanner();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AlarmModel>> call, @NonNull Throwable t) { hideAlarmBanner(); }
        });
    }

    private void displayAlarmBanner(AlarmModel alarm) {
        cardAlarmBanner.setVisibility(View.VISIBLE);

        // PERBAIKAN FORMAT NOTIFIKASI
        String category = alarm.getCategory();
        if (category != null && !category.isEmpty()) {
            String formattedCategory = category.substring(0, 1).toUpperCase() + category.substring(1);
            String pelaporName = alarm.getPetugasName() != null ? alarm.getPetugasName().toLowerCase() : "";
            tvAlarmDetail.setText(String.format(Locale.getDefault(), "%s - Pelapor: %s", formattedCategory, pelaporName));
        }

        setAlarmTime(alarm.getTimestamp());

        if (alarm.getId() != currentAlarmId) {
            currentAlarmId = alarm.getId();
            isSirenMuted = false;
            playSiren();
        }
    }

    private void hideAlarmBanner() {
        cardAlarmBanner.setVisibility(View.GONE);
        stopSiren();
        currentAlarmId = -1;
        currentAlarmData = null;
    }

    private void setAlarmTime(String timestamp) {
        try {
            String timePart = timestamp.substring(11, 19);
            tvAlarmTime.setText(timePart);
        } catch (Exception e) {
            Log.e(TAG, "Gagal memotong string timestamp: " + timestamp, e);
            tvAlarmTime.setText("--:--:--");
        }
    }

    private void playSiren() {
        if (isSirenPlaying || getContext() == null) return;
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (notification == null) notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getContext(), notification);
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isSirenPlaying = true;
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void stopSiren() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isSirenPlaying = false;
    }

    private void checkPermissionAndTriggerAlarm() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) showAlarmDialog(location);
            else Toast.makeText(getContext(), "Gagal mendapatkan lokasi GPS.", Toast.LENGTH_LONG).show();
        });
    }

    private void showAlarmDialog(Location location) {
        final String[] categories = {"Maling", "Kebakaran", "Bencana Alam", "Keributan/Tawuran", "Gawat Darurat Medis", "Lainnya"};
        final String[] categoryKeys = {"maling", "kebakaran", "bencana", "keributan", "medis", "lainnya"};

        new AlertDialog.Builder(requireContext())
                .setTitle("PILIH KATEGORI DARURAT")
                .setCancelable(false)
                .setItems(categories, (dialog, which) -> {
                    String selected = categoryKeys[which];
                    if (selected.equals("lainnya")) showCustomDescriptionDialog(location);
                    else sendAlarm(selected, location, "");
                })
                .setNegativeButton("BATAL", (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    private void showCustomDescriptionDialog(Location location) {
        final EditText input = new EditText(requireContext());
        input.setHint("Jelaskan situasi...");
        new AlertDialog.Builder(requireContext())
                .setTitle("Deskripsi Darurat")
                .setView(input)
                .setPositiveButton("KIRIM", (dialog, which) -> sendAlarm("lainnya", location, input.getText().toString().trim()))
                .setNegativeButton("BATAL", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void sendAlarm(String category, Location location, String description) {
        String latStr = String.format(Locale.US, "%.6f", location.getLatitude());
        String lonStr = String.format(Locale.US, "%.6f", location.getLongitude());
        AlarmRequest request = new AlarmRequest(category, latStr, lonStr, description);

        apiService.triggerAlarm(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "ALARM DARURAT TERKIRIM!", Toast.LENGTH_LONG).show();
                } else {
                    try {
                        String errorMsg = response.errorBody() != null ? response.errorBody().string() : "Gagal kirim alarm.";
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Gagal membaca response error.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Koneksi Gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(pollingRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(pollingRunnable);
        stopSiren();
    }
}
