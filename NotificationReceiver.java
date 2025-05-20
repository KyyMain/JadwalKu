package com.example.jadwalku;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

/**
 * BroadcastReceiver untuk menampilkan notifikasi pengingat tugas
 */
public class NotificationReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID = "jadwalku_reminder_channel";
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Ambil data dari intent
        String taskId = intent.getStringExtra("taskId");
        String taskTitle = intent.getStringExtra("taskTitle");
        String taskDate = intent.getStringExtra("taskDate");

        if (taskTitle == null) {
            taskTitle = "Pengingat Tugas";
        }

        // Intent untuk membuka MainActivity saat notifikasi diklik
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.putExtra("taskId", taskId);

        // PendingIntent untuk notifikasi
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                taskId != null ? taskId.hashCode() : 0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Buat NotificationManager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Buat channel untuk Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Pengingat JadwalKu",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Pengingat untuk tugas di aplikasi JadwalKu");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        // Buat notifikasi
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_add_24) // Ganti dengan ikon yang sesuai
                .setContentTitle("Pengingat: " + taskTitle)
                .setContentText(taskDate != null ? "Batas waktu: " + taskDate : "Waktunya mengerjakan tugas!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColorized(true)
                .setColor(Color.parseColor("#FF4081")) // Sesuaikan dengan warna tema aplikasi Anda
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Tampilkan notifikasi
        notificationManager.notify(taskId != null ? taskId.hashCode() : 0, builder.build());

        Log.d(TAG, "Notifikasi ditampilkan untuk tugas: " + taskTitle + " dengan batas waktu: " + taskDate);
    }
}