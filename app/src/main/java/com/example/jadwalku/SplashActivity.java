package com.example.jadwalku;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        new Handler().postDelayed(() -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                // Jika pengguna masih login, arahkan ke MainActivity
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                // Jika belum login, arahkan ke LoginActivity
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, 2000); // durasi splash screen
    }

}
