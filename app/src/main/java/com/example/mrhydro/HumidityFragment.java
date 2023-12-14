package com.example.mrhydro;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mrhydro.databinding.FragmentHumidityBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HumidityFragment extends HomeFragment {
    private static final int UPDATE_INTERVAL = 5000;

    FragmentHumidityBinding binding;
    DatabaseReference reference;
    Handler handler = new Handler(Looper.getMainLooper());

    public HumidityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHumidityBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        readHumidityData();

        handler.postDelayed(updateRunnable, UPDATE_INTERVAL);

        return view;
    }

    private void readHumidityData() {
        reference = FirebaseDatabase.getInstance().getReference("DHT");
        reference.child("Humidity").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() instanceof Double) {
                    String humidityValue = String.valueOf(dataSnapshot.getValue());
                    Log.d("HumidityFragment", "Humidity value from Firebase: " + humidityValue);

                    if (humidityValue != null) {
                        binding.humidityValue.setText(humidityValue);
                    }
                }
                    }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("HumidityFragment", "Failed to read humidity data", databaseError.toException());
            }
        });
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            readHumidityData();
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
