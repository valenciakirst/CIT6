package com.example.mrhydro;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity implements View.OnClickListener {

    private List<NotificationItem> notificationList;
    private NotificationAdapter adapter;
    private DatabaseReference temperatureRef;
    private int notificationIdCounter = 1;
    private Handler handler = new Handler();
    private Runnable notificationRunnable;
    private Double lastNotifiedTemperature = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(
                R.layout.activity_notifications);

        ImageView backBT = findViewById(R.id.backButton);
        backBT.setOnClickListener(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Intent serviceIntent = new Intent(this, TemperatureMonitorService.class);
        startService(serviceIntent);

        // Initialize Firebase reference to the temperature data
        temperatureRef = FirebaseDatabase.getInstance().getReference("DHT/TemperatureInC");

        // Monitor temperature changes in Firebase
        temperatureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double currentTemperature = snapshot.getValue(Double.class);
                    if (currentTemperature != null && currentTemperature >= 25) {
                        if (!currentTemperature.equals(lastNotifiedTemperature)) {
                            lastNotifiedTemperature = currentTemperature;
                            addNotification(currentTemperature, System.currentTimeMillis());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle any database error
            }
        });

        // Set up periodic notification every 2 minutes
        notificationRunnable = new Runnable() {
            @Override
            public void run() {
                sendPeriodicNotification();
                handler.postDelayed(this, 120000); // 120000 milliseconds = 2 minutes
            }
        };
        handler.postDelayed(notificationRunnable, 120000); // Initial delay
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backButton) {
            finish();
        }
    }

    private void addNotification(double temperature, long timestamp) {
        String message = "Temperature is High: " + temperature + "°C";

        // Create notification item
        NotificationItem notification = new NotificationItem(notificationIdCounter++, message, timestamp);

        // Save to Firebase
        addNotificationToFirebase(notification);

        // Add to local list and update UI
        notificationList.add(notification);
        adapter.notifyDataSetChanged();

        // Show notification to the user
        showNotification("High Temperature Alert", message);
    }

    private void addNotificationToFirebase(NotificationItem notification) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notificationHistory");
        databaseReference.push().setValue(notification);
    }


    private void sendPeriodicNotification() {
        String message = "Periodic Temperature Check";
        long timestamp = System.currentTimeMillis();

        // Create a notification item
        NotificationItem notification = new NotificationItem(notificationIdCounter++, message, timestamp);

        // Save to Firebase
        addNotificationToFirebase(notification);

        // Add to local list and update UI
        notificationList.add(notification);
        adapter.notifyDataSetChanged();

        // Show notification to the user
        showNotification("Regular Update", message);
    }


    private void showNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "my_channel_id";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Temperature Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for high temperature alerts");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(notificationIdCounter++, builder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(notificationRunnable);
    }
}