package com.example.jadwalku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail, tvVersion;
    private Button btnLogout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // Sesuaikan dengan layout XML Anda

        // Inisialisasi FirebaseAuth dan Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inisialisasi Views
        tvUsername = findViewById(R.id.textView4);
        tvEmail = findViewById(R.id.textView5);
        tvVersion = findViewById(R.id.versionApp);
        btnLogout = findViewById(R.id.btnLogout);

        // Ambil data pengguna yang sedang login dari Firestore
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Setel Email di TextView
            tvEmail.setText(currentUser.getEmail());

            // Ambil UID pengguna
            String userId = currentUser.getUid();

            // Ambil data username dari Firestore
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Ambil data username dari dokumen Firestore
                            String username = documentSnapshot.getString("username");
                            tvUsername.setText(username != null ? username : "Username tidak ditemukan");
                        } else {
                            Toast.makeText(ProfileActivity.this, "Dokumen tidak ditemukan", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Gagal mengambil data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Jika pengguna tidak login, arahkan ke LoginActivity
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        }

        // Set versi aplikasi
        tvVersion.setText("V.1.0.0");

        // Fungsi Logout
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(ProfileActivity.this, "Logout berhasil", Toast.LENGTH_SHORT).show();
            // Arahkan pengguna ke LoginActivity setelah logout
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
