package com.example.mrhydro;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TemperatureMonitorService extends Service {

    private DatabaseReference temperatureRef;
    private Double lastNotifiedTemperature = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase reference
        temperatureRef = FirebaseDatabase.getInstance().getReference("DHT/TemperatureInC");

        // Listen for temperature changes
        temperatureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double currentTemperature = snapshot.getValue(Double.class);
                    if (currentTemperature != null && currentTemperature >= 35) {
                        if (!currentTemperature.equals(lastNotifiedTemperature)) {
                            lastNotifiedTemperature = currentTemperature;
                            sendNotification("High Temperature Alert", "Temperature is High: " + currentTemperature + "°C");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle errors
            }
        });
    }

    private void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "temperature_alert_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Temperature Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove Firebase listener to avoid memory leaks
        if (temperatureRef != null) {
            temperatureRef.removeEventListener((ValueEventListener) this);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // This service is not designed for binding
    }
}
