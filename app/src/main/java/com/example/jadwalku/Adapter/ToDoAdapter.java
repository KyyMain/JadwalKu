package com.example.jadwalku.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jadwalku.AddNewTask;
import com.example.jadwalku.MainActivity;
import com.example.jadwalku.Model.ToDoModel;
import com.example.jadwalku.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.MyViewHolder> {

    // Daftar tugas dan variabel Firebase
    private List<ToDoModel> todoList;
    private MainActivity activity;
    private FirebaseFirestore firestore;
    private String userId;

    // Konstruktor Adapter, menerima MainActivity dan daftar tugas
    public ToDoAdapter(MainActivity mainActivity, List<ToDoModel> todoList) {
        this.todoList = todoList;
        activity = mainActivity;
        firestore = FirebaseFirestore.getInstance();
        // Mengambil userId dari pengguna yang sedang login
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Menghubungkan setiap item tugas dengan layout XML `each_task`
        View view = LayoutInflater.from(activity).inflate(R.layout.each_task, parent, false);
        return new MyViewHolder(view);
    }

    // Fungsi untuk menghapus tugas berdasarkan posisi di daftar
    public void deleteTask(int position) {
        ToDoModel toDoModel = todoList.get(position);
        firestore.collection("users").document(userId)
                .collection("tasks").document(toDoModel.getId()).delete(); // Hapus dari Firestore
        todoList.remove(position); // Hapus dari daftar lokal
        notifyItemRemoved(position); // Beritahu adapter bahwa item dihapus
    }

    // Fungsi untuk mendapatkan konteks dari MainActivity
    public Context getContext() {
        return activity;
    }

    // Fungsi untuk mengedit tugas, membuka dialog dengan data yang ada
    public void editTask(int position) {
        ToDoModel toDoModel = todoList.get(position);

        // Buat bundle untuk mengirim data ke dialog
        Bundle bundle = new Bundle();
        bundle.putString("task", toDoModel.getTask());
        bundle.putString("due", toDoModel.getDue());
        bundle.putString("id", toDoModel.getId());

        // Buat dan tampilkan dialog AddNewTask
        AddNewTask addNewTask = new AddNewTask();
        addNewTask.setArguments(bundle);
        addNewTask.show(activity.getSupportFragmentManager(), addNewTask.getTag());
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ToDoModel toDoModel = todoList.get(position);

        holder.mCheckBox.setOnCheckedChangeListener(null); // Hapus listener sementara

        // Set nilai item berdasarkan data model
        holder.mCheckBox.setText(toDoModel.getTask());
        holder.mDueDateTv.setText("Tanggal " + toDoModel.getDue());
        holder.mCheckBox.setChecked(toBoolean(toDoModel.getStatus()));

        if (toDoModel.getStatus() == 1) {
            holder.mCheckBox.setPaintFlags(holder.mCheckBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.mCheckBox.setPaintFlags(holder.mCheckBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Set ulang listener setelah nilai diatur
        holder.mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int newStatus = isChecked ? 1 : 0;
            firestore.collection("users").document(userId)
                    .collection("tasks").document(toDoModel.getId())
                    .update("status", newStatus);

            holder.mCheckBox.setPaintFlags(isChecked ?
                    (holder.mCheckBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG) :
                    (holder.mCheckBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG))
            );
        });
    }


    // Fungsi bantu untuk mengonversi status integer ke boolean
    private boolean toBoolean(int status) {
        return status != 0;
    }

    @Override
    public int getItemCount() {
        return todoList.size(); // Mengembalikan jumlah item di daftar tugas
    }

    // ViewHolder untuk memegang dan mengelola tampilan item tugas
    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mDueDateTv;
        CheckBox mCheckBox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mDueDateTv = itemView.findViewById(R.id.due_date_tv); // Teks untuk tanggal tugas
            mCheckBox = itemView.findViewById(R.id.mcheckbox); // Checkbox untuk tugas
        }
    }
}
