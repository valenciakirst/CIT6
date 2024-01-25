package com.example.mrhydro;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mrhydro.databinding.FragmentHomeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment implements View.OnClickListener {
    private static final int UPDATE_INTERVAL = 2000;
    private static final int PERMISSION_REQUEST_CODE = 123;
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder builder; // Use any unique code

    FragmentHomeBinding binding;
    DatabaseReference reference;
    Handler handler = new Handler(Looper.getMainLooper());
    private String humidityValue;
    private String temperatureValue;
    private long lastNotificationTime = 0;
    private static final long NOTIFICATION_INTERVAL = 60 * 1000; // Adjust the interval as needed (in milliseconds)

    // Flag to keep track of whether the notification has been sent for a specific temperature
    private boolean isNotificationSentForTemperature = false;
    ToggleButton misterSwitch;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();


        CardView tempcard = view.findViewById(R.id.TempCard);
        CardView humiditycard = view.findViewById(R.id.HumidityCard);
        CardView mistercard = view.findViewById(R.id.MisterCard);

        misterSwitch = view.findViewById(R.id.MisterSwitch);
        misterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                handleMisterSwitch(isChecked);
            }
        });

        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.showToolbar();

        tempcard.setOnClickListener(this);
        humiditycard.setOnClickListener(this);
        mistercard.setOnClickListener(this);

        readHumidityData();
        readTemperatureData();

        handler.postDelayed(updateRunnable, UPDATE_INTERVAL);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.TempCard) {
            openFragment(new TemperatureFragment());
        } else if (v.getId() == R.id.HumidityCard) {
            openFragment(new HumidityFragment());
        } else if (v.getId() == R.id.MisterCard) {
            openFragment(new MisterFragment());
        }
    }


    private void handleMisterSwitch(boolean isChecked) {
        if (isChecked) {
            // Mister is turned ON
            showToast("Mister turned ON");
            // Perform any other actions you need when the mister is turned ON
        } else {
            // Mister is turned OFF
            showToast("Mister turned OFF");
            // Perform any other actions you need when the mister is turned OFF
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void readHumidityData() {
        reference = FirebaseDatabase.getInstance().getReference("DHT");
        reference.child("Humidity").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() instanceof Double) {
                    humidityValue = String.valueOf(dataSnapshot.getValue());
                    Log.d("HumidityFragment", "Humidity value from Firebase: " + humidityValue);

                    if (humidityValue != null && !humidityValue.isEmpty()) {
                        binding.humidityValue.setText(humidityValue);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HumidityFragment", "Failed to read humidity data", databaseError.toException());
                Toast.makeText(getContext(), "Failed to read humidity data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readTemperatureData() {
        reference = FirebaseDatabase.getInstance().getReference("DHT");
        reference.child("Temperature in C").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() instanceof Double) {
                    double temperatureCelsius = (double) dataSnapshot.getValue();
                    double temperatureFahrenheit = celsiusToFahrenheit(temperatureCelsius);

                    Log.d("TemperatureFragment", "Temperature value from Firebase: " + temperatureCelsius + "°C");

                    if (binding.celsiusValue != null && binding.fahrenheitValue != null) {
                        binding.celsiusValue.setText(String.format("%.2f", temperatureCelsius));
                        binding.fahrenheitValue.setText(String.format("%.2f", temperatureFahrenheit));

                        if (temperatureCelsius >= 28) {
                            if (!isNotificationSentForTemperature) {
                                showTemperatureNotification(temperatureCelsius);
                                isNotificationSentForTemperature = true;
                            }
                        } else {
                            // Reset the flag when the temperature is below the threshold
                            isNotificationSentForTemperature = false;
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
                Log.e("TemperatureFragment", "Failed to read temperature data", databaseError.toException());
                Toast.makeText(getContext(), "Failed to read temperature data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTemperatureNotification(double temperature) {
        // Customize the notification message as per your requirement
        String notificationMessage = "Temperature Alert: Current temperature is " + temperature + "°C. Water mister is turning on.";

        // Create an Intent to open the MainActivity when the notification is clicked
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create a NotificationCompat.Builder to build the notification
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.logofinal);
        builder = new NotificationCompat.Builder(requireContext(), "CHANNEL_ID")
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle("MR.HYDRO")
                .setContentText(notificationMessage)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Get the NotificationManager system service
        notificationManager = NotificationManagerCompat.from(requireContext());

        // Check for permission before showing the notification
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED) {
            // You have the permission, proceed with showing the notification.
            notificationManager.notify(1, builder.build());
        } else {
            // You don't have the permission, handle it accordingly.
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.FOREGROUND_SERVICE)) {
                // Provide additional rationale to the user if needed.
                Toast.makeText(getContext(), "Permission is required to show the notification.", Toast.LENGTH_SHORT).show();
            }

            // Request the permission.
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.FOREGROUND_SERVICE}, PERMISSION_REQUEST_CODE);
        }

        // Move the following lines outside of the else block
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastNotificationTime >= NOTIFICATION_INTERVAL) {
            // Show the notification
            notificationManager.notify(1, builder.build());

            // Update the last notification time
            lastNotificationTime = currentTime;
        }
    }



    private double celsiusToFahrenheit ( double celsius){
            return (celsius * 9 / 5) + 32;
        }

        private Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                readHumidityData();
                readTemperatureData();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };

        @Override
        public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with showing the notification.
                    notificationManager.notify(1, builder.build());
                } else {
                    // Permission denied, handle it accordingly.
                    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.FOREGROUND_SERVICE)) {
                        // Provide additional rationale to the user if needed.
                        Toast.makeText(getContext(), "Permission is required to show the notification.", Toast.LENGTH_SHORT).show();
                    }

                    // Request the permission.
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.FOREGROUND_SERVICE}, PERMISSION_REQUEST_CODE);
                }
            }
        }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

        @Override
        public void onDestroyView () {
            // Remove the callbacks to prevent memory leaks
            handler.removeCallbacks(updateRunnable);
            super.onDestroyView();
        }
    }

