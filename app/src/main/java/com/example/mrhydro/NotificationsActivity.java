package com.example.mrhydro;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity implements View.OnClickListener {

    private List<NotificationItem> notificationList;
    private NotificationAdapter adapter;
    int notificationIdCounter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        ImageView backBT = findViewById(R.id.backButton);
        backBT.setOnClickListener(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        double currentTemperature = getCurrentTemperature();
        if (currentTemperature >= 25) {
            addNotification(currentTemperature, System.currentTimeMillis());
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backButton) {
            finish();
        }
    }

    private double getCurrentTemperature() {

        return 30.0;
    }
    private void addNotification(double temperature, long timestamp) {
        int notificationId = notificationIdCounter++;

        String temperatureLevel;
        if (temperature >= 25) {
            temperatureLevel = "High";
        } else if (temperature < 15) {
            temperatureLevel = "Low";
        } else {
            temperatureLevel = "Normal";
        }

        String message = "Temperature is " + temperatureLevel + ": " + temperature + "°C";

        addNotificationToFirebase(message, timestamp);

        // Create and add notification to list
        NotificationItem notification = new NotificationItem(notificationId, message, timestamp);
        notificationList.add(notification);
        adapter.notifyDataSetChanged();

        // Show high temperature notification
        if (temperature >= 25) {
            showNotification("High Temperature", message);
        }
    }

    private void addNotificationToFirebase(String message, long timestamp) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notifications");
        NotificationItem notification = new NotificationItem(notificationIdCounter++, message, timestamp);
        databaseReference.child(String.valueOf(notification.getId())).setValue(notification);
    }

    private void showNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "my_channel_id";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Channel Name", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(notificationIdCounter, builder.build());
    }
}