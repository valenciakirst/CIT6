package com.example.mrhydro;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mrhydro.databinding.FragmentTemperatureBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TemperatureFragment extends HomeFragment {
    private static final int UPDATE_INTERVAL = 2000;

    FragmentTemperatureBinding binding;
    DatabaseReference reference;
    Handler handler = new Handler(Looper.getMainLooper());

    public TemperatureFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTemperatureBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        readTemperatureData();

        handler.postDelayed(updateRunnable, UPDATE_INTERVAL);

        return view;
    }

        private void readTemperatureData() {
            reference = FirebaseDatabase.getInstance().getReference("DHT");

            reference.child("Temperature in C").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.getValue() instanceof Double) {
                        double temperatureCelsius = (double) dataSnapshot.getValue();
                        double temperatureFahrenheit = celsiusToFahrenheit(temperatureCelsius);

                        Log.d("TemperatureFragment", "Temperature value from Firebase: " + temperatureCelsius + "°C");

                        if (binding.celsiusValue != null && binding.fahrenheitValue != null) {
                            binding.celsiusValue.setText(String.format("%.2f°C", temperatureCelsius));
                            binding.fahrenheitValue.setText(String.format("%.2f°F", temperatureFahrenheit));
                        }
                    }
                }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TemperatureFragment", "Failed to read temperature data", databaseError.toException());
            }
        });
    }

    private double celsiusToFahrenheit(double celsius) {
        return (celsius * 9 / 5) + 32;
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            // Fetch new data periodically
            readTemperatureData();
            // Schedule the next update
            handler.postDelayed(this, UPDATE_INTERVAL);
        }
    };

    @Override
    public void onDestroyView() {
        // Remove the callbacks to prevent memory leaks
        handler.removeCallbacks(updateRunnable);
        super.onDestroyView();
    }
}
