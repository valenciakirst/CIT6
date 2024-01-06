package com.example.mrhydro;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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

    FragmentHomeBinding binding;
    DatabaseReference reference;
    Handler handler = new Handler(Looper.getMainLooper());
    private String humidityValue;
    private String temperatureValue;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        CardView tempcard = view.findViewById(R.id.TempCard);
        CardView humiditycard = view.findViewById(R.id.HumidityCard);
        CardView mistercard = view.findViewById(R.id.MisterCard);
        ImageView notificationbt = view.findViewById(R.id.notificationIcon);

        tempcard.setOnClickListener(this);
        humiditycard.setOnClickListener(this);
        mistercard.setOnClickListener(this);
        notificationbt.setOnClickListener(this);

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
        } else if (v.getId() == R.id.notificationIcon) {
            // Start a new activity
            startActivity(new Intent(getActivity(), NotificationsActivity.class));
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
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
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
            readHumidityData();
            readTemperatureData();
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