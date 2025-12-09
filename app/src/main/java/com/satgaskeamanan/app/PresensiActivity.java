package com.satgaskeamanan.app;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;

import com.satgaskeamanan.app.api.APIClient;
import com.satgaskeamanan.app.api.APIService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PresensiActivity extends AppCompatActivity {
    private static final String TAG = "PresensiActivity";

    private static final int REQ_PERMISSIONS = 100;
    private static final int REQ_IMAGE_CAPTURE = 101;

    // UI
    private TextView txtLocation;
    private ImageView imgSelfie;
    private EditText etNote;
    private Button btnAmbilFoto, btnKirim, btnBack; // Tambahkan btnBack

    // Location & Camera
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location currentLocation;

    private String currentPhotoPath;
    private APIService apiService;

    private final String[] REQUIRED_PERMS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presensi);

        // Hapus kode Action Bar sebelumnya karena kita pakai tombol manual sekarang

        // init UI
        txtLocation = findViewById(R.id.txtLocation);
        imgSelfie = findViewById(R.id.imgSelfie);
        etNote = findViewById(R.id.etNote);
        btnAmbilFoto = findViewById(R.id.btnAmbilFoto);
        btnKirim = findViewById(R.id.btnKirim);
        btnBack = findViewById(R.id.btn_back_presensi); // Bind ID

        // init api & location
        apiService = APIClient.getAPIService(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupLocationRequest();
        setupLocationCallback();

        // check permissions
        if (!hasAllPermissions()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMS, REQ_PERMISSIONS);
        } else {
            startLocationUpdates();
        }

        btnAmbilFoto.setOnClickListener(v -> dispatchTakePictureIntent());

        // Listener Tombol Kembali (Manual)
        btnBack.setOnClickListener(v -> finish());

        btnKirim.setOnClickListener(v -> {
            btnKirim.setEnabled(false);
            new Handler(Looper.getMainLooper()).postDelayed(() -> btnKirim.setEnabled(true), 1500);

            if (currentLocation == null) {
                Toast.makeText(this, "Lokasi belum siap, tunggu sebentar...", Toast.LENGTH_SHORT).show();
                startLocationUpdates();
                return;
            }

            if (currentPhotoPath == null) {
                Toast.makeText(this, "Silakan ambil foto terlebih dahulu.", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadPresensi();
        });
    }

    private boolean hasAllPermissions() {
        for (String p : REQUIRED_PERMS) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSIONS) {
            boolean granted = true;
            if (grantResults.length == 0) granted = false;
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Izin lokasi/kamera diperlukan agar presensi berfungsi.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2000); // 2s
        locationRequest.setFastestInterval(1000);
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;
                currentLocation = locationResult.getLastLocation();
                if (currentLocation != null) {
                    txtLocation.setText(String.format(Locale.US, "Lat: %.6f | Lon: %.6f",
                            currentLocation.getLatitude(), currentLocation.getLongitude()));
                    try {
                        fusedLocationClient.removeLocationUpdates(locationCallback);
                    } catch (SecurityException se) {
                        Log.w(TAG, "removeLocationUpdates denied: " + se.getMessage());
                    }
                }
            }
        };
    }

    private void startLocationUpdates() {
        if (!hasAllPermissions()) return;
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(loc -> {
                        if (loc != null) {
                            currentLocation = loc;
                            txtLocation.setText(String.format(Locale.US, "Lat: %.6f | Lon: %.6f",
                                    loc.getLatitude(), loc.getLongitude()));
                        }
                    });
        } catch (SecurityException se) {
            Log.e(TAG, "startLocationUpdates SecurityException: " + se.getMessage());
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && !storageDir.exists()) storageDir.mkdirs();
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, "Kamera tidak tersedia di perangkat ini.", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.e(TAG, "createImageFile failed: " + ex.getMessage());
            Toast.makeText(this, "Gagal membuat file foto.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivityForResult(takePictureIntent, REQ_IMAGE_CAPTURE);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Kamera tidak ditemukan.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (currentPhotoPath != null) {
                File f = new File(currentPhotoPath);
                if (f.exists()) {
                    imgSelfie.setImageURI(Uri.fromFile(f));
                    Toast.makeText(this, "Foto tersimpan.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "File foto tidak ditemukan.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Path foto kosong.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadPresensi() {
        if (currentLocation == null) {
            Toast.makeText(this, "Lokasi belum tersedia.", Toast.LENGTH_SHORT).show();
            startLocationUpdates();
            return;
        }

        if (currentPhotoPath == null) {
            Toast.makeText(this, "Silakan ambil foto terlebih dahulu.", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = new File(currentPhotoPath);
        if (!photoFile.exists()) {
            Toast.makeText(this, "File foto tidak ditemukan.", Toast.LENGTH_SHORT).show();
            return;
        }

        String latStr = String.format(Locale.US, "%.6f", currentLocation.getLatitude());
        String lonStr = String.format(Locale.US, "%.6f", currentLocation.getLongitude());
        String locationNote = "Lokasi otomatis";
        String noteText = etNote.getText() != null && !etNote.getText().toString().trim().isEmpty()
                ? etNote.getText().toString().trim() : "-";

        RequestBody latRB = RequestBody.create(MediaType.parse("text/plain"), latStr);
        RequestBody lonRB = RequestBody.create(MediaType.parse("text/plain"), lonStr);
        RequestBody locRB = RequestBody.create(MediaType.parse("text/plain"), locationNote);
        RequestBody noteRB = RequestBody.create(MediaType.parse("text/plain"), noteText);

        RequestBody fileRB = RequestBody.create(MediaType.parse("image/jpeg"), photoFile);
        MultipartBody.Part photoPart = MultipartBody.Part.createFormData("selfie_photo", photoFile.getName(), fileRB);

        Call<Void> call = apiService.kirimPresensi(latRB, lonRB, locRB, noteRB, photoPart);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PresensiActivity.this, "Presensi berhasil dikirim!", Toast.LENGTH_LONG).show();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 900);
                } else {
                    Toast.makeText(PresensiActivity.this, "Gagal upload: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(PresensiActivity.this, "Gagal terhubung: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        } catch (Exception ignored) { }
    }
}
