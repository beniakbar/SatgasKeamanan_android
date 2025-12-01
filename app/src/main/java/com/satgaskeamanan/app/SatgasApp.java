package com.satgaskeamanan.app;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class SatgasApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Memaksa aplikasi untuk selalu menggunakan Light Mode (Tema Terang)
        // meskipun sistem HP menggunakan Dark Mode.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}
