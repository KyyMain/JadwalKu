package com.example.jadwalku;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jadwalku.Model.ToDoModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * BroadcastReceiver untuk menjadwalkan ulang pengingat saat perangkat direstart
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(TAG, "Perangkat direstart, mengembalikan pengingat");

            // Inisialisasi ReminderManager
            ReminderManager reminderManager = new ReminderManager(context);

            // Cek pengguna yang login
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();

            if (user == null) {
                Log.d(TAG, "Tidak ada pengguna yang login");
                return;
            }

            String userId = user.getUid();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            // Ambil semua tugas dengan pengingat
            firestore.collection("users").document(userId)
                    .collection("tasks")
                    .whereEqualTo("reminder", true)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    // Konversi dokumen ke ToDoModel
                                    ToDoModel todoModel = document.toObject(ToDoModel.class);
                                    todoModel.withId(document.getId());

                                    // Jadwalkan ulang pengingat
                                    if (todoModel.hasReminder()) {
                                        reminderManager.scheduleReminder(todoModel, todoModel.getReminderMinutes());

                                        String timeInfo = todoModel.getTime() != null ?
                                                " dengan waktu " + todoModel.getTime() : "";
                                        Log.d(TAG, "Dijadwalkan ulang: " + todoModel.getTask() +
                                                " untuk tanggal " + todoModel.getDue() + timeInfo);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error menjadwalkan ulang tugas: " + e.getMessage());
                                }
                            }
                            Log.d(TAG, "Berhasil menjadwalkan ulang pengingat: " + task.getResult().size());
                        } else {
                            Log.e(TAG, "Error mengambil tugas: ", task.getException());
                        }
                    });
        }
    }
}