package com.example.jadwalku;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jadwalku.Adapter.ToDoAdapter;
import com.example.jadwalku.Model.ToDoModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnDialogCloseListener {

    // Deklarasi variabel UI dan Firebase
    private RecyclerView recyclerView;
    private FloatingActionButton mFab, profileFab;
    private FirebaseFirestore firestore;
    private ToDoAdapter adapter;
    private List<ToDoModel> mList;
    private Query query;
    private ListenerRegistration listenerRegistration;
    private TextView greetingTextView;

    // Cek status autentikasi saat Activity dimulai
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            // Jika belum login, arahkan ke LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            // Jika pengguna tidak ada, kembali ke LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Inisialisasi TextView untuk ucapan
        greetingTextView = findViewById(R.id.textView2);

        // Mengambil jam saat ini untuk menentukan ucapan
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        // Tentukan ucapan berdasarkan waktu
        String greetingMessage;
        if (hourOfDay >= 5 && hourOfDay < 12) {
            greetingMessage = "Selamat Pagi";
        } else if (hourOfDay >= 12 && hourOfDay < 15) {
            greetingMessage = "Selamat Siang";
        } else if (hourOfDay >= 15 && hourOfDay < 18) {
            greetingMessage = "Selamat Sore";
        } else {
            greetingMessage = "Selamat Malam";
        }

        // Ambil username pengguna dari Firestore
        firestore = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        // Ambil dokumen pengguna dari Firestore
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        if (username != null) {
                            // Gabungkan ucapan dan username
                            greetingTextView.setText(greetingMessage + ", " + username + "!");
                        } else {
                            greetingTextView.setText(greetingMessage + "!");
                        }
                    } else {
                        greetingTextView.setText(greetingMessage + "!");
                    }
                })
                .addOnFailureListener(e -> {
                    // Tampilkan ucapan default jika terjadi error
                    greetingTextView.setText(greetingMessage + "!");
                });

        // Inisialisasi RecyclerView dan FloatingActionButtons
        recyclerView = findViewById(R.id.recycerlview);
        mFab = findViewById(R.id.floatingActionButton);
        profileFab = findViewById(R.id.profileFab);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        // Buka ProfileActivity saat tombol profileFab diklik
        profileFab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Tampilkan dialog untuk menambah tugas saat tombol mFab diklik
        mFab.setOnClickListener(v -> AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG));

        // Inisialisasi daftar tugas dan adapter untuk RecyclerView
        mList = new ArrayList<>();
        adapter = new ToDoAdapter(MainActivity.this, mList);

        // Tambahkan fungsi geser untuk menghapus tugas
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Ambil data dari Firestore dan atur RecyclerView dengan adapter
        showData();
        recyclerView.setAdapter(adapter);
    }

    private void showData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            return;
        }

        String userId = user.getUid();
        // Query untuk mengambil tugas berdasarkan userId dari Firestore
        query = firestore.collection("users").document(userId)
                .collection("tasks")
                .orderBy("time", Query.Direction.DESCENDING);

        listenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (value == null || value.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Belum ada tugas !!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                HashSet<String> existingIds = new HashSet<>();
                for (ToDoModel model : mList) {
                    existingIds.add(model.getTaskId());
                }

                // Update daftar tugas berdasarkan perubahan dari Firestore
                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    String id = documentChange.getDocument().getId();
                    ToDoModel toDoModel = documentChange.getDocument().toObject(ToDoModel.class).withId(id);

                    switch (documentChange.getType()) {
                        case ADDED:
                            if (!existingIds.contains(id)) {
                                mList.add(toDoModel);
                                existingIds.add(id);
                            }
                            break;
                        case REMOVED:
                            mList.remove(toDoModel);
                            existingIds.remove(id);
                            break;
                        case MODIFIED:
                            int index = -1;
                            for (int i = 0; i < mList.size(); i++) {
                                if (mList.get(i).getTaskId().equals(id)) {
                                    index = i;
                                    break;
                                }
                            }
                            if (index != -1) {
                                mList.set(index, toDoModel);
                            }
                            break;
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        // Perbarui tampilan data setelah dialog ditutup
        mList.clear();
        showData();
        adapter.notifyDataSetChanged();
    }
}
