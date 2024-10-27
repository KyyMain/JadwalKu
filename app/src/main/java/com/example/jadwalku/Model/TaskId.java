package com.example.jadwalku.Model;

public class TaskId {
    private String id;

    public String getTaskId() {
        return id;
    }

    public void setTaskId(String id) {
        this.id = id;
    }

    // Metode withId untuk mengatur ID dan mengembalikan objek dengan ID yang diatur
    public <T extends TaskId> T withId(String id) {
        this.id = id;
        return (T) this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
