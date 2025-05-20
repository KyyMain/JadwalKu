package com.example.jadwalku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailSignUp, usernameSignUp, passwordSignUp;
    private Button signUpButton;
    private TextView loginDirect;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Inisialisasi FirebaseAuth dan FirebaseFirestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inisialisasi views
        emailSignUp = findViewById(R.id.Email_signup);
        usernameSignUp = findViewById(R.id.Signup_username);
        passwordSignUp = findViewById(R.id.Signup_pw);
        signUpButton = findViewById(R.id.Signup_btn);
        loginDirect = findViewById(R.id.loginDirect);

        // Handle tombol sign up
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailSignUp.getText().toString().trim();
                String username = usernameSignUp.getText().toString().trim();
                String password = passwordSignUp.getText().toString().trim();

                // Validasi input
                if (TextUtils.isEmpty(email)) {
                    emailSignUp.setError("Email diperlukan");
                    return;
                }
                if (TextUtils.isEmpty(username)) {
                    usernameSignUp.setError("Username diperlukan");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    passwordSignUp.setError("Password diperlukan");
                    return;
                }
                if (password.length() < 6) {
                    passwordSignUp.setError("Password harus minimal 6 karakter");
                    return;
                }

                // Proses registrasi dengan Firebase
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Registrasi berhasil, dapatkan UID pengguna
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        String userId = user.getUid();
                                        Map<String, Object> userMap = new HashMap<>();
                                        userMap.put("username", username);
                                        userMap.put("email", email);

                                        // Simpan data pengguna di Firestore
                                        db.collection("users").document(userId).set(userMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(SignUpActivity.this, "Registrasi dan penyimpanan berhasil", Toast.LENGTH_SHORT).show();
                                                        // Arahkan ke halaman utama
                                                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(SignUpActivity.this, "Gagal menyimpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                } else {
                                    // Jika gagal, tampilkan pesan error
                                    Toast.makeText(SignUpActivity.this, "Registrasi gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Handle text login jika pengguna sudah punya akun
        loginDirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Arahkan ke halaman login
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}
