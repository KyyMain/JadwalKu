package com.example.jadwalku;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.jadwalku.Model.ToDoModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "AddNewTask";

    private TextView setDueDate;
    private TextView setTime;
    private EditText mTaskEdit;
    private Button mSaveBtn;
    private FirebaseFirestore firestore;
    private Context context;
    private String dueDate = "";
    private String selectedTime = ""; // Untuk menyimpan waktu yang dipilih
    private String id = "";
    private String dueDateUpdate = "";
    private String timeUpdate = ""; // Untuk waktu pada saat update

    // Komponen UI untuk pengingat
    private CheckBox reminderCheckBox;
    private Spinner reminderTimeSpinner;
    private boolean hasReminder = false;
    private int reminderMinutes = 30; // Default 30 menit

    private ReminderManager reminderManager;

    public static AddNewTask newInstance() {
        return new AddNewTask();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_new_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        setDueDate = view.findViewById(R.id.set_due_tv);
        setTime = view.findViewById(R.id.set_time_tv);
        mTaskEdit = view.findViewById(R.id.task_edittext);
        mSaveBtn = view.findViewById(R.id.save_btn);

        // Inisialisasi komponen UI pengingat
        reminderCheckBox = view.findViewById(R.id.reminder_checkbox);
        reminderTimeSpinner = view.findViewById(R.id.reminder_time_spinner);

        // Inisialisasi ReminderManager
        reminderManager = new ReminderManager(getContext());

        firestore = FirebaseFirestore.getInstance();

        boolean isUpdate;

        // Cek apakah dialog ini untuk update atau tambah baru
        final Bundle bundle = getArguments();
        if (bundle != null) {
            isUpdate = true;
            String task = bundle.getString("task");
            id = bundle.getString("id");
            dueDateUpdate = bundle.getString("due");
            timeUpdate = bundle.getString("time", ""); // Ambil waktu jika ada

            // Ambil data pengingat jika ada
            hasReminder = bundle.getBoolean("hasReminder", false);
            reminderMinutes = bundle.getInt("reminderMinutes", 30);

            mTaskEdit.setText(task);
            dueDate = dueDateUpdate;
            selectedTime = timeUpdate;

            setDueDate.setText(dueDate);

            if (!selectedTime.isEmpty()) {
                setTime.setText(selectedTime);
            }

            // Set nilai untuk pengingat
            reminderCheckBox.setChecked(hasReminder);

            // Tampilkan spinner jika pengingat aktif
            if (hasReminder) {
                reminderTimeSpinner.setVisibility(View.VISIBLE);
                // Pilih item yang sesuai di spinner
                int position = 1; // Default 30 menit
                if (reminderMinutes == 15) position = 0;
                else if (reminderMinutes == 30) position = 1;
                else if (reminderMinutes == 60) position = 2;
                else if (reminderMinutes == 120) position = 3;
                else if (reminderMinutes == 1440) position = 4;
                reminderTimeSpinner.setSelection(position);
            } else {
                reminderTimeSpinner.setVisibility(View.GONE);
            }

            mSaveBtn.setEnabled(true);
            mSaveBtn.setBackgroundColor(Color.parseColor("#F10091"));
        } else {
            isUpdate = false;
        }

        // Listener untuk reminder checkbox
        reminderCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hasReminder = isChecked;
                reminderTimeSpinner.setVisibility(isChecked ? View.VISIBLE : View.GONE);

                // Periksa apakah tanggal sudah diisi
                if (isChecked && (dueDate == null || dueDate.isEmpty())) {
                    Toast.makeText(context, "Isi tanggal terlebih dahulu", Toast.LENGTH_SHORT).show();
                    reminderCheckBox.setChecked(false);
                }
            }
        });

        // Listener untuk spinner waktu pengingat
        reminderTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Konversi posisi ke menit
                switch (position) {
                    case 0: reminderMinutes = 15; break;    // 15 menit
                    case 1: reminderMinutes = 30; break;    // 30 menit
                    case 2: reminderMinutes = 60; break;    // 1 jam
                    case 3: reminderMinutes = 120; break;   // 2 jam
                    case 4: reminderMinutes = 1440; break;  // 1 hari
                    default: reminderMinutes = 30;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                reminderMinutes = 30; // Default 30 menit
            }
        });

        // Listener untuk perubahan teks tugas
        mTaskEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSaveBtn.setEnabled(!s.toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Listener untuk klik pada tanggal
        setDueDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view1, selectedYear, selectedMonth, selectedDay) -> {
                selectedMonth += 1; // months are indexed from 0
                dueDate = selectedDay + "/" + selectedMonth + "/" + selectedYear;
                setDueDate.setText(dueDate);
            }, year, month, day);

            datePickerDialog.show();
        });

        // Listener untuk klik pada waktu
        setTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                    (view1, selectedHour, selectedMinute) -> {
                        // Format waktu 24 jam dengan padding nol di depan jika perlu
                        selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                        setTime.setText(selectedTime);
                    }, hourOfDay, minute, true); // true untuk format 24 jam

            timePickerDialog.show();
        });

        // Listener untuk tombol Simpan
        mSaveBtn.setOnClickListener(v -> {
            String task = mTaskEdit.getText().toString();

            if (task.isEmpty()) {
                Toast.makeText(context, "Tugas tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dueDate.isEmpty()) {
                Toast.makeText(context, "Tanggal tidak boleh kosong!", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> taskMap = new HashMap<>();
            taskMap.put("task", task);
            taskMap.put("due", dueDate);
            taskMap.put("status", 0);
            taskMap.put("reminder", hasReminder);
            taskMap.put("reminderMinutes", reminderMinutes);
            taskMap.put("time", selectedTime); // Simpan waktu ke Firestore

            if (isUpdate) {
                // Update tugas yang sudah ada
                firestore.collection("users").document(userId)
                        .collection("tasks").document(id)
                        .update(taskMap)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Tugas Diperbarui", Toast.LENGTH_SHORT).show();

                            // Update pengingat jika aktif
                            if (hasReminder) {
                                ToDoModel model = new ToDoModel();
                                model.withId(id);
                                model.setTask(task);
                                model.setDue(dueDate);
                                model.setTime(selectedTime);
                                model.setStatus(0);
                                model.setReminder(hasReminder);
                                model.setReminderMinutes(reminderMinutes);

                                reminderManager.scheduleReminder(model, reminderMinutes);
                            } else {
                                // Batalkan pengingat jika dinonaktifkan
                                reminderManager.cancelReminder(id);
                            }

                            dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                // Tambah tugas baru
                firestore.collection("users").document(userId).collection("tasks")
                        .add(taskMap)
                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isSuccessful()) {
                                    // Ambil ID dokumen baru
                                    String newTaskId = task.getResult().getId();

                                    Toast.makeText(context, "Tugas Tersimpan", Toast.LENGTH_SHORT).show();

                                    // Jadwalkan pengingat jika aktif
                                    if (hasReminder) {
                                        ToDoModel model = new ToDoModel();
                                        model.withId(newTaskId);
                                        model.setTask(taskMap.get("task").toString());
                                        model.setDue(taskMap.get("due").toString());
                                        model.setTime(taskMap.get("time").toString());
                                        model.setStatus(0);
                                        model.setReminder(hasReminder);
                                        model.setReminderMinutes(reminderMinutes);

                                        reminderManager.scheduleReminder(model, reminderMinutes);
                                    }

                                    dismiss();
                                } else {
                                    Toast.makeText(context, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof OnDialogCloseListener) {
            ((OnDialogCloseListener) activity).onDialogClose(dialog);
        }
    }
}