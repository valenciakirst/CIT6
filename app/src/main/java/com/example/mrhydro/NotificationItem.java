package com.example.mrhydro;

public class NotificationItem {
    private int id;
    private String message;
    private long timestamp;

    public NotificationItem(int id, String message, long timestamp) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
