package com.example.jadwalku;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.example.jadwalku.Model.ToDoModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Class untuk mengelola pengingat tugas
 */
public class ReminderManager {
    private static final String TAG = "ReminderManager";
    private Context context;
    private AlarmManager alarmManager;

    public ReminderManager(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * Menjadwalkan pengingat untuk tugas
     */
    public void scheduleReminder(ToDoModel task, int reminderMinutes) {
        if (task == null || task.getId() == null || task.getTask() == null || task.getDue() == null) {
            Log.e(TAG, "Data tugas tidak lengkap");
            return;
        }

        try {
            // Parsing tanggal tugas
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = dateFormat.parse(task.getDue());

            if (date == null) {
                Log.e(TAG, "Gagal parsing tanggal");
                return;
            }

            // Set kalender dengan tanggal tugas
            Calendar taskCalendar = Calendar.getInstance();
            taskCalendar.setTime(date);

            // Cek apakah ada waktu yang ditentukan
            String taskTime = task.getTime();
            if (taskTime != null && !taskTime.isEmpty()) {
                // Parse waktu dari format "HH:mm"
                String[] timeParts = taskTime.split(":");
                if (timeParts.length == 2) {
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);

                    taskCalendar.set(Calendar.HOUR_OF_DAY, hour);
                    taskCalendar.set(Calendar.MINUTE, minute);
                    taskCalendar.set(Calendar.SECOND, 0);
                } else {
                    // Fallback jika format waktu tidak valid
                    taskCalendar.set(Calendar.HOUR_OF_DAY, 12);
                    taskCalendar.set(Calendar.MINUTE, 0);
                    taskCalendar.set(Calendar.SECOND, 0);
                }
            } else {
                // Default jika waktu tidak diisi (menggunakan jam 12 siang)
                taskCalendar.set(Calendar.HOUR_OF_DAY, 12);
                taskCalendar.set(Calendar.MINUTE, 0);
                taskCalendar.set(Calendar.SECOND, 0);
            }

            // Kurangi dengan waktu pengingat (dalam menit)
            Calendar reminderCalendar = (Calendar) taskCalendar.clone();
            reminderCalendar.add(Calendar.MINUTE, -reminderMinutes);

            // Jika waktu pengingat sudah lewat, tidak perlu dijadwalkan
            if (reminderCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
                Log.d(TAG, "Waktu pengingat sudah lewat: " + task.getTask());
                return;
            }

            // Siapkan intent untuk broadcast receiver
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra("taskId", task.getId());
            intent.putExtra("taskTitle", task.getTask());

            String displayTime = taskTime != null && !taskTime.isEmpty() ?
                    " " + taskTime : "";
            intent.putExtra("taskDate", task.getDue() + displayTime);

            // Buat PendingIntent yang unik untuk setiap tugas
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    task.getId().hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Jadwalkan alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderCalendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        reminderCalendar.getTimeInMillis(),
                        pendingIntent
                );
            }

            SimpleDateFormat logFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            Log.d(TAG, "Pengingat dijadwalkan untuk: " + task.getTask() +
                    " pada " + logFormat.format(reminderCalendar.getTime()));

        } catch (ParseException e) {
            Log.e(TAG, "Error parsing tanggal: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }

    /**
     * Membatalkan pengingat untuk tugas
     */
    public void cancelReminder(String taskId) {
        if (taskId == null) return;

        try {
            Intent intent = new Intent(context, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    taskId.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();

            Log.d(TAG, "Pengingat dibatalkan untuk tugas ID: " + taskId);
        } catch (Exception e) {
            Log.e(TAG, "Error saat membatalkan pengingat: " + e.getMessage());
        }
    }
}