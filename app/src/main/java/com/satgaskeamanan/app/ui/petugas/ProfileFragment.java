package com.satgaskeamanan.app.ui.petugas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.satgaskeamanan.app.LoginActivity;
import com.satgaskeamanan.app.R;
import com.satgaskeamanan.app.api.APIClient;
import com.satgaskeamanan.app.api.APIService;
import com.satgaskeamanan.app.models.UserModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private CircleImageView ivProfilePicture;
    private TextInputEditText etFirstName, etLastName, etEmail, etPhone;
    private Button btnSave, btnLogout;
    private ProgressBar progressBar;
    private APIService apiService;
    private Uri selectedImageUri;

    private static final int PICK_IMAGE_REQUEST = 1;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivProfilePicture = view.findViewById(R.id.iv_profile_picture);
        etFirstName = view.findViewById(R.id.et_profile_name);
        etLastName = view.findViewById(R.id.et_profile_last_name);
        etEmail = view.findViewById(R.id.et_profile_email);
        etPhone = view.findViewById(R.id.et_profile_phone);
        btnSave = view.findViewById(R.id.btn_save_profile);
        btnLogout = view.findViewById(R.id.btn_logout_profile);
        progressBar = view.findViewById(R.id.pb_profile);

        apiService = APIClient.getAPIService(requireContext());

        loadUserProfile();

        ivProfilePicture.setOnClickListener(v -> openGallery());
        btnSave.setOnClickListener(v -> updateProfile());
        btnLogout.setOnClickListener(v -> performLogout());
    }

    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getUserProfile().enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(@NonNull Call<UserModel> call, @NonNull Response<UserModel> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    UserModel user = response.body();
                    etFirstName.setText(user.getFirstName());
                    etLastName.setText(user.getLastName());
                    etEmail.setText(user.getEmail());
                    etPhone.setText(user.getPhoneNumber());

                    if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                        Glide.with(requireContext())
                                .load(user.getProfilePicture())
                                .placeholder(R.mipmap.ic_launcher)
                                .into(ivProfilePicture);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserModel> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Gagal memuat profil.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ivProfilePicture.setImageURI(selectedImageUri); // Preview lokal
        }
    }

    private void updateProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        // Gunakan "multipart/form-data" untuk string part juga
        RequestBody rbFirstName = RequestBody.create(MediaType.parse("text/plain"), firstName);
        RequestBody rbLastName = RequestBody.create(MediaType.parse("text/plain"), lastName);
        RequestBody rbPhone = RequestBody.create(MediaType.parse("text/plain"), phone);

        MultipartBody.Part photoPart = null;
        if (selectedImageUri != null) {
            try {
                File file = getFileFromUri(selectedImageUri);
                
                // Pastikan MIME Type valid
                String mimeType = requireContext().getContentResolver().getType(selectedImageUri);
                if (mimeType == null) {
                    mimeType = "image/jpeg"; // Fallback default
                }
                
                // Buat RequestBody untuk file gambar
                RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
                
                // Nama parameter harus 'profile_picture' sesuai serializer backend
                photoPart = MultipartBody.Part.createFormData("profile_picture", file.getName(), requestFile);
                
            } catch (Exception e) {
                e.printStackTrace();
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Gagal memproses gambar", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        apiService.updateProfile(rbFirstName, rbLastName, rbPhone, photoPart).enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(@NonNull Call<UserModel> call, @NonNull Response<UserModel> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Gagal update profil: " + response.code() + " " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserModel> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File getFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        
        // Buat nama file unik agar tidak konflik
        String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
        File file = new File(requireContext().getCacheDir(), fileName);
        
        OutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[4096]; // Buffer lebih besar sedikit
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
        return file;
    }

    private void performLogout() {
        SharedPreferences settings = requireContext().getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        settings.edit().clear().apply();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
