package com.example.mrhydro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import android.widget.Toast;
public class NotificationsActivity extends AppCompatActivity {

    private List<NotificationItem> notificationList;
    private NotificationAdapter adapter;
    int notificationIdCounter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }


    // Add notification to Firebase and list
    private void addNotification(double temperature, long timestamp) {
        int notificationId = notificationIdCounter++;

        // Determine temperature level based on criteria
        String temperatureLevel;
        if (temperature > 25) {
            temperatureLevel = "High";
            // Show toast notification for high temperature
            Toast.makeText(this, "Temperature is High: " + temperature + "°C", Toast.LENGTH_SHORT).show();
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
    }

    // Store notification in Firebase
    private void addNotificationToFirebase(String message, long timestamp) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notifications");
        NotificationItem notification = new NotificationItem(notificationIdCounter++, message, timestamp);
        databaseReference.child(String.valueOf(notification.getId())).setValue(notification);

        }
    }