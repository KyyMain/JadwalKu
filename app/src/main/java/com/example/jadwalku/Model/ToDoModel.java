package com.example.jadwalku.Model;

public class ToDoModel extends TaskId {

    private String task, due;
    private int status;
    private boolean reminder; // Flag untuk menandai apakah tugas memiliki pengingat
    private int reminderMinutes; // Menit sebelum deadline untuk menampilkan pengingat
    private String time; // Menyimpan waktu tugas dalam format "HH:mm"

    public String getTask() {
        return task;
    }

    public String getDue() {
        return due;
    }

    public int getStatus() {
        return status;
    }

    public boolean hasReminder() {
        return reminder;
    }

    public int getReminderMinutes() {
        return reminderMinutes;
    }

    public String getTime() {
        return time;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public void setDue(String due) {
        this.due = due;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setReminder(boolean reminder) {
        this.reminder = reminder;
    }

    public void setReminderMinutes(int reminderMinutes) {
        this.reminderMinutes = reminderMinutes;
    }

    public void setTime(String time) {
        this.time = time;
    }
}