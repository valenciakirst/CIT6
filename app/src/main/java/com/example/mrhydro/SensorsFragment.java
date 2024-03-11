package com.example.mrhydro;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mrhydro.databinding.FragmentHomeBinding;
import com.example.mrhydro.databinding.FragmentSensorsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SensorsFragment extends Fragment implements View.OnClickListener {
    private static final int UPDATE_INTERVAL = 2000;
    private static final int PERMISSION_REQUEST_CODE = 123;
    private long lastNotificationTime = 0;
    private static final long NOTIFICATION_INTERVAL = 60 * 1000;

    FragmentSensorsBinding binding;
    DatabaseReference reference;
    Handler handler = new Handler(Looper.getMainLooper());
    private String humidityValue;
    private String temperatureValue;
    private boolean isCelsius = true;
    private boolean isNotificationSentForTemperature = false;
    Switch temperatureSwitch;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSensorsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        temperatureSwitch = view.findViewById(R.id.switch2);
        CardView tempcard = view.findViewById(R.id.TempCard);
        CardView humiditycard = view.findViewById(R.id.HumidityCard);

        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.showToolbar();

        tempcard.setOnClickListener(this);
        humiditycard.setOnClickListener(this);

        temperatureSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isCelsius = isChecked;
                readTemperatureData();
                String toastMessage = isChecked ? "Switched to Celsius" : "Switched to Fahrenheit";
                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show();
                readHumidityData();
            }
        });

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
        }
    }

    private void handleMisterSwitch(boolean isChecked) {
        if (isChecked) {
            showToast("Mister turned ON");
            // Perform any other actions you need when the mister is turned ON
        } else {
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
                    double temperatureValue = isCelsius ? temperatureCelsius : celsiusToFahrenheit(temperatureCelsius);

                    Log.d("TemperatureFragment", "Temperature value from Firebase: " + temperatureValue +
                            (isCelsius ? "°C" : "°F"));

                    updateSingleTemperature(temperatureCelsius, temperatureValue);


                }
            }

            private void updateSingleTemperature(double temperatureCelsius, double temperatureValue) {
                if (binding.singleTemperatureValue != null && binding.singleTemperatureUnit != null) {
                    if (isCelsius) {
                        binding.singleTemperatureValue.setText(String.format("%.2f", temperatureCelsius));
                        binding.singleTemperatureUnit.setText("°C");
                    } else {
                        binding.singleTemperatureValue.setText(String.format("%.2f", temperatureValue));
                        binding.singleTemperatureUnit.setText("°F");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TemperatureFragment", "Failed to read temperature data", databaseError.toException());
                Toast.makeText(getContext(), "Failed to read temperature data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double celsiusToFahrenheit(double celsius) {
        return (celsius * 9 / 5) + 32;
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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
    public void onDestroyView() {
        handler.removeCallbacks(updateRunnable);
        super.onDestroyView();
    }
}