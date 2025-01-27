package com.example.mrhydro;

public class HumidityData {
    private float humidity;
    private long timestamp;

    // Default constructor required for Firebase
    @SuppressWarnings("unused")
    public HumidityData() {}

    public HumidityData(float humidity, long timestamp) {
        this.humidity = humidity;
        this.timestamp = timestamp;
    }

    public float getHumidity() {
        return humidity;
    }

    @SuppressWarnings("unused") // Required for Firebase deserialization
    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @SuppressWarnings("unused") // Required for Firebase deserialization
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
