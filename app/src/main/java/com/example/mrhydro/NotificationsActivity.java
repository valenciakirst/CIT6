package com.example.mrhydro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private List<NotificationItem> notificationList;
    private NotificationAdapter adapter;
    private int notificationIdCounter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        addSampleNotifications();
    }

    private void addNotificationToFirebase(String message, long timestamp) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notifications");
        NotificationItem notification = new NotificationItem(notificationIdCounter++, message, timestamp);
        databaseReference.child(String.valueOf(notification.getId())).setValue(notification);
    }

    private void addSampleNotifications() {
        // Add sample notifications with timestamps
        addNotification("New message received", System.currentTimeMillis());
        addNotification("Reminder: Meeting at 2 PM", System.currentTimeMillis() - 100000);
        addNotification("App update available", System.currentTimeMillis() - 200000);
        // Add more notifications as needed
    }

    private void addNotification(String message, long timestamp) {
        int notificationId = notificationIdCounter++;
        NotificationItem notification = new NotificationItem(notificationId, message, timestamp);
        notificationList.add(notification);
        adapter.notifyDataSetChanged();

        // Use your existing notification creation logic here
        createNotification(notificationId, message);
    }

    private void createNotification(int notificationId, String message) {
        // Your existing notification creation logic here
    }
}
