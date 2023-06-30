package com.example.medmate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000; // Durasi splash screen dalam milisecond (2 detik)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        // Menggunakan Handler untuk menunda perpindahan ke Activity berikutnya
        new Handler().postDelayed(() -> {
            if (currentUser != null) {
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Menutup activity ini agar tidak dapat dikembalikan dengan tombol back
            }
        }, SPLASH_DELAY);
    }
}
